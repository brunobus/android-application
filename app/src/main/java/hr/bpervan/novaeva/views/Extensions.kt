package hr.bpervan.novaeva.views

import android.content.SharedPreferences
import android.webkit.WebSettings
import android.webkit.WebView
import hr.bpervan.novaeva.defaultTextSize
import hr.bpervan.novaeva.textSizePrefKey

/**
 *
 */
fun WebView.applyEvaConfiguration(prefs: SharedPreferences) {
    settings.defaultFontSize = prefs.getInt(textSizePrefKey, defaultTextSize)
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