package hr.bpervan.novaeva.player

import io.reactivex.Single
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 */
fun getStreamLinksFromPlaylistUri(playlistFileUri: String): Single<List<String>> {
    return Single
            .create<List<String>> { emitter ->
                val url = URL(playlistFileUri)
                val httpConnection = url.openConnection() as HttpURLConnection

                try {
                    if (httpConnection.responseCode == HttpURLConnection.HTTP_OK) {
                        httpConnection.inputStream.bufferedReader().useLines {
                            val streamUris = extractStreamLinksFromPlaylist(
                                    it.toList(), playlistFileUri)
                            emitter.onSuccess(streamUris)
                        }
                    } else throw RuntimeException("Http error ${httpConnection.responseCode} for $playlistFileUri")
                } finally {
                    httpConnection.disconnect()
                }
            }
}

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