package hr.bpervan.novaeva.adapters

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.RecyclerItemEvaContentBinding
import hr.bpervan.novaeva.main.databinding.RecyclerItemFolderBinding
import hr.bpervan.novaeva.main.databinding.RecyclerItemProgressBinding
import hr.bpervan.novaeva.model.AttachmentIndicatorHelper
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.EvaNode
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.util.EvaTouchFeedback
import hr.bpervan.novaeva.util.TransitionAnimation
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaRecyclerAdapter(private val data: List<EvaNode>,
                         val isLoadingSupplier: () -> Boolean = { false },
                         val themeId: Int = -1) :
        androidx.recyclerview.widget.RecyclerView.Adapter<EvaRecyclerAdapter.BindableViewHolder>() {

    companion object {
        val dayMonthFormat = SimpleDateFormat("d.M.", Locale.US)
        val yearHourMinuteFormat = SimpleDateFormat("yyyy, HH:mm", Locale.US)

        const val CONTENT_VIEW_TYPE = 1
        const val SUBDIRECTORY_VIEW_TYPE = 2
        const val PROGRESS_VIEW_TYPE = 3
    }

    private var themeColor: Int = 0
    private var themeColorTrans: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val typedVal = TypedValue()
        recyclerView.context.theme.resolveAttribute(R.attr.colorPrimary, typedVal, true)
        themeColor = typedVal.data
        themeColorTrans = Color.argb(127, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == data.size -> PROGRESS_VIEW_TYPE
            data[position] is EvaContent -> CONTENT_VIEW_TYPE
            else -> SUBDIRECTORY_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int = data.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        return when (viewType) {
            CONTENT_VIEW_TYPE -> {
                val viewBinding = RecyclerItemEvaContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                viewBinding.root.background.mutate()
                ContentInfoViewHolder(viewBinding)
            }
            SUBDIRECTORY_VIEW_TYPE -> {
                val viewBinding = RecyclerItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                viewBinding.root.background.mutate()
                DirectoryInfoViewHolder(viewBinding)
            }
            else -> {
                val viewBinding = RecyclerItemProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ProgressBarViewHolder(viewBinding)
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

    inner abstract class BindableViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        abstract fun bindTo(t: Any)
    }

    private inner class DirectoryInfoViewHolder(viewBinding: RecyclerItemFolderBinding) : BindableViewHolder(viewBinding.root) {
        val tvMapaNaslov: TextView = viewBinding.tvMapaNaslov

        init {
            tvMapaNaslov.typeface = NovaEvaApp.openSansBold
        }

        override fun bindTo(t: Any) {
            val directoryInfo = t as EvaDirectory

            tvMapaNaslov.text = directoryInfo.title

            view.setOnTouchListener(EvaTouchFeedback(view, themeColorTrans))
            view.setOnClickListener {
                EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                        directoryId = directoryInfo.id,
                        domain = enumValueOf(directoryInfo.domain!!),
                        title = directoryInfo.title,
                        theme = themeId,
                        animation = TransitionAnimation.LEFTWARDS))
            }
        }

    }

    private inner class ContentInfoViewHolder(viewBinding: RecyclerItemEvaContentBinding) : BindableViewHolder(viewBinding.root) {
        val tvNaslov: TextView = viewBinding.tvNaslov
        val tvUvod: TextView = viewBinding.tvUvod
        val tvDatum: TextView = viewBinding.tvDatum
        val tvGodinaSatMinuta: TextView = viewBinding.tvGodinaSatMinuta

        val imgHasLink: ImageView = viewBinding.imgViewLink
        val imgHasTxt: ImageView = viewBinding.imgViewTxt
        val imgHasAudio: ImageView = viewBinding.imgViewMp3

        val tvUvodNatpis: TextView = viewBinding.tvUvodNatpis

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
            val contentInfo = t as EvaContent

            tvNaslov.text = contentInfo.title

            val datetime = Date(contentInfo.created)

            //todo move formatted datetime to DB
            val dayMonth: String = dayMonthFormat.format(datetime)
            val yearHourMinute: String = yearHourMinuteFormat.format(datetime)

            contentInfo.attachmentsIndicator.let {
                imgHasTxt.isVisible = AttachmentIndicatorHelper.hasDocs(it)
                imgHasLink.isVisible = AttachmentIndicatorHelper.hasVideo(it)
                imgHasAudio.isVisible = AttachmentIndicatorHelper.hasMusic(it)
            }

            tvGodinaSatMinuta.text = yearHourMinute
            tvDatum.text = dayMonth
            tvUvod.text = contentInfo.preview

            view.setOnTouchListener(EvaTouchFeedback(view, themeColorTrans))

            view.setOnClickListener {
                EventPipelines.openContent.onNext(
                        OpenContentEvent(
                                contentId = contentInfo.id,
                                title = contentInfo.title,
                                domain = enumValueOf(contentInfo.domain!!),
                                theme = themeId,
                                animation = TransitionAnimation.LEFTWARDS))
            }
        }
    }

    private inner class ProgressBarViewHolder(viewBinding: RecyclerItemProgressBinding) : BindableViewHolder(viewBinding.root) {

        override fun bindTo(t: Any) {
            view.visibility = if (isLoadingSupplier()) View.VISIBLE else View.GONE
        }
    }

}