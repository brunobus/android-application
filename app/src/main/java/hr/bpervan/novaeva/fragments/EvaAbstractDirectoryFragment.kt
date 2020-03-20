package hr.bpervan.novaeva.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.content.edit
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.RecyclerView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.EvaDomainInfo
import hr.bpervan.novaeva.model.EvaNode
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.kotlin.where

/**
 *
 */
abstract class EvaAbstractDirectoryFragment : EvaBaseFragment() {


    private val handler = Handler()

    private var fetchFromServerDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }
    private var evaDirectoryChangesDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    protected var domain: EvaDomain = EvaDomain.GOSPEL
    protected var directoryId: Long = -1
    protected var directoryTitle: String = ""

    protected var fetchItems: Long = 20

    protected lateinit var adapter: RecyclerView.Adapter<*>

    protected lateinit var realm: Realm

    protected val elementsList: MutableList<EvaNode> = mutableListOf()

    protected var hasMore = true
    protected var fetchingFromServer = true
    protected var loadingFromDb = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        prefs.edit {
            remove("$HAS_NEW_CONTENT_KEY_PREFIX.$domain")
        }

        if (directoryId == -1L) {
            directoryId = realm.where<EvaDomainInfo>()
                    .equalTo("domain", domain.toString())
                    .findFirst()
                    ?.rootCategoryId
                    ?: domain.rootId
        }

        if (directoryId > 0L) {

            if (context?.networkConnectionExists() == true) {
                val lastEvictionTime = prefs
                        .getLong("$LAST_EVICTION_TIME_MILLIS_KEY_PREFIX.$domain.$directoryId", 0L)

                if (System.currentTimeMillis() - lastEvictionTime > evictionIntervalMillis) {
                    EvaDirectoryDbAdapter.deleteDirectoryContent(realm, directoryId)

                    prefs.edit {
                        putLong("$LAST_EVICTION_TIME_MILLIS_KEY_PREFIX.$domain.$directoryId", System.currentTimeMillis())
                    }
                }
            }

            createIfMissingAndSubscribeToEvaDirectoryUpdates()
        }

        if (savedInstanceState == null) {
            if (domain.isLegacy()) {
                fetchEvaDirectoryDataFromServer_legacy()
            } else {
                fetchEvaDirectoryDataFromServer()
            }
        }
    }

    protected fun createIfMissingAndSubscribeToEvaDirectoryUpdates() {
        EvaDirectoryDbAdapter.createIfMissingEvaDirectoryAsync(realm, directoryId,
                valuesApplier = {
                    it.title = directoryTitle
                },
                onSuccess = {
                    subscribeToDirectoryUpdates()
                })
    }

    protected fun subscribeToDirectoryUpdates() {
        evaDirectoryChangesDisposable = EvaDirectoryDbAdapter.subscribeToEvaDirectoryUpdatesAsync(
                realm, directoryId) { evaDirectory ->

            fillElements(evaDirectory)

            evaDirectory.image?.let { image ->
                prefs.edit {
                    putString("hr.bpervan.novaeva.categoryheader.$domain", image.url)
                }
            }

            loadingFromDb = false

            adapter.notifyDataSetChanged()
        }
    }

    abstract fun fillElements(evaDirectory: EvaDirectory)

    override fun onConfigurationChanged(newConfig: Configuration?) {
        //A HACK TO DISPLAY CORRECT FRAGMENT VIEWS WHEN SWITCHING BETWEEN PORTRAIT AND LANDSCAPE
        activity?.supportFragmentManager?.beginTransaction()?.detach(this)?.commitAllowingStateLoss()
        super.onConfigurationChanged(newConfig)
        activity?.supportFragmentManager?.beginTransaction()?.attach(this)?.commitAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    protected fun refreshLoadingCircleState() {
        view?.let {
            val loadingCircleIndex = adapter.itemCount - 1
            it.post { adapter.notifyItemChanged(loadingCircleIndex) }
        }
    }

    protected var pageOn: Long = -99

    protected fun fetchEvaDirectoryDataFromServer_legacy(timestamp: Long? = null) {
        fetchingFromServer = true
        refreshLoadingCircleState()

        fetchFromServerDisposable = NovaEvaService.v2
                .getDirectoryContent(directoryId, timestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { evaDirectoryDTO ->
                    loadingFromDb = true
                    fetchingFromServer = false

                    evaDirectoryDTO.directoryId = directoryId
                    evaDirectoryDTO.domain = domain
                    evaDirectoryDTO.title = directoryTitle

                    EvaDirectoryDbAdapter.addOrUpdateEvaDirectoryAsync_legacy(realm, evaDirectoryDTO)

                    handler.postDelayed(4000) {
                        /*if there are no actual new changes from server, data in cache will not be "updated"
                        and on update callback (in subscribeToDirectoryUpdates) will not be called,
                        resulting in progress circle spinning forever
                        This block kills loading circle after some reasonable time
                        */
                        loadingFromDb = false
                        refreshLoadingCircleState()
                    }

                    hasMore = evaDirectoryDTO.more > 0

                }, onError = {
                    Log.e("fetchDirectoryLegacy", it.message, it)
                    handler.postDelayed(2000) {
                        fetchingFromServer = false
                        refreshLoadingCircleState()

                        view?.dataErrorSnackbar()
                    }
                })
    }

    protected fun fetchEvaDirectoryDataFromServer(page: Long = 1) {
        fetchingFromServer = true
        refreshLoadingCircleState()

        fetchFromServerDisposable = NovaEvaService.v3
                .categoryContent(
                        domain = domain.domainEndpoint,
                        categoryId = directoryId,
                        page = page,
                        items = fetchItems)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { categoryDto ->
                    loadingFromDb = true
                    fetchingFromServer = false

                    if (directoryId == 0L) {
                        directoryId = categoryDto.id
                        createIfMissingAndSubscribeToEvaDirectoryUpdates()
                    }

                    EvaDirectoryDbAdapter.addOrUpdateEvaCategoryAsync(realm, categoryDto)

                    handler.postDelayed(4000) {
                        /*if there are no actual new changes from server, data in cache will not be "updated"
                        and on-update callback (in subscribeToDirectoryUpdates) will not be called,
                        resulting in progress circle spinning forever
                        This block kills loading circle after some reasonable time
                        */
                        loadingFromDb = false
                        refreshLoadingCircleState()
                    }

                    hasMore = categoryDto.pagingInfo?.hasNext ?: false
                    pageOn = categoryDto.pagingInfo?.pageNumber ?: 1

                }, onError = {
                    Log.e("fetchDirectory", it.message, it)
                    handler.postDelayed(2000) {
                        fetchingFromServer = false
                        refreshLoadingCircleState()

                        view?.dataErrorSnackbar()
                    }
                })
    }
}