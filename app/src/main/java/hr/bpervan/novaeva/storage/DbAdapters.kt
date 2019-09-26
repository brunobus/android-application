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
                subCategories.clear()
                contents.clear()
            }
        }
    }

    private fun loadEvaDirectory(realm: Realm, directoryId: Long): EvaDirectory? {
        return realm.where<EvaDirectory>().equalTo(DIRECTORY_ID_FIELD, directoryId).findFirst()
    }

    fun loadEvaDirectoryAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory?) -> Unit): Disposable {
        return realm.where<EvaDirectory>().loadOneAsync(DIRECTORY_ID_FIELD, directoryId, directoryConsumer)
    }

    fun subscribeToEvaDirectoryUpdatesAsync(realm: Realm, directoryId: Long, directoryConsumer: (EvaDirectory) -> Unit): Disposable {
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

    fun addOrUpdateEvaCategoryAsync(realm: Realm, evaCategoryDto: CategoryDto, onSuccess: () -> Unit = {}) {
        realm.executeTransactionAsync({ realmInTrans ->

            val directoryId = evaCategoryDto.id

            val existingEvaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)

            val evaDirectory = if (existingEvaDirectory != null) {
                val dtoFirstImage = evaCategoryDto.images?.firstOrNull()
                if (existingEvaDirectory.image?.url != dtoFirstImage?.link) {
                    val newPic = dtoFirstImage?.toDbModel()
                    if (newPic != null) {
                        existingEvaDirectory.image = realmInTrans.copyToRealm(newPic)
                    }
                }
                existingEvaDirectory
            } else {
                EvaDirectory(
                        id = directoryId,
                        domain = evaCategoryDto.domain?.toString(),
                        title = evaCategoryDto.title ?: "",
                        image = evaCategoryDto.images?.firstOrNull()?.toDbModel())
            }

            evaCategoryDto.content.orEmpty()
                    .map {
                        it.domain = evaCategoryDto.domain
                        it.toDbModel(EvaContentDbAdapter.loadEvaContent(realmInTrans, contentId = it.id))
                    }
                    .forEach { candidate ->

                        realmInTrans.copyToRealmOrUpdate(candidate)

                        evaDirectory.contents.addIfNoneExistingMatch(candidate) { existing ->
                            candidate.id == existing.id
                        }
                    }

            evaCategoryDto.subcategories.orEmpty()
                    .map {
                        it.domain = evaCategoryDto.domain
                        it.toDbModel(EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId = it.id))
                    }
                    .forEach { candidate ->

                        realmInTrans.copyToRealmOrUpdate(candidate)

                        evaDirectory.subCategories.addIfNoneExistingMatch(candidate) { existing ->
                            candidate.id == existing.id
                        }
                    }

            realmInTrans.insertOrUpdate(evaDirectory)
        }, onSuccess)
    }

    @Deprecated("legacy")
    fun addOrUpdateEvaDirectoryAsync_legacy(realm: Realm, evaDirectoryDTO: EvaDirectoryDTO,
                                            onSuccess: () -> Unit = {}) {

        realm.executeTransactionAsync({ realmInTrans ->

            val directoryId = evaDirectoryDTO.directoryId

            val existingEvaDirectory = EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, directoryId)

            val evaDirectory = if (existingEvaDirectory != null) {
                val candidateImage = evaDirectoryDTO.image
                val imageURL = candidateImage?.original
                        ?: candidateImage?.size720
                        ?: candidateImage?.size640

                if (existingEvaDirectory.image?.url != imageURL) {
                    val newPic = evaDirectoryDTO.image?.toDbModel()
                    if (newPic != null) {
                        existingEvaDirectory.image = realmInTrans.copyToRealm(newPic)
                    }
                }
                existingEvaDirectory
            } else {
                EvaDirectory(
                        id = directoryId,
                        domain = evaDirectoryDTO.domain?.toString(),
                        title = evaDirectoryDTO.title ?: "",
                        image = evaDirectoryDTO.image?.toDbModel())
            }

            evaDirectoryDTO.contentMetadataList
                    .map {
                        it.directoryId = evaDirectoryDTO.directoryId
                        it.domain = evaDirectoryDTO.domain
                        it.toDbModel(EvaContentDbAdapter.loadEvaContent(realmInTrans, it.contentId))
                    }
                    .forEach { candidate ->

                        realmInTrans.copyToRealmOrUpdate(candidate)

                        evaDirectory.contents.addIfNoneExistingMatch(candidate) { existing ->
                            candidate.id == existing.id
                        }
                    }

            evaDirectoryDTO.subDirectoryMetadataList
                    .map {
                        it.domain = evaDirectoryDTO.domain
                        it.toDbModel(EvaDirectoryDbAdapter.loadEvaDirectory(realmInTrans, it.directoryId))
                    }
                    .forEach { candidate ->

                        realmInTrans.copyToRealmOrUpdate(candidate)

                        evaDirectory.subCategories.addIfNoneExistingMatch(candidate) { existing ->
                            candidate.id == existing.id
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

    @Deprecated("legacy")
    fun addOrUpdateEvaContentAsync_legacy(realm: Realm,
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

    fun loadManyEvaContents(realm: Realm,
                            predicate: (EvaContent) -> Boolean,
                            subscriber: (EvaContent) -> Unit): Disposable {
        return realm
                .where<EvaContent>()
                .loadManyAsync(predicate, subscriber)
    }
}