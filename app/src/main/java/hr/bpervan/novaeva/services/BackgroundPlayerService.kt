package hr.bpervan.novaeva.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import java.io.IOException
import java.util.Timer
import java.util.TimerTask

import hr.bpervan.novaeva.main.R

/**
 * Scenariji:
 * 1. VijestActivity šalje DIRECTIVE_SET_SOURCE_PLAY i locka ekran ili gasi aplikaciju
 * 2. Korisnik otključava mobitel, activity se otvara
 * 3. Korisnik je gdje god, pritišće gumb u notification baru
 * 4. Korisnik je gdje god, otvara opet aplikaciju
 * 5. Korisnik je gdje god, otvara iz recent appova aplikaciju
 * 6. Korisnik nakon što je pustio audio, nastavlja zujati po aplikaciji
 *
 * Komunikacija nazad:
 * 1. Event svake sekunde koji broadcasta trenutnu poziciju mediaplayera
 * 2. Event koji na set source and play broadcasta ukupno trajanje tracka ako je aplikabilno (.mp3 vs radio stream)
 *
 * Komunikacija unutra:
 * 1. set source and play
 * 2. pause
 * 3. stop
 * 4. resume/play
 * 5. seekto
 */
class BackgroundPlayerService
/**
 * Creates an IntentService.  Invoked by your subclass's constructor.
 *
 * @param name Used to name the worker thread, important only for debugging.
 */
@JvmOverloads constructor(name: String = TAG) : IntentService(name) {

    private lateinit var notificationManager: NotificationManager

    private val timer = Timer()

    override fun onCreate() {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        isRunning = true
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        if(intent==null) return


        val directive = intent.getIntExtra(KEY_DIRECTIVE, DIRECTIVE_ERROR)
        when (directive) {
            DIRECTIVE_ERROR -> Log.d(TAG, "DIRECTIVE_ERROR")
            DIRECTIVE_PAUSE -> {
                Log.d(TAG, "DIRECTIVE_PAUSE")
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
                mediaPlayer.pause()
                val enableStart = Intent(INTENT_CLASS)
                enableStart.putExtra(KEY_DIRECTIVE, DIRECTIVE_ENABLE_PLAY_BUTTON)
                LocalBroadcastManager.getInstance(this@BackgroundPlayerService).sendBroadcast(enableStart)
                Log.d(TAG, mediaPlayer.isPlaying.toString() + " playing")
            }
            DIRECTIVE_PLAY -> {
                Log.d(TAG, "DIRECTIVE_PLAY")
                if (isMediaPlayerPrepared && !mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            }
            DIRECTIVE_SET_SOURCE_PLAY -> {
                Log.d(TAG, "DIRECTIVE_SET_SOURCE_PLAY")
                val audioUrl = intent.getStringExtra(KEY_PATH)
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                try {
                    mediaPlayer.setDataSource(audioUrl)
                    mediaPlayer.setOnPreparedListener { mp ->
                        isMediaPlayerPrepared = true
                        mp.start()
                        timer.schedule(object : TimerTask() {
                            override fun run() {
                                val seconds = (mediaPlayer.currentPosition / 1000) % 60
                                val minutes = (mediaPlayer.currentPosition / (1000 * 60) % 60)

                                val broadcastIntent = Intent(INTENT_CLASS)
                                broadcastIntent.putExtra(KEY_ELAPSED_TIME, mediaPlayer.currentPosition)
                                LocalBroadcastManager.getInstance(this@BackgroundPlayerService).sendBroadcast(broadcastIntent)
                            }
                        }, 0, 1000)
                        val enablePause = Intent(INTENT_CLASS)
                        enablePause.putExtra(KEY_DIRECTIVE, DIRECTIVE_ENABLE_PAUSE_BUTTON)
                        enablePause.putExtra(KEY_TRACK_DURATION, mediaPlayer.duration)
                        LocalBroadcastManager.getInstance(this@BackgroundPlayerService).sendBroadcast(enablePause)
                    }
                    mediaPlayer.prepareAsync()

                    val pauseIntent = Intent(this, BackgroundPlayerService::class.java)
                    pauseIntent.putExtra(KEY_DIRECTIVE, DIRECTIVE_PAUSE)
                    //PendingIntent pendingIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), pauseIntent, 0);
                    val pendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0)
                    val notification = NotificationCompat.Builder(this)
                            .setContentTitle("Nova Eva")
                            .setContentText(intent.getStringExtra(KEY_TITLE))
                            .setSmallIcon(R.drawable.ic_launcher)
                            //.setOngoing(true)
                            .addAction(R.drawable.player_btn_pause, "Pause", pendingIntent)
                            .build()
                    notificationManager.notify(notificationId, notification)
                } catch (e: IOException) {
                }

            }
            DIRECTIVE_STOP -> {
                Log.d(TAG, "DIRECTIVE_STOP")
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    isMediaPlayerPrepared = false
                }
            }
        }
    }

    override fun onDestroy() {
        //notificationManager.cancel(0);
        //notificationManager.cancel(notificationId);
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        super.onDestroy()
    }

    companion object {
        val INTENT_CLASS = "novaeva-backgroun-audio-service"

        private val TAG = "BackgroundPlayerService"
        private val notificationId = 10000

        val KEY_DIRECTIVE = "directive"
        val KEY_PATH = "path"
        val KEY_TITLE = "title"
        val KEY_ELAPSED_TIME = "elapsedTimeKey"
        val KEY_TRACK_DURATION = "keyTrackDuration"

        val DIRECTIVE_ERROR = -1
        val DIRECTIVE_SET_SOURCE_PLAY = 0
        val DIRECTIVE_PLAY = 1
        val DIRECTIVE_PAUSE = 2
        val DIRECTIVE_STOP = 3

        val DIRECTIVE_ENABLE_PAUSE_BUTTON = 4
        val DIRECTIVE_ENABLE_PLAY_BUTTON = 5

        @Volatile
        var isRunning = false
        @Volatile private var isMediaPlayerPrepared = false

        private val mediaPlayer = MediaPlayer()
    }
}
