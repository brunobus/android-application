package hr.bpervan.novaeva.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R

typealias MediaStyleCompat = android.support.v4.media.app.NotificationCompat.MediaStyle

/**
 * todo
 * Created by vpriscan on 08.12.17..
 */
class AudioPlayerService : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager

    private lateinit var playbackStateBuilder: PlaybackStateCompat.Builder
//    lateinit var mediaMetadataBuilder: MediaMetadataCompat.Builder

    override fun onCreate() {
        super.onCreate()

        exoPlayer = (application as NovaEvaApp).exoPlayer

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, "novaEvaMediaSession")
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

//        mediaSession.setCallback(mediaSessionCallback)

        playbackStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

        mediaSession.setPlaybackState(playbackStateBuilder.build())

        val mediaSessionConnector = MediaSessionConnector(mediaSession, MyPlaybackController())

        mediaSessionConnector.setPlayer(exoPlayer, null, null)

        mediaSession.isActive = true

//        mediaMetadataBuilder = MediaMetadataCompat.Builder()
    }

    val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer.playWhenReady = true
            }
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                exoPlayer.playWhenReady = false
            }
        }
    }

    private inner class MyPlaybackController : DefaultPlaybackController() {
        override fun onPlay(player: Player?) {

            val focusRequestResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)

            if (focusRequestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                super.onPlay(player)
            }

            val notification = buildNotification(this@AudioPlayerService, mediaSession)
            startForeground(33313331, notification)
        }

        override fun onStop(player: Player?) {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
            super.onStop(player)
        }

        override fun onPause(player: Player?) {
            stopForeground(false)
            super.onPause(player)
        }
    }

//    private val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
//
//        override fun onPlay() {
//            exoPlayer.playWhenReady = true
//            mediaSession.isActive = true//todo?
//            startService(Intent(this@AudioPlayerService, AudioPlayerService::class.java))
//
//            val controller = mediaSession.controller
//            val description = controller.metadata.description
//
//            val notification = buildNotification(this@AudioPlayerService, description, controller)
//
//            startForeground(567, notification)
//        }
//
//        override fun onPause() {
//            exoPlayer.playWhenReady = false
//            stopForeground(false)
//        }
//
//        override fun onStop() {
//            exoPlayer.stop()
//            mediaSession.isActive = false
//            stopForeground(true)
//            stopSelf()
//        }
//    }

    private fun buildNotification(context: Context, mediaSession: MediaSessionCompat): Notification {
        val controller = mediaSession.controller
        val description = controller.metadata.description

        return NotificationCompat.Builder(context, "myChannel")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setSubText(description.description)
                .setLargeIcon(description.iconBitmap)
                .setContentIntent(controller.sessionActivity)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(ContextCompat.getColor(context, R.color.novaEva))
                .addAction(NotificationCompat.Action(R.drawable.player_btn_pause, "Pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .setStyle(MediaStyleCompat()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)//todo
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP))
                )
                .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        val CONTINUE_ACTION = "hr.bpervan.novaeva.CONTINUE_ACTION"
        val PAUSE_ACTION = "hr.bpervan.novaeva.PAUSE_ACTION"
        val STOP_ACTION = "hr.bpervan.novaeva.STOP_ACTION"
    }

    override fun onDestroy() {
        super.onDestroy()

        exoPlayer.release()
    }
}