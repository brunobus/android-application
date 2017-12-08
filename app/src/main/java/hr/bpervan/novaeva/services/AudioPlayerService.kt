package hr.bpervan.novaeva.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import hr.bpervan.novaeva.RxEventBus
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * todo
 * Created by vpriscan on 08.12.17..
 */
class AudioPlayerService : Service() {

    private val exoPlayer: ExoPlayer by lazy {
        val bandwidthMeter = DefaultBandwidthMeter()
        val factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(factory)
        ExoPlayerFactory.newSimpleInstance(this, trackSelector)
    }
    override fun onCreate() {
        super.onCreate()
//
//        RxEventBus.subjectExoPlayer
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    exoPlayer = it
//                }
    }

//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        intent.action?.let {
//            when (it) {
//                CONTINUE_ACTION -> {
//                    exoPlayer?.playWhenReady = true
//                }
//                PAUSE_ACTION -> {
//                    exoPlayer?.playWhenReady = false
//                }
//                else -> {
//                    exoPlayer?.stop()
//                }
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId)
//    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        val CONTINUE_ACTION = "hr.bpervan.novaeva.CONTINUE_ACTION"
        val PAUSE_ACTION = "hr.bpervan.novaeva.PAUSE_ACTION"
        val STOP_ACTION = "hr.bpervan.novaeva.STOP_ACTION"

//        val subjectExoPlayer: BehaviorSubject<ExoPlayer> = BehaviorSubject.create()
    }
}