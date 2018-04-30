package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.util.SCROLL_PERCENT_KEY
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.views.afterLoadAndLayoutComplete
import hr.bpervan.novaeva.views.applyEvaConfiguration
import hr.bpervan.novaeva.views.calcScrollYAbsolute
import hr.bpervan.novaeva.views.calcScrollYPercent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Informacije")
                        .setAction("OtvoreneInformacije")
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.AppTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.fragment_simple_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        val savedScrollPercent = savedInstanceState?.getFloat(SCROLL_PERCENT_KEY, 0f) ?: 0f
        if (savedScrollPercent > 0) {
            webView.afterLoadAndLayoutComplete {
                simpleContentScrollView.scrollY = calcScrollYAbsolute(savedScrollPercent, webView.height)
            }
        }

        initUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(SCROLL_PERCENT_KEY, simpleContentScrollView.calcScrollYPercent(webView.height))
        super.onSaveInstanceState(outState)
    }

    private fun initUI() {

        webView.applyEvaConfiguration(prefs)

        webView.loadUrl("file:///android_asset/info.htm")

        evaCollapsingBar.collapsingToolbar.title = "Nova Eva info"
    }
}