package hr.bpervan.novaeva.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaDomainInfo
import hr.bpervan.novaeva.model.EvaNode
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.TIMESTAMP_FIELD
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
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.collapsing_directory_header.view.*
import kotlinx.android.synthetic.main.fragment_directory_contents.*
import kotlinx.android.synthetic.main.top_izbornik.view.*

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaDirectoryFragment : EvaBaseFragment() {

    companion object : EvaBaseFragment.EvaFragmentFactory<EvaDirectoryFragment, OpenDirectoryEvent> {

        override fun newInstance(initializer: OpenDirectoryEvent): EvaDirectoryFragment {
            return EvaDirectoryFragment().apply {
                arguments = bundleOf(
                        EvaFragmentFactory.INITIALIZER to initializer
                )
            }
        }
    }


    private val handler = Handler()

    private var fetchFromServerDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }
    private var evaDirectoryChangesDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private lateinit var initializer: OpenDirectoryEvent
    private lateinit var domain: EvaDomain
    private var directoryId: Long = -1
    private lateinit var directoryTitle: String
    private var themeId: Int = -1

    private lateinit var adapter: EvaRecyclerAdapter

    private val elementsList: MutableList<EvaNode> = mutableListOf()
    private var hasMore = true
    private var fetchingFromServer = true
    private var loadingFromDb = true

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: arguments!!

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        initializer = inState.getParcelable(EvaFragmentFactory.INITIALIZER)!!

        domain = initializer.domain
        directoryId = initializer.directoryId

        if (directoryId == -1L) {
            directoryId = realm.where<EvaDomainInfo>()
                    .equalTo("domain", domain.toString())
                    .findFirst()
                    ?.rootCategoryId
                    ?: domain.rootId
        }

        directoryTitle = initializer.title
        themeId = initializer.theme

        adapter = EvaRecyclerAdapter(elementsList, { loadingFromDb || fetchingFromServer }, themeId)
        adapter.registerAdapterDataObserver(DataChangeLogger())

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Direktorij")
                        .setAction("OtvorenDirektorij")
                        .setLabel(directoryTitle)
                        .setValue(directoryId)
                        .build())

        prefs.edit {
            remove("$HAS_NEW_CONTENT_KEY_PREFIX.$domain")
        }

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

        if (directoryId > 0L) {
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

    private fun createIfMissingAndSubscribeToEvaDirectoryUpdates() {
        EvaDirectoryDbAdapter.createIfMissingEvaDirectoryAsync(realm, directoryId,
                valuesApplier = {
                    it.title = directoryTitle
                },
                onSuccess = {
                    subscribeToDirectoryUpdates()
                })
    }

    private fun subscribeToDirectoryUpdates() {
        evaDirectoryChangesDisposable = EvaDirectoryDbAdapter.subscribeToEvaDirectoryUpdatesAsync(
                realm, directoryId) { evaDirectory ->

            elementsList.clear()

            val contentSorted = evaDirectory.contents.sortedByDescending { it.created }

            val subdirectoriesSorted = evaDirectory.subCategories//.sortedByDescending { todo ON SERVER }

            if (contentSorted.size > 10) {
                elementsList.addAll(contentSorted.take(10))
                elementsList.addAll(subdirectoriesSorted)
                elementsList.addAll(contentSorted.drop(10))
            } else {
                elementsList.addAll(contentSorted)
                elementsList.addAll(subdirectoriesSorted)
            }

            evaDirectory.image?.let { image ->
                prefs.edit {
                    putString("hr.bpervan.novaeva.categoryheader.$domain", image.url)
                }
            }

            loadingFromDb = false

            adapter.notifyDataSetChanged()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EvaFragmentFactory.INITIALIZER, initializer)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflaterToUse =
                if (themeId != -1) inflater.cloneInContext(ContextThemeWrapper(activity, themeId))
                else inflater
        return inflaterToUse.inflate(R.layout.fragment_directory_contents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        evaDirectoryCollapsingBar.izbornikTop.izbornikTopNazivKategorije.apply {
            text = directoryTitle
            typeface = NovaEvaApp.openSansBold
        }

        val recyclerView = evaRecyclerView as androidx.recyclerview.widget.RecyclerView

        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(EndlessScrollListener(linearLayoutManager))

        when (domain) {
            EvaDomain.VOCATION -> {
                btnPoziv.isVisible = true
                btnPoziv.setOnClickListener {
                    sendEmailIntent(context,
                            subject = getString(R.string.thinking_of_vocation),
                            text = getString(R.string.mail_preamble_praise_the_lord)
                                    + getString(R.string.mail_intro_vocation),
                            receiver = getString(R.string.vocation_email))
                }
            }
            EvaDomain.ANSWERS -> {
                btnPitanje.isVisible = true
                btnPitanje.setOnClickListener {
                    sendEmailIntent(context,
                            subject = getString(R.string.having_a_question),
                            text = getString(R.string.mail_preamble_praise_the_lord),
                            receiver = getString(R.string.answers_email))
                }
            }
            else -> {/*nothing*/
            }
        }
    }

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

    class DataChangeLogger : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            Log.d("recyclerDataChanged", "RecyclerView data changed")
        }
    }

    inner class EndlessScrollListener(private val linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager) : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        private val visibleThreshold = 2

        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            val visibleItemCount = recyclerView.childCount
            val totalItemCount = linearLayoutManager.itemCount
            val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

            if (firstVisibleItem > 0 && visibleItemCount > 0 && totalItemCount > 0) {
                if (!fetchingFromServer && hasMore && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {

                    fetchingFromServer = true
                    refreshLoadingCircleState()

                    if (domain.isLegacy()) {
                        disposables += EvaDirectoryDbAdapter.loadEvaDirectoryAsync(realm, directoryId) { evaDirectory ->
                            if (evaDirectory != null) {
                                val oldestTimestamp = evaDirectory.contents.sort(TIMESTAMP_FIELD, Sort.DESCENDING)
                                        .lastOrNull()?.created?.let { it / 1000 }
                                fetchEvaDirectoryDataFromServer_legacy(oldestTimestamp)
                            }
                        }
                    } else {
                        fetchEvaDirectoryDataFromServer(pageOn + 1)
                    }
                }
            }
        }
    }

    private fun refreshLoadingCircleState() {
        view?.let {
            val loadingCircleIndex = adapter.itemCount - 1
            it.post { adapter.notifyItemChanged(loadingCircleIndex) }
        }
    }

    private var pageOn: Long = -99

    private fun fetchEvaDirectoryDataFromServer_legacy(timestamp: Long? = null) {
        fetchingFromServer = true
        refreshLoadingCircleState()

        fetchFromServerDisposable = NovaEvaService.v2.getDirectoryContent(directoryId, timestamp)
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

    private fun fetchEvaDirectoryDataFromServer(page: Long = 1) {
        fetchingFromServer = true
        refreshLoadingCircleState()

        fetchFromServerDisposable = NovaEvaService.v3.categoryContent(
                domain = domain.domainEndpoint,
                categoryId = directoryId,
                page = page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { categoryDto ->
                    loadingFromDb = true
                    fetchingFromServer = false

                    categoryDto.domain = domain

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