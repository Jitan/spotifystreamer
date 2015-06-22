package nu.jitan.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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
import nu.jitan.spotifystreamer.model.MyTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import trikita.log.Log;


public class TrackActivity extends AppCompatActivity {
    private static String TRACK_LIST_KEY = "nu.jitan.spotifystreamer.tracklistkey";
    private ArrayList<MyTrack> mLastSearchResults;
    private TrackAdapter mTrackAdapter;
    private SpotifyService mSpotifyService;

    @InjectView(R.id.listview_track) ListView mTrackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ButterKnife.inject(this);

        mSpotifyService = new SpotifyApi().getService();
        mTrackAdapter = new TrackAdapter(this);
        mTrackList.setAdapter(mTrackAdapter);

        Intent intent = getIntent();

        if (savedInstanceState == null && intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            loadTopTracks(intent.getStringExtra(Intent.EXTRA_TEXT));
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLastSearchResults = savedInstanceState.getParcelableArrayList(TRACK_LIST_KEY);
        mTrackAdapter.clear();
        mTrackAdapter.addAll(mLastSearchResults);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRACK_LIST_KEY, mLastSearchResults);
    }

    private void loadTopTracks(String artistId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(SpotifyService.COUNTRY, "SE");

        mSpotifyService.getArtistTopTrack(artistId, queryParams, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                Context context = getApplicationContext();
                if (tracks.tracks.isEmpty()) {
                    Toast.makeText(context, "No tracks found for this artist", Toast
                        .LENGTH_SHORT).show();
                } else {
                    ArrayList<MyTrack> trackList = Util.extractTrackData(tracks);
                    runOnUiThread(() -> {
                        mTrackAdapter.clear();
                        mTrackAdapter.addAll(trackList);
                        mLastSearchResults = trackList;
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("RetrofitError when loading Artist Tracks: ", error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);
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
