package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.LocalCategory
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ConnectionChecker
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_dashboard.*

//// TODO: 13.10.17. refactor me
class DashboardActivity : EvaBaseActivity(), OnTouchListener, OnClickListener {
    private val syncInterval = 90000L

    private var fetchBreviaryImageDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*switch from SplashTheme to DashboardTheme*/
        setTheme(R.style.DashboardTheme)

        setContentView(R.layout.activity_dashboard)

        fetchBreviaryImage()

        /*mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-40344870-1");*/

        val mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.setScreenName("Dashboard")
        mGaTracker.send(HitBuilders.AppViewBuilder().build())

        NovaEvaApp.openSansRegular?.let { titleLineTitle.typeface = it }

        sequenceOf(btnBrevijar, btnMolitvenik, btnIzreke, btnMp3, btnAktualno, btnPoziv,
                btnOdgovori, btnMultimedia, btnPropovjedi, btnDuhovnost,
                btnEvandjelje, btnInfo, btnBookmarks)
                .forEach {
                    it.setOnTouchListener(this)
                    it.setOnClickListener(this)
                }
    }

    public override fun onResume() {
        super.onResume()

        val vrijemeZadnjeSync = prefs.getLong("vrijemeZadnjeSinkronizacije", 0L)
        if (System.currentTimeMillis() - vrijemeZadnjeSync > syncInterval) {
            if (ConnectionChecker.hasConnection(this)) {
                NovaEvaService.instance
                        .getNewStuff()
                        .subscribeAsync({ indicators ->

                            checkLastNid(EvaCategory.DUHOVNOST, indicators.duhovnost)
                            checkLastNid(EvaCategory.AKTUALNO, indicators.aktualno)
                            checkLastNid(EvaCategory.IZREKE, indicators.izreke)
                            checkLastNid(EvaCategory.MULTIMEDIJA, indicators.multimedija)
                            checkLastNid(EvaCategory.EVANDJELJE, indicators.evandjelje)
                            checkLastNid(EvaCategory.PROPOVIJEDI, indicators.propovijedi)
                            checkLastNid(EvaCategory.POZIV, indicators.poziv)
                            checkLastNid(EvaCategory.ODGOVORI, indicators.odgovori)
                            checkLastNid(EvaCategory.PJESMARICA, indicators.pjesmarica)

                            testAndSetRedDots()
                            prefs.edit().putLong("vrijemeZadnjeSinkronizacije", System.currentTimeMillis()).apply()
                        }) { t ->
                            Log.e("evaSyncError", t.message, t)
                        }
            }
        }

        testAndSetRedDots()
    }

    private fun checkLastNid(kategorija: EvaCategory, dobiveniZadnjiNid: Int?) {
        if (dobiveniZadnjiNid == null) return

        val kategorijaStr = kategorija.id.toString()
        val spremljeniZadnjiNid = prefs.getInt(kategorijaStr, 0)
        if (spremljeniZadnjiNid != dobiveniZadnjiNid) {
            prefs.edit().putInt(kategorijaStr, dobiveniZadnjiNid).apply()
            prefs.edit().putInt("vidjenoKategorija" + kategorijaStr, 0).apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        fetchBreviaryImageDisposable?.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSearch -> {
                if (ConnectionChecker.hasConnection(this))
                    showSearchPopup()
                else
                    Toast.makeText(this@DashboardActivity, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun fetchBreviaryImage() {
        fetchBreviaryImageDisposable?.dispose()

        fetchBreviaryImageDisposable = NovaEvaService.instance
                .getDirectoryContent(546, null)
                .subscribeAsync({ directoryContent ->
                    if (directoryContent.image != null) {
                        prefs.edit().putString("hr.bpervan.novaeva.brevijarheaderimage", directoryContent.image.size640).apply()
                    }
                }) {
                    Toast.makeText(this@DashboardActivity, "Failed to connect", Toast.LENGTH_SHORT).show()
                }
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")

        val et = EditText(this)
        searchBuilder.setView(et)

        searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this)
        }

        searchBuilder.setNegativeButton("Odustani") { _, _ -> }
        searchBuilder.show()
    }

    //TODO: sredi ovo pod hitno
    private fun testAndSetRedDots() {
        if (prefs.getInt("vidjenoKategorija1", 0) == 0) {
            btnIzreke.setBackgroundResource(R.drawable.button_izreke_news)
        } else {
            btnIzreke.setBackgroundResource(R.drawable.button_izreke)
        }
        if (prefs.getInt("vidjenoKategorija4", 0) == 0) {
            btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje_news)
        } else {
            btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje)
        }
        if (prefs.getInt("vidjenoKategorija7", 0) == 0) {
            btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi_news)
        } else {
            btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi)
        }
        if (prefs.getInt("vidjenoKategorija10", 0) == 0) {
            btnMultimedia.setBackgroundResource(R.drawable.button_multimedia_news)
        } else {
            btnMultimedia.setBackgroundResource(R.drawable.button_multimedia)
        }
        if (prefs.getInt("vidjenoKategorija11", 0) == 0) {
            btnOdgovori.setBackgroundResource(R.drawable.button_odgovori_news)
        } else {
            btnOdgovori.setBackgroundResource(R.drawable.button_odgovori)
        }
        if (prefs.getInt("vidjenoKategorija9", 0) == 0) {
            btnAktualno.setBackgroundResource(R.drawable.button_aktualno_news)
        } else {
            btnAktualno.setBackgroundResource(R.drawable.button_aktualno)
        }
        if (prefs.getInt("vidjenoKategorija355", 0) == 0) {
            btnMp3.setBackgroundResource(R.drawable.button_mp3_news)
        } else {
            btnMp3.setBackgroundResource(R.drawable.button_mp3)
        }
        if (prefs.getInt("vidjenoKategorija8", 0) == 0) {
            btnPoziv.setBackgroundResource(R.drawable.button_poziv_news)
        } else {
            btnPoziv.setBackgroundResource(R.drawable.button_poziv)
        }
        if (prefs.getInt("vidjenoKategorija354", 0) == 0) {
            btnDuhovnost.setBackgroundResource(R.drawable.button_duhovnost_news)
        } else {
            btnDuhovnost.setBackgroundResource(R.drawable.button_duhovnost)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_dashboard, menu)
        return true
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.btnMolitvenik -> startActivity(Intent(this, MolitvenikActivity::class.java))
            R.id.btnInfo -> startActivity(Intent(this, InfoActivity::class.java))
            R.id.btnBookmarks -> startActivity(Intent(this, BookmarksActivity::class.java))
            R.id.btnBrevijar -> startActivity(Intent(this, BreviaryActivity::class.java))
            R.id.btnEvandjelje -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.EVANDJELJE.id)
                putExtra("categoryName", EvaCategory.EVANDJELJE.rawName)
                putExtra("themeId", R.style.EvandjeljeTheme)
            })
            R.id.btnMp3 -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.PJESMARICA.id)
                putExtra("categoryName", EvaCategory.PJESMARICA.rawName)
                putExtra("themeId", R.style.PjesmaricaTheme)
            })
            R.id.btnPropovjedi -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.PROPOVIJEDI.id)
                putExtra("categoryName", EvaCategory.PROPOVIJEDI.rawName)
                putExtra("themeId", R.style.PropovjediTheme)
            })
            R.id.btnOdgovori -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.ODGOVORI.id)
                putExtra("categoryName", EvaCategory.ODGOVORI.rawName)
                putExtra("themeId", R.style.OdgovoriTheme)
            })
            R.id.btnPoziv -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.POZIV.id)
                putExtra("categoryName", EvaCategory.POZIV.rawName)
                putExtra("themeId", R.style.PozivTheme)
            })
            R.id.btnDuhovnost -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.DUHOVNOST.id)
                putExtra("categoryName", EvaCategory.DUHOVNOST.rawName)
                putExtra("themeId", R.style.DuhovnostTheme)
            })
            R.id.btnIzreke -> startActivity(Intent(this, IzrekeActivity::class.java).apply {
                putExtra("themeId", R.style.IzrekeTheme)
            })
            R.id.btnMultimedia -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.MULTIMEDIJA.id)
                putExtra("categoryName", EvaCategory.MULTIMEDIJA.rawName)
                putExtra("themeId", R.style.MultimedijaTheme)
            })
            R.id.btnAktualno -> startActivity(Intent(this, ListaVijestiActivity::class.java).apply {
                putExtra("categoryId", EvaCategory.AKTUALNO.id)
                putExtra("categoryName", EvaCategory.AKTUALNO.rawName)
                putExtra("themeId", R.style.AktualnoTheme)
            })
        }
    }

    //na drugim mobovima treba return true, zato nestane natpis odmah!!!
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var text: String? = null

        text = if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getString(R.string.app_name)
        } else {
            getString(R.string.app_name_vertical)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (v.id) {
                    R.id.btnBrevijar -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                LocalCategory.BREVIJAR.rawName
                            else {
                                LocalCategory.BREVIJAR.rawNameVertical
                            }
                    R.id.btnMolitvenik -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                LocalCategory.MOLITVENIK.rawName
                            else {
                                LocalCategory.MOLITVENIK.rawNameVertical
                            }
                    R.id.btnEvandjelje -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.EVANDJELJE.rawName
                            else {
                                EvaCategory.EVANDJELJE.rawNameVertical
                            }
                    R.id.btnMp3 -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.PJESMARICA.rawName
                            else {
                                EvaCategory.PJESMARICA.rawNameVertical
                            }
                    R.id.btnPropovjedi -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.PROPOVIJEDI.rawName
                            else {
                                EvaCategory.PROPOVIJEDI.rawNameVertical
                            }
                    R.id.btnOdgovori -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.ODGOVORI.rawName
                            else {
                                EvaCategory.ODGOVORI.rawNameVertical
                            }
                    R.id.btnPoziv -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.POZIV.rawName
                            else {
                                EvaCategory.POZIV.rawNameVertical
                            }
                    R.id.btnDuhovnost -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.DUHOVNOST.rawName
                            else {
                                EvaCategory.DUHOVNOST.rawNameVertical
                            }
                    R.id.btnIzreke -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.IZREKE.rawName
                            else {
                                EvaCategory.IZREKE.rawNameVertical
                            }
                    R.id.btnMultimedia -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.MULTIMEDIJA.rawName
                            else {
                                EvaCategory.MULTIMEDIJA.rawNameVertical
                            }
                    R.id.btnAktualno -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                EvaCategory.AKTUALNO.rawName
                            else {
                                EvaCategory.AKTUALNO.rawNameVertical
                            }
                    R.id.btnInfo -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                LocalCategory.INFO.rawName
                            else {
                                LocalCategory.INFO.rawNameVertical
                            }
                    R.id.btnBookmarks -> text =
                            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                                LocalCategory.BOOKMARKS.rawName
                            else {
                                LocalCategory.BOOKMARKS.rawNameVertical
                            }
                }
                titleLineTitle.text = text
            }
        //return true;
            MotionEvent.ACTION_UP -> titleLineTitle.text = text
        }//return true;

        return false
    }
}