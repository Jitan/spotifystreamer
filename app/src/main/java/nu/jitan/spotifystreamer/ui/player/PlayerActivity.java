package nu.jitan.spotifystreamer.ui.player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            PlayerFragment playerFragment = new PlayerFragment();

            args.putParcelable(Util.TRACK_KEY, getIntent().getParcelableExtra(Util.TRACK_KEY));
            playerFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, playerFragment)
                .commit();
        }
    }
}
