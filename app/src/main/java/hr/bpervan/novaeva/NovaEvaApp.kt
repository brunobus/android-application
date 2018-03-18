package hr.bpervan.novaeva

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaTheme
import hr.bpervan.novaeva.player.MyExoPlayerFactory
import hr.bpervan.novaeva.receivers.ConnectionDetector
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import hr.bpervan.novaeva.utilities.LifecycleLogger
import hr.bpervan.novaeva.utilities.TransitionAnimation
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

        activeExoPlayer = MyExoPlayerFactory.createDefaultExoPlayer(this)
        preparedExoPlayer = MyExoPlayerFactory.createDefaultExoPlayer(this)

        RxEventBus.didSetActiveExoPlayer.onNext(activeExoPlayer)

        RxEventBus.setActiveExoPlayer
                .observeOn(AndroidSchedulers.mainThread())
                .filter({ it != activeExoPlayer })
                .subscribe {
                    activeExoPlayer.playWhenReady = false
                    activeExoPlayer.stop()
                    preparedExoPlayer = activeExoPlayer
                    activeExoPlayer = it
                    RxEventBus.didSetActiveExoPlayer.onNext(it)
                }

        RxEventBus.changeEvaTheme
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    prefs.edit().putString(evaThemeKey, it.toString()).apply()
                }

        val filter = IntentFilter()
        filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE")
        filter.addAction("android.net.wifi.STATE_CHANGE")
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(ConnectionDetector(), filter)

        registerActivityLifecycleCallbacks(LifecycleLogger())
    }

    companion object {
        private const val evaThemeKey = "EVA_THEME"
        private const val NOVA_EVA_TRACKER_ID = "UA-40344870-1"
        var instance: NovaEvaApp? = null

        var activeAudioTrackUri: String? = null
        lateinit var activeExoPlayer: ExoPlayer
        lateinit var preparedExoPlayer: ExoPlayer
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

        //public static final properties

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

        fun showFetchErrorSnackbar(throwable: Throwable?, context: Context, holderView: View?) {
            if (throwable != null) Log.e("evaError", throwable.message, throwable)

            holderView?.let { Snackbar.make(it, context.getString(R.string.error_fetching_data), Snackbar.LENGTH_LONG).show() }
        }

        fun showNetworkUnavailableToast(context: Context) {
            Toast.makeText(context, context.getString(R.string.network_unavailable), Toast.LENGTH_LONG).show()
        }

        inline fun showFetchErrorDialog(throwable: Throwable?, context: Activity, crossinline onTryAgain: () -> Unit) {
            if (throwable != null) Log.e("evaError", throwable.message, throwable)

            val error = AlertDialog.Builder(context)
            error.setTitle(context.getString(R.string.error))

            val tv = TextView(context)
            tv.text = context.getString(R.string.error_fetching_data)

            NovaEvaApp.openSansRegular?.let { tv.typeface = it }

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            error.setView(tv)

            error.setPositiveButton(context.getString(R.string.try_again)) { _, _ -> onTryAgain() }
            error.setNegativeButton(context.getString(R.string.go_back)) { _, _ -> RxEventBus.goHome.onNext(TransitionAnimation.RIGHTWARDS) }
            error.show()
        }

        fun getDefaultAppBackground(): Drawable {
            return ContextCompat.getDrawable(NovaEvaApp.instance!!, R.drawable.background)!!
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
