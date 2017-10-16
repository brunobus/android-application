package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.MenuElementAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.ContentInfo
import hr.bpervan.novaeva.model.TreeElementInfo
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.LoadableFromBundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by vpriscan on 08.10.17..
 */
class MenuRecyclerFragment : Fragment(), LoadableFromBundle {

    private var hasMore = true
    private var menuElementsDisposable: Disposable? = null

    private lateinit var fragmentConfig: FragmentConfig
    private lateinit var adapter: MenuElementAdapter
    private var elementsList: MutableList<TreeElementInfo> = ArrayList()

    private var loading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            loadStateFromBundle(savedInstanceState)
        } else {
            loadStateFromBundle(arguments)
        }

        val infoText = if (fragmentConfig.isSubDirectory) "NALAZITE SE U MAPI" else "NALAZITE SE U KATEGORIJI"

        adapter = MenuElementAdapter(elementsList,
                MenuElementAdapter.ConfigData(fragmentConfig.colourSet, { loading }),
                MenuElementAdapter.HeaderData(fragmentConfig.directoryName, infoText))
        adapter.registerAdapterDataObserver(DataChangeLogger())


        loadData()
    }

    class FragmentConfig(val directoryId: Long,
                         val directoryName: String,
                         val isSubDirectory: Boolean,
                         val colourSet: Int) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readLong(),
                parcel.readString(),
                parcel.readByte() != 0.toByte(),
                parcel.readInt())

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeLong(directoryId)
            dest.writeString(directoryName)
            dest.writeByte(if (isSubDirectory) 1.toByte() else 0.toByte())
            dest.writeInt(colourSet)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<FragmentConfig> {
            override fun createFromParcel(parcel: Parcel): FragmentConfig = FragmentConfig(parcel)
            override fun newArray(size: Int): Array<FragmentConfig?> = arrayOfNulls(size)
        }
    }

    override fun loadStateFromBundle(bundle: Bundle) {
        fragmentConfig = bundle.getParcelable("fragmentConfig")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("fragmentConfig", FragmentConfig(
                fragmentConfig.directoryId,
                fragmentConfig.directoryName,
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
        menuElementsDisposable?.dispose()
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
                    val zadnjiDatum = elementsList
                            .filter { it is ContentInfo }
                            .map { it as ContentInfo }
                            .lastOrNull()
                            ?.datetime

                    loading = true
                    val progressBarIndex = adapter.itemCount - 1
                    recyclerView.post {
                        adapter.notifyItemChanged(progressBarIndex)
                    }
                    loadData(date = zadnjiDatum)
                }
            }
        }
    }

    private fun showErrorPopup() {
        activity?.let { activity ->
            val error = AlertDialog.Builder(activity)
            error.setTitle("Greška")

            val tv = TextView(activity)
            tv.text = "Greška pri dohvaćanju podataka sa poslužitelja"
            tv.typeface = NovaEvaApp.openSansRegular
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            error.setView(tv)

            error.setPositiveButton("Pokušaj ponovno") { dialog, which ->
                loadData()
            }
            error.setNegativeButton("Povratak") { dialog, whichButton ->
                NovaEvaApp.goHome(context)
            }
            error.show()
        }
    }

    private fun loadData(date: String? = null) {
        menuElementsDisposable?.dispose()
        menuElementsDisposable = NovaEvaService.instance.getDirectoryContent(fragmentConfig.directoryId, date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    if (result.contentInfoList != null) {
                        elementsList.addAll(result.contentInfoList)
                    }
                    if (result.subDirectoryInfoList != null) {
                        elementsList.addAll(result.subDirectoryInfoList)
                    }

                    hasMore = result.more > 0
                    loading = false
                    adapter.notifyDataSetChanged()
                }, { t ->
                    Log.e("listElementError", t.message, t)
                    showErrorPopup()
                })
    }
}