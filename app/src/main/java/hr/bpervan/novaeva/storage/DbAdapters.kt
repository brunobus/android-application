package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.util.addIfNoneExistingMatch
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.kotlin.where

/**
 * Created by vpriscan on 19.10.17..
 */

object EvaDirectoryDbAdapter {

    private fun loadEvaDirectoryMetadata(realm: Realm, directoryId: Long): EvaDirectoryMetadata? {
        return realm.where<EvaDirectoryMetadata>().equalTo(DIRECTORY_ID_FIELD, directoryId).findFirst()
    }

    private fun loadEvaDirectory(realm: Realm, directoryId: Long): EvaDirectory? {
        return realm.where<EvaDirectory>().equalTo(DIRECTORY_ID_FIELD, directoryId).findFirst()
    }

    fun loadEvaDirectoryAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory?) -> Unit): Disposable? {
        return realm.where<EvaDirectory>().loadOneAsync(DIRECTORY_ID_FIELD, directoryId, directoryConsumer)
    }

    fun subscribeToEvaDirectoryUpdatesAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory) -> Unit): Disposable? {
        return realm.where<EvaDirectory>().subscribeToUpdatesAsync(DIRECTORY_ID_FIELD, directoryId, directoryConsumer)
    }

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
                                     newContentMetadata: List<EvaContentMetadata>,
                                     newSubDirectoryMetadata: List<EvaDirectoryMetadata>,
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

            newContentMetadata.forEach {
                val contentInDB = realmInTrans.where<EvaContentMetadata>().equalTo(CONTENT_ID_FIELD, it.contentId).findFirst()
                it.bookmark = contentInDB?.bookmark ?: false
                realmInTrans.copyToRealmOrUpdate(it)

                evaDirectory.contentMetadataList.addIfNoneExistingMatch(it) { existing ->
                    it.contentId == existing.contentId
                }
            }

            //val contentMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(newContentMetadataSupplier())

            val subDirectoryMetadataToAddManaged = realmInTrans.copyToRealmOrUpdate(newSubDirectoryMetadata)

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
    fun loadEvaContentMetadata(realm: Realm, contentId: Long): EvaContentMetadata? {
        return realm.where<EvaContentMetadata>().equalTo(CONTENT_ID_FIELD, contentId).findFirst()
    }

    private fun loadEvaContent(realm: Realm, contentId: Long): EvaContent? {
        return realm.where<EvaContent>().equalTo(CONTENT_ID_FIELD, contentId).findFirst()
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

    fun subscribeToEvaContentUpdatesAsync(realm: Realm, contentId: Long, contentConsumer: (EvaContent) -> Unit): Disposable? {
        return realm.where<EvaContent>().subscribeToUpdatesAsync(CONTENT_ID_FIELD, contentId, contentConsumer)
    }

    fun subscribeToEvaContentMetadataUpdatesAsync(realm: Realm, contentId: Long,
                                                  contentMetadataConsumer: (EvaContentMetadata) -> Unit): Disposable? {
        return realm.where<EvaContentMetadata>().subscribeToUpdatesAsync(CONTENT_ID_FIELD, contentId, contentMetadataConsumer)
    }

    fun addOrUpdateEvaContentAsync(realm: Realm,
                                   evaContent: EvaContent) {
        realm.executeTransactionAsync { realmInTrans ->
            if (evaContent.contentMetadata == null) {
                evaContent.contentMetadata =
                        loadEvaContentMetadata(realmInTrans, evaContent.contentId) ?: EvaContentMetadata(evaContent.contentId)
            }
            realmInTrans.insertOrUpdate(evaContent)
        }
    }

    fun updateEvaContentMetadataAsync(realm: Realm, evaContentId: Long,
                                      updateFunction: (EvaContentMetadata) -> Unit,
                                      onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            loadEvaContentMetadata(realmInTrans, evaContentId)?.let(updateFunction)
        }, onSuccess)
    }

    fun loadManyEvaContentMetadata(realm: Realm,
                                   predicate: (EvaContentMetadata) -> Boolean,
                                   subscriber: (EvaContentMetadata) -> Unit): Disposable? {
        return realm
                .where<EvaContentMetadata>()
                .loadManyAsync(predicate, subscriber)
    }
}