package hr.bpervan.novaeva

import android.graphics.drawable.Drawable
import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.TransitionAnimation
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 *
 */
object EventPipelines {

    val chooseRadioStation = PublishSubject.create<EvaContentMetadata>()

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

    //appearance
    val changeWindowBackgroundDrawable = BehaviorSubject.create<Drawable>()
    val changeFragmentBackgroundResource = BehaviorSubject.create<Int>()
    val changeNavbarColor = BehaviorSubject.create<Int>()
    val changeStatusbarColor = BehaviorSubject.create<Int>()

    //themes
    val dashboardBackground: BehaviorSubject<Drawable> by lazy {
        BehaviorSubject.createDefault(NovaEvaApp.defaultDashboardBackground)
    }
    val evaTheme: BehaviorSubject<EvaTheme> by lazy {
        BehaviorSubject.createDefault(NovaEvaApp.getDefaultEvaTheme())
    }

    //action
    val search = PublishSubject.create<String>()

    //network
    val connectedToNetwork = PublishSubject.create<Unit>()
}

