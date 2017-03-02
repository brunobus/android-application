package hr.bpervan.novaeva.activities;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import android.R.color;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MolitvenikDetaljiActivity extends Activity {
	
	protected WebView webView;
	
	private Tracker mGaTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_molitvenik_detalji);

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Molitvenik")
                        .setAction("OtvorenaMolitva")
                        .setLabel(getIntent().getStringExtra("id"))
                        .build()
        );

		//mGaTracker.sendEvent("Molitvenik", "OtvorenaMolitva", getIntent().getStringExtra("id"), null);
		
		initUI();
		
	}

    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
	
	protected void initUI(){
		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(true);
		webView.setBackgroundColor(color.background_light);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setLongClickable(false);
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView webView, String url){
				webView.loadUrl(url);
				return false;
			}
		});
		
		this.setTitle(getIntent().getStringExtra("naslov"));

		webView.getSettings().setDefaultTextEncodingName("utf-8");
		
		String url = getUrl(Integer.parseInt(getIntent().getStringExtra("id")));
		
		url = "file:///android_asset/" + url;
		webView.loadUrl(url);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_molitvenik_detalji, menu);
		return true;
	}

	private String getUrl(int id){
		String url = null;
		
		switch(id){
		case 0:
			url = "24_Najcesce_Koristene_Molitve.htm";
			break;
		case 1:
			url = "0_Uvod.htm";
			break;
		case 2:
			url = "1_Obrasci_vjere.htm";
			break;
		case 3:
			url = "2_Osnovne_molitve.htm";
			break;
		case 4:
			url = "3_Svagdanje_jutarnje_molitve.htm";
			break;
		case 5:
			url = "4_Svagdanje_vecernje_molitve.htm";
			break;
		case 6:
			url = "5_Prigodne_molitve.htm";
			break;
		case 7:
			url = "6_Molitve_mladih.htm";
			break;
		case 8:
			url = "7_Molitve_u_kusnji_i_napasti.htm";
			break;
		case 9:
			url = "8_Molitve_za_obitelj_i_roditelje.htm";
			break;
		case 10:
			url = "9_Molitve_za_bolesne_i_umiruce.htm";
			break;
		case 11:
			url = "10_Molitve_po_posebnim_nakanama.htm";
			break;
		case 12:
			url = "11_Molitve_svetih_i_velikih_ljudi.htm";
			break;
		case 13:
			url = "12_Kratke_molitve_i_zazivi.htm";
			break;
		case 14:
			url = "13_Molitve_Duhu_Svetome.htm";
			break;
		case 15:
			url = "14_Euharistijska_poboznost.htm";
			break;
		case 16:
			url = "15_Pomirenje.htm";
			break;
		case 17:
			url = "16_Poboznost_kriznog_puta.htm";
			break;
		case 18:
			url = "17_Deventica_i_krunica_bozanskom_milosrdu.htm";
			break;
		case 19:
			url = "18_Molitve_Blazenoj_Djevici_Mariji.htm";
			break;
		case 20:
			url = "19_Salezijanske_molitve.htm";
			break;
		case 21:
			url = "20_Molitve_mladih.htm";
			break;
		case 22:
			url = "21_Molitve_svetima.htm";
			break;
		case 23:
			url = "22_Lectio_Divina.htm";
			break;
		case 24:
			url = "23_Moliti_igrajuci_pred_Gospodinom.htm";
			break;
		}
		return url;
	}
}
