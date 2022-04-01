package hr.bpervan.novaeva.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentDashboardBinding
import hr.bpervan.novaeva.model.ContentDto
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.OpenPrayerDirectoryEvent
import hr.bpervan.novaeva.model.OpenQuotesEvent
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.HAS_NEW_CONTENT_KEY_PREFIX
import hr.bpervan.novaeva.util.LAST_SYNC_TIME_MILLIS_KEY
import hr.bpervan.novaeva.util.LATEST_CONTENT_ID_KEY_PREFIX
import hr.bpervan.novaeva.util.TransitionAnimation
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.util.stripTrimAndEllipsizeText
import hr.bpervan.novaeva.util.syncIntervalMillis
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import java.util.*

/**
 *
 */
class EvaDashboardFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaDashboardFragment, Unit> {

        override fun newInstance(initializer: Unit): EvaDashboardFragment {
            return EvaDashboardFragment()
        }
    }

    private var _viewBinding: FragmentDashboardBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var realm: Realm

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme))
        _viewBinding = FragmentDashboardBinding.inflate(newInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Transparent)
        EventPipelines.changeStatusbarColor.onNext(R.color.Transparent)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        disposables += EventPipelines.dashboardBackground.subscribe {
            EventPipelines.changeWindowBackgroundDrawable.onNext(it)
        }

        viewBinding.btnBrevijar.setOnClickListener {
            EventPipelines.openBreviaryChooser.onNext(TransitionAnimation.FADE)
        }
        viewBinding.btnMolitvenik.setOnClickListener {
            EventPipelines.openPrayerBook.onNext(OpenPrayerDirectoryEvent(
                    title = getString(EvaDomain.PRAYERS.title)
            ))
        }
        viewBinding.btnBookmarks.setOnClickListener {
            EventPipelines.openBookmarks.onNext(TransitionAnimation.FADE)
        }
        viewBinding.btnIzreke.setOnClickListener {
            EventPipelines.openQuotes.onNext(OpenQuotesEvent())
        }
        viewBinding.btnPjesmarica.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.SONGS.title),
                    domain = EvaDomain.SONGS,
                    theme = R.style.PjesmaricaTheme))
        }
        viewBinding.btnAktualno.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.TRENDING.title),
                    domain = EvaDomain.TRENDING,
                    theme = R.style.AktualnoTheme))
        }
        viewBinding.btnPoziv.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.VOCATION.title),
                    domain = EvaDomain.VOCATION,
                    theme = R.style.PozivTheme))
        }
        viewBinding.btnOdgovori.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.ANSWERS.title),
                    domain = EvaDomain.ANSWERS,
                    theme = R.style.OdgovoriTheme))
        }
        viewBinding.btnMultimedia.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.MULTIMEDIA.title),
                    domain = EvaDomain.MULTIMEDIA,
                    theme = R.style.MultimedijaTheme))
        }
        viewBinding.btnPropovijedi.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.SERMONS.title),
                    domain = EvaDomain.SERMONS,
                    theme = R.style.PropovjediTheme))
        }
        viewBinding.btnDuhovnost.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.SPIRITUALITY.title),
                    domain = EvaDomain.SPIRITUALITY,
                    theme = R.style.DuhovnostTheme))
        }
        viewBinding.btnCalendar.setOnClickListener {
            EventPipelines.openDirectory.onNext(OpenDirectoryEvent(
                    title = getString(EvaDomain.GOSPEL.title),
                    domain = EvaDomain.GOSPEL,
                    theme = R.style.EvandjeljeTheme))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    disposables += liveContentCompletable().subscribe()
                }
            }

            val connMan = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let {
                connMan.registerDefaultNetworkCallback(it)
            }
        } else {
            disposables += liveContentCompletable().subscribe()
        }

        updateUI()

        val lastSyncTimeMillis = prefs.getLong(LAST_SYNC_TIME_MILLIS_KEY, 0L)
        if (System.currentTimeMillis() - lastSyncTimeMillis > syncIntervalMillis) {

            disposables += NovaEvaService.v2.getNewStuff()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = { indicatorsDTO ->
                        updateLatestContentId(EvaDomain.GOSPEL, indicatorsDTO.gospel)

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
                        updateLatestContentId(EvaDomain.SONGS, latest.songs.content)

                        updateUI()

                        prefs.edit {
                            putLong(LAST_SYNC_TIME_MILLIS_KEY, System.currentTimeMillis())
                        }
                    }, onError = {
                        Log.e("fetchLatest", it.message, it)
                    })
        }
    }

    private fun liveContentCompletable(): Completable {
        return NovaEvaService.v3.categoryContent(EvaDomain.LIVE.domainEndpoint, items = 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { onLiveContentReceived(it.content?.firstOrNull()) }
                .doOnError { disableLive() }
                .ignoreElement()
                .onErrorComplete()
    }

    private fun onLiveContentReceived(liveContent: ContentDto?) {
        if (liveContent == null) {
            disableLive()
        } else {
            EvaContentDbAdapter.addOrUpdateEvaContentAsync(realm, liveContent) {
                view ?: return@addOrUpdateEvaContentAsync

                if (!liveContent.active) {
                    disableLive()
                } else {
                    val title = liveContent.title?.toUpperCase(Locale.getDefault())
                            ?: getString(R.string.default_live_title)
                    val preview = stripTrimAndEllipsizeText(25, liveContent.html) ?: ""

                    val videoUrl = liveContent.video?.firstOrNull()?.link

                    if (videoUrl != null) {
                        enableLive(title, preview, videoUrl)
                    } else {
                        disableLive()
                    }
                }
            }
        }
    }

    fun enableLive(title: String, preview: String, videoUrl: String) {
        viewBinding.btnLive?.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, videoUrl.toUri()))
        }
        viewBinding.btnLive?.isEnabled = true
        viewBinding.btnLive?.text = "$title\n$preview"
    }

    fun disableLive() {
        viewBinding.btnLive?.isEnabled = false
        viewBinding.btnLive?.text = ""
        viewBinding.btnLive?.setOnClickListener(null)
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Glavni izbornik", "Dashboard")

        disposables += liveContentCompletable().subscribe()
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

//        viewBinding.btnCalendar.indicateNews = hasNewContent(EvaDomain.GOSPEL)
        viewBinding.btnDuhovnost.indicateNews = hasNewContent(EvaDomain.SPIRITUALITY)
        viewBinding.btnIzreke.indicateNews = hasNewContent(EvaDomain.QUOTES)
        viewBinding.btnAktualno.indicateNews = hasNewContent(EvaDomain.TRENDING)
        viewBinding.btnMultimedia.indicateNews = hasNewContent(EvaDomain.MULTIMEDIA)
        viewBinding.btnPropovijedi.indicateNews = hasNewContent(EvaDomain.SERMONS)
        viewBinding.btnOdgovori.indicateNews = hasNewContent(EvaDomain.ANSWERS)
        viewBinding.btnPoziv.indicateNews = hasNewContent(EvaDomain.VOCATION)
        viewBinding.btnPjesmarica.indicateNews = hasNewContent(EvaDomain.SONGS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connMan = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let {
                connMan.unregisterNetworkCallback(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }
}
