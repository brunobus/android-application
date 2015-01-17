package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.MolitvenikAdapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

/*import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;*/


public class MolitvenikActivity extends ListActivity implements OnClickListener{
	/*private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;*/
	private Typeface openSansRegular;
	
	private ListView mainListView;
	private MolitvenikAdapter molitvenikAdapter;
	private List<String> mainContentList;
	
	private ImageView btnBack, btnHome, btnSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_molitvenik);
		
		/*mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-40344870-1");*/
		
		openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");
		
		initUI();
				
		this.setTitle("Molitvenik");
		
		mainContentList = new ArrayList<String>();
		
		mainContentList.add("Često tražene molitve");
		mainContentList.add("0. Uvod");
		mainContentList.add("1. Obrasci vjere");
		mainContentList.add("2. Osnovne molitve");
		mainContentList.add("3. Svagdanje jutarnje molitve");
		mainContentList.add("4. Svagdanje večernje molitve");
		mainContentList.add("5. Prigodne molitve");
		mainContentList.add("6. Molitve mladih");
		mainContentList.add("7. Molitve u kušnji i napasti");
		mainContentList.add("8. Molitve za obitelj i roditelje");
		mainContentList.add("9. Molitve za bolesne i umiruće");
		mainContentList.add("10. Molitve po posebnim nakanama");
		mainContentList.add("11. Molitve svetih i velikih ljudi");
		mainContentList.add("12. Kratke molitve i zazivi");
		mainContentList.add("13. Molitve Duhu Svetome");
		mainContentList.add("14. Euharistijska pobožnost");
		mainContentList.add("15. Pomirenje");
		mainContentList.add("16. Pobožnost križnog puta");
		mainContentList.add("17. Deventica i krunica Božanskom milosrđu");
		mainContentList.add("18. Molitve Blaženoj Djevici Mariji");
		mainContentList.add("19. Salezijanske molitve");
		mainContentList.add("20. Molitve mladih");
		mainContentList.add("21. Molitve svetima");
		mainContentList.add("22. Lectio Divina");
		mainContentList.add("23. Moliti igrajući pred Gospodinom");
		
		molitvenikAdapter = new MolitvenikAdapter(this,mainContentList,this.openSansRegular);

		mainListView.setAdapter(molitvenikAdapter);
		
		mainListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				
				//mGaTracker.sendEvent("Molitvenik", "OtvorenaMolitva", String.valueOf(position), null);
				Intent intent=new Intent(MolitvenikActivity.this,MolitvenikDetaljiActivity.class);
				intent.putExtra("id",String.valueOf(position));
				intent.putExtra("naslov", mainContentList.get(position));
				startActivity(intent);			
			}				
		});
	}
	
	public void onStart(){
		super.onStart();
		//EasyTracker.getInstance().activityStart(this);
	}
	
	public void onStop(){
		super.onStop();
		//EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_molitvenik);
		
		initUI();
		
		mainListView.setAdapter(molitvenikAdapter);
		mainListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				
				//mGaTracker.sendEvent("Molitvenik", "OtvorenaMolitva", String.valueOf(position), null);
				Intent intent=new Intent(MolitvenikActivity.this,MolitvenikDetaljiActivity.class);
				intent.putExtra("id",String.valueOf(position));
				intent.putExtra("naslov", mainContentList.get(position));
				startActivity(intent);			
			}				
		});
	}
	
	private void initUI(){
		mainListView = getListView();
		mainListView.setClickable(true);
		
		btnBack = (ImageView) findViewById(R.id.btnBackListaVijesti);
		btnSearch = (ImageView) findViewById(R.id.btnSearchListaVijesti);
		btnHome = (ImageView) findViewById(R.id.btnHomeListaVijesti);
		
		btnBack.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btnHome.setOnClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_molitvenik, menu);
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnSearchListaVijesti:
			showSearchPopup();
			break;
		case R.id.btnHomeListaVijesti:
			startActivity(new Intent(MolitvenikActivity.this,DashboardActivity.class));
			break;
		case R.id.btnBackListaVijesti:
			MolitvenikActivity.this.onBackPressed();
			break;
		}
	}
	
	private void showSearchPopup(){
		AlertDialog.Builder search = new AlertDialog.Builder(this);
		search.setTitle("Pretraga");
		
		final EditText et = new EditText(this);
		search.setView(et);
				
		search.setPositiveButton("Pretraži", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String search = et.getText().toString();
				Intent i = new Intent(MolitvenikActivity.this,SearchActivity.class);	
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

}
