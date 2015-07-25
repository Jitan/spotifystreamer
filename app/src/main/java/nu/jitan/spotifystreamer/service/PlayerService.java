package nu.jitan.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.service.events.UpdateUiEvent;
import nu.jitan.spotifystreamer.ui.player.PlayerActivity;

public final class PlayerService extends Service {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    public static final int NOTIFICATION_ID = 1001;

    private final IBinder playerBind = new PlayerBinder();
    private StreamPlayer mStreamPlayer = null;
    private MediaSessionCompat mMediaSession;
    private NotificationManager mNotificationManager;

    private boolean foregroundNotificationStarted = false;

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context
            .NOTIFICATION_SERVICE);
        initPlayer();
    }

    @DebugLog
    private void initPlayer() {
        ComponentName receiver = new ComponentName(getPackageName(),
            RemoteReceiver.class.getName());
        mMediaSession = new MediaSessionCompat(getApplicationContext(), "PlayerService",
            receiver, null);

        mStreamPlayer = new StreamPlayer(this);
        mMediaSession.setCallback(mStreamPlayer);

        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setPlaybackState(buildPlaybackState());
        mMediaSession.setActive(true);
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        if (mStreamPlayer == null) initPlayer();

        if (mStreamPlayer.isPlaying()) {
            mMediaSession.setPlaybackState(buildPlaybackState());
        } else {
            mMediaSession.setPlaybackState(buildPlaybackState());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @DebugLog
    private PlaybackStateCompat buildPlaybackState() {
        int state;
        long action;
        float playbackSpeed;

        if (mStreamPlayer.isPlaying()) {
            state = PlaybackStateCompat.STATE_PLAYING;
            action = PlaybackStateCompat.ACTION_PAUSE;
            playbackSpeed = 1.0f;
        } else {
            state = PlaybackStateCompat.STATE_PAUSED;
            action = PlaybackStateCompat.ACTION_PLAY;
            playbackSpeed = 0.0f;
        }

        return new PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    action |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
            .setState(state, mStreamPlayer.getCurrentPosition(), playbackSpeed)
            .build();
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
    public void onEvent(UpdateUiEvent event) {
        updateMediaSessionMetaData(event.track);

        Notification updatedNotification;
        if (event.action == ACTION_PLAY) {
            updatedNotification = buildNotification(generateAction(R.drawable
                .ic_action_playback_pause, "Pause", ACTION_PAUSE), event.track);
        } else {
            updatedNotification = buildNotification(generateAction(R.drawable
                .ic_action_playback_play, "Play", ACTION_PLAY), event.track);
        }

        if (foregroundNotificationStarted) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            mNotificationManager.notify(NOTIFICATION_ID, updatedNotification);
        } else {
            startForeground(NOTIFICATION_ID, updatedNotification);
            foregroundNotificationStarted = true;
        }
    }

    @DebugLog
    private Notification buildNotification(NotificationCompat.Action action, MyTrack track) {

        Intent stopIntent = new Intent(getApplicationContext(), PlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingActionStopIntent = PendingIntent.getService(getApplicationContext(), 1,
            stopIntent, 0);

        Intent openPlayerIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        openPlayerIntent.putExtra(Util.TRACKLIST_KEY, mStreamPlayer.getTrackList());
        openPlayerIntent.putExtra(Util.TRACKLIST_POSITION_KEY, mStreamPlayer.getCurrentTrackIndex
            ());
        PendingIntent pendingOpenPlayerIntent = PendingIntent.getService(getApplicationContext(), 1,
            openPlayerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle()
            .setMediaSession(mMediaSession.getSessionToken())
            .setShowActionsInCompactView(0, 1, 2);

        style.setShowCancelButton(true);
        style.setCancelButtonIntent(pendingActionStopIntent);

        return new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setUsesChronometer(true)
            .setTicker(track.getTrackName())
            .setContentTitle(track.getArtists())
            .setContentText(track.getTrackName())
            .setDeleteIntent(pendingActionStopIntent)
            .setContentIntent(pendingOpenPlayerIntent)
            .addAction(generateAction(R.drawable.ic_action_playback_prev, "Previous",
                ACTION_PREVIOUS))
            .addAction(action)
            .addAction(generateAction(R.drawable.ic_action_playback_next,
                "Next",
                ACTION_NEXT))
            .setStyle(style)
            .build();
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

    /**
     * Updates the lockscreen controls, if enabled.
     */
    @DebugLog
    private void updateMediaSessionMetaData(MyTrack track) {

        MediaMetadataCompat.Builder metaDataBuilder = new MediaMetadataCompat.Builder();

        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track
            .getArtists());
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track
            .getAlbumName());
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track
            .getTrackName());
        metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration());
        metaDataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        mMediaSession.setPlaybackState(buildPlaybackState());
        mMediaSession.setMetadata(metaDataBuilder.build());
    }

    @DebugLog
    @Override
    public IBinder onBind(Intent intent) {
        EventBus.getDefault().register(this);
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
        if (mStreamPlayer != null) mStreamPlayer.release();
        EventBus.getDefault().unregister(this);
        mMediaSession.release();
        stopForeground(true);
    }

    public boolean isFirstLoad() {
        return mStreamPlayer.getTrackList() != null;
    }

    @DebugLog
    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }

    }

    @DebugLog
    public MediaControllerCompat getMediaController() {
        if (mMediaSession != null) {
            return mMediaSession.getController();
        }
        return null;
    }

    @DebugLog
    public void setTrackList(ArrayList<MyTrack> trackList) {
        mStreamPlayer.setTrackList(trackList);
    }

    @DebugLog
    public void pausePlay() {
        if (mStreamPlayer.isPlaying()) {
            mStreamPlayer.onPause();
        } else {
            mStreamPlayer.onPlay();
        }
    }

    public void nextTrack() {
        mStreamPlayer.onSkipToNext();
    }

    public void prevTrack() {
        mStreamPlayer.onSkipToPrevious();
    }

    public int getDuration() {
        return mStreamPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mStreamPlayer.getCurrentPosition();
    }

    public void setCurrentTrack(int index) {
        mStreamPlayer.setCurrentTrack(index);
    }

    public void seekTo(int progress) {
        mStreamPlayer.onSeekTo(progress);
    }

    public boolean isPlaying() {
        return mStreamPlayer.isPlaying();
    }

    public MyTrack getCurrentTrack() {
        return mStreamPlayer.getTrackList().get(mStreamPlayer.getCurrentTrackIndex());
    }
}
