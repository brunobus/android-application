package hr.bpervan.novaeva.fragments

import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter
import hr.bpervan.novaeva.main.R
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

        adapter = PrayerBookRecyclerAdapter(NovaEvaApp.prayerBook.values.toList())

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Molitvenik")
                        .setAction("OtvorenMolitvenik")
                        .build())
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

        val title = "Molitvenik"

        prayerArrow.isInvisible = true
        prayerTitleTextView.apply {
            text = title
            typeface = NovaEvaApp.openSansBold
        }

        val recyclerView = evaRecyclerView as androidx.recyclerview.widget.RecyclerView
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        recyclerView.adapter = adapter
    }
}