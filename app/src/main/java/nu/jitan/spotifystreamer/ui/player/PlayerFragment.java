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
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import de.greenrobot.event.EventBus;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.service.PlayerService;
import nu.jitan.spotifystreamer.service.PlayerService.PlayerBinder;

public class PlayerFragment extends DialogFragment {

    @InjectView(R.id.player_artist_name) TextView mArtistName;
    @InjectView(R.id.player_album_name) TextView mAlbumName;
    @InjectView(R.id.player_album_image) ImageView mAlbumImage;
    @InjectView(R.id.player_track_name) TextView mTrackName;
    @InjectView(R.id.player_seekbar) SeekBar mSeekBar;

    private PlayerService mPlayerService;
    private Intent mPlayIntent;
    private ArrayList<MyTrack> mTrackList;
    private MyTrack mCurrentTrack;
    private Handler mHandler;

    private int mInterval = 100;
    private int mCurrentTrackIndex;
    private boolean mTwoPane;
    private boolean mPlayerBound = false;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, view);

        updateTrackUi();
        return view;
    }

    private void updateTrackUi() {
        MyTrack track = mTrackList.get(mCurrentTrackIndex);
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

    private Runnable mSeekbarUpdater = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mSeekbarUpdater, mInterval);
            updateSeekbar();
        }
    };

    private void updateSeekbar() {
        mSeekBar.setProgress(mPlayerService.getCurrentPosition());
    }

    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerBinder binder = (PlayerBinder) service;
            mPlayerService = ((PlayerBinder) service).getService();
            mPlayerService.setList(mTrackList);
            mPlayerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        if (mPlayIntent == null) {
            mPlayIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(mPlayIntent, playerConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayIntent);
        }
    }

    public void onEvent(PlaybackPreparedEvent event) {
        mSeekBar.setMax(event.duration);
        mPlayerService.pausePlay();
        mSeekbarUpdater.run();
    }


    @OnClick(R.id.player_play_pause)
    public void pausePlayAction() {
        mPlayerService.pausePlay();
    }

    @OnClick(R.id.player_next_track)
    public void nextTrackAction() {
        int newIndex = mCurrentTrackIndex + 1;
        if (newIndex >= 0 && newIndex < mTrackList.size()) {
            mCurrentTrackIndex = newIndex;
        } else {
            mCurrentTrackIndex = 0;
            Toast.makeText(getActivity(), "No more tracks in list, repeating", Toast
                .LENGTH_SHORT).show();
        }
        updateTrackUi();
        mPlayerService.setTrack(mCurrentTrackIndex);
    }

    @OnClick(R.id.player_previous_track)
    public void previousTrackAction() {
        int newIndex = mCurrentTrackIndex - 1;
        if (newIndex >= 0 && newIndex < mTrackList.size()) {
            mCurrentTrackIndex = newIndex;
        } else {
            mCurrentTrackIndex = mTrackList.size() - 1;
            Toast.makeText(getActivity(), "No more tracks in list, repeating", Toast
                .LENGTH_SHORT).show();
        }
        updateTrackUi();
        mPlayerService.setTrack(mCurrentTrackIndex);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        getActivity().unbindService(playerConnection);
        mHandler.removeCallbacks(mSeekbarUpdater);
    }

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
}
