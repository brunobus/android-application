package hr.bpervan.novaeva.model

import hr.bpervan.novaeva.util.notNullNorEmpty
import io.realm.RealmList

//todo 13.12.

fun CategoryInfoDto.toDatabaseModel(): EvaDirectory {
    return EvaDirectory(
            directoryId = id,
            title = title.orEmpty()
    )
}

fun ContentDto.toDatabaseModel(): EvaContent {

    val attachmentsDb = documents.orEmpty()
            .map { doc -> EvaAttachment(doc.title.orEmpty(), doc.link.orEmpty()) }
            .toTypedArray()

    val image = images?.map { image -> EvaImage(image.link.orEmpty(), 0) }?.firstOrNull()

    return EvaContent(
            contentId = id,
            directoryId = supercategory?.id ?: -1,
            categoryId = supercategory?.id ?: -1,
            timestamp = created,
            preview = description.orEmpty(),
            title = title.orEmpty(),
            text = text.orEmpty(),
            attachments = RealmList(*attachmentsDb),
            image = image,
            videoURL = video?.firstOrNull()?.link,
            audioURL = audio?.firstOrNull()?.link,
            attachmentsIndicator = AttachmentIndicatorHelper.encode(EvaAttachmentsIndicatorDTO().apply {
                hasVideo = video.notNullNorEmpty()
                hasDocuments = documents.notNullNorEmpty()
                hasMusic = audio.notNullNorEmpty()
                hasImages = images.notNullNorEmpty()
                hasText = text.notNullNorEmpty() || html.notNullNorEmpty()
            }))
}