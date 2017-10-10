package hr.bpervan.novaeva.model

/**
 * Created by vpriscan on 08.10.17..
 */

interface TreeElement {
    val isLeaf: Boolean
}

class DirectoryInfo(var dirId: Long = -1,
                    var title: String? = null) : TreeElement {
    override val isLeaf = false
}

class ContentInfo(var contentId: Long = 0,
                  var dirId: Long = 0,
                  var title: String? = null,
                  var summary: String? = null,
                  var datetime: String? = null,
                  var hasVideo: Boolean = false,
                  var hasDocuments: Boolean = false,
                  var hasMusic: Boolean = false,
                  var hasImages: Boolean = false,
                  var hasText: Boolean = false) : TreeElement {
    override val isLeaf = true
}