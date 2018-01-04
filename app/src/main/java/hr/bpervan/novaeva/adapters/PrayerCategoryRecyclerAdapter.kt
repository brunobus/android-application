package hr.bpervan.novaeva.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.PRAYERS_ASSETS_DIR_PATH
import hr.bpervan.novaeva.model.Prayer
import hr.bpervan.novaeva.model.PrayerCategory
import hr.bpervan.novaeva.utilities.EvaTouchFeedback
import kotlinx.android.synthetic.main.recycler_item_prayer.view.*
import net.cachapa.expandablelayout.ExpandableLayout


/**
 * Created by vpriscan on 03.01.18..
 */
class PrayerCategoryRecyclerAdapter(private val prayerCategory: PrayerCategory) :
        RecyclerView.Adapter<PrayerCategoryRecyclerAdapter.PrayerViewHolder>() {

    override fun getItemCount(): Int = prayerCategory.prayerList.size

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
        holder.bindTo(prayerCategory.prayerList[position])
    }

    inner class PrayerViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val expandableLayout: ExpandableLayout = view.expandableLayout
        private val prayerTitle: TextView = view.prayerTitleTextView
        private val prayerContent: WebView = view.prayerContentView

        init {
            expandableLayout.setInterpolator(DecelerateInterpolator())

            prayerTitle.typeface = NovaEvaApp.openSansRegular
        }

        fun bindTo(prayer: Prayer) {

            prayerTitle.text = prayer.title
            prayerContent.loadUrl(PRAYERS_ASSETS_DIR_PATH + prayerCategory.directoryName + "/" + prayer.fileName)

            expandableLayout.setExpanded(false, false)

            view.prayerTitleConstraintLayout.let {
                it.setOnTouchListener(EvaTouchFeedback(it, themeColorTrans))
                it.setOnClickListener(this@PrayerViewHolder)
            }
        }

        override fun onClick(v: View?) {

            if (expandableLayout.isExpanded) {
                expandableLayout.collapse()
            } else {
                expandableLayout.expand()
            }
        }
    }
}