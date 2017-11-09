package hr.bpervan.novaeva.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
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
import kotlinx.android.synthetic.main.eva_collapsing_bar.view.*
import kotlinx.android.synthetic.main.izreke_fake_action_bar.view.*

//import com.google.analytics.tracking.android.EasyTracker;

class IzrekeActivity : EvaBaseActivity() {

    private var themeId: Int = 0

    private var contentTitle: String? = null
    private var contentData: String? = null
    private var contentId: Long = -1

    private var randomIzrekaDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs.edit().putInt("vidjenoKategorija1", 1).apply()

        val inState = savedInstanceState ?: intent.extras
        themeId = inState.getInt("themeId", -1)

        if (themeId != -1) {
            setTheme(themeId)
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("themeId", themeId)
        super.onSaveInstanceState(outState)
    }

    private fun loadRandomIzreka() {
        randomIzrekaDisposable?.dispose()

        if (ConnectionChecker.hasConnection(this)) {

            randomIzrekaDisposable = NovaEvaService.instance
                    .getRandomDirectoryContent(1)
                    .subscribeAsync({ directoryContent ->
                        if (directoryContent.contentMetadataList != null && directoryContent.contentMetadataList.isNotEmpty()) {
                            val contentInfo = directoryContent.contentMetadataList[0]

                            contentTitle = contentInfo.title
                            contentData = contentInfo.text
                            contentId = contentInfo.contentId

                            evaCollapsingBar.collapsingToolbar.title = contentTitle
                            webText.loadDataWithBaseURL(null, contentData, "text/html", "utf-8", "")
                        }
                    }) {
                        NovaEvaApp.showFetchErrorDialog(it, this) { loadRandomIzreka() }
                    }
        } else {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initUI() {
        setContentView(R.layout.activity_izreke)

        if (contentTitle != null && contentData != null) {
            evaCollapsingBar.collapsingToolbar.title = contentTitle
            webText.loadDataWithBaseURL(null, contentData, "text/html", "utf-8", "")
        }

        btnObnovi.setOnClickListener {
            loadRandomIzreka()
        }

        fakeActionBar.btnShare.setOnClickListener {
            val temp = "http://novaeva.com/node/" + contentId
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, temp)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share)))
        }
        fakeActionBar.btnMail.setOnClickListener {
            val temp2 = "http://novaeva.com/node/" + contentId
            val mailIntent = Intent(Intent.ACTION_SEND)
            mailIntent.type = "message/rfc822" //ovo ispipati još malo
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, contentTitle)
            mailIntent.putExtra(Intent.EXTRA_TEXT, temp2)
            startActivity(Intent.createChooser(mailIntent, getString(R.string.title_share_mail)))
        }

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

//    override fun onClick(v: View) {
//        val vId = v.id
//        when (vId) {
//            R.id.btnTextPlus -> {//showTextSizePopup();
//                var mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
//                mCurrentSize += 2
//                if (mCurrentSize >= 28) {
//                    mCurrentSize = 12
//                }
//
//                prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).apply()
//                webText.settings.defaultFontSize = mCurrentSize
//
//            }
//        }
//    }
}
