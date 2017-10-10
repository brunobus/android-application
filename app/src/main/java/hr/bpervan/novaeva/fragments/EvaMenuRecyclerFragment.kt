package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
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
import hr.bpervan.novaeva.model.TreeElement
import hr.bpervan.novaeva.services.getMenuElements
import hr.bpervan.novaeva.utilities.MenuItemAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaMenuRecyclerFragment : Fragment() {

    private var directoryId: Long = -1
    private lateinit var directoryName: String
    private var isSubDirectory: Boolean = false
    private var colourSet: Int = -1
    private var loading: AtomicBoolean = AtomicBoolean(true)

    private var hasMore = true
    private var menuElementsDisposable: Disposable? = null

    private lateinit var adapter: MenuItemAdapter

    private val elementsList = ArrayList<TreeElement>()
    private lateinit var metadata: MenuItemAdapter.Metadata

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            directoryId = it.getLong("directoryId", -1)
            directoryName = it.getString("directoryName", "")
            isSubDirectory = it.getBoolean("isSubDirectory")
            colourSet = it.getInt("colourSet", 7)
        }

        metadata = MenuItemAdapter.Metadata(directoryId, directoryName,
                if (isSubDirectory) "NALAZITE SE U MAPI" else "NALAZITE SE U KATEGORIJI",
                resources.configuration.orientation, colourSet, loading)

        adapter = MenuItemAdapter(elementsList, metadata)
        adapter.registerAdapterDataObserver(DataChangeLogger())

        loadListElements()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_evamenu, container, false) as RecyclerView

        val linearLayoutManager = LinearLayoutManager(view.context)
        view.layoutManager = linearLayoutManager
        view.itemAnimator = DefaultItemAnimator()
//        val dividerItemDecoration = DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL)
//        view.addItemDecoration(dividerItemDecoration)

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
        menuElementsDisposable = getMenuElements(directoryId.toString(), date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<String> { result ->
                    val jObj: JSONObject
                    try {
                        jObj = JSONObject(result)
                    } catch (e: JSONException) {
                        Log.e("JSON Parser", "Greska u parsiranju JSONa " + e.toString())
                        return@Consumer
                    }

                    extractMetadataFromJson(jObj)
                    val listElements = parseTreeElementsJson(jObj)

                    elementsList.addAll(listElements)
                    loading.set(false)
                    adapter.notifyDataSetChanged()

                }, Consumer<Throwable> { t ->
                    Log.e("listElementError", t.message, t)
                    showErrorPopup()
                })
    }

    private fun extractMetadataFromJson(jObj: JSONObject) {

        try {
            if (jObj.has("jos")) {
                hasMore = jObj.getInt("jos") != 0
            } else {
                hasMore = false
            }

            if (jObj.has("image")) {
                val temp = jObj.getJSONObject("image")
                //                prefs.edit().putString("hr.bpervan.novaeva.categoryheader." +
                //                        ListaVijestiActivity.this.inputIntent.getIntExtra("kategorija",11), temp.getString("640")).commit();

            }
        } catch (e: NumberFormatException) {
            Log.e("erroooor", e.message, e)
        } catch (e: JSONException) {
            Log.e("erroooor", e.message, e)
        }
    }

    private fun parseTreeElementsJson(TreeElementsJsonObj: JSONObject): List<TreeElement> {
        val treeElementList = ArrayList<TreeElement>()

        try {
            if (TreeElementsJsonObj.has("vijesti") && !TreeElementsJsonObj.isNull("vijesti")) {
                val poljeVijesti = TreeElementsJsonObj.getJSONArray("vijesti")
                for (i in 0 until poljeVijesti.length()) {

                    val jednaVijest = poljeVijesti.getJSONObject(i)

                    val contentId = jednaVijest.getString("nid").toLong()
                    val title = jednaVijest.getString("naslov")
                    val summary = jednaVijest.getString("uvod")
                    val date = jednaVijest.getString("datum")

                    val contentInfo =
                            if (jednaVijest.has("attach")) {
                                val attachField = jednaVijest.getJSONObject("attach")

                                ContentInfo(contentId, directoryId, title, summary, date,
                                        hasVideo = attachField.getBoolean("video"),
                                        hasDocuments = attachField.getBoolean("documents"),
                                        hasMusic = attachField.getBoolean("music"),
                                        hasImages = attachField.getBoolean("images"),
                                        hasText = attachField.getBoolean("text"))
                            } else {
                                ContentInfo(contentId, directoryId, title, summary, date)
                            }

                    treeElementList.add(contentInfo)
                }
            }

            //TODO: i ove malverzacije treba testirati
            parseSubDirArray(TreeElementsJsonObj, treeElementList)
        } catch (e: JSONException) {
            //e.printStackTrace();
            Log.e("errorParsingCids", e.message, e)
        }
        return treeElementList
    }

    @Throws(JSONException::class)
    private fun parseSubDirArray(cidList: JSONObject, treeElementList: MutableList<TreeElement>) {

        if (!cidList.isNull("subcat")) {
            val poljePodkategorija = cidList.getJSONArray("subcat")
            if (poljePodkategorija != null) {
                for (i in 0 until poljePodkategorija.length()) {
                    val subDirectory = poljePodkategorija.getJSONObject(i)

                    treeElementList.add(DirectoryInfo(
                            subDirectory.getString("cid").toLong(),
                            subDirectory.getString("name")))
                }
            }
        }
    }

    companion object {

        fun newInstance(args: Bundle): EvaMenuRecyclerFragment {
            val fragment = EvaMenuRecyclerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}