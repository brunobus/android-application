package hr.bpervan.novaeva.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.TransitionAnimation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_dashboard.*

/**
 *
 */
class EvaDashboardFragment : EvaBaseFragment() {
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val ctw = ContextThemeWrapper(activity, R.style.DashboardTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {

        btnBrevijar.setOnClickListener {
            EventPipelines.openBreviaryChooser.onNext(TransitionAnimation.FADE)
        }
        btnMolitvenik.setOnClickListener {
            EventPipelines.openPrayerBook.onNext(TransitionAnimation.FADE)
        }
        btnBookmarks.setOnClickListener {
            EventPipelines.openBookmarks.onNext(TransitionAnimation.FADE)
        }
        btnIzreke.setOnClickListener {
            EventPipelines.openQuotes.onNext(OpenQuotesEvent())
        }
        btnPjesmarica.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.PJESMARICA.id.toLong(),
                            EvaCategory.PJESMARICA.rawName),
                    R.style.PjesmaricaTheme))
        }
        btnAktualno.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.AKTUALNO.id.toLong(),
                            EvaCategory.AKTUALNO.rawName),
                    R.style.AktualnoTheme))
        }
        btnPoziv.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.POZIV.id.toLong(),
                            EvaCategory.POZIV.rawName),
                    R.style.PozivTheme))
        }
        btnOdgovori.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.ODGOVORI.id.toLong(),
                            EvaCategory.ODGOVORI.rawName),
                    R.style.OdgovoriTheme))
        }
        btnMultimedia.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.MULTIMEDIJA.id.toLong(),
                            EvaCategory.MULTIMEDIJA.rawName),
                    R.style.MultimedijaTheme))
        }
        btnPropovijedi.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.PROPOVIJEDI.id.toLong(),
                            EvaCategory.PROPOVIJEDI.rawName),
                    R.style.PropovjediTheme))
        }
        btnDuhovnost.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.DUHOVNOST.id.toLong(),
                            EvaCategory.DUHOVNOST.rawName),
                    R.style.DuhovnostTheme))
        }
        btnCalendar.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.EVANDJELJE.id.toLong(),
                            EvaCategory.EVANDJELJE.rawName),
                    R.style.EvandjeljeTheme))
        }
        baseDisposables += EventPipelines.changeDashboardBackground
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    activity?.window?.setBackgroundDrawable(it)
                }
    }

    override fun provideNavBarColorId(evaTheme: EvaTheme): Int = R.color.Transparent

    override fun provideStatusBarColorId(evaTheme: EvaTheme): Int = R.color.Transparent

    override fun provideFragmentBackgroundDrawable(evaTheme: EvaTheme): Drawable? = null
}
