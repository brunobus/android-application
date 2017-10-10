package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import hr.bpervan.novaeva.activities.DashboardActivity
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.ContentInfo
import hr.bpervan.novaeva.model.DirectoryInfo
import hr.bpervan.novaeva.model.TreeElementInfo
import hr.bpervan.novaeva.adapters.MenuElementAdapter
import hr.bpervan.novaeva.model.DirectoryContent
import hr.bpervan.novaeva.services.NovaEvaService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by vpriscan on 08.10.17..
 */
class MenuRecyclerFragment : Fragment() {

    private var directoryId: Long = -1
    private lateinit var directoryName: String
    private var isSubDirectory: Boolean = false
    private var colourSet: Int = -1
    private var loading: AtomicBoolean = AtomicBoolean(true)

    private var hasMore = true
    private var menuElementsDisposable: Disposable? = null

    private lateinit var adapter: MenuElementAdapter

    private val elementsList = ArrayList<TreeElementInfo>()
    private lateinit var metadata: MenuElementAdapter.Metadata

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            directoryId = it.getLong("directoryId", -1)
            directoryName = it.getString("directoryName", "")
            isSubDirectory = it.getBoolean("isSubDirectory")
            colourSet = it.getInt("colourSet", 7)
        }

        metadata = MenuElementAdapter.Metadata(directoryId, directoryName,
                if (isSubDirectory) "NALAZITE SE U MAPI" else "NALAZITE SE U KATEGORIJI",
                resources.configuration.orientation, colourSet, loading)

        adapter = MenuElementAdapter(elementsList, metadata)
        adapter.registerAdapterDataObserver(DataChangeLogger())

        loadListElements()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_evamenu, container, false) as RecyclerView

        val linearLayoutManager = LinearLayoutManager(view.context)
        view.layoutManager = linearLayoutManager
        view.itemAnimator = DefaultItemAnimator()
        view.adapter = adapter
        view.addOnScrollListener(EndlessScrollListener(linearLayoutManager))

        return view
    }

    class DataChangeLogger : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            Log.d("recyclerDataChanged", "RecyclerView data changed")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        menuElementsDisposable?.dispose()
    }

    inner class EndlessScrollListener(private val linearLayoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
        private val visibleThreshold = 2

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val visibleItemCount = recyclerView.childCount
            val totalItemCount = linearLayoutManager.itemCount
            val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

            if (firstVisibleItem > 0 && visibleItemCount > 0 && totalItemCount > 0) {
                if (!loading.get() && hasMore && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {

                    /** Ako je zadnji u listi podkategorija, onda on nema UnixDatum, pa tražimo zadnji koji ima */
                    val zadnjiDatum = elementsList
                            .filter { it is ContentInfo }
                            .map { it as ContentInfo }
                            .lastOrNull()
                            ?.datetime

                    loading.set(true)
                    val progressBarIndex = adapter.itemCount - 1
                    recyclerView.post {
                        adapter.notifyItemChanged(progressBarIndex)
                    }
                    loadListElements(zadnjiDatum)
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
                loadListElements()
            }
            error.setNegativeButton("Povratak") { dialog, whichButton ->
                startActivity(Intent(activity, DashboardActivity::class.java))
                activity.finish()
            }
            error.show()
        }
    }

    private fun loadListElements(date: String? = null) {
        menuElementsDisposable?.dispose()
        menuElementsDisposable = NovaEvaService.instance.getDirectoryContent(directoryId, date)
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
                    loading.set(false)
                    adapter.notifyDataSetChanged()

                }, { t ->
                    Log.e("listElementError", t.message, t)
                    showErrorPopup()
                })
    }

    companion object {

        fun newInstance(args: Bundle): MenuRecyclerFragment {
            val fragment = MenuRecyclerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}