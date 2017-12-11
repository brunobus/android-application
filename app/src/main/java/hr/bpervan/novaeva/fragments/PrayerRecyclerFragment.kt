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
import hr.bpervan.novaeva.model.hardcodedPrayerList
import kotlinx.android.synthetic.main.eva_directory.view.*
import kotlinx.android.synthetic.main.eva_directory_collapsing_bar.view.*
import kotlinx.android.synthetic.main.izbornik_top.view.*

/**
 * Created by vpriscan on 11.12.17..
 */
class PrayerRecyclerFragment : EvaBaseFragment() {

    companion object {
        fun newInstance(): PrayerRecyclerFragment {
            return PrayerRecyclerFragment()
        }
    }

    private lateinit var adapter: PrayerBookRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        adapter = PrayerBookRecyclerAdapter(hardcodedPrayerList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.eva_directory, container, false).apply {
            val infoText = "NALAZITE SE U KATEGORIJI"
            val title = "MOLITVENIK"
            evaDirectoryCollapsingBar.izbornikTop.izbornikTopNatpis.apply {
                text = infoText
                typeface = NovaEvaApp.openSansBold
            }
            evaDirectoryCollapsingBar.izbornikTop.izbornikTopNazivKategorije.apply {
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