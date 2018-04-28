package hr.bpervan.novaeva.player

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.services.AudioPlayerService

/**
 *
 */
class EvaPlayerEventListener(
        private val contextSupplier: () -> Context?,
        private val exoPlayerSupplier: () -> ExoPlayer?,
        private val currentAudioUriSupplier: () -> String?
) : Player.DefaultEventListener() {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if ((playbackState == Player.STATE_READY)) {
            if (playWhenReady) {
                val exoPlayer = exoPlayerSupplier() ?: return
                NovaEvaApp.evaPlayer.currentAudioTrackUri = currentAudioUriSupplier()
                NovaEvaApp.evaPlayer.currentPlayer = exoPlayer
                val ctx = contextSupplier() ?: return
                ContextCompat.startForegroundService(ctx, Intent(ctx, AudioPlayerService::class.java))
            }
        }
    }
}