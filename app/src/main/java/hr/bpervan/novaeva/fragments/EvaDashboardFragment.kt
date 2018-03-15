package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ConnectionChecker
import hr.bpervan.novaeva.utilities.TransitionAnimation
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_dashboard.*

/**
 * todo refactor
 */
@Deprecated("Will soon be replaced with new dashboard")
class EvaDashboardFragment : EvaBaseFragment(), View.OnClickListener {
    private val syncInterval = 90000L

    private var fetchBreviaryImageDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    companion object : EvaFragmentFactory<EvaDashboardFragment, Unit> {
        override fun newInstance(initializer: Unit): EvaDashboardFragment {
            return EvaDashboardFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("PocetniEkran")
                        .setAction("OtvorenPocetniEkran")
                        .build())

        fetchBreviaryImage()
    }

    //todo reimplement in BreviaryChooserFragment
    private fun fetchBreviaryImage() {

        fetchBreviaryImageDisposable = NovaEvaService.instance
                .getDirectoryContent(546, null)
                .subscribeAsync({ directoryContent ->
                    if (directoryContent.image != null) {
                        prefs.edit().putString("hr.bpervan.novaeva.brevijarheaderimage", directoryContent.image.size640).apply()
                    }
                }) {
                    context?.let {
                        NovaEvaApp.showNetworkUnavailableToast(it)
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.DashboardTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.activity_dashboard, container, false).apply {
            setBackgroundResource(R.drawable.background)

            savedInstanceState ?: RxEventBus.replaceAppBackground.onNext(
                    BackgroundReplaceEvent(R.color.WhiteSmoke, BackgroundType.COLOR))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NovaEvaApp.openSansRegular?.let { titleLineTitle.typeface = it }

        sequenceOf(btnBrevijar, btnMolitvenik, btnIzreke, btnMp3, btnAktualno, btnPoziv,
                btnOdgovori, btnMultimedia, btnPropovjedi, btnDuhovnost,
                btnEvandjelje, btnInfo, btnBookmarks)
                .forEach {
                    //                    it.setOnTouchListener(this)
                    it.setOnClickListener(this)
                }
    }

    //TODO: sredi ovo pod hitno
    private fun testAndSetRedDots() {
        view ?: return

        if (prefs.getBoolean("newContentInCategory1", false)) {
            btnIzreke.setBackgroundResource(R.drawable.button_izreke_news)
        } else {
            btnIzreke.setBackgroundResource(R.drawable.button_izreke)
        }
        if (prefs.getBoolean("newContentInCategory4", false)) {
            btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje_news)
        } else {
            btnEvandjelje.setBackgroundResource(R.drawable.button_evandjelje)
        }
        if (prefs.getBoolean("newContentInCategory7", false)) {
            btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi_news)
        } else {
            btnPropovjedi.setBackgroundResource(R.drawable.button_propovjedi)
        }
        if (prefs.getBoolean("newContentInCategory10", false)) {
            btnMultimedia.setBackgroundResource(R.drawable.button_multimedia_news)
        } else {
            btnMultimedia.setBackgroundResource(R.drawable.button_multimedia)
        }
        if (prefs.getBoolean("newContentInCategory11", false)) {
            btnOdgovori.setBackgroundResource(R.drawable.button_odgovori_news)
        } else {
            btnOdgovori.setBackgroundResource(R.drawable.button_odgovori)
        }
        if (prefs.getBoolean("newContentInCategory9", false)) {
            btnAktualno.setBackgroundResource(R.drawable.button_aktualno_news)
        } else {
            btnAktualno.setBackgroundResource(R.drawable.button_aktualno)
        }
        if (prefs.getBoolean("newContentInCategory355", false)) {
            btnMp3.setBackgroundResource(R.drawable.button_mp3_news)
        } else {
            btnMp3.setBackgroundResource(R.drawable.button_mp3)
        }
        if (prefs.getBoolean("newContentInCategory8", false)) {
            btnPoziv.setBackgroundResource(R.drawable.button_poziv_news)
        } else {
            btnPoziv.setBackgroundResource(R.drawable.button_poziv)
        }
        if (prefs.getBoolean("newContentInCategory354", false)) {
            btnDuhovnost.setBackgroundResource(R.drawable.button_duhovnost_news)
        } else {
            btnDuhovnost.setBackgroundResource(R.drawable.button_duhovnost)
        }
    }

