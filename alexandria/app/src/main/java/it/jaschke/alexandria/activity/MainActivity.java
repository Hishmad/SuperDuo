package it.jaschke.alexandria.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.fragment.About;
import it.jaschke.alexandria.fragment.AddBook;
import it.jaschke.alexandria.fragment.BookDetail;
import it.jaschke.alexandria.fragment.ListOfBooks;
import it.jaschke.alexandria.fragment.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback {

    // No need
    //Context context;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen mTitle. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // Member variables
    private BroadcastReceiver mMessageReciever;
    private int mPosition;

    // Constant
    public static final int POSITION_ZERO = 0;
    public static final int POSTITION_ONE = 1;
    public static final int POSITION_TWO = 2;
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static final String FRAG_TAG_ZERO = "tagZero";
    public static final String FRAG_TAG_ONE = "tagOne";
    public static final String FRAG_TAG_TWO = "tagTwo";
    public static final String FRAG_TAG_THREE = "tagThree";
    private static final String KEY_POSITION = "key";
    private static boolean IS_TABLET = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restore savedInstanceState
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION);
        }

        // Setup the layout for phone or tablet
        if (findViewById(R.id.right_container) != null) {
            IS_TABLET = true;
        }

        // Initialize broadcast receiver
        mMessageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciever, filter);

        // Initialize NavigationDrawerFragment
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    /**
     * Store data into onSaveInstanceState
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_POSITION, mPosition);
        super.onSaveInstanceState(outState);
    }

    /**
     * Especially on configuration changes such as screen orientation
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (findViewById(R.id.right_container) != null) {
            IS_TABLET = true;
            findViewById(R.id.right_container).setVisibility(View.VISIBLE);
        } else {
            IS_TABLET = false;
        }

        if (mPosition == 1 && findViewById(R.id.right_container) != null) {
            findViewById(R.id.right_container).setVisibility(View.INVISIBLE);
        }

        if (mPosition == 0 && findViewById(R.id.right_container) == null) {
            getSupportFragmentManager().popBackStackImmediate(FRAG_TAG_ZERO, 0);

        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        mPosition = position;

        ListOfBooks listOfBooks = new ListOfBooks();
        AddBook addBook = new AddBook();
        About about = new About();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (position) {
            default:
            case POSITION_ZERO:
                fragmentTransaction
                        .replace(R.id.container, listOfBooks, FRAG_TAG_ZERO)
                        .addToBackStack(FRAG_TAG_ZERO)
                        .commit();

                break;
            case POSTITION_ONE:

                if (IS_TABLET) {
                    findViewById(R.id.right_container).setVisibility(View.INVISIBLE);
                }
                
                fragmentTransaction
                        .replace(R.id.container, addBook , FRAG_TAG_ONE)
                        .addToBackStack(FRAG_TAG_TWO)
                        .commit();
                break;
            case POSITION_TWO:
                fragmentTransaction
                        .replace(R.id.container, about, FRAG_TAG_TWO)
                        .addToBackStack(FRAG_TAG_TWO)
                        .commit();
                break;

        }

    }

    public void setTitle(int titleId) {
        mTitle = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReciever);
        super.onDestroy();
    }

    /**
     * The call back from the ListOfBooks
     * @param ean
     */
    @Override
    public void onItemSelected(String ean) {

        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        ListOfBooks listOfBooks = new ListOfBooks();
        BookDetail bookDetail = new BookDetail();
        bookDetail.setArguments(args);


        // If two pane use FragmentManager, else use intent
        if (IS_TABLET) {
            findViewById(R.id.right_container).setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, listOfBooks, FRAG_TAG_ZERO)
                    .addToBackStack(FRAG_TAG_ZERO)
                    .replace(R.id.right_container, bookDetail, FRAG_TAG_THREE)
                    .addToBackStack(FRAG_TAG_THREE)
                    .commit();

        } else {
            Intent i = new Intent(this, BookDetailActivity.class);
            i.putExtra(BookDetail.EAN_KEY, ean);
            startActivity(i);
        }


    }

    /**
     * no need.
     * @return

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    } */

    @Override
    public void onBackPressed() {

        // Exit the app if the fragment in back stack is only 1.
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            finish();
        }

        // Return to the ListOfBooks from anywhere else.
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            if (getSupportFragmentManager().findFragmentByTag(FRAG_TAG_ZERO) != null) {
                boolean xBackStack = getSupportFragmentManager().popBackStackImmediate(FRAG_TAG_ZERO, 0);
            }
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    /**
     * Inner class
     */
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

}