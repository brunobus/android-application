package hr.bpervan.novaeva.model

import io.realm.RealmList

/**
 * Created by vpriscan on 19.10.17..
 */

fun EvaDirectoryMetadataDTO.toDatabaseModel(): EvaDirectory =
        EvaDirectory(directoryId, categoryId, title ?: "")

fun EvaContentMetadataDTO.toDatabaseModel(): EvaContent =
        EvaContent(contentId, directoryId, categoryId, datetime?.toLong() ?: 0,
                preview, title ?: "",
                attachmentsIndicator = attachmentsIndicator?.toDatabaseModel())

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
    return EvaContent(contentId, directoryId, categoryId, 0,
            title = title ?: "", text = text ?: "",
            attachments = RealmList(*attachmentsDb),
            image = image?.toDatabaseModel(),
            videoURL = videoURL,
            audioURL = audioURL)
}
