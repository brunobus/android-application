package hr.bpervan.novaeva

import com.google.android.exoplayer2.ExoPlayer
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.TransitionAnimation
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 *
 */
object RxEventBus {
    //internal
    val setActiveExoPlayer = PublishSubject.create<ExoPlayer>()!!
    val didSetActiveExoPlayer = BehaviorSubject.create<ExoPlayer>()!!

    //navigation
    val goHome = PublishSubject.create<TransitionAnimation>()!!
    val openContent = PublishSubject.create<OpenContentEvent>()!!
    val openDirectory = PublishSubject.create<OpenDirectoryEvent>()!!
    val openQuotes = PublishSubject.create<OpenQuotesEvent>()!!
    val openBreviaryChooser = PublishSubject.create<TransitionAnimation>()!!
    val openBreviaryContent = PublishSubject.create<OpenBreviaryContentEvent>()!!
    val openInfo = PublishSubject.create<TransitionAnimation>()!!
    val openSettingsDrawer = PublishSubject.create<TransitionAnimation>()!!
    val openPrayerBook = PublishSubject.create<TransitionAnimation>()!!
    val openPrayerCategory = PublishSubject.create<OpenPrayerCategoryEvent>()!!
    val openRadio = PublishSubject.create<TransitionAnimation>()!!
    val openCalendar = PublishSubject.create<TransitionAnimation>()!!
    val openBookmarks = PublishSubject.create<TransitionAnimation>()!!

    //ux
    val replaceAppBackground = PublishSubject.create<BackgroundReplaceEvent>()!! //splash and transitions between fragments
    val replaceDashboardBackground = PublishSubject.create<BackgroundReplaceEvent>()!!

    //action
    val search = PublishSubject.create<String>()!!
}

