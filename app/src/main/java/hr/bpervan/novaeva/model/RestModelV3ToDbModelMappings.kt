package hr.bpervan.novaeva.model

import hr.bpervan.novaeva.storage.toRealmList
import hr.bpervan.novaeva.util.notNullNorEmpty

fun CategoryInfoDto.toDbModel(mergeWith: EvaDirectory? = null): EvaDirectory {
    val dto = this

    val evaDirectory = mergeWith ?: EvaDirectory(id = dto.id)

    return evaDirectory.apply {
        title = dto.title.orEmpty()
    }
}

fun LinkDto.toDbModel(): EvaResource {
    return EvaResource(
            id = id,
            title = title,
            url = link
    )
}

fun ContentDto.toDbModel(mergeWith: EvaContent? = null): EvaContent {
    val dto = this

    val evaContent = mergeWith ?: EvaContent(id = dto.id)

    return evaContent.apply {
        directoryId = dto.supercategory?.id ?: -1
        domain = dto.domain?.toString()
        timestamp = dto.created
        preview = dto.description.orEmpty()
        title = dto.title.orEmpty()
        text = dto.text.orEmpty()
        attachments = documents.orEmpty()
                .map { doc -> EvaAttachment(doc.title.orEmpty(), doc.link.orEmpty()) }
                .toTypedArray()
                .toRealmList()
        image = dto.images.orEmpty()
                .map { image ->
                    EvaResource(
                            id = image.id,
                            title = image.title,
                            url = image.link.orEmpty())
                }.firstOrNull()
        videoURL = dto.video?.firstOrNull()?.link
        audioURL = dto.audio?.firstOrNull()?.link
        attachmentsIndicator = AttachmentIndicatorHelper.encode(EvaAttachmentsIndicatorDTO().apply {
            hasVideo = dto.video.notNullNorEmpty()
            hasDocuments = dto.documents.notNullNorEmpty()
            hasMusic = dto.audio.notNullNorEmpty()
            hasImages = dto.images.notNullNorEmpty()
            hasText = dto.text.notNullNorEmpty() || html.notNullNorEmpty()
        })
    }
}