package hr.bpervan.novaeva.views

import android.content.SharedPreferences
import android.support.v4.view.ViewCompat
import android.support.v4.widget.NestedScrollView
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import hr.bpervan.novaeva.defaultTextSize
import hr.bpervan.novaeva.TEXT_SIZE_KEY

/**
 *
 */
fun WebView.applyEvaConfiguration(prefs: SharedPreferences) {
    settings.defaultFontSize = prefs.getInt(TEXT_SIZE_KEY, defaultTextSize)
    settings.builtInZoomControls = false
    settings.displayZoomControls = false
    settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN

    //prevent C/P
    setOnLongClickListener { true }
    isLongClickable = false


}

fun WebView.loadHtmlText(text: String?) {
    text ?: return
    loadDataWithBaseURL(null, text, "text/html", "utf-8", null)
}

fun View.onLayoutComplete(action: () -> Unit) {
    if (ViewCompat.isLaidOut(this)) {
        action()
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (ViewCompat.isLaidOut(this@onLayoutComplete)) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    action()
                }
            }
        })
    }
}

fun NestedScrollView.calcScrollYPercent(maxChildHeight: Int): Float {
    return if (maxChildHeight > 0) {
        (scrollY.toFloat() / maxChildHeight.toFloat()) * 100
    } else {
        0F
    }
}

fun calcScrollYAbsolute(scrollYPercent: Float, maxChildHeight: Int): Int {
    return ((scrollYPercent / 100) * maxChildHeight).toInt()
}

fun WebView.afterLoadAndLayoutComplete(action: () -> Unit) {
    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLayoutComplete {
                action()
            }
            webViewClient = null
        }
    }
}