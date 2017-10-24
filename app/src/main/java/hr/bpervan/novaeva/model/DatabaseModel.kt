package hr.bpervan.novaeva.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 */

const val CONTENT_ID_FIELD = "contentId"
const val DIRECTORY_ID_FIELD = "directoryId"

interface TreeElementInfo

open class EvaDirectoryMetadata(
        var directoryId: Long = -1,
        var title: String = ""
) : RealmObject(), TreeElementInfo

open class EvaContentMetadata(
        @PrimaryKey
        var contentId: Long = -1,
        var attachmentsIndicator: EvaAttachmentsIndicator? = null,
        var datetime: String = "",
        var title: String = "",
        var preview: String = ""
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
        var rawImage: ByteArray = ByteArray(0),
        var date: Int = 0
) : RealmObject()

open class EvaContent(
        @PrimaryKey
        var contentId: Long = -1,
        var contentMetadata: EvaContentMetadata? = null,
        var color: EvaColor? = null,
        var attachments: RealmList<EvaAttachment> = RealmList(),
        var image: EvaImage? = null,
        var text: String = "",
        var title: String = "",
        var videoURL: String = "",
        var audioURL: String = ""
) : RealmObject()
