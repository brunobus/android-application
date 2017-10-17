package hr.bpervan.novaeva.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.adapters.VijestAdapter;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.BookmarksDBHandlerV2;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.EvaCategory;
import hr.bpervan.novaeva.utilities.ListElement;

public class BookmarksActivity extends ListActivity implements View.OnClickListener{
	
	ListView listView;
	BookmarksDBHandlerV2 db = new BookmarksDBHandlerV2(this);
	VijestAdapter adapter;
	List<ListElement> listaBookmarksa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		listaBookmarksa = new ArrayList<>();

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
	}
	
	private void initUI(){
		setContentView(R.layout.activity_bookmarks);

		this.setTitle("Bookmarks");
		listView = getListView();

		View fakeActionBar = findViewById(R.id.fakeActionBar);
		fakeActionBar.findViewById(R.id.btnHome).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnSearch).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnBack).setOnClickListener(this);
		
		/*
		for(ListElement l : listaBookmarksa){
			l.setUvod(makeUvod())
		}*/
		
		adapter = new VijestAdapter(this, listaBookmarksa, EvaCategory.PROPOVIJEDI.getId());


		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {				
				Intent i = new Intent(BookmarksActivity.this,VijestActivity.class);
				i.putExtra("nid", listaBookmarksa.get(arg2).getNid());
				i.putExtra("kategorija", listaBookmarksa.get(arg2).getKategorija());
				
				if(ConnectionChecker.hasConnection(BookmarksActivity.this))
					startActivity(i);
			}
			
		});

		listView.setAdapter(adapter);
	}
	
	
	private void pokupiVijestiIzBaze(){
		List<ListElement> bookmarkList = db.getAllVijest();
		
		if(bookmarkList.isEmpty()){
			showEmptyListInfo();
		}
		
		Collections.reverse(bookmarkList);
		listaBookmarksa.addAll(bookmarkList);
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
		initUI();
		listaBookmarksa.clear();
		pokupiVijestiIzBaze();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
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
