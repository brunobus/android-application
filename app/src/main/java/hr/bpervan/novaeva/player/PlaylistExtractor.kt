package hr.bpervan.novaeva.player

/**
 *
 */
object PlaylistExtractor {

    fun extractStreamLinksFromPlaylist(playlistLines: List<String>, playlistFileUri: String): List<String> {
        val extension = playlistFileUri.substringAfterLast(".").toLowerCase()
        return when (extension) {
            "m3u" -> extractStreamLinksFromM3U(playlistLines)
            "pls" -> extractStreamLinksFromPLS(playlistLines)
            else -> emptyList()
        }
    }

    private fun extractStreamLinksFromPLS(playlistLines: List<String>): List<String> {
        return playlistLines
                .filter { it.startsWith("File") }
                .map { it.substringAfter("=") }
    }

    private fun extractStreamLinksFromM3U(playlistLines: List<String>): List<String> {
        return playlistLines.toList()
    }
}