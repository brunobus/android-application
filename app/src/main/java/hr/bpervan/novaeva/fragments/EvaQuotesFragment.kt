package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.actions.sendEmailIntent
import hr.bpervan.novaeva.actions.shareIntent
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.toolbar_eva_quotes.view.*
import kotlinx.android.synthetic.main.fragment_eva_quotes.view.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaQuotesFragment : EvaBaseFragment() {
    companion object {
        fun newInstance(): EvaQuotesFragment {
            return EvaQuotesFragment()
        }
    }

    private var contentTitle: String? = null
    private var contentData: String? = null
    private var contentId: Long = -1

    private var loadRandomIzrekaDisposable: Disposable? = null

    private var showTools = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (savedInstanceState != null) {
            contentTitle = savedInstanceState.getString("contentTitle")
            contentData = savedInstanceState.getString("contentData")
            contentId = savedInstanceState.getLong("contentId", -1L)
        } else {
            NovaEvaApp.instance?.getTracker(NovaEvaApp.TrackerName.APP_TRACKER)?.send(
                    HitBuilders.EventBuilder()
                            .setCategory("Kategorije")
                            .setAction("OtvorenaKategorija")
                            .setLabel(EvaCategory.IZREKE.rawName)
                            .build()
            )
        }

        if (contentTitle == null || contentData == null || contentId == -1L) {
            fetchRandomQuote()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val izrekeView = inflater.inflate(R.layout.fragment_eva_quotes, container, false)

        if (contentTitle != null && contentData != null && contentId != -1L) {
            applyContent(izrekeView)
        }

        applyFakeActionBarVisibility(izrekeView)

        izrekeView.btnToggleActionBar.setOnClickListener {
            showTools = !showTools
            applyFakeActionBarVisibility(izrekeView)
        }

        izrekeView.btnObnovi.setOnClickListener {
            fetchRandomQuote()
        }

        izrekeView.options.btnShare.setOnClickListener {
            context?.let {
                shareIntent(it, "http://novaeva.com/node/$contentId")
            }
        }
        izrekeView.options.btnMail.setOnClickListener {
            context?.let {
                sendEmailIntent(it, contentTitle!!, "http://novaeva.com/node/$contentId")
            }
        }

        izrekeView.webText.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)

        return izrekeView
    }

    private fun applyFakeActionBarVisibility(view: View) {
        if (showTools) {
            view.options.visibility = View.VISIBLE
            view.btnToggleActionBar.setImageResource(R.drawable.action_button_toolbar_hide)
        } else {
            view.options.visibility = View.GONE
            view.btnToggleActionBar.setImageResource(R.drawable.action_button_toolbar_show)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("contentTitle", contentTitle)
        outState.putString("contentData", contentData)
        outState.putLong("contentId", contentId)

        super.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        loadRandomIzrekaDisposable?.dispose()
        super.onDetach()
    }

    override fun onDestroy() {
        loadRandomIzrekaDisposable?.dispose()
        super.onDestroy()
    }

    private fun fetchRandomQuote() {
        loadRandomIzrekaDisposable?.dispose()

        loadRandomIzrekaDisposable = NovaEvaService.instance
                .getRandomDirectoryContent(1)
                .subscribeAsync({ directoryContent ->
                    if (directoryContent.contentMetadataList != null && directoryContent.contentMetadataList.isNotEmpty()) {
                        val contentInfo = directoryContent.contentMetadataList[0]

                        contentTitle = contentInfo.title
                        contentData = contentInfo.text
                        contentId = contentInfo.contentId

                        view?.let {
                            applyContent(it)
                        }
                    }
                }) {
                    view?.let { view ->
                        Snackbar.make(view, "Internetska veza nije dostupna", Snackbar.LENGTH_SHORT).show()
                    }
                }
    }

    private fun applyContent(izrekeView: View) {
        izrekeView.evaCollapsingBar.collapsingToolbar.title = contentTitle ?: ""
        izrekeView.webText.loadDataWithBaseURL(null, contentData, "text/html", "utf-8", "")
    }
}