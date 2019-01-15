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
                    EvaDirectory(title = getString(R.string.songbook),
                            domain = EvaDomain.SONGBOOK.toString()),
                    R.style.PjesmaricaTheme))
        }
        btnAktualno.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(title = getString(R.string.trending),
                            domain = EvaDomain.TRENDING.toString()),
                    R.style.AktualnoTheme))
        }
        btnPoziv.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(title = getString(R.string.vocation),
                            domain = EvaDomain.VOCATION.toString()),
                    R.style.PozivTheme))
        }
        btnOdgovori.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(title = getString(R.string.answers),
                            domain = EvaDomain.ANSWERS.toString()),
                    R.style.OdgovoriTheme))
        }
        btnMultimedia.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(R.string.multimedia),
                            domain = EvaDomain.MULTIMEDIA.toString()),
                    R.style.MultimedijaTheme))
        }
        btnPropovijedi.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(R.string.sermons),
                            domain = EvaDomain.SERMONS.toString()),
                    R.style.PropovjediTheme))
        }
        btnDuhovnost.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(R.string.spirituality),
                            domain = EvaDomain.SPIRITUALITY.toString()),
                    R.style.DuhovnostTheme))
        }
        btnCalendar.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectory(
                            title = getString(R.string.gospel),
                            domain = EvaDomain.GOSPEL.toString()),
                    R.style.EvandjeljeTheme))
        }

        val lastSyncTimeMillis = prefs.getLong(LAST_SYNC_TIME_MILLIS_KEY, 0L)
        if (System.currentTimeMillis() - lastSyncTimeMillis > syncIntervalMillis) {
            NovaEvaService.v2.getNewStuff().networkRequest({ indicatorsDTO ->

                checkLatestContentId(EvaDomain.SPIRITUALITY, indicatorsDTO.spirituality)
                checkLatestContentId(EvaDomain.QUOTES, indicatorsDTO.quotes)
                checkLatestContentId(EvaDomain.TRENDING, indicatorsDTO.trending)
                checkLatestContentId(EvaDomain.MULTIMEDIA, indicatorsDTO.multimedia)
                checkLatestContentId(EvaDomain.SERMONS, indicatorsDTO.sermons)
                checkLatestContentId(EvaDomain.ANSWERS, indicatorsDTO.answers)
                checkLatestContentId(EvaDomain.SONGBOOK, indicatorsDTO.songbook)
                checkLatestContentId(EvaDomain.GOSPEL, indicatorsDTO.gospel)

                updateUI()
                prefs.edit {
                    putLong(LAST_SYNC_TIME_MILLIS_KEY, System.currentTimeMillis())
                }
            }, onError = {})
            NovaEvaService.v3.latest().networkRequest(onSuccess = { latest ->
                checkLatestContentId(EvaDomain.VOCATION, latest.vocation.content)
            }, onError = {})
        }
        updateUI()
    }

    private fun checkLatestContentId(domain: EvaDomain, receivedLatestContentId: Long?) {
        receivedLatestContentId ?: return

        val savedLatestContentId = prefs.getLong("$LATEST_CONTENT_ID_KEY_PREFIX${domain.rootId}", 0)
        if (savedLatestContentId != receivedLatestContentId) {
            prefs.edit {
                putLong("$LATEST_CONTENT_ID_KEY_PREFIX${domain.rootId}", receivedLatestContentId)
                putBoolean("$NEW_CONTENT_KEY_PREFIX${domain.rootId}", true)
            }
        }
    }

    private fun hasNewContent(categoryId: Long): Boolean {
        return prefs.getBoolean("$NEW_CONTENT_KEY_PREFIX$categoryId", false)
    }

    private fun updateUI() {
        view ?: return

        btnDuhovnost.indicateNews = hasNewContent(EvaDomain.SPIRITUALITY.rootId)
        btnIzreke.indicateNews = hasNewContent(EvaDomain.QUOTES.rootId)
        btnAktualno.indicateNews = hasNewContent(EvaDomain.TRENDING.rootId)
        btnMultimedia.indicateNews = hasNewContent(EvaDomain.MULTIMEDIA.rootId)
        btnPropovijedi.indicateNews = hasNewContent(EvaDomain.SERMONS.rootId)
        btnOdgovori.indicateNews = hasNewContent(EvaDomain.ANSWERS.rootId)
        btnPoziv.indicateNews = hasNewContent(EvaDomain.VOCATION.rootId)
        btnPjesmarica.indicateNews = hasNewContent(EvaDomain.SONGBOOK.rootId)
    }
}
