package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.rest.novaEvaServiceV2
import hr.bpervan.novaeva.util.NEW_CONTENT_KEY_PREFIX
import hr.bpervan.novaeva.util.networkRequest
import hr.bpervan.novaeva.views.loadHtmlText
import hr.bpervan.novaeva.views.snackbar
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_eva_quotes.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaQuotesFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaQuotesFragment, Long> {

        private const val QUOTE_ID_KEY = "quoteId"
        private const val QUOTE_TITLE_KEY = "quoteTitle"
        private const val QUOTE_DATA_KEY = "quoteData"

        override fun newInstance(initializer: Long): EvaQuotesFragment {
            return EvaQuotesFragment().apply {
                arguments = bundleOf(QUOTE_ID_KEY to initializer)
            }
        }
    }

    private var quoteTitle: String? = null
    private var quoteData: String? = null
    private var quoteId: Long = -1

    private var loadRandomQuoteDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            quoteId = savedInstanceState.getLong(QUOTE_ID_KEY, -1L)
            quoteTitle = savedInstanceState.getString(QUOTE_TITLE_KEY)
            quoteData = savedInstanceState.getString(QUOTE_DATA_KEY)
        }

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Izreke")
                        .setAction("OtvoreneIzreke")
                        .build())

        prefs.edit {
            remove("${NEW_CONTENT_KEY_PREFIX}1")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.IzrekeTheme))
                .inflate(R.layout.fragment_eva_quotes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        if (quoteTitle == null || quoteData == null || quoteId == -1L) {
            fetchRandomQuote()
        } else {
            showQuote()
        }

        initUI()
    }

    private fun initUI() {
        val ctx = context ?: return

        btnObnovi.setOnClickListener {
            fetchRandomQuote()
        }

        //todo move to options drawer
//        options.btnShare.setOnClickListener {
//            shareIntent(ctx, "http://novaeva.com/node/$contentId")
//        }
//        options.btnMail.setOnClickListener {
//            sendEmailIntent(ctx, quoteTitle!!, "http://novaeva.com/node/$contentId")
//        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(QUOTE_TITLE_KEY, quoteTitle)
        outState.putString(QUOTE_DATA_KEY, quoteData)
        outState.putLong(QUOTE_ID_KEY, quoteId)

        super.onSaveInstanceState(outState)
    }

    private fun fetchRandomQuote() {
//        loadRandomQuoteDisposable = novaEvaServiceV2.getRandomDirectoryContent(1)
//                .networkRequest({ directoryContent ->
//                    val contentMetadataList = directoryContent.contentMetadataList
//                    if (contentMetadataList != null && contentMetadataList.isNotEmpty()) {
//                        val quoteInfo = contentMetadataList[0]
//
//                        quoteTitle = quoteInfo.title
//                        quoteData = quoteInfo.text
//                        quoteId = quoteInfo.contentId
//
//                        showQuote()
//                    }
//                }, onError = {
//                    view?.snackbar(R.string.network_unavailable, Snackbar.LENGTH_SHORT)
//                })
    }

    private fun showQuote() {
        view ?: return
        evaCollapsingBar.collapsingToolbar.title = quoteTitle ?: ""
        webText.loadHtmlText(quoteData)
    }
}