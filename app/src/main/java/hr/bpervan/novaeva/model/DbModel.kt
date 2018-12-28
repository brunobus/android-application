package hr.bpervan.novaeva.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 */

const val CONTENT_ID_FIELD = "id"
const val DIRECTORY_ID_FIELD = "id"
const val TIMESTAMP_FIELD = "created"

interface EvaNode

open class EvaAttachment(
        var name: String = "",
        var url: String = ""
) : RealmObject()

open class EvaResource(
//        @PrimaryKey
        var id: Long = -1,
        var title: String? = null,
        var url: String? = null
) : RealmObject()

open class EvaDirectory(
        @PrimaryKey
        var id: Long = -1,
        var domain: String? = null,
//        var created: Long = 0,
//        var modified: Long = created,
        var title: String = "",
        var image: EvaResource? = null,
        var subCategories: RealmList<EvaDirectory> = RealmList(),
        var contents: RealmList<EvaContent> = RealmList()
) : RealmObject(), EvaNode

open class EvaContent(
        @PrimaryKey
        var id: Long = -1,
        var directoryId: Long = -1,
        var domain: String? = null,
        var created: Long = 0,
        var modified: Long = created,
        var preview: String = "",
        var title: String = "",
        var text: String = "",
        var bookmarked: Boolean = false,
        var attachmentsIndicator: Int = 0,
        var attachments: RealmList<EvaAttachment> = RealmList(),
        var image: EvaResource? = null,
        var videoURL: String? = null,
        var audioURL: String? = null
) : RealmObject(), EvaNode
