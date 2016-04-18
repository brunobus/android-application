package hr.bpervan.novaeva.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Branimir on 17.4.2016..
 */
public class BackgroundPlayerService extends Service {
    public static final int MSG_PLAY = 0;
    public static final int MSG_PAUSE = 1;
    public static final int MSG_STOP = 2;

    public static final int MSG_SET_SOURCE = 3;

    private static final String TAG = "BackgroundPlayerService";

    //private final NotificationManager notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
    private final Messenger messenger = new Messenger(new IncomingMessageHandler());

    private final MediaPlayer mediaPlayer = new MediaPlayer();


    @Override
    public void onCreate(){
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "onStartCommand");
        /*String audioUrl = intent.getStringExtra("audioUrl");
        if(audioUrl == null || audioUrl.isEmpty()){
            //banana, handle somehow
        }

        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            try {
                mediaPlayer.setDataSource(audioUrl);
            } catch (IOException e){
                //banana
            }
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepareAsync();

        }
        return START_NOT_STICKY;*/
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.messenger.getBinder();
    }

    private class IncomingMessageHandler extends Handler {

        @Override
        public void handleMessage(Message m){
            /*MediaPlayer mediaPlayer = BackgroundPlayerService.this.mediaPlayer;
            if(mediaPlayer == null){
                return;
            }*/

            switch (m.what){
                case MSG_PLAY:
                    if(!mediaPlayer.isPlaying()){
                        //mediaPlayer.start();
                        Log.d(TAG, "Received MSG_PLAY");
                    }
                    break;
                case MSG_PAUSE:
                    //if(mediaPlayer.isPlaying()){
                        //mediaPlayer.pause();
                        Log.d(TAG, "Received MSG_PAUSE");
                    //}
                    break;
                case MSG_STOP:
                    //if(mediaPlayer.isPlaying()){
                        //mediaPlayer.stop();
                        Log.d(TAG, "Received MSG_STOP");
                    //}
                    break;
                case MSG_SET_SOURCE:
                    Log.d(TAG, (String)m.obj);
                    break;
            }
        }
    }
}
