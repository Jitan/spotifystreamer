package nu.jitan.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import java.io.IOException;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.service.events.PlaybackCompletedEvent;
import nu.jitan.spotifystreamer.service.events.PlaybackPreparedEvent;
import nu.jitan.spotifystreamer.service.events.SeekToFinishedEvent;
import trikita.log.Log;

public class PlayerService extends Service implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, AudioManager
    .OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private static final int NOTIFICATION_ID = 1001;

    private final IBinder playerBind = new PlayerBinder();
    private StreamPlayer mStreamPlayer = null;
    private MediaSessionCompat mMediaSession;
    private MediaSessionCompat.Callback mSessionCallback;

    private ArrayList<MyTrack> mTrackList;
    private MyTrack mCurrentTrack;
    private boolean mTrackDataIsSet = false;
    private boolean mTrackIsPrepared = false;

    private int mCurrentTrackIndex;


    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentTrackIndex = 0;
        initPlayer();
    }

    @DebugLog
    private void initPlayer() {

        if (!getAudioFocus()) {
            return; //Failed to gain audio focus
        }


        ComponentName receiver = new ComponentName(getPackageName(),
            RemoteReceiver.class.getName());
        mMediaSession = new MediaSessionCompat(getApplicationContext(), "PlayerService", receiver, null);

        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
            .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_PLAY_PAUSE |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            ).build());

        mMediaSession.setActive(true);

        mStreamPlayer = new StreamPlayer(this, mMediaSession);
        mMediaSession.setCallback(mStreamPlayer);
    }

    @DebugLog
    private boolean getAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_GAIN) {
            Log.e("Could not get AUDIOFOCUS");
            return false;
        }
        return true;
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handleIntent(intent);

        if (mMediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat
            .STATE_PLAYING) {
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_PLAY |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                .build());
        } else {
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_PAUSE |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                .build());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @DebugLog
    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();
        MediaControllerCompat controller = mMediaSession.getController();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            controller.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            controller.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            controller.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            controller.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            controller.getTransportControls().stop();
        }
    }

    @DebugLog
    private PlaybackStateCompat.Builder getPlaybackStateBuilder(int currentState) {
        if (currentState == PlaybackStateCompat.STATE_PAUSED) {
            return new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING,
                0, 1.0f);
        } else {
            return new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED,
                0, 0.0f);
        }
    }

    /**
     * Updates the lockscreen controls, if enabled.
     */
    @DebugLog
    private void updateMediaSessionMetaData() {
        if (mTrackList != null && !mTrackList.isEmpty()) {
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mCurrentTrack
                .getArtists());
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mCurrentTrack
                .getAlbumName());
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, mCurrentTrack
                .getTrackName());
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration());
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
            mMediaSession.setMetadata(builder.build());
        }
    }


    @DebugLog
    private void buildNotification(NotificationCompat.Action action) {

        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1,
            intent, 0);

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle()
            .setMediaSession(mMediaSession.getSessionToken())
            .setShowActionsInCompactView(0, 1, 2);

        style.setShowCancelButton(true);
        style.setCancelButtonIntent(pendingIntent);

        Notification notification = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setTicker(mCurrentTrack.getTrackName())
            .setContentTitle(mCurrentTrack.getArtists())
            .setContentText(mCurrentTrack.getTrackName())
            .setDeleteIntent(pendingIntent)
            .setContentIntent(pendingIntent)
            .addAction(generateAction(R.drawable.ic_action_playback_prev, "Previous",
                ACTION_PREVIOUS))
            .addAction(action)
            .addAction(action).addAction(generateAction(R.drawable.ic_action_playback_next,
                "Next",
                ACTION_NEXT))
            .setStyle(style)
            .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @DebugLog
    private NotificationCompat.Action generateAction(int icon, String title, String
        intentAction) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1,
            intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private PlaybackStateCompat buildPlaybackState(int state, long position) {
        return new PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .setState(state, position, 1)
            .build();
    }

//    private Notification createNotification() {
//        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
//            intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setTicker(mTrackList.get(mCurrentTrackIndex).getTrackName())
//            .setContentTitle(mTrackList.get(mCurrentTrackIndex).getArtists())
//            .setContentText(mTrackList.get(mCurrentTrackIndex).getTrackName())
//            .addAction(R.drawable.ic_previous, "Previous", prevPendingIntent)
//            .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
//            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
//            .setStyle(new Notification.MediaStyle()
//                .setShowActionsInCompactView(0, 1, 2).build())
//            .setContentIntent(pendingIntent);
//
//        return notificationBuilder.build();
//    }

    @DebugLog
    public void setPlayList(ArrayList<MyTrack> trackList) {
        mTrackList = trackList;
        mCurrentTrack = mTrackList.get(mCurrentTrackIndex);
    }

    @DebugLog
    public void playNewTrack() {
        mTrackIsPrepared = false;
        setDataSource();
        mMediaPlayer.prepareAsync();
    }

    @DebugLog
    private void setDataSource() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mTrackList.get(mCurrentTrackIndex).getPreviewUrl());
            mTrackDataIsSet = true;
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
    }

    @DebugLog
    public void pausePlay() {
        if (mMediaPlayer.isPlaying()) {
            buildNotification(generateAction(R.drawable.ic_action_playback_pause, "Pause",
                ACTION_PAUSE));
            mMediaPlayer.pause();
        } else {
            buildNotification(generateAction(R.drawable.ic_action_playback_play, "Play",
                ACTION_PLAY));
            playNewTrack();
        }
    }

    /**
     * Plays the next track
     *
     * @return True if a new track has been loaded. False if not.
     */
    @DebugLog
    public boolean nextTrack() {
        int newIndex = mCurrentTrackIndex + 1;
        if (newIndex >= 0 && newIndex < mTrackList.size()) {
            mCurrentTrackIndex = newIndex;
            buildNotification(generateAction(R.drawable.ic_action_playback_pause, "Pause",
                ACTION_PLAY));
            playNewTrack();
            return true;
        }
        return false;
    }

    /**
     * Plays the previous track.
     *
     * @return True if a new track has been loaded. False if not.
     */

    public boolean prevTrack() {
        int newIndex = mCurrentTrackIndex - 1;
        if (newIndex >= 0 && newIndex < mTrackList.size()) {
            mCurrentTrackIndex = newIndex;
            buildNotification(generateAction(R.drawable.ic_action_playback_pause, "Pause",
                ACTION_PLAY));
            playNewTrack();
            return true;
        }
        return false;
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
        if (mTrackDataIsSet) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @DebugLog
    public void seekTo(int progress) {
        if (mTrackIsPrepared) {
            mMediaPlayer.seekTo(progress);
        }
    }

    @DebugLog
    @Override
    public void onPrepared(MediaPlayer mp) {
        mTrackIsPrepared = true;
        updateMediaSessionMetaData();
        EventBus.getDefault().post(new PlaybackPreparedEvent(mMediaPlayer.getDuration()));
    }

    @DebugLog
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("MediaPlayer error, codes: ", what, extra);
        return false;
    }

    @DebugLog
    @Override
    public void onCompletion(MediaPlayer mp) {
        EventBus.getDefault().post(new PlaybackCompletedEvent());
    }

    @DebugLog
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        EventBus.getDefault().post(new SeekToFinishedEvent());
    }

    @DebugLog
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initPlayer();
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

    @DebugLog
    @Override
    public IBinder onBind(Intent intent) {
        return playerBind;
    }

    @DebugLog
    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @DebugLog
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) mMediaPlayer.release();
        mMediaSession.release();
        stopForeground(true);
    }

    @DebugLog
    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
