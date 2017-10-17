package hr.bpervan.novaeva.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.List;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.ContentInfo;
import hr.bpervan.novaeva.model.DirectoryContent;
import hr.bpervan.novaeva.services.NovaEvaService;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.EvaCategory;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

//import com.google.analytics.tracking.android.EasyTracker;

public class IzrekeActivity extends EvaBaseActivity implements OnClickListener{

	private TextView tvNaslov, tvKategorija;

	/** Test test test */
	private WebView webView;

	private String naslov,tekst;
	private long nid = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPrefs().edit().putInt("vidjenoKategorija1", 1).apply();

		Tracker mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);

		mGaTracker.send(
				new HitBuilders.EventBuilder()
						.setCategory("Kategorije")
						.setAction("OtvorenaKategorija")
						.setLabel(EvaCategory.IZREKE.getRawName())
						.build()
		);

		initUI();

		loadRandomIzreka();
	}

	private Disposable randomIzrekaDisposable;

	public void loadRandomIzreka(){
		if (randomIzrekaDisposable != null) {
			randomIzrekaDisposable.dispose();
		}
		if(ConnectionChecker.hasConnection(this)) {

			randomIzrekaDisposable = NovaEvaService.Companion.getInstance()
					.getRandomDirectoryContent(1)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Consumer<DirectoryContent>() {
						@Override
						public void accept(DirectoryContent directoryContent) throws Exception {
							List<ContentInfo> contentInfoList = directoryContent.getContentInfoList();
							if (contentInfoList != null && !contentInfoList.isEmpty()) {
								ContentInfo randomIzreka = contentInfoList.get(0);

								naslov = randomIzreka.getTitle();
								tekst = randomIzreka.getText();
								nid = randomIzreka.getContentId();

								tvNaslov.setText(naslov);
								webView.loadDataWithBaseURL(null, tekst, "text/html", "UTF-8", "");
							}
						}
					}, new Consumer<Throwable>() {
						@Override
						public void accept(Throwable t) throws Exception {
							showErrorPopup(t, new Function0<Unit>() {
								@Override
								public Unit invoke() {
									loadRandomIzreka();
									return null;
								}
							});
						}
					});
		} else {
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
		}
	}

	private void initUI(){
		setContentView(R.layout.activity_izreke);

		tvNaslov = (TextView)findViewById(R.id.tvNaslov);
		tvKategorija = (TextView)findViewById(R.id.tvKategorija);

		webView = (WebView) findViewById(R.id.webText);
		webView.getSettings().setDefaultTextEncodingName("utf-8");

		tvKategorija.setText("Izreke");

		View fakeActionBar = findViewById(R.id.fakeActionBar);

		findViewById(R.id.btnTextPlus).setOnClickListener(this);
		findViewById(R.id.btnObnovi).setOnClickListener(this);

		fakeActionBar.findViewById(R.id.btnHome).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnShare).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnMail).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnSearch).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnBack).setOnClickListener(this);

		webView.getSettings().setDefaultFontSize(getPrefs().getInt("hr.bpervan.novaeva.velicinateksta", 14));

        getWindow().getDecorView().setBackgroundColor(this.getResources().getColor(android.R.color.background_light));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);

		initUI();

		tvNaslov.setText(naslov);
		webView.loadDataWithBaseURL(null, tekst, "text/html", "UTF-8", "");
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (randomIzrekaDisposable != null) {
			randomIzrekaDisposable.dispose();
		}
	}

	@Override
	public void onClick(View v) {
		int vId = v.getId();
		if (vId == R.id.btnObnovi) {
			loadRandomIzreka();

		} else if (vId == R.id.btnHome) {
			NovaEvaApp.Companion.goHome(this);

		} else if (vId == R.id.btnSearch) {
			if (ConnectionChecker.hasConnection(this))
				showSearchPopup();

		} else if (vId == R.id.btnBookmark) {
		} else if (vId == R.id.btnShare) {
			CharSequence temp = "http://novaeva.com/node/" + nid;
			Intent faceIntent = new Intent(Intent.ACTION_SEND);
			faceIntent.setType("text/plain");
			faceIntent.putExtra(Intent.EXTRA_TEXT, temp);
			startActivity(Intent.createChooser(faceIntent, "Facebook"));

		} else if (vId == R.id.btnMail) {
			String temp2 = "http://novaeva.com/node/" + nid;
			Intent mailIntent = new Intent(Intent.ACTION_SEND);
			mailIntent.setType("message/rfc822"); //ovo ispipati još malo
			mailIntent.putExtra(Intent.EXTRA_SUBJECT, naslov);
			mailIntent.putExtra(Intent.EXTRA_TEXT, temp2);
			startActivity(Intent.createChooser(mailIntent, "Odaberite aplikaciju"));

		} else if (vId == R.id.btnTextPlus) {//showTextSizePopup();
			int mCurrentSize = getPrefs().getInt("hr.bpervan.novaeva.velicinateksta", 14);
			mCurrentSize += 2;
			if (mCurrentSize >= 28) {
				mCurrentSize = 12;
			}

			getPrefs().edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).apply();
			webView.getSettings().setDefaultFontSize(mCurrentSize);

		} else if (vId == R.id.btnBack) {
			IzrekeActivity.this.onBackPressed();

		}

	}

	private void showSearchPopup(){
		AlertDialog.Builder search = new AlertDialog.Builder(this);
		search.setTitle("Pretraga");

		final EditText et = new EditText(this);
		search.setView(et);

		search.setPositiveButton("Pretrazi", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String search=et.getText().toString();
				NovaEvaApp.Companion.goSearch(search, IzrekeActivity.this);
			}
		});

		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
}
