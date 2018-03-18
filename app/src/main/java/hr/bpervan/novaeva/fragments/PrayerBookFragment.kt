package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.PRAYER_CATEGORIES
import kotlinx.android.synthetic.main.fragment_prayers.*
import kotlinx.android.synthetic.main.top_prayerbook.*

/**
 * Created by vpriscan on 11.12.17..
 */
class PrayerBookFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<PrayerBookFragment, Unit> {
        override fun newInstance(initializer: Unit): PrayerBookFragment {
            return PrayerBookFragment()
        }
    }


    private lateinit var adapter: PrayerBookRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = PrayerBookRecyclerAdapter(PRAYER_CATEGORIES)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Molitvenik")
                        .setAction("OtvorenMolitvenik")
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.PrayersTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.fragment_prayers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {
        val title = "Molitvenik"

        prayerArrow.visibility = View.INVISIBLE
        prayerTitleTextView.apply {
            text = title
            typeface = NovaEvaApp.openSansBold
        }

        val recyclerView = evaRecyclerView as RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }
}