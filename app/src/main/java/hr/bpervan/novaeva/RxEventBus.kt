package hr.bpervan.novaeva

import hr.bpervan.novaeva.model.ContentInfo
import hr.bpervan.novaeva.model.DirectoryInfo
import io.reactivex.subjects.PublishSubject

/**
 * Created by vpriscan on 07.10.17..
 */
class RxEventBus {
    val directoryOpenRequest = PublishSubject.create<DirectoryInfo>()
    val contentOpenRequest = PublishSubject.create<ContentInfo>()
}