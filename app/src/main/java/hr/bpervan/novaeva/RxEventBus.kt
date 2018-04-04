package hr.bpervan.novaeva

import android.graphics.drawable.Drawable
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.TransitionAnimation
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 *
 */
object RxEventBus {

    val playRadioStream = PublishSubject.create<RadioStation>()

    //navigation
    val goHome = PublishSubject.create<TransitionAnimation>()
    val openContent = PublishSubject.create<OpenContentEvent>()
    val openDirectory = PublishSubject.create<OpenDirectoryEvent>()
    val openQuotes = PublishSubject.create<OpenQuotesEvent>()
    val openBreviaryChooser = PublishSubject.create<TransitionAnimation>()
    val openBreviaryContent = PublishSubject.create<OpenBreviaryContentEvent>()
    val openInfo = PublishSubject.create<TransitionAnimation>()
    val openOptionsDrawer = PublishSubject.create<Unit>()
    val openPrayerBook = PublishSubject.create<TransitionAnimation>()
    val openPrayerCategory = PublishSubject.create<OpenPrayerCategoryEvent>()
    val openRadio = PublishSubject.create<Unit>()
    val openCalendar = PublishSubject.create<TransitionAnimation>()
    val openBookmarks = PublishSubject.create<TransitionAnimation>()

    //themes
    val changeDashboardBackground: BehaviorSubject<Drawable> by lazy {
        BehaviorSubject.createDefault(NovaEvaApp.getDefaultAppBackground())
    }
    val changeEvaTheme: BehaviorSubject<EvaTheme> by lazy {
        BehaviorSubject.createDefault(NovaEvaApp.getDefaultEvaTheme())
    }

    //action
    val search = PublishSubject.create<String>()

    //network
    val connectedToNetwork = PublishSubject.create<Unit>()
}

