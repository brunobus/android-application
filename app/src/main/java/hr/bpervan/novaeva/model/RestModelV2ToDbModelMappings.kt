package hr.bpervan.novaeva.model

import io.realm.RealmList

/**
 * Created by vpriscan on 19.10.17..
 */

fun EvaDirectoryMetadataDTO.toDbModel(mergeWith: EvaDirectory? = null): EvaDirectory {
    val dto = this

    val evaDirectory = mergeWith ?: EvaDirectory(id = dto.directoryId)

    return evaDirectory.apply {
        domain = dto.domain?.toString()
        title = dto.title ?: ""
    }
}

fun EvaContentMetadataDTO.toDbModel(mergeWith: EvaContent? = null): EvaContent {
    val dto = this

    if (mergeWith != null
            && dto.contentId != mergeWith.id) {
        throw RuntimeException("Merging wrong EvaContent (expected content id: ${this.contentId}, actual: ${mergeWith.id})")
    }

    val evaContent = mergeWith ?: EvaContent(id = dto.contentId)

    return evaContent.apply {
        directoryId = dto.directoryId
        domain = null

        datetime?.toLong()?.let {
            timestamp = it
        }

        preview = dto.preview
        title = dto.title ?: ""
        attachmentsIndicator = dto.attachmentsIndicator?.let { AttachmentIndicatorHelper.encode(it) } ?: 0
    }
}

fun EvaAttachmentDTO.toDbModel(): EvaAttachment = EvaAttachment(name ?: "", url ?: "")

fun EvaImageDTO.toDbModel(): EvaResource? {
    val imageURL = this.original ?: this.size720 ?: this.size640 ?: return null
    return EvaResource(url = imageURL)
}

fun EvaContentDTO.toDbModel(mergeWith: EvaContent? = null): EvaContent {
    val dto = this

    val evaContent = mergeWith ?: EvaContent(id = dto.contentId)

    return evaContent.apply {
        directoryId = dto.directoryId
        domain = null
        title = dto.title ?: ""
        text = dto.text ?: ""

        val attachmentsArray = dto.attachments
                .map { it.toDbModel() }
                .map { evaContent.realm?.copyToRealm(it) ?: it }
                .toTypedArray()

        attachments = RealmList(*attachmentsArray)

        val oldImage = image
        val candidateImage = dto.images.firstOrNull()

        if (candidateImage == null) {
            image = null
        } else if (oldImage == null) {
            image = candidateImage.toDbModel()?.let { realm?.copyToRealm(it) ?: it }
        } else {
            val imageURL = candidateImage.original
                    ?: candidateImage.size720
                    ?: candidateImage.size640
            if (oldImage.url != imageURL) {
                image = candidateImage.toDbModel()?.let { realm?.copyToRealm(it) ?: it }
            }
        }

        videoURL = dto.videoURL
        audioURL = dto.audioURL
    }
}