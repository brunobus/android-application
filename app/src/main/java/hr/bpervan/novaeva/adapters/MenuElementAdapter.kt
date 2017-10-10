package hr.bpervan.novaeva.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.ListTypes
import hr.bpervan.novaeva.utilities.ResourceHandler
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by vpriscan on 08.10.17..
 */
class MenuElementAdapter(val data: List<TreeElementInfo>, val metadata: Metadata) : RecyclerView.Adapter<MenuElementAdapter.BindableViewHolder>() {

    private val cal = Calendar.getInstance()

    companion object {
        val HEADER_VIEW_TYPE = 0
        val CONTENT_VIEW_TYPE = 1
        val SUBDIRECTORY_VIEW_TYPE = 2
        val PROGRESS_VIEW_TYPE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_VIEW_TYPE
        else if (position == itemCount - 1) PROGRESS_VIEW_TYPE
        else if (data[position - 1] is ContentInfo) CONTENT_VIEW_TYPE
        else SUBDIRECTORY_VIEW_TYPE
    }

    override fun getItemCount(): Int = data.size + 2 /*(1 for header and 1 for progressbar)*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder =
            when (viewType) {
                HEADER_VIEW_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.izbornik_top, parent, false)
                    view.setBackgroundResource(ResourceHandler.getListViewHeader(metadata.colourSet, metadata.deviceOrientation))
                    HeaderViewHolder(view)
                }
                CONTENT_VIEW_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.vijest_row, parent, false)
                    view.setBackgroundResource(ResourceHandler.getResourceId(metadata.colourSet, ListTypes.VIJEST, metadata.deviceOrientation))
                    ContentInfoViewHolder(view)
                }
                SUBDIRECTORY_VIEW_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_row, parent, false)
                    view.setBackgroundResource(ResourceHandler.getResourceId(metadata.colourSet, ListTypes.PODKATEGORIJA, metadata.deviceOrientation))
                    DirectoryInfoViewHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.progress_row, parent, false)
                    ProgressBarViewHolder(view)
                }
            }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        val subject: Any =
                when {
                    position == 0 -> metadata
                    position == itemCount - 1 -> metadata
                    else -> data[position - 1]
                }
        holder.bindTo(subject)
    }

    class Metadata(val directoryId: Long,
                   val directoryName: String,
                   val infoMessage: String,
                   val deviceOrientation: Int,
                   val colourSet: Int,
                   val loading: AtomicBoolean)

    inner abstract class BindableViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindTo(t: Any)
    }

    private inner class HeaderViewHolder(view: View) : BindableViewHolder(view) {
        private val headerTextView: TextView = view.findViewById(R.id.izbornikTopNazivKategorije)
        private val headerTextViewNatpis: TextView = view.findViewById(R.id.izbornikTopNatpis)

        init {
            headerTextView.typeface = NovaEvaApp.openSansBold
            headerTextViewNatpis.typeface = NovaEvaApp.openSansBold
        }

        override fun bindTo(t: Any) {
            val metadata = t as Metadata
            headerTextViewNatpis.text = metadata.infoMessage
            headerTextView.text = metadata.directoryName.toUpperCase()

            view.setOnClickListener(null)
        }
    }

    private inner class DirectoryInfoViewHolder(view: View) : BindableViewHolder(view) {
        val tvNaslov: TextView = view.findViewById(R.id.tvNaslov)
        val tvMapaNatpis: TextView = view.findViewById(R.id.tvMapaNatpis)

        override fun bindTo(t: Any) {
            val directoryInfo = t as DirectoryInfo

            tvNaslov.typeface = NovaEvaApp.openSansBold
            tvMapaNatpis.typeface = NovaEvaApp.openSansBold
            tvNaslov.text = directoryInfo.title

            view.setOnClickListener {
                NovaEvaApp.bus.directoryOpenRequest.onNext(directoryInfo)
            }
        }

    }

    private inner class ContentInfoViewHolder(view: View) : BindableViewHolder(view) {
        val tvNaslov: TextView = view.findViewById(R.id.tvNaslov)
        val tvUvod: TextView = view.findViewById(R.id.tvUvod)
        val tvDatum: TextView = view.findViewById(R.id.tvDatum)
        val tvGodinaSatMinuta: TextView = view.findViewById(R.id.tvGodinaSatMinuta)

        val imgHasLink: ImageView = view.findViewById(R.id.imgViewLink)
        val imgHasTxt: ImageView = view.findViewById(R.id.imgViewTxt)
        val imgHasAudio: ImageView = view.findViewById(R.id.imgViewMp3)

        val tvUvodNatpis: TextView = view.findViewById(R.id.tvUvodNatpis)

        override fun bindTo(t: Any) {
            val contentInfo = t as ContentInfo

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

            //todo refactor and simplify this old code

            tvNaslov.text = contentInfo.title
            cal.timeInMillis = 1000 * java.lang.Long.parseLong(contentInfo.datetime)

            val sat: String
            val minuta: String
            val dan: String
            val mjesec: String
            val godina: String = cal.get(Calendar.YEAR).toString()

            //sat = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

            if (cal.get(Calendar.HOUR_OF_DAY) < 10) {
                sat = "0" + cal.get(Calendar.HOUR_OF_DAY).toString()
            } else {
                sat = cal.get(Calendar.HOUR_OF_DAY).toString()
            }

            if (cal.get(Calendar.MINUTE) < 10) {
                minuta = "0" + cal.get(Calendar.MINUTE).toString()
            } else {
                minuta = cal.get(Calendar.MINUTE).toString()
            }

            //dan = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

            if (cal.get(Calendar.DAY_OF_MONTH) < 9) {
                dan = "0" + (cal.get(Calendar.DAY_OF_MONTH) + 1).toString()
            } else {
                dan = (cal.get(Calendar.DAY_OF_MONTH) + 1).toString()
            }

            if (cal.get(Calendar.MONTH) < 9) {
                mjesec = "0" + (cal.get(Calendar.MONTH) + 1).toString()
            } else {
                mjesec = (cal.get(Calendar.MONTH) + 1).toString()
            }
            /** Pitanje je kako će se u APIu ovo mapirati youtube - video itd. */

            if (contentInfo.attach != null) {
                if (contentInfo.attach.hasVideo) {
                    imgHasLink.visibility = View.VISIBLE
                }
                if (contentInfo.attach.hasMusic) {
                    imgHasAudio.visibility = View.VISIBLE
                }
                if (contentInfo.attach.hasDocuments) {
                    imgHasTxt.visibility = View.VISIBLE
                }
            }

            tvGodinaSatMinuta.text = "$godina, $sat:$minuta"
            tvDatum.text = "$dan.$mjesec."
            tvUvod.text = contentInfo.summary

            view.setOnClickListener {
                NovaEvaApp.bus.contentOpenRequest.onNext(contentInfo)
            }
        }

    }

    private inner class ProgressBarViewHolder(view: View) : BindableViewHolder(view) {

        override fun bindTo(t: Any) {
            val metadata = t as Metadata
            view.visibility = if (metadata.loading.get()) View.VISIBLE else View.GONE
            /**/
        }
    }
}