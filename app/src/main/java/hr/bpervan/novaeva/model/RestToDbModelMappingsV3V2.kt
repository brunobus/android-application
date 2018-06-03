package hr.bpervan.novaeva.model

import io.realm.RealmList

fun CategoryInfoDto.toDatabaseModel(): EvaDirectoryMetadata {
    return EvaDirectoryMetadata(id, title ?: "")
}

fun ContentDto.extractMeta(): EvaContentMetadata {
    val attachmentsIndicator = EvaAttachmentsIndicator(
            video.orEmpty().isNotEmpty(),
            documents.orEmpty().isNotEmpty(),
            audio.orEmpty().isNotEmpty(),
            images.orEmpty().isNotEmpty(),
            text.orEmpty().isNotBlank() || html.orEmpty().isNotBlank())

    val categoryId = supercategory?.id ?: -1

    return EvaContentMetadata(id, categoryId, -1,
            attachmentsIndicator, modified, title.orEmpty(), description.orEmpty())
}

fun ContentDto.toDatabaseModel(): EvaContent {

    val contentMeta = extractMeta()

    val attachmentsDb = documents.orEmpty()
            .map { doc -> EvaAttachment(doc.title.orEmpty(), doc.link.orEmpty()) }
            .toTypedArray()

    val image = images?.map { image -> EvaImage(image.link.orEmpty(), 0) }?.firstOrNull()

    return EvaContent(id, contentMeta, text
            ?: "", null, RealmList(*attachmentsDb), image, video?.firstOrNull()?.link, audio?.firstOrNull()?.link)
}