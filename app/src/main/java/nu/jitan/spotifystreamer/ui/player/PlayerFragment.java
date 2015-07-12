package nu.jitan.spotifystreamer.ui.player;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
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
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
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

    private PlayerService mPlayerService;
    private Intent mPlayIntent;
    private ArrayList<MyTrack> mTrackList;

    private int mCurrentTrack;
    private boolean mTwoPane;
    private boolean mPlayerBound = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackList = arguments.getParcelableArrayList(Util.TRACKLIST_KEY);
            mCurrentTrack = arguments.getInt(Util.TRACKLIST_POSITION_KEY);
            mTwoPane = arguments.getBoolean(Util.IS_TWOPANE_KEY);
        }
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, view);

        MyTrack track = mTrackList.get(mCurrentTrack);
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
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(mPlayIntent, playerConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayIntent);
        }
    }

    @OnClick(R.id.player_play_pause)
    public void pausePlayAction() {
        mPlayerService.pausePlay();
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
        getActivity().unbindService(playerConnection);
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
