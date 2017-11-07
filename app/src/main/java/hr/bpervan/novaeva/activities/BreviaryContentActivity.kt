package hr.bpervan.novaeva.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ConnectionChecker
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.eva_simple_content.*
import kotlinx.android.synthetic.main.eva_collapsing_bar.view.*

class BreviaryContentActivity : EvaBaseActivity() {

    private var breviaryId: Int = -1

    private lateinit var mGaTracker: Tracker

    private var loadBreviaryDisposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.eva_simple_content)

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
        initUI()

        if (ConnectionChecker.hasConnection(this)) {
            loadBreviary()
        } else {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show()
        }

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

//        webView.settings.builtInZoomControls = true
//        webView.settings.displayZoomControls = false

        /*webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(true);
		webView.setBackgroundColor(color.background_light);*/
        webView.setOnLongClickListener { true }
        webView.isLongClickable = false
        setTitle()

        val coverImage = evaCollapsingBar.collapsingToolbar.coverImage

        val headerUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null)

        if (headerUrl != null && coverImage != null) {
            imageLoader.displayImage(headerUrl, coverImage, ImageLoaderConfigurator.createDefaultDisplayImageOptions(true))
        }

//        webView!!.settings.defaultTextEncodingName = "utf-8"
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
        evaCollapsingBar.collapsingToolbar.title = activityTitle
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
                }) {
                    NovaEvaApp.showFetchErrorDialog(it, this) { loadBreviary() }
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadBreviaryDisposable?.dispose()
    }
}