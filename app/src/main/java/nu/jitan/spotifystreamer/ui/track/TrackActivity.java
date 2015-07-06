package nu.jitan.spotifystreamer.ui.track;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import nu.jitan.spotifystreamer.R;


public final class TrackActivity extends AppCompatActivity {
    private static final String TRACK_FRAGMENT_TAG = "track_fragment_tag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_track_container, new TrackFragment(), TRACK_FRAGMENT_TAG)
                .commit();
        }
    }
}
