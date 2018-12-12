package hr.bpervan.novaeva.model

import io.realm.RealmList

/**
 * Created by vpriscan on 19.10.17..
 */

fun EvaDirectoryMetadataDTO.toDatabaseModel(): EvaDirectoryMetadata =
        EvaDirectoryMetadata(directoryId, categoryId, title ?: "")

fun EvaContentMetadataDTO.toDatabaseModel(): EvaContentMetadata =
        EvaContentMetadata(contentId, directoryId, categoryId,
                attachmentsIndicator?.toDatabaseModel(),
                datetime?.toLong() ?: 0, title ?: "", preview)

fun EvaAttachmentsIndicatorDTO.toDatabaseModel(): EvaAttachmentsIndicator =
        EvaAttachmentsIndicator(hasVideo, hasDocuments, hasMusic, hasImages, hasText)

fun EvaAttachmentDTO.toDatabaseModel(): EvaAttachment = EvaAttachment(naziv ?: "", url ?: "")

fun EvaImageDTO.toDatabaseModel(): EvaImage? {
    val imageURL = this.original ?: this.size720 ?: this.size640 ?: return null
    return EvaImage(imageURL, this.timestamp)
}

fun EvaContentDTO.toDatabaseModel(): EvaContent {
    val attachmentsDb = attachments.map { it.toDatabaseModel() }.toTypedArray()
    val image = images.firstOrNull()
    return EvaContent(contentId, null, text ?: "",
            null, RealmList(*attachmentsDb), image?.toDatabaseModel(), videoURL, audioURL)
}
