package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentDirectoryContentsBinding
import hr.bpervan.novaeva.model.CategoryDto
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.TIMESTAMP_FIELD
import hr.bpervan.novaeva.model.toDbModel
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.util.sendEmailIntent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Sort
import java.util.concurrent.TimeUnit

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaDirectoryFragment : EvaAbstractDirectoryFragment() {

    companion object : EvaBaseFragment.EvaFragmentFactory<EvaDirectoryFragment, OpenDirectoryEvent> {

        override fun newInstance(initializer: OpenDirectoryEvent): EvaDirectoryFragment {
            return EvaDirectoryFragment().apply {
                arguments = bundleOf(
                    EvaFragmentFactory.INITIALIZER to initializer
                )
            }
        }
    }

    private lateinit var initializer: OpenDirectoryEvent

    private var _viewBinding: FragmentDirectoryContentsBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var themeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        val inState: Bundle = savedInstanceState ?: arguments!!

        initializer = inState.getParcelable(EvaFragmentFactory.INITIALIZER)!!

        domain = initializer.domain
        directoryId = initializer.directoryId
        directoryTitle = initializer.title
        themeId = initializer.theme

        adapter = EvaRecyclerAdapter(elementsList, { loadingFromDb || fetchingFromServer }, themeId)

        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EvaFragmentFactory.INITIALIZER, initializer)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val newInflater = if (themeId != -1) inflater.cloneInContext(ContextThemeWrapper(activity, themeId)) else inflater
        _viewBinding = FragmentDirectoryContentsBinding.inflate(newInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        viewBinding.evaDirectoryCollapsingBar.izbornikTop.izbornikTopNazivKategorije.apply {
            text = directoryTitle
            typeface = NovaEvaApp.openSansBold
        }

        val recyclerView = viewBinding.evaRecyclerView.root

        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(EndlessScrollListener(linearLayoutManager))

        when (domain) {
            EvaDomain.VOCATION -> {
                viewBinding.btnPoziv.isVisible = true
                viewBinding.btnPoziv.setOnClickListener {
                    sendEmailIntent(
                        context,
                        subject = getString(R.string.thinking_of_vocation),
                        text = getString(R.string.mail_preamble_praise_the_lord)
                                + getString(R.string.mail_intro_vocation),
                        receiver = getString(R.string.vocation_email)
                    )
                }
            }
            EvaDomain.ANSWERS -> {
                viewBinding.btnPitanje.isVisible = true
                viewBinding.btnPitanje.setOnClickListener {
                    sendEmailIntent(
                        context,
                        subject = getString(R.string.having_a_question),
                        text = getString(R.string.mail_preamble_praise_the_lord),
                        receiver = getString(R.string.answers_email)
                    )
                }
            }
            else -> {/*nothing*/
            }
        }

        disposables += EventPipelines.search
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { searchQuery ->
                onSearch(searchQuery)
            }

        val searchView = viewBinding.evaDirectoryCollapsingBar.izbornikTop.directorySearchView

        if (domain.isLegacy()) {
            searchView.visibility = View.GONE
        } else {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String): Boolean {
                    onSearch(query.trim())
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    EventPipelines.search.onNext(newText.trim())
                    return true
                }
            })
        }
    }

    private fun onSearch(searchQuery: String) {
        if (useLocalDb && searchQuery.length >= 3) {
            useLocalDb = false
            unsubscribeFromDirectoryUpdates() // will clear elements
        } else if (!useLocalDb && searchQuery.length < 3) {
            useLocalDb = true
            subscribeToDirectoryUpdates() // will fill elements
        }

        if (!useLocalDb) {
            fetchEvaDirectoryDataFromServer(searchQuery = searchQuery)
        }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
            .setCurrentScreen(requireActivity(), "Kategorija '$directoryTitle'".take(36), "Category")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun fillElements(categoryDto: CategoryDto) {

        elementsList.clear()
        val contentSorted =
            categoryDto.content.orEmpty().map { it.toDbModel() }.sortedByDescending { it.created }

        val subdirectoriesSorted =
            categoryDto.subcategories.orEmpty().map { it.toDbModel() }.sortedByDescending { it.position }

        if (contentSorted.size > 10) {
            elementsList.addAll(contentSorted.take(10))
            elementsList.addAll(subdirectoriesSorted)
            elementsList.addAll(contentSorted.drop(10))
        } else {
            elementsList.addAll(contentSorted)
            elementsList.addAll(subdirectoriesSorted)
        }
    }

    override fun fillElements(evaDirectory: EvaDirectory) {
        elementsList.clear()

        val contentSorted = evaDirectory.contents.sortedByDescending { it.created }

        val subdirectoriesSorted = evaDirectory.subCategories.sortedByDescending { it.position }

        if (contentSorted.size > 10) {
            elementsList.addAll(contentSorted.take(10))
            elementsList.addAll(subdirectoriesSorted)
            elementsList.addAll(contentSorted.drop(10))
        } else {
            elementsList.addAll(contentSorted)
            elementsList.addAll(subdirectoriesSorted)
        }
    }

    inner class EndlessScrollListener(private val linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager) :
        androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        private val visibleThreshold = 2

        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {

            if (!useLocalDb) {
                // todo support scrolling while searching directly on server
                return
            }

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
}