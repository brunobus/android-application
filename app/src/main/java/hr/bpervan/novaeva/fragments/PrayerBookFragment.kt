package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentPrayersBinding
import hr.bpervan.novaeva.model.CategoryDto
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.OpenPrayerDirectoryEvent
import hr.bpervan.novaeva.model.toDbModel
import hr.bpervan.novaeva.rest.EvaDomain

/**
 * Created by vpriscan on 11.12.17..
 */
class PrayerBookFragment : EvaAbstractDirectoryFragment() {

    companion object : EvaFragmentFactory<PrayerBookFragment, OpenPrayerDirectoryEvent> {
        override fun newInstance(initializer: OpenPrayerDirectoryEvent): PrayerBookFragment {
            return PrayerBookFragment().apply {
                arguments = bundleOf(EvaFragmentFactory.INITIALIZER to initializer)
            }
        }
    }

    private var _viewBinding: FragmentPrayersBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var initializer: OpenPrayerDirectoryEvent

    override fun onCreate(savedInstanceState: Bundle?) {

        val inState: Bundle = savedInstanceState ?: arguments!!

        initializer = inState.getParcelable(EvaFragmentFactory.INITIALIZER)!!

        domain = EvaDomain.PRAYERS
        directoryId = initializer.directoryId
        directoryTitle = initializer.title

        fetchItems = 500
        searchFetchItems = 20

        adapter = PrayerBookRecyclerAdapter(elementsList)

        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EvaFragmentFactory.INITIALIZER, initializer)
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

        viewBinding.collapsingPrayerHeader.prayerTop.prayerArrow.isInvisible = true
        viewBinding.collapsingPrayerHeader.prayerTop.prayerTitleTextView.apply {
            text = directoryTitle
            typeface = NovaEvaApp.openSansBold
        }

        val recyclerView = viewBinding.evaRecyclerView.root
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        recyclerView.adapter = adapter

        val coverImageView = viewBinding.collapsingPrayerHeader.prayerCoverImage
        val url = prefs.getString("hr.bpervan.novaeva.categoryheader.$domain", null)
        if (url != null) {
            imageLoader.displayImage(url, coverImageView)
        }

        val searchView = viewBinding.collapsingPrayerHeader.prayerTop.prayerbookSearchView
        initSearch(searchView)
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Molitvenik", "PrayerBook")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun fillElements(categoryDto: CategoryDto) {

        elementsList.clear()

        val subcategoriesSorted = categoryDto.subcategories.orEmpty().map{it.toDbModel()}.sortedByDescending { it.position }

        elementsList.addAll(subcategoriesSorted)
    }

    override fun fillElements(evaDirectory: EvaDirectory) {
        elementsList.clear()

        val subcategoriesSorted = evaDirectory.subCategories.sortedByDescending { it.position }

        elementsList.addAll(subcategoriesSorted)
    }
}