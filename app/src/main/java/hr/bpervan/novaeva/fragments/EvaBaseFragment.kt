package hr.bpervan.novaeva.fragments

import android.content.Context
import android.content.SharedPreferences
import android.support.v4.app.Fragment
import com.nostra13.universalimageloader.core.ImageLoader

/**
 * Created by vpriscan on 26.11.17..
 */
open class EvaBaseFragment : Fragment() {

    protected val prefs: SharedPreferences by lazy {
        context!!.getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)
    }

    protected val imageLoader: ImageLoader by lazy {
        ImageLoader.getInstance()
    }
}