package hr.bpervan.novaeva.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.receivers.NewsPullReceiver

class SplashActivity : EvaBaseActivity() {

    private var alarmManager: AlarmManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefs.edit().putLong("hr.bpervan.novaeva.vrijemesinkronizacije", System.currentTimeMillis()).apply()

        val intent = Intent(this, NewsPullReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this.applicationContext, 0, intent, 0)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10000), 10000, pendingIntent);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10000), pendingIntent);

        //Ajmo osigurati da nam notif uvijek dolazi
        //prefs.edit().putInt("hr.bpervan.novaeva.zadnjinid", 0).commit();

        SystemClock.sleep(500)
        finish()
        startActivity(Intent(this, DashboardActivity::class.java))
    }
}
