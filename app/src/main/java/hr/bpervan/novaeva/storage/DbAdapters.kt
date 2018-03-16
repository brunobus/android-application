package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.utilities.addIfNoneExistingMatch
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.*

/**
 * Created by vpriscan on 19.10.17..
 */

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

    //todo fix leak?
    fun createIfMissingEvaDirectoryAsync(realm: Realm, directoryId: Long,
                                         valuesApplier: (EvaDirectoryMetadata) -> Unit = {},
                                         onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            val evaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)
            if (evaDirectory == null) {
                val directoryMetadata = loadEvaDirectoryMetadata(realmInTrans, directoryId)
                        ?: EvaDirectoryMetadata(directoryId).apply(valuesApplier)
                val defaultDirectory = EvaDirectory(directoryId, directoryMetadata)
                realmInTrans.insertOrUpdate(defaultDirectory)
            }
        }, onSuccess)
    }

    fun addOrUpdateEvaDirectoryAsync(realm: Realm, directoryId: Long,
                                     newContentMetadataSupplier: () -> List<EvaContentMetadata>,
                                     newSubDirectoryMetadataSupplier: () -> List<EvaDirectoryMetadata>,
                                     onSuccess: () -> Unit = {}) {

        realm.executeTransactionAsync({ realmInTrans ->

            val existingEvaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)

            val evaDirectory = if (existingEvaDirectory != null) {
                existingEvaDirectory
            } else {
                val directoryMetadata = loadEvaDirectoryMetadata(realmInTrans, directoryId)
                        ?: EvaDirectoryMetadata(directoryId)
                EvaDirectory(directoryId, directoryMetadata)
            }

            newContentMetadataSupplier().forEach {
                var contentInDB = realmInTrans.where(it.javaClass).equalTo(CONTENT_ID_FIELD, it.contentId).findFirst()

                var bookmark = contentInDB?.bookmark ?: false
                it.bookmark = bookmark
                realmInTrans.copyToRealmOrUpdate(it);

                evaDirectory.contentMetadataList.addIfNoneExistingMatch(it) { existing ->
                    it.contentId == existing.contentId
                }
            }

            //val contentMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(newContentMetadataSupplier())

            val subDirectoryMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(newSubDirectoryMetadataSupplier())

            //contentMetadataToAddManaged.forEach { candidate ->
            //    evaDirectory.contentMetadataList.addIfNoneExistingMatch(candidate) { existing ->
            //        candidate.contentId == existing.contentId
            //    }
            //}

            subDirectoryMetadataToAddManaged.forEach { candidate ->
                evaDirectory.subDirectoryMetadataList.addIfNoneExistingMatch(candidate) { existing ->
                    candidate.directoryId == existing.directoryId
                }
            }

            realmInTrans.insertOrUpdate(evaDirectory)
        }, onSuccess)
    }
}

object EvaContentDbAdapter {
    private fun loadEvaContentMetadata(realm: Realm, contentId: Long): EvaContentMetadata? {
        return loadOne(realm, EvaContentMetadata::class.java, CONTENT_ID_FIELD, contentId)
    }

    private fun loadEvaContent(realm: Realm, contentId: Long): EvaContent? {
        return loadOne(realm, EvaContent::class.java, CONTENT_ID_FIELD, contentId)
    }

    fun createIfMissingEvaContentAsync(realm: Realm, contentId: Long,
                                       valuesApplier: (EvaContentMetadata) -> Unit = {},
                                       onSuccess: () -> Unit) {
        realm.executeTransactionAsync({ realmInTrans ->
            val evaContent = EvaContentDbAdapter.loadEvaContent(realmInTrans, contentId)
            if (evaContent == null) {
                val contentMetadata = loadEvaContentMetadata(realmInTrans, contentId)
                        ?: EvaContentMetadata(contentId).apply(valuesApplier)
                val defaultContent = EvaContent(contentId, contentMetadata)
                realmInTrans.insertOrUpdate(defaultContent)
            }
        }, onSuccess)
    }

    fun subscribeToEvaContentUpdatesAsync(realm: Realm, contentId: Long, contentConsumer: (EvaContent) -> Unit): Disposable {
        return subscribeToUpdatesAsync(realm, EvaContent::class.java, CONTENT_ID_FIELD, contentId, contentConsumer)
    }

    fun subscribeToEvaContentMetadataUpdatesAsync(realm: Realm, contentId: Long,
                                                  contentMetadataConsumer: (EvaContentMetadata) -> Unit): Disposable {
        return subscribeToUpdatesAsync(realm, EvaContentMetadata::class.java, CONTENT_ID_FIELD, contentId, contentMetadataConsumer)
    }

    fun addOrUpdateEvaContent(realm: Realm, evaContent: EvaContent) {
        realm.executeTransactionAsync { realmInTrans ->
            if (evaContent.contentMetadata == null) {
                evaContent.contentMetadata =
                        loadEvaContentMetadata(realmInTrans, evaContent.contentId) ?: EvaContentMetadata(evaContent.contentId)
            }
            realmInTrans.insertOrUpdate(evaContent)
        }
    }

    fun updateEvaContentMetadataAsync(realm: Realm, evaContentId: Long, updateFunction: (EvaContentMetadata) -> Unit) {
        realm.executeTransactionAsync({ realmInTrans ->
            loadEvaContentMetadata(realmInTrans, evaContentId)?.let(updateFunction)
        })
    }

    fun loadManyEvaContentMetadata(realm: Realm, predicate: (EvaContentMetadata) -> Boolean,
                                   subscriber: (EvaContentMetadata) -> Unit): Disposable {
        return loadManyAsync(realm, EvaContentMetadata::class.java, predicate, subscriber)
    }
}