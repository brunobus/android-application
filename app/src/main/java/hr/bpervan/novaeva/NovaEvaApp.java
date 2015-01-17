package hr.bpervan.novaeva;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Map;

import hr.bpervan.novaeva.main.R;

/**
 * Created by Branimir on 17.1.2015..
 */
public class NovaEvaApp extends Application{

    private static final String PROPERTY_ID = "UA-40344870-1";
    public static int GENERAL_TRACKER = 0;

    Map<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public NovaEvaApp(){
        super();
    }

    public synchronized Tracker getTracker(TrackerName trackerId){
        if(!mTrackers.containsKey(trackerId)){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            Tracker t = null;
            switch (trackerId){
                case APP_TRACKER:
                    t = analytics.newTracker(PROPERTY_ID);
                    break;
                case GLOBAL_TRACKER:
                    t = analytics.newTracker(R.xml.global_tracker);
                    break;
            }

            t.enableAdvertisingIdCollection(false);
            Log.d("NULL", (t == null) ? "NULL JE" : "NIJE NULL");
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER
    }
}
