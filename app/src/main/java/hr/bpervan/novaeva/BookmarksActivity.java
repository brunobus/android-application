package hr.bpervan.novaeva;

import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.ListElement;
import hr.bpervan.novaeva.utilities.VijestAdapter;
import hr.bpervan.novaeva.v2.BookmarksDBHandlerV2;
import hr.bpervan.novaeva.v2.VijestActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

//import com.google.analytics.tracking.android.EasyTracker;

public class BookmarksActivity extends ListActivity implements View.OnClickListener{
	
	ListView listView;
	BookmarksDBHandlerV2 db = new BookmarksDBHandlerV2(this);
	VijestAdapter adapter;
	List<ListElement> listaBookmarksa;
    private static Typeface openSansBold, openSansItalic, openSansLight, openSansRegular;
    
    private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;

    
    private ImageView btnHome, btnSearch, btnBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmarks);
		
		openSansBold = Typeface.createFromAsset(getAssets(), "opensans-bold.ttf");
		openSansItalic = Typeface.createFromAsset(getAssets(), "opensans-italic.ttf");
		openSansLight = Typeface.createFromAsset(getAssets(), "opensans-light.ttf");
		openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");
		listaBookmarksa = new ArrayList<ListElement>();
		
		mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-40344870-1");
		mGaTracker.sendEvent("Zabiljeske", "OtvoreneZabiljeske", "", null);
		//initUI();
		//pokupiVijestiIzBaze();
	}
	
	private void initUI(){
		this.setTitle("Bookmarks");
		listView = getListView();
		
		btnHome = (ImageView) findViewById(R.id.btnHomeListaVijesti);
		btnSearch = (ImageView) findViewById(R.id.btnSearchListaVijesti);
		btnBack = (ImageView) findViewById(R.id.btnBackListaVijesti);
		btnHome.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		
		/*
		for(ListElement l : listaBookmarksa){
			l.setUvod(makeUvod())
		}*/
		
		adapter = new VijestAdapter(this, listaBookmarksa, openSansBold, openSansItalic, openSansLight, openSansRegular, Constants.CAT_PROPOVJEDI);
		


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
		EasyTracker.getInstance().activityStart(this);
	}
	
	public void onStop(){
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_bookmarks);
		//TODO: kad se promjeni orjentacija, lista ode na po�etak :)
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
			startActivity(new Intent(BookmarksActivity.this,DashboardActivity.class));
			break;
		case R.id.btnBackListaVijesti:
			BookmarksActivity.this.onBackPressed();
			break;
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
				Intent i = new Intent(BookmarksActivity.this,SearchActivity.class);	
				i.putExtra("string", search);
				startActivity(i);
			}
		});
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
}
