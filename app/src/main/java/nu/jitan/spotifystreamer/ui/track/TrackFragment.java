package nu.jitan.spotifystreamer.ui.track;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.model.MyTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TrackFragment extends Fragment {
    public static final String ARTIST_NAME_KEY = "nu.jitan.spotifystreamer.artistnamekey";
    public static final String ARTIST_ID_KEY = "nu.jitan.spotifystreamer.artistidkey";
    private static final String TRACK_LIST_KEY = "nu.jitan.spotifystreamer.tracklistkey";
    private static final String STATE_KEY = "nu.jitan.spotifystreamer.statekey";
    private String mArtistName;
    private ArrayList<MyTrack> mLastSearchResults;
    private TrackAdapter mTrackAdapter;
    private SpotifyService mSpotifyService;
    @InjectView(R.id.listview_track) ListView mTrackList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpotifyService = new SpotifyApi().getService();
        mTrackAdapter = new TrackAdapter(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);
        ButterKnife.inject(this, view);

        mTrackList.setAdapter(mTrackAdapter);

        Intent intent = getActivity().getIntent();
        if (savedInstanceState == null && intent != null && intent.hasExtra(ARTIST_NAME_KEY) &&
            intent.hasExtra(ARTIST_ID_KEY)) {
            mArtistName = intent.getStringExtra(ARTIST_NAME_KEY);
            setActionBarSubtitle(mArtistName);
            loadTopTracks(intent.getStringExtra(ARTIST_ID_KEY));
        }
        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            setActionBarSubtitle(savedInstanceState.getString(ARTIST_NAME_KEY));
            mLastSearchResults = savedInstanceState.getParcelableArrayList(TRACK_LIST_KEY);
            mTrackAdapter.addAll(mLastSearchResults);
            mTrackList.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_KEY));
        }
    }

    private void setActionBarSubtitle(String artistName) {
        if (getActivity() instanceof TrackActivity) {
            ActionBar actionbar = ((TrackActivity) getActivity()).getSupportActionBar();
            if (actionbar != null) {
                actionbar.setSubtitle(artistName);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARTIST_NAME_KEY, mArtistName);
        outState.putParcelable(STATE_KEY, mTrackList.onSaveInstanceState());
        outState.putParcelableArrayList(TRACK_LIST_KEY, mLastSearchResults);
    }

    private void loadTopTracks(String artistId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(SpotifyService.COUNTRY, "SE");

        mSpotifyService.getArtistTopTrack(artistId, queryParams, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                if (tracks.tracks.isEmpty()) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), getString(R
                        .string
                        .error_tracks_notfound), Toast
                        .LENGTH_SHORT).show());
                } else {
                    ArrayList<MyTrack> trackList = Util.extractTrackData(tracks);
                    getActivity().runOnUiThread(() -> {
                        mTrackAdapter.clear();
                        mTrackAdapter.addAll(trackList);
                        mLastSearchResults = trackList;
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), getString(R.string
                    .error_tracks_network), Toast.LENGTH_LONG).show());
            }
        });
    }
}