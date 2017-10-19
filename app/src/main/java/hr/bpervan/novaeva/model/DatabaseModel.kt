package hr.bpervan.novaeva.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by vpriscan on 07.10.17..
 */

const val CONTENT_ID_FIELD = "contentId"
const val DIRECTORY_ID_FIELD = "directoryId"

interface TreeElementInfo

open class EvaDirectoryInfo(
        var directoryId: Long = -1,
        var title: String? = null
) : RealmObject(), TreeElementInfo

open class EvaContentInfo(
        @PrimaryKey
        var contentId: Long = -1,
        var attachmentsIndicator: EvaAttachmentsIndicator? = null,
        var datetime: String? = null,
        var title: String? = null,
        var preview: String? = null
) : RealmObject(), TreeElementInfo

open class EvaAttachmentsIndicator(
        var hasVideo: Boolean = false,
        var hasDocuments: Boolean = false,
        var hasMusic: Boolean = false,
        var hasImages: Boolean = false,
        var hasText: Boolean = false
) : RealmObject()


//todo add others