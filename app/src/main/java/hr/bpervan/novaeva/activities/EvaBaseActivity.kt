package hr.bpervan.novaeva.activities

import android.support.v7.app.AppCompatActivity
import com.google.android.gms.analytics.GoogleAnalytics

/**
 * Created by vpriscan on 17.10.17..
 */
abstract class EvaBaseActivity : AppCompatActivity() {

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }
}