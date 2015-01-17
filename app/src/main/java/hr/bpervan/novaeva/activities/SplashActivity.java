package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator;
import hr.bpervan.novaeva.receivers.NewsPullReceiver;

import java.io.File;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
@SuppressWarnings("unused")
public class SplashActivity extends Activity {
		
	private SharedPreferences prefs;
	private AlarmManager alarmManager;
	
	private File cacheDir;
	private ImageLoaderConfiguration config;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);
		prefs.edit().putLong("hr.bpervan.novaeva.vrijemesinkronizacije", System.currentTimeMillis()).commit();
		
		Intent intent = new Intent(this, NewsPullReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
		
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10000), 10000, pendingIntent);
		//alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10000), pendingIntent);
		
		//Ajmo osigurati da nam notif uvijek dolazi
		//prefs.edit().putInt("hr.bpervan.novaeva.zadnjinid", 0).commit();
		
		if(!ImageLoader.getInstance().isInited()){
			new ImageLoaderConfigurator(this).doInit();
		}
		
		SystemClock.sleep(500);
		startActivity(new Intent(this,DashboardActivity.class));
	}
}
