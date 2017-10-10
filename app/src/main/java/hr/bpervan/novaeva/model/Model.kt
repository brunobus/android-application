package hr.bpervan.novaeva.model

import com.google.gson.annotations.SerializedName

data class DirectoryContent(val APIstatus: Int = 0,
                            val rezultat: Int = 0,
                            val image: Image? = null,
                            val paket: Int = 0,
                            @SerializedName("jos") val more: Int = 0,
                            @SerializedName("subcat") val subDirectoryInfoList: List<DirectoryInfo>? = null,
                            @SerializedName("vijesti") val contentInfoList: List<ContentInfo>? = null) {

    override fun toString(): String {
        return "DirectoryContent{APIstatus=$APIstatus, rezultat=$rezultat, subcat=$subDirectoryInfoList" +
                ", image=$image, paket=$paket, jos=$more, vijesti=$contentInfoList}"
    }
}

interface TreeElementInfo

data class DirectoryInfo(@SerializedName("cid") val directoryId: Long = -1,
                         @SerializedName("name") val title: String? = null) : TreeElementInfo {

    override fun toString(): String {
        return "DirectoryInfo{cid=$directoryId, name='$title'}"
    }
}

data class ContentInfo(val attach: Attachments? = null,
                       @SerializedName("nid") val contentId: Long = -1,
                       @SerializedName("datum") val datetime: String? = null,
                       @SerializedName("naslov") val title: String? = null,
                       @SerializedName("uvod") val summary: String? = null) : TreeElementInfo {

    data class Attachments(@SerializedName("video") val hasVideo: Boolean = false,
                           @SerializedName("documents") val hasDocuments: Boolean = false,
                           @SerializedName("music") val hasMusic: Boolean = false,
                           @SerializedName("images") val hasImages: Boolean = false,
                           @SerializedName("text") val hasText: Boolean = false) {

        override fun toString(): String {
            return "Attachments{video=$hasVideo, documents=$hasDocuments, music=$hasMusic, images=$hasImages, text=$hasText}"
        }
    }

    override fun toString(): String {
        return "ContentInfo{attach=$attach, nid=$contentId, datum='$datetime', naslov='$title', uvod='$summary'}"
    }
}

//todo change field names
data class ContentData(val APIstatus: Int = 0,
                       val rezultat: Int = 0,
                       val prilozi: List<Attachment>? = null,
                       val image: List<Image>? = null,
                       val tekst: String? = null,
                       val nid: Int = 0,
                       val cid: Int = 0,
                       val naslov: String? = null,
                       val youtube: String? = null,
                       val audio: String? = null,
                       val time: String? = null) {

    fun hasImage(): Boolean {
        return image != null && image.isNotEmpty() && this.image[0].size640 != null
    }


    data class Attachment(val naziv: String? = null,
                          val url: String? = null) {

        override fun toString(): String {
            return "Attachment{naziv='$naziv', url='$url'}"
        }
    }

    override fun toString(): String {
        return "ContentData{APIstatus=$APIstatus, rezultat=$rezultat, prilozi=$prilozi, image=$image" +
                ", tekst='${tekst?.substring(0, 50)}', nid=$nid, cid=$cid, naslov='$naslov" +
                "', youtube='$youtube', audio='$audio', time='$time'}"
    }
}

data class Image(@SerializedName("640") val size640: String? = null,
                 @SerializedName("720") val size720: String? = null,
                 val date: Int = 0,
                 val original: String? = null) {

    override fun toString(): String {
        return "Image{size640='$size640', size720='$size720', date=$date, original='$original'}"
    }
}

data class Breviary(@SerializedName("tekst") val text: String? = null)