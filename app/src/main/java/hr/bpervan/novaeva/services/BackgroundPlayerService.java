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

/**
 * Created by Branimir on 17.4.2016..
 */
public class BackgroundPlayerService extends Service {
    public static final int MSG_PLAY = 0;
    public static final int MSG_PAUSE = 1;
    public static final int MSG_STOP = 2;

    private final NotificationManager notificationManager;
    private final Messenger messenger = new Messenger(new IncomingMessageHandler());

    private final MediaPlayer mediaPlayer;

    public BackgroundPlayerService(String streamingUrl){
        this.notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.messenger.getBinder();
    }

    private class IncomingMessageHandler extends Handler {

        @Override
        public void handleMessage(Message m){
            switch (m.what){
                case MSG_PLAY:
                    break;
                case MSG_PAUSE:
                    break;
                case MSG_STOP:
                    break;
            }
        }
    }
}
