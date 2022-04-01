package hr.bpervan.novaeva.adapters

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerBookRecyclerAdapter.PrayerCategoryViewHolder
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.RecyclerItemPrayerCategoryBinding
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.EvaNode
import hr.bpervan.novaeva.model.OpenPrayerDirectoryEvent
import hr.bpervan.novaeva.util.EvaTouchFeedback
import hr.bpervan.novaeva.util.TransitionAnimation

class PrayerBookRecyclerAdapter(private val prayerCategoryList: List<EvaNode>) :
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
        val viewBinding = RecyclerItemPrayerCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        viewBinding.root.background.mutate()
        return PrayerCategoryViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: PrayerCategoryViewHolder, position: Int) {
        holder.bindTo(prayerCategoryList[position])
    }

    inner class PrayerCategoryViewHolder(val viewBinding: RecyclerItemPrayerCategoryBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(viewBinding.root) {
        private val prayerCategoryTitle: TextView = viewBinding.prayerCategoryTitleTextView

        init {
            prayerCategoryTitle.typeface = NovaEvaApp.openSansRegular
        }

        fun bindTo(prayerCategory: EvaNode) {
            prayerCategory as EvaDirectory

            prayerCategoryTitle.text = prayerCategory.title

            viewBinding.root.setOnTouchListener(EvaTouchFeedback(viewBinding.root, themeColorTrans))
            viewBinding.root.setOnClickListener {
                EventPipelines.openPrayerCategory.onNext(OpenPrayerDirectoryEvent(
                        directoryId = prayerCategory.id,
                        title = prayerCategory.title,
                        animation = TransitionAnimation.LEFTWARDS))
            }
        }
    }
}
