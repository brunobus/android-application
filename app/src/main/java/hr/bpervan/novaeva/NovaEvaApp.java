package hr.bpervan.novaeva;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Map;

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
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID) : null;
            t.enableAdvertisingIdCollection(false);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER
    }
}
