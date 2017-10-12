package hr.bpervan.novaeva.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import kotlinx.android.synthetic.main.activity_brevijar.*
import java.text.SimpleDateFormat
import java.util.*

class BreviaryActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    //private DisplayImageOptions options;
    private lateinit var imageLoader: ImageLoader
    private lateinit var imageLoaderConfigurator: ImageLoaderConfigurator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)

        imageLoaderConfigurator = ImageLoaderConfigurator(this)
        imageLoader = ImageLoader.getInstance()
        if (!imageLoader.isInited) {
            imageLoaderConfigurator.doInit()
        }

        initUI()
    }

    private fun initUI() {
        setContentView(R.layout.activity_brevijar)

        val openSansRegular = NovaEvaApp.openSansRegular
        if (openSansRegular != null) {
            txtKs.typeface = openSansRegular
            txtLaudato.typeface = openSansRegular
        }
        val datum = SimpleDateFormat("dd.MM.yyyy")
        imgDanas.text = datum.format(Date())

        val headerUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null)

        if (headerUrl != null && headerImageBrevijar != null && imageLoader.isInited) {
            imageLoader.displayImage(headerUrl, headerImageBrevijar, imageLoaderConfigurator.doConfig(true))
        }

        btnJucerJutarnja.setOnClickListener(BreviaryClickListener(1))
        btnJucerVecernja.setOnClickListener(BreviaryClickListener(2))
        btnJucerPovecerje.setOnClickListener(BreviaryClickListener(3))

        btnDanasJutarnja.setOnClickListener(BreviaryClickListener(4))
        btnDanasVecernja.setOnClickListener(BreviaryClickListener(5))
        btnDanasPovecerje.setOnClickListener(BreviaryClickListener(6))

        btnSutraJutarnja.setOnClickListener(BreviaryClickListener(7))
        btnSutraVecernja.setOnClickListener(BreviaryClickListener(8))
        btnSutraPovecerje.setOnClickListener(BreviaryClickListener(9))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        initUI()
    }

    inner class BreviaryClickListener(private val breviaryId: Int) : OnClickListener {

        override fun onClick(v: View?) {
            val intent = Intent(this@BreviaryActivity, BreviaryContentActivity::class.java)
            intent.putExtra("BREV_CAT", breviaryId)
            this@BreviaryActivity.startActivity(intent)
        }
    }
}
