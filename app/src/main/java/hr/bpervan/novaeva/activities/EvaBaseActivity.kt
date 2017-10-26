package hr.bpervan.novaeva.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R

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
}