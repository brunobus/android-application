package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.util.showFetchErrorDialog
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.toDatabaseModel
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.util.networkRequest
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.eva_recycler_view.view.*
import java.util.*

/**
 *
 */
class EvaSearchFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaSearchFragment, String> {

        private const val searchStringKey = "searchString"
        override fun newInstance(initializer: String): EvaSearchFragment {
            return EvaSearchFragment().apply {
                arguments = bundleOf(searchStringKey to initializer)
            }
        }
    }


    private var searchForContentDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private val searchResultList = ArrayList<EvaContentMetadata>()

    private lateinit var adapter: EvaRecyclerAdapter
    private lateinit var searchString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: arguments!!
        searchString = inState.getString(searchStringKey)

        adapter = EvaRecyclerAdapter(searchResultList)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Pretraga")
                        .setAction("KljucneRijeci")
                        .setLabel(searchString)
                        .build())
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        activity?.title = "Pretraga: " + searchString
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme))
                .inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        initUI()
    }

    private fun initUI() {
//        btnSearch.setOnClickListener(this)

        val recyclerView = evaRecyclerView as RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(evaRecyclerView.evaRecyclerView.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun showSearchPopup() {
        activity?.let { activity ->

            val searchBuilder = AlertDialog.Builder(activity)
            searchBuilder.setTitle("Pretraga")

            val et = EditText(activity)
            searchBuilder.setView(et)

            searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
                val search = et.text.toString()
                searchForContent(search)
            }
            searchBuilder.setNegativeButton("Odustani") { _, _ -> /**/ }
            searchBuilder.show()
        }
    }

    private fun searchForContent(searchString: String) {
        this.searchString = searchString

        searchResultList.clear()
        adapter.notifyDataSetChanged()

        searchForContentDisposable = novaEvaService.searchForContent(searchString)
                .networkRequest({ searchResult ->
                    if (!searchResult.searchResultContentMetadataList.isEmpty()) {
                        searchResultList.addAll(
                                searchResult.searchResultContentMetadataList.map { it.toDatabaseModel() })

                        adapter.notifyDataSetChanged()
                    } else {
                        showEmptyListInfo()
                    }
                }, onError = {
                    activity?.let { activity ->
                        showFetchErrorDialog(it, activity) { searchForContent(searchString) }
                    }
                })
    }

    private fun showEmptyListInfo() {
        activity?.let { activity ->
            val emptyInfo = AlertDialog.Builder(activity)
            emptyInfo.setTitle("Pretraga")
            emptyInfo.setMessage("Pretraga nije vratila rezultate")

            emptyInfo.setPositiveButton("U redu") { _, _ -> activity.onBackPressed() }

            emptyInfo.show()
        }

    }
}