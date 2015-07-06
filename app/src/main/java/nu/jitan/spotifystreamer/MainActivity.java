package nu.jitan.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import nu.jitan.spotifystreamer.ui.artist.ArtistFragment;

public final class MainActivity extends AppCompatActivity {

    private static final String ARTIST_FRAGMENT_TAG = "main_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_main_container, new ArtistFragment(), ARTIST_FRAGMENT_TAG)
                .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            ((ArtistFragment) getSupportFragmentManager().findFragmentByTag(ARTIST_FRAGMENT_TAG))
                .handleIntent(intent);
        }
    }
}
