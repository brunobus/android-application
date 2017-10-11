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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.TimeUnit;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.fragments.MenuRecyclerFragment;
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

public class ListaVijestiActivity extends AppCompatActivity implements OnClickListener {

    private RelativeLayout fakeActionBar;

    private Tracker mGaTracker;

    private SharedPreferences prefs;

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
        if (savedInstanceState != null) {
            directoryId = savedInstanceState.getInt("kategorija", 11);
            colourSet = savedInstanceState.getInt("colourSet", (int) directoryId);
        } else {
            directoryId = getIntent().getIntExtra("kategorija", 11);
            colourSet = getIntent().getIntExtra("colourSet", (int) directoryId);
        }

        //mGaTracker.sendEvent("Kategorije", "OtvorenaKategorija", Constants.getCatNameById(kategorija), null);
        killRedDot(directoryId);

        initUI();

        if (savedInstanceState == null) {
            if (ConnectionChecker.hasConnection(this)) {
                showFragmentForDirectory(directoryId, Constants.getCatNameById(directoryId).toUpperCase(), false);
            } else {
                Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
            }
        } else {
            //fragment backstack already exists, don't create new fragment
        }
    }

    private void initUI() {
        fakeActionBar = findViewById(R.id.fakeActionBar);
        fakeActionBar.findViewById(R.id.btnHome).setOnClickListener(this);
        fakeActionBar.findViewById(R.id.btnSearch).setOnClickListener(this);
        fakeActionBar.findViewById(R.id.btnBack).setOnClickListener(this);

        if (directoryId == Constants.CAT_ODGOVORI) {
            btnImamPitanje = findViewById(R.id.btnImamPitanjeListaVijesti);
            btnImamPitanje.setOnClickListener(this);
            btnImamPitanje.setVisibility(View.VISIBLE);
        }

        this.setCategoryTypeColour();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt("kategorija", (int) directoryId); //// TODO: 11.10.17. long
        outState.putInt("colourSet", colourSet);

        super.onSaveInstanceState(outState);
    }

    private void showFragmentForDirectory(final Long dirId, final String dirName, final boolean isSubDir) {

        this.directoryId = dirId;

        MenuRecyclerFragment menuRecyclerFragment = new MenuRecyclerFragment();
        menuRecyclerFragment.setConfig(new MenuRecyclerFragment.FragmentConfig(dirId, dirName, isSubDir, colourSet, -1));

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.move_right_in, R.anim.move_left_out, R.anim.move_left_in, R.anim.move_right_out)
                .replace(R.id.eva_directory_fragment_frame, menuRecyclerFragment)
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

    //FIXME: 11.10.17.
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        setContentView(R.layout.activity_lista_vijesti);
//        //TODO: kad se promijeni orijentacija, lista ode na početak :)
//        initUI();
//    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();

        if (vId == R.id.btnSearch) {
            showSearchPopup();

        } else if (vId == R.id.btnHome) {
            NovaEvaApp.Companion.goHome(this);

        } else if (vId == R.id.btnBack) {
            onBackPressed();

        } else if (vId == R.id.btnImamPitanjeListaVijesti) {
            String text = "Hvaljen Isus i Marija, javljam Vam se jer imam pitanje.";
            String mail[] = new String[1];
            mail[0] = "odgovori.novaeva@gmail.com";
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, "Nova Eva pitanje");
            i.putExtra(Intent.EXTRA_TEXT, text);
            i.putExtra(Intent.EXTRA_EMAIL, mail);
            startActivity(Intent.createChooser(i, "Odaberite aplikaciju"));

        }
    }

    private void setCategoryTypeColour() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fakeActionBar.setBackgroundResource(ResourceHandler.getResourceId(colourSet, Configuration.ORIENTATION_LANDSCAPE));
        } else {
            fakeActionBar.setBackgroundResource(ResourceHandler.getResourceId(colourSet, Configuration.ORIENTATION_PORTRAIT));
        }
    }

    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    public void onRestart() {
        super.onRestart();
        if (!ConnectionChecker.hasConnection(this)) {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
        }
    }

    public void onResume() {
        super.onResume();
        if (!ConnectionChecker.hasConnection(this)) {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
            return;
        }

        disposables.add(NovaEvaApp.Companion.getBus().getDirectoryOpenRequest()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<DirectoryInfo>() {
                    @Override
                    public void accept(DirectoryInfo dirInfo) throws Exception {
                        showFragmentForDirectory(dirInfo.getDirectoryId(), dirInfo.getTitle(), true);
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
                        i = new Intent(ListaVijestiActivity.this, VijestActivity.class);
                        i.putExtra("contentId", contentInfo.getContentId());
                        i.putExtra("colourSet", colourSet);
                        i.putExtra("directoryId", directoryId);
                        startActivity(i);
                        overridePendingTransition(R.anim.move_right_in, R.anim.move_left_out);
                    }
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        disposables.clear(); //clears and disposes
    }

    // Napraviti Builder za search i errorokvir
    private void showSearchPopup() {
        AlertDialog.Builder search = new AlertDialog.Builder(this);
        search.setTitle("Pretraga");

        final EditText et = new EditText(this);
        search.setView(et);

        search.setPositiveButton("Pretrazi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String search = et.getText().toString();
                NovaEvaApp.Companion.goSearch(search, ListaVijestiActivity.this);
            }
        });
        search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        search.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void killRedDot(long kategorija) {
        prefs.edit().putInt("vidjenoKategorija" + kategorija, 1).apply();
    }
}