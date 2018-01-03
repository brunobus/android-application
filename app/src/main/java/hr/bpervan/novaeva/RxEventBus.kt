package hr.bpervan.novaeva

import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.EvaDirectoryMetadata
import hr.bpervan.novaeva.model.PrayerCategory
import io.reactivex.subjects.PublishSubject

/**
 * Created by vpriscan on 07.10.17..
 */
object RxEventBus {
    val frontTextChange = PublishSubject.create<String>()
    val directoryOpenRequest = PublishSubject.create<EvaDirectoryMetadata>()
    val contentOpenRequest = PublishSubject.create<EvaContentMetadata>()

    val prayerCategoryOpenRequest = PublishSubject.create<PrayerCategory>()
}