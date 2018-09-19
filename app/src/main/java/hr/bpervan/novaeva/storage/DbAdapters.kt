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

    fun deleteDirectoryContent(realm: Realm, directoryId: Long) {
        realm.executeTransaction { realmInTrans ->
            val directory = realmInTrans.where<EvaDirectory>().equalTo(DIRECTORY_ID_FIELD, directoryId).findFirst()
            directory?.apply {
                subDirectoriesList.clear()
                contentsList.clear()
            }
        }
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
                                         valuesApplier: (EvaDirectory) -> Unit = {},
                                         onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            val evaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)
            if (evaDirectory == null) {
                val defaultDirectory = EvaDirectory(directoryId).apply(valuesApplier)
                realmInTrans.insertOrUpdate(defaultDirectory)
            }
        }, onSuccess)
    }

    fun addOrUpdateEvaDirectoryAsync(realm: Realm, evaDirectoryDTO: EvaDirectoryDTO,
                                     onSuccess: () -> Unit = {}) {

        realm.executeTransactionAsync({ realmInTrans ->

            val directoryId = evaDirectoryDTO.directoryId

            val existingEvaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)

            val evaDirectory = if (existingEvaDirectory != null) {
                if (existingEvaDirectory.image?.timestamp != evaDirectoryDTO.image?.timestamp) {
                    val newPic = evaDirectoryDTO.image?.toDbModel()
                    if (newPic != null) {
                        existingEvaDirectory.image = realmInTrans.copyToRealm(newPic)
                    }
                }
                existingEvaDirectory
            } else {
                EvaDirectory(directoryId, evaDirectoryDTO.categoryId,
                        evaDirectoryDTO.title ?: "", evaDirectoryDTO.image?.toDbModel())
            }

            val newContentMetadata = evaDirectoryDTO.contentMetadataList.map {
                it.directoryId = evaDirectoryDTO.directoryId
                it.categoryId = evaDirectoryDTO.categoryId
                it.toDbModel(EvaContentDbAdapter.loadEvaContent(realmInTrans, it.contentId))
            }

            val newSubDirectoryMetadata = evaDirectoryDTO.subDirectoryMetadataList.map {
                it.categoryId = evaDirectoryDTO.categoryId
                it.toDbModel(EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, it.directoryId))
            }

            newContentMetadata.forEach { candidate ->

                realmInTrans.copyToRealmOrUpdate(candidate)

                evaDirectory.contentsList.addIfNoneExistingMatch(candidate) { existing ->
                    candidate.contentId == existing.contentId
                }
            }

            newSubDirectoryMetadata.forEach { candidate ->

                realmInTrans.copyToRealmOrUpdate(candidate)

                evaDirectory.subDirectoriesList.addIfNoneExistingMatch(candidate) { existing ->
                    candidate.directoryId == existing.directoryId
                }
            }

            realmInTrans.insertOrUpdate(evaDirectory)
        }, onSuccess)
    }
}

object EvaContentDbAdapter {

    fun loadEvaContent(realm: Realm, contentId: Long): EvaContent? {
        return realm.where<EvaContent>().equalTo(CONTENT_ID_FIELD, contentId).findFirst()
    }

    fun addOrUpdateEvaContentAsync(realm: Realm,
                                   evaContentDTO: EvaContentDTO,
                                   onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            val evaContent = loadEvaContent(realmInTrans, evaContentDTO.contentId)
            val updated = evaContentDTO.toDbModel(mergeWith = evaContent)
            realmInTrans.insertOrUpdate(updated)
        }, onSuccess)
    }

    fun updateEvaContentIfExistsAsync(realm: Realm,
                                      contentId: Long,
                                      updateFunc: (EvaContent) -> Unit,
                                      onSuccess: () -> Unit) {
        realm.executeTransactionAsync({ realmInTrans ->
            loadEvaContent(realmInTrans, contentId)?.let(updateFunc)
        }, onSuccess)
    }

    fun loadManyEvaContentMetadata(realm: Realm,
                                   predicate: (EvaContent) -> Boolean,
                                   subscriber: (EvaContent) -> Unit): Disposable? {
        return realm
                .where<EvaContent>()
                .loadManyAsync(predicate, subscriber)
    }
}