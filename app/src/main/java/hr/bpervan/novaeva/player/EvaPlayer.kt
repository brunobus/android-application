package hr.bpervan.novaeva.player

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.services.AudioPlayerService
import io.reactivex.subjects.BehaviorSubject

/**
 *
 */
class EvaPlayer(context: Context) {

    class PlaybackChange(val player: ExoPlayer,
                         val playbackInfo: PlaybackInfo?)

    class PlaybackInfo(val id: String,
                       val title: String? = null)

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
            playbackChangeSubject.onNext(PlaybackChange(thisPlayer, playerPlaybackInfoMap[thisPlayer]))
        }
    }

    val playbackChangeSubject = BehaviorSubject.create<PlaybackChange>()

    fun prepareIfNeeded(playbackInfo: PlaybackInfo, doAutoPlay: Boolean = false, mediaSourceProvider: () -> MediaSource) {

        val currentPlayer = if (playerBeta.isPlaying()) playerBeta else playerAlpha
        val otherPlayer = if (currentPlayer == playerAlpha) playerBeta else playerAlpha

        when {
            playbackInfo.id == playerPlaybackInfoMap[currentPlayer]?.id -> {
                if (doAutoPlay) {
                    currentPlayer.playWhenReady = true
                }
            }
            currentPlayer.isStopped() || doAutoPlay -> {
                currentPlayer.apply {
                    playWhenReady = false
                    prepare(mediaSourceProvider())
                    playWhenReady = doAutoPlay
                }
                playerPlaybackInfoMap[currentPlayer] = playbackInfo

            }
            else -> {
                otherPlayer.apply {
                    playWhenReady = false
                    prepare(mediaSourceProvider())
                }
                playerPlaybackInfoMap[otherPlayer] = playbackInfo
            }
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
        return NovaEvaApp.evaPlayer.playbackChangeSubject.value?.playbackInfo
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