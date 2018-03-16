package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContextType
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.eva_simple_content.*

/**
 *
 */
class EvaInfoFragment : EvaBaseFragment() {
    companion object : EvaFragmentFactory<EvaInfoFragment, Unit> {

        override fun newInstance(initializer: Unit): EvaInfoFragment {
            return EvaInfoFragment()
        }
    }

    override val evaContextType = EvaContextType.CONTENT

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
        return localInflater.inflate(R.layout.eva_simple_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {

        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.loadUrl("file:///android_asset/info.htm")

        evaCollapsingBar.collapsingToolbar.title = "Nova Eva info"
    }
}