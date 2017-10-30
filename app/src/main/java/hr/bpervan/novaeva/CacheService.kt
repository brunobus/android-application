package hr.bpervan.novaeva

import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import io.realm.Realm

/**
 * Created by vpriscan on 26.10.17..
 */
//todo change package
object CacheService {

    fun cache(realm: Realm, evaContentDTO: EvaContentDTO) {
        EvaContentDbAdapter.addOrUpdateEvaContent(realm, evaContentDTO.toDatabaseModel())
    }

    fun cache(realm: Realm, evaDirectoryDTO: EvaDirectoryDTO, directoryId: Long) {

        EvaDirectoryDbAdapter.addOrUpdateEvaDirectoryAsync(realm, directoryId,
                newContentMetadataSupplier = {
                    evaDirectoryDTO.contentMetadataList?.map { it.toDatabaseModel() } ?: listOf()
                },
                newSubDirectoryMetadataSupplier = {
                    evaDirectoryDTO.subDirectoryMetadataList?.map { it.toDatabaseModel() } ?: listOf()
                })
    }
}