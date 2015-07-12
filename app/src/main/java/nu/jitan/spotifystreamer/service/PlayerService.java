package nu.jitan.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import java.io.IOException;
import trikita.log.Log;

public class PlayerService extends Service {
    public static final String ACTION_PLAY = "nu.jitan.spotifystreamer.PLAY";
    private MediaPlayer mMediaPlayer = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_PLAY)) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(intent.getStringExtra("url"));
                mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.d("MediaPlayer error, codes: ", what, extra);
                        return false;
                    }
                });
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                //TODO Handle this exception gracefully..
                mMediaPlayer.release();
                mMediaPlayer = null;
                e.printStackTrace();
            }

        }

        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }
}
