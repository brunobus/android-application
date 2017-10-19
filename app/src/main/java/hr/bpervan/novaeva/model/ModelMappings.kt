package hr.bpervan.novaeva.model

/**
 * Created by vpriscan on 19.10.17..
 */

fun EvaDirectoryInfoDTO.asDatabaseModel(): EvaDirectoryInfo =
        EvaDirectoryInfo(directoryId, title)

fun EvaContentInfoDTO.asDatabaseModel(): EvaContentInfo =
        EvaContentInfo(contentId, attachmentsIndicator?.asDatabaseModel(), datetime, title, preview)

fun EvaAttachmentsIndicatorDTO.asDatabaseModel(): EvaAttachmentsIndicator =
        EvaAttachmentsIndicator(hasVideo, hasDocuments, hasMusic, hasImages, hasText)

//todo help expand