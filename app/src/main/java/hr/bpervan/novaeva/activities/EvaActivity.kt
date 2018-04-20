package hr.bpervan.novaeva.activities

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.fragments.*
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.utilities.TransitionAnimation
import hr.bpervan.novaeva.utilities.TransitionAnimation.*
import hr.bpervan.novaeva.utilities.networkRequest
import hr.bpervan.novaeva.utilities.screenChangeThrottle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.eva_main_layout.*
import java.util.concurrent.TimeUnit

/**
 *
 */
class EvaActivity : EvaBaseActivity() {

    private var mainContainerId: Int = -1

    private lateinit var mainContainer: ViewGroup

    private val bus = EventPipelines
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.eva_main_layout)

        initGui()

        if (savedInstanceState == null) {
            openDashboardFragment()

            val contentId = intent.data?.lastPathSegment?.toLongOrNull() ?: -1L
            if (contentId != -1L) {
                openContentFragment(OpenContentEvent(
                        EvaContentMetadata(contentId, 0, -1)))
            }
        }
    }

    private fun initGui() {
        mainContainerId = R.id.evaMainFragmentFrame

        mainContainer = findViewById(mainContainerId)

        mainContainer.let {
            it.setOnHierarchyChangeListener(EvaHierarchyChangeListener(it))
        }

    }

    private inner class EvaHierarchyChangeListener(private val targetView: ViewGroup)
        : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewRemoved(parent: View?, child: View?) {
            if (targetView.childCount == 0) {
                targetView.visibility = View.GONE
            }
        }

        override fun onChildViewAdded(parent: View?, child: View?) {
            targetView.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()

        disposables += bus.goHome.screenChangeThrottle().subscribe(::openDashboardFragment)
        disposables += bus.openContent.screenChangeThrottle().subscribe(::openContentFragment)

        disposables += bus.search.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, EvaSearchFragment, it, FADE)
        }

        disposables += bus.openDirectory.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, EvaDirectoryFragment, it, it.animation)
        }

        disposables += bus.openQuotes.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, EvaQuotesFragment, it.quoteId, it.animation)
        }

        disposables += bus.openBreviaryChooser.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, BreviaryChooserFragment, it)
        }

        disposables += bus.openBreviaryContent.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, BreviaryContentFragment, it.breviaryId, it.animation)
        }

        disposables += bus.openInfo.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, EvaInfoFragment, it)
        }

        disposables += bus.openOptionsDrawer.screenChangeThrottle().subscribe {
            evaRoot.openDrawer(GravityCompat.END)
        }

        disposables += bus.openPrayerBook.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, PrayerBookFragment, it)
        }

        disposables += bus.openPrayerCategory.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, PrayerListFragment, it.prayerCategory.id, it.animation)
        }

        disposables += bus.openRadio.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, RadioFragment, RIGHTWARDS)
        }

        disposables += bus.openCalendar.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, EvaCalendarFragment, it)
        }

        disposables += bus.openBookmarks.screenChangeThrottle().subscribe {
            addToBackStack(mainContainerId, EvaBookmarksFragment, it)
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
                .subscribe {
                    fetchBreviaryCoverUrl()
                    fetchDashboardBackgroundUrl()
                }

        fetchBreviaryCoverUrl()
        fetchDashboardBackgroundUrl()
    }

    private fun fetchBreviaryCoverUrl() {
        novaEvaService.getDirectoryContent(546, null)
                .networkRequest({ directoryContent ->
                    val image = directoryContent.image?.size640 ?: directoryContent.image?.size640
                    if (image != null) {
                        NovaEvaApp.prefs.edit().putString("hr.bpervan.novaeva.brevijarheaderimage", image).apply()
                    }
                }, onError = {})
    }

    private fun fetchDashboardBackgroundUrl() {
        novaEvaService.getDashboardBackground(EventPipelines.evaTheme.value!!)
                .networkRequest({ url ->
                    NovaEvaApp.imageLoader.loadImage(url, object : SimpleImageLoadingListener() {
                        override fun onLoadingComplete(imageUri: String, view: View?, loadedImage: Bitmap) {
                            EventPipelines.dashboardBackground.onNext(BitmapDrawable(resources, loadedImage))
                        }
                    })
                }, onError = {})
    }

    private fun openDashboardFragment(animation: TransitionAnimation = FADE) {
        popAllFragments()

        supportFragmentManager.beginTransaction()
                .setCustomAnimation(animation)
                .replace(mainContainerId, EvaDashboardFragment)
                .commitAllowingStateLoss()
    }

    private fun openContentFragment(request: OpenContentEvent) {

        addToBackStack(mainContainerId, EvaContentFragment, request, request.animation)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun popAllFragments() {
        supportFragmentManager.popBackStackImmediate(null, POP_BACK_STACK_INCLUSIVE)
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

    private fun <T : Fragment, K> FragmentTransaction.replace(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, K>,
            fragmentInitializer: K): FragmentTransaction {
        return this.replace(containerViewId, evaFragmentFactory.newInstance(fragmentInitializer), evaFragmentFactory.tag)
    }

    private fun <T : Fragment> FragmentTransaction.replace(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, Unit>): FragmentTransaction {
        return this.replace(containerViewId, evaFragmentFactory, Unit)
    }

    private fun <T : Fragment, K> addToBackStack(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, K>,
            fragmentInitializer: K, animation: TransitionAnimation = NONE) {

        supportFragmentManager.beginTransaction()
                .setCustomAnimation(animation)
                .replace(containerViewId, evaFragmentFactory, fragmentInitializer)
                .addToBackStack(null)
                .commitAllowingStateLoss()
    }

    private fun <T : Fragment> addToBackStack(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, Unit>, animation: TransitionAnimation = NONE) {
        return addToBackStack(containerViewId, evaFragmentFactory, Unit, animation)
    }
}