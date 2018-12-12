package hr.bpervan.novaeva.model

/**
 *
 */

object AttachmentIndicatorHelper {

    private const val videoMask = 1 shl 0
    private const val docsMask = 1 shl 1
    private const val musicMask = 1 shl 2
    private const val imagesMask = 1 shl 3
    private const val textMask = 1 shl 4

    fun hasVideo(encoded: Int) = encoded and videoMask != 0
    fun hasDocs(encoded: Int) = encoded and docsMask != 0
    fun hasMusic(encoded: Int) = encoded and musicMask != 0
    fun hasImages(encoded: Int) = encoded and imagesMask != 0
    fun hasText(encoded: Int) = encoded and textMask != 0

    fun encode(aiDto: EvaAttachmentsIndicatorDTO): Int {
        var encoded = 0

        fun setBit(mask: Int, set: Boolean) {
            encoded = if (set) (encoded or mask) else (encoded and mask.inv())
        }

        setBit(videoMask, aiDto.hasVideo)
        setBit(docsMask, aiDto.hasDocuments)
        setBit(musicMask, aiDto.hasMusic)
        setBit(imagesMask, aiDto.hasImages)
        setBit(textMask, aiDto.hasText)

        return encoded
    }
}
