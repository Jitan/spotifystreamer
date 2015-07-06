package nu.jitan.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;

public final class MainActivity extends AppCompatActivity {

    private static final String MAIN_FRAGMENT_TAG = "main_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new MainFragment(), MAIN_FRAGMENT_TAG)
                .commit();
        } else {
            MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag
                (MAIN_FRAGMENT_TAG);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mainFragment)
                .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            ((MainFragment)getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG)).handleIntent(intent);
        }
    }
}
