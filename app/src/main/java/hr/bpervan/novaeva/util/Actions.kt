package hr.bpervan.novaeva.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.core.widget.toast
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R

/**
 * Created by vpriscan on 04.12.17..
 */

fun sendEmailIntent(context: Context?, subject: String, text: String, receiver: String) {
    context ?: return

    try {
        val uriText = "mailto:$receiver" +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode("$text\n")

        val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriText))
        context.startActivity(Intent.createChooser(mailIntent, context.getString(R.string.title_share_mail)))

    } catch (e: Exception) {
        context.toast(context.getString(R.string.send_email_failed)).show()
    }
}

fun shareIntent(context: Context?, text: String) {
    context ?: return

    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.title_share)))

    } catch (e: Exception) {
        context.toast(context.getString(R.string.share_failed)).show()
    }
}

inline fun showFetchErrorDialog(throwable: Throwable?, context: Activity, crossinline onTryAgain: () -> Unit) {
    if (throwable != null) Log.e("evaError", throwable.message, throwable)

    val error = AlertDialog.Builder(context)
    error.setTitle(context.getString(R.string.error))

    val tv = TextView(context)
    tv.text = context.getString(R.string.error_fetching_data)

    NovaEvaApp.openSansRegular?.let { tv.typeface = it }

    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    error.setView(tv)

    error.setPositiveButton(context.getString(R.string.try_again)) { _, _ ->
        onTryAgain()
    }
    error.setNegativeButton(context.getString(R.string.go_back)) { _, _ ->
        EventPipelines.goHome.onNext(TransitionAnimation.RIGHTWARDS)
    }
    error.show()
}