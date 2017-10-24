package hr.bpervan.novaeva

import hr.bpervan.novaeva.model.EvaContentInfo
import hr.bpervan.novaeva.model.EvaDirectoryInfo
import io.reactivex.subjects.PublishSubject

/**
 * Created by vpriscan on 07.10.17..
 */
class RxEventBus {
    val directoryOpenRequest = PublishSubject.create<EvaDirectoryInfo>()
    val contentOpenRequest = PublishSubject.create<EvaContentInfo>()


}