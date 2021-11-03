package hr.bpervan.novaeva.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.adapters.RadioStationsAdapter.RadioStationViewHolder
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.RecyclerItemRadioStationBinding
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.util.EvaTouchFeedback
import hr.bpervan.novaeva.views.PlayPauseView

/**
 *
 */
class RadioStationsAdapter(private val radioStations: List<EvaContent>) : RecyclerView.Adapter<RadioStationViewHolder>() {

    override fun getItemCount() = radioStations.size

    var radioStationPlaying: Long? = null

    private var touchFeedbackColor: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        touchFeedbackColor = ContextCompat.getColor(recyclerView.context, R.color.TranslucentGray)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioStationViewHolder {
        val viewBinding = RecyclerItemRadioStationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        viewBinding.root.background.mutate()
        return RadioStationViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: RadioStationViewHolder, position: Int) {
        holder.bindTo(radioStations[position])
    }

    inner class RadioStationViewHolder(val viewBinding: RecyclerItemRadioStationBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        private val stationName: TextView = viewBinding.stationName
        private val imgPlayPause: PlayPauseView = viewBinding.imgPlayPause

        fun bindTo(radioStation: EvaContent) {
            stationName.text = radioStation.title
            viewBinding.root.setOnTouchListener(EvaTouchFeedback(viewBinding.root, touchFeedbackColor))
            viewBinding.root.setOnClickListener {
                EventPipelines.chooseRadioStation.onNext(radioStation)
            }

            imgPlayPause.isPlaying = radioStation.id == radioStationPlaying
        }
    }
}