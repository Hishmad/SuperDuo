package it.jaschke.alexandria.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;

/**
 *
 */
public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String LIST_OF_BOOKS = "ListOfBooks";

    // Constant
    private static final int BOOK_LIST_ADAPTER_FLAG = 0;
    private static final int LOADER_ID = 10;


    // Private fields, member variables
    private BookListAdapter mBookListAdapter;
    private ListView mBookList;
    private int mPosition = ListView.INVALID_POSITION;
    private EditText mSearchText;

    // Empty constructor
    public ListOfBooks() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Moving this code from onAttach() for screen rotation
        getActivity().setTitle(R.string.books);


        // Initializing the Cursor object with getContentResolver().query(...);
        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        // Initializing the Cursor Adapter, we pass the Cursor object into the second argument
        mBookListAdapter = new BookListAdapter(getActivity(),
                cursor,
                BOOK_LIST_ADAPTER_FLAG);

        // Initializing the root view and inflate the layout.
        View rootView = inflater.inflate(R.layout.fragment_list_of_books,
                container,
                false);

        // Initializing the EditText
        mSearchText = (EditText) rootView.findViewById(R.id.searchText);


        // Initializing the list view.
        mBookList = (ListView) rootView.findViewById(R.id.listOfBooks);

        // set the adapter
        mBookList.setAdapter(mBookListAdapter);

        // Initialing the search button then set the click listener
        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // This will trigger to reload everything in the list.
                        ListOfBooks.this.restartLoader();
                    }
                }
        );

        // set the item click
        mBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mBookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback) getActivity())
                            .onItemSelected(
                                    cursor.getString(
                                            cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        // set to delete item from the list
        mBookList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Cursor cursor = mBookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {

                    // Instantiate AlertDialog
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.delete_entry_alert_dialog)
                            .setMessage(R.string.message_delete_alert_dialog)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    // Use BookService.class to do the delete job
                                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                                    bookIntent.putExtra(BookService.EAN,
                                            cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                                    bookIntent.setAction(BookService.DELETE_BOOK);
                                    getActivity().startService(bookIntent);

                                    Toast.makeText(getActivity(), R.string.book_delete_toast, Toast.LENGTH_SHORT).show();
                                    ListOfBooks.this.restartLoader();

                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                return true;
            }
        });


        return rootView;
    }

    /**
     * This method will restart the loader, when the user click the search button
     */
    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection =
                AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = mSearchText.getText().toString();

        if(searchString.length() > 0){
            searchString = "%" + searchString + "%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString, searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBookListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mBookList.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.swapCursor(null);
    }


    /**
     * no need this code makes trouble
     * @param activity

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    */
}
