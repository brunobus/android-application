package hr.bpervan.novaeva.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hr.bpervan.novaeva.CacheService
import hr.bpervan.novaeva.NovaEvaApp
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

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaRecyclerFragment : Fragment() {

    private var fetchFromServerDisposable: Disposable? = null
    private var evaDirectoryChangesDisposable: Disposable? = null

    private lateinit var fragmentConfig: FragmentConfig
    private lateinit var headerData: EvaRecyclerAdapter.HeaderData
    private lateinit var adapter: EvaRecyclerAdapter
    private var elementsList: MutableList<TreeElementInfo> = ArrayList()

    private var hasMore = true
    private var loading = true

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: arguments
        fragmentConfig = inState.getParcelable("fragmentConfig")

        realm = Realm.getInstance(RealmConfigProvider.cacheConfig)

        val infoText = if (fragmentConfig.isSubDirectory) "NALAZITE SE U MAPI" else "NALAZITE SE U KATEGORIJI"
        headerData = EvaRecyclerAdapter.HeaderData(fragmentConfig.directoryTitle, infoText)

        adapter = EvaRecyclerAdapter(elementsList,
                EvaRecyclerAdapter.ConfigData(fragmentConfig.colourSet, { loading }),
                headerData)
        adapter.registerAdapterDataObserver(DataChangeLogger())

        subscribeToDirectoryUpdates()
        fetchDirectoryDataFromServer()
    }

    private fun subscribeToDirectoryUpdates() {
        evaDirectoryChangesDisposable?.dispose()
        evaDirectoryChangesDisposable = EvaDirectoryDbAdapter.subscribeToEvaDirectoryUpdatesAsync(
                realm, fragmentConfig.directoryId, { evaDirectory ->

            headerData.directoryName = evaDirectory.directoryMetadata!!.title

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
            loading = false

            adapter.notifyDataSetChanged()
        })
    }

//    private fun createAdapter(contentMetadataList: RealmList<EvaContentMetadata>,
//                              subDirectoryMetadataList: RealmList<EvaDirectoryMetadata>): EvaRecyclerAdapter {
//        val infoText = if (fragmentConfig.isSubDirectory) "NALAZITE SE U MAPI" else "NALAZITE SE U KATEGORIJI"
//
//        //FIXME Create new adapter class that efficiently uses TWO OBSERVABLE REALMLISTS and has a header and loadingCircle
//        adapter = EvaRecyclerAdapter(contentMetadataList,
//                EvaRecyclerAdapter.ConfigData(fragmentConfig.colourSet, { loading }),
//                EvaRecyclerAdapter.HeaderData(fragmentConfig.directoryName, infoText))
//        adapter.registerAdapterDataObserver(DataChangeLogger())
//        return adapter
//    }

    class FragmentConfig(val directoryId: Long,
                         val directoryTitle: String,
                         val isSubDirectory: Boolean,
                         val colourSet: Int) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readLong(),
                parcel.readString(),
                parcel.readByte() != 0.toByte(),
                parcel.readInt())

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeLong(directoryId)
            dest.writeString(directoryTitle)
            dest.writeByte(if (isSubDirectory) 1.toByte() else 0.toByte())
            dest.writeInt(colourSet)
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
                fragmentConfig.isSubDirectory,
                fragmentConfig.colourSet
        ))

        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val recyclerView = inflater.inflate(R.layout.eva_recycler_view, container, false) as RecyclerView

        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(EndlessScrollListener(linearLayoutManager))

        //todo use this when new adapter class that uses two realmlists is created
//        EvaDirectoryDbAdapter.loadEvaDirectoryAsync(realm, fragmentConfig.directoryId) { evaDirectory ->
//            if (evaDirectory != null) {
//                recyclerView.adapter = createAdapter(evaDirectory.contentMetadataList, evaDirectory.subDirectoryMetadataList)
//            }
//        }

        return recyclerView
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        //A HACK TO DISPLAY CORRECT FRAGMENT VIEWS WHEN SWITCHING BETWEEN PORTRAIT AND LANDSCAPE
        activity.supportFragmentManager.beginTransaction().detach(this).commitAllowingStateLoss()
        super.onConfigurationChanged(newConfig)
        activity.supportFragmentManager.beginTransaction().attach(this).commitAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        evaDirectoryChangesDisposable?.dispose()
        fetchFromServerDisposable?.dispose()
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
                if (!loading && hasMore && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {

                    /** Ako je zadnji u listi podkategorija, onda on nema UnixDatum, pa tražimo zadnji koji ima */

                    loading = true
                    safeRefreshLoadingCircle()
                    val progressBarIndex = adapter.itemCount - 1
                    recyclerView.post { adapter.notifyItemChanged(progressBarIndex) }

                    EvaDirectoryDbAdapter.loadEvaDirectoryAsync(realm, fragmentConfig.directoryId, { evaDirectory ->
                        if (evaDirectory != null) {
                            val oldestTimestamp = evaDirectory.contentMetadataList.sort(TIMESTAMP_FIELD, Sort.DESCENDING).lastOrNull()?.timestamp
                            fetchDirectoryDataFromServer(oldestTimestamp)
                        }
                    })
                }
            }
        }
    }

    private fun safeRefreshLoadingCircle() {
        view?.let {
            val progressBarIndex = adapter.itemCount - 1
            it.post { adapter.notifyItemChanged(progressBarIndex) }
        }
    }

    private fun fetchDirectoryDataFromServer(timestamp: Long? = null) {
        loading = true
        safeRefreshLoadingCircle()

        fetchFromServerDisposable?.dispose()
        fetchFromServerDisposable = NovaEvaService.instance
                .getDirectoryContent(fragmentConfig.directoryId, timestamp)
                .subscribeAsync({ evaDirectoryDTO ->
                    CacheService.cache(realm, evaDirectoryDTO, fragmentConfig.directoryId)

                    hasMore = evaDirectoryDTO.more > 0

                    loading = false
                }) {
                    loading = false
                    NovaEvaApp.showErrorSnackbar(it, context, view)
                    safeRefreshLoadingCircle()
                }
    }
}