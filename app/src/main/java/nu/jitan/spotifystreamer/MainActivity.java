package nu.jitan.spotifystreamer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import nu.jitan.spotifystreamer.model.MyTrack;
import nu.jitan.spotifystreamer.model.MyTrackList;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import trikita.log.Log;

public class MainActivity extends AppCompatActivity {
    private SpotifyService mSpotifyService;
    private SearchAdapter mSearchAdapter;
    @InjectView(R.id.searchview) SearchView mSearchView;
    @InjectView(R.id.listview_search) ListView mSearchResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mSpotifyService = new SpotifyApi().getService();
        mSearchAdapter = new SearchAdapter(this);

        mSearchResultList.setAdapter(mSearchAdapter);
        setupSearchView();
    }

    private void searchArtist(String searchQuery) {
        mSpotifyService.searchArtists(searchQuery, new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                final List<Artist> artistList = artistsPager.artists.items;

                runOnUiThread(() -> {
                    if (artistList.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "No artist found", Toast
                            .LENGTH_SHORT).show();
                    } else {
                        mSearchAdapter.clear();
                        mSearchAdapter.addAll(artistList);
                        mSearchResultList.setSelection(0);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("RetrofitError when searching Artists: ", error.getMessage());
            }
        });
    }

    @OnItemClick(R.id.listview_search)
    public void loadArtistTracks(int position) {
        Artist artist = mSearchAdapter.getItem(position);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(SpotifyService.COUNTRY, "SE");

        mSpotifyService.getArtistTopTrack(artist.id, queryParams, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                Context context = getApplicationContext();
                if (tracks.tracks.isEmpty()) {
                    Toast.makeText(context, "No tracks found for this artist", Toast
                        .LENGTH_SHORT).show();
                } else {
                    Intent loadTracksIntent = new Intent(context, TrackActivity.class);
                    List<MyTrack> myTrackList = new ArrayList<>();

                    String albumName, trackName, imgUrl;
                    for (Track track : tracks.tracks) {
                        albumName = track.album.name;
                        trackName = track.name;

                        if (track.album.images.size() > 0) {
                            imgUrl = track.album.images.get(0).url;
                        } else {
                            imgUrl = "";
                        }
                        myTrackList.add(MyTrack.create(albumName, trackName, imgUrl));
                    }

                    loadTracksIntent.putExtra(TrackActivity.TRACKLIST_KEY, MyTrackList.create
                        (myTrackList));
                    loadTracksIntent.putExtra(Intent.EXTRA_TEXT, artist.name);
                    startActivity(loadTracksIntent);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("RetrofitError when loading Artist Tracks: ", error.getMessage());
            }
        });

    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        removeMagnifierFromSearchView();
    }

    private void removeMagnifierFromSearchView() {
        int magId = getResources().getIdentifier("android:id/search_mag_icon", null,
            null);
        ImageView magImage = ButterKnife.findById(mSearchView, magId);
        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            searchArtist(searchQuery);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
