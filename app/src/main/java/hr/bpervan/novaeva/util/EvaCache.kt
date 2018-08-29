package hr.bpervan.novaeva.util

import hr.bpervan.novaeva.model.EvaContentDTO
import hr.bpervan.novaeva.model.EvaDirectoryDTO
import hr.bpervan.novaeva.model.toDatabaseModel
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import io.realm.Realm

/**
 * Created by vpriscan on 26.10.17..
 */
object EvaCache {

    fun cache(realm: Realm, evaContentDTO: EvaContentDTO) {
        EvaContentDbAdapter.addOrUpdateEvaContentAsync(realm, evaContentDTO.toDatabaseModel())
    }

    fun cache(realm: Realm, evaDirectoryDTO: EvaDirectoryDTO) {
        EvaDirectoryDbAdapter.addOrUpdateEvaDirectoryAsync(realm, evaDirectoryDTO.directoryId,
                evaDirectoryDTO.contentMetadataList.map {
                    it.directoryId = evaDirectoryDTO.directoryId
                    it.categoryId = evaDirectoryDTO.categoryId
                    it.toDatabaseModel()
                },
                evaDirectoryDTO.subDirectoryMetadataList.map {
                    it.categoryId = evaDirectoryDTO.categoryId
                    it.toDatabaseModel()
                }
        )
    }
}