package hr.bpervan.novaeva.player

import com.google.android.exoplayer2.Player

/**
 *
 */

fun Player.isPlaying(): Boolean {
    return playbackState != Player.STATE_ENDED
            && playbackState != Player.STATE_IDLE
            && playWhenReady
}

fun Player.isPaused(): Boolean {
    return playbackState != Player.STATE_ENDED
            && playbackState != Player.STATE_IDLE
            && !playWhenReady
}

fun Player.isStopped(): Boolean {
    return playbackState == Player.STATE_IDLE
            || playbackState == Player.STATE_ENDED
}