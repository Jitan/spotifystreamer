package nu.jitan.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import de.greenrobot.event.EventBus;
import nu.jitan.spotifystreamer.event.ArtistClickedEvent;
import nu.jitan.spotifystreamer.ui.search.SearchFragment;
import nu.jitan.spotifystreamer.ui.track.TrackActivity;
import nu.jitan.spotifystreamer.ui.track.TrackFragment;

public final class MainActivity extends AppCompatActivity {
    private boolean mTwoPane;
    private static final String TRACKFRAGMENT_TAG = "TRACKTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.track_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_container, new TrackFragment())
                    .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(ArtistClickedEvent event) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(TrackFragment.ARTIST_KEY, event.artist);

            TrackFragment trackFragment = new TrackFragment();
            trackFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.track_container, trackFragment)
                .commit();
        } else {
            Intent loadTracksIntent = new Intent(this, TrackActivity.class);
            loadTracksIntent.putExtra(TrackFragment.ARTIST_KEY, event.artist);
            startActivity(loadTracksIntent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            ((SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_search))
                .handleIntent(intent);
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
