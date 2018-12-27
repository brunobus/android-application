package hr.bpervan.novaeva.model

import com.google.gson.annotations.SerializedName
import hr.bpervan.novaeva.rest.EvaDomain

/**
 *
 */

class ContentInfoDto {
    var id: Long = -1
    var title: String? = null
}

typealias CategoryInfoDto = ContentInfoDto

open class ContentDto {
    var id: Long = 1
    var regionId: Long = 1
    var domain: EvaDomain? = null
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
    var subcategories: List<CategoryInfoDto>? = null
    var content: List<ContentDto>? = null
}

open class LinkDto {
    var id: Long = -1
    var title: String? = null
    var link: String? = null
}

class AudioDto : LinkDto() {
    var duration: Long = -1
}

class DocumentDto : LinkDto() {
    var type: String? = null
}

val noChanges = LatestDto()

class LatestByDomainDto {
    var songs: LatestDto = noChanges
    var multimedia: LatestDto = noChanges
    var spirituality: LatestDto = noChanges
    var sermons: LatestDto = noChanges
    var trending: LatestDto = noChanges
    var vocation: LatestDto = noChanges
    var answers: LatestDto = noChanges
    var prayers: LatestDto = noChanges
}

class LatestDto {
    var content: Long? = null
    var category: Long? = null
}