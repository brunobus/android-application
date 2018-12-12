package hr.bpervan.novaeva.model

import com.google.gson.annotations.SerializedName

class EvaDirectoryDTO {
    var directoryId: Long = 0
    var categoryId: Long = -1
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

class EvaDirectoryMetadataDTO {
    @SerializedName("cid")
    val directoryId: Long = 0
    var categoryId: Long = 0
    @SerializedName("name")
    val title: String? = null
}

class EvaContentMetadataDTO {

    @SerializedName("nid")
    val contentId: Long = -1
    var directoryId: Long = 0
    var categoryId: Long = -1
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

class EvaContentDTO {
    @SerializedName("nid")
    val contentId: Long = 0

    @SerializedName("cid")
    val directoryId: Long = 0

    var categoryId: Long = -1

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

class EvaAttachmentDTO {
    @SerializedName("naziv")
    val name: String? = null
    val url: String? = null
}

class EvaImageDTO {
    @SerializedName("640")
    val size640: String? = null
    @SerializedName("720")
    val size720: String? = null
    @SerializedName("date")
    val timestamp: Long = 0
    val original: String? = null
}

class EvaBreviaryDTO {
    @SerializedName("tekst")
    val text: String? = null
}

class EvaSearchResultDTO {
    val paket: Int = 0

    @SerializedName("vijesti")
    private val _searchResultContentMetadataList: List<EvaContentMetadataDTO>? = null
    val searchResultContentMetadataList: List<EvaContentMetadataDTO>
        get() = _searchResultContentMetadataList ?: emptyList()
}

class EvaIndicatorsDTO {
    @SerializedName("354")
    val spirituality: Int? = null
    @SerializedName("9")
    val trending: Int? = null
    @SerializedName("1")
    val quotes: Int? = null
    @SerializedName("10")
    val multimedia: Int? = null
    @SerializedName("4")
    val gospel: Int? = null
    @SerializedName("7")
    val sermons: Int? = null
    @SerializedName("8")
    val vocation: Int? = null
    @SerializedName("11")
    val answers: Int? = null
    @SerializedName("355")
    val songbook: Int? = null
}