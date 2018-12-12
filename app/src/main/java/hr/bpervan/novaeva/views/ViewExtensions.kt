package hr.bpervan.novaeva.views

import android.content.SharedPreferences
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.widget.NestedScrollView
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ui.PlayerView
import hr.bpervan.novaeva.util.TEXT_SIZE_KEY
import hr.bpervan.novaeva.util.defaultTextSize

/**
 *
 */
fun WebView.applyEvaConfiguration(prefs: SharedPreferences) {
    applyConfiguredFontSize(prefs)
    settings.builtInZoomControls = false
    settings.displayZoomControls = false
    settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN

    isLongClickable = true
}

fun WebView.applyConfiguredFontSize(prefs: SharedPreferences){
    settings.defaultFontSize = prefs.getInt(TEXT_SIZE_KEY, defaultTextSize)
}

fun PlayerView.applyEvaConfiguration() {
    isVisible = true
    useController = true
    controllerAutoShow = true
    controllerShowTimeoutMs = 0
    controllerHideOnTouch = false
    setControllerHideDuringAds(false)
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

fun View.snackbar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, resId, duration).show()
}