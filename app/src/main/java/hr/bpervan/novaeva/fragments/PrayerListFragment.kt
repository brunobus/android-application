package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.adapters.PrayerCategoryRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.BackgroundReplaceEvent
import hr.bpervan.novaeva.model.BackgroundType
import hr.bpervan.novaeva.model.PRAYER_CATEGORIES
import hr.bpervan.novaeva.model.PrayerCategory
import kotlinx.android.synthetic.main.fragment_prayers.*
import kotlinx.android.synthetic.main.top_prayerbook.*

class PrayerListFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<PrayerListFragment, Int> {

        private const val prayerCategoryIdKey = "prayerCategoryId"

        override fun newInstance(initializer: Int): PrayerListFragment {
            return PrayerListFragment().apply {
                arguments = Bundle().apply {
                    putInt(prayerCategoryIdKey, initializer)
                }
            }
        }
    }

    private lateinit var prayerCategory: PrayerCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: arguments!!
        prayerCategory = PRAYER_CATEGORIES.first { it.id == inState.getInt(prayerCategoryIdKey) }

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
        outState.putInt("prayerCategoryId", prayerCategory.id)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.PrayersTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.fragment_prayers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RxEventBus.appBackground.onNext(BackgroundReplaceEvent(R.color.WhiteSmoke, BackgroundType.COLOR))
        RxEventBus.navigationAndStatusBarColor.onNext(R.color.Black)

        initUI()
    }

    private fun initUI() {
        val recyclerView = evaRecyclerView as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PrayerCategoryRecyclerAdapter(prayerCategory)

        prayerTitleTextView.text = prayerCategory.title
        prayerTitleTextView.typeface = NovaEvaApp.openSansBold
    }
}
