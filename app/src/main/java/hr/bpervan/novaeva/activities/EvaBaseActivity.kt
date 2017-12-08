package hr.bpervan.novaeva.activities

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.analytics.GoogleAnalytics
import com.nostra13.universalimageloader.core.ImageLoader

/**
 * Created by vpriscan on 17.10.17..
 */
abstract class EvaBaseActivity : AppCompatActivity() {

    protected val prefs: SharedPreferences by lazy {
        getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)
    }

    protected val imageLoader: ImageLoader by lazy {
        ImageLoader.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }
}