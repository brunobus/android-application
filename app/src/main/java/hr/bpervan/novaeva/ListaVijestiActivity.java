package hr.bpervan.novaeva;

import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.ListElement;
import hr.bpervan.novaeva.utilities.ListTypes;
import hr.bpervan.novaeva.utilities.ResourceHandler;
import hr.bpervan.novaeva.utilities.VijestAdapter;
import hr.bpervan.novaeva.v2.VijestActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

public class ListaVijestiActivity extends ListActivity implements OnClickListener{
	
	private boolean hasMore = true;
	private int kategorija = 0, colourset = 0;
	
	private ListView mainListView;
	private List<ListElement> listaVijesti;
	
	private VijestAdapter adapter;
	
	private Button btnLoadMore;
	private LinearLayout fakeActionBarListaVijesti;
	
	private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;
	
	private SharedPreferences prefs;
	
	private ImageView btnHome, btnSearch, btnBack;
	private ImageView btnImamPitanje;
    private static Typeface openSansBold, openSansItalic, openSansLight, openSansRegular;
    
    private boolean hasParsedSubcatArray = false;
    
    private ResourceHandler resourceHandler;
    private Intent inputIntent;
        	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_vijesti);

        inputIntent = this.getIntent();
		
		prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);

		openSansBold = Typeface.createFromAsset(getAssets(), "opensans-bold.ttf");
		openSansItalic = Typeface.createFromAsset(getAssets(), "opensans-italic.ttf");
		openSansLight = Typeface.createFromAsset(getAssets(), "opensans-light.ttf");
		openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");
		
		mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-40344870-1");
		
		btnLoadMore = new Button(this);
		btnLoadMore.setText("Učitaj još");
		listaVijesti = new ArrayList<ListElement>();
		kategorija = getIntent().getIntExtra("kategorija", 11);
		if(inputIntent.hasExtra("colourset")){
			colourset = inputIntent.getIntExtra("colourset", 11);
		} else {
			colourset = kategorija;
		}
		
		mGaTracker.sendEvent("Kategorije", "OtvorenaKategorija", kategorija + "", null);
		
		killRedDot(kategorija);
		resourceHandler = new ResourceHandler(colourset);
		
		initUI();
		if(!ConnectionChecker.hasConnection(this)){
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(ListaVijestiActivity.this,DashboardActivity.class));
		}else{
			new AsyncHttpPostTask(this).execute(kategorija + "");
		}
	}
	
	private void initUI(){
		btnHome = (ImageView) findViewById(R.id.btnHomeListaVijesti);
		btnSearch = (ImageView) findViewById(R.id.btnSearchListaVijesti);
		btnBack = (ImageView) findViewById(R.id.btnBackListaVijesti);
		btnHome.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		
		if(kategorija == Constants.CAT_ODGOVORI){
			btnImamPitanje = (ImageView) findViewById(R.id.btnImamPitanjeListaVijesti);
			btnImamPitanje.setOnClickListener(this);
			btnImamPitanje.setVisibility(View.VISIBLE);
		}
		

		fakeActionBarListaVijesti = (LinearLayout) findViewById(R.id.fakeActionBarListaVijesti);
		
		this.setCategoryTypeColour();

		mainListView = getListView();

        View headerView = getLayoutInflater().inflate(R.layout.izbornik_top, null);
        headerView.setBackgroundResource(resourceHandler.getListViewHeader(
                this.getResources().getConfiguration().orientation));
        TextView headerTextView = (TextView) headerView.findViewById(R.id.izbornikTopNazivKategorije);
        TextView headerTextViewNatpis = (TextView) headerView.findViewById(R.id.izbornikTopNatpis);
        headerTextView.setTypeface(openSansBold);
        headerTextViewNatpis.setTypeface(openSansBold);
        if(!this.inputIntent.getBooleanExtra("isPotkategorija", false)){
            headerTextViewNatpis.setText("NALAZITE SE U KATEGORIJI");
            headerTextView.setText(Constants.getCatNameById(kategorija).toUpperCase());
        } else {
            headerTextViewNatpis.setText("NALAZITE SE U MAPI");
            headerTextView.setText(this.inputIntent.getStringExtra("nazivKategorije").toUpperCase());
        }




        mainListView.addHeaderView(headerView);
		mainListView.setOnScrollListener(new EndlessScrollListener());
		mainListView.setOnItemClickListener(new ListClickHandler());

		//TODO: getScrollX()
		//ovo čudo ne treba uvijek raditi, adapter je u memoriji, zašto ga svaki put
		//stvarati?, samo set adapter treba napraviti
		adapter = new VijestAdapter(this, listaVijesti, openSansBold, openSansItalic, openSansLight, openSansRegular, colourset);
		mainListView.setAdapter(adapter);
	}
	
	private class ListClickHandler implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int listViewPosition,
				long arg3) {
			Intent i;
            int position = listViewPosition - 1;
            if(position >= 0){
                if(listaVijesti.get(position).getListType() == ListTypes.PODKATEGORIJA){
                    i = new Intent(ListaVijestiActivity.this,ListaVijestiActivity.class);
                    i.putExtra("kategorija", listaVijesti.get(position).getKategorija());
                    i.putExtra("nazivKategorije", listaVijesti.get(position).getNaslov());
                    i.putExtra("isPotkategorija", true);
                    //TODO: check integrity - 4.1.2013 16:27 -> so far so good
                    if(colourset != 0){
                        i.putExtra("colourset", colourset);
                    }else{
                        i.putExtra("colourset", kategorija);
                    }
                } else {
                    i = new Intent(ListaVijestiActivity.this,VijestActivity.class);
                    i.putExtra("nid", listaVijesti.get(position).getNid());
                    i.putExtra("isPotkategorija", false);
                    if(colourset != 0){
                        i.putExtra("kategorija", colourset);
                    }else{
                        i.putExtra("kategorija", kategorija);
                    }

                }
                startActivity(i);
            }
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
			fakeActionBarListaVijesti.setBackgroundResource(resourceHandler.getResourceId(Configuration.ORIENTATION_LANDSCAPE));
		} else {
			fakeActionBarListaVijesti.setBackgroundResource(resourceHandler.getResourceId(Configuration.ORIENTATION_PORTRAIT));
		}
	}
	
	public void onStart(){
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	public void onStop(){
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
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
		}
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
	//TODO: dogodilo se da je krepao activity, windowmanager, badtokenexception
	//nije bilo activityja da se popup attacha
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
				new AsyncHttpPostTask(ListaVijestiActivity.this).execute(kategorija + "");
			}
		});
		error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				startActivity(new Intent(ListaVijestiActivity.this, DashboardActivity.class));
				//TODO: Check integrity, 21.2.2014 12:25
				//ListaVijestiActivity.this.finish();
				ListaVijestiActivity.this.onBackPressed();
			}
			});
		error.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {return true;}

	private void killRedDot(int kategorija){
		prefs.edit().putInt("vidjenoKategorija" + kategorija, 1).commit();
	}
	
	private class AsyncHttpPostTask extends AsyncTask<String, Void, Void>{
		private String URL = null;
		private ProgressDialog pDialog;
				
		private InputStream is = null;
		private JSONObject jObj = null;
		private String json = null;
				
		public AsyncHttpPostTask(Context context){
			this.URL="http://novaeva.com/json?api=2&items=20&filter=1&cid=";
									
			pDialog = new ProgressDialog(context);
	        pDialog.setMessage("Učitavam...");
	        pDialog.setIndeterminate(true);
	        pDialog.setCancelable(false);
            this.pDialog.setCanceledOnTouchOutside(false);
	        
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
					ListaVijestiActivity.this.onBackPressed();
				}
			});
		}
		
		@Override
		protected void onPreExecute(){
	        super.onPreExecute();
            pDialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			if(params.length == 1){
				this.URL = this.URL + params[0];
			} else{
				if(params.length == 2){
					this.URL = this.URL + params[0] + "&date=" + params[1];
				}
			}
			try{
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				HttpClient httpClient = new DefaultHttpClient(httpParams);
				HttpPost httpPost = new HttpPost(this.URL);

				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntitiy = httpResponse.getEntity();
				is = httpEntitiy.getContent();
                Log.d("NetworkDebug", "Promet gotov");
			}
			catch(Exception e){
				pDialog.dismiss();
				this.cancel(true);
				ListaVijestiActivity.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {showErrorPopup();}
				});
			}
				
			try{
                Log.d("NetworkDebug", "Počinje parsiranje");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null){
					sb.append(line + "\n");
				}
                Log.d("NetworkDebug", "Parsiranje gotovo");
				is.close();
				json = sb.toString();
			} catch (IOException e) {
				pDialog.dismiss();
				this.cancel(true);
				ListaVijestiActivity.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {showErrorPopup();}
				});
			}
			
			try{
				jObj = new JSONObject(json);
			}catch(JSONException e){
				Log.e("JSON Parser","Greska u parsiranju JSONa "+e.toString());
			}
			
			
			listaVijesti.addAll(parseCidList(jObj));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param){
            super.onPostExecute(param);
			adapter.notifyDataSetChanged();
			try {
				if(jObj.has("jos")){
					if(jObj.getInt("jos") == 0){
						hasMore = false;
					} else {
						hasMore = true;
					}
				} else {
					hasMore = false;
				}
				
				if(jObj.has("image")){
					JSONObject temp = jObj.getJSONObject("image");
					prefs.edit().putString("hr.bpervan.novaeva.categoryheader." + 
							ListaVijestiActivity.this.inputIntent.getIntExtra("kategorija",11), temp.getString("640")).commit();

				}
			} catch (NumberFormatException e) {
				//e.printStackTrace();
			} catch (JSONException e) {
				//e.printStackTrace();
			}
			pDialog.dismiss();
		}
		
		private List<ListElement> parseCidList(JSONObject cidList){
			List<ListElement> listaCidova = new ArrayList<ListElement>();
			ListElement listElement;
			
			JSONObject jednaVijest;
			try {
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
							
							if(jednaVijest.has("attach")){
								JSONObject poljeAttach = jednaVijest.getJSONObject("attach");
								if(poljeAttach.getBoolean("video")){
									listElement.itHasVideo();
								}
								
								if(poljeAttach.getBoolean("documents")){
									listElement.itHasDocuments();
								}
								
								if(poljeAttach.getBoolean("music")){
									listElement.itHasMusic();
								}
								
								if(poljeAttach.getBoolean("images")){
									listElement.itHasImages();
								}
								
								if(poljeAttach.getBoolean("text")){
									listElement.itHasText();
								}
							}
							
							listaCidova.add(listElement);
						}
					}
				}
				
				//TODO: i ove malverzacije treba testirati
				if(!hasParsedSubcatArray){
					parseSubcatArray(cidList, listaCidova);
				}
			} catch (JSONException e) {
				//e.printStackTrace();
				return null;
			}
			

			

			return listaCidova;
		}

		private void parseSubcatArray(JSONObject cidList,
				List<ListElement> listaCidova) throws JSONException{
		
		ListElement listElement;
		JSONObject jednaPodkategorija;	
			if(!cidList.get("subcat").equals(null)){
				JSONArray poljePodkategorija = cidList.getJSONArray("subcat");
				if(poljePodkategorija != null){
					for(int i = 0; i < poljePodkategorija.length(); i++){
						listElement = new ListElement();
						jednaPodkategorija = poljePodkategorija.getJSONObject(i);
					
						listElement.setKategorija(Integer.parseInt(jednaPodkategorija.getString("cid")));
						listElement.setNaslov(jednaPodkategorija.getString("name"));
						listElement.setListType(ListTypes.PODKATEGORIJA);
						listaCidova.add(listElement);
					}
				}
			}	
				
			hasParsedSubcatArray = true;
		}
	}
	
	private class EndlessScrollListener implements OnScrollListener{
		private int visibleThreshold = 5;
	    private int previousTotal = 0;
	    private boolean loading = true;
	    public EndlessScrollListener() {
	    	this.visibleThreshold = 2;
	    }
	    @Override
	    public void onScroll(AbsListView view, int firstVisibleItem,
	            int visibleItemCount, int totalItemCount) {

	        if (loading) {
	            if (totalItemCount > previousTotal) {
	                loading = false;
	                previousTotal = totalItemCount;
	            }
	        }
	        if((firstVisibleItem > 0) && (visibleItemCount > 0) && (totalItemCount > 0))
	        if (!loading && hasMore && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
				String zadnjiDatum = null;
				
				/** Ako je zadnji u listi podkategorija, onda on nema UnixDatum, pa tražimo zadnji koji ima*/
				for(int i = 1; i < listaVijesti.size(); ++i){
					zadnjiDatum = listaVijesti.get(listaVijesti.size() - i).getUnixDatum();
					if(zadnjiDatum != null){
						break;
					}		
				}
				
				new AsyncHttpPostTask(ListaVijestiActivity.this).execute(kategorija + "",zadnjiDatum);
	            loading = true;
	        }
	    }
	    @Override
	    public void onScrollStateChanged(AbsListView view, int scrollState) {}
	}

}