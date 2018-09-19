package hr.bpervan.novaeva.model

import io.realm.RealmList

/**
 * Created by vpriscan on 19.10.17..
 */

fun EvaDirectoryMetadataDTO.toDbModel(mergeWith: EvaDirectory? = null): EvaDirectory {
    val dto = this

    val evaDirectory = mergeWith ?: EvaDirectory().apply {
        directoryId = dto.directoryId
    }

    return evaDirectory.apply {
        categoryId = dto.categoryId
        title = dto.title ?: ""
    }
}

fun EvaContentMetadataDTO.toDbModel(mergeWith: EvaContent? = null): EvaContent {
    val dto = this

    if (mergeWith != null
            && dto.contentId != mergeWith.contentId) {
        throw RuntimeException("Merging wrong EvaContent (expected contentId: ${this.contentId}, actual: ${mergeWith.contentId})")
    }

    val evaContent = mergeWith ?: EvaContent().apply {
        contentId = dto.contentId
    }


    return evaContent.apply {
        directoryId = dto.directoryId
        categoryId = dto.categoryId

        datetime?.toLong()?.let {
            timestamp = it
        }

        preview = dto.preview
        title = dto.title ?: ""
        attachmentsIndicator = dto.attachmentsIndicator?.let { AttachmentIndicatorHelper.encode(it) } ?: 0
    }
}

fun EvaAttachmentDTO.toDbModel(): EvaAttachment = EvaAttachment(name ?: "", url ?: "")

fun EvaImageDTO.toDbModel(): EvaImage? {
    val imageURL = this.original ?: this.size720 ?: this.size640 ?: return null
    return EvaImage(imageURL, this.timestamp)
}

fun EvaContentDTO.toDbModel(mergeWith: EvaContent? = null): EvaContent {
    val dto = this

    val evaContent = mergeWith ?: EvaContent().apply {
        contentId = dto.contentId
    }

    return evaContent.apply {
        directoryId = dto.directoryId
        categoryId = dto.categoryId
        title = dto.title ?: ""
        text = dto.text ?: ""

        val attachmentsArray = dto.attachments
                .map { it.toDbModel() }
                .map { evaContent.realm?.copyToRealm(it) ?: it }
                .toTypedArray()

        attachments = RealmList(*attachmentsArray)

        val oldImage = image
        val candidateImage = dto.images.firstOrNull()
        when {
            candidateImage == null -> image = null
            oldImage == null -> {
                image = candidateImage.toDbModel()?.let { realm?.copyToRealm(it) ?: it }
            }
            oldImage.timestamp != candidateImage.timestamp -> {
                image = candidateImage.toDbModel()?.let { realm?.copyToRealm(it) ?: it }
            }
        }

        videoURL = dto.videoURL
        audioURL = dto.audioURL
    }
}