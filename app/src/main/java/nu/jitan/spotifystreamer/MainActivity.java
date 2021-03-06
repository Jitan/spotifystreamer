package nu.jitan.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import de.greenrobot.event.EventBus;
import nu.jitan.spotifystreamer.ui.search.ArtistClickedEvent;
import nu.jitan.spotifystreamer.ui.track.TrackClickedEvent;
import nu.jitan.spotifystreamer.ui.player.PlayerFragment;
import nu.jitan.spotifystreamer.ui.search.SearchFragment;
import nu.jitan.spotifystreamer.ui.track.TrackActivity;
import nu.jitan.spotifystreamer.ui.track.TrackFragment;

public final class MainActivity extends AppCompatActivity {
    private boolean mTwoPane;

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

    public void onEvent(ArtistClickedEvent event) {
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

    public void onEvent(TrackClickedEvent event) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelableArrayList(Util.TRACKLIST_KEY, event.trackList);
            args.putInt(Util.TRACKLIST_POSITION_KEY, event.trackListPos);
            args.putBoolean(Util.IS_TWOPANE_KEY, mTwoPane);

            PlayerFragment playerFragment = new PlayerFragment();
            playerFragment.setUiArguments(args);
            playerFragment.show(getSupportFragmentManager(), "dialog");
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
