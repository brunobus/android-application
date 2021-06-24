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
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.OpenPrayerDirectoryEvent
import hr.bpervan.novaeva.rest.EvaDomain
import kotlinx.android.synthetic.main.collapsing_prayerbook_header.view.*
import kotlinx.android.synthetic.main.fragment_prayers.*
import kotlinx.android.synthetic.main.top_prayerbook.*

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

    private lateinit var initializer: OpenPrayerDirectoryEvent

    override fun onCreate(savedInstanceState: Bundle?) {

        val inState: Bundle = savedInstanceState ?: arguments!!

        initializer = inState.getParcelable(EvaFragmentFactory.INITIALIZER)!!

        domain = EvaDomain.PRAYERS
        directoryId = initializer.directoryId
        directoryTitle = initializer.title

        fetchItems = 1000

        adapter = PrayerBookRecyclerAdapter(elementsList)

        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EvaFragmentFactory.INITIALIZER, initializer)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.PrayersTheme))
                .inflate(R.layout.fragment_prayers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        prayerArrow.isInvisible = true
        prayerTitleTextView.apply {
            text = directoryTitle
            typeface = NovaEvaApp.openSansBold
        }

        val recyclerView = evaRecyclerView as androidx.recyclerview.widget.RecyclerView
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        recyclerView.adapter = adapter

        val coverImageView = collapsingPrayerHeader.prayerCollapsingToolbar.prayerCoverImage
        val url = prefs.getString("hr.bpervan.novaeva.categoryheader.$domain", null)
        if (url != null && coverImageView != null) {
            imageLoader.displayImage(url, coverImageView)
        }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Molitvenik", "PrayerBook")
    }

    override fun fillElements(evaDirectory: EvaDirectory) {
        elementsList.clear()

        val subcategoriesSorted = evaDirectory.subCategories.sortedByDescending { it.position }

        elementsList.addAll(subcategoriesSorted)
    }
}