    override fun onResume() {
        super.onResume()

        context?.let { context ->
            val vrijemeZadnjeSync = prefs.getLong("vrijemeZadnjeSinkronizacije", 0L)
            if (System.currentTimeMillis() - vrijemeZadnjeSync > syncInterval) {
                if (ConnectionChecker.hasConnection(context)) {
                    NovaEvaService.instance
                            .getNewStuff()
                            .subscribeAsync({ indicators ->
                                view ?: return@subscribeAsync

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
        }
        testAndSetRedDots()
    }

    private fun checkLastNid(kategorija: EvaCategory, receivedLatestContentId: Int?) {
        if (receivedLatestContentId == null) return

        val categoryId = kategorija.id.toString()
        val savedLatestContentId = prefs.getInt(categoryId, 0)
        if (savedLatestContentId != receivedLatestContentId) {

            prefs.edit()
                    .putInt(categoryId, receivedLatestContentId)
                    .putBoolean("newContentInCategory$categoryId", true)
                    .apply()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuSearch -> {
                showSearchPopup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.activity_dashboard, menu)
    }

    private fun showSearchPopup() {
        activity?.let { activity ->

            val searchBuilder = AlertDialog.Builder(activity)
            searchBuilder.setTitle("Pretraga")

            val et = EditText(activity)
            searchBuilder.setView(et)

            searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
                val searchString = et.text.toString()
                RxEventBus.search.onNext(searchString)
            }

            searchBuilder.setNegativeButton("Odustani") { _, _ -> }
            searchBuilder.show()
        }
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.btnMolitvenik -> RxEventBus.openPrayerBook.onNext(TransitionAnimation.NONE)
            R.id.btnInfo -> RxEventBus.openInfo.onNext(TransitionAnimation.NONE)
            R.id.btnBookmarks -> RxEventBus.openBookmarks.onNext(TransitionAnimation.NONE)
            R.id.btnBrevijar -> RxEventBus.openBreviaryChooser.onNext(TransitionAnimation.NONE)
            R.id.btnIzreke -> RxEventBus.openQuotes.onNext(OpenQuotesEvent())
            R.id.btnEvandjelje -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.EVANDJELJE.id.toLong(),
                                    EvaCategory.EVANDJELJE.rawName),
                            R.style.EvandjeljeTheme))

            R.id.btnMp3 -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.PJESMARICA.id.toLong(),
                                    EvaCategory.PJESMARICA.rawName),
                            R.style.PjesmaricaTheme))

            R.id.btnPropovjedi -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.PROPOVIJEDI.id.toLong(),
                                    EvaCategory.PROPOVIJEDI.rawName),
                            R.style.PropovjediTheme))

            R.id.btnOdgovori -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.ODGOVORI.id.toLong(),
                                    EvaCategory.ODGOVORI.rawName),
                            R.style.OdgovoriTheme))

            R.id.btnPoziv -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.POZIV.id.toLong(),
                                    EvaCategory.POZIV.rawName),
                            R.style.PozivTheme))

            R.id.btnDuhovnost -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.DUHOVNOST.id.toLong(),
                                    EvaCategory.DUHOVNOST.rawName),
                            R.style.DuhovnostTheme))

            R.id.btnMultimedia -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.MULTIMEDIJA.id.toLong(),
                                    EvaCategory.MULTIMEDIJA.rawName),
                            R.style.MultimedijaTheme))

            R.id.btnAktualno -> RxEventBus.openDirectory.onNext(
                    OpenDirectoryEvent(
                            EvaDirectoryMetadata(EvaCategory.AKTUALNO.id.toLong(),
                                    EvaCategory.AKTUALNO.rawName),
                            R.style.AktualnoTheme))
        }
    }
}