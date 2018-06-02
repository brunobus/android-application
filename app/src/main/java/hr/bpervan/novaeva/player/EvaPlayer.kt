package hr.bpervan.novaeva.player

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import io.reactivex.subjects.BehaviorSubject

/**
 *
 */
class EvaPlayer(context: Context) {

    class CurrentPlayerChangeData(val newPlayer: ExoPlayer, val oldPlayer: ExoPlayer)

    private val playerAlpha: ExoPlayer = createDefaultExoPlayer(context)
    private val playerBeta: ExoPlayer = createDefaultExoPlayer(context)

    val currentPlayerChange = BehaviorSubject.createDefault(CurrentPlayerChangeData(playerAlpha, playerBeta))

    var currentPlayer: ExoPlayer = playerAlpha
        set(newPlayer) {
            val oldPlayer = field
            if (oldPlayer == newPlayer) return
            oldPlayer.playWhenReady = false
            oldPlayer.stop()
            field = newPlayer
            currentPlayerChange.onNext(CurrentPlayerChangeData(newPlayer, oldPlayer))
        }
    private val otherPlayer: ExoPlayer
        get() = if (currentPlayer == playerAlpha) playerBeta else playerAlpha

    var currentPlaybackId: String? = null

    fun prepareIfNeededAndGetPlayer(playbackId: String, mediaSourceProvider: () -> MediaSource): ExoPlayer {
        val currentPlayer = this.currentPlayer

        return when {
            currentPlayer.isStopped() -> {
                currentPlayer.playWhenReady = false
                currentPlayer.prepare(mediaSourceProvider())
                currentPlayer
            }
            playbackId == currentPlaybackId -> currentPlayer /*don't prepare*/
            else -> {
                otherPlayer.playWhenReady = false
                otherPlayer.prepare(mediaSourceProvider())
                otherPlayer
            }
        }
    }

    private fun createDefaultExoPlayer(context: Context): ExoPlayer {
        val bandwidthMeter = DefaultBandwidthMeter()
        val factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(factory)
        return ExoPlayerFactory.newSimpleInstance(context, trackSelector)
    }
}