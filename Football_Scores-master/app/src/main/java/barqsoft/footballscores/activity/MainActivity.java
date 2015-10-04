package barqsoft.footballscores.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.fragment.PagerFragment;
import barqsoft.footballscores.R;

public class MainActivity extends ActionBarActivity {

    // Constant
    private static final String KEY_PAGER_CURRENT = "pagerCurrent";
    private static final String KEY_SELECTED_MATCH = "selectedMatch";
    private static final String KEY_FRAGMENT = "pagerFragment";

    // Static global variables
    public static int selected_match_id;
    public static int current_fragment = 2;

    // Member variable
    private PagerFragment mPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the fragment dynamically.
        if (savedInstanceState == null) {
            mPagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPagerFragment)
                    .commit();
        }
    }


    /**
     * Inflate the option menu into the action bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * When user select an item in the menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // This will go to the AboutActivity.class
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(KEY_PAGER_CURRENT, mPagerFragment.mPagerHandler.getCurrentItem());
        outState.putInt(KEY_SELECTED_MATCH, selected_match_id);
        getSupportFragmentManager().putFragment(outState, KEY_FRAGMENT, mPagerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        current_fragment = savedInstanceState.getInt(KEY_PAGER_CURRENT);
        selected_match_id = savedInstanceState.getInt(KEY_SELECTED_MATCH);
        mPagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_FRAGMENT);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
