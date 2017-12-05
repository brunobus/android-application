package hr.bpervan.novaeva.actions

import android.content.Context
import android.content.Intent
import hr.bpervan.novaeva.main.R

/**
 * Created by vpriscan on 04.12.17..
 */

fun sendEmailIntent(context: Context, subject: String, text: String, receivers: Array<String> = arrayOf()) {
    val mailIntent = Intent(Intent.ACTION_SEND)
    mailIntent.type = "message/rfc822"
    mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    mailIntent.putExtra(Intent.EXTRA_TEXT, text)
    if (receivers.isNotEmpty()) {
        mailIntent.putExtra(Intent.EXTRA_EMAIL, receivers)
    }
    context.startActivity(Intent.createChooser(mailIntent, context.getString(R.string.title_share_mail)))
}

fun shareIntent(context: Context, text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.title_share)))
}