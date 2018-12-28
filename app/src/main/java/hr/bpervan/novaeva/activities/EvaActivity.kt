package hr.bpervan.novaeva.activities

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.GravityCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.edit
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.fragments.*
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.model.OpenQuotesEvent
import hr.bpervan.novaeva.player.getStreamLinksFromPlaylistUri
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.rest.serverByDomain
import hr.bpervan.novaeva.util.*
import hr.bpervan.novaeva.util.TransitionAnimation.*
import hr.bpervan.novaeva.views.snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_eva_main.*
import java.util.concurrent.TimeUnit

/**
 *
 */
class EvaActivity : EvaBaseActivity() {

    private var mainContainerId: Int = -1

    private lateinit var mainContainer: ViewGroup

    private val bus = EventPipelines
    private val disposables = CompositeDisposable()

    private lateinit var gestureDetector: GestureDetectorCompat

    private val displayMetrics = DisplayMetrics()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_eva_main)

        initGui()

        if (savedInstanceState == null) {
            openDashboardFragment()

            intent.data?.let { uriData ->
                val contentId = uriData.lastPathSegment?.toLongOrNull() ?: -1L

                if (contentId != -1L) {
                    if (uriData.fragment == "quote") {
                        openQuotesFragment(OpenQuotesEvent(contentId))
                    } else {
                        openContentFragment(OpenContentEvent(
                                EvaContent(contentId, domain = EvaDomain.SERMONS.toString())))
                    }
                }
            }
        }

        updateDisplayMetrics()

        gestureDetector = GestureDetectorCompat(this, SwipeLeftToRightGestureListener(displayMetrics))

        supportFragmentManager?.addOnBackStackChangedListener {
            if (evaRoot.isDrawerOpen(GravityCompat.END)) {
                evaRoot.closeDrawer(GravityCompat.END)
            }
        }
    }

    private fun updateDisplayMetrics() {
        windowManager.defaultDisplay.getMetrics(displayMetrics)
    }

    private val drawerCloseWait = 200L
    var lastDownPress: Long = 0

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(ev)) return true

        if (evaRoot.isDrawerOpen(GravityCompat.END)) {

            val viewRect = Rect()
            evaOptionsFragmentFrame.getGlobalVisibleRect(viewRect)
            if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {

                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastDownPress = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_UP -> {
                        if (System.currentTimeMillis() - lastDownPress <= drawerCloseWait) {
                            evaRoot.closeDrawer(GravityCompat.END)
                        }
                        return true
                    }
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    inner class SwipeLeftToRightGestureListener(displayMetrics: DisplayMetrics) : SwipeGestureListener(displayMetrics) {
        override fun onSwipeRight(): Boolean {
            return when {
                evaRoot.isDrawerOpen(GravityCompat.END) -> false /*drawer already listens for swipe right gesture, don't consume the event here*/
                supportFragmentManager.backStackEntryCount > 1 -> {
                    onBackPressed()
                    true
                }
                else -> false
            }
        }
    }

    private fun initGui() {
        mainContainerId = R.id.evaMainFragmentFrame
        mainContainer = findViewById(mainContainerId)
    }

    override fun onStart() {
        super.onStart()

        disposables += bus.goHome.subscribeThrottled(::openDashboardFragment)
        disposables += bus.openContent.subscribeThrottled(::openContentFragment)

        disposables += bus.search.subscribeThrottled {
            addToBackStack(mainContainerId, EvaSearchFragment, it, FADE, true)
        }

        disposables += bus.openDirectory.subscribeThrottled {
            addToBackStack(mainContainerId, EvaDirectoryFragment, it, it.animation, false)
        }

        disposables += bus.openQuotes.subscribeThrottled(this::openQuotesFragment)

        disposables += bus.openBreviaryChooser.subscribeThrottled {
            addToBackStack(mainContainerId, BreviaryChooserFragment, it, true)
        }

        disposables += bus.openBreviaryContent.subscribeThrottled {
            addToBackStack(mainContainerId, BreviaryContentFragment, it.breviaryId, it.animation, true)
        }

        disposables += bus.openInfo.subscribeThrottled {
            addToBackStack(mainContainerId, EvaInfoFragment, it, true)
        }

        disposables += bus.toggleOptionsDrawer.subscribeThrottled {
            if (evaRoot.isDrawerOpen(GravityCompat.END)) {
                evaRoot.closeDrawer(GravityCompat.END)
            } else {
                evaRoot.openDrawer(GravityCompat.END)
            }
        }

        disposables += bus.openPrayerBook.subscribeThrottled {
            addToBackStack(mainContainerId, PrayerBookFragment, it, true)
        }

        disposables += bus.openPrayerCategory.subscribeThrottled {
            addToBackStack(mainContainerId, PrayerListFragment, it.prayerCategory.id, it.animation, true)
        }

        disposables += bus.openRadio.subscribeThrottled {
            addToBackStack(mainContainerId, RadioFragment, LEFTWARDS, true)
        }

        disposables += bus.openCalendar.subscribeThrottled {
            addToBackStack(mainContainerId, EvaCalendarFragment, it, true)
        }

        disposables += bus.openBookmarks.subscribeThrottled {
            addToBackStack(mainContainerId, EvaBookmarksFragment, it, true)
        }

        disposables += bus.changeWindowBackgroundDrawable
                .distinctUntilChanged()
                .subscribe {
                    window?.setBackgroundDrawable(it)
                }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            disposables += bus.changeNavbarColor
                    .distinctUntilChanged()
                    .subscribe {
                        window?.navigationBarColor = ContextCompat.getColor(this, it)
                    }

            disposables += bus.changeStatusbarColor
                    .distinctUntilChanged()
                    .subscribe {
                        window?.statusBarColor = ContextCompat.getColor(this, it)
                    }
        }

        disposables += bus.connectedToNetwork
                .throttleWithTimeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (!NovaEvaApp.evaPlayer.isStopped()) {
                        Log.w("networkChange", "Network change detected - stopping audio player")
                        NovaEvaApp.evaPlayer.stop() //roaming safeguard
                        evaRoot?.snackbar(R.string.network_changed_player_stopped)
                    }
                }
                .subscribe {
                    fetchBreviaryCoverUrl()
                    fetchDashboardBackgroundUrl()
                }

        disposables += bus.playAnyRadioStation
                .throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    playFirstRadioStation()
                }

        fetchBreviaryCoverUrl()
        fetchDashboardBackgroundUrl()
    }

    private fun playFirstRadioStation() {
        disposables += NovaEvaService.v2.getDirectoryContent(EvaDomain.RADIO.legacyId, null, items = 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.contentMetadataList.first() }
                .doOnError {
                    Log.e("evaNetworkError", it.message, it)
                }
                .observeOn(Schedulers.io())
                .flatMap {
                    NovaEvaService.v2.getContentData(it.contentId)
                            .subscribeOn(Schedulers.io())
                }
                .flatMap { radioStation ->
                    getStreamLinksFromPlaylistUri(radioStation.audioURL!!)
                            .map { Pair(radioStation, it) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stationStreams ->
                    val radioStation = stationStreams.first
                    val streamUris = stationStreams.second
                    for (streamUri in streamUris.shuffled()) {
                        try {
                            NovaEvaApp.evaPlayer.prepareAudioStream(
                                    streamUri, radioStation.contentId.toString(),
                                    radioStation.title ?: "nepoznato",
                                    isRadio = true,
                                    doAutoPlay = true,
                                    auth = serverByDomain(EvaDomain.RADIO).auth)
                            break
                        } catch (e: Exception) {
                            /*continue*/
                        }
                    }
                }) {
                    evaRoot?.dataErrorSnackbar()
                }
    }

    private fun openDashboardFragment(animation: TransitionAnimation = FADE) {
        addToBackStack(mainContainerId, EvaDashboardFragment, animation, true)
    }

    private fun openContentFragment(request: OpenContentEvent) {
        addToBackStack(mainContainerId, EvaContentFragment, request, request.animation, false)
    }

    private fun openQuotesFragment(request: OpenQuotesEvent) {
        addToBackStack(mainContainerId, EvaQuotesFragment, request.quoteId, request.animation, true)
    }

    private fun fetchBreviaryCoverUrl() {
        disposables += NovaEvaService.v2.getDirectoryContent(546, null)
                .networkRequest({ directoryContent ->
                    val image = directoryContent.image?.size640
                    if (image != null) {
                        NovaEvaApp.prefs.edit {
                            putString("hr.bpervan.novaeva.brevijarheaderimage", image)
                        }
                    }
                }, onError = {})
    }

    private fun fetchDashboardBackgroundUrl() {
//        novaEvaServiceV2.getDashboardBackground(EventPipelines.evaTheme.value!!)
//                .networkRequest({ url ->
//                    NovaEvaApp.imageLoader.loadImage(url, object : SimpleImageLoadingListener() {
//                        override fun onLoadingComplete(imageUri: String, view: View?, loadedImage: Bitmap) {
//                            EventPipelines.dashboardBackground.onNext(BitmapDrawable(resources, loadedImage))
//                        }
//                    })
//                }, onError = {})
    }

    override fun onBackPressed() {
        if (evaRoot.isDrawerOpen(GravityCompat.END)) {
            evaRoot.closeDrawer(GravityCompat.END)
        } else {
            supportFragmentManager.ifPresent {
                if (it.backStackEntryCount == 1) {
                    it.popBackStack()
                }
            }
            super.onBackPressed()
        }
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun <T : Fragment, K> addToBackStack(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, K>,
            fragmentInitializer: K, animation: TransitionAnimation = NONE,
            popUpToLastOfSameTypeInclusive: Boolean) {

        if (popUpToLastOfSameTypeInclusive) {
            supportFragmentManager.popBackStack(evaFragmentFactory.tag, POP_BACK_STACK_INCLUSIVE)
        }

        supportFragmentManager.beginTransaction()
                .setCustomAnimation(animation)
                .replace(containerViewId, evaFragmentFactory.newInstance(fragmentInitializer), evaFragmentFactory.tag)
                .addToBackStack(evaFragmentFactory.tag)
                .commitAllowingStateLoss()
    }

    private fun <T : Fragment> addToBackStack(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, Unit>, animation: TransitionAnimation = NONE,
            popUpToLastOfSameTypeInclusive: Boolean) {
        return addToBackStack(containerViewId, evaFragmentFactory, Unit, animation, popUpToLastOfSameTypeInclusive)
    }

    private fun FragmentTransaction.setCustomAnimation(animation: TransitionAnimation): FragmentTransaction {
        return when (animation) {
            LEFTWARDS ->
                setCustomAnimations(R.anim.move_right_in, R.anim.move_left_out, R.anim.move_left_in, R.anim.move_right_out)
            RIGHTWARDS ->
                setCustomAnimations(R.anim.move_left_in, R.anim.move_right_out, R.anim.move_right_in, R.anim.move_left_out)
            UPWARDS ->
                setCustomAnimations(R.anim.move_bottom_in, R.anim.move_top_out, R.anim.move_top_in, R.anim.move_bottom_out)
            DOWNWARDS ->
                setCustomAnimations(R.anim.move_top_in, R.anim.move_bottom_out, R.anim.move_bottom_in, R.anim.move_top_out)
            FADE ->
                setCustomAnimations(R.anim.fade_in, R.anim.nothing, R.anim.fade_in, R.anim.nothing)
            else -> this
        }
    }
}