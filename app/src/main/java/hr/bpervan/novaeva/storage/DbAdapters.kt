package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.addIfNoneExistingMatch
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.*

/**
 * Created by vpriscan on 19.10.17..
 */

object EvaBookmarkDbAdapter {
    fun loadEvaBookmarksAsync(realm: Realm, bookmarksConsumer: (RealmResults<EvaContentMetadata>) -> Unit) {
        realm.where(EvaContentMetadata::class.java)
                .findAllSortedAsync(TIMESTAMP_FIELD, Sort.DESCENDING)
                .asFlowable()
                .filter { it.isLoaded }
                .firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bookmarksConsumer, ::handleError)
    }

    fun storeEvaBookmarkAsync(realm: Realm, bookmark: EvaContentMetadata) {
        realm.executeTransactionAsync { realmInTrans ->
            realmInTrans.insertOrUpdate(bookmark)
        }
    }
}

object EvaDirectoryDbAdapter {

    fun loadEvaDirectory(realm: Realm, directoryId: Long): EvaDirectory? {
        return realm.where(EvaDirectory::class.java)
                .equalTo(DIRECTORY_ID_FIELD, directoryId)
                .findFirst()
    }

    fun loadEvaDirectoryAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory?) -> Unit) {
        loadNullableOnceAsync(realm, EvaDirectory::class.java, DIRECTORY_ID_FIELD, directoryId, directoryConsumer)
    }

    fun subscribeToEvaDirectoryChangesAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory) -> Unit): Disposable =
            subscribeToChangesAsync(realm, EvaDirectory::class.java, DIRECTORY_ID_FIELD, directoryId, directoryConsumer)

    fun createIfMissingEvaDirectoryAsync(realm: Realm, directoryId: Long, defaultSupplier: () -> EvaDirectory,
                                         onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            val evaDir = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)
            if (evaDir == null) {
                realmInTrans.insertOrUpdate(defaultSupplier())
            }
        }, onSuccess)
    }

    fun updateIfExistsEvaDirectoryAsync(realm: Realm, directoryId: Long,
                                        contentMetadataToAdd: List<EvaContentMetadata>,
                                        subDirectoryMetadataToAdd: List<EvaDirectoryMetadata>,
                                        onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            val contentMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(contentMetadataToAdd)
            val subDirectoryMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(subDirectoryMetadataToAdd)

            val evaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realm, directoryId)
            if (evaDirectory != null) {
                contentMetadataToAddManaged.forEach { candidate ->
                    evaDirectory.contentMetadataList.addIfNoneExistingMatch(candidate) { existing ->
                        candidate.contentId == existing.contentId
                    }
                }
                subDirectoryMetadataToAddManaged.forEach { candidate ->
                    evaDirectory.subDirectoryMetadataList.addIfNoneExistingMatch(candidate) { existing ->
                        candidate.directoryId == existing.directoryId
                    }
                }
            }
        }, onSuccess)
    }
}

object EvaContentDbAdapter {
    fun loadEvaContentMetadataAsync(realm: Realm, contentId: Long, contentMetadataConsumer: (EvaContentMetadata) -> Unit) {
        loadOnceAsync(realm, EvaContentMetadata::class.java, CONTENT_ID_FIELD, contentId, contentMetadataConsumer)
    }

    fun subscribeToEvaContentChangesAsync(realm: Realm, contentId: Long, contentConsumer: (EvaContent) -> Unit): Disposable =
            subscribeToChangesAsync(realm, EvaContent::class.java, CONTENT_ID_FIELD, contentId, contentConsumer)

    fun storeEvaContent(realm: Realm, evaContent: EvaContent) {
        realm.executeTransactionAsync { realmInTrans ->
            realmInTrans.insertOrUpdate(evaContent)
        }
    }
}