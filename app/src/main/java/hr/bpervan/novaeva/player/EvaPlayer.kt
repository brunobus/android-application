package hr.bpervan.novaeva.player

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.v4.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.EventPipelines.playbackChanged
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.services.AudioPlayerService

/**
 *
 */
class EvaPlayer(context: Context) {

    class PlaybackChange(val player: ExoPlayer,
                         val playbackInfo: PlaybackInfo?)

    class PlaybackInfo(val id: String,
                       val title: String?,
                       val isRadio: Boolean)

    private val playerAlpha: ExoPlayer = createDefaultExoPlayer(context)
    private val playerBeta: ExoPlayer = createDefaultExoPlayer(context)

    private val playerPlaybackInfoMap = mutableMapOf<ExoPlayer, PlaybackInfo?>()

    init {
        playerAlpha.addListener(StartServiceIfNeeded(playerAlpha, playerBeta))
        playerBeta.addListener(StartServiceIfNeeded(playerBeta, playerAlpha))
    }

    inner class StartServiceIfNeeded(private val thisPlayer: ExoPlayer,
                                     private val otherPlayer: ExoPlayer) : Player.DefaultEventListener() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if ((playbackState == Player.STATE_READY)) {
                if (playWhenReady) {

                    otherPlayer.playWhenReady = false
                    otherPlayer.stop(true)

                    val ctx = NovaEvaApp.instance ?: return
                    ContextCompat.startForegroundService(ctx, Intent(ctx, AudioPlayerService::class.java))
                }
            } else if (playbackState == Player.STATE_IDLE) {
                playerPlaybackInfoMap[thisPlayer] = null
            }
            emitPlaybackChange()
        }

        private fun emitPlaybackChange() {
            playbackChanged.onNext(PlaybackChange(thisPlayer, playerPlaybackInfoMap[thisPlayer]))
        }
    }

    private val handler = Handler()

    fun prepareAudioStream(audioUri: String, contentId: String, contentTitle: String, isRadio: Boolean, doAutoPlay: Boolean) {

        val context = NovaEvaApp.instance ?: return

        prepareIfNeeded(EvaPlayer.PlaybackInfo(contentId, contentTitle, isRadio), doAutoPlay) {
            //        val factory = ExtractorMediaSource.Factory(dataSourceFactory).setCustomCacheKey(audioUri)
//        val mediaSource = factory.createMediaSource(streamingUri)
            val dataSourceFactory = DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, context.resources.getString(R.string.app_name)),
                    DefaultBandwidthMeter())

            ExtractorMediaSource(audioUri.toUri(), dataSourceFactory, DefaultExtractorsFactory(),
                    handler, null, audioUri)
        }
    }

    private fun Player.isPlaying(): Boolean {
        return (playbackState == Player.STATE_READY
                || playbackState == Player.STATE_BUFFERING) && playWhenReady
    }

    private fun Player.isStopped(): Boolean {
        return playbackState == Player.STATE_IDLE
                || playbackState == Player.STATE_ENDED
    }

    private fun prepareIfNeeded(playbackInfo: PlaybackInfo, doAutoPlay: Boolean = false, mediaSourceProvider: () -> MediaSource) {

        val currentPlayer = if (playerBeta.isPlaying()) playerBeta else playerAlpha
        val otherPlayer = if (currentPlayer == playerAlpha) playerBeta else playerAlpha

        if (playbackInfo.id == playerPlaybackInfoMap[currentPlayer]?.id) {
            if (doAutoPlay) {
                currentPlayer.playWhenReady = true
            }
            return
        }

        val playerToUse = if (currentPlayer.isStopped()) currentPlayer else otherPlayer

        playerToUse.apply {
            playWhenReady = false
            prepare(mediaSourceProvider())
            playWhenReady = doAutoPlay
            playerPlaybackInfoMap[this] = playbackInfo
        }
    }

    private fun createDefaultExoPlayer(context: Context): ExoPlayer {
        val bandwidthMeter = DefaultBandwidthMeter()
        val factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(factory)
        return ExoPlayerFactory.newSimpleInstance(context, trackSelector)
    }

    fun stop() {
        playerAlpha.stop()
        playerBeta.stop()
    }

    fun pause() {
        playerAlpha.playWhenReady = false
        playerBeta.playWhenReady = false
    }

    fun currentPlaybackInfo(): PlaybackInfo? {
        return EventPipelines.playbackChanged.value?.playbackInfo
    }

    fun supplyPlayerToView(playerView: PlayerView, playbackId: String) {
        for (playerTrack in playerPlaybackInfoMap) {
            if (playerTrack.value?.id == playbackId) {
                playerView.player = playerTrack.key
                return
            }
        }
    }
}