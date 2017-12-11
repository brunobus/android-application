package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.Prayer
import kotlinx.android.synthetic.main.eva_collapsing_bar.view.*
import kotlinx.android.synthetic.main.eva_simple_content.view.*


class PrayerContentFragment : EvaBaseFragment() {

    companion object {
        fun newInstance(prayer: Prayer): PrayerContentFragment {
            return PrayerContentFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("prayer", prayer)
                }
            }
        }
    }

    private lateinit var prayer: Prayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: arguments!!
        prayer = inState.getParcelable("prayer")

//        val mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
//        mGaTracker.send(
//                HitBuilders.EventBuilder()
//                        .setCategory("Molitvenik")
//                        .setAction("OtvorenaMolitva")
//                        .setLabel(intent.getStringExtra("id"))
//                        .build()
//        )

        //mGaTracker.sendEvent("Molitvenik", "OtvorenaMolitva", getIntent().getStringExtra("id"), null);

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("prayer", prayer)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.eva_simple_content, container, false).apply {
            webView.settings.builtInZoomControls = true
            webView.settings.displayZoomControls = false

//            webView.settings.setSupportZoom(true)
            webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            webView.isScrollbarFadingEnabled = true
            webView.setOnLongClickListener { true }
            webView.isLongClickable = false
            webView.settings.defaultTextEncodingName = "utf-8"

//            webView.webViewClient = object : WebViewClient() {
//                override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
//                    webView.loadUrl(url)
//                    return false
//                }
//            }

            evaCollapsingBar.collapsingToolbar.title = prayer.prayerTitle

            webView.loadUrl(prayer.prayerUrl)
        }
    }
}
