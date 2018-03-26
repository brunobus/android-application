package hr.bpervan.novaeva.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import hr.bpervan.novaeva.adapters.RadioStationsAdapter.RadioStationViewHolder
import hr.bpervan.novaeva.main.R

/**
 *
 */
class RadioStationsAdapter() : RecyclerView.Adapter<RadioStationViewHolder>() {

    override fun getItemCount() = 10

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioStationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_folder, parent, false)
        return RadioStationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RadioStationViewHolder, position: Int) {
        holder.bindTo("todo")
    }

    inner class RadioStationViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bindTo(todoMe: String) {

        }
    }
}