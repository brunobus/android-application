package hr.bpervan.novaeva.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

/**
 * Created by vpriscan on 07.10.17..
 */

////temporary, todo
//open class EvaDirectoryInfo(
//        @PrimaryKey
//        var dirId: Long = -1,
//        var name: String = "",
//        var preview: String = ""
//) : RealmObject() {
//
//    companion object {
//        @Ignore
//        val ID_FIELD = "dirId"
//    }
//}

//temporary, todo
//open class EvaDirectory(
//        @PrimaryKey
//        var dirId: Long = -1,
//        var name: String = "",
//        var preview: String = "",
//        var contentElements: RealmList<EvaContent> = RealmList(),
//        var childDirectoryElements: RealmList<EvaDirectory> = RealmList()
//) : RealmObject() {
//    companion object {
//        @Ignore
//        val ID_FIELD = "dirId"
//    }
//}
//
////temporary, todo
//open class EvaContentInfo(
//        @PrimaryKey
//        var contentId: Int = 0,
//        var dirId: Int = 0,
//        var title: String? = null,
//        var text: String? = null,
//        var datetime: String? = null,
//        var hasVideo: Boolean = false,
//        var hasDocuments: Boolean = false,
//        var hasMusic: Boolean = false,
//        var hasImages: Boolean = false,
//        var hasText: Boolean = false
//) : RealmObject() {
//    companion object {
//        @Ignore
//        val ID_FIELD = "contentId"
//    }
//}

//temporary, todo
//open class EvaContent(
//        @PrimaryKey
//        var contentId: Int = 0,
//        var menuId: Int = 0,
//        var attachments: RealmList<EvaAttachment> = RealmList(),
////        var image: Image? = null,
//        var text: String? = null,
//        var title: String? = null,
//        var youtube: String? = null,
//        var audio: String? = null,
//        var datetime: String? = null
//) : RealmObject() {
//    companion object {
//        @Ignore
//        val ID_FIELD = "contentId"
//    }
//}

//temporary, todo
//open class EvaAttachment(
//        var name: String? = null,
//        var url: String? = null
//) : RealmObject()