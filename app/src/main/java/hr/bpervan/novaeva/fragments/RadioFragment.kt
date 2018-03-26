package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.RadioStationsAdapter
import hr.bpervan.novaeva.main.R
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_radio.*

/**
 *
 */
class RadioFragment : EvaBaseFragment() {
    companion object : EvaBaseFragment.EvaFragmentFactory<RadioFragment, Unit> {

        override fun newInstance(initializer: Unit): RadioFragment {
            return RadioFragment()
        }
    }

    private lateinit var adapter: RadioStationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = RadioStationsAdapter()

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Radio")
                        .setAction("OtvorenRadioIzbornik")
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_radio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {

        collapsingRadioHeader.collapsingToolbar.title = getString(R.string.radio_stations)

        val recyclerView = evaRecyclerView as RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }
}