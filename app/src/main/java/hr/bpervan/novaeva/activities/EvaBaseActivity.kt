package hr.bpervan.novaeva.activities

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.analytics.GoogleAnalytics
import hr.bpervan.novaeva.NovaEvaApp

/**
 * Created by vpriscan on 17.10.17..
 */
abstract class EvaBaseActivity : AppCompatActivity() {

    protected val prefs: SharedPreferences
        get() = NovaEvaApp.prefs

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }
}