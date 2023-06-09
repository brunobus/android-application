package hr.bpervan.novaeva.activities

import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.fragments.BreviaryChooserFragment
import hr.bpervan.novaeva.fragments.BreviaryContentFragment
import hr.bpervan.novaeva.fragments.EvaBaseFragment
import hr.bpervan.novaeva.fragments.EvaBookmarksFragment
import hr.bpervan.novaeva.fragments.EvaCalendarFragment
import hr.bpervan.novaeva.fragments.EvaContentFragment
import hr.bpervan.novaeva.fragments.EvaDashboardFragment
import hr.bpervan.novaeva.fragments.EvaDirectoryFragment
import hr.bpervan.novaeva.fragments.EvaInfoFragment
import hr.bpervan.novaeva.fragments.EvaQuotesFragment
import hr.bpervan.novaeva.fragments.PrayerBookFragment
import hr.bpervan.novaeva.fragments.PrayerListFragment
import hr.bpervan.novaeva.fragments.RadioFragment
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.ActivityEvaMainBinding
import hr.bpervan.novaeva.model.EvaDomainInfo
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.model.OpenQuotesEvent
import hr.bpervan.novaeva.player.getStreamLinksFromPlaylist
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.BREVIARY_IMAGE_KEY
import hr.bpervan.novaeva.util.SwipeGestureListener
import hr.bpervan.novaeva.util.TransitionAnimation
import hr.bpervan.novaeva.util.TransitionAnimation.DOWNWARDS
import hr.bpervan.novaeva.util.TransitionAnimation.FADE
import hr.bpervan.novaeva.util.TransitionAnimation.LEFTWARDS
import hr.bpervan.novaeva.util.TransitionAnimation.NONE
import hr.bpervan.novaeva.util.TransitionAnimation.RIGHTWARDS
import hr.bpervan.novaeva.util.TransitionAnimation.UPWARDS
import hr.bpervan.novaeva.util.dataErrorSnackbar
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.util.subscribeThrottled
import hr.bpervan.novaeva.views.snackbar
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.kotlin.where
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

    private lateinit var realm: Realm

    private lateinit var viewBinding: ActivityEvaMainBinding

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewBinding = ActivityEvaMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        updateAllDomainRoots()

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
                                contentId = contentId,
                                title = contentId.toString(),
                                domain = EvaDomain.SERMONS))
                    }
                }
            }
        }

        updateDisplayMetrics()

        gestureDetector = GestureDetectorCompat(this, SwipeLeftToRightGestureListener(displayMetrics))

        supportFragmentManager.addOnBackStackChangedListener {
            if (viewBinding.root.isDrawerOpen(GravityCompat.END)) {
                viewBinding.root.closeDrawer(GravityCompat.END)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        supportFragmentManager.let {
            if (it.backStackEntryCount == 0) {
                openDashboardFragment()
            }
        }
    }

    private fun updateAllDomainRoots() {
        realm.where<EvaDomainInfo>()
                .notEqualTo("rootCategoryId", -1L)
                .findAll()
                .forEach { domainInfo ->
                    updateDomainRoot(domainInfo)
                }
    }

    private fun updateMissingDomainRoots() {
        realm.where<EvaDomainInfo>()
                .equalTo("rootCategoryId", 0L)
                .findAll()
                .forEach { domainInfo ->
                    updateDomainRoot(domainInfo)
                }
    }

    private fun updateDomainRoot(domainInfo: EvaDomainInfo) {
        disposables += NovaEvaService.v3.categoryContent(domainInfo.endpointRoot, categoryId = 0, page = 1, items = 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { categoryDto ->
                    realm.executeTransaction { transRealm ->
                        domainInfo.rootCategoryId = categoryDto.id
                        transRealm.copyToRealmOrUpdate(domainInfo)
                    }
                }, onError = {
                    Log.e("categoryRootId", it.message, it)
                })
    }

    private fun updateDisplayMetrics() {
        windowManager.defaultDisplay.getMetrics(displayMetrics)
    }

    private val drawerCloseWait = 200L
    var lastDownPress: Long = 0

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(ev)) return true

        if (viewBinding.root.isDrawerOpen(GravityCompat.END)) {

            val viewRect = Rect()
            viewBinding.evaOptionsFragmentFrame.getGlobalVisibleRect(viewRect)
            if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {

                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastDownPress = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_UP -> {
                        if (System.currentTimeMillis() - lastDownPress <= drawerCloseWait) {
                            viewBinding.root.closeDrawer(GravityCompat.END)
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
                viewBinding.root.isDrawerOpen(GravityCompat.END) -> false /*drawer already listens for swipe right gesture, don't consume the event here*/
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
            if (viewBinding.root.isDrawerOpen(GravityCompat.END)) {
                viewBinding.root.closeDrawer(GravityCompat.END)
            } else {
                viewBinding.root.openDrawer(GravityCompat.END)
            }
        }

        disposables += bus.openPrayerBook.subscribeThrottled {
            addToBackStack(mainContainerId, PrayerBookFragment, it, it.animation, true)
        }

        disposables += bus.openPrayerCategory.subscribeThrottled {
            addToBackStack(mainContainerId, PrayerListFragment, it, it.animation, true)
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

        disposables += bus.playAnyRadioStation
                .throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    playFirstRadioStation()
                }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            networkCallback = object : ConnectivityManager.NetworkCallback() {

                private var wasRoaming: Boolean = false;

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)

                    Completable
                        .fromAction {
                            updateMissingDomainRoots()
                        }
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe()

                }

                // TODO try to move to AudioPlayerService
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities)

                    val isRoaming = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
                    if (isRoaming && !wasRoaming) {
                        Completable
                            .fromAction {
                                if (!NovaEvaApp.evaPlayer.isStopped()) {
                                    Log.w("roamingDetected", "Roaming detected - stopping audio player")
                                    NovaEvaApp.evaPlayer.stop()
                                    viewBinding.root.snackbar(R.string.network_changed_player_stopped)
                                }
                            }
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe()
                        wasRoaming = true
                    } else if (!isRoaming) {
                        wasRoaming = false
                    }
                }
            }

            val connMan = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let {
                connMan.registerDefaultNetworkCallback(it)
            }
        }

        fetchBreviaryCoverUrl()
        fetchDashboardBackgroundUrl()
    }

    private fun playFirstRadioStation() {
        disposables += NovaEvaService.v3.categoryContent(EvaDomain.RADIO.domainEndpoint, items = 1000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.content!!.first() }
                .doOnError {
                    Log.e("evaNetworkError", it.message, it)
                }
                .observeOn(Schedulers.io())
                .flatMap { radioStation ->
                    val playlist = radioStation.audio!!.first()
                    getStreamLinksFromPlaylist(playlist.link!!, playlist.title!!)
                            .map { Pair(radioStation, it) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stationStreams ->
                    val radioStation = stationStreams.first
                    val streamUris = stationStreams.second
                    for (streamUri in streamUris.shuffled()) {
                        try {
                            NovaEvaApp.evaPlayer.prepareAudioStream(
                                    streamUri, radioStation.id.toString(),
                                    radioStation.title ?: "nepoznato",
                                    isRadio = true,
                                    doAutoPlay = true,
                                    auth = null)
                            break
                        } catch (e: Exception) {
                            /*continue*/
                        }
                    }
                }) {
                    viewBinding.root.dataErrorSnackbar()
                }
    }

    private fun openDashboardFragment(animation: TransitionAnimation = NONE) {
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { directoryContent ->
                    val image = directoryContent.image?.size640
                    if (image != null) {
                        NovaEvaApp.prefs.edit {
                            putString(BREVIARY_IMAGE_KEY, image)
                        }
                    }
                }, onError = {
                    Log.e("breviaryCover", it.message, it)
                })
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
        if (viewBinding.root.isDrawerOpen(GravityCompat.END)) {
            viewBinding.root.closeDrawer(GravityCompat.END)
        } else {
            supportFragmentManager.let {
                if (it.backStackEntryCount == 1) {
                    it.popBackStack()
                }
            }
            super.onBackPressed()
        }
    }

    override fun onStop() {
        disposables.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connMan = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let {
                connMan.unregisterNetworkCallback(it)
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        disposables.dispose()

        realm.close()
        super.onDestroy()
    }

    private fun <T : androidx.fragment.app.Fragment, K> addToBackStack(
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

    private fun <T : androidx.fragment.app.Fragment> addToBackStack(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, Unit>, animation: TransitionAnimation = NONE,
            popUpToLastOfSameTypeInclusive: Boolean) {
        return addToBackStack(containerViewId, evaFragmentFactory, Unit, animation, popUpToLastOfSameTypeInclusive)
    }

    private fun androidx.fragment.app.FragmentTransaction.setCustomAnimation(animation: TransitionAnimation): androidx.fragment.app.FragmentTransaction {
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