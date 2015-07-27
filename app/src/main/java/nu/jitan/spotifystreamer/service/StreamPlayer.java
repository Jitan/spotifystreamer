package nu.jitan.spotifystreamer.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import java.io.IOException;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.service.events.NoMoreTracksEvent;
import nu.jitan.spotifystreamer.service.events.PlaybackCompletedEvent;
import nu.jitan.spotifystreamer.service.events.PlaybackPreparedEvent;
import nu.jitan.spotifystreamer.service.events.UpdateUiEvent;
import trikita.log.Log;

/**
 * Wrapper class for MediaPlayer for providing easy streaming of audio.
 */
public final class StreamPlayer extends MediaSessionCompat.Callback implements MediaPlayer
    .OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager
    .OnAudioFocusChangeListener {

    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private NotificationManager mNotificationManager;
    private AudioManager mAudioManager;
    private ArrayList<MyTrack> mTrackList;
    private int mCurrentTrackIndex = 0;
    private boolean mTrackIsPrepared = false;
    private boolean mTrackUrlIsSet = false;
    private MyTrack mCurrentTrack;

    @DebugLog
    public StreamPlayer(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        initMediaPlayer();

        mNotificationManager = (NotificationManager) context.getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @DebugLog
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(mContext.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @DebugLog
    private boolean getAudioFocus() {
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @DebugLog
    @Override
    public void onPlay() {
        if (!isPlaying() && getAudioFocus()) {
            if (mTrackIsPrepared) {
                mMediaPlayer.start();
                updateUi(PlayerService.ACTION_PLAY);
            } else {
                playNewTrack();
            }
        }
    }

    @DebugLog
    @Override
    public void onPause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            updateUi(PlayerService.ACTION_PAUSE);

        }
    }

    @DebugLog
    @Override
    public void onSkipToNext() {
        int newIndex = mCurrentTrackIndex + 1;
        if (newIndex >= 0 && newIndex < mTrackList.size()) {
            setCurrentTrack(newIndex);
            playNewTrack();
        } else {
            EventBus.getDefault().post(new NoMoreTracksEvent());
        }
    }

    @DebugLog
    @Override
    public void onSkipToPrevious() {
        int newIndex = mCurrentTrackIndex - 1;
        if (newIndex >= 0 && newIndex < mTrackList.size()) {
            setCurrentTrack(newIndex);
            playNewTrack();
        } else {
            EventBus.getDefault().post(new NoMoreTracksEvent());
        }
    }

    @Override
    public void onSeekTo(long pos) {
        if (mTrackIsPrepared) {
            mMediaPlayer.seekTo((int) pos);
        }
    }

    @DebugLog
    @Override
    public void onStop() {
        super.onStop();
        Log.e("MediaPlayerService", "onStop");
        //Stop media player here
        mNotificationManager.cancel(PlayerService.NOTIFICATION_ID);
        Intent intent = new Intent(mContext.getApplicationContext(), PlayerService.class);
        mContext.stopService(intent);
    }

    @DebugLog
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @DebugLog
    public void release() {
        mMediaPlayer.release();
    }

    @DebugLog
    public void playNewTrack() {
        mTrackIsPrepared = false;
        setTrackUrl();
        mMediaPlayer.prepareAsync();
    }

    @DebugLog
    private void setTrackUrl() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mCurrentTrack.getPreviewUrl());
            mTrackUrlIsSet = true;
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
    }

    @DebugLog
    public void setTrackList(ArrayList<MyTrack> trackList) {
        mTrackList = trackList;
        setCurrentTrack(0);
    }

    @DebugLog
    public void setCurrentTrack(int index) {
        mCurrentTrackIndex = index;
        mCurrentTrack = mTrackList.get(mCurrentTrackIndex);
    }

    @DebugLog
    public int getDuration() {
        if (mTrackIsPrepared) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    public int getCurrentPosition() {
        if (mTrackUrlIsSet) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    private void updateUi(String playServiceActionString) {
        EventBus.getDefault().postSticky(new UpdateUiEvent(mCurrentTrack, playServiceActionString));
    }

    public ArrayList<MyTrack> getTrackList() {
        return mTrackList;
    }

    public int getCurrentTrackIndex() {
        return mCurrentTrackIndex;
    }

    @DebugLog
    @Override
    public void onPrepared(MediaPlayer mp) {
        mTrackIsPrepared = true;
        EventBus.getDefault().postSticky(new PlaybackPreparedEvent(mMediaPlayer.getDuration()));
        updateUi(PlayerService.ACTION_PLAY);
        mMediaPlayer.start();
    }

    @DebugLog
    @Override
    public void onCompletion(MediaPlayer mp) {
        EventBus.getDefault().post(new PlaybackCompletedEvent());
        onSkipToNext();
    }

    @DebugLog
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("MediaPlayer error, codes: ", what, extra);
        return false;
    }

    @DebugLog
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media
                // player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }
}
