package hr.bpervan.novaeva

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Logger
import com.google.android.gms.analytics.Tracker
import hr.bpervan.novaeva.activities.DashboardActivity
import hr.bpervan.novaeva.activities.SearchActivity

import java.util.HashMap

import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import hr.bpervan.novaeva.utilities.LifecycleLogger
import io.realm.Realm

/**
 * Created by Branimir on 17.1.2015..
 */
class NovaEvaApp : Application() {
    private var trackers: MutableMap<TrackerName, Tracker> = HashMap()

    @Synchronized
    fun getTracker(trackerId: TrackerName): Tracker {
        if (!trackers.containsKey(trackerId)) {
            val analytics = GoogleAnalytics.getInstance(this)
            analytics.logger.logLevel = Logger.LogLevel.VERBOSE

            val t: Tracker = when (trackerId) {
                NovaEvaApp.TrackerName.APP_TRACKER -> analytics.newTracker(PROPERTY_ID)
                else -> analytics.newTracker(R.xml.global_tracker)
            }

            t.enableAdvertisingIdCollection(false)
            trackers.put(trackerId, t)
        }
        return trackers[trackerId]!!
    }

    enum class TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER
    }

    override fun onCreate() {
        super.onCreate()

        instance = this


        if (BuildConfig.DEBUG) {
            registerActivityLifecycleCallbacks(LifecycleLogger())
        }

        Realm.init(this)
        ImageLoaderConfigurator.doInit(this)
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
            i.putExtra("searchString", searchString)
            i.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            context.startActivity(i)
        }
    }
}