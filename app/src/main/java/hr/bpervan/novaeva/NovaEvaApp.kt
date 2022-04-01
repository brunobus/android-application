package hr.bpervan.novaeva

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.player.EvaPlayer
import hr.bpervan.novaeva.util.ImageLoaderConfigurator
import hr.bpervan.novaeva.util.LifecycleLogger
import hr.bpervan.novaeva.util.NOVA_EVA_PREFS
import io.realm.Realm

/**
 * Created by Branimir on 17.1.2015..
 */
class NovaEvaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        Realm.init(this)

        if (BuildConfig.DEBUG) {
            registerActivityLifecycleCallbacks(LifecycleLogger())
        }
    }

    companion object {
        var instance: NovaEvaApp? = null

        val evaPlayer: EvaPlayer by lazy {
            EvaPlayer(instance!!)
        }

        val imageLoader: ImageLoader by lazy {
            ImageLoader.getInstance().apply {
                init(ImageLoaderConfigurator.createConfiguration(instance!!))
            }
        }

        val prefs: SharedPreferences by lazy {
            instance!!.getSharedPreferences(NOVA_EVA_PREFS, Context.MODE_PRIVATE)
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

        private fun loadTypeface(assetFile: String): Typeface? {
            Log.d("loadingTypeface", "loading typeface from $assetFile")
            return try {
                Typeface.createFromAsset(instance!!.assets, assetFile)
            } catch (e: Exception) {
                null
            }
        }

        val defaultDashboardBackground: Drawable by lazy {
            ContextCompat.getDrawable(NovaEvaApp.instance!!, R.drawable.background_01)!!
        }

        val defaultBreviaryBackground: Drawable by lazy {
            ContextCompat.getDrawable(NovaEvaApp.instance!!, R.drawable.brevijar_backbrevijar)!!
        }
    }
}
