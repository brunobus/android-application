package hr.bpervan.novaeva

import com.google.android.exoplayer2.ExoPlayer
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.EvaDirectoryMetadata
import hr.bpervan.novaeva.model.PrayerCategory
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Created by vpriscan on 07.10.17..
 */
object RxEventBus {
    val setActiveExoPlayer = PublishSubject.create<ExoPlayer>()!!
    val didSetActiveExoPlayer = BehaviorSubject.create<ExoPlayer>()!!

    val frontTextChange = PublishSubject.create<String>()!!

    val goHomeRequest = PublishSubject.create<Unit>()!!
    val searchRequest = PublishSubject.create<String>()!!
    //itd

    val directoryOpenRequest = PublishSubject.create<EvaDirectoryMetadata>()!!
    val contentOpenRequest = PublishSubject.create<EvaContentMetadata>()!!
    val prayerCategoryOpenRequest = PublishSubject.create<PrayerCategory>()!!
}