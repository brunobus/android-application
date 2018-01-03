package hr.bpervan.novaeva.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import kotlinx.android.synthetic.main.activity_breviary.*
import kotlinx.android.synthetic.main.collapsing_breviary_header.*
import java.text.SimpleDateFormat
import java.util.*

class BreviaryActivity : EvaBaseActivity() {

    //private DisplayImageOptions options;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breviary)

        initUI()
    }

    private fun initUI() {

        val openSansRegular = NovaEvaApp.openSansRegular
        if (openSansRegular != null) {
            txtKs.typeface = openSansRegular
            txtLaudato.typeface = openSansRegular
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("hr", "HR"))
        imgDanas.text = dateFormat.format(Date())

        val headerUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null)

        if (headerUrl != null && breviaryCoverImage != null) {
            imageLoader.displayImage(headerUrl, breviaryCoverImage, ImageLoaderConfigurator.createDefaultDisplayImageOptions(true))
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

    inner class BreviaryClickListener(private val breviaryId: Int) : OnClickListener {

        override fun onClick(v: View?) {
            val intent = Intent(this@BreviaryActivity, BreviaryContentActivity::class.java)
            intent.putExtra(BreviaryContentActivity.BREVIARY_ID_KEY, breviaryId)
            this@BreviaryActivity.startActivity(intent)
        }
    }
}
