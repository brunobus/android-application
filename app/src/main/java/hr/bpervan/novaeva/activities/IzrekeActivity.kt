package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ConnectionChecker
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_izreke.*
import kotlinx.android.synthetic.main.izreke_fake_action_bar.view.*

//import com.google.analytics.tracking.android.EasyTracker;

class IzrekeActivity : EvaBaseActivity(), OnClickListener {

    private var contentTitle: String? = null
    private var contentData: String? = null
    private var contentId: Long = -1

    private var randomIzrekaDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs.edit().putInt("vidjenoKategorija1", 1).apply()

        val mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)

        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Kategorije")
                        .setAction("OtvorenaKategorija")
                        .setLabel(EvaCategory.IZREKE.rawName)
                        .build()
        )

        initUI()

        loadRandomIzreka()
    }

    private fun loadRandomIzreka() {
        randomIzrekaDisposable?.dispose()

        if (ConnectionChecker.hasConnection(this)) {

            randomIzrekaDisposable = NovaEvaService.instance
                    .getRandomDirectoryContent(1)
                    .subscribeAsync({ directoryContent ->
                        if (directoryContent.contentMetadataList != null && directoryContent.contentMetadataList.isNotEmpty()) {
                            val contentInfo = directoryContent.contentMetadataList[0]

                            this.contentTitle = contentInfo.title
                            this.contentData = contentInfo.text
                            this.contentId = contentInfo.contentId

                            tvNaslov.text = title
                            webText.loadDataWithBaseURL(null, contentData, "text/html", "UTF-8", "")
                        }
                    }) {
                        NovaEvaApp.showErrorPopupDialog(it, this) { loadRandomIzreka() }
                    }
        } else {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initUI() {
        setContentView(R.layout.activity_izreke)

        if (contentTitle != null && contentData != null) {
            tvNaslov.text = contentTitle
            webText.loadDataWithBaseURL(null, contentData, "text/html", "UTF-8", "")
        }

        webText.settings.defaultTextEncodingName = "utf-8"

        tvKategorija.text = "Izreke"

        btnTextPlus.setOnClickListener(this)
        btnObnovi.setOnClickListener(this)

        fakeActionBar.btnHome.setOnClickListener(this)
        fakeActionBar.btnShare.setOnClickListener(this)
        fakeActionBar.btnMail.setOnClickListener(this)
        fakeActionBar.btnSearch.setOnClickListener(this)
        fakeActionBar.btnBack.setOnClickListener(this)

        webText.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)

        window.decorView.setBackgroundResource(android.R.color.background_light)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this?.clearFindViewByIdCache() // due to a bug in viewbinding library you must use null safe access!!
        initUI()
    }

    override fun onDestroy() {
        super.onDestroy()

        randomIzrekaDisposable?.dispose()
    }

    override fun onClick(v: View) {
        val vId = v.id
        if (vId == R.id.btnObnovi) {
            loadRandomIzreka()

        } else if (vId == R.id.btnHome) {
            NovaEvaApp.goHome(this)

        } else if (vId == R.id.btnSearch) {
            if (ConnectionChecker.hasConnection(this))
                showSearchPopup()

        } else if (vId == R.id.btnBookmark) {
        } else if (vId == R.id.btnShare) {
            val temp = "http://novaeva.com/node/" + contentId
            val faceIntent = Intent(Intent.ACTION_SEND)
            faceIntent.type = "text/plain"
            faceIntent.putExtra(Intent.EXTRA_TEXT, temp)
            startActivity(Intent.createChooser(faceIntent, "Facebook"))

        } else if (vId == R.id.btnMail) {
            val temp2 = "http://novaeva.com/node/" + contentId
            val mailIntent = Intent(Intent.ACTION_SEND)
            mailIntent.type = "message/rfc822" //ovo ispipati još malo
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, contentTitle)
            mailIntent.putExtra(Intent.EXTRA_TEXT, temp2)
            startActivity(Intent.createChooser(mailIntent, "Odaberite aplikaciju"))

        } else if (vId == R.id.btnTextPlus) {//showTextSizePopup();
            var mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
            mCurrentSize += 2
            if (mCurrentSize >= 28) {
                mCurrentSize = 12
            }

            prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).apply()
            webText.settings.defaultFontSize = mCurrentSize

        } else if (vId == R.id.btnBack) {
            onBackPressed()

        }

    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")

        val et = EditText(this)
        searchBuilder.setView(et)

        searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this@IzrekeActivity)
        }

        searchBuilder.setNegativeButton("Odustani") { dialog, whichButton -> }
        searchBuilder.show()
    }
}
