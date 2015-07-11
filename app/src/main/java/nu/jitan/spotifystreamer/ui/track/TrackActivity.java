package nu.jitan.spotifystreamer.ui.track;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.event.TrackClickedEvent;
import nu.jitan.spotifystreamer.ui.player.PlayerFragment;


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
        Bundle args = new Bundle();
        args.putParcelable(Util.TRACK_KEY, event.track);
        PlayerFragment playerFragment = new PlayerFragment();
        playerFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .add(R.id.track_container, playerFragment, Util.PLAYERFRAGMENT_TAG)
            .addToBackStack(null).commit();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
