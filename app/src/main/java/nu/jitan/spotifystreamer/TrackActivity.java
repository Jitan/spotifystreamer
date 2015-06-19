package nu.jitan.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import java.util.List;


public class TrackActivity extends AppCompatActivity {
    private TrackAdapter mTrackAdapter;
    @InjectView(R.id.listview_track) ListView mTrackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ButterKnife.inject(this);

        mTrackAdapter = new TrackAdapter(this);
        mTrackList.setAdapter(mTrackAdapter);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("tracks")) {
            List<MyTrack> trackList = ((MyTrackList) intent.getParcelableExtra
                ("tracks")).getMyTrackList();
            mTrackAdapter.clear();
            mTrackAdapter.addAll(trackList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
