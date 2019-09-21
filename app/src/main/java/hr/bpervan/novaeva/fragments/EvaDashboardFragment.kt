package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.OpenQuotesEvent
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.util.*
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
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme))
                .inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Transparent)
        EventPipelines.changeStatusbarColor.onNext(R.color.Transparent)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        disposables += EventPipelines.dashboardBackground.subscribe {
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
                    EvaDirectory(
                            title = getString(EvaDomain.SONGBOOK.title),
                            domain = EvaDomain.SONGBOOK.toString()),
                    R.style.PjesmaricaTheme))
        }
        btnAktualno.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(EvaDomain.TRENDING.title),
                            domain = EvaDomain.TRENDING.toString()),
                    R.style.AktualnoTheme))
        }
        btnPoziv.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(EvaDomain.VOCATION.title),
                            domain = EvaDomain.VOCATION.toString()),
                    R.style.PozivTheme))
        }
        btnOdgovori.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(EvaDomain.ANSWERS.title),
                            domain = EvaDomain.ANSWERS.toString()),
                    R.style.OdgovoriTheme))
        }
        btnMultimedia.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(EvaDomain.MULTIMEDIA.title),
                            domain = EvaDomain.MULTIMEDIA.toString()),
                    R.style.MultimedijaTheme))
        }
        btnPropovijedi.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(EvaDomain.SERMONS.title),
                            domain = EvaDomain.SERMONS.toString()),
                    R.style.PropovjediTheme))
        }
        btnDuhovnost.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(EvaDomain.SPIRITUALITY.title),
                            domain = EvaDomain.SPIRITUALITY.toString()),
                    R.style.DuhovnostTheme))
        }
        btnCalendar.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(EvaDomain.GOSPEL.title),
                            domain = EvaDomain.GOSPEL.toString()),
                    R.style.EvandjeljeTheme))
        }

        updateUI()

        val lastSyncTimeMillis = prefs.getLong(LAST_SYNC_TIME_MILLIS_KEY, 0L)
        if (System.currentTimeMillis() - lastSyncTimeMillis > syncIntervalMillis) {

            NovaEvaService.v2.getNewStuff().networkRequest({ indicatorsDTO ->
                checkLatestContentId(EvaDomain.GOSPEL, indicatorsDTO.gospel)
                checkLatestContentId(EvaDomain.SONGBOOK, indicatorsDTO.songbook)

                updateUI()
                prefs.edit {
                    putLong(LAST_SYNC_TIME_MILLIS_KEY, System.currentTimeMillis())
                }
            }, onError = {})

            NovaEvaService.v3.latest().networkRequest(onSuccess = { latest ->
                checkLatestContentId(EvaDomain.SPIRITUALITY, latest.spirituality.content)
//                checkLatestContentId(EvaDomain.QUOTES, latest.proverbs.content)
                checkLatestContentId(EvaDomain.TRENDING, latest.trending.content)
                checkLatestContentId(EvaDomain.MULTIMEDIA, latest.multimedia.content)
                checkLatestContentId(EvaDomain.SERMONS, latest.sermons.content)
                checkLatestContentId(EvaDomain.ANSWERS, latest.answers.content)
                checkLatestContentId(EvaDomain.VOCATION, latest.vocation.content)

                updateUI()
                prefs.edit {
                    putLong(LAST_SYNC_TIME_MILLIS_KEY, System.currentTimeMillis())
                }
            }, onError = {})
        }
    }

    private fun checkLatestContentId(domain: EvaDomain, receivedLatestContentId: Long?) {
        receivedLatestContentId ?: return

        val savedLatestContentId = prefs.getLong("$LATEST_CONTENT_ID_KEY_PREFIX.$domain", -1L)
        if (savedLatestContentId != receivedLatestContentId) {
            prefs.edit {
                putLong("$LATEST_CONTENT_ID_KEY_PREFIX.$domain", receivedLatestContentId)
                putBoolean("$HAS_NEW_CONTENT_KEY_PREFIX.$domain", true)
            }
        }
    }

    private fun hasNewContent(domain: EvaDomain): Boolean {
        return prefs.getBoolean("$HAS_NEW_CONTENT_KEY_PREFIX.$domain", false)
    }

    private fun updateUI() {
        view ?: return

//        btnCalendar.indicateNews = hasNewContent(EvaDomain.GOSPEL)
        btnDuhovnost.indicateNews = hasNewContent(EvaDomain.SPIRITUALITY)
        btnIzreke.indicateNews = hasNewContent(EvaDomain.QUOTES)
        btnAktualno.indicateNews = hasNewContent(EvaDomain.TRENDING)
        btnMultimedia.indicateNews = hasNewContent(EvaDomain.MULTIMEDIA)
        btnPropovijedi.indicateNews = hasNewContent(EvaDomain.SERMONS)
        btnOdgovori.indicateNews = hasNewContent(EvaDomain.ANSWERS)
        btnPoziv.indicateNews = hasNewContent(EvaDomain.VOCATION)
        btnPjesmarica.indicateNews = hasNewContent(EvaDomain.SONGBOOK)
    }
}
