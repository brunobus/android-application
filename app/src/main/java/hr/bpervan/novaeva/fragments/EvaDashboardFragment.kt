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
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaDirectoryMetadata
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.OpenQuotesEvent
import hr.bpervan.novaeva.services.novaEvaService
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

    private val syncIntervalMillis: Long = 120 * 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("PocetniEkran")
                        .setAction("OtvorenPocetniEkran")
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.DashboardTheme))
                .inflate(R.layout.fragment_dashboard, container, false)
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
                    EvaDirectoryMetadata(EvaCategory.SONGBOOK.id, getString(R.string.songbook)),
                    R.style.PjesmaricaTheme))
        }
        btnAktualno.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.TRENDING.id, getString(R.string.trending)),
                    R.style.AktualnoTheme))
        }
        btnPoziv.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.VOCATION.id, getString(R.string.vocation)),
                    R.style.PozivTheme))
        }
        btnOdgovori.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.ANSWERS.id, getString(R.string.answers)),
                    R.style.OdgovoriTheme))
        }
        btnMultimedia.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.MULTIMEDIA.id, getString(R.string.multimedia)),
                    R.style.MultimedijaTheme))
        }
        btnPropovijedi.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.SERMONS.id, getString(R.string.sermons)),
                    R.style.PropovjediTheme))
        }
        btnDuhovnost.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.SPIRITUALITY.id, getString(R.string.spirituality)),
                    R.style.DuhovnostTheme))
        }
        btnCalendar.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    EvaDirectoryMetadata(EvaCategory.GOSPEL.id, getString(R.string.gospel)),
                    R.style.EvandjeljeTheme))
        }

        val lastSyncTimeMillis = prefs.getLong(LAST_SYNC_TIME_MILLIS_KEY, 0L)
        if (System.currentTimeMillis() - lastSyncTimeMillis > syncIntervalMillis) {
            novaEvaService.getNewStuff().networkRequest({ indicatorsDTO ->

                checkLatestContentId(EvaCategory.SPIRITUALITY, indicatorsDTO.spirituality)
                checkLatestContentId(EvaCategory.QUOTES, indicatorsDTO.quotes)
                checkLatestContentId(EvaCategory.TRENDING, indicatorsDTO.trending)
                checkLatestContentId(EvaCategory.MULTIMEDIA, indicatorsDTO.multimedia)
                checkLatestContentId(EvaCategory.SERMONS, indicatorsDTO.sermons)
                checkLatestContentId(EvaCategory.VOCATION, indicatorsDTO.vocation)
                checkLatestContentId(EvaCategory.ANSWERS, indicatorsDTO.answers)
                checkLatestContentId(EvaCategory.SONGBOOK, indicatorsDTO.songbook)
                checkLatestContentId(EvaCategory.GOSPEL, indicatorsDTO.gospel)

                updateUI()
                prefs.edit {
                    putLong(LAST_SYNC_TIME_MILLIS_KEY, System.currentTimeMillis())
                }
            }, onError = {})
        }

        updateUI()
    }

    private fun checkLatestContentId(category: EvaCategory, receivedLatestContentId: Int?) {
        receivedLatestContentId ?: return

        val savedLatestContentId = prefs.getInt("$LATEST_CONTENT_ID_KEY_PREFIX${category.id}", 0)
        if (savedLatestContentId != receivedLatestContentId) {
            prefs.edit {
                putInt("$LATEST_CONTENT_ID_KEY_PREFIX${category.id}", receivedLatestContentId)
                putBoolean("$NEW_CONTENT_KEY_PREFIX${category.id}", true)
            }
        }
    }

    private fun hasNewContent(categoryId: Long): Boolean {
        return prefs.getBoolean("$NEW_CONTENT_KEY_PREFIX$categoryId", false)
    }

    private fun updateUI() {
        view ?: return

        btnDuhovnost.indicateNews = hasNewContent(EvaCategory.SPIRITUALITY.id)
        btnIzreke.indicateNews = hasNewContent(EvaCategory.QUOTES.id)
        btnAktualno.indicateNews = hasNewContent(EvaCategory.TRENDING.id)
        btnMultimedia.indicateNews = hasNewContent(EvaCategory.MULTIMEDIA.id)
        btnPropovijedi.indicateNews = hasNewContent(EvaCategory.SERMONS.id)
        btnOdgovori.indicateNews = hasNewContent(EvaCategory.ANSWERS.id)
        btnPoziv.indicateNews = hasNewContent(EvaCategory.VOCATION.id)
        btnPjesmarica.indicateNews = hasNewContent(EvaCategory.SONGBOOK.id)
    }
}
