package hr.bpervan.novaeva.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.DirectoryContent;
import hr.bpervan.novaeva.model.Image;
import hr.bpervan.novaeva.model.Indicators;
import hr.bpervan.novaeva.services.NovaEvaService;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.EvaCategory;
import hr.bpervan.novaeva.utilities.LocalCategory;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

//// TODO: 13.10.17. refactor me
public class DashboardActivity extends EvaBaseActivity implements OnTouchListener, OnClickListener{
	private final long syncInterval = 90000L;

	private Tracker mGaTracker;

	private ImageView btnBrevijar, btnMolitvenik, btnIzreke, btnMp3, btnAktualno,
	btnPoziv, btnOdgovori, btnMultimedia, btnPropovjedi,
	btnDuhovnost, btnEvandjelje;

	private ImageView btnInfo, btnBookmarks;
	private TextView titleLineTitle;
	private Typeface openSansRegular;

	//List<Integer> listaKategorija = Constants.getIntCategoryList();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);

		fetchBreviaryImage();

		/*mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-40344870-1");*/

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.setScreenName("Dashboard");
        mGaTracker.send(new HitBuilders.AppViewBuilder().build());

		btnBrevijar = (ImageView) findViewById(R.id.btnBrevijar);
		btnMolitvenik = (ImageView) findViewById(R.id.btnMolitvenik);
		btnIzreke = (ImageView) findViewById(R.id.btnIzreke);
		btnMp3 = (ImageView) findViewById(R.id.btnMp3);
		btnAktualno = (ImageView) findViewById(R.id.btnAktualno);
		btnPoziv = (ImageView) findViewById(R.id.btnPoziv);
		btnOdgovori = (ImageView) findViewById(R.id.btnOdgovori);
		btnMultimedia = (ImageView) findViewById(R.id.btnMultimedia);
		btnPropovjedi = (ImageView) findViewById(R.id.btnPropovjedi);
		btnDuhovnost = (ImageView) findViewById(R.id.btnDuhovnost);
		btnEvandjelje = (ImageView) findViewById(R.id.btnEvandjelje);
		btnInfo = (ImageView) findViewById(R.id.btnInfo);
		btnBookmarks = (ImageView) findViewById(R.id.btnBookmarks);

		openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");
		titleLineTitle = (TextView) findViewById(R.id.titleLineTitle);
		titleLineTitle.setTypeface(openSansRegular);

		btnBrevijar.setOnTouchListener(this);
		btnMolitvenik.setOnTouchListener(this);
		btnIzreke.setOnTouchListener(this);
		btnMp3.setOnTouchListener(this);
		btnAktualno.setOnTouchListener(this);
		btnPoziv.setOnTouchListener(this);
		btnOdgovori.setOnTouchListener(this);
		btnMultimedia.setOnTouchListener(this);
		btnPropovjedi.setOnTouchListener(this);
		btnDuhovnost.setOnTouchListener(this);
		btnEvandjelje.setOnTouchListener(this);
		btnInfo.setOnTouchListener(this);
		btnBookmarks.setOnTouchListener(this);

