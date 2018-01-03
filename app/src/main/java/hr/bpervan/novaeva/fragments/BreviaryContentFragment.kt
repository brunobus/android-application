package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.eva_simple_content.view.*

/**
 * Created by vpriscan on 26.11.17..
 */
class BreviaryContentFragment : EvaBaseFragment() {
    companion object {
        private val BREVIARY_ID_KEY = "breviaryId"

        fun newInstance(breviaryId: Int): BreviaryContentFragment {
            return BreviaryContentFragment().apply {
                arguments = Bundle().apply {
                    putInt(BREVIARY_ID_KEY, breviaryId)
                }
            }
        }
    }

    private var breviaryId: Int = -1
    private var coverImageUrl: String? = null

    private var breviaryText: String? = null

    private var loadBreviaryDisposable: Disposable? = null

    /*webview instance is being cached because its content can only be loaded asynchronously and his parent scrollview can't restore its scroll position*/
    private var cachedWebViewInstance: WebView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        val inState = savedInstanceState ?: arguments!!
        breviaryId = inState.getInt(BREVIARY_ID_KEY, 4)
        coverImageUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null)

        fetchBreviary()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.eva_simple_content, container, false).apply {
            val cachedWebViewInst: WebView? = cachedWebViewInstance
            if (cachedWebViewInst == null) {

                cachedWebViewInstance = webView.apply {
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    setOnLongClickListener { true }
                    isLongClickable = false
//                    setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//                    setScrollbarFadingEnabled(true);
//                    settings.defaultTextEncodingName = "utf-8"
//                    setBackgroundColor(color.background_light);
                }

                if (breviaryText != null) {
                    loadBreviary()
                }

            } else {
                val nestedScrollView = simpleContentScrollView as NestedScrollView
                nestedScrollView.removeAllViews()
                nestedScrollView.addView(cachedWebViewInst)
            }

            val title = "Brevijar - " + when (breviaryId) {
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
            evaCollapsingBar.collapsingToolbar.title = title

            val coverImageView = evaCollapsingBar.coverImage

            if (coverImageUrl != null && coverImageView != null) {
                imageLoader.displayImage(coverImageUrl, coverImageView, ImageLoaderConfigurator.createDefaultDisplayImageOptions(true))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BREVIARY_ID_KEY, breviaryId)

        super.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        super.onDetach()
        loadBreviaryDisposable?.dispose()
        (cachedWebViewInstance?.parent as? NestedScrollView)?.removeAllViews()
        cachedWebViewInstance?.invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadBreviaryDisposable?.dispose()
        cachedWebViewInstance = null
    }

    private fun fetchBreviary() {
        Log.d("fetchingBreviary", "fetching breviary: " + breviaryId)

        loadBreviaryDisposable?.dispose()
        loadBreviaryDisposable = NovaEvaService.instance
                .getBreviary(breviaryId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ breviary ->
                    breviaryText = breviary.text
                    loadBreviary()
                }) {
                    context?.let { ctx ->
                        NovaEvaApp.showFetchErrorSnackbar(it, ctx, view)
                    }
                }
    }

    private fun loadBreviary() {
        cachedWebViewInstance?.loadDataWithBaseURL(null, breviaryText, "text/html", "utf-8", "")
    }
}
