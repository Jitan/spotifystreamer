package nu.jitan.spotifystreamer.ui.player;

import android.app.Dialog;
import android.content.ComponentName;
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
import android.widget.Button;
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
    @Bind(R.id.player_play_pause) Button mPlayPauseButton;

    private PlayerService mPlayerService;
    private Intent mStartPlayerIntent;
    private ArrayList<MyTrack> mTrackList;
    private Handler mHandler;

    private int mCurrentTrackIndex;
    private boolean mTwoPane;
    private boolean mSeekBarIsBeingScrolled = false;

    @DebugLog
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mHandler = new Handler();
    }

    @DebugLog
    public void setUiArguments(Bundle args) {
        mTrackList = args.getParcelableArrayList(Util.TRACKLIST_KEY);
        mCurrentTrackIndex = args.getInt(Util.TRACKLIST_POSITION_KEY);
        mTwoPane = args.getBoolean(Util.IS_TWOPANE_KEY);
    }

    @DebugLog
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);

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

    private ServiceConnection mPlayerConnection = new ServiceConnection() {
        @DebugLog
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = ((PlayerBinder) service).getService();

            if (mPlayerService.isRebound()) {
                if (mTrackList.get(mCurrentTrackIndex).equals(mPlayerService.getCurrentTrack())) {
                    getStickyEvents();
                } else if (mTrackList.equals(mPlayerService.getTrackList())) {
                    mPlayerService.setCurrentTrack(mCurrentTrackIndex);
                    mPlayerService.playNewTrack();
                } else {
                    loadNewTrackListAndPlay();
                }
            } else {
                loadNewTrackListAndPlay();
            }
            updateUi(mTrackList.get(mCurrentTrackIndex));
        }

        @DebugLog
        @Override
        public void onServiceDisconnected(ComponentName name) {
            stopSeekbarUpdates();
            mPlayerService = null;
            closePlayerFragment();
        }
    };

    private void loadNewTrackListAndPlay() {
        mPlayerService.setTrackList(mTrackList);
        mPlayerService.setCurrentTrack(mCurrentTrackIndex);
        mPlayerService.playNewTrack();
    }

    @DebugLog
    private void closePlayerFragment() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @DebugLog
    private void getStickyEvents() {
        onEvent(EventBus.getDefault().getStickyEvent(UpdateUiEvent.class));
        onEvent(EventBus.getDefault().getStickyEvent(PlaybackPreparedEvent.class));
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

    @DebugLog
    public void onEvent(UpdateUiEvent event) {
        if (event.action.equals(PlayerService.ACTION_STOP) ||
            event.action.equals(PlayerService.ACTION_PAUSE)) {
            mPlayPauseButton.setBackground(getActivity().getResources()
                .getDrawable(R.drawable.ic_action_playback_play));
        } else {
            mPlayPauseButton.setBackground(getActivity().getResources()
                .getDrawable(R.drawable.ic_action_playback_pause));
        }
        updateUi(event.track);
    }

    @DebugLog
    public void onEvent(PlaybackPreparedEvent event) {
        mSeekBar.setMax(event.duration);
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

    @DebugLog
    @Override
    public void onStart() {
        super.onStart();
        if (mStartPlayerIntent == null) {
            mStartPlayerIntent = new Intent(getActivity(), PlayerService.class);
        }

        getActivity().startService(mStartPlayerIntent);
        getActivity().bindService(mStartPlayerIntent, mPlayerConnection, 0);
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
        super.onResume();
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
        if (seekbarIsBeingUpdated()) { // If we are checking for SeekBar updates
            stopSeekbarUpdates();
        } else {
            mSeekbarUpdater.run();
        }
    }

    private boolean seekbarIsBeingUpdated() {
        return mHandler.hasMessages(0);
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
    }

    @DebugLog
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacks(mSeekbarUpdater);
        getActivity().unbindService(mPlayerConnection);
    }

    @DebugLog
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public ArrayList<MyTrack> getTrackList() {
        return mTrackList;
    }
}
