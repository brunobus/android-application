package hr.bpervan.novaeva.model

/**
 *
 */

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

typealias CategoryInfoDto = ContentInfoDto

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
    var type: String? = null
}

class LinkDto {
    var id: Long = -1
    var title: String? = null
    var link: String? = null
}