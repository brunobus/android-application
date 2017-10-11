package hr.bpervan.novaeva.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.ContentInfo;
import hr.bpervan.novaeva.model.DirectoryContent;
import hr.bpervan.novaeva.services.NovaEvaService;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

//import com.google.analytics.tracking.android.EasyTracker;

public class IzrekeActivity extends Activity implements OnClickListener{
	@SuppressWarnings("unused")
	private TextView tvNaslov, tvKategorija, tvText;
	
	/** Test test test */
	private WebView webView;

	private String naslov,tekst;
	private long nid = -1;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_izreke);
		
		prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);
		prefs.edit().putInt("vidjenoKategorija1", 1).apply();
		
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
					.getDirectoryContent(1, null, 1)
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
					});
		} else {
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void initUI(){
		tvNaslov = (TextView)findViewById(R.id.tvNaslov);
		tvKategorija = (TextView)findViewById(R.id.tvKategorija);
		
		webView = (WebView) findViewById(R.id.webText);
		webView.getSettings().setDefaultTextEncodingName("utf-8");

		tvKategorija.setText("Izreke");

		View fakeActionBar = findViewById(R.id.fakeActionBar);

		findViewById(R.id.btnTextPlus).setOnClickListener(this);
		findViewById(R.id.btnObnovi).setOnClickListener(this);

		fakeActionBar.findViewById(R.id.btnHome).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnFace).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnMail).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnSearch).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnBack).setOnClickListener(this);
		
		webView.getSettings().setDefaultFontSize(prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14));

        getWindow().getDecorView().setBackgroundColor(this.getResources().getColor(android.R.color.background_light));
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_izreke);
		
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
		} else if (vId == R.id.btnFace) {
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
			int mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14);
			mCurrentSize += 2;
			if (mCurrentSize >= 28) {
				mCurrentSize = 12;
			}

			prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).commit();
			webView.getSettings().setDefaultFontSize(mCurrentSize);

		} else if (vId == R.id.btnBack) {
			IzrekeActivity.this.onBackPressed();

		}
		
	}

	public boolean onOptionsItemSelected(MenuItem item){
		return true;
		
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_izreke, menu);
		return true;
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
				loadRandomIzreka();
			}
		});
		error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				NovaEvaApp.Companion.goHome(IzrekeActivity.this);
			}
			});
		error.show();
	}
}
