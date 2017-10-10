package hr.bpervan.novaeva.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.TimeUnit;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.fragments.EvaMenuRecyclerFragment;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.ContentInfo;
import hr.bpervan.novaeva.model.DirectoryInfo;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.ResourceHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ListaVijestiActivity extends AppCompatActivity implements OnClickListener{

	private LinearLayout fakeActionBarListaVijesti;

	private Tracker mGaTracker;

	private SharedPreferences prefs;

	private ImageView btnHome, btnSearch, btnBack;
	private ImageView btnImamPitanje;

	private long directoryId;

	private int colourSet = 0;

	private CompositeDisposable disposables = new CompositeDisposable();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_vijesti);

		prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);

		//mGaTracker = mGaInstance.getTracker("UA-40344870-1");
        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Kategorije")
                        .setAction("OtvorenaKategorija")
                        .setLabel(Constants.getCatNameById(directoryId))
                        .build()
        );
		directoryId = getIntent().getIntExtra("kategorija", 11);

		colourSet = (int) directoryId;

		//mGaTracker.sendEvent("Kategorije", "OtvorenaKategorija", Constants.getCatNameById(kategorija), null);
		killRedDot(directoryId);

		initUI();
		if(!ConnectionChecker.hasConnection(this)){
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(ListaVijestiActivity.this,DashboardActivity.class));
		}else{
			showFragmentForDirectory(directoryId, Constants.getCatNameById(directoryId).toUpperCase(), false);
		}
	}

	private void initUI(){
		btnHome = findViewById(R.id.btnHomeListaVijesti);
		btnSearch = findViewById(R.id.btnSearchListaVijesti);
		btnBack = findViewById(R.id.btnBackListaVijesti);
		btnHome.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btnBack.setOnClickListener(this);

		if(directoryId == Constants.CAT_ODGOVORI){
			btnImamPitanje = findViewById(R.id.btnImamPitanjeListaVijesti);
			btnImamPitanje.setOnClickListener(this);
			btnImamPitanje.setVisibility(View.VISIBLE);
		}


		fakeActionBarListaVijesti = findViewById(R.id.fakeActionBarListaVijesti);

		this.setCategoryTypeColour();
    }

	private void showFragmentForDirectory(final Long dirId, final String dirName, final boolean isSubDir) {

		this.directoryId = dirId;

		Bundle bundle = new Bundle();
		bundle.putLong("directoryId", dirId);
		bundle.putString("directoryName", dirName);
		bundle.putBoolean("isSubDirectory", isSubDir);
		bundle.putInt("colourSet", colourSet);

		getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.move_right_in, R.anim.move_left_out, R.anim.move_left_in, R.anim.move_right_out)
				.replace(R.id.eva_directory_fragment_frame, EvaMenuRecyclerFragment.Companion.newInstance(bundle))
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			getSupportFragmentManager().popBackStack();
		} else {
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_lista_vijesti);
		//TODO: kad se promjeni orjentacija, lista ode na početak :)
		initUI();
		//mainListView.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnSearchListaVijesti:
			showSearchPopup();
			break;
		case R.id.btnHomeListaVijesti:
			startActivity(new Intent(ListaVijestiActivity.this,DashboardActivity.class));
			break;
		case R.id.btnBackListaVijesti:
			ListaVijestiActivity.this.onBackPressed();
			break;
		case R.id.btnImamPitanjeListaVijesti:
			String text = "Hvaljen Isus i Marija, javljam Vam se jer imam pitanje.";
			String mail[] = new String[1];
			mail[0] = "odgovori.novaeva@gmail.com";
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_SUBJECT, "Nova Eva pitanje");
			i.putExtra(Intent.EXTRA_TEXT, text);
			i.putExtra(Intent.EXTRA_EMAIL, mail);
			startActivity(Intent.createChooser(i, "Odaberite aplikaciju"));
			break;
		}
	}

	private void setCategoryTypeColour(){
		if(this.getResources().getConfiguration().orientation  == Configuration.ORIENTATION_LANDSCAPE){
			fakeActionBarListaVijesti.setBackgroundResource(ResourceHandler.getResourceId(colourSet, Configuration.ORIENTATION_LANDSCAPE));
		} else {
			fakeActionBarListaVijesti.setBackgroundResource(ResourceHandler.getResourceId(colourSet, Configuration.ORIENTATION_PORTRAIT));
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

	public void onRestart(){
		super.onRestart();
		if(!ConnectionChecker.hasConnection(this)){
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(ListaVijestiActivity.this,DashboardActivity.class));
		}
	}

	public void onResume(){
		super.onResume();
		if(!ConnectionChecker.hasConnection(this)){
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(ListaVijestiActivity.this,DashboardActivity.class));
			return;
		}

		disposables.add(NovaEvaApp.Companion.getBus().getDirectoryOpenRequest()
				.throttleFirst(500, TimeUnit.MILLISECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<DirectoryInfo>() {
					@Override
					public void accept(DirectoryInfo dirInfo) throws Exception {
						showFragmentForDirectory(dirInfo.getDirId(), dirInfo.getTitle(), true);
					}
				}));

		disposables.add(NovaEvaApp.Companion.getBus().getContentOpenRequest()
		.throttleFirst(500, TimeUnit.MILLISECONDS)
		.subscribeOn(Schedulers.io())
		.observeOn(AndroidSchedulers.mainThread())
		.subscribe(new Consumer<ContentInfo>() {
			@Override
			public void accept(ContentInfo contentInfo) throws Exception {
				Intent i;
				i = new Intent(ListaVijestiActivity.this,VijestActivity.class);
				i.putExtra("contentId", contentInfo.getContentId());
				i.putExtra("colourSet", colourSet);
				i.putExtra("directoryId", directoryId);
				startActivity(i);
			}
		}));
	}

	@Override
	protected void onPause() {
		super.onPause();

		disposables.clear(); //clears and disposes
	}

	// Napraviti Builder za search i errorokvir
	private void showSearchPopup(){
		AlertDialog.Builder search = new AlertDialog.Builder(this);
		search.setTitle("Pretraga");

		final EditText et = new EditText(this);
		search.setView(et);

		search.setPositiveButton("Pretrazi", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String search = et.getText().toString();
				Intent i = new Intent(ListaVijestiActivity.this,SearchActivity.class);
				i.putExtra("searchString", search);
				startActivity(i);
			}
		});
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {return true;}

	private void killRedDot(long kategorija){
		prefs.edit().putInt("vidjenoKategorija" + kategorija, 1).apply();
	}
}