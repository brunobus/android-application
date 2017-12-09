package hr.bpervan.novaeva.player

import android.content.Context
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter

/**
 * Created by vpriscan on 09.12.17..
 */
class PlayerHolder(private val context: Context) {

    val exoPlayer: SimpleExoPlayer by lazy {
        val bandwidthMeter = DefaultBandwidthMeter()
        val factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(factory)
        ExoPlayerFactory.newSimpleInstance(context, trackSelector)
    }
}