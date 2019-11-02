package hr.bpervan.novaeva.model

import com.google.gson.annotations.SerializedName
import hr.bpervan.novaeva.rest.EvaDomain

@Deprecated("legacy v2 api")
class EvaDirectoryDTO {
    var directoryId: Long = 0
    var domain: EvaDomain? = null
    var title: String? = null
    var image: EvaImageDTO? = null

    var paket: Int = 0

    @SerializedName("jos")
    var more: Int = 0

    @SerializedName("subcat")
    var _subDirectoryMetadataList: List<EvaDirectoryMetadataDTO>? = null
    val subDirectoryMetadataList: List<EvaDirectoryMetadataDTO>
        get() = _subDirectoryMetadataList ?: emptyList()

    @SerializedName("vijesti")
    var _contentMetadataList: List<EvaContentMetadataDTO>? = null
    val contentMetadataList: List<EvaContentMetadataDTO>
        get() = _contentMetadataList ?: emptyList()
}

@Deprecated("legacy v2 api")
class EvaDirectoryMetadataDTO {
    @SerializedName("cid")
    val directoryId: Long = 0
    var domain: EvaDomain? = null
    @SerializedName("name")
    val title: String? = null
}

@Deprecated("legacy v2 api")
class EvaContentMetadataDTO {

    @SerializedName("nid")
    val contentId: Long = -1
    var directoryId: Long = 0
    var domain: EvaDomain? = null
    @SerializedName("attach")
    val attachmentsIndicator: EvaAttachmentsIndicatorDTO? = null
    @SerializedName("datum")
    val datetime: String? = null
    @SerializedName("naslov")
    val title: String? = null
    @SerializedName(value = "tekst", alternate = ["uvod"])
    val text: String? = null

    val preview: String by lazy {
        if (text != null) {
            val stripped = text.replace(Regex("<[^>]+>"), "")

            val trimmed = when {
                stripped.length > 50 -> stripped.substring(0, 50)
                else -> stripped
            }
            "$trimmed..."
        } else {
            "null"
        }
    }

}

@Deprecated("legacy v2 api")
class EvaAttachmentsIndicatorDTO {
    @SerializedName("video")
    var hasVideo: Boolean = false
    @SerializedName("documents")
    var hasDocuments: Boolean = false
    @SerializedName("music")
    var hasMusic: Boolean = false
    @SerializedName("images")
    var hasImages: Boolean = false
    @SerializedName("text")
    var hasText: Boolean = false
}

@Deprecated("legacy v2 api")
class EvaContentDTO {
    @SerializedName("nid")
    val contentId: Long = 0

    @SerializedName("cid")
    val directoryId: Long = 0

    var domain: EvaDomain? = null

    @SerializedName("prilozi")
    private val _attachments: List<EvaAttachmentDTO>? = null
    val attachments: List<EvaAttachmentDTO>
        get() = _attachments ?: emptyList()

    @SerializedName("image")
    private val _images: List<EvaImageDTO>? = null
    val images: List<EvaImageDTO>
        get() = _images ?: emptyList()

    @SerializedName("tekst")
    val text: String? = null
    @SerializedName("naslov")
    val title: String? = null
    @SerializedName("youtube")
    val videoURL: String? = null
    @SerializedName("audio")
    val audioURL: String? = null
    @SerializedName("time")
    val datetime: String? = null
}

@Deprecated("legacy v2 api")
class EvaAttachmentDTO {
    @SerializedName("naziv")
    val name: String? = null
    val url: String? = null
}

@Deprecated("legacy v2 api")
class EvaImageDTO {
    @SerializedName("640")
    val size640: String? = null
    @SerializedName("720")
    val size720: String? = null
    @SerializedName("date")
    val timestamp: Long = 0
    val original: String? = null
}

@Deprecated("legacy v2 api")
class EvaBreviaryDTO {
    @SerializedName("tekst")
    val text: String? = null
}

@Deprecated("legacy v2 api")
class EvaSearchResultDTO {
    val paket: Int = 0

    @SerializedName("vijesti")
    private val _searchResultContentMetadataList: List<EvaContentMetadataDTO>? = null
    val searchResultContentMetadataList: List<EvaContentMetadataDTO>
        get() = _searchResultContentMetadataList ?: emptyList()
}

@Deprecated("legacy v2 api")
class EvaIndicatorsDTO {
    @SerializedName("354")
    val spirituality: Long? = null
    @SerializedName("9")
    val trending: Long? = null
    @SerializedName("1")
    val quotes: Long? = null
    @SerializedName("10")
    val multimedia: Long? = null
    @SerializedName("4")
    val gospel: Long? = null
    @SerializedName("7")
    val sermons: Long? = null
    @SerializedName("8")
    val vocation: Long? = null
    @SerializedName("11")
    val answers: Long? = null
    @SerializedName("355")
    val songbook: Long? = null
}