package hr.bpervan.novaeva.util

import hr.bpervan.novaeva.model.*
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

    fun cache(realm: Realm, contentDto: ContentDto) {
        EvaContentDbAdapter.addOrUpdateEvaContentAsync(realm, contentDto.toDatabaseModel())
    }

    fun cache(realm: Realm, evaDirectoryDTO: EvaDirectoryDTO) {
        EvaDirectoryDbAdapter.addOrUpdateEvaDirectoryAsync(realm, evaDirectoryDTO.directoryId,
                evaDirectoryDTO.contentMetadataList.map { it.toDatabaseModel() },
                evaDirectoryDTO.subDirectoryMetadataList.map { it.toDatabaseModel() })
    }

    fun cache(realm: Realm, categoryDto: CategoryDto) {
        EvaDirectoryDbAdapter.addOrUpdateEvaDirectoryAsync(realm, categoryDto.id,
                categoryDto.content.orEmpty().map { it.extractMeta() },
                categoryDto.subcategories.orEmpty().map { it.toDatabaseModel() })

        categoryDto.content.orEmpty().forEach {
            cache(realm, it)
        }
    }
}