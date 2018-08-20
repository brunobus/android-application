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

    class PlayerChange(val newPlayer: PlayerWrapper, val oldPlayer: PlayerWrapper)

    class PlayerWrapper(val player: ExoPlayer, var playbackId: String? = null)

    private val wrapperAlpha: PlayerWrapper = PlayerWrapper(createDefaultExoPlayer(context))
    private val wrapperBeta: PlayerWrapper = PlayerWrapper(createDefaultExoPlayer(context))

    init {
        wrapperAlpha.player.addListener(StartServiceIfNeeded(wrapperAlpha, wrapperBeta))
        wrapperBeta.player.addListener(StartServiceIfNeeded(wrapperBeta, wrapperAlpha))
    }

    inner class StartServiceIfNeeded(val thisPlayer: PlayerWrapper, val otherPlayer: PlayerWrapper) : Player.DefaultEventListener() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if ((playbackState == Player.STATE_READY)) {
                if (playWhenReady) {
                    val ctx = NovaEvaApp.instance ?: return
                    ContextCompat.startForegroundService(ctx, Intent(ctx, AudioPlayerService::class.java))

                    playerChangeSubject.onNext(PlayerChange(thisPlayer, otherPlayer))
                }
            } else if (playbackState == Player.STATE_IDLE) {
                thisPlayer.playbackId = null
            }
        }
    }

    val playerChangeSubject = BehaviorSubject.create<PlayerChange>()

    init {
        playerChangeSubject.subscribe { pc ->
            pc.oldPlayer.apply {
                player.playWhenReady = false
                player.stop(true)
            }
        }
    }

//    val currentPlaybackId: String?
//        get() = when {
//            !currentPlayerWrapper.player.isPlaying() -> null
//            else -> currentPlayerWrapper.playbackId
//        }

    fun prepareIfNeeded(preparingPlaybackId: String, doAutoPlay: Boolean = false, mediaSourceProvider: () -> MediaSource) {
        val latestPlaybackChange = playerChangeSubject.value

        val currentPlayer = latestPlaybackChange?.newPlayer ?: wrapperAlpha
        val standbyPlayer = latestPlaybackChange?.oldPlayer ?: wrapperBeta

        when {
            preparingPlaybackId == currentPlayer.playbackId -> {
                if (doAutoPlay) {
                    currentPlayer.player.playWhenReady = true
                }
            }
            currentPlayer.player.isStopped() || doAutoPlay -> {
                currentPlayer.apply {
                    player.playWhenReady = false
                    playbackId = preparingPlaybackId
                    player.prepare(mediaSourceProvider())
                    player.playWhenReady = doAutoPlay
                }

            }
            else -> {
                standbyPlayer.apply {
                    player.playWhenReady = false
                    playbackId = preparingPlaybackId
                    player.prepare(mediaSourceProvider())
                }
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
        wrapperAlpha.player.stop()
        wrapperBeta.player.stop()
    }

    fun pause() {
        wrapperAlpha.player.playWhenReady = false
        wrapperBeta.player.playWhenReady = false
    }

    fun addListener(listener: Player.EventListener) {
        wrapperAlpha.player.addListener(listener)
        wrapperBeta.player.addListener(listener)
    }

    fun removeListener(listener: Player.EventListener) {
        wrapperAlpha.player.removeListener(listener)
        wrapperBeta.player.removeListener(listener)
    }

    fun supplyPlayerToView(playerView: PlayerView, playbackId: String) {
        playerView.player = if (playbackId == wrapperAlpha.playbackId) wrapperAlpha.player else wrapperBeta.player
    }
}