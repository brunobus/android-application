package hr.bpervan.novaeva.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 */

const val CONTENT_ID_FIELD = "contentId"
const val DIRECTORY_ID_FIELD = "directoryId"
const val TIMESTAMP_FIELD = "timestamp"

interface TreeElementInfo

open class EvaDirectoryMetadata(
        @PrimaryKey
        var directoryId: Long = -1,
        var title: String = ""
) : RealmObject(), TreeElementInfo

open class EvaContentMetadata(
        @PrimaryKey
        var contentId: Long = -1,
        var attachmentsIndicator: EvaAttachmentsIndicator? = null,
        var timestamp: Long = 0,
        var title: String = "",
        var preview: String = "",
        var bookmark: Boolean = false
) : RealmObject(), TreeElementInfo

open class EvaAttachmentsIndicator(
        var hasVideo: Boolean = false,
        var hasDocuments: Boolean = false,
        var hasMusic: Boolean = false,
        var hasImages: Boolean = false,
        var hasText: Boolean = false
) : RealmObject()

open class EvaColor(
        var cRed: Byte = 0x0,
        var cGreen: Byte = 0xff.toByte(),
        var cBlue: Byte = 0x0,
        var cAlpha: Byte = 0x0
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
        var directoryMetadata: EvaDirectoryMetadata? = null,
        var color: EvaColor? = null,
        var image: EvaImage? = null,
        var subDirectoryMetadataList: RealmList<EvaDirectoryMetadata> = RealmList(),
        var contentMetadataList: RealmList<EvaContentMetadata> = RealmList()
) : RealmObject()

open class EvaContent(
        @PrimaryKey
        var contentId: Long = -1,
        var contentMetadata: EvaContentMetadata? = null,
        var text: String = "",
        var color: EvaColor? = null,
        var attachments: RealmList<EvaAttachment> = RealmList(),
        var image: EvaImage? = null,
        var videoURL: String? = null,
        var audioURL: String? = null
) : RealmObject()
