package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.NovaEvaApp

/**
 * Created by vpriscan on 17.10.17..
 */
abstract class EvaBaseActivity : AppCompatActivity() {

    protected lateinit var prefs: SharedPreferences
        private set

    protected lateinit var imageLoader: ImageLoader
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)
        imageLoader = ImageLoader.getInstance()
    }

    inline protected fun showErrorPopup(throwable: Throwable, crossinline onTryAgain: () -> Unit) {
        Log.e("evaError", throwable.message, throwable)

        val error = AlertDialog.Builder(this)
        error.setTitle("Greška")

        val tv = TextView(this)
        tv.text = "Greška pri dohvaćanju podataka sa poslužitelja"
        NovaEvaApp.openSansRegular?.let {
            tv.typeface = it
        }
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        error.setView(tv)

        error.setPositiveButton("Pokušaj ponovno") { _, _ -> onTryAgain() }
        error.setNegativeButton("Povratak") { _, _ ->
            NovaEvaApp.goHome(this)
        }
        error.show()
    }
}