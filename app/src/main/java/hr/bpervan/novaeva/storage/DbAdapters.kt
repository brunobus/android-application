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
                    val newPic = evaDirectoryDTO.image?.toDatabaseModel()
                    if (newPic != null) {
                        existingEvaDirectory.image = realmInTrans.copyToRealm(newPic)
                    }
                }
                existingEvaDirectory
            } else {
                EvaDirectory(directoryId, evaDirectoryDTO.categoryId, ""/*todo*/, evaDirectoryDTO.image?.toDatabaseModel())
            }

            val newContentMetadata = evaDirectoryDTO.contentMetadataList.map {
                it.directoryId = evaDirectoryDTO.directoryId
                it.categoryId = evaDirectoryDTO.categoryId
                it.toDatabaseModel()
            }
            val newSubDirectoryMetadata = evaDirectoryDTO.subDirectoryMetadataList.map {
                it.categoryId = evaDirectoryDTO.categoryId
                it.toDatabaseModel()
            }

            newContentMetadata.forEach {
                val contentInDB = realmInTrans.where<EvaContent>().equalTo(CONTENT_ID_FIELD, it.contentId).findFirst()
                it.bookmarked = contentInDB?.bookmarked ?: false

                realmInTrans.copyToRealmOrUpdate(it)

                evaDirectory.contentsList.addIfNoneExistingMatch(it) { existing ->
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
                                   evaContent: EvaContent,
                                   onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            realmInTrans.insertOrUpdate(evaContent)
        }, onSuccess)
    }

    fun updateEvaContentAsync(realm: Realm, contentId: Long,
                              updateFunction: (EvaContent) -> Unit,
                              onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->
            loadEvaContent(realmInTrans, contentId)?.let(updateFunction)
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