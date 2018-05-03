package hr.bpervan.novaeva.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
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
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.util.EvaCache
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.networkRequest
import hr.bpervan.novaeva.views.snackbar
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

        private const val directoryIdKey = "directoryId"
        private const val directoryTitleKey = "directoryTitle"
        private const val themeIdKey = "themeId"

        override fun newInstance(initializer: OpenDirectoryEvent): EvaDirectoryFragment {
            return EvaDirectoryFragment().apply {
                arguments = bundleOf(
                        directoryIdKey to initializer.directoryMetadata.directoryId,
                        directoryTitleKey to initializer.directoryMetadata.title,
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

    private var loadEvaDirectoryDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private var directoryId: Long = -1
    private lateinit var directoryTitle: String
    private var themeId: Int = -1

    private lateinit var adapter: EvaRecyclerAdapter

    private val elementsList: MutableList<TreeElementInfo> = mutableListOf()
    private var hasMore = true
    private var fetchingFromServer = true
    private var loadingFromDb = true

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: arguments!!
        directoryId = inState.getLong(directoryIdKey)
        directoryTitle = inState.getString(directoryTitleKey)
        themeId = inState.getInt(themeIdKey)

        adapter = EvaRecyclerAdapter(elementsList, { loadingFromDb || fetchingFromServer }, themeId)
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
            remove("newContentInCategory$directoryId")
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
                realm, directoryId, { evaDirectory ->

            elementsList.clear()
            elementsList.addAll(evaDirectory.contentMetadataList)
            elementsList.addAll(evaDirectory.subDirectoryMetadataList)

            //FIXME ON SERVER: ADD TIMESTAMP TO DIRECTORIES
            elementsList.sortByDescending {
                when (it) {
                    is EvaContentMetadata -> it.timestamp
                    else -> 0L
                }
            }

            loadingFromDb = false

            adapter.notifyDataSetChanged()
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
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

        initUI()
    }

    private fun initUI() {
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

                    /** Ako je zadnji u listi podkategorija, onda on nema UnixDatum, pa tražimo zadnji koji ima */

                    fetchingFromServer = true
                    refreshLoadingCircleState()

                    loadEvaDirectoryDisposable = EvaDirectoryDbAdapter.loadEvaDirectoryAsync(realm, directoryId, { evaDirectory ->
                        if (evaDirectory != null) {
                            val oldestTimestamp = evaDirectory.contentMetadataList.sort(TIMESTAMP_FIELD, Sort.DESCENDING)
                                    .lastOrNull()?.timestamp
                            fetchEvaDirectoryDataFromServer(oldestTimestamp)
                        }
                    })
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

    private fun fetchEvaDirectoryDataFromServer(timestamp: Long? = null) {
        fetchingFromServer = true
        refreshLoadingCircleState()

        fetchFromServerDisposable = novaEvaService.getDirectoryContent(directoryId, timestamp)
                .networkRequest({ evaDirectoryDTO ->
                    loadingFromDb = true
                    fetchingFromServer = false

                    evaDirectoryDTO.directoryId = directoryId //todo fix on server

                    EvaCache.cache(realm, evaDirectoryDTO)

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

                        view?.snackbar(R.string.error_fetching_data, Snackbar.LENGTH_LONG)
                    }
                })
    }
}