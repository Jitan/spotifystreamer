package nu.jitan.spotifystreamer.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import hugo.weaving.DebugLog;
import trikita.log.Log;

/**
 * Wrapper class for MediaPlayer for providing easy streaming of audio.
 */
@DebugLog
public class StreamPlayer extends MediaSessionCompat.Callback implements MediaPlayer
    .OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final int NOTIFICATION_ID = 1;
    Context mContext;
    MediaPlayer mMediaPlayer;
    NotificationManager mNotificationManager;

    public StreamPlayer(Context context, MediaSessionCompat mediaSessionCompat) {
        mContext = context;

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

        mNotificationManager = (NotificationManager) context.getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @DebugLog
    @Override
    public void onPlay() {
        super.onPlay();
    }

    @DebugLog
    @Override
    public void onPause() {
        super.onPause();
    }

    @DebugLog
    @Override
    public void onSkipToNext() {
        super.onSkipToNext();

    }

    @DebugLog
    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();

    }

    @DebugLog
    @Override
    public void onStop() {
        super.onStop();
        Log.e("MediaPlayerService", "onStop");
        //Stop media player here
        mNotificationManager.cancel(NOTIFICATION_ID);
        Intent intent = new Intent(mContext.getApplicationContext(), PlayerService.class);
        stopForeground(true);
        stopService(intent);

        @Override
        public void onPrepared (MediaPlayer mp){

        }

        @Override
        public void onCompletion (MediaPlayer mp){

        }

        @Override
        public boolean onError (MediaPlayer mp,int what, int extra){
            return false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }
}
