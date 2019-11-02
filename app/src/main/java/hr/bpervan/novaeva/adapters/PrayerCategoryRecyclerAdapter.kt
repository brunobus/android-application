package hr.bpervan.novaeva.adapters

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerCategoryRecyclerAdapter.PrayerViewHolder
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.EvaNode
import hr.bpervan.novaeva.util.EvaTouchFeedback
import hr.bpervan.novaeva.views.loadHtmlText
import kotlinx.android.synthetic.main.recycler_item_prayer.view.*
import net.cachapa.expandablelayout.ExpandableLayout


/**
 * Created by vpriscan on 03.01.18..
 */
class PrayerCategoryRecyclerAdapter(private val prayerCategory: List<EvaNode>) :
        RecyclerView.Adapter<PrayerViewHolder>() {

    override fun getItemCount(): Int = prayerCategory.size

    var expandedItemPos: Int = RecyclerView.NO_POSITION
        set(newValue) {
            val oldValue = field
            field = newValue

            setExpanded(position = oldValue, expand = false)
            setExpanded(position = newValue, expand = true)
        }

    private var themeColorTrans: Int = 0

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val typedVal = TypedValue()
        recyclerView.context.theme.resolveAttribute(R.attr.colorPrimary, typedVal, true)
        val themeColor = typedVal.data
        themeColorTrans = Color.argb(127, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))

        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_prayer, parent, false)
        view.prayerTitleConstraintLayout.background.mutate()
        return PrayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        holder.bindTo(prayerCategory[position])
    }

    inner class PrayerViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val expandableLayout: ExpandableLayout = view.expandableLayout
        private val prayerTitle: TextView = view.prayerTitleTextView
        private val prayerContent: WebView = view.prayerContentView

        init {
            expandableLayout.setInterpolator(DecelerateInterpolator())

            prayerTitle.typeface = NovaEvaApp.openSansRegular
        }

        fun bindTo(prayer: EvaNode) {
            prayer as EvaContent

            prayerTitle.text = prayer.title
            prayerContent.loadHtmlText(prayer.text)

            expandableLayout.setExpanded(adapterPosition == expandedItemPos, false)

            view.prayerTitleConstraintLayout.let {
                it.setOnTouchListener(EvaTouchFeedback(it, themeColorTrans))
                it.setOnClickListener(this@PrayerViewHolder)
            }
        }

        override fun onClick(v: View?) {
            expandedItemPos =
                    if (adapterPosition != expandedItemPos) adapterPosition
                    else RecyclerView.NO_POSITION
        }

    }

    private fun setExpanded(position: Int, expand: Boolean, animate: Boolean = true) {
        if (position != RecyclerView.NO_POSITION) {
            val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position) as? PrayerViewHolder
            viewHolder?.expandableLayout?.setExpanded(expand, animate)
        }
    }
}