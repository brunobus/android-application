package hr.bpervan.novaeva.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter.BindableViewHolder
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.Prayer
import hr.bpervan.novaeva.utilities.EvaTouchFeedback
import kotlinx.android.synthetic.main.prayer_row.view.*

class PrayerBookRecyclerAdapter(private val prayerList: List<Prayer>)
    : RecyclerView.Adapter<BindableViewHolder>() {

    override fun getItemCount(): Int = prayerList.size

    private var themeColor: Int = 0
    private var themeColorTrans: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val typedVal = TypedValue()
        recyclerView.context.theme.resolveAttribute(R.attr.colorPrimary, typedVal, true)
        themeColor = typedVal.data
        themeColorTrans = Color.argb(127, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))
    }

    override fun getItemViewType(position: Int): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.prayer_row, parent, false)
        view.background.mutate()
        return PrayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        holder.bindTo(prayerList[position])
    }

    inner abstract class BindableViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindTo(t: Any)
    }

    private inner class PrayerViewHolder(view: View) : BindableViewHolder(view) {
        val tvMolitva: TextView = view.tvMolitva

        init {
            tvMolitva.typeface = NovaEvaApp.openSansBold
        }

        override fun bindTo(t: Any) {
            val prayer = t as Prayer
            tvMolitva.text = prayer.prayerTitle

            view.setOnTouchListener(EvaTouchFeedback(view, themeColorTrans))
            view.setOnClickListener {
                NovaEvaApp.bus.prayerOpenRequest.onNext(prayer)
            }
        }
    }
}
