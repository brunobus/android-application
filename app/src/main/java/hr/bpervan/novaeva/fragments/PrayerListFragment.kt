package hr.bpervan.novaeva.fragments

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerCategoryRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.PrayerCategory
import hr.bpervan.novaeva.views.onLayoutComplete
import kotlinx.android.synthetic.main.fragment_prayers.*
import kotlinx.android.synthetic.main.top_prayerbook.*

class PrayerListFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<PrayerListFragment, Int> {

        private const val prayerCategoryIdKey = "prayerCategoryId"
        private const val expandedItemKey = "expandedItem"

        override fun newInstance(initializer: Int): PrayerListFragment {
            return PrayerListFragment().apply {
                arguments = bundleOf(prayerCategoryIdKey to initializer)
            }
        }
    }

    private lateinit var prayerCategory: PrayerCategory
    private lateinit var adapter: PrayerCategoryRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: arguments!!
        prayerCategory = NovaEvaApp.prayerBook[inState.getInt(prayerCategoryIdKey)]!!

        adapter = PrayerCategoryRecyclerAdapter(prayerCategory)

        savedInstanceState ?: NovaEvaApp.defaultTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Molitvenik")
                        .setAction("OtvorenaGrupaMolitvi")
                        .setLabel(prayerCategory.title)
                        .setValue(prayerCategory.id.toLong())
                        .build()
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(prayerCategoryIdKey, prayerCategory.id)
        outState.putInt(expandedItemKey, adapter.expandedItemPos)
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

        prayerTitleTextView.text = prayerCategory.title
        prayerTitleTextView.typeface = NovaEvaApp.openSansBold

        if (savedInstanceState != null) {
            val savedExpandedItemPos = savedInstanceState.getInt(expandedItemKey, androidx.recyclerview.widget.RecyclerView.NO_POSITION)
            if (savedExpandedItemPos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                recyclerView.onLayoutComplete {
                    adapter.expandedItemPos = savedExpandedItemPos
                }
            }
        }
    }
}
