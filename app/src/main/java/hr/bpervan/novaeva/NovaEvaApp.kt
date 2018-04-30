package hr.bpervan.novaeva

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.util.EvaTheme
import hr.bpervan.novaeva.player.EvaPlayer
import hr.bpervan.novaeva.receivers.ConnectionDetector
import hr.bpervan.novaeva.util.ImageLoaderConfigurator
import hr.bpervan.novaeva.util.LifecycleLogger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm

/**
 * Created by Branimir on 17.1.2015..
 */
class NovaEvaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        Realm.init(this)
        ImageLoaderConfigurator.doInit(this)

        val analytics = GoogleAnalytics.getInstance(this).apply {
            setDryRun(BuildConfig.DEBUG)
        }

        defaultTracker = analytics.newTracker(NOVA_EVA_TRACKER_ID).apply {
            enableAdvertisingIdCollection(false)
        }

        EventPipelines.evaTheme
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    prefs.edit {
                        putString(evaThemeKey, it.toString())
                    }
                }

        val filter = IntentFilter()
        filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE")
        filter.addAction("android.net.wifi.STATE_CHANGE")
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(ConnectionDetector(), filter)

        if (BuildConfig.DEBUG) {
            registerActivityLifecycleCallbacks(LifecycleLogger())
        }
    }

    companion object {
        private const val evaThemeKey = "EVA_THEME"
        private const val NOVA_EVA_TRACKER_ID = "UA-40344870-1"
        var instance: NovaEvaApp? = null

        val evaPlayer: EvaPlayer by lazy {
            EvaPlayer(instance!!)
        }

        lateinit var defaultTracker: Tracker

        val imageLoader: ImageLoader by lazy {
            ImageLoader.getInstance()
        }

        val prefs: SharedPreferences by lazy {
            instance!!.getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)
        }

        private fun loadTypeface(resFile: String): Typeface? {
            Log.d("loadingTypeface", "loading typeface from $resFile")
            return try {
                Typeface.createFromAsset(instance!!.assets, resFile)
            } catch (e: Exception) {
                null
            }
        }

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

        val defaultDashboardBackground: Drawable by lazy {
            ContextCompat.getDrawable(NovaEvaApp.instance!!, R.drawable.background_01)!!
        }

        val defaultBreviaryBackground: Drawable by lazy {
            ContextCompat.getDrawable(NovaEvaApp.instance!!, R.drawable.brevijar_backbrevijar)!!
        }

        fun getDefaultEvaTheme(): EvaTheme {
            return try {
                val evaThemeString = prefs.getString(evaThemeKey, EvaTheme.DEFAULT.toString())
                EvaTheme.valueOf(evaThemeString)
            } catch (iae: IllegalArgumentException) {
                EvaTheme.DEFAULT
            }
        }
    }
}
