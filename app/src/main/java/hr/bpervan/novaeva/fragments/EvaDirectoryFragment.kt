package hr.bpervan.novaeva.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.CacheService
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.TIMESTAMP_FIELD
import hr.bpervan.novaeva.model.TreeElementInfo
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.eva_directory.view.*
import kotlinx.android.synthetic.main.collapsing_directory_header.view.*
import kotlinx.android.synthetic.main.top_izbornik.view.*

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaDirectoryFragment : EvaBaseFragment() {

    companion object : EvaBaseFragment.EvaFragmentFactory<EvaDirectoryFragment, OpenDirectoryEvent> {

        private const val fragmentConfigKey = "fragmentConfig"

        override fun newInstance(initializer: OpenDirectoryEvent): EvaDirectoryFragment {
            return EvaDirectoryFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(fragmentConfigKey, EvaDirectoryFragment.FragmentConfig(
                            initializer.directoryMetadata.directoryId,
                            initializer.directoryMetadata.title,
                            initializer.themeId))
                }
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

    private lateinit var fragmentConfig: FragmentConfig
    private lateinit var adapter: EvaRecyclerAdapter

    private var elementsList: MutableList<TreeElementInfo> = mutableListOf()
    private var hasMore = true
    private var loadingFromDb = true

    private var fetchingFromServer = true

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: arguments!!
        fragmentConfig = inState.getParcelable(fragmentConfigKey)
        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        adapter = EvaRecyclerAdapter(elementsList, { loadingFromDb || fetchingFromServer }, fragmentConfig.themeId)
        adapter.registerAdapterDataObserver(DataChangeLogger())

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Direktorij")
                        .setAction("OtvorenDirektorij")
                        .setLabel(fragmentConfig.directoryTitle)
                        .setValue(fragmentConfig.directoryId)
                        .build())

        createIfMissingAndSubscribeToEvaDirectoryUpdates()

        if (savedInstanceState == null) {
            fetchEvaDirectoryDataFromServer()
        }
    }

    private fun createIfMissingAndSubscribeToEvaDirectoryUpdates() {
        EvaDirectoryDbAdapter.createIfMissingEvaDirectoryAsync(realm, fragmentConfig.directoryId, {
            it.title = fragmentConfig.directoryTitle
        }) {
            subscribeToDirectoryUpdates()
        }
    }

    private fun subscribeToDirectoryUpdates() {
        evaDirectoryChangesDisposable = EvaDirectoryDbAdapter.subscribeToEvaDirectoryUpdatesAsync(
                realm, fragmentConfig.directoryId, { evaDirectory ->

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

//    private fun createAdapter(contentMetadataList: RealmList<EvaContentMetadata>,
//                              subDirectoryMetadataList: RealmList<EvaDirectoryMetadata>): EvaRecyclerAdapter {
//        val infoText = if (fragmentConfig.isSubDirectory) "NALAZITE SE U MAPI" else "NALAZITE SE U KATEGORIJI"
//
//        //FIXME Create new adapter class that efficiently uses TWO OBSERVABLE REALMLISTS and has a header and loadingCircle
//        adapter = EvaRecyclerAdapter(contentMetadataList,
//                EvaRecyclerAdapter.HeaderData(fragmentConfig.directoryName, infoText), { loading })
//        adapter.registerAdapterDataObserver(DataChangeLogger())
//        return adapter
//    }

    class FragmentConfig(val directoryId: Long,
                         val directoryTitle: String,
                         val themeId: Int) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readLong(),
                parcel.readString(),
                parcel.readInt())

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeLong(directoryId)
            dest.writeString(directoryTitle)
            dest.writeInt(themeId)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<FragmentConfig> {
            override fun createFromParcel(parcel: Parcel): FragmentConfig = FragmentConfig(parcel)
            override fun newArray(size: Int): Array<FragmentConfig?> = arrayOfNulls(size)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("fragmentConfig", FragmentConfig(
                fragmentConfig.directoryId,
                fragmentConfig.directoryTitle,
                fragmentConfig.themeId
        ))

        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflaterToUse =
                if (fragmentConfig.themeId != -1) {
                    inflater.cloneInContext(ContextThemeWrapper(activity, fragmentConfig.themeId))
                } else {
                    inflater
                }
        return inflaterToUse.inflate(R.layout.eva_directory, container, false).apply {
            val infoText = "NALAZITE SE U KATEGORIJI"

            evaDirectoryCollapsingBar.izbornikTop.izbornikTopNatpis.apply {
                text = infoText
                typeface = NovaEvaApp.openSansBold
            }
            evaDirectoryCollapsingBar.izbornikTop.izbornikTopNazivKategorije.apply {
                text = fragmentConfig.directoryTitle
                typeface = NovaEvaApp.openSansBold
            }

            val recyclerView = evaRecyclerView as RecyclerView

            val linearLayoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = adapter
            recyclerView.addOnScrollListener(EndlessScrollListener(linearLayoutManager))
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

                    /** Ako je zadnji u listi podkategorija, onda on nema UnixDatum, pa tražimo zadnji koji ima */

                    fetchingFromServer = true
                    refreshLoadingCircleState()

                    EvaDirectoryDbAdapter.loadEvaDirectoryAsync(realm, fragmentConfig.directoryId, { evaDirectory ->
                        if (evaDirectory != null) {
                            val oldestTimestamp = evaDirectory.contentMetadataList.sort(TIMESTAMP_FIELD, Sort.DESCENDING).lastOrNull()?.timestamp
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

        fetchFromServerDisposable = NovaEvaService.instance
                .getDirectoryContent(fragmentConfig.directoryId, timestamp)
                .subscribeAsync({ evaDirectoryDTO ->
                    loadingFromDb = true
                    fetchingFromServer = false

                    CacheService.cache(realm, evaDirectoryDTO, fragmentConfig.directoryId)

                    handler.postDelayed({
                        /*if there are no actual new changes from server, data in cache will not be "updated"
                        and on update callback (in subscribeToDirectoryUpdates) will not be called,
                        resulting in progress circle spinning forever
                        This block kills loading circle after some reasonable time
                        */
                        loadingFromDb = false
                        refreshLoadingCircleState()
                    }, 4000)

                    hasMore = evaDirectoryDTO.more > 0

                }) {
                    handler.postDelayed({
                        fetchingFromServer = false
                        refreshLoadingCircleState()
                    }, 2000)
                    context?.let { ctx ->
                        NovaEvaApp.showFetchErrorSnackbar(it, ctx, view)
                    }
                }
    }
}