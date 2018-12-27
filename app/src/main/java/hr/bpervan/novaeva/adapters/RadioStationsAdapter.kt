package hr.bpervan.novaeva.adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.adapters.RadioStationsAdapter.RadioStationViewHolder
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.util.EvaTouchFeedback
import hr.bpervan.novaeva.views.PlayPauseView
import kotlinx.android.synthetic.main.recycler_item_radio_station.view.*

/**
 *
 */
class RadioStationsAdapter(private val radioStations: List<EvaContent>)
    : RecyclerView.Adapter<RadioStationViewHolder>() {

    override fun getItemCount() = radioStations.size

    var radioStationPlaying: Long? = null

    private var touchFeedbackColor: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        touchFeedbackColor = ContextCompat.getColor(recyclerView.context, R.color.TranslucentGray)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioStationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_radio_station, parent, false)
        view.background.mutate()
        return RadioStationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RadioStationViewHolder, position: Int) {
        holder.bindTo(radioStations[position])
    }

    inner class RadioStationViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val stationName: TextView = view.stationName
        private val imgPlayPause: PlayPauseView = view.imgPlayPause

        fun bindTo(radioStation: EvaContent) {
            stationName.text = radioStation.title
            view.setOnTouchListener(EvaTouchFeedback(view, touchFeedbackColor))
            view.setOnClickListener {
                EventPipelines.chooseRadioStation.onNext(radioStation)
            }

            imgPlayPause.isPlaying = radioStation.id == radioStationPlaying
        }
    }
}