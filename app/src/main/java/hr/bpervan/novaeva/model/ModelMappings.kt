package hr.bpervan.novaeva.model

/**
 * Created by vpriscan on 19.10.17..
 */

fun EvaDirectoryMetadataDTO.asDatabaseModel(): EvaDirectoryMetadata =
        EvaDirectoryMetadata(directoryId, title ?: "")

fun EvaContentMetadataDTO.asDatabaseModel(): EvaContentMetadata =
        EvaContentMetadata(contentId, attachmentsIndicator?.asDatabaseModel(), datetime ?: "", title ?: "", preview)

fun EvaAttachmentsIndicatorDTO.asDatabaseModel(): EvaAttachmentsIndicator =
        EvaAttachmentsIndicator(hasVideo, hasDocuments, hasMusic, hasImages, hasText)

//todo help expand