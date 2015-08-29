package nu.jitan.spotifystreamer.ui.player;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.Util;
import nu.jitan.spotifystreamer.service.events.PlayerServiceStoppedEvent;

@DebugLog
public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        EventBus.getDefault().register(this);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();

            args.putParcelableArrayList(Util.TRACKLIST_KEY,
                getIntent().getParcelableArrayListExtra(Util.TRACKLIST_KEY));
            args.putInt(Util.TRACKLIST_POSITION_KEY,
                getIntent().getIntExtra(Util.TRACKLIST_POSITION_KEY, 0));

            PlayerFragment playerFragment = (PlayerFragment) getSupportFragmentManager()
                .findFragmentByTag(Util.PLAYERFRAGMENT_TAG);

            if (playerFragment == null || !playerFragment.getTrackList().equals(args
                .getParcelableArrayList(Util.TRACKLIST_KEY)))
            {
                playerFragment = new PlayerFragment();
            }

            playerFragment.setUiArguments(args);

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, playerFragment, Util.PLAYERFRAGMENT_TAG)
                .commit();
        }
    }

    public void onEvent(PlayerServiceStoppedEvent event) {
        getSupportFragmentManager().beginTransaction().remove(
            getSupportFragmentManager().findFragmentByTag(Util.PLAYERFRAGMENT_TAG)
        ).commit();
        finish();


//        Intent loadTracksIntent = new Intent(this, TrackActivity.class);
//        ArtistClickedEvent artistClickedEvent = EventBus.getDefault().getStickyEvent
// (ArtistClickedEvent.class);
//        loadTracksIntent.putExtra(TrackFragment.ARTIST_KEY, artistClickedEvent.artist);
//        startActivity(loadTracksIntent);
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

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
