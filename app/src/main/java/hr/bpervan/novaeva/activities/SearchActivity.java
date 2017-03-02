package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.ListElement;
import hr.bpervan.novaeva.utilities.ListTypes;
import hr.bpervan.novaeva.utilities.VijestAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity implements OnClickListener{
	
	private ListView mainListView;
	private List<ListElement> listaVijesti;
	
	private VijestAdapter adapter;
		
	private ImageView btnHome, btnSearch, btnBack;
	
	private static Typeface openSansBold, openSansItalic, openSansLight, openSansRegular;
	
	private Tracker mGaTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		String searchString = getIntent().getStringExtra("searchString");
		
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
				
		openSansBold = Typeface.createFromAsset(getAssets(), "opensans-bold.ttf");
		openSansItalic = Typeface.createFromAsset(getAssets(), "opensans-italic.ttf");
		openSansLight = Typeface.createFromAsset(getAssets(), "opensans-light.ttf");
		openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");
		
		listaVijesti = new ArrayList<ListElement>();
		
		initUI();
		if(ConnectionChecker.hasConnection(this)){
			new AsyncHttpPostTask(this).execute(searchString);
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
	
	private void initUI(){
		mainListView = (ListView) findViewById(R.id.listViewSearch);
		
		btnHome = (ImageView) findViewById(R.id.btnHomeListaVijesti);
		btnSearch = (ImageView) findViewById(R.id.btnSearchListaVijesti);
		btnBack = (ImageView) findViewById(R.id.btnBackListaVijesti);
		btnHome.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		
		mainListView.setOnItemClickListener(new ListClickHandler());
		
		adapter = new VijestAdapter(this, listaVijesti, openSansBold, openSansItalic, openSansLight, openSansRegular, Constants.CAT_PROPOVJEDI);
		mainListView.setAdapter(adapter);
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
				Intent i = new Intent(SearchActivity.this,SearchActivity.class);	
				i.putExtra("string", search);
				startActivity(i);
			}
		});
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
	
	private void showErrorPopup(){
		AlertDialog.Builder error = new AlertDialog.Builder(this);
		error.setTitle("Greška");
		
		final TextView tv = new TextView(this);
		tv.setText("Greška pri dohvaćanju podataka sa poslužitelja");
		tv.setTypeface(openSansRegular);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		error.setView(tv);
				
		error.setPositiveButton("Pokušaj ponovno", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//new AsyncHttpPostTask(SearchActivity.this).execute(kategorija + "");
			}
		});
		error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				startActivity(new Intent(SearchActivity.this, DashboardActivity.class));
				//TODO: Check integrity, 21.2.2014 12:25
				SearchActivity.this.finish();
			}
			});
		error.show();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_lista_vijesti);
		initUI();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnSearchListaVijesti:
			showSearchPopup();
			break;
		case R.id.btnHomeListaVijesti:
			startActivity(new Intent(SearchActivity.this,DashboardActivity.class));
			break;
		case R.id.btnBackListaVijesti:
			SearchActivity.this.onBackPressed();
			break;
		}
	}
	
	private class ListClickHandler implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent i = new Intent(SearchActivity.this,VijestActivity.class);
			i.putExtra("nid", listaVijesti.get(arg2).getNid());
			startActivity(i);
		}
		
	}
	
	private void showEmptyListInfo(){
		AlertDialog.Builder emptyInfo = new AlertDialog.Builder(this);
		emptyInfo.setTitle("Pretraga");
		emptyInfo.setMessage("Pretraga nije vratila rezultate");
		
		emptyInfo.setPositiveButton("U redu", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {SearchActivity.this.onBackPressed();}
		});
		
		emptyInfo.show();
	}
	
	private class AsyncHttpPostTask extends AsyncTask<String, Void, Void>{
		private String URL = null;
		private ProgressDialog pDialog;
				
		private InputStream is = null;
		private JSONObject jObj = null;
		private String json = null;
				
		public AsyncHttpPostTask(Context context){
			this.URL="http://novaeva.com/json?api=2&filter=1&search=";
									
			pDialog = new ProgressDialog(context);
	        pDialog.setMessage("Učitavam...");
	        pDialog.setIndeterminate(true);
	        pDialog.setCancelable(true);
	        
	        this.pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Odustani", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
			
			pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					AsyncHttpPostTask.this.cancel(true);
				}
			});
		}
		
		@Override
		protected void onCancelled(){
			SearchActivity.this.onBackPressed();
		}
		
		@Override
		protected void onPreExecute(){pDialog.show();}
		
		@Override
		protected Void doInBackground(String... params) {
			if(params.length < 1){
				this.cancel(true);
			} else {
				this.URL += params[0];
			}
			
			try{
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				HttpClient httpClient = new DefaultHttpClient(httpParams);

				HttpGet httpGet = new HttpGet(this.URL);
				
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntitiy = httpResponse.getEntity();
				is = httpEntitiy.getContent();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null){
					sb.append(line + "\n");
				}
				is.close();
				json = sb.toString();
				
				jObj = new JSONObject(json);
				
				//TODO: testirati ove malverzacije sa throws JSONException
				listaVijesti.addAll(parseCidList(jObj));
			}
			catch(Exception e){
				pDialog.dismiss();
				
				SearchActivity.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {showErrorPopup();}
				});
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param){
			pDialog.dismiss();
			pDialog = null;
			adapter.notifyDataSetChanged();
			
			if(listaVijesti.isEmpty()){
				SearchActivity.this.showEmptyListInfo();
			}
		}
		
		private List<ListElement> parseCidList(JSONObject cidList) throws JSONException{
			List<ListElement> listaCidova = new ArrayList<ListElement>();
			JSONObject jednaVijest;
			ListElement listElement;
			
			if(cidList.has("vijesti")){
				if(!cidList.get("vijesti").equals(null)){
					JSONArray poljeVijesti = cidList.getJSONArray("vijesti");
					for(int i = 0; i < poljeVijesti.length(); i++){
						listElement = new ListElement();
						jednaVijest = poljeVijesti.getJSONObject(i);
						
						listElement.setNid(Integer.parseInt(jednaVijest.getString("nid")));
						listElement.setUnixDatum(jednaVijest.getString("datum"));
						listElement.setNaslov(jednaVijest.getString("naslov"));
						listElement.setUvod(jednaVijest.getString("uvod"));
						listElement.setListType(ListTypes.VIJEST);

						listaCidova.add(listElement);
					}
				}
			}
			
			return listaCidova;
		}
		
	}
}
