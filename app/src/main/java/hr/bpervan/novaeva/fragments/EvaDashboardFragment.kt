package hr.bpervan.novaeva.fragments

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
import hr.bpervan.novaeva.util.TransitionAnimation
import hr.bpervan.novaeva.util.plusAssign
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

        EventPipelines.changeNavbarColor.onNext(R.color.Transparent)
        EventPipelines.changeStatusbarColor.onNext(R.color.Transparent)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        baseDisposables += EventPipelines.dashboardBackground.subscribe {
            EventPipelines.changeWindowBackgroundDrawable.onNext(it)
        }


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
                    EvaDirectoryMetadata(EvaCategory.SONGBOOK.id.toLong(), getString(R.string.songbook)),
                    R.style.PjesmaricaTheme))
        }
        btnAktualno.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.TRENDING.id.toLong(), getString(R.string.trending)),
                    R.style.AktualnoTheme))
        }
        btnPoziv.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.VOCATION.id.toLong(), getString(R.string.vocation)),
                    R.style.PozivTheme))
        }
        btnOdgovori.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.ANSWERS.id.toLong(), getString(R.string.answers)),
                    R.style.OdgovoriTheme))
        }
        btnMultimedia.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.MULTIMEDIA.id.toLong(), getString(R.string.multimedia)),
                    R.style.MultimedijaTheme))
        }
        btnPropovijedi.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.SERMONS.id.toLong(), getString(R.string.sermons)),
                    R.style.PropovjediTheme))
        }
        btnDuhovnost.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.SPIRITUALITY.id.toLong(), getString(R.string.spirituality)),
                    R.style.DuhovnostTheme))
        }
        btnCalendar.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.GOSPEL.id.toLong(), getString(R.string.gospel)),
                    R.style.EvandjeljeTheme))
        }
    }
}
