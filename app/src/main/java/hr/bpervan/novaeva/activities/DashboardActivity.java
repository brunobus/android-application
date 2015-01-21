package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class DashboardActivity extends Activity implements OnTouchListener, OnClickListener{
	private final long syncInterval = 90000L;
	
	private Tracker mGaTracker;

	private ImageView btnBrevijar, btnMolitvenik, btnIzreke, btnMp3, btnAktualno,
	btnPoziv, btnOdgovori, btnMultimedia, btnPropovjedi,
	btnDuhovnost, btnEvandjelje;
	
	private ImageView btnInfo, btnBookmarks;
	private TextView titleLineTitle;
	private Typeface openSansRegular;
	
	//List<Integer> listaKategorija = Constants.getIntCategoryList();
	SharedPreferences prefs;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);
		
		prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);

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

		long vrijemeZadnjeSync = prefs.getLong("vrijemeZadnjeSinkronizacije", 0L);
		if((System.currentTimeMillis() - vrijemeZadnjeSync) > syncInterval){
			if(ConnectionChecker.hasConnection(this)){
				new AsyncHttpPostTask(this).execute();
			}
		}

		testAndSetRedDots();
	}
	
	public void onStart(){
		super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	public void onStop(){
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
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
				Intent i=new Intent(DashboardActivity.this,SearchActivity.class);	
				i.putExtra("string", search);
				startActivity(i);
			}
		});
		
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
	//TODO: sredi ovo pod hitno
	private void testAndSetRedDots(){
		if(prefs.getInt("vidjenoKategorija1", 0) == 0){
			btnIzreke.setBackgroundResource(R.drawable.button_izreke_news);
		} else {
			btnIzreke.setBackgroundResource(R.drawable.button_izreke);
		}
		if(prefs.getInt("vidjenoKategorija4", 0) == 0){
			btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje_news);
		} else {
			btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje);
		}
		if(prefs.getInt("vidjenoKategorija7", 0) == 0){
			btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi_news);
		} else {
			btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi);
		}
		if(prefs.getInt("vidjenoKategorija10", 0) == 0){
			btnMultimedia.setBackgroundResource(R.drawable.button_multimedia_news);
		} else {
			btnMultimedia.setBackgroundResource(R.drawable.button_multimedia);
		}
		if(prefs.getInt("vidjenoKategorija11", 0) == 0){
			btnOdgovori.setBackgroundResource(R.drawable.button_odgovori_news);
		} else {
			btnOdgovori.setBackgroundResource(R.drawable.button_odgovori);
		}
		if(prefs.getInt("vidjenoKategorija9", 0) == 0){
			btnAktualno.setBackgroundResource(R.drawable.button_aktualno_news);
		} else {
			btnAktualno.setBackgroundResource(R.drawable.button_aktualno);
		}
		if(prefs.getInt("vidjenoKategorija355", 0) == 0){
			btnMp3.setBackgroundResource(R.drawable.button_mp3_news);
		} else {
			btnMp3.setBackgroundResource(R.drawable.button_mp3);
		}
		if(prefs.getInt("vidjenoKategorija8", 0) == 0){
			btnPoziv.setBackgroundResource(R.drawable.button_poziv_news);
		} else {
			btnPoziv.setBackgroundResource(R.drawable.button_poziv);
		}
		if(prefs.getInt("vidjenoKategorija354", 0) == 0){
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

	private class AsyncHttpPostTask extends AsyncTask<Void, Void, Void>{
		private String URL=null;
		private String newStuffIndicatorUrl;
		
		private BufferedReader bufferedReader = null;
		private InputStream is = null;
		private JSONObject jObj = null;
		private String json = null;
		private Image brevijarHeader = null;
		
		/** Communicatiooooooon */
		private HttpClient httpClient;
		private HttpGet httpGet;
		private HttpResponse httpResponse;
		private HttpEntity httpEntity;
        private HttpParams httpParams;
		
		public AsyncHttpPostTask(Context context){
			URL="http://novaeva.com/json?api=2&items=1&filter=1&cid=";
			//TODO:Ubaciti url
			this.newStuffIndicatorUrl = "http://novaeva.com/json?api=2&indicators=1";
		}
		
		private void getNewStuff() throws ClientProtocolException, IOException, JSONException{
            httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

			httpClient = new DefaultHttpClient(httpParams);
			httpGet = new HttpGet(this.newStuffIndicatorUrl);
			httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			
			is = httpEntity.getContent();
			bufferedReader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while((line = bufferedReader.readLine()) != null){
				stringBuilder.append(line + "\n");
			}
			is.close();
			json = stringBuilder.toString();
			jObj = new JSONObject(json);
			Log.d("CAT", json);
			for(int category : Constants.getIntCategoryList(true)){
				if(jObj.has(category + "")){
					int currentCat = jObj.getInt(category + "");
					int zadnjiNidIzKategorije = prefs.getInt(category + "", 0);
					if(zadnjiNidIzKategorije != currentCat){
						prefs.edit().putInt(category + "", currentCat).commit();
						prefs.edit().putInt("vidjenoKategorija" + category, 0).commit();
					}
				}
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			String zadnjiNid = null;
            List<Integer> tempLista = Constants.getIntCategoryList(false);
/*
            for(int i = 0; i < tempLista.size(); i++){
				zadnjiNid = dohvatiNajnovijiNidIzKategorije(tempLista.get(i));
				//TODO: Testiraj ovo čudo
				//Vratilo nullPointerException na neku foru jednom
				if(!zadnjiNid.equals(null)){
					int zadnjiNidIzKategorije = prefs.getInt(String.valueOf(tempLista.get(i)), 0);
					if(zadnjiNidIzKategorije != Integer.parseInt(zadnjiNid)){
						prefs.edit().putInt(String.valueOf(tempLista.get(i)), Integer.parseInt(zadnjiNid)).commit();
						prefs.edit().putInt("vidjenoKategorija" + tempLista.get(i), 0).commit();
					}
				}
			}*/
            try {
                getNewStuff();
                fetchBrevijarImage();
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            } catch (JSONException e) {
            }

			DashboardActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					testAndSetRedDots();
					prefs.edit().putLong("vrijemeZadnjeSinkronizacije", System.currentTimeMillis()).commit();
					if(brevijarHeader != null){
						if(brevijarHeader.getUrl640() != null){
							prefs.edit().putString("hr.bpervan.novaeva.brevijarheaderimage", brevijarHeader.getUrl640()).commit();
						}
					}
				}
			});


			
			return null;
		}
		
		private Image fetchBrevijarImage() throws ClientProtocolException, IOException, JSONException{
			httpClient = new DefaultHttpClient();
			httpGet = new HttpGet("http://novaeva.com/json?api=2&cid=546");

			httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

			bufferedReader = new BufferedReader(new InputStreamReader(is,"utf-8"), 8);
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while((line = bufferedReader.readLine()) != null){
				stringBuilder.append(line + "\n");
			}
			is.close();
			json=stringBuilder.toString();

			jObj = new JSONObject(json);
			JSONObject image = jObj.getJSONObject("image");
			brevijarHeader = new Image(image.getString("640"), image.getString("720"), image.getString("date"), image.getString("original"));

			prefs.edit().putString("hr.bpervan.novaeva.brevijarheaderimage", brevijarHeader.getUrl640()).commit();
			return brevijarHeader;
		}
		
		private String dohvatiNajnovijiNidIzKategorije(int kategorija){
			String url2 = URL + kategorija;
			Log.d("URL", url2);
			try{
				
				DefaultHttpClient httpClient=new DefaultHttpClient();
				HttpPost httpPost=new HttpPost(url2);
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntitiy=httpResponse.getEntity();
				is=httpEntitiy.getContent();
			}catch(Exception e){
				this.cancel(true);
				return null;
			}
			
			try{

				BufferedReader reader=new BufferedReader(new InputStreamReader(is,"utf-8"),8);
				StringBuilder sb=new StringBuilder();
				String line=null;
				while((line=reader.readLine())!=null){
					sb.append(line+"\n");
				}
				is.close();
				json=sb.toString();
			}catch(Exception e){
				this.cancel(true);
				return null;
			}
			
			try{
				jObj=new JSONObject(json);
			}catch(JSONException e){
				this.cancel(true);
				return null;
			}
			
			try {
				JSONArray sveVijesti = jObj.getJSONArray("vijesti");
				JSONObject najnovijaVijest = sveVijesti.getJSONObject(0);
				return najnovijaVijest.getString("nid");
			} catch (JSONException e) {
				this.cancel(true);
				return null;
			}
		}
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
				i = new Intent(DashboardActivity.this,BrevijarActivity.class);
				startActivity(i);
				break;
			case R.id.btnEvandjelje:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_EVANDJELJE);
				i.putExtra("nazivKategorije", "Evanđelje");
				startActivity(i);
				break;
			case R.id.btnMp3:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_PJESMARICA);
				i.putExtra("nazivKategorije", "Pjesmarica");
				startActivity(i);
				break;
			case R.id.btnPropovjedi:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_PROPOVJEDI);
				i.putExtra("nazivKategorije", "Propovijedi");
				startActivity(i);
				break;
			case R.id.btnOdgovori:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_ODGOVORI);
				i.putExtra("nazivKategorije", "Odgovori");
				startActivity(i);
				break;
			case R.id.btnPoziv:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_POZIV);
				i.putExtra("nazivKategorije", "Poziv");
				startActivity(i);
				break;
			case R.id.btnDuhovnost:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_DUHOVNOST);
				i.putExtra("nazivKategorije", "Duhovnost");
				startActivity(i);
				break;
			case R.id.btnIzreke:
				i = new Intent(DashboardActivity.this,IzrekeActivity.class);
                mGaTracker.send(
                        new HitBuilders.EventBuilder()
                                .setCategory("Kategorije")
                                .setAction("OtvorenaKategorija")
                                .setLabel(Constants.CAT_IZREKE + "")
                                .build()
                );
				//mGaTracker.sendEvent("Kategorije", "OtvorenaKategorija", Constants.CAT_IZREKE + "", null);
				startActivity(i);
				break;
			case R.id.btnMultimedia:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_MULTIMEDIJA);
				i.putExtra("nazivKategorije", "Multimedija");
				startActivity(i);
				break;
			case R.id.btnAktualno:
				i = new Intent(DashboardActivity.this,ListaVijestiActivity.class);
				i.putExtra("kategorija", Constants.CAT_AKTUALNO);
				i.putExtra("nazivKategorije", "Aktualno");
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
			text = Constants.APP_NAME;
		} else {
			text = Constants.APP_NAME_LAND;
		}
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			switch(v.getId()){
			case R.id.btnBrevijar:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_BREVIJAR_NAZIV_PORT;
				else{
					text = Constants.CAT_BREVIJAR_NAZIV_LAND;
				}
				break;
			case R.id.btnMolitvenik:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_MOLITVENIK_NAZIV_PORT;
				else{
					text = Constants.CAT_MOLITVENIK_NAZIV_LAND;
				}
				break;
			case R.id.btnEvandjelje:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_EVANDJELJE_NAZIV_PORT;
				else{
					text = Constants.CAT_EVANDJELJE_NAZIV_LAND;
				}
				break;
			case R.id.btnMp3:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_PJESMARICA_NAZIV_PORT;
				else{
					text = Constants.CAT_PJESMARICA_NAZIV_LAND;
				}
				break;
			case R.id.btnPropovjedi:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_PROPOVJEDI_NAZIV_PORT;
				else{
					text = Constants.CAT_PROPOVJEDI_NAZIV_LAND;
				}
				break;
			case R.id.btnOdgovori:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_ODGOVORI_NAZIV_PORT;
				else{
					text = Constants.CAT_ODGOVORI_NAZIV_LAND;
				}
				break;
			case R.id.btnPoziv:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_POZIV_NAZIV_PORT;
				else{
					text = Constants.CAT_POZIV_NAZIV_LAND;
				}
				break;
			case R.id.btnDuhovnost:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_DUHOVNOST_NAZIV_PORT;
				else{
					text = Constants.CAT_DUHOVNOST_NAZIV_LAND;
				}
				break;
			case R.id.btnIzreke:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_IZREKE_NAZIV_PORT;
				else{
					text = Constants.CAT_IZREKE_NAZIV_LAND;
				}
				break;
			case R.id.btnMultimedia:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_MULTIMEDIJA_NAZIV_PORT;
				else{
					text = Constants.CAT_MULTIMEDIJA_NAZIV_LAND;
				}
				break;
			case R.id.btnAktualno:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_AKTUALNO_NAZIV_PORT;
				else{
					text = Constants.CAT_AKTUALNO_NAZIV_LAND;
				}
				break;
			case R.id.btnInfo:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_INFO_NAZIV_PORT;
				else{
					text = Constants.CAT_INFO_NAZIV_LAND;
				}
				break;
			case R.id.btnBookmarks:
				if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					text = Constants.CAT_BOOKMARKS_NAZIV_PORT;
				else{
					text = Constants.CAT_BOOKMARKS_NAZIV_LAND;
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