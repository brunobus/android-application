package hr.bpervan.novaeva.player

import android.os.Handler
import androidx.core.net.toUri
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
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
                            val streamUris = PlaylistExtractor.extractStreamLinksFromPlaylist(
                                    it.toList(), playlistFileUri)
                            emitter.onSuccess(streamUris)
                        }
                    } else throw RuntimeException("Http error ${httpConnection.responseCode} for $playlistFileUri")
                } finally {
                    httpConnection.disconnect()
                }
            }
}

private val handler = Handler()

fun prepareAudioStream(audioUri: String, contentId: String, contentTitle: String, isRadio: Boolean, doAutoPlay: Boolean) {

    val context = NovaEvaApp.instance!!.applicationContext
    val dataSourceFactory = DefaultDataSourceFactory(context,
            Util.getUserAgent(context, context.resources.getString(R.string.app_name)),
            DefaultBandwidthMeter())

//        val factory = ExtractorMediaSource.Factory(dataSourceFactory).setCustomCacheKey(audioUri)
//        val mediaSource = factory.createMediaSource(streamingUri)

    NovaEvaApp.evaPlayer.prepareIfNeeded(EvaPlayer.PlaybackInfo(contentId, contentTitle, isRadio), doAutoPlay) {
        ExtractorMediaSource(audioUri.toUri(), dataSourceFactory, DefaultExtractorsFactory(),
                handler, null, audioUri)
    }
}