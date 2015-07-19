package nu.jitan.spotifystreamer.ui.player;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.service.PlayerService;
import nu.jitan.spotifystreamer.service.PlayerService.PlayerBinder;
import nu.jitan.spotifystreamer.service.events.NoMoreTracksEvent;
import nu.jitan.spotifystreamer.service.events.PlaybackCompletedEvent;
import nu.jitan.spotifystreamer.service.events.PlaybackPreparedEvent;
import nu.jitan.spotifystreamer.service.events.UpdateUiEvent;

public class PlayerFragment extends DialogFragment {
    private static final int SEEKBAR_UPDATE_INTERVAL = 1000; // milliseconds

    @Bind(R.id.player_artist_name) TextView mArtistName;
    @Bind(R.id.player_album_name) TextView mAlbumName;
    @Bind(R.id.player_album_image) ImageView mAlbumImage;
    @Bind(R.id.player_track_name) TextView mTrackName;
    @Bind(R.id.player_seekbar) SeekBar mSeekBar;

    private PlayerService mPlayerService;
    private Intent mPlayIntent;
    private ArrayList<MyTrack> mTrackList;
    private MyTrack mCurrentTrack;
    private Handler mHandler;

    private int mCurrentTrackIndex;
    private boolean mTwoPane;
    private boolean mPlayerBound = false;
    private boolean mSeekBarIsBeingScrolled = false;
    private boolean paused = false, playbackPaused = false;

    @DebugLog
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackList = arguments.getParcelableArrayList(Util.TRACKLIST_KEY);
            mCurrentTrackIndex = arguments.getInt(Util.TRACKLIST_POSITION_KEY);
            mTwoPane = arguments.getBoolean(Util.IS_TWOPANE_KEY);
        }
    }

    @DebugLog
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);

        updateUi(mTrackList.get(mCurrentTrackIndex));
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @DebugLog
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekBarIsBeingScrolled = true;
            }

            @DebugLog
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekBarIsBeingScrolled = false;
                mPlayerService.seekTo(seekBar.getProgress());
            }
        });
        return view;
    }

    @DebugLog
    private void updateUi(MyTrack track) {
        mArtistName.setText(track.getArtists());
        mAlbumName.setText(track.getAlbumName());
        mTrackName.setText(track.getTrackName());

        if (!track.getLargeImgUrl().isEmpty()) {
            String imgUrl = track.getLargeImgUrl();
            Picasso.with(getActivity())
                .load(imgUrl)
                .fit()
                .centerInside()
                .into(mAlbumImage);
        }
    }

    private ServiceConnection playerConnection = new ServiceConnection() {
        @DebugLog
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = ((PlayerBinder) service).getService();
            mPlayerService.setTrackList(mTrackList);
            mPlayerService.setCurrentTrack(mCurrentTrackIndex);
            mPlayerBound = true;
        }

        @DebugLog
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerBound = false;
            mPlayerService = null;
        }
    };

    @DebugLog
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @DebugLog
    @Override
    public void onResume() {
        if (mTwoPane) {
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            final float scale = getResources().getDisplayMetrics().density;

            params.width = (int) (650 * scale + 0.5f);
            params.height = (int) (650 * scale + 0.5f);
            getDialog().getWindow().setAttributes(params);
        }

        if (mPlayIntent == null) {
            mPlayIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(mPlayIntent, playerConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayIntent);
        }
        super.onResume();
    }

    @DebugLog
    public void onEvent(UpdateUiEvent event) {
        updateUi(event.track);
    }

    @DebugLog
    public void onEvent(PlaybackPreparedEvent event) {
        mSeekBar.setMax(event.duration);
        mPlayerService.pausePlay();
        mSeekbarUpdater.run();
    }

    @DebugLog
    public void onEvent(PlaybackCompletedEvent event) {
        stopSeekbarUpdates();
    }

    @DebugLog
    public void onEvent(NoMoreTracksEvent event) {
        Toast.makeText(getActivity(), "No more tracks in list", Toast
            .LENGTH_SHORT).show();
    }

    private Runnable mSeekbarUpdater = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mSeekbarUpdater, SEEKBAR_UPDATE_INTERVAL);
            updateSeekbar();
        }
    };

    private void updateSeekbar() {
        if (!mSeekBarIsBeingScrolled) {
            mSeekBar.setProgress(mPlayerService.getCurrentPosition());
        }
    }

    @DebugLog
    private void stopSeekbarUpdates() {
        mHandler.removeCallbacks(mSeekbarUpdater);
    }

    @DebugLog
    @OnClick(R.id.player_play_pause)
    public void pausePlayAction() {
        mPlayerService.pausePlay();
        if (mHandler.hasMessages(0)) { // If we are checking for SeekBar updates
            stopSeekbarUpdates();
        } else {
            mSeekbarUpdater.run();
        }
    }

    @DebugLog
    @OnClick(R.id.player_next_track)
    public void nextTrackAction() {
        mPlayerService.nextTrack();
    }

    @DebugLog
    @OnClick(R.id.player_previous_track)
    public void previousTrackAction() {
        mPlayerService.prevTrack();
    }

    @DebugLog
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @DebugLog
    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @DebugLog
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacks(mSeekbarUpdater);
        if (mPlayerBound) {
            getActivity().unbindService(playerConnection);
        }
    }

    @DebugLog
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
