package hr.bpervan.novaeva.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NewsPullBootStarter extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		Intent i = new Intent(context, NewsPullReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10000), 10000, pendingIntent);
		*/
	}

}
