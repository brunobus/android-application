package hr.bpervan.novaeva.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

/**
 * Created by vpriscan on 16.10.17..
 */
class LifecycleLogger : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d("activityLifecycle", "Activity created:$activity")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d("activityLifecycle", "Activity started:$activity")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("activityLifecycle", "Activity resumed:$activity")
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d("activityLifecycle", "Activity paused:$activity")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("activityLifecycle", "Activity stopped:$activity")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d("activityLifecycle", "Activity saved instance state:$activity")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("activityLifecycle", "Activity destroyed:$activity")
    }
}