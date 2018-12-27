package hr.bpervan.novaeva.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import hr.bpervan.novaeva.model.EvaNode
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.TIMESTAMP_FIELD
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.rest.region
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.*
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.collapsing_directory_header.view.*
import kotlinx.android.synthetic.main.fragment_directory_contents.*
import kotlinx.android.synthetic.main.top_izbornik.view.*

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaDirectoryFragment : EvaBaseFragment() {

    companion object : EvaBaseFragment.EvaFragmentFactory<EvaDirectoryFragment, OpenDirectoryEvent> {

        private const val domainKey = "domain"
        private const val directoryIdKey = "id"
        private const val directoryTitleKey = "directoryTitle"
        private const val themeIdKey = "themeId"

        override fun newInstance(initializer: OpenDirectoryEvent): EvaDirectoryFragment {
            return EvaDirectoryFragment().apply {
                arguments = bundleOf(
                        domainKey to initializer.domain,
                        directoryIdKey to initializer.directory.id,
                        directoryTitleKey to initializer.directory.title,
                        themeIdKey to initializer.themeId
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

    private lateinit var domain: EvaDomain
    private var directoryId: Long = 1
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
        domain = inState.getSerializable(domainKey) as EvaDomain
        directoryId = inState.getLong(directoryIdKey)
        directoryTitle = inState.getString(directoryTitleKey)
        themeId = inState.getInt(themeIdKey)

        adapter = EvaRecyclerAdapter(domain, elementsList, { loadingFromDb || fetchingFromServer }, themeId)
        adapter.registerAdapterDataObserver(DataChangeLogger())

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Direktorij")
                        .setAction("OtvorenDirektorij")
                        .setLabel(directoryTitle)
                        .setValue(directoryId)
                        .build())

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        prefs.edit {
            remove("$NEW_CONTENT_KEY_PREFIX$directoryId")
        }

        if (context?.networkConnectionExists() == true) {
            val lastEvictionTime = prefs.getLong(LAST_EVICTION_TIME_MILLIS_KEY + directoryId, 0L)
            if (System.currentTimeMillis() - lastEvictionTime > evictionIntervalMillis) {
                EvaDirectoryDbAdapter.deleteDirectoryContent(realm, directoryId)

                prefs.edit {
                    putLong(LAST_EVICTION_TIME_MILLIS_KEY + directoryId, System.currentTimeMillis())
                }
            }
        }

        createIfMissingAndSubscribeToEvaDirectoryUpdates()

        if (savedInstanceState == null) {
            fetchEvaDirectoryDataFromServer()
        }
    }

    private fun createIfMissingAndSubscribeToEvaDirectoryUpdates() {
        EvaDirectoryDbAdapter.createIfMissingEvaDirectoryAsync(realm, directoryId, {
            it.title = directoryTitle
        }) {
            subscribeToDirectoryUpdates()
        }
    }

    private fun subscribeToDirectoryUpdates() {
        evaDirectoryChangesDisposable = EvaDirectoryDbAdapter.subscribeToEvaDirectoryUpdatesAsync(
                realm, directoryId) { evaDirectory ->

            elementsList.clear()

            val contentSorted = evaDirectory.contents.sortedByDescending { it.timestamp }

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
        outState.putSerializable(domainKey, domain)
        outState.putLong(directoryIdKey, directoryId)
        outState.putString(directoryTitleKey, directoryTitle)
        outState.putInt(themeIdKey, themeId)

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

        val recyclerView = evaRecyclerView as RecyclerView

        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(EndlessScrollListener(linearLayoutManager))

        when (domain) {
            EvaDomain.VOCATION -> {
                btnPoziv.isVisible = true
                btnPoziv.setOnClickListener {
                    sendEmailIntent(context,
                            subject = getString(R.string.thinking_of_vocation),
                            text = "Hvaljen Isus i Marija, javljam vam se jer razmišljam o duhovnom pozivu.",
                            receiver = getString(R.string.vocation_email))
                }
            }
            EvaDomain.ANSWERS -> {
                btnPitanje.isVisible = true
                btnPitanje.setOnClickListener {
                    sendEmailIntent(context,
                            subject = getString(R.string.having_a_question),
                            text = getString(R.string.praise_the_lord),
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

    class DataChangeLogger : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            Log.d("recyclerDataChanged", "RecyclerView data changed")
        }
    }

    inner class EndlessScrollListener(private val linearLayoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
        private val visibleThreshold = 2

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val visibleItemCount = recyclerView.childCount
            val totalItemCount = linearLayoutManager.itemCount
            val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

            if (firstVisibleItem > 0 && visibleItemCount > 0 && totalItemCount > 0) {
                if (!fetchingFromServer && hasMore && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {

                    fetchingFromServer = true
                    refreshLoadingCircleState()

                    if (domain == EvaDomain.VOCATION) {
                        fetchEvaDirectoryDataFromServer(pageOn + 1)
                    } else {
                        disposables += EvaDirectoryDbAdapter.loadEvaDirectoryAsync(realm, directoryId) { evaDirectory ->
                            if (evaDirectory != null) {
                                val oldestTimestamp = evaDirectory.contents.sort(TIMESTAMP_FIELD, Sort.DESCENDING)
                                        .lastOrNull()?.timestamp
                                fetchEvaDirectoryDataFromServer_legacy(oldestTimestamp)
                            }
                        }
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
                .networkRequest({ evaDirectoryDTO ->
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
                domain.domainEndpoint, directoryId, page, region.id)
                .networkRequest({ categoryDto ->
                    loadingFromDb = true
                    fetchingFromServer = false

                    categoryDto.domain = domain

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

                    hasMore = page < categoryDto.totalPages

                    pageOn = page
                }, onError = {
                    handler.postDelayed(2000) {
                        fetchingFromServer = false
                        refreshLoadingCircleState()

                        view?.dataErrorSnackbar()
                    }
                })
    }
}