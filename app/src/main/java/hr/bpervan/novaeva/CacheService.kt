package hr.bpervan.novaeva

import hr.bpervan.novaeva.model.*
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import io.realm.Realm

/**
 * Created by vpriscan on 26.10.17..
 */
object CacheService {

    fun cache(realm: Realm, evaContentDTO: EvaContentDTO) {
        EvaContentDbAdapter.loadEvaContentMetadataAsync(realm, evaContentDTO.contentId, { contentMetadata ->
            val evaContent = evaContentDTO.toDatabaseModel(contentMetadata)
            EvaContentDbAdapter.storeEvaContent(realm, evaContent)
        })
    }

    fun cache(realm: Realm, evaDirectoryDTO: EvaDirectoryDTO, directoryId: Long) {
        val contentMetadataList: List<EvaContentMetadata> =
                evaDirectoryDTO.contentMetadataList?.map { it.toDatabaseModel() } ?: listOf()

        val subDirectoryMetadataList: List<EvaDirectoryMetadata> =
                evaDirectoryDTO.subDirectoryMetadataList?.map { it.toDatabaseModel() } ?: listOf()

        EvaDirectoryDbAdapter.updateIfExistsEvaDirectoryAsync(realm, directoryId,
                contentMetadataList, subDirectoryMetadataList)
    }
}