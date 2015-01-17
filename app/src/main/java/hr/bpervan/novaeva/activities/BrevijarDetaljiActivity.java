package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.NovaEvaApp;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

public class BrevijarDetaljiActivity extends Activity {

	private WebView webView;
	private String BREV_CAT;
	private String tempHTML;
	
	private Tracker mGaTracker;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_brevijar_detalji);
		
		BREV_CAT = String.valueOf(getIntent().getIntExtra("BREV_CAT", 4));

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Brevijar")
                        .setAction("OtvorenaMolitva")
                        .setLabel(BREV_CAT)
                        .build()
        );

		//mGaTracker.sendEvent("Brevijar", "OtvorenaMolitva", BREV_CAT, null);
		
		initUI();

		if(!ConnectionChecker.hasConnection(this)){
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(BrevijarDetaljiActivity.this,DashboardActivity.class));
		}else{
			new AsyncHttpPostTask(this).execute(BREV_CAT);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {return true;}
	
	private void initUI(){
		webView = (WebView) findViewById(R.id.webViewBrevijar);
		/*webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(true);
		webView.setBackgroundColor(color.background_light);*/
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setLongClickable(false);
        setTitle();

		webView.getSettings().setDefaultTextEncodingName("utf-8");
	}
	
	private void setTitle(){
		String activityTitle = "Brevijar - ";
		switch(getIntent().getIntExtra("BREV_CAT", 4)){
		case 1:
			activityTitle += "Jučer, Jutarnja";
			break;
		case 2:
			activityTitle += "Jučer, Večernja";
			break;
		case 3:
			activityTitle += "Jučer, Povečerje";
			break;
		case 4:
			activityTitle += "Danas, Jutarnja";
			break;
		case 5:
			activityTitle += "Danas, Večernja";
			break;
		case 6:
			activityTitle += "Danas, Povečerje";
			break;
		case 7:
			activityTitle += "Sutra, Jutarnja";
			break;
		case 8:
			activityTitle += "Sutra, Večernja";
			break;
		case 9:
			activityTitle += "Sutra, Povečerje";
			break;
		}
		this.setTitle(activityTitle);
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
				new AsyncHttpPostTask(BrevijarDetaljiActivity.this).execute(BREV_CAT);
			}
		});
		error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				startActivity(new Intent(BrevijarDetaljiActivity.this, DashboardActivity.class));
				//TODO: Check integrity, 21.2.2014 12:25
				BrevijarDetaljiActivity.this.finish();
			}
			});
		error.show();
	}
	
	private class AsyncHttpPostTask extends AsyncTask<String, Void, Void>{
		private String URL = null;
		private ProgressDialog pDialog;
	
		private InputStream is = null;
		private JSONObject jObj = null;
		private String json = null;
				
		public AsyncHttpPostTask(Context context){
			this.URL = "http://novaeva.com/json?api=2&brev=";

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
                    BrevijarDetaljiActivity.this.onBackPressed();
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
			try{
				this.URL = this.URL + params[0];
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient httpClient = new DefaultHttpClient(httpParams);

				HttpGet httpGet = new HttpGet(URL);
				
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntitiy = httpResponse.getEntity();
				is = httpEntitiy.getContent();
			}catch(IOException e){
                BrevijarDetaljiActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorPopup();
                    }
                });

				this.cancel(true);
			}
			
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null){
					sb.append(line + "\n");
				}
				is.close();
				json = sb.toString();
			}catch(Exception e){
				Log.e("Greska na medjuspremniku","Greska sa konvertiranjem " + e.toString());
			}
			
			try{
				jObj = new JSONObject(json);
				tempHTML = jObj.getString("tekst");
			}catch(JSONException e){
				tempHTML = "Greška";
				Log.e("JSON Parser","Greska u parsiranju JSONa " + e.toString());
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void param){
            super.onPostExecute(param);
			webView.loadDataWithBaseURL(null, tempHTML, "text/html", "utf-8", "");
			pDialog.dismiss();
		}
	}
}