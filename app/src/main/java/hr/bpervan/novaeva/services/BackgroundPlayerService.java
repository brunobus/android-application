package hr.bpervan.novaeva.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import hr.bpervan.novaeva.main.R;

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
public class BackgroundPlayerService extends IntentService {
    public static final String INTENT_CLASS = "novaeva-backgroun-audio-service";

    private static final String TAG = "BackgroundPlayerService";
    private static final int notificationId = 10000;

    public static final String KEY_DIRECTIVE = "directive";
    public static final String KEY_PATH = "path";
    public static final String KEY_TITLE = "title";
    public static final String KEY_ELAPSED_TIME = "elapsedTimeKey";
    public static final String KEY_TRACK_DURATION = "keyTrackDuration";

    public static final int DIRECTIVE_ERROR = -1;
    public static final int DIRECTIVE_SET_SOURCE_PLAY = 0;
    public static final int DIRECTIVE_PLAY = 1;
    public static final int DIRECTIVE_PAUSE = 2;
    public static final int DIRECTIVE_STOP = 3;

    public static final int DIRECTIVE_ENABLE_PAUSE_BUTTON = 4;
    public static final int DIRECTIVE_ENABLE_PLAY_BUTTON = 5;

    public static volatile boolean isRunning = false;
    private static volatile boolean isMediaPlayerPrepared = false;

    private NotificationManager notificationManager;

    private static final MediaPlayer mediaPlayer = new MediaPlayer();

    private final Timer timer = new Timer();

    public BackgroundPlayerService(){
        this(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundPlayerService(String name) {
        super(name);
    }

    @Override
    public void onCreate(){
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);

        isRunning = true;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int directive = intent.getIntExtra(KEY_DIRECTIVE, DIRECTIVE_ERROR);
        switch (directive) {
            case DIRECTIVE_ERROR:
                Log.d(TAG, "DIRECTIVE_ERROR");
                break;
            case DIRECTIVE_PAUSE:
                Log.d(TAG, "DIRECTIVE_PAUSE");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                mediaPlayer.pause();
                Intent enableStart = new Intent(INTENT_CLASS);
                enableStart.putExtra(KEY_DIRECTIVE, DIRECTIVE_ENABLE_PLAY_BUTTON);
                LocalBroadcastManager.getInstance(BackgroundPlayerService.this).sendBroadcast(enableStart);
                Log.d(TAG, mediaPlayer.isPlaying() + " playing");
                break;
            case DIRECTIVE_PLAY:
                Log.d(TAG, "DIRECTIVE_PLAY");
                if (isMediaPlayerPrepared && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                break;
            case DIRECTIVE_SET_SOURCE_PLAY:
                Log.d(TAG, "DIRECTIVE_SET_SOURCE_PLAY");
                String audioUrl = intent.getStringExtra(KEY_PATH);
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                try {
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            isMediaPlayerPrepared = true;
                            mp.start();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    int seconds = (int) (mediaPlayer.getCurrentPosition() / 1000) % 60 ;
                                    int minutes = (int) ((mediaPlayer.getCurrentPosition() / (1000*60)) % 60);

                                    Intent broadcastIntent = new Intent(INTENT_CLASS);
                                    broadcastIntent.putExtra(KEY_ELAPSED_TIME, mediaPlayer.getCurrentPosition());
                                    LocalBroadcastManager.getInstance(BackgroundPlayerService.this).sendBroadcast(broadcastIntent);
                                }
                            }, 0, 1000);
                            Intent enablePause = new Intent(INTENT_CLASS);
                            enablePause.putExtra(KEY_DIRECTIVE, DIRECTIVE_ENABLE_PAUSE_BUTTON);
                            enablePause.putExtra(KEY_TRACK_DURATION, mediaPlayer.getDuration());
                            LocalBroadcastManager.getInstance(BackgroundPlayerService.this).sendBroadcast(enablePause);
                        }
                    });
                    mediaPlayer.prepareAsync();

                    Intent pauseIntent = new Intent(this, BackgroundPlayerService.class);
                    pauseIntent.putExtra(KEY_DIRECTIVE, DIRECTIVE_PAUSE);
                    //PendingIntent pendingIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), pauseIntent, 0);
                    PendingIntent pendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
                    Notification notification = new NotificationCompat.Builder(this)
                            .setContentTitle("Nova Eva")
                            .setContentText(intent.getStringExtra(KEY_TITLE))
                            .setSmallIcon(R.drawable.ic_launcher)
                            //.setOngoing(true)
                            .addAction(R.drawable.player_btn_pause, "Pause", pendingIntent)
                            .build();
                    notificationManager.notify(notificationId, notification);
                } catch (IOException e) {
                }
                break;
            case DIRECTIVE_STOP:
                Log.d(TAG, "DIRECTIVE_STOP");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    isMediaPlayerPrepared = false;
                }
                break;
        }
    }

    @Override
    public void onDestroy(){
        //notificationManager.cancel(0);
        //notificationManager.cancel(notificationId);
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
