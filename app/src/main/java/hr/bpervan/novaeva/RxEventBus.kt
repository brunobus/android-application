package hr.bpervan.novaeva

import android.content.Intent
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.EvaDirectoryMetadata
import io.reactivex.subjects.PublishSubject

/**
 * Created by vpriscan on 07.10.17..
 */
class RxEventBus {
    val frontTextChange = PublishSubject.create<String>()
    val directoryOpenRequest = PublishSubject.create<EvaDirectoryMetadata>()
    val contentOpenRequest = PublishSubject.create<EvaContentMetadata>()
}