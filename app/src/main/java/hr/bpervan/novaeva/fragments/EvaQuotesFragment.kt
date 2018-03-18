package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.actions.sendEmailIntent
import hr.bpervan.novaeva.actions.shareIntent
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_eva_quotes.*
import kotlinx.android.synthetic.main.toolbar_eva_quotes.view.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaQuotesFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaQuotesFragment, Long> {

        val TAG: String = EvaQuotesFragment::class.java.canonicalName
        private const val INITIAL_QUOTE_ID_KEY = "initialQuoteId"
        override fun newInstance(initializer: Long): EvaQuotesFragment {
            return EvaQuotesFragment().apply {
                arguments = Bundle().apply {
                    putLong(INITIAL_QUOTE_ID_KEY, initializer) //todo read
                }
            }
        }
    }


    private var contentTitle: String? = null
    private var contentData: String? = null
    private var contentId: Long = -1

    private var loadRandomQuoteDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private var showTools = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            contentTitle = savedInstanceState.getString("contentTitle")
            contentData = savedInstanceState.getString("contentData")
            contentId = savedInstanceState.getLong("contentId", -1L)
        }

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Izreke")
                        .setAction("OtvoreneIzreke")
                        .build())

        prefs.edit().remove("newContentInCategory1").apply()

        if (contentTitle == null || contentData == null || contentId == -1L) {
            fetchRandomQuote()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.IzrekeTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.fragment_eva_quotes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {
        if (contentTitle != null && contentData != null && contentId != -1L) {
            applyContent()
        }

        btnObnovi.setOnClickListener {
            fetchRandomQuote()
        }

        options.btnShare.setOnClickListener {
            context?.let { ctx ->
                shareIntent(ctx, "http://novaeva.com/node/$contentId")
            }
        }
        options.btnMail.setOnClickListener {
            context?.let { ctx ->
                sendEmailIntent(ctx, contentTitle!!, "http://novaeva.com/node/$contentId")
            }
        }

        webText.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("contentTitle", contentTitle)
        outState.putString("contentData", contentData)
        outState.putLong("contentId", contentId)

        super.onSaveInstanceState(outState)
    }

    private fun fetchRandomQuote() {
        loadRandomQuoteDisposable = NovaEvaService.instance
                .getRandomDirectoryContent(1)
                .subscribeAsync({ directoryContent ->
                    if (directoryContent.contentMetadataList != null && directoryContent.contentMetadataList.isNotEmpty()) {
                        val contentInfo = directoryContent.contentMetadataList[0]

                        contentTitle = contentInfo.title
                        contentData = contentInfo.text
                        contentId = contentInfo.contentId

                        applyContent()
                    }
                }) {
                    view?.let { view ->
                        Snackbar.make(view, "Internetska veza nije dostupna", Snackbar.LENGTH_SHORT).show()
                    }
                }
    }

    private fun applyContent() {
        view ?: return
        evaCollapsingBar.collapsingToolbar.title = contentTitle ?: ""
        webText.loadDataWithBaseURL(null, contentData, "text/html", "utf-8", "")
    }
}