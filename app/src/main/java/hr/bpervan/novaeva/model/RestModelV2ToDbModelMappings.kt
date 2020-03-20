package hr.bpervan.novaeva.model

import io.realm.RealmList

/**
 * Created by vpriscan on 19.10.17..
 */

@Deprecated("legacy v2 api")
fun EvaDirectoryMetadataDTO.toDbModel(mergeWith: EvaDirectory? = null): EvaDirectory {
    val dto = this

    val evaDirectory = mergeWith ?: EvaDirectory(id = dto.directoryId)

    return evaDirectory.apply {
        domain = dto.domain?.toString()
        title = dto.title ?: ""
    }
}

@Deprecated("legacy v2 api")
fun EvaContentMetadataDTO.toDbModel(mergeWith: EvaContent? = null): EvaContent {
    val dto = this

    if (mergeWith != null
            && dto.contentId != mergeWith.id) {
        throw RuntimeException("Merging wrong EvaContent (expected content id: ${this.contentId}, actual: ${mergeWith.id})")
    }

    val evaContent = mergeWith ?: EvaContent(id = dto.contentId)

    return evaContent.apply {
        directoryId = dto.directoryId
        domain = dto.domain?.toString()

        dto.datetime?.toLong()?.let { datetime ->
            created = 1000 * datetime
        }

        preview = dto.preview
        title = dto.title ?: ""
        attachmentsIndicator = dto.attachmentsIndicator?.let { AttachmentIndicatorHelper.encode(it) } ?: 0
    }
}

@Deprecated("legacy v2 api")
fun EvaAttachmentDTO.toDbModel(): EvaAttachment = EvaAttachment(name ?: "", url ?: "")

@Deprecated("legacy v2 api")
fun EvaImageDTO.toDbModel(): EvaResource? {
    val imageURL = this.original ?: this.size720 ?: this.size640 ?: return null
    return EvaResource(url = imageURL)
}

@Deprecated("legacy v2 api")
fun EvaContentDTO.toDbModel(mergeWith: EvaContent? = null): EvaContent {
    val dto = this

    val evaContent = mergeWith ?: EvaContent(id = dto.contentId)

    return evaContent.apply {
        directoryId = dto.directoryId
        domain = dto.domain?.toString()
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