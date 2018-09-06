package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.util.dataErrorSnackbar
import hr.bpervan.novaeva.util.networkRequest
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.applyConfiguredFontSize
import hr.bpervan.novaeva.views.applyEvaConfiguration
import hr.bpervan.novaeva.views.loadHtmlText
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_simple_content.*

/**
 * Created by vpriscan on 26.11.17..
 */
class BreviaryContentFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<BreviaryContentFragment, Int> {

        private const val BREVIARY_ID_KEY = "breviaryId"
        override fun newInstance(initializer: Int): BreviaryContentFragment {
            return BreviaryContentFragment().apply {
                arguments = bundleOf(BREVIARY_ID_KEY to initializer)
            }
        }

        private var savedBreviaryText: String? = null
    }

    private var breviaryId: Int = -1
    private lateinit var breviaryName: String
    private var breviaryText: String? = null
    private var coverImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            savedBreviaryText = null
        }

        val inState = savedInstanceState ?: arguments!!
        breviaryId = inState.getInt(BREVIARY_ID_KEY, 4)
        coverImageUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null)
        breviaryName = "Brevijar - " + when (breviaryId) {
            1 -> "Jučer, Jutarnja"
            2 -> "Jučer, Večernja"
            3 -> "Jučer, Povečerje"
            4 -> "Danas, Jutarnja"
            5 -> "Danas, Večernja"
            6 -> "Danas, Povečerje"
            7 -> "Sutra, Jutarnja"
            8 -> "Sutra, Večernja"
            else -> "Sutra, Povečerje"
        }

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Brevijar")
                        .setAction("OtvorenBrevijar")
                        .setLabel(breviaryName)
                        .setValue(breviaryId.toLong())
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.BreviaryTheme))
                .inflate(R.layout.fragment_simple_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeWindowBackgroundDrawable.onNext(NovaEvaApp.defaultBreviaryBackground)
        EventPipelines.changeNavbarColor.onNext(R.color.Transparent)
        EventPipelines.changeStatusbarColor.onNext(R.color.Transparent)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        if (savedInstanceState != null && savedBreviaryText != null) {
            breviaryText = savedBreviaryText
            showBreviary()
        } else {
            fetchBreviary()
        }

        disposables += EventPipelines.resizeText.subscribe {
            webView?.applyConfiguredFontSize(prefs)
        }

        webView.applyEvaConfiguration(prefs)

        evaCollapsingBar.collapsingToolbar.title = breviaryName

        val coverImageView = evaCollapsingBar.coverImage

        if (coverImageUrl != null && coverImageView != null) {
            imageLoader.displayImage(coverImageUrl, coverImageView)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BREVIARY_ID_KEY, breviaryId)
        savedBreviaryText = breviaryText
        super.onSaveInstanceState(outState)
    }

    private fun fetchBreviary() {

        disposables += novaEvaService
                .getBreviary(breviaryId.toString())
                .networkRequest({ breviary ->
                    view ?: return@networkRequest
                    breviaryText = breviary.text ?: ""
                    showBreviary()
                }) {
                    view?.dataErrorSnackbar()
                }
    }

    private fun showBreviary() {
        webView.loadHtmlText(breviaryText)
    }
}
