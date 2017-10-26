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

    private fun loadEvaDirectoryMetadata(realm: Realm, directoryId: Long): EvaDirectoryMetadata? =
            loadOne(realm, EvaDirectoryMetadata::class.java, DIRECTORY_ID_FIELD, directoryId)

    private fun loadEvaDirectory(realm: Realm, directoryId: Long): EvaDirectory? =
            loadOne(realm, EvaDirectory::class.java, DIRECTORY_ID_FIELD, directoryId)

    fun loadEvaDirectoryAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory?) -> Unit) {
        loadOneAsync(realm, EvaDirectory::class.java, DIRECTORY_ID_FIELD, directoryId, directoryConsumer)
    }

    fun subscribeToEvaDirectoryUpdatesAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory) -> Unit): Disposable =
            subscribeToUpdatesAsync(realm, EvaDirectory::class.java, DIRECTORY_ID_FIELD, directoryId, directoryConsumer)

    fun createIfMissingEvaDirectoryAsync(realm: Realm, directoryId: Long, defaultSupplier: () -> EvaDirectory,
                                         onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            val evaDir = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)
            if (evaDir == null) {
                val suppliedEvaDirectory = defaultSupplier()
                if (suppliedEvaDirectory.directoryMetadata == null) {
                    val evaDirectoryMetadata = loadEvaDirectoryMetadata(realmInTrans, directoryId)
                    suppliedEvaDirectory.directoryMetadata = evaDirectoryMetadata!!
                }
                realmInTrans.insertOrUpdate(suppliedEvaDirectory)
            }
        }, onSuccess)
    }

    fun updateIfExistsEvaDirectoryAsync(realm: Realm, directoryId: Long,
                                        newContentMetadataSupplier: () -> List<EvaContentMetadata>,
                                        newSubDirectoryMetadataSupplier: () -> List<EvaDirectoryMetadata>,
                                        onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->

            val evaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)
            if (evaDirectory != null) {

                val contentMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(newContentMetadataSupplier())
                val subDirectoryMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(newSubDirectoryMetadataSupplier())

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
    private fun loadEvaContentMetadata(realm: Realm, contentId: Long): EvaContentMetadata? =
            loadOne(realm, EvaContentMetadata::class.java, CONTENT_ID_FIELD, contentId)

    fun subscribeToEvaContentUpdatesAsync(realm: Realm, contentId: Long, contentConsumer: (EvaContent) -> Unit): Disposable =
            subscribeToUpdatesAsync(realm, EvaContent::class.java, CONTENT_ID_FIELD, contentId, contentConsumer)

    fun addOrUpdateEvaContent(realm: Realm, evaContent: EvaContent) {
        realm.executeTransactionAsync { realmInTrans ->
            if (evaContent.contentMetadata == null) {
                val evaContentMetadata = loadEvaContentMetadata(realmInTrans, evaContent.contentId)
                evaContent.contentMetadata = evaContentMetadata!!
            }
            realmInTrans.insertOrUpdate(evaContent)
        }
    }
}