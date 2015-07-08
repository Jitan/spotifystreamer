package nu.jitan.spotifystreamer.ui.track;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import nu.jitan.spotifystreamer.R;


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
}
