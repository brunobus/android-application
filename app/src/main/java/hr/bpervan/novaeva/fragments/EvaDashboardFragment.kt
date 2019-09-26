package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.OpenQuotesEvent
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
                    title = getString(EvaDomain.SONGBOOK.title),
                    domain = EvaDomain.SONGBOOK,
                    theme = R.style.PjesmaricaTheme))
        }
        btnAktualno.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.TRENDING.title),
                    domain = EvaDomain.TRENDING,
                    theme = R.style.AktualnoTheme))
        }
        btnPoziv.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.VOCATION.title),
                    domain = EvaDomain.VOCATION,
                    theme = R.style.PozivTheme))
        }
        btnOdgovori.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.ANSWERS.title),
                    domain = EvaDomain.ANSWERS,
                    theme = R.style.OdgovoriTheme))
        }
        btnMultimedia.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.MULTIMEDIA.title),
                    domain = EvaDomain.MULTIMEDIA,
                    theme = R.style.MultimedijaTheme))
        }
        btnPropovijedi.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.SERMONS.title),
                    domain = EvaDomain.SERMONS,
                    theme = R.style.PropovjediTheme))
        }
        btnDuhovnost.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.SPIRITUALITY.title),
                    domain = EvaDomain.SPIRITUALITY,
                    theme = R.style.DuhovnostTheme))
        }
        btnCalendar.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.GOSPEL.title),
                    domain = EvaDomain.GOSPEL,
                    theme = R.style.EvandjeljeTheme))
        }

        updateUI()

        val lastSyncTimeMillis = prefs.getLong(LAST_SYNC_TIME_MILLIS_KEY, 0L)
        if (System.currentTimeMillis() - lastSyncTimeMillis > syncIntervalMillis) {

            disposables += NovaEvaService.v2.getNewStuff()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = { indicatorsDTO ->
                        updateLatestContentId(EvaDomain.GOSPEL, indicatorsDTO.gospel)
                        updateLatestContentId(EvaDomain.SONGBOOK, indicatorsDTO.songbook)

                        updateUI()
                        prefs.edit {
                            putLong(LAST_SYNC_TIME_MILLIS_KEY, System.currentTimeMillis())
                        }
                    }, onError = {
                        Log.e("fetchLatestLegacy", it.message, it)
                    })

            disposables += NovaEvaService.v3.latest()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = { latest ->
                        updateLatestContentId(EvaDomain.SPIRITUALITY, latest.spirituality.content)
//                      updateLatestContentId(EvaDomain.QUOTES, latest.proverbs.content)
                        updateLatestContentId(EvaDomain.TRENDING, latest.trending.content)
                        updateLatestContentId(EvaDomain.MULTIMEDIA, latest.multimedia.content)
                        updateLatestContentId(EvaDomain.SERMONS, latest.sermons.content)
                        updateLatestContentId(EvaDomain.ANSWERS, latest.answers.content)
                        updateLatestContentId(EvaDomain.VOCATION, latest.vocation.content)

                        updateUI()

                        prefs.edit {
                            putLong(LAST_SYNC_TIME_MILLIS_KEY, System.currentTimeMillis())
                        }
                    }, onError = {
                        Log.e("fetchLatest", it.message, it)
                    })
        }
    }

    private fun updateLatestContentId(domain: EvaDomain, receivedLatestContentId: Long?) {
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
