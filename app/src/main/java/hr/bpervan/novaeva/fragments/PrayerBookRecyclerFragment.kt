package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.HARDCODED_PRAYER_CATEGORY_LIST
import kotlinx.android.synthetic.main.eva_directory.view.*
import kotlinx.android.synthetic.main.prayerbook_top.view.*

/**
 * Created by vpriscan on 11.12.17..
 */
class PrayerBookRecyclerFragment : EvaBaseFragment() {

    companion object {
        fun newInstance(): PrayerBookRecyclerFragment {
            return PrayerBookRecyclerFragment()
        }
    }

    private lateinit var adapter: PrayerBookRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        adapter = PrayerBookRecyclerAdapter(HARDCODED_PRAYER_CATEGORY_LIST)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prayers, container, false).apply {
            val title = "MOLITVENIK"
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
}