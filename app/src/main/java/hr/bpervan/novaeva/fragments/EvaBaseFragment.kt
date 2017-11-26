package hr.bpervan.novaeva.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import com.nostra13.universalimageloader.core.ImageLoader

/**
 * Created by vpriscan on 26.11.17..
 */
open class EvaBaseFragment : Fragment() {
    protected lateinit var prefs: SharedPreferences
        private set

    protected lateinit var imageLoader: ImageLoader
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = context.getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)
        imageLoader = ImageLoader.getInstance()
    }
}