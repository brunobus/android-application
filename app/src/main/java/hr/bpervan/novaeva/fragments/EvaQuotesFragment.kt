package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentEvaQuotesBinding
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.util.HAS_NEW_CONTENT_KEY_PREFIX
import hr.bpervan.novaeva.util.dataErrorSnackbar
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.applyConfiguredFontSize
import hr.bpervan.novaeva.views.applyEvaConfiguration
import hr.bpervan.novaeva.views.loadHtmlText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

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

    private var _viewBinding: FragmentEvaQuotesBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var quoteTitle: String? = null
    var quoteData: String? = null
    var quoteId: Long = -1L

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

        prefs.edit {
            remove("$HAS_NEW_CONTENT_KEY_PREFIX.${EvaDomain.QUOTES}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val newInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.IzrekeTheme))
        _viewBinding = FragmentEvaQuotesBinding.inflate(newInflater, container, false)
        return viewBinding.root
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

        disposables += EventPipelines.resizeText.subscribe {
            viewBinding.webText.applyConfiguredFontSize(prefs)
        }

        viewBinding.webText.applyEvaConfiguration(prefs)
        viewBinding.evaCollapsingBar.collapsingToolbar.title = context!!.getString(R.string.quotes)

        viewBinding.btnObnovi.setOnClickListener {
            fetchRandomQuote()
        }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Izreke", "Proverbs")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(QUOTE_TITLE_KEY, quoteTitle)
        outState.putString(QUOTE_DATA_KEY, quoteData)
        outState.putLong(QUOTE_ID_KEY, quoteId)

        super.onSaveInstanceState(outState)
    }

    private fun fetchRandomQuote() {
        loadRandomQuoteDisposable = NovaEvaService.v3.random()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { content ->
                    quoteTitle = content.title
                    quoteData = content.html ?: content.text ?: ""
                    quoteId = content.id

                    showQuote()
                }, onError = {
                    view?.dataErrorSnackbar()
                })
    }

    private fun showQuote() {
        viewBinding.webText.loadHtmlText(quoteData)
    }
}