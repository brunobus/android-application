package hr.bpervan.novaeva.customviews

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import hr.bpervan.novaeva.activities.ListaVijestiActivity
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory

/**
 * Created by vpriscan on 30.10.17..
 */

class AktualnoView(context: Context) : EvaBaseCustomView(context) {

    override fun backgroundResource(hasNewStuff: Boolean): Int {
        return if (hasNewStuff) {
            R.drawable.button_aktualno_news
            //todo declare hasNewStuff in xml, use selector to choose drawable
        } else {
            R.drawable.button_aktualno
        }
    }

    override fun intentToStartActivity(): Intent =
            Intent(context, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.AKTUALNO.id)
                putExtra("categoryName", EvaCategory.AKTUALNO.rawName)
                putExtra("themeId", R.style.MultimedijaTheme)
            }

    override fun textFeedback(): String {
        return if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            EvaCategory.AKTUALNO.rawName
        else {
            EvaCategory.AKTUALNO.rawNameVertical
        }
    }
}