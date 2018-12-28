package hr.bpervan.novaeva.adapters

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.model.OpenPrayerCategoryEvent
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter.PrayerCategoryViewHolder
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.PrayerCategory
import hr.bpervan.novaeva.util.EvaTouchFeedback
import hr.bpervan.novaeva.util.TransitionAnimation
import kotlinx.android.synthetic.main.recycler_item_prayer_category.view.*

class PrayerBookRecyclerAdapter(private val prayerCategoryList: List<PrayerCategory>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<PrayerCategoryViewHolder>() {

    override fun getItemCount(): Int = prayerCategoryList.size

    private var themeColorTrans: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val typedVal = TypedValue()
        recyclerView.context.theme.resolveAttribute(R.attr.colorPrimary, typedVal, true)
        val themeColor = typedVal.data
        themeColorTrans = Color.argb(127, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_prayer_category, parent, false)
        view.background.mutate()
        return PrayerCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerCategoryViewHolder, position: Int) {
        holder.bindTo(prayerCategoryList[position])
    }

    inner class PrayerCategoryViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val prayerCategoryTitle: TextView = view.prayerCategoryTitleTextView

        init {
            prayerCategoryTitle.typeface = NovaEvaApp.openSansRegular
        }

        fun bindTo(prayerCategory: PrayerCategory) {
            prayerCategoryTitle.text = prayerCategory.title

            view.setOnTouchListener(EvaTouchFeedback(view, themeColorTrans))
            view.setOnClickListener {
                EventPipelines.openPrayerCategory.onNext(OpenPrayerCategoryEvent(prayerCategory, TransitionAnimation.LEFTWARDS))
            }
        }
    }
}
