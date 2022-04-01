package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentSearchBinding
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.toDbModel
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.util.showFetchErrorDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

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

    private var _viewBinding: FragmentSearchBinding? = null
    private val viewBinding get() = _viewBinding!!


    private var searchForContentDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private val searchResultList = ArrayList<EvaContent>()

    private lateinit var adapter: EvaRecyclerAdapter
    private lateinit var searchString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: arguments!!
        searchString = inState.getString(searchStringKey, "Isus")

        adapter = EvaRecyclerAdapter(searchResultList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.title = "Pretraga: $searchString"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme))
        _viewBinding = FragmentSearchBinding.inflate(newInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

//        btnSearch.setOnClickListener(this)

        val recyclerView = viewBinding.evaRecyclerView.root
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(viewBinding.evaRecyclerView.evaRecyclerView.context)
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Pretraga '$searchString'".take(36), "Search")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
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

        searchForContentDisposable = NovaEvaService.v2.searchForContent(searchString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { searchResult ->
                    if (!searchResult.searchResultContentMetadataList.isEmpty()) {
                        searchResultList.addAll(
                                searchResult.searchResultContentMetadataList.map { it.toDbModel() })

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