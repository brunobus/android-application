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
import android.media.AudioAttributes
import android.media.AudioFocusRequest
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
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.util.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

typealias MediaStyleCompat = android.support.v4.media.app.NotificationCompat.MediaStyle

/**
 * Created by vpriscan on 08.12.17..
 */
class AudioPlayerService : Service() {

    companion object {
        @JvmStatic
        private lateinit var mediaSession: MediaSessionCompat
    }

    private val disposables = CompositeDisposable()

    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var audioManager: AudioManager
    private lateinit var novaEvaBitmap: Bitmap

    override fun onCreate() {
        super.onCreate()

        novaEvaBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, "novaEvaMediaSession")
        mediaSession.setMediaButtonReceiver(null)
        mediaSession.isActive = true

        mediaSessionConnector = MediaSessionConnector(mediaSession, EvaPlaybackController())

        disposables += EventPipelines.playbackStartStopPause
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    val player = it.player

                    when (player.playbackState) {
                        Player.STATE_READY -> {
                            if (player.playWhenReady || player.contentPosition > 0) {

                                val notification =
                                        buildNotification(mediaSession,
                                                player.playWhenReady, it.playbackInfo?.title ?: "")
                                val notificationId = 33313331

                                startForeground(notificationId, notification)

                                if (!player.playWhenReady) {
                                    stopForeground(false)
                                }

                                mediaSessionConnector.setPlayer(player, null)
                            }
                        }
                        Player.STATE_IDLE, Player.STATE_ENDED -> {
                            stopForeground(true)
                        }
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

    private inner class EvaPlaybackController : DefaultPlaybackController() {

        var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
        var audioFocusRequest: AudioFocusRequest? = null

        override fun onPlay(player: Player) {
            val focusRequestResult =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                                .setAudioAttributes(AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                                        .build())
                                .build()

                        audioManager.requestAudioFocus(audioFocusRequest)
                    } else {
                        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
                            when (focusChange) {
                                AudioManager.AUDIOFOCUS_GAIN -> {
                                    /*dont autostart*/
                                }
                                AudioManager.AUDIOFOCUS_LOSS,
                                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                                    player.playWhenReady = false
                                }
                            }
                        }

                        @Suppress("DEPRECATION")
                        audioManager.requestAudioFocus(audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN)
                    }

            if (focusRequestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                player.playWhenReady = true
            }
        }

        override fun onStop(player: Player) {
            player.stop()
            player.playWhenReady = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let {
                    audioManager.abandonAudioFocusRequest(it)
                }
            } else {
                @Suppress("DEPRECATION")
                audioFocusChangeListener?.let {
                    audioManager.abandonAudioFocus(it)
                }
            }
        }

        override fun onPause(player: Player) {
            player.playWhenReady = false
        }
    }

    private fun buildNotification(mediaSession: MediaSessionCompat,
                                  playing: Boolean,
                                  contentText: String = ""): Notification {
        val controller = mediaSession.controller
        val context = this

        val playPauseIcon = if (playing) R.drawable.exo_controls_pause else R.drawable.exo_controls_play
        val playPauseString = if (playing) getString(R.string.pause) else getString(R.string.play)

        val stopIcon = R.drawable.player_controls_stop
        val stopText = getString(R.string.stop)

        return NotificationCompat.Builder(context, createNotificationChannel())
                .setLargeIcon(novaEvaBitmap)
                .setSmallIcon(R.drawable.notification_icon)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setContentTitle("Nova Eva")
                .setContentText(contentText)
                .setContentIntent(controller.sessionActivity) //todo set
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(ContextCompat.getColor(context, R.color.novaEva))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(NotificationCompat.Action(playPauseIcon, playPauseString,
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .addAction(NotificationCompat.Action(stopIcon, stopText,
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)))
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
        disposables.dispose()
        mediaSession.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}