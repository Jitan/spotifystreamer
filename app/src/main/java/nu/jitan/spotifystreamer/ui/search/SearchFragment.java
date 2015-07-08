package nu.jitan.spotifystreamer.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.event.ArtistClickedEvent;
import nu.jitan.spotifystreamer.model.MyArtist;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchFragment extends Fragment {
    private static final String STATE_KEY = "nu.jitan.spotifystreamer.statekey";
    private static final String ARTIST_LIST_KEY = "nu.jitan.spotifystreamer.artistlistkey";
    private SpotifyService mSpotifyService;
    private SearchAdapter mSearchAdapter;
    @InjectView(R.id.searchview) SearchView mSearchView;
    @InjectView(R.id.listview_search) ListView mSearchResultList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpotifyService = new SpotifyApi().getService();
        mSearchAdapter = new SearchAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
        savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);

        mSearchResultList.setAdapter(mSearchAdapter);
        setupSearchView();

        return view;
    }

    public void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            searchArtist(searchQuery);
        }
    }

    @OnItemClick(R.id.listview_search)
    public void loadArtistTracks(int position) {
        MyArtist artist = mSearchAdapter.getItem(position);
        EventBus.getDefault().post(new ArtistClickedEvent(artist));
    }

    private void searchArtist(String searchQuery) {
        mSpotifyService.searchArtists(searchQuery, new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                if (artistsPager.artists.total == 0) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(),
                            getString(R.string.error_artist_notfound),
                            Toast.LENGTH_SHORT).show();
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        mSearchAdapter.clear();
                        mSearchAdapter.addAll(Util.extractArtistData(artistsPager));
                        mSearchResultList.setSelection(0);
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), getString(R.string
                    .error_artist_network), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context
            .SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity()
            .getComponentName()));
        mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        removeMagnifierFromSearchView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARTIST_LIST_KEY, mSearchAdapter.getList());
        outState.putParcelable(STATE_KEY, mSearchResultList.onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mSearchAdapter.addAll(savedInstanceState.getParcelableArrayList(ARTIST_LIST_KEY));
            mSearchResultList.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_KEY));
        }
    }

    private void removeMagnifierFromSearchView() {
        int magId = getResources().getIdentifier("android:id/search_mag_icon", null,
            null);
        ImageView magImage = ButterKnife.findById(mSearchView, magId);
        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }
}
