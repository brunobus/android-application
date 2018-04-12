package hr.bpervan.novaeva.cache

import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import io.realm.Realm

/**
 * Created by vpriscan on 26.10.17..
 */
object EvaCacheService {

    fun cache(realm: Realm, evaContentDTO: EvaContentDTO) {
        EvaContentDbAdapter.addOrUpdateEvaContentAsync(realm, evaContentDTO.toDatabaseModel())
    }

    fun cache(realm: Realm, evaDirectoryDTO: EvaDirectoryDTO) {
        EvaDirectoryDbAdapter.addOrUpdateEvaDirectoryAsync(realm, evaDirectoryDTO.directoryId,
                evaDirectoryDTO.contentMetadataList?.map { it.toDatabaseModel() } ?: listOf(),
                evaDirectoryDTO.subDirectoryMetadataList?.map { it.toDatabaseModel() } ?: listOf())
    }
}