package nu.jitan.spotifystreamer.ui.track;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.ui.player.PlayerActivity;


public final class TrackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            TrackFragment trackFragment = new TrackFragment();

            args.putParcelable(TrackFragment.ARTIST_KEY, getIntent().getParcelableExtra
                (TrackFragment.ARTIST_KEY));
            trackFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.track_container, trackFragment)
                .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onEvent(TrackClickedEvent event) {
        Intent loadPlayerIntent = new Intent(this, PlayerActivity.class);
        loadPlayerIntent.putExtra(Util.TRACKLIST_KEY, event.trackList);
        loadPlayerIntent.putExtra(Util.TRACKLIST_POSITION_KEY, event.trackListPos);
        startActivity(loadPlayerIntent);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
