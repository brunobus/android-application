package hr.bpervan.novaeva

import android.graphics.drawable.Drawable
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.OpenBreviaryContentEvent
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.model.OpenDirectoryEvent
import hr.bpervan.novaeva.model.OpenPrayerDirectoryEvent
import hr.bpervan.novaeva.model.OpenQuotesEvent
import hr.bpervan.novaeva.player.EvaPlayer
import hr.bpervan.novaeva.util.TransitionAnimation
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 *
 */
object EventPipelines {

    //navigation
    val goHome = PublishSubject.create<TransitionAnimation>()
    val openContent = PublishSubject.create<OpenContentEvent>()
    val openDirectory = PublishSubject.create<OpenDirectoryEvent>()
    val openQuotes = PublishSubject.create<OpenQuotesEvent>()
    val openBreviaryChooser = PublishSubject.create<TransitionAnimation>()
    val openBreviaryContent = PublishSubject.create<OpenBreviaryContentEvent>()
    val openInfo = PublishSubject.create<TransitionAnimation>()
    val toggleOptionsDrawer = PublishSubject.create<Unit>()
    val openPrayerBook = PublishSubject.create<OpenPrayerDirectoryEvent>()
    val openPrayerCategory = PublishSubject.create<OpenPrayerDirectoryEvent>()
    val openRadio = PublishSubject.create<Unit>()
    val openCalendar = PublishSubject.create<TransitionAnimation>()
    val openBookmarks = PublishSubject.create<TransitionAnimation>()

    //appearance
    val changeWindowBackgroundDrawable = BehaviorSubject.create<Drawable>()
    val changeFragmentBackgroundResource = BehaviorSubject.create<Int>()
    val changeNavbarColor = BehaviorSubject.create<Int>()
    val changeStatusbarColor = BehaviorSubject.create<Int>()

    //themes
    val dashboardBackground: BehaviorSubject<Drawable> by lazy {
        BehaviorSubject.createDefault(NovaEvaApp.defaultDashboardBackground)
    }

    //action
    val search = PublishSubject.create<String>()
    val chooseRadioStation = PublishSubject.create<EvaContent>()
    val playAnyRadioStation = PublishSubject.create<Unit>()
    val resizeText = PublishSubject.create<Unit>()

    //event
    val playbackStartStopPause = BehaviorSubject.create<EvaPlayer.PlaybackChange>()
}

