package hr.bpervan.novaeva.receivers;

import hr.bpervan.novaeva.activities.VijestActivity;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class NewsPullReceiver extends BroadcastReceiver{
	
	private JSONObject jObj;
	private SharedPreferences prefs;
	private NotificationCompat.Builder mBuilder;
	private Intent resultIntent;
	private TaskStackBuilder stackBuilder;
	private NotificationManager mNotificationManager;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		prefs = context.getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE);
		mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher);
		if(ConnectionChecker.hasConnection(context)){
			new AsyncHttpPostTask(context).execute();
		}
	}
	
	private class AsyncHttpPostTask extends AsyncTask<Void, Void, Void>{		
		private InputStream is = null;
		private String json = null;
		private Context context;
				
		public AsyncHttpPostTask(Context context){
			this.context = context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try{
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(Constants.alertURL);
				
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntitiy = httpResponse.getEntity();
				is = httpEntitiy.getContent();
			}catch(IOException e){
				/** Hoće li ovo proći dobro? */
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
				Log.e("Greska na medjuspremniku","Greska sa konvertiranjem "+e.toString());
			}
			
			try{
				jObj = new JSONObject(json);
				if(jObj.getInt("rezultat") == 0){
					JSONObject zadnjaVijest = jObj.getJSONArray("vijesti").getJSONObject(0);
					int zadnjiNid = zadnjaVijest.getInt("nid");
					if(zadnjiNid != prefs.getInt("hr.bpervan.novaeva.zadnjinid", 0)){
						mBuilder.setContentTitle("Nova vijest");
						mBuilder.setContentText(zadnjaVijest.getString("naslov"));
						mBuilder.setAutoCancel(true);
						mBuilder.setOnlyAlertOnce(true);
						
						resultIntent = new Intent(context, VijestActivity.class);
						resultIntent.putExtra("nid", Integer.parseInt(zadnjaVijest.getString("nid")));
						resultIntent.putExtra("kategorija", 18); //Ovo je banana, jer response ne vraća kategoriju :-/
						
						stackBuilder = TaskStackBuilder.create(context);
						stackBuilder.addParentStack(VijestActivity.class);
						stackBuilder.addNextIntent(resultIntent);
						
						PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
						mBuilder.setContentIntent(resultPendingIntent);
						
						Notification notification = mBuilder.build();
						notification.defaults |= Notification.DEFAULT_ALL;
						
						mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.notify(0, notification);
						prefs.edit().putInt("hr.bpervan.novaeva.zadnjinid", zadnjiNid).commit();
					}
				}
			}catch(JSONException e){
				/** !!! */
				Log.e("JSON Parser","Greska u parsiranju JSONa "+e.toString());
				this.cancel(true);
			}
						
			return null;
		}
	}
}

