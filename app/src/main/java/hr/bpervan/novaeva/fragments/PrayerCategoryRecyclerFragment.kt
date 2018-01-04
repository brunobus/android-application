package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.PrayerCategoryRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.Prayer
import hr.bpervan.novaeva.model.PrayerCategory
import kotlinx.android.synthetic.main.fragment_prayers.view.*
import kotlinx.android.synthetic.main.prayerbook_top.view.*

class PrayerCategoryRecyclerFragment : EvaBaseFragment() {

    companion object {
        fun newInstance(prayerCategory: PrayerCategory): PrayerCategoryRecyclerFragment {
            return PrayerCategoryRecyclerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("prayerCategory", prayerCategory)
                }
            }
        }
    }

    private lateinit var prayerCategory: PrayerCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        val inState = savedInstanceState ?: arguments!!
        prayerCategory = inState.getParcelable("prayerCategory")

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
        outState.putParcelable("prayerCategory", prayerCategory)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val prayerList = generateSequence { Prayer(prayerCategory.title, prayerCategory.url) }.take(10).toList() //TODO FIXME split prayer URLs

        return inflater.inflate(R.layout.fragment_prayers, container, false).apply {

            val recyclerView = evaRecyclerView as RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = PrayerCategoryRecyclerAdapter(prayerList)

            prayerTitleTextView.text = prayerCategory.title
            prayerTitleTextView.typeface = NovaEvaApp.openSansBold

//            webView.settings.builtInZoomControls = true
//            webView.settings.displayZoomControls = false
//
////            webView.settings.setSupportZoom(true)
//            webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
//            webView.isScrollbarFadingEnabled = true
//            webView.setOnLongClickListener { true }
//            webView.isLongClickable = false
//            webView.settings.defaultTextEncodingName = "utf-8"
//
////            webView.webViewClient = object : WebViewClient() {
////                override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
////                    webView.loadUrl(url)
////                    return false
////                }
////            }
//
//            evaCollapsingBar.collapsingToolbar.title = prayerCategory.title
//
//            webView.loadUrl(prayerCategory.url)
        }
    }
}
