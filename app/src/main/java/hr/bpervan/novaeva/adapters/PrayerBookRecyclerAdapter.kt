package hr.bpervan.novaeva.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.model.OpenPrayerCategoryEvent
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter.BindableViewHolder
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.PrayerCategory
import hr.bpervan.novaeva.utilities.EvaTouchFeedback
import hr.bpervan.novaeva.utilities.TransitionAnimation
import kotlinx.android.synthetic.main.recycler_item_prayer_category.view.*

class PrayerBookRecyclerAdapter(private val prayerCategoryList: List<PrayerCategory>) :
        RecyclerView.Adapter<BindableViewHolder>() {

    override fun getItemCount(): Int = prayerCategoryList.size

    private var themeColorTrans: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val typedVal = TypedValue()
        recyclerView.context.theme.resolveAttribute(R.attr.colorPrimary, typedVal, true)
        val themeColor = typedVal.data
        themeColorTrans = Color.argb(127, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_prayer_category, parent, false)
        view.background.mutate()
        return PrayerCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        holder.bindTo(prayerCategoryList[position])
    }

    abstract inner class BindableViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindTo(t: Any)
    }

    private inner class PrayerCategoryViewHolder(view: View) : BindableViewHolder(view) {
        private val prayerCategoryTitle: TextView = view.prayerCategoryTitleTextView

        init {
            prayerCategoryTitle.typeface = NovaEvaApp.openSansRegular
        }

        override fun bindTo(t: Any) {
            val prayerCategory = t as PrayerCategory
            prayerCategoryTitle.text = prayerCategory.title

            view.setOnTouchListener(EvaTouchFeedback(view, themeColorTrans))
            view.setOnClickListener {
                RxEventBus.openPrayerCategory.onNext(OpenPrayerCategoryEvent(prayerCategory, TransitionAnimation.RIGHTWARDS))
            }
        }
    }
}
