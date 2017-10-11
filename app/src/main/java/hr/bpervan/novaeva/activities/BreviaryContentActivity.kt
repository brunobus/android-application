package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ConnectionChecker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_brevijar_detalji.*

class BreviaryContentActivity : AppCompatActivity() {

    private var breviaryId: Int = -1

    private lateinit var mGaTracker: Tracker

    private var loadBreviaryDisposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brevijar_detalji)

        breviaryId = intent.getIntExtra("BREV_CAT", 4)

        mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Brevijar")
                        .setAction("OtvorenaMolitva")
                        .setLabel(breviaryId.toString())
                        .build()
        )

        //mGaTracker.sendEvent("Brevijar", "OtvorenaMolitva", breviaryId, null);

        if (ConnectionChecker.hasConnection(this)) {
            loadBreviary()
        } else {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show()
        }

        initUI()
    }

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    private fun initUI() {
        /*webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(true);
		webView.setBackgroundColor(color.background_light);*/
        webView.setOnLongClickListener { true }
        webView.isLongClickable = false
        setTitle()

        webView!!.settings.defaultTextEncodingName = "utf-8"
    }

    private fun setTitle() {
        var activityTitle = "Brevijar - "
        when (breviaryId) {
            1 -> activityTitle += "Jučer, Jutarnja"
            2 -> activityTitle += "Jučer, Večernja"
            3 -> activityTitle += "Jučer, Povečerje"
            4 -> activityTitle += "Danas, Jutarnja"
            5 -> activityTitle += "Danas, Večernja"
            6 -> activityTitle += "Danas, Povečerje"
            7 -> activityTitle += "Sutra, Jutarnja"
            8 -> activityTitle += "Sutra, Večernja"
            9 -> activityTitle += "Sutra, Povečerje"
        }
        this.title = activityTitle
    }

    private fun showErrorPopup() {
        val error = AlertDialog.Builder(this)
        error.setTitle("Greška")

        val tv = TextView(this)
        tv.text = "Greška pri dohvaćanju podataka sa poslužitelja"
        //tv.setTypeface(openSansRegular);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        error.setView(tv)

        error.setPositiveButton("Pokušaj ponovno") { _, _ -> loadBreviary() }
        error.setNegativeButton("Povratak") { _, _ ->
            NovaEvaApp.goHome(this)
        }
        error.show()
    }

    private fun loadBreviary() {
        Log.d("loadingBreviary", "loading breviary: " + breviaryId)

        loadBreviaryDisposable?.dispose()
        loadBreviaryDisposable = NovaEvaService.instance
                .getBreviary(breviaryId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ breviary ->
                    webView.loadDataWithBaseURL(null, breviary.text, "text/html", "utf-8", "")
                }) { t ->
                    Log.e("breviaryError", t.message, t)
                    showErrorPopup()
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadBreviaryDisposable?.dispose()
    }
}