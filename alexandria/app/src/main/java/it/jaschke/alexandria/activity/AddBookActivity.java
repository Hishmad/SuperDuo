package it.jaschke.alexandria.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.fragment.About;
import it.jaschke.alexandria.fragment.AddBook;
import it.jaschke.alexandria.fragment.ListOfBooks;
import it.jaschke.alexandria.fragment.NavigationDrawerFragment;

public class AddBookActivity extends ActionBarActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;
    /**
     * Used to store the last screen title.
     */
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        if (savedInstanceState == null) {
            AddBook addBook = new AddBook();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frameContainerAtActivityAddBook, addBook)
                    .commit();
        }


    }

}
