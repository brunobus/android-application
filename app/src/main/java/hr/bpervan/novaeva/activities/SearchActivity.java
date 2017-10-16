package hr.bpervan.novaeva.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.adapters.MenuElementAdapter;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.ContentInfo;
import hr.bpervan.novaeva.model.SearchResult;
import hr.bpervan.novaeva.services.NovaEvaService;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.LoadableFromBundle;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function0;

public class SearchActivity extends Activity implements OnClickListener, LoadableFromBundle {

    private final CompositeDisposable disposables = new CompositeDisposable();
    private Disposable searchForContentDisposable;

    private List<ContentInfo> searchResultList = new ArrayList<>();

    private MenuElementAdapter adapter;

    private Tracker mGaTracker;

    private String searchString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            loadStateFromBundle(savedInstanceState);
        } else {
            loadStateFromBundle(getIntent().getExtras());
        }

        this.setTitle("Pretraga: " + searchString);

		/*mGaTracker = mGaInstance.getTracker("UA-40344870-1");

		mGaTracker.sendEvent("Pretraga", "KljucneRijeci", searchString, null);*/

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Pretraga")
                        .setAction("KljucneRijeci")
                        .setLabel(searchString)
                        .build()
        );

        initUI();

        if (ConnectionChecker.hasConnection(this)) {
            searchForContent(searchString);
        }
    }

    @Override
    public void loadStateFromBundle(@NonNull Bundle bundle) {
        searchString = bundle.getString("searchString");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("searchString", searchString);
//        outState.putParcelableArrayList("searchResultList", searchResultList); //// TODO: 13.10.17.

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        disposables.add(NovaEvaApp.Companion.getBus().getContentOpenRequest()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ContentInfo>() {
                    @Override
                    public void accept(ContentInfo contentInfo) throws Exception {
                        Intent i;
                        i = new Intent(SearchActivity.this, VijestActivity.class);
                        i.putExtra("contentId", contentInfo.getContentId());
                        startActivity(i);
                    }
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        disposables.clear();
    }

    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (searchForContentDisposable != null) {
            searchForContentDisposable.dispose();
        }
    }

    private void initUI() {
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.eva_recyclerview);

        View fakeActionBar = findViewById(R.id.fakeActionBar);

        fakeActionBar.findViewById(R.id.btnHome).setOnClickListener(this);
        fakeActionBar.findViewById(R.id.btnSearch).setOnClickListener(this);
        fakeActionBar.findViewById(R.id.btnBack).setOnClickListener(this);

        MenuElementAdapter.ConfigData configData = new MenuElementAdapter.ConfigData(
                Constants.CAT_PROPOVJEDI,
                new Function0<Boolean>() {
                    @Override
                    public Boolean invoke() {
                        return false;
                    }
                });

        adapter = new MenuElementAdapter(searchResultList, configData, null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void showSearchPopup() {
        AlertDialog.Builder search = new AlertDialog.Builder(this);
        search.setTitle("Pretraga");

        final EditText et = new EditText(this);
        search.setView(et);

        search.setPositiveButton("Pretrazi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String search = et.getText().toString();
                searchForContent(search);
            }
        });
        search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        search.show();
    }

    private void searchForContent(String searchString) {
        this.searchString = searchString;

        searchResultList.clear();
        adapter.notifyDataSetChanged();

        if (searchForContentDisposable != null) {
            searchForContentDisposable.dispose();
        }
        searchForContentDisposable = NovaEvaService.Companion.getInstance()
                .searchForContent(searchString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<SearchResult>() {
                    @Override
                    public void accept(SearchResult searchResult) throws Exception {
                        List<ContentInfo> searchResultContentInfoList = searchResult.getSearchResultContentInfoList();
                        if (searchResultContentInfoList != null && !searchResultContentInfoList.isEmpty()) {
                            searchResultList.addAll(searchResultContentInfoList);
                            adapter.notifyDataSetChanged();
                        } else {
                            SearchActivity.this.showEmptyListInfo();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable t) throws Exception {
                        Log.e("searchForContent", t.getMessage(), t);
                        showErrorPopup();
                    }
                });
    }

    private void showErrorPopup() {
        AlertDialog.Builder error = new AlertDialog.Builder(this);
        error.setTitle("Greška");

        final TextView tv = new TextView(this);
        tv.setText("Greška pri dohvaćanju podataka sa poslužitelja");
        if (NovaEvaApp.Companion.getOpenSansRegular() != null) {
            tv.setTypeface(NovaEvaApp.Companion.getOpenSansRegular());
        }
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        error.setView(tv);

        error.setPositiveButton("Pokušaj ponovno", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                searchForContent(searchString);
            }
        });
        error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                NovaEvaApp.Companion.goHome(SearchActivity.this);
            }
        });
        error.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        initUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                showSearchPopup();
                break;
            case R.id.btnHome:
                NovaEvaApp.Companion.goHome(this);
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }

    private void showEmptyListInfo() {
        AlertDialog.Builder emptyInfo = new AlertDialog.Builder(this);
        emptyInfo.setTitle("Pretraga");
        emptyInfo.setMessage("Pretraga nije vratila rezultate");

        emptyInfo.setPositiveButton("U redu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SearchActivity.this.onBackPressed();
            }
        });

        emptyInfo.show();
    }
}
