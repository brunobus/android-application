package hr.bpervan.novaeva.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 */

const val CONTENT_ID_FIELD = "contentId"
const val DIRECTORY_ID_FIELD = "directoryId"
const val TIMESTAMP_FIELD = "timestamp"

interface EvaNode

open class EvaAttachmentsIndicator(
        var hasVideo: Boolean = false,
        var hasDocuments: Boolean = false,
        var hasMusic: Boolean = false,
        var hasImages: Boolean = false,
        var hasText: Boolean = false
) : RealmObject()

open class EvaAttachment(
        var name: String = "",
        var url: String = ""
) : RealmObject()

open class EvaImage(
        var url: String = "",
        var timestamp: Long = 0
) : RealmObject()

open class EvaDirectory(
        @PrimaryKey
        var directoryId: Long = -1,
        var categoryId: Long = directoryId,
        var title: String = "",
        var image: EvaImage? = null,
        var subDirectoriesList: RealmList<EvaDirectory> = RealmList(),
        var contentsList: RealmList<EvaContent> = RealmList()
) : RealmObject(), EvaNode

open class EvaContent(
        @PrimaryKey
        var contentId: Long = -1,
        var directoryId: Long = -1,
        var categoryId: Long = -1,
        var timestamp: Long = 0,
        var preview: String = "",
        var title: String = "",
        var text: String = "",
        var bookmarked: Boolean = false,
        var attachmentsIndicator: EvaAttachmentsIndicator? = null,
        var attachments: RealmList<EvaAttachment> = RealmList(),
        var image: EvaImage? = null,
        var videoURL: String? = null,
        var audioURL: String? = null
) : RealmObject(), EvaNode
