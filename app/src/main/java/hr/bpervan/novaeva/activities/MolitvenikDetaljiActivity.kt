package hr.bpervan.novaeva.activities

import android.R.color
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import kotlinx.android.synthetic.main.activity_molitvenik_detalji.*

class MolitvenikDetaljiActivity : Activity() {

    private var mGaTracker: Tracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_molitvenik_detalji)

        mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker!!.send(
                HitBuilders.EventBuilder()
                        .setCategory("Molitvenik")
                        .setAction("OtvorenaMolitva")
                        .setLabel(intent.getStringExtra("id"))
                        .build()
        )

        //mGaTracker.sendEvent("Molitvenik", "OtvorenaMolitva", getIntent().getStringExtra("id"), null);

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

    protected fun initUI() {
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isScrollbarFadingEnabled = true
        webView.setBackgroundColor(color.background_light)
        webView.setOnLongClickListener { true }
        webView.isLongClickable = false

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                webView.loadUrl(url)
                return false
            }
        }

        this.title = intent.getStringExtra("naslov")

        webView.settings.defaultTextEncodingName = "utf-8"

        var url = getUrl(Integer.parseInt(intent.getStringExtra("id")))

        url = "file:///android_asset/" + url!!
        webView.loadUrl(url)
    }

    private fun getUrl(id: Int): String? {
        var url: String? = null

        when (id) {
            0 -> url = "24_Najcesce_Koristene_Molitve.htm"
            1 -> url = "0_Uvod.htm"
            2 -> url = "1_Obrasci_vjere.htm"
            3 -> url = "2_Osnovne_molitve.htm"
            4 -> url = "3_Svagdanje_jutarnje_molitve.htm"
            5 -> url = "4_Svagdanje_vecernje_molitve.htm"
            6 -> url = "5_Prigodne_molitve.htm"
            7 -> url = "6_Molitve_mladih.htm"
            8 -> url = "7_Molitve_u_kusnji_i_napasti.htm"
            9 -> url = "8_Molitve_za_obitelj_i_roditelje.htm"
            10 -> url = "9_Molitve_za_bolesne_i_umiruce.htm"
            11 -> url = "10_Molitve_po_posebnim_nakanama.htm"
            12 -> url = "11_Molitve_svetih_i_velikih_ljudi.htm"
            13 -> url = "12_Kratke_molitve_i_zazivi.htm"
            14 -> url = "13_Molitve_Duhu_Svetome.htm"
            15 -> url = "14_Euharistijska_poboznost.htm"
            16 -> url = "15_Pomirenje.htm"
            17 -> url = "16_Poboznost_kriznog_puta.htm"
            18 -> url = "17_Deventica_i_krunica_bozanskom_milosrdu.htm"
            19 -> url = "18_Molitve_Blazenoj_Djevici_Mariji.htm"
            20 -> url = "19_Salezijanske_molitve.htm"
            21 -> url = "20_Molitve_mladih.htm"
            22 -> url = "21_Molitve_svetima.htm"
            23 -> url = "22_Lectio_Divina.htm"
            24 -> url = "23_Moliti_igrajuci_pred_Gospodinom.htm"
        }
        return url
    }
}
