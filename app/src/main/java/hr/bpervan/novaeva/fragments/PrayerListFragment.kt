package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerCategoryRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.OpenPrayerDirectoryEvent
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.views.onLayoutComplete
import kotlinx.android.synthetic.main.fragment_prayers.*
import kotlinx.android.synthetic.main.top_prayerbook.*

class PrayerListFragment : EvaAbstractDirectoryFragment() {

    companion object : EvaFragmentFactory<PrayerListFragment, OpenPrayerDirectoryEvent> {

        private const val expandedItemKey = "expandedItem"

        override fun newInstance(initializer: OpenPrayerDirectoryEvent): PrayerListFragment {
            return PrayerListFragment().apply {
                arguments = bundleOf(EvaFragmentFactory.INITIALIZER to initializer)
            }
        }
    }

    private lateinit var initializer: OpenPrayerDirectoryEvent

    override fun onCreate(savedInstanceState: Bundle?) {

        val inState = savedInstanceState ?: arguments!!
        initializer = inState.getParcelable(EvaFragmentFactory.INITIALIZER)!!

        domain = EvaDomain.PRAYERS
        directoryId = initializer.directoryId
        directoryTitle = initializer.title

        fetchItems = 1000

        adapter = PrayerCategoryRecyclerAdapter(elementsList)

        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EvaFragmentFactory.INITIALIZER, initializer)
        outState.putInt(expandedItemKey, (adapter as PrayerCategoryRecyclerAdapter).expandedItemPos)
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

        val recyclerView = evaRecyclerView as androidx.recyclerview.widget.RecyclerView
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = adapter

        prayerTitleTextView.text = directoryTitle
        prayerTitleTextView.typeface = NovaEvaApp.openSansBold

        if (savedInstanceState != null) {
            val savedExpandedItemPos = savedInstanceState.getInt(expandedItemKey, androidx.recyclerview.widget.RecyclerView.NO_POSITION)
            if (savedExpandedItemPos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                recyclerView.onLayoutComplete {
                    (adapter as PrayerCategoryRecyclerAdapter).expandedItemPos = savedExpandedItemPos
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Molitve '$directoryTitle'".take(36), "PrayerList")
    }

    override fun fillElements(evaDirectory: EvaDirectory) {
        elementsList.clear()

        val contentSorted = evaDirectory.contents.sortedByDescending { it.position }

        elementsList.addAll(contentSorted)
    }
}
