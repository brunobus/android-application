package hr.bpervan.novaeva.model

import io.realm.RealmList

/**
 * Created by vpriscan on 19.10.17..
 */

fun EvaDirectoryMetadataDTO.toDatabaseModel(): EvaDirectoryMetadata =
        EvaDirectoryMetadata(directoryId, title ?: "")

fun EvaContentMetadataDTO.toDatabaseModel(): EvaContentMetadata =
        EvaContentMetadata(contentId, attachmentsIndicator?.toDatabaseModel(), datetime?.toLong() ?: 0, title ?: "", preview)

fun EvaAttachmentsIndicatorDTO.toDatabaseModel(): EvaAttachmentsIndicator =
        EvaAttachmentsIndicator(hasVideo, hasDocuments, hasMusic, hasImages, hasText)

fun EvaImageDTO.toDatabaseModel(): EvaImage = EvaImage(timestamp = timestamp) //todo

fun EvaAttachmentDTO.toDatabaseModel(): EvaAttachment = EvaAttachment(naziv ?: "", url ?: "")

fun EvaContentDTO.toDatabaseModel(contentMetadata: EvaContentMetadata): EvaContent {
    val attachmentsDb = attachments?.map { it.toDatabaseModel() }?.toTypedArray() ?: arrayOf()
    val imagesDb = images?.get(0)?.toDatabaseModel()
    return EvaContent(contentId, contentMetadata, text ?: "", null, RealmList(*attachmentsDb), imagesDb, youtube, audio)
}
