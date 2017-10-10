package hr.bpervan.novaeva.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.Breviary;
import hr.bpervan.novaeva.services.NovaEvaService;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BrevijarDetaljiActivity extends Activity {

	private WebView webView;
	private String BREV_CAT;

	private Tracker mGaTracker;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_brevijar_detalji);

		BREV_CAT = String.valueOf(getIntent().getIntExtra("BREV_CAT", 4));

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Brevijar")
                        .setAction("OtvorenaMolitva")
                        .setLabel(BREV_CAT)
                        .build()
        );

		//mGaTracker.sendEvent("Brevijar", "OtvorenaMolitva", BREV_CAT, null);

		initUI();

		if(!ConnectionChecker.hasConnection(this)){
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(BrevijarDetaljiActivity.this,DashboardActivity.class));
		}else{
			loadBreviary();
		}
	}

    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {return true;}

	private void initUI(){
		webView = (WebView) findViewById(R.id.webViewBrevijar);
		/*webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(true);
		webView.setBackgroundColor(color.background_light);*/
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setLongClickable(false);
        setTitle();

		webView.getSettings().setDefaultTextEncodingName("utf-8");
	}

	private void setTitle(){
		String activityTitle = "Brevijar - ";
		switch(getIntent().getIntExtra("BREV_CAT", 4)){
		case 1:
			activityTitle += "Jučer, Jutarnja";
			break;
		case 2:
			activityTitle += "Jučer, Večernja";
			break;
		case 3:
			activityTitle += "Jučer, Povečerje";
			break;
		case 4:
			activityTitle += "Danas, Jutarnja";
			break;
		case 5:
			activityTitle += "Danas, Večernja";
			break;
		case 6:
			activityTitle += "Danas, Povečerje";
			break;
		case 7:
			activityTitle += "Sutra, Jutarnja";
			break;
		case 8:
			activityTitle += "Sutra, Večernja";
			break;
		case 9:
			activityTitle += "Sutra, Povečerje";
			break;
		}
		this.setTitle(activityTitle);
	}

	private void showErrorPopup(){
		AlertDialog.Builder error = new AlertDialog.Builder(this);
		error.setTitle("Greška");

		final TextView tv = new TextView(this);
		tv.setText("Greška pri dohvaćanju podataka sa poslužitelja");
		//tv.setTypeface(openSansRegular);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		error.setView(tv);

		error.setPositiveButton("Pokušaj ponovno", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				loadBreviary();
			}
		});
		error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				startActivity(new Intent(BrevijarDetaljiActivity.this, DashboardActivity.class));
				//TODO: Check integrity, 21.2.2014 12:25
				BrevijarDetaljiActivity.this.finish();
			}
			});
		error.show();
	}

	Disposable disposable;

	private void loadBreviary(){
		if (disposable != null) {
			disposable.dispose();
		}

		Log.d("loadingBreviary", "loading breviary: " + BREV_CAT);
		disposable = NovaEvaService.Companion.getInstance()
				.getBreviary(BREV_CAT)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Breviary>() {
					@Override
					public void accept(Breviary result) throws Exception {
						webView.loadDataWithBaseURL(null, result.getText(), "text/html", "utf-8", "");
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable t) throws Exception {
						Log.e("breviarError", t.getMessage(), t);
						showErrorPopup();
					}
				});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (disposable != null) {
			disposable.dispose();
		}
	}
}