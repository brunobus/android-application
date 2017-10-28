package hr.bpervan.novaeva.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.EvaDirectoryMetadata
import hr.bpervan.novaeva.model.TreeElementInfo
import hr.bpervan.novaeva.utilities.ResourceHandler
import kotlinx.android.synthetic.main.folder_row.view.*
import kotlinx.android.synthetic.main.izbornik_top.view.*
import kotlinx.android.synthetic.main.vijest_row.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by vpriscan on 08.10.17..
 */
class EvaRecyclerAdapter(private val data: List<TreeElementInfo>,
                         private val configData: ConfigData,
                         private val headerData: HeaderData?
) : RecyclerView.Adapter<EvaRecyclerAdapter.BindableViewHolder>() {

    companion object {
        val dayMonthFormat = SimpleDateFormat("d.M.", Locale.US)
        val yearHourMinuteFormat = SimpleDateFormat("yyyy, HH:mm", Locale.US)

        val HEADER_VIEW_TYPE = 0
        val CONTENT_VIEW_TYPE = 1
        val SUBDIRECTORY_VIEW_TYPE = 2
        val PROGRESS_VIEW_TYPE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return if (headerData != null) {
            when {
                position == 0 -> HEADER_VIEW_TYPE
                position == data.size + 1 -> PROGRESS_VIEW_TYPE
                data[position - 1] is EvaContentMetadata -> CONTENT_VIEW_TYPE
                else -> SUBDIRECTORY_VIEW_TYPE
            }
        } else {
            when {
                position == data.size -> PROGRESS_VIEW_TYPE
                data[position] is EvaContentMetadata -> CONTENT_VIEW_TYPE
                else -> SUBDIRECTORY_VIEW_TYPE
            }
        }
    }

    override fun getItemCount(): Int = data.size + if (headerData != null) 2 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder =
            when (viewType) {
                HEADER_VIEW_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.izbornik_top, parent, false)
                    view.setBackgroundResource(ResourceHandler.getListViewHeader(configData.colourSet))
                    HeaderViewHolder(view)
                }
                CONTENT_VIEW_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.vijest_row, parent, false)
                    view.setBackgroundResource(ResourceHandler.getContentListItemResourceId(configData.colourSet))
                    ContentInfoViewHolder(view)
                }
                SUBDIRECTORY_VIEW_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_row, parent, false)
                    view.setBackgroundResource(ResourceHandler.getDirectoryListItemResourceId(configData.colourSet))
                    DirectoryInfoViewHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.progress_row, parent, false)
                    ProgressBarViewHolder(view)
                }
            }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        val subject: Any =
                if (headerData != null) {
                    when (position) {
                        0 -> headerData
                        data.size + 1 -> Unit
                        else -> data[position - 1]
                    }
                } else {
                    when (position) {
                        data.size -> Unit
                        else -> data[position]
                    }
                }
        holder.bindTo(subject)
    }

    class HeaderData(var directoryName: String = "",
                     var infoMessage: String = "")

    class ConfigData(var colourSet: Int,
                     val isLoadingSupplier: () -> Boolean = { false })

    inner abstract class BindableViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindTo(t: Any)
    }

    private inner class HeaderViewHolder(view: View) : BindableViewHolder(view) {
        private val headerTextView: TextView = view.izbornikTopNazivKategorije
        private val headerTextViewNatpis: TextView = view.izbornikTopNatpis

        init {
            headerTextView.typeface = NovaEvaApp.openSansBold
            headerTextViewNatpis.typeface = NovaEvaApp.openSansBold
        }

        override fun bindTo(t: Any) {
            val headerData = t as HeaderData
            headerTextViewNatpis.text = headerData.infoMessage
            headerTextView.text = headerData.directoryName.toUpperCase()
        }
    }

    private inner class DirectoryInfoViewHolder(view: View) : BindableViewHolder(view) {
        val tvMapaNaslov: TextView = view.tvMapaNaslov
        val tvMapaNatpis: TextView = view.tvMapaNatpis

        init {
            tvMapaNaslov.typeface = NovaEvaApp.openSansBold
            tvMapaNatpis.typeface = NovaEvaApp.openSansBold
        }

        override fun bindTo(t: Any) {
            val directoryInfo = t as EvaDirectoryMetadata

            tvMapaNaslov.text = directoryInfo.title

            view.setOnClickListener {
                NovaEvaApp.bus.directoryOpenRequest.onNext(directoryInfo)
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

            view.setOnClickListener {
                NovaEvaApp.bus.contentOpenRequest.onNext(contentInfo)
            }
        }

    }

    private inner class ProgressBarViewHolder(view: View) : BindableViewHolder(view) {

        override fun bindTo(t: Any) {
            view.visibility = if (configData.isLoadingSupplier()) View.VISIBLE else View.GONE
        }
    }
}