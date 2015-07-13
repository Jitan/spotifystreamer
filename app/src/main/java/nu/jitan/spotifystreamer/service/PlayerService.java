package nu.jitan.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import java.io.IOException;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.model.MyTrack;
import trikita.log.Log;

public class PlayerService extends Service implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    private final IBinder playerBind = new PlayerBinder();
    private MediaPlayer mMediaPlayer = null;

    private ArrayList<MyTrack> mTrackList;
    private boolean mTrackIsLoaded = false;
    private int mCurrentTrackIndex;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentTrackIndex = 0;
        mMediaPlayer = new MediaPlayer();
        initPlayer();
    }

    public void initPlayer() {
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setList(ArrayList<MyTrack> trackList) {
        mTrackList = trackList;
    }

    public void playNewTrack() {
        mMediaPlayer.reset();
        setDataSource();
        mMediaPlayer.prepareAsync();
    }

    private void setDataSource() {
        try {
            mMediaPlayer.setDataSource(mTrackList.get(mCurrentTrackIndex).getPreviewUrl());
            mTrackIsLoaded = true;
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
    }

    public void pausePlay() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else if (mTrackIsLoaded) {
            mMediaPlayer.start();
        } else {
            playNewTrack();
        }
    }

    public void setTrack(int trackIndex) {
        if (trackIndex >= 0 && trackIndex < mTrackList.size()) {
            mCurrentTrackIndex = trackIndex;
            playNewTrack();
        }
    }

    public void nextTrack() {

    }

    public void previousTrack() {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("MediaPlayer error, codes: ", what, extra);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }
}
