package nu.jitan.spotifystreamer.ui.player;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.service.PlayerService;

public class PlayerFragment extends DialogFragment {
    private MyTrack mTrack;
    @InjectView(R.id.player_artist_name) TextView mArtistName;
    @InjectView(R.id.player_album_name) TextView mAlbumName;
    @InjectView(R.id.player_album_image) ImageView mAlbumImage;
    @InjectView(R.id.player_track_name) TextView mTrackName;
    private boolean mTwoPane;
    private boolean musicIsPlaying = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrack = arguments.getParcelable(Util.TRACK_KEY);
            mTwoPane = arguments.getBoolean(Util.IS_TWOPANE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, view);

        mArtistName.setText(mTrack.getArtists());
        mAlbumName.setText(mTrack.getAlbumName());
        mTrackName.setText(mTrack.getTrackName());

        if (!mTrack.getLargeImgUrl().isEmpty()) {
            String imgUrl = mTrack.getLargeImgUrl();
            Picasso.with(getActivity())
                .load(imgUrl)
                .fit()
                .centerInside()
                .into(mAlbumImage);
        }
        return view;
    }

    @OnClick(R.id.player_play_pause)
    public void playPauseAction() {
        Intent intent = new Intent(getActivity(), PlayerService.class);
        if (musicIsPlaying) {
            getActivity().stopService(intent);
            musicIsPlaying = false;
        } else {
            intent.putExtra("url", mTrack.getPreviewUrl());
            intent.setAction(PlayerService.ACTION_PLAY);
            getActivity().startService(intent);
            musicIsPlaying = true;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
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
