package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerCategoryRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentPrayersBinding
import hr.bpervan.novaeva.model.CategoryDto
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.OpenPrayerDirectoryEvent
import hr.bpervan.novaeva.model.toDbModel
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.views.onLayoutComplete

class PrayerListFragment : EvaAbstractDirectoryFragment() {

    companion object : EvaFragmentFactory<PrayerListFragment, OpenPrayerDirectoryEvent> {

        private const val expandedItemKey = "expandedItem"

        override fun newInstance(initializer: OpenPrayerDirectoryEvent): PrayerListFragment {
            return PrayerListFragment().apply {
                arguments = bundleOf(EvaFragmentFactory.INITIALIZER to initializer)
            }
        }
    }

    private var _viewBinding: FragmentPrayersBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var initializer: OpenPrayerDirectoryEvent

    override fun onCreate(savedInstanceState: Bundle?) {

        val inState = savedInstanceState ?: arguments!!
        initializer = inState.getParcelable(EvaFragmentFactory.INITIALIZER)!!

        domain = EvaDomain.PRAYERS
        directoryId = initializer.directoryId
        directoryTitle = initializer.title

        fetchItems = 500
        searchFetchItems = 20

        adapter = PrayerCategoryRecyclerAdapter(elementsList)

        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EvaFragmentFactory.INITIALIZER, initializer)
        outState.putInt(expandedItemKey, (adapter as PrayerCategoryRecyclerAdapter).expandedItemPos)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val newInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.PrayersTheme))
        _viewBinding = FragmentPrayersBinding.inflate(newInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        val recyclerView = viewBinding.evaRecyclerView.root
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewBinding.collapsingPrayerHeader.prayerTop.prayerTitleTextView.text = directoryTitle
        viewBinding.collapsingPrayerHeader.prayerTop.prayerTitleTextView.typeface = NovaEvaApp.openSansBold

        if (savedInstanceState != null) {
            val savedExpandedItemPos = savedInstanceState.getInt(expandedItemKey, androidx.recyclerview.widget.RecyclerView.NO_POSITION)
            if (savedExpandedItemPos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                recyclerView.onLayoutComplete {
                    (adapter as PrayerCategoryRecyclerAdapter).expandedItemPos = savedExpandedItemPos
                }
            }
        }

        val searchView = viewBinding.collapsingPrayerHeader.prayerTop.prayerbookSearchView
        initSearch(searchView)
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Molitve '$directoryTitle'".take(36), "PrayerList")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun fillElements(categoryDto: CategoryDto) {
        elementsList.clear()

        val contentSorted = categoryDto.content.orEmpty().map { it.toDbModel() }.sortedByDescending { it.position }

        elementsList.addAll(contentSorted)
    }

    override fun fillElements(evaDirectory: EvaDirectory) {
        elementsList.clear()

        val contentSorted = evaDirectory.contents.sortedByDescending { it.position }

        elementsList.addAll(contentSorted)
    }
}