		btnBrevijar.setOnClickListener(this);
		btnMolitvenik.setOnClickListener(this);
		btnIzreke.setOnClickListener(this);
		btnMp3.setOnClickListener(this);
		btnAktualno.setOnClickListener(this);
		btnPoziv.setOnClickListener(this);
		btnOdgovori.setOnClickListener(this);
		btnMultimedia.setOnClickListener(this);
		btnPropovjedi.setOnClickListener(this);
		btnDuhovnost.setOnClickListener(this);
		btnEvandjelje.setOnClickListener(this);
		btnInfo.setOnClickListener(this);
		btnBookmarks.setOnClickListener(this);
	}

	public void onResume(){
		super.onResume();

		long vrijemeZadnjeSync = getPrefs().getLong("vrijemeZadnjeSinkronizacije", 0L);
		if((System.currentTimeMillis() - vrijemeZadnjeSync) > syncInterval){
			if(ConnectionChecker.hasConnection(this)){
				NovaEvaService.Companion.getInstance().getNewStuff()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(new Consumer<Indicators>() {
							@Override
							public void accept(Indicators indicators) throws Exception {

                                checkZadnjiNid(EvaCategory.DUHOVNOST, indicators.getDuhovnost());
                                checkZadnjiNid(EvaCategory.AKTUALNO, indicators.getAktualno());
                                checkZadnjiNid(EvaCategory.IZREKE, indicators.getIzreke());
                                checkZadnjiNid(EvaCategory.MULTIMEDIJA, indicators.getMultimedija());
                                checkZadnjiNid(EvaCategory.EVANDJELJE, indicators.getEvandjelje());
                                checkZadnjiNid(EvaCategory.PROPOVIJEDI, indicators.getPropovijedi());
                                checkZadnjiNid(EvaCategory.POZIV, indicators.getPoziv());
                                checkZadnjiNid(EvaCategory.ODGOVORI, indicators.getOdgovori());
                                checkZadnjiNid(EvaCategory.PJESMARICA, indicators.getPjesmarica());

                                testAndSetRedDots();
                                getPrefs().edit().putLong("vrijemeZadnjeSinkronizacije", System.currentTimeMillis()).apply();
							}
						}, new Consumer<Throwable>() {
							@Override
							public void accept(Throwable throwable) throws Exception {
								//error
							}
						});
			}
		}

		testAndSetRedDots();
	}

	private void checkZadnjiNid(EvaCategory kategorija, @Nullable Integer dobiveniZadnjiNid){
        if(dobiveniZadnjiNid==null) return;

        String kategorijaStr = String.valueOf(kategorija.getId());
        int spremljeniZadnjiNid = getPrefs().getInt(kategorijaStr, 0);
        if (spremljeniZadnjiNid != dobiveniZadnjiNid) {
            getPrefs().edit().putInt(kategorijaStr, dobiveniZadnjiNid).apply();
            getPrefs().edit().putInt("vidjenoKategorija" + kategorijaStr, 0).apply();
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
	protected void onDestroy() {
		super.onDestroy();

		if (fetchBrevijarImageDisposable != null) {
			fetchBrevijarImageDisposable.dispose();
		}
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.menuSearch:
			if(ConnectionChecker.hasConnection(this))
				showSearchPopup();
			else
				Toast.makeText(DashboardActivity.this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			return true;


		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private Disposable fetchBrevijarImageDisposable;

	private void fetchBreviaryImage(){
		if (fetchBrevijarImageDisposable != null) {
			fetchBrevijarImageDisposable.dispose();
		}
		fetchBrevijarImageDisposable = NovaEvaService.Companion.getInstance()
				.getDirectoryContent(546, null)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<DirectoryContent>() {
					@Override
					public void accept(DirectoryContent directoryContent) throws Exception {
						Image breviaryImage = directoryContent.getImage();
						if (breviaryImage != null) {
							getPrefs().edit().putString("hr.bpervan.novaeva.brevijarheaderimage", breviaryImage.getSize640()).apply();
						}
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
						Toast.makeText(DashboardActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
					}
				});
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
				NovaEvaApp.Companion.goSearch(search, DashboardActivity.this);
			}
		});

		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
	//TODO: sredi ovo pod hitno
	private void testAndSetRedDots(){
		if(getPrefs().getInt("vidjenoKategorija1", 0) == 0){
			btnIzreke.setBackgroundResource(R.drawable.button_izreke_news);
		} else {
			btnIzreke.setBackgroundResource(R.drawable.button_izreke);
		}
		if(getPrefs().getInt("vidjenoKategorija4", 0) == 0){
			btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje_news);
		} else {
			btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje);
		}
		if(getPrefs().getInt("vidjenoKategorija7", 0) == 0){
			btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi_news);
		} else {
			btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi);
		}
		if(getPrefs().getInt("vidjenoKategorija10", 0) == 0){
			btnMultimedia.setBackgroundResource(R.drawable.button_multimedia_news);
		} else {
			btnMultimedia.setBackgroundResource(R.drawable.button_multimedia);
		}
		if(getPrefs().getInt("vidjenoKategorija11", 0) == 0){
			btnOdgovori.setBackgroundResource(R.drawable.button_odgovori_news);
		} else {
			btnOdgovori.setBackgroundResource(R.drawable.button_odgovori);
		}
		if(getPrefs().getInt("vidjenoKategorija9", 0) == 0){
			btnAktualno.setBackgroundResource(R.drawable.button_aktualno_news);
		} else {
			btnAktualno.setBackgroundResource(R.drawable.button_aktualno);
		}
		if(getPrefs().getInt("vidjenoKategorija355", 0) == 0){
			btnMp3.setBackgroundResource(R.drawable.button_mp3_news);
		} else {
			btnMp3.setBackgroundResource(R.drawable.button_mp3);
		}
		if(getPrefs().getInt("vidjenoKategorija8", 0) == 0){
			btnPoziv.setBackgroundResource(R.drawable.button_poziv_news);
		} else {
			btnPoziv.setBackgroundResource(R.drawable.button_poziv);
		}
		if(getPrefs().getInt("vidjenoKategorija354", 0) == 0){
			btnDuhovnost.setBackgroundResource(R.drawable.button_duhovnost_news);
		} else {
			btnDuhovnost.setBackgroundResource(R.drawable.button_duhovnost);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_dashboard, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		Intent i = null;

		if(v.getId() == R.id.btnMolitvenik){
			startActivity(new Intent(DashboardActivity.this,MolitvenikActivity.class));
		} else if(v.getId() == R.id.btnInfo){
			startActivity(new Intent(DashboardActivity.this,InfoActivity.class));
		} else if(v.getId() == R.id.btnBookmarks){
			startActivity(new Intent(DashboardActivity.this,BookmarksActivity.class));
		}

		if(ConnectionChecker.hasConnection(this)){
			switch(v.getId()){
			case R.id.btnBrevijar:
				i = new Intent(DashboardActivity.this, BreviaryActivity.class);
				startActivity(i);
				break;
			case R.id.btnEvandjelje:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.EVANDJELJE.getId());
				i.putExtra("categoryName", EvaCategory.EVANDJELJE.getRawName());
				startActivity(i);
				break;
			case R.id.btnMp3:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.PJESMARICA.getId());
				i.putExtra("categoryName", EvaCategory.PJESMARICA.getRawName());
				startActivity(i);
				break;
			case R.id.btnPropovjedi:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.PROPOVIJEDI.getId());
				i.putExtra("categoryName", EvaCategory.PROPOVIJEDI.getRawName());
				startActivity(i);
				break;
			case R.id.btnOdgovori:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.ODGOVORI.getId());
				i.putExtra("categoryName", EvaCategory.ODGOVORI.getRawName());
				startActivity(i);
				break;
			case R.id.btnPoziv:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.POZIV.getId());
				i.putExtra("categoryName", EvaCategory.POZIV.getRawName());
				startActivity(i);
				break;
			case R.id.btnDuhovnost:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.DUHOVNOST.getId());
				i.putExtra("categoryName", EvaCategory.DUHOVNOST.getRawName());
				startActivity(i);
				break;
			case R.id.btnIzreke:
				i = new Intent(DashboardActivity.this,IzrekeActivity.class);
				startActivity(i);
				break;
			case R.id.btnMultimedia:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.MULTIMEDIJA.getId());
				i.putExtra("categoryName", EvaCategory.MULTIMEDIJA.getRawName());
				startActivity(i);
				break;
			case R.id.btnAktualno:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("categoryId", EvaCategory.AKTUALNO.getId());
				i.putExtra("categoryName", EvaCategory.AKTUALNO.getRawName());
				startActivity(i);
				break;
			}
		} else {
			Toast.makeText(DashboardActivity.this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
		}
	}
	//na drugim mobovima treba return true, zato nestane natpis odmah!!!
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		String text = null;
		if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			text = getString(R.string.app_name);
		} else {
			text = getString(R.string.app_name_vertical);
		}
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			switch(v.getId()){
			case R.id.btnBrevijar:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = LocalCategory.BREVIJAR.getRawName();
				else{
					text = LocalCategory.BREVIJAR.getRawNameVertical();
				}
				break;
			case R.id.btnMolitvenik:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = LocalCategory.MOLITVENIK.getRawName();
				else{
					text = LocalCategory.MOLITVENIK.getRawNameVertical();
				}
				break;
			case R.id.btnEvandjelje:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.EVANDJELJE.getRawName();
				else{
					text = EvaCategory.EVANDJELJE.getRawNameVertical();
				}
				break;
			case R.id.btnMp3:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.PJESMARICA.getRawName();
				else{
					text = EvaCategory.PJESMARICA.getRawNameVertical();
				}
				break;
			case R.id.btnPropovjedi:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.PROPOVIJEDI.getRawName();
				else{
					text = EvaCategory.PROPOVIJEDI.getRawNameVertical();
				}
				break;
			case R.id.btnOdgovori:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.ODGOVORI.getRawName();
				else{
					text = EvaCategory.ODGOVORI.getRawNameVertical();
				}
				break;
			case R.id.btnPoziv:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.POZIV.getRawName();
				else{
					text = EvaCategory.POZIV.getRawNameVertical();
				}
				break;
			case R.id.btnDuhovnost:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.DUHOVNOST.getRawName();
				else{
					text = EvaCategory.DUHOVNOST.getRawNameVertical();
				}
				break;
			case R.id.btnIzreke:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.IZREKE.getRawName();
				else{
					text = EvaCategory.IZREKE.getRawNameVertical();
				}
				break;
			case R.id.btnMultimedia:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.MULTIMEDIJA.getRawName();
				else{
					text = EvaCategory.MULTIMEDIJA.getRawNameVertical();
				}
				break;
			case R.id.btnAktualno:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = EvaCategory.AKTUALNO.getRawName();
				else{
					text = EvaCategory.AKTUALNO.getRawNameVertical();
				}
				break;
			case R.id.btnInfo:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = LocalCategory.INFO.getRawName();
				else{
					text = LocalCategory.INFO.getRawNameVertical();
				}
				break;
			case R.id.btnBookmarks:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = LocalCategory.BOOKMARKS.getRawName();
				else{
					text = LocalCategory.BOOKMARKS.getRawNameVertical();
				}
				break;
			}
			titleLineTitle.setText(text);
			//return true;
		case MotionEvent.ACTION_UP:
			titleLineTitle.setText(text);
			//return true;
		}

		return false;
		}
}