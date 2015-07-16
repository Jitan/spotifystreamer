package nu.jitan.spotifystreamer.ui.player;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import hugo.weaving.DebugLog;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;

@DebugLog
public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            PlayerFragment playerFragment = new PlayerFragment();

            args.putParcelableArrayList(Util.TRACKLIST_KEY,
                getIntent().getParcelableArrayListExtra(Util.TRACKLIST_KEY));

            args.putInt(Util.TRACKLIST_POSITION_KEY,
                getIntent().getIntExtra(Util.TRACKLIST_POSITION_KEY, 0));

            playerFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, playerFragment)
                .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
