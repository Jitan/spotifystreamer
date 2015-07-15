package nu.jitan.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import de.greenrobot.event.EventBus;
import java.io.IOException;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.ui.player.PlaybackPreparedEvent;
import nu.jitan.spotifystreamer.ui.player.SeekToFinishedEvent;
import trikita.log.Log;

public class PlayerService extends Service implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private static final int NOTIFICATION_ID = 1001;

    private final IBinder playerBind = new PlayerBinder();
    private MediaPlayer mMediaPlayer = null;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;

    private ArrayList<MyTrack> mTrackList;
    private boolean mTrackDataIsSet = false;
    private boolean mTrackIsPrepared = false;

    private int mCurrentTrackIndex;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentTrackIndex = 0;
        initPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handleIntent( intent );
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;


        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
            mController.getTransportControls().stop();
        }
    }

    public void initPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

        mSession = new MediaSession(getApplicationContext(), "PlayerServiceSession");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.e("MediaPlayerService", "onPlay");
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause",
                    ACTION_PAUSE));
            }


            @Override
            public void onPause() {
                super.onPause();
                Log.e("MediaPlayerService", "onPause");
                buildNotification(generateAction(android.R.drawable.ic_media_play, "Play",
                    ACTION_PLAY));
            }


            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.e("MediaPlayerService", "onSkipToNext");
                //Change media here
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause",
                    ACTION_PAUSE));
            }


            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.e("MediaPlayerService", "onSkipToPrevious");
                //Change media here
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause",
                    ACTION_PAUSE));
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.e("MediaPlayerService", "onStop");
                //Stop media player here
                NotificationManager notificationManager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                Intent intent = new Intent(getApplicationContext(), PlayerService.class);
                stopService(intent);
            }
        });
    }

    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1,
            intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(Notification.Action action) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1,
            intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(mTrackList.get(mCurrentTrackIndex).getArtists())
            .setContentText(mTrackList.get(mCurrentTrackIndex).getTrackName())
            .setDeleteIntent(pendingIntent)
            .setStyle(style)
            .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous",
                ACTION_PREVIOUS))
            .addAction(action)
            .addAction(action).addAction(generateAction(android.R.drawable.ic_media_next, "Next",
                ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2);
        style.setMediaSession(mSession.getSessionToken());

        startForeground(NOTIFICATION_ID, builder.build());
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
//                .setShowActionsInCompactView(1)
//                .setMediaSession(mSession.getSessionToken()))
//            .setContentIntent(pendingIntent);
//
//        return notificationBuilder.build();
//    }

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
            mTrackDataIsSet = true;
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
    }

    public void pausePlay() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else if (mTrackDataIsSet) {
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

    public void seekTo(int progress) {
        if (mTrackIsPrepared) {
            mMediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mTrackIsPrepared = true;
        buildNotification(generateAction(android.R.drawable.ic_media_play, "Play",
            ACTION_PLAY));
//        EventBus.getDefault().post(new PlaybackPreparedEvent(mMediaPlayer.getDuration()));
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
    public void onSeekComplete(MediaPlayer mp) {
        EventBus.getDefault().post(new SeekToFinishedEvent());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
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
        stopForeground(true);
    }
}
