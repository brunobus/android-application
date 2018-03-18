package hr.bpervan.novaeva.activities

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.fragments.*
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.TransitionAnimation
import hr.bpervan.novaeva.utilities.TransitionAnimation.*
import hr.bpervan.novaeva.utilities.subscribeAsync
import hr.bpervan.novaeva.utilities.subscribeThrottled
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.eva_main_layout.*

/**
 *
 */
class EvaActivity : EvaBaseActivity() {

    private val dashboardBackgroundUrlSubject = PublishSubject.create<String>()
    private val breviaryImageUrlSubject = PublishSubject.create<String>()

    private var mainContainerId: Int = -1

    private lateinit var mainContainer: ViewGroup

    private val bus = RxEventBus
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

        btnRadio.setOnClickListener {

        }
        btnOptions.setOnClickListener {
            bus.openOptionsDrawer.onNext(Unit)
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

        disposables.addAll(
                bus.goHome.subscribeThrottled(::openDashboardFragment),
                bus.openContent.subscribeThrottled(::openContentFragment),

                bus.search.subscribeThrottled {

                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(FADE)
                            .replace(mainContainerId, EvaSearchFragment, it)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },
                bus.openDirectory.subscribeThrottled {


                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it.animation)
                            .replace(mainContainerId, EvaDirectoryFragment, it)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },
                bus.openQuotes.subscribeThrottled {


                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it.animation)
                            .replace(mainContainerId, EvaQuotesFragment, it.quoteId)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },
                bus.openBreviaryChooser.subscribeThrottled {


                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it)
                            .replace(mainContainerId, BreviaryChooserFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },
                bus.openBreviaryContent.subscribeThrottled {


                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it.animation)
                            .replace(mainContainerId, BreviaryContentFragment, it.breviaryId)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },

                bus.openInfo.subscribeThrottled {


                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it)
                            .replace(mainContainerId, EvaInfoFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },

                bus.openOptionsDrawer.subscribeThrottled {
                    evaRoot.openDrawer(GravityCompat.END)
                },

                bus.openPrayerBook.subscribeThrottled {

                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it)
                            .replace(mainContainerId, PrayerBookFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },

                bus.openPrayerCategory.subscribeThrottled {

                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it.animation)
                            .replace(mainContainerId, PrayerListFragment, it.prayerCategory.id)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },

                bus.openRadio.subscribeThrottled {
                    //todo
                },

                bus.openCalendar.subscribeThrottled {


                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it)
                            .replace(mainContainerId, EvaCalendarFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },

                bus.openBookmarks.subscribeThrottled {

                    supportFragmentManager.beginTransaction()
                            .setCustomAnimation(it)
                            .replace(mainContainerId, EvaBookmarksFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                },

                dashboardBackgroundUrlSubject
                        .distinctUntilChanged()
                        .subscribe {
                            NovaEvaApp.imageLoader.loadImage(it, object : SimpleImageLoadingListener() {
                                override fun onLoadingComplete(imageUri: String, view: View?, loadedImage: Bitmap) {
                                    val drawable = BitmapDrawable(resources, loadedImage)
                                    RxEventBus.changeDashboardBackground.onNext(drawable)
                                }
                            })
                        },

                breviaryImageUrlSubject
                        .distinctUntilChanged()
                        .subscribe {
                            NovaEvaApp.prefs.edit().putString("hr.bpervan.novaeva.brevijarheaderimage", it).apply()
                        },

                bus.connectedToNetwork.subscribe {
                    fetchBreviaryImageUrl()
                    fetchDashboardBackgroundUrl()
                }
        )

        fetchBreviaryImageUrl()
        fetchDashboardBackgroundUrl()
    }

    private fun fetchBreviaryImageUrl() {
        NovaEvaService.instance
                .getDirectoryContent(546, null)
                .subscribeAsync({ directoryContent ->
                    val image = directoryContent.image?.size640 ?: directoryContent.image?.size640
                    if (image != null) {
                        breviaryImageUrlSubject.onNext(image)
                    }
                }, onError = {})
    }

    private fun fetchDashboardBackgroundUrl() {
        NovaEvaService.instance
                .getDashboardBackground(RxEventBus.changeEvaTheme.value!!)
                .subscribeAsync(dashboardBackgroundUrlSubject::onNext, onError = {})
    }

    private fun openDashboardFragment(animation: TransitionAnimation = FADE) {
        popAllFragments()
        supportFragmentManager.beginTransaction()
                .setCustomAnimation(animation)
                .replace(mainContainerId, EvaDashboardFragment)
                .commitAllowingStateLoss()
    }

    private fun openContentFragment(request: OpenContentEvent) {

        supportFragmentManager.beginTransaction()
                .setCustomAnimation(request.animation)
                .replace(mainContainerId, EvaContentFragment, request)
                .addToBackStack(null)
                .commitAllowingStateLoss()
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
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            else -> this
        }
    }

    private fun <T : Fragment> FragmentTransaction.replace(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, Unit>): FragmentTransaction {
        return this.replace(containerViewId, evaFragmentFactory.newInstance(Unit), evaFragmentFactory.tag)
    }

    private fun <T : Fragment, K> FragmentTransaction.replace(
            containerViewId: Int,
            evaFragmentFactory: EvaBaseFragment.EvaFragmentFactory<T, K>,
            fragmentInitializer: K): FragmentTransaction {
        return this.replace(containerViewId, evaFragmentFactory.newInstance(fragmentInitializer), evaFragmentFactory.tag)
    }
}