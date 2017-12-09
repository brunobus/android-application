package hr.bpervan.novaeva.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.main.R
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * todo
 * Created by vpriscan on 08.12.17..
 */
typealias MediaStyleCompat = android.support.v4.media.app.NotificationCompat.MediaStyle

class AudioPlayerService : Service() {

    private val exoPlayer: ExoPlayer by lazy {
        val bandwidthMeter = DefaultBandwidthMeter()
        val factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(factory)
        ExoPlayerFactory.newSimpleInstance(this, trackSelector)
    }

    lateinit var mediaSession: MediaSessionCompat
    lateinit var audioManager: AudioManager

    lateinit var playbackStateBuilder: PlaybackStateCompat.Builder
//    lateinit var mediaMetadataBuilder: MediaMetadataCompat.Builder

    override fun onCreate() {

        super.onCreate()

//        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, "novaEvaMediaSession")
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        mediaSession.setCallback(mediaSessionCallback)

        playbackStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

        mediaSession.setPlaybackState(playbackStateBuilder.build())

//        mediaMetadataBuilder = MediaMetadataCompat.Builder()
//
//        RxEventBus.subjectExoPlayer
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    exoPlayer = it
//                }
    }

    val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            exoPlayer.playWhenReady = true
            mediaSession.isActive = true
            startService(Intent(this@AudioPlayerService, AudioPlayerService::class.java))

            val controller = mediaSession.controller
            val desccription = controller.metadata.description

            val notification: Notification = NotificationCompat.Builder(this@AudioPlayerService, "myChannel")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(desccription.title)
                    .setContentText(desccription.subtitle)
                    .setSubText(desccription.description)
                    .setLargeIcon(desccription.iconBitmap)
                    .setContentIntent(controller.sessionActivity)
                    .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this@AudioPlayerService,
                            PlaybackStateCompat.ACTION_STOP))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(ContextCompat.getColor(this@AudioPlayerService, R.color.novaEva))
                    .addAction(NotificationCompat.Action(R.drawable.player_btn_pause, "Pause",
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this@AudioPlayerService,
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                    .setStyle(MediaStyleCompat()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0)//todo
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this@AudioPlayerService,
                                    PlaybackStateCompat.ACTION_STOP))
                    )
                    .build()

            startForeground(567, notification)
        }

        override fun onPause() {
            exoPlayer.playWhenReady = false
            stopForeground(false)
        }

        override fun onStop() {
            exoPlayer.stop()
            mediaSession.isActive = false
            stopForeground(true)
            stopSelf()
        }
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

    override fun onDestroy() {
        super.onDestroy()

        exoPlayer.release()
    }
}