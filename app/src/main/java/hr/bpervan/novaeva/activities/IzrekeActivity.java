package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ConnectionChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.analytics.tracking.android.EasyTracker;

public class IzrekeActivity extends Activity implements OnClickListener{
	@SuppressWarnings("unused")
	private TextView tvNaslov, tvKategorija, tvText;
	private ImageView btnObnovi;
	private ImageView btnHome, btnFace, btnMail, btnSearch, btnTextPlus, btnBack;
	
	/** Test test test */
	private WebView webView;
	
	private String naslov,tekst,nid;
	private SharedPreferences prefs;
	
	private static Bundle savedInstanceState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_izreke);
		
		IzrekeActivity.savedInstanceState = savedInstanceState;
		
		prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);
		prefs.edit().putInt("vidjenoKategorija1", 1).commit();
		
		initUI();

		if(ConnectionChecker.hasConnection(this)){
			new AsyncHttpPostTask(this).execute("");
		} else {
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(IzrekeActivity.this,DashboardActivity.class));
		}
		
	}
	
	private void initUI(){
		tvNaslov = (TextView)findViewById(R.id.tvNaslov);
		tvKategorija = (TextView)findViewById(R.id.tvKategorija);
		
		webView = (WebView) findViewById(R.id.webText);
		webView.getSettings().setDefaultTextEncodingName("utf-8");

		tvKategorija.setText("Izreke");
		
		btnObnovi = (ImageView)findViewById(R.id.btnObnovi);
		btnHome = (ImageView) findViewById(R.id.btnHome);
		btnFace = (ImageView) findViewById(R.id.btnFace);
		btnMail = (ImageView) findViewById(R.id.btnMail);
		btnSearch = (ImageView) findViewById(R.id.btnSearch);
		btnTextPlus = (ImageView) findViewById(R.id.btnTextPlus);
		btnBack = (ImageView) findViewById(R.id.btnBack);
		btnObnovi.setOnClickListener(this);
		btnHome.setOnClickListener(this);
		btnFace.setOnClickListener(this);
		btnMail.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btnTextPlus.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		
		webView.getSettings().setDefaultFontSize(prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14));

        getWindow().getDecorView().setBackgroundColor(this.getResources().getColor(android.R.color.background_light));
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_izreke);
		
		initUI();
		
		tvNaslov.setText(naslov);
		webView.loadDataWithBaseURL(null, tekst, "text/html", "UTF-8", "");
    }
	

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnObnovi:
			onCreate(savedInstanceState);
			break;
		case R.id.btnHome:
			startActivity(new Intent(this, DashboardActivity.class));
			break;
		case R.id.btnSearch:
			if(ConnectionChecker.hasConnection(this))
				showSearchPopup();
			break;
		case R.id.btnBookmark:
			break;
		case R.id.btnFace:
			CharSequence temp = "http://novaeva.com/node/" + nid;		
			Intent faceIntent = new Intent(Intent.ACTION_SEND);
			faceIntent.setType("text/plain");
			faceIntent.putExtra(Intent.EXTRA_TEXT,temp);
			startActivity(Intent.createChooser(faceIntent, "Facebook"));
			break;
		case R.id.btnMail:
			String temp2 = "http://novaeva.com/node/" + nid;
			Intent mailIntent = new Intent(Intent.ACTION_SEND);
			mailIntent.setType("message/rfc822"); //ovo ispipati još malo
			mailIntent.putExtra(Intent.EXTRA_SUBJECT, naslov);
			mailIntent.putExtra(Intent.EXTRA_TEXT, temp2);
			startActivity(Intent.createChooser(mailIntent, "Odaberite aplikaciju"));
			break;
		case R.id.btnTextPlus:
			//showTextSizePopup();
			int mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14);
			mCurrentSize += 2;
			if(mCurrentSize >= 28){
				mCurrentSize = 12;
			}
				
			prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).commit();
			webView.getSettings().setDefaultFontSize(mCurrentSize);
			break;
		case R.id.btnBack:
			IzrekeActivity.this.onBackPressed();
			break;
		}
		
	}

	
	public void onStart(){
		super.onStart();
		//EasyTracker.getInstance().activityStart(this);
	}
	
	public void onStop(){
		super.onStop();
		//EasyTracker.getInstance().activityStop(this);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		return true;
		
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
				Intent i=new Intent(IzrekeActivity.this,SearchActivity.class);	
				i.putExtra("string", search);
				startActivity(i);
			}
		});
		
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_izreke, menu);
		return true;
	}
	
	private void showErrorPopup(){
		AlertDialog.Builder error = new AlertDialog.Builder(this);
		error.setTitle("Greška");
		
		final TextView tv = new TextView(this);
		tv.setText("Greška pri dohvaćanju podataka sa poslužitelja");
		//tv.setTypeface(openSansRegular);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		error.setView(tv);
				
		error.setPositiveButton("Pokušaj ponovno", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new AsyncHttpPostTask(IzrekeActivity.this).execute("");
			}
		});
		error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				startActivity(new Intent(IzrekeActivity.this, DashboardActivity.class));
				//TODO: Check integrity, 21.2.2014 12:25
				IzrekeActivity.this.finish();
			}
			});
		error.show();
	}

	private class AsyncHttpPostTask extends AsyncTask<String, Void, Void>{
		private String URL=null;
		private ProgressDialog pDialog;		
		
		private InputStream is = null;
		private JSONObject jObj = null;
		private String json = null;
		
		
		@SuppressWarnings("unused")
		private Context context;
		
		public AsyncHttpPostTask(Context context){
			this.context=context;
			URL="http://novaeva.com/json?api=2&cid=1&rand=1";
			
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
                    IzrekeActivity.this.onBackPressed();
                }
            });
			
		}

		protected void onPreExecute(){
	        super.onPreExecute();
            pDialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			try{
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				HttpClient httpClient=new DefaultHttpClient(httpParams);

				HttpGet httpGet=new HttpGet(URL);
				
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntitiy=httpResponse.getEntity();
				is=httpEntitiy.getContent();
			} catch(IOException e){
				//TODO: tu bi moglo bit svšta xD
                IzrekeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorPopup();
                    }
                });
				this.cancel(true);
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
				Log.e("Greska na medjuspremniku","Greska sa konvertiranjem "+e.toString());
			}
			
			try{
				jObj=new JSONObject(json);
			}catch(JSONException e){
				Log.e("JSON Parser","Greska u parsiranju JSONa "+e.toString());
			}
			
			try {
				JSONArray jArray = jObj.getJSONArray("vijesti");
				JSONObject c = jArray.getJSONObject(0);
				
				naslov = c.getString("naslov");
				tekst = c.getString("tekst");
				nid = c.getString("nid");
					
			} catch (JSONException e) {
				Log.e("Greska na medjuspremniku","Greska sa konvertiranjem " + e.toString());
				showErrorPopup();
				this.cancel(true);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
            super.onPostExecute(params);
			tvNaslov.setText(naslov);
			webView.loadDataWithBaseURL(null, tekst, "text/html", "UTF-8", "");
			pDialog.dismiss();
		}
	}
}
