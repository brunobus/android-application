package hr.bpervan.novaeva.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R

typealias MediaStyleCompat = android.support.v4.media.app.NotificationCompat.MediaStyle

/**
 * Created by vpriscan on 08.12.17..
 */
class AudioPlayerService : Service() {

    companion object {
        @JvmStatic
        private lateinit var mediaSession: MediaSessionCompat
    }

    private lateinit var mediaSessionConnector: MediaSessionConnector
    private val playerEventListener = EvaServicePlayerEventListener()

    private lateinit var audioManager: AudioManager
    private lateinit var novaEvaBitmap: Bitmap

    override fun onCreate() {
        super.onCreate()

        novaEvaBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, "novaEvaMediaSession")
        mediaSession.setMediaButtonReceiver(null)
        mediaSession.isActive = true

        mediaSessionConnector = MediaSessionConnector(mediaSession, EvaPlaybackController())

        NovaEvaApp.evaPlayer.currentPlayerChange.subscribe {
            it.oldPlayer.removeListener(playerEventListener)
            val newPlayer = it.newPlayer

            playerEventListener.onPlayerStateChanged(newPlayer.playWhenReady, newPlayer.playbackState)
            newPlayer.addListener(playerEventListener)
            mediaSessionConnector.setPlayer(newPlayer, null)
        }
    }

    private inner class EvaServicePlayerEventListener : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                val notification = buildNotification(this@AudioPlayerService, mediaSession, playWhenReady)
                startForeground(33313331, notification)
                if (!playWhenReady) {
                    stopForeground(false)
                }
            } else {
                stopForeground(true)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel(): String {
        val channelId = "nova_eva_audio_player_service"
        val channelName = "Nova Eva Audio Player Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            channel.enableLights(false)
            channel.setShowBadge(false)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }
        return channelId
    }

    /**
     * Must not be "inner" class to work
     */
    class EvaMediaReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            MediaButtonReceiver.handleIntent(mediaSession, intent)
        }
    }

    private fun getCurrentPlayer(): Player? {
        return NovaEvaApp.evaPlayer.currentPlayerChange.value?.newPlayer
    }

    private inner class EvaPlaybackController : DefaultPlaybackController() {

        val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
            when (it) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    getCurrentPlayer()?.playWhenReady = true
                }
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    getCurrentPlayer()?.playWhenReady = false
                }
            }
        }

        override fun onPlay(player: Player) {
            @Suppress("DEPRECATION")
            val focusRequestResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)

            if (focusRequestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                player.playWhenReady = true
            }
        }

        override fun onStop(player: Player) {
            player.playWhenReady = false
            player.stop()
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }

        override fun onPause(player: Player) {
            player.playWhenReady = false
        }
    }

    private fun buildNotification(context: Context, mediaSession: MediaSessionCompat,
                                  playing: Boolean): Notification {
        val controller = mediaSession.controller

        val playPauseIcon = if (playing) R.drawable.exo_controls_pause else R.drawable.exo_controls_play
        val playPauseString = if (playing) getString(R.string.pause) else getString(R.string.play)

//        val contentText = NovaEvaApp.evaPlayer.currentPlaybackId

        return NotificationCompat.Builder(context, createNotificationChannel())
                .setLargeIcon(novaEvaBitmap)
                .setSmallIcon(R.drawable.notification_icon)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setContentTitle("Nova Eva")
//                .setContentText(contentText)
                .setContentIntent(controller.sessionActivity) //todo set
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(ContextCompat.getColor(context, R.color.novaEva))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(NotificationCompat.Action(playPauseIcon, playPauseString,
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .setStyle(MediaStyleCompat()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                        PlaybackStateCompat.ACTION_STOP))
                )
                .build()
    }

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}