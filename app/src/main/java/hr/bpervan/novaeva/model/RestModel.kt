package hr.bpervan.novaeva.model

import com.google.gson.annotations.SerializedName

/**
 * All constructor parameters must have a default value specified
 * so that no-argument constructor is created and GSON invokes it
 */
class EvaDirectoryDTO {
    var directoryId: Long = 0
    val image: EvaImageDTO? = null
    val paket: Int = 0
    @SerializedName("jos")
    val more: Int = 0
    @SerializedName("subcat")
    val subDirectoryMetadataList: List<EvaDirectoryMetadataDTO> = emptyList()
    @SerializedName("vijesti")
    val contentMetadataList: List<EvaContentMetadataDTO> = emptyList()
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
    val attachments: List<EvaAttachmentDTO> = emptyList()
    @SerializedName("image")
    val images: List<EvaImageDTO> = emptyList()
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
    val searchResultContentMetadataList: List<EvaContentMetadataDTO> = emptyList()
}

class EvaIndicatorsDTO {
    @SerializedName("354")
    val duhovnost: Int? = null
    @SerializedName("9")
    val aktualno: Int? = null
    @SerializedName("1")
    val izreke: Int? = null
    @SerializedName("10")
    val multimedija: Int? = null
    @SerializedName("4")
    val evandjelje: Int? = null
    @SerializedName("7")
    val propovijedi: Int? = null
    @SerializedName("8")
    val poziv: Int? = null
    @SerializedName("11")
    val odgovori: Int? = null
    @SerializedName("355")
    val pjesmarica: Int? = null
}