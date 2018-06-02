package hr.bpervan.novaeva.model

import com.google.gson.annotations.SerializedName

open class ContentDto {
    var id: Long = 1
    var regionId: Long = 1
    var created: Long = -1
    var modified: Long = -1
    var author: String? = null
    var text: String? = null
    var html: String? = null
    var title: String? = null
    var description: String? = null
    var audio: List<AudioDto>? = null
    var documents: List<DocumentDto>? = null
    var images: List<LinkDto>? = null
    var video: List<LinkDto>? = null
    var links: List<LinkDto>? = null
    var supercategory: ContentInfoDto? = null
}

class CategoryDto : ContentDto() {
    var totalPages: Long = -1
    var subcategories: List<ContentInfoDto>? = null
    var content: List<ContentDto>? = null
}

class ContentInfoDto {
    var id: Long = -1
    var title: String? = null
}

class AudioDto {
    var id: Long = -1
    var title: String? = null
    var link: String? = null
    var duration: Long = -1
}

class DocumentDto {
    var id: Long = -1
    var title: String? = null
    var link: String? = null
    var type: Long = -1
}

class LinkDto {
    var id: Long = -1
    var title: String? = null
    var link: String? = null
}

class EvaDirectoryDTO {
    var directoryId: Long = 0

    val image: EvaImageDTO? = null

    val paket: Int = 0

    @SerializedName("jos")
    val more: Int = 0

    @SerializedName("subcat")
    private val _subDirectoryMetadataList: List<EvaDirectoryMetadataDTO>? = null
    val subDirectoryMetadataList: List<EvaDirectoryMetadataDTO>
        get() = _subDirectoryMetadataList ?: emptyList()

    @SerializedName("vijesti")
    private val _contentMetadataList: List<EvaContentMetadataDTO>? = null
    val contentMetadataList: List<EvaContentMetadataDTO>
        get() = _contentMetadataList ?: emptyList()
}

class EvaDirectoryMetadataDTO {
    @SerializedName("cid")
    val directoryId: Long = 0
    @SerializedName("name")
    val title: String? = null
}

class EvaContentMetadataDTO {

    @SerializedName("nid")
    val contentId: Long = -1
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
    val hasVideo: Boolean = false
    @SerializedName("documents")
    val hasDocuments: Boolean = false
    @SerializedName("music")
    val hasMusic: Boolean = false
    @SerializedName("images")
    val hasImages: Boolean = false
    @SerializedName("text")
    val hasText: Boolean = false
}

class EvaContentDTO {
    @SerializedName("nid")
    val contentId: Long = 0

    @SerializedName("cid")
    val directoryId: Long = 0

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
    val naziv: String? = null
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