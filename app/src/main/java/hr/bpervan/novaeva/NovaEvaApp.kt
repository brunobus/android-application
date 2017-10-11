package hr.bpervan.novaeva

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Logger
import com.google.android.gms.analytics.Tracker
import hr.bpervan.novaeva.activities.DashboardActivity
import hr.bpervan.novaeva.activities.SearchActivity

import java.util.HashMap

import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R

/**
 * Created by Branimir on 17.1.2015..
 */
class NovaEvaApp : Application() {
    private var mTrackers: MutableMap<TrackerName, Tracker> = HashMap()

    @Synchronized
    fun getTracker(trackerId: TrackerName): Tracker {
        if (!mTrackers.containsKey(trackerId)) {
            val analytics = GoogleAnalytics.getInstance(this)
            analytics.logger.logLevel = Logger.LogLevel.VERBOSE

            val t: Tracker = when (trackerId) {
                NovaEvaApp.TrackerName.APP_TRACKER -> analytics.newTracker(PROPERTY_ID)
                else -> analytics.newTracker(R.xml.global_tracker)
            }

            t.enableAdvertisingIdCollection(false)
            mTrackers.put(trackerId, t)
        }
        return mTrackers[trackerId]!!
    }

    enum class TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        if (BuildConfig.DEBUG) {
            registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    Log.d("activityLifecycle", "Activity created:" + activity)
                }

                override fun onActivityStarted(activity: Activity) {
                    Log.d("activityLifecycle", "Activity started:" + activity)
                }

                override fun onActivityResumed(activity: Activity) {
                    Log.d("activityLifecycle", "Activity resumed:" + activity)
                }

                override fun onActivityPaused(activity: Activity) {
                    Log.d("activityLifecycle", "Activity paused:" + activity)
                }

                override fun onActivityStopped(activity: Activity) {
                    Log.d("activityLifecycle", "Activity stopped:" + activity)
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
                    Log.d("activityLifecycle", "Activity saved instance state:" + activity)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    Log.d("activityLifecycle", "Activity destroyed:" + activity)
                }
            })
        }
    }

    companion object {
        private val PROPERTY_ID = "UA-40344870-1"
        private var instance: NovaEvaApp? = null

        private fun loadTypeface(resFile: String): Typeface? {
            Log.d("loadingTypeface", "loading typeface from $resFile")
            try {
                return Typeface.createFromAsset(instance!!.assets, resFile)
            } catch (e: Exception) {
                return null
            }
        }

        //public static final properties
        val bus = RxEventBus()

        val openSansBold: Typeface? by lazy {
            loadTypeface("opensans-bold.ttf")
        }
        val openSansItalic: Typeface? by lazy {
            loadTypeface("opensans-italic.ttf")
        }
        val openSansLight: Typeface? by lazy {
            loadTypeface("opensans-light.ttf")
        }
        val openSansRegular: Typeface? by lazy {
            loadTypeface("opensans-regular.ttf")
        }


        //temp place for common activity navigation

        fun goHome(context: Context) {
            val i = Intent(context, DashboardActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(i)
        }

        fun goSearch(searchString: String, context: Context) {
            val i = Intent(context, SearchActivity::class.java)
            i.putExtra("string", searchString)
            i.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            context.startActivity(i)
        }
    }
}
