package hr.bpervan.novaeva.activities

import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.player.getStreamLinksFromPlaylistUri
import hr.bpervan.novaeva.services.novaEvaService
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

            val contentId = intent.data?.lastPathSegment?.toLongOrNull() ?: -1L
            if (contentId != -1L) {
                openContentFragment(OpenContentEvent(
                        EvaContentMetadata(contentId, 0, -1)))
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (gestureDetector.onTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)
    }

    inner class SwipeLeftToRightGestureListener(displayMetrics: DisplayMetrics) : SwipeGestureListener(displayMetrics) {
        override fun onSwipeRight(): Boolean {
            return if (supportFragmentManager.backStackEntryCount > 0
                    && !evaRoot.isDrawerOpen(GravityCompat.END)) {
                onBackPressed()
                true
            } else {
                /*drawer already listens for swipe right gesture, don't consume the event here*/
                false
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

        disposables += bus.openQuotes.subscribeThrottled {
            addToBackStack(mainContainerId, EvaQuotesFragment, it.quoteId, it.animation, true)
        }

        disposables += bus.openBreviaryChooser.subscribeThrottled {
            addToBackStack(mainContainerId, BreviaryChooserFragment, it, true)
        }

        disposables += bus.openBreviaryContent.subscribeThrottled {
            addToBackStack(mainContainerId, BreviaryContentFragment, it.breviaryId, it.animation, true)
        }

        disposables += bus.openInfo.subscribeThrottled {
            addToBackStack(mainContainerId, EvaInfoFragment, it, true)
        }

        disposables += bus.openOptionsDrawer.subscribeThrottled {
            evaRoot.openDrawer(GravityCompat.END)
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
                    NovaEvaApp.evaPlayer.stop() //roaming safeguard
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
        disposables += novaEvaService.getDirectoryContent(EvaCategory.RADIO.id, null, items = 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.contentMetadataList.first() }
                .doOnError {
                    Log.e("evaNetworkError", it.message, it)
                }
                .observeOn(Schedulers.io())
                .flatMap {
                    novaEvaService.getContentData(it.contentId)
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
                                    isRadio = true, doAutoPlay = true)
                            break
                        } catch (e: Exception) {
                            /*continue*/
                        }
                    }
                }) {
                    evaRoot?.snackbar(R.string.error_fetching_data, Snackbar.LENGTH_LONG)
                }
    }

    private fun openDashboardFragment(animation: TransitionAnimation = FADE) {
        addToBackStack(mainContainerId, EvaDashboardFragment, animation, true)
    }

    private fun openContentFragment(request: OpenContentEvent) {
        addToBackStack(mainContainerId, EvaContentFragment, request, request.animation, false)
    }

    private fun fetchBreviaryCoverUrl() {
        disposables += novaEvaService.getDirectoryContent(546, null)
                .networkRequest({ directoryContent ->
                    val image = directoryContent.image?.size640 ?: directoryContent.image?.size640
                    if (image != null) {
                        NovaEvaApp.prefs.edit {
                            putString("hr.bpervan.novaeva.brevijarheaderimage", image)
                        }
                    }
                }, onError = {})
    }

    private fun fetchDashboardBackgroundUrl() {
//        novaEvaService.getDashboardBackground(EventPipelines.evaTheme.value!!)
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