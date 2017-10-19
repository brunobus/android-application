package hr.bpervan.novaeva.model

import com.google.gson.annotations.SerializedName

data class EvaDirectoryContentListDTO(
        val image: EvaImageDTO? = null,
        val paket: Int = 0,
        @SerializedName("jos") val more: Int = 0,
        @SerializedName("subcat") val subDirectoryInfoList: List<EvaDirectoryInfoDTO>? = null,
        @SerializedName("vijesti") val contentInfoList: List<EvaContentInfoDTO>? = null)

data class EvaDirectoryInfoDTO(
        @SerializedName("cid") val directoryId: Long = -1,
        @SerializedName("name") val title: String? = null)

data class EvaContentInfoDTO(
        @SerializedName("nid") val contentId: Long = -1,
        @SerializedName("attach") val attachmentsIndicator: EvaAttachmentsIndicatorDTO? = null,
        @SerializedName("datum") val datetime: String? = null,
        @SerializedName("naslov") val title: String? = null,
        @SerializedName(value = "tekst", alternate = arrayOf("uvod")) val text: String? = null) {

    val preview: String by lazy {
        if (text != null) {
            val stripped = text.replace(Regex("<[^>]+>"), "")

            val trimmed = when {
                stripped.length > 50 -> stripped.substring(0, 50)
                else -> stripped
            }
            trimmed + "..."
        } else {
            "null"
        }
    }

}

data class EvaAttachmentsIndicatorDTO(
        @SerializedName("video") val hasVideo: Boolean = false,
        @SerializedName("documents") val hasDocuments: Boolean = false,
        @SerializedName("music") val hasMusic: Boolean = false,
        @SerializedName("images") val hasImages: Boolean = false,
        @SerializedName("text") val hasText: Boolean = false)

data class EvaContentDataDTO(
        @SerializedName("nid") val contentId: Long = 0,
        @SerializedName("cid") val directoryId: Long = 0,
        @SerializedName("prilozi") val attachments: List<AttachmentDTO>? = null,
        @SerializedName("image") val images: List<EvaImageDTO>? = null,
        @SerializedName("tekst") val text: String? = null,
        @SerializedName("naslov") val title: String? = null,
        val youtube: String? = null,
        val audio: String? = null,
        @SerializedName("time") val datetime: String? = null) {

    fun hasImage(): Boolean = images != null && images.isNotEmpty() && this.images[0].size640 != null


}

data class AttachmentDTO(val naziv: String? = null,
                         val url: String? = null)

data class EvaImageDTO(@SerializedName("640") val size640: String? = null,
                       @SerializedName("720") val size720: String? = null,
                       val date: Int = 0,
                       val original: String? = null)

data class EvaBreviaryDTO(@SerializedName("tekst") val text: String? = null)

data class EvaSearchResultDTO(
        val paket: Int = 0,
        @SerializedName("vijesti") val searchResultContentInfoList: List<EvaContentInfoDTO>? = null)

data class EvaIndicatorsDTO(
        @SerializedName("354") val duhovnost: Int? = null,
        @SerializedName("9") val aktualno: Int? = null,
        @SerializedName("1") val izreke: Int? = null,
        @SerializedName("10") val multimedija: Int? = null,
        @SerializedName("4") val evandjelje: Int? = null,
        @SerializedName("7") val propovijedi: Int? = null,
        @SerializedName("8") val poziv: Int? = null,
        @SerializedName("11") val odgovori: Int? = null,
        @SerializedName("355") val pjesmarica: Int? = null
)