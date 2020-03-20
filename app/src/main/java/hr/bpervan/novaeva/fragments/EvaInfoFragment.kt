package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.applyConfiguredFontSize
import hr.bpervan.novaeva.views.applyEvaConfiguration
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_simple_content.*

/**
 *
 */
class EvaInfoFragment : EvaBaseFragment() {
    companion object : EvaFragmentFactory<EvaInfoFragment, Unit> {

        override fun newInstance(initializer: Unit): EvaInfoFragment {
            return EvaInfoFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme))
                .inflate(R.layout.fragment_simple_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        disposables += EventPipelines.resizeText.subscribe {
            webView?.applyConfiguredFontSize(prefs)
        }

        webView.applyEvaConfiguration(prefs)

        webView.loadUrl("file:///android_asset/info.html")

        val titleText = "${getString(R.string.app_name)} - v${BuildConfig.VERSION_NAME}" +
                if (BuildConfig.DEBUG) " (debug)" else ""

        evaCollapsingBar.collapsingToolbar.title = titleText
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Informacije", "Information")
    }
}