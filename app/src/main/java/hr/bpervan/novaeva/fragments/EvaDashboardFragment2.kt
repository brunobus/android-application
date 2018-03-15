package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.TransitionAnimation
import hr.bpervan.novaeva.utilities.setBackground
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.eva_dashboard_v2.*

/**
 *
 */
class EvaDashboardFragment2 : EvaBaseFragment() {
    companion object : EvaFragmentFactory<EvaDashboardFragment2, Unit> {

        override fun newInstance(initializer: Unit): EvaDashboardFragment2 {
            return EvaDashboardFragment2()
        }
    }

    private var dashboardBackgroundDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("PocetniEkran")
                        .setAction("OtvorenPocetniEkran")
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.DashboardTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.eva_dashboard_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()

        dashboardBackgroundDisposable = RxEventBus.dashboardBackground
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    view.setBackground(it.backgroundType, it.resId)
                    savedInstanceState ?: RxEventBus.appBackground.onNext(
                            BackgroundReplaceEvent(R.color.WhiteSmoke, BackgroundType.COLOR))
                }
    }

    private fun initUI() {
        NovaEvaApp.openSansRegular?.let { titleLineTitle.typeface = it }

        btnBrevijar.setOnClickListener {
            RxEventBus.openBreviaryChooser.onNext(TransitionAnimation.FADE)
        }
        btnMolitvenik.setOnClickListener {
            RxEventBus.openPrayerBook.onNext(TransitionAnimation.FADE)
        }
        btnInfo.setOnClickListener {
            RxEventBus.openInfo.onNext(TransitionAnimation.FADE)
        }
        btnBookmarks.setOnClickListener {
            RxEventBus.openBookmarks.onNext(TransitionAnimation.FADE)
        }
        btnIzreke.setOnClickListener {
            RxEventBus.openQuotes.onNext(OpenQuotesEvent())
        }
        btnMp3.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.PJESMARICA.id.toLong(),
                            EvaCategory.PJESMARICA.rawName),
                    R.style.PjesmaricaTheme))
        }
        btnAktualno.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.AKTUALNO.id.toLong(),
                            EvaCategory.AKTUALNO.rawName),
                    R.style.AktualnoTheme))
        }
        btnPoziv.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.POZIV.id.toLong(),
                            EvaCategory.POZIV.rawName),
                    R.style.PozivTheme))
        }
        btnOdgovori.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.ODGOVORI.id.toLong(),
                            EvaCategory.ODGOVORI.rawName),
                    R.style.OdgovoriTheme))
        }
        btnMultimedia.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.MULTIMEDIJA.id.toLong(),
                            EvaCategory.MULTIMEDIJA.rawName),
                    R.style.MultimedijaTheme))
        }
        btnPropovjedi.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.PROPOVIJEDI.id.toLong(),
                            EvaCategory.PROPOVIJEDI.rawName),
                    R.style.PropovjediTheme))
        }
        btnDuhovnost.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.DUHOVNOST.id.toLong(),
                            EvaCategory.DUHOVNOST.rawName),
                    R.style.DuhovnostTheme))
        }
        btnEvandjelje.setOnClickListener {
            RxEventBus.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.EVANDJELJE.id.toLong(),
                            EvaCategory.EVANDJELJE.rawName),
                    R.style.EvandjeljeTheme))
        }
    }
}
