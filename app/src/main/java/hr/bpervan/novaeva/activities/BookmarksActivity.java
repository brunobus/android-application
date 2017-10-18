package hr.bpervan.novaeva.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.adapters.MenuElementAdapter;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.ContentInfo;
import hr.bpervan.novaeva.utilities.BookmarksDBHandlerV2;
import hr.bpervan.novaeva.utilities.EvaCategory;
import hr.bpervan.novaeva.utilities.ListElement;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function0;

public class BookmarksActivity extends EvaBaseActivity implements View.OnClickListener{

	private final CompositeDisposable lifecycleBoundDisposables = new CompositeDisposable();

	BookmarksDBHandlerV2 db = new BookmarksDBHandlerV2(this);
	MenuElementAdapter adapter;
	List<ContentInfo> bookmarksList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Tracker mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Zabiljeske")
                        .setAction("OtvoreneZabiljeske")
                        .setLabel("")
                        .setValue(0L)
                        .build()
        );
		//mGaTracker.sendEvent("Zabiljeske", "OtvoreneZabiljeske", "", null);
		//initUI();
		//pokupiVijestiIzBaze();

        initUI();
	}

	private void initUI(){
		setContentView(R.layout.activity_bookmarks);

		this.setTitle("Bookmarks");

		RecyclerView recyclerView = findViewById(R.id.eva_recyclerview);

		View fakeActionBar = findViewById(R.id.fakeActionBar);
		fakeActionBar.findViewById(R.id.btnHome).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnSearch).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnBack).setOnClickListener(this);

		/*
		for(ListElement l : listaBookmarksa){
			l.setUvod(makeUvod())
		}*/

		MenuElementAdapter.ConfigData configData = new MenuElementAdapter.ConfigData(
				EvaCategory.PROPOVIJEDI.getId(),
				new Function0<Boolean>() {
			@Override
			public Boolean invoke() {
				return false;
			}
		});

		adapter = new MenuElementAdapter(bookmarksList, configData, null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
	}

	//TODO DONT USE ListElement ANYMORE
	private void pokupiVijestiIzBaze(){
		List<ListElement> bookmarkList_legacy = db.getAllVijest();

		if(bookmarkList_legacy.isEmpty()){
			showEmptyListInfo();
		}

		Collections.reverse(bookmarkList_legacy);

        for (ListElement listElement : bookmarkList_legacy) {
            bookmarksList.add(new ContentInfo(null, listElement.getNid(), listElement.getUnixDatum(), listElement.getNaslov(), listElement.getUvod()));
        }
		adapter.notifyDataSetChanged();
	}

	private void showEmptyListInfo(){
		AlertDialog.Builder emptyInfo = new AlertDialog.Builder(this);
		emptyInfo.setTitle("Zabilješke");
		emptyInfo.setMessage("Trenutno nemate zabilješki");

		emptyInfo.setPositiveButton("U redu", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {BookmarksActivity.this.onBackPressed();}
		});

		emptyInfo.show();
	}

	protected void onResume(){
		super.onResume();
		bookmarksList.clear();
		pokupiVijestiIzBaze();

		lifecycleBoundDisposables.add(NovaEvaApp.Companion.getBus().getContentOpenRequest()
				.throttleFirst(500, TimeUnit.MILLISECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<ContentInfo>() {
					@Override
					public void accept(ContentInfo contentInfo) throws Exception {
						Intent i;
						i = new Intent(BookmarksActivity.this, VijestActivity.class);
						i.putExtra("contentId", contentInfo.getContentId());
						startActivity(i);
					}
				}));
	}

	@Override
	protected void onPause() {
		super.onPause();

		lifecycleBoundDisposables.clear();
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
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);

		initUI();
	}

	@Override
	public void onClick(View v) {
		int vId = v.getId();
		if (vId == R.id.btnSearch) {
			showSearchPopup();

		} else if (vId == R.id.btnHome) {
			NovaEvaApp.Companion.goHome(this);

		} else if (vId == R.id.btnBack) {
			onBackPressed();

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
				String search = et.getText().toString();
				NovaEvaApp.Companion.goSearch(search, BookmarksActivity.this);
			}
		});
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
}
