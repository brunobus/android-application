package hr.bpervan.novaeva.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.EvaDirectoryMetadata
import hr.bpervan.novaeva.model.TreeElementInfo
import hr.bpervan.novaeva.utilities.EvaTouchFeedback
import hr.bpervan.novaeva.utilities.TransitionAnimation
import kotlinx.android.synthetic.main.recycler_item_eva_content.view.*
import kotlinx.android.synthetic.main.recycler_item_folder.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaRecyclerAdapter(private val data: List<TreeElementInfo>,
                         val isLoadingSupplier: () -> Boolean = { false },
                         val themeId: Int = -1) :
        RecyclerView.Adapter<EvaRecyclerAdapter.BindableViewHolder>() {

    companion object {
        val dayMonthFormat = SimpleDateFormat("d.M.", Locale.US)
        val yearHourMinuteFormat = SimpleDateFormat("yyyy, HH:mm", Locale.US)

        val CONTENT_VIEW_TYPE = 1
        val SUBDIRECTORY_VIEW_TYPE = 2
        val PROGRESS_VIEW_TYPE = 3
    }

    private var themeColor: Int = 0
    private var themeColorTrans: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val typedVal = TypedValue()
        recyclerView.context.theme.resolveAttribute(R.attr.colorPrimary, typedVal, true)
        themeColor = typedVal.data
        themeColorTrans = Color.argb(127, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == data.size -> PROGRESS_VIEW_TYPE
            data[position] is EvaContentMetadata -> CONTENT_VIEW_TYPE
            else -> SUBDIRECTORY_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int = data.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        return when (viewType) {
            CONTENT_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_eva_content, parent, false)
                view.background.mutate()
                ContentInfoViewHolder(view)
            }
            SUBDIRECTORY_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_folder, parent, false)
                view.background.mutate()
                DirectoryInfoViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_progress, parent, false)
                ProgressBarViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        val subject: Any =
                when (position) {
                    data.size -> Unit
                    else -> data[position]
                }
        holder.bindTo(subject)
    }

    inner abstract class BindableViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindTo(t: Any)
    }

    private inner class DirectoryInfoViewHolder(view: View) : BindableViewHolder(view) {
        val tvMapaNaslov: TextView = view.tvMapaNaslov

        init {
            tvMapaNaslov.typeface = NovaEvaApp.openSansBold
        }

        override fun bindTo(t: Any) {
            val directoryInfo = t as EvaDirectoryMetadata

            tvMapaNaslov.text = directoryInfo.title

            view.setOnTouchListener(EvaTouchFeedback(view, themeColorTrans))
            view.setOnClickListener {
                RxEventBus.openDirectory.onNext(OpenDirectoryEvent(directoryInfo, themeId, TransitionAnimation.RIGHTWARDS))
            }
        }

    }

    private inner class ContentInfoViewHolder(view: View) : BindableViewHolder(view) {
        val tvNaslov: TextView = view.tvNaslov
        val tvUvod: TextView = view.tvUvod
        val tvDatum: TextView = view.tvDatum
        val tvGodinaSatMinuta: TextView = view.tvGodinaSatMinuta

        val imgHasLink: ImageView = view.imgViewLink
        val imgHasTxt: ImageView = view.imgViewTxt
        val imgHasAudio: ImageView = view.imgViewMp3

        val tvUvodNatpis: TextView = view.tvUvodNatpis

        init {
            NovaEvaApp.openSansBold?.let {
                tvNaslov.typeface = it
                tvUvodNatpis.typeface = it
            }

            NovaEvaApp.openSansItalic?.let {
                tvUvod.typeface = it
            }

            NovaEvaApp.openSansLight?.let {
                tvDatum.typeface = it
            }

            NovaEvaApp.openSansRegular?.let {
                tvGodinaSatMinuta.typeface = it
            }
        }

        override fun bindTo(t: Any) {
            val contentInfo = t as EvaContentMetadata

            tvNaslov.text = contentInfo.title

            val datetime = Date(1000 * contentInfo.timestamp)

            //todo move formatted datetime to DB
            val dayMonth: String = dayMonthFormat.format(datetime)
            val yearHourMinute: String = yearHourMinuteFormat.format(datetime)

            contentInfo.attachmentsIndicator.let {
                imgHasTxt.visibility = if (it != null && it.hasDocuments) View.VISIBLE else View.GONE
                imgHasLink.visibility = if (it != null && it.hasVideo) View.VISIBLE else View.GONE
                imgHasAudio.visibility = if (it != null && it.hasMusic) View.VISIBLE else View.GONE
            }

            tvGodinaSatMinuta.text = yearHourMinute
            tvDatum.text = dayMonth
            tvUvod.text = contentInfo.preview

            view.setOnTouchListener(EvaTouchFeedback(view, themeColorTrans))

            view.setOnClickListener {
                RxEventBus.openContent.onNext(OpenContentEvent(contentInfo, themeId, TransitionAnimation.RIGHTWARDS))
            }
        }
    }

    private inner class ProgressBarViewHolder(view: View) : BindableViewHolder(view) {

        override fun bindTo(t: Any) {
            view.visibility = if (isLoadingSupplier()) View.VISIBLE else View.GONE
        }
    }

}