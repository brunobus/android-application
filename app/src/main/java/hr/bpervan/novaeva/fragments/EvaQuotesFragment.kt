package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.actions.sendEmailIntent
import hr.bpervan.novaeva.actions.shareIntent
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.BackgroundReplaceEvent
import hr.bpervan.novaeva.model.BackgroundType
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
    companion object :EvaFragmentFactory<EvaQuotesFragment, Long>{

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
        val quotesView = localInflater.inflate(R.layout.fragment_eva_quotes, container, false)
        //todo quotesView move

        if (contentTitle != null && contentData != null && contentId != -1L) {
            applyContent(quotesView)
        }

        applyFakeActionBarVisibility(quotesView)

        quotesView.btnToggleActionBar.setOnClickListener {
            showTools = !showTools
            applyFakeActionBarVisibility(quotesView)
        }

        quotesView.btnObnovi.setOnClickListener {
            fetchRandomQuote()
        }

        quotesView.options.btnShare.setOnClickListener {
            context?.let {
                shareIntent(it, "http://novaeva.com/node/$contentId")
            }
        }
        quotesView.options.btnMail.setOnClickListener {
            context?.let {
                sendEmailIntent(it, contentTitle!!, "http://novaeva.com/node/$contentId")
            }
        }

        quotesView.webText.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)

        return quotesView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RxEventBus.appBackground.onNext(BackgroundReplaceEvent(R.color.WhiteSmoke, BackgroundType.COLOR))
        RxEventBus.navigationAndStatusBarColor.onNext(R.color.Black)
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

    private fun fetchRandomQuote() {
        loadRandomQuoteDisposable = NovaEvaService.instance
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