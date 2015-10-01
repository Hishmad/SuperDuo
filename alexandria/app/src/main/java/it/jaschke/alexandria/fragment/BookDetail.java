package it.jaschke.alexandria.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import it.jaschke.alexandria.activity.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;

/**
 * The app will crash once the user rotate the screen, and we did correct this problem
 */
public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Constant
    private final int LOADER_ID = 10;
    public static final String EAN_KEY = "EAN";

    // Member variables
    private View mRootView;
    private String mEan;
    private String mBookTitle;
    private ShareActionProvider mShareActionProvider;

    // Constructor
    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Screen title on action bar
        getActivity().setTitle(R.string.title_book_detail);


        Bundle arguments = getArguments();
        if (arguments != null) {
            mEan = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        mRootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        
        // When user click delete button
        // The idea of this delete button is not supported in the right way..
        // It will have some bad behavior especial during two pane mode,
        // how ever, we already make the delete in the ListOfBook when user longClick the item.
        // Now we have to options to fix this, rewrite the code below or just
        // disable it so the user will no longer delete item from the BookDetail
        // but still can delete item by long click on the item at ListOfBooks.
        // For this time I will take the second option.
        Button deleteButton = (Button) mRootView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEan);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                //getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });

        // Disable the delete button when two pane mode.
        if (getActivity().findViewById(R.id.right_container) != null) {
            deleteButton.setVisibility(View.INVISIBLE);
        }
        return mRootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // This is to correct the crash on screen rotation, this code been removed from onLoadFinished()
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType(getActivity().getString(R.string.share_intent_book_detail));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + mBookTitle);
        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(mEan)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        
        // Make sure the cursor is valid
        if (!data.moveToFirst()) {
            return;
        }

        // The book Title
        mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        TextView textBookTitle = ((TextView) mRootView.findViewById(R.id.fullBookTitle));
        textBookTitle.setText(mBookTitle);

        ImageView fullBookCover = (ImageView) mRootView.findViewById(R.id.fullBookCover);


        // Remove the shareIntent from here in to onCreateOptionsMenu, this will correct the crash
        // Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        // shareIntent.setType(getActivity().getString(R.string.share_intent_book_detail));
        // shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text)+ mBookTitle);
        // mShareActionProvider.setShareIntent(shareIntent);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) mRootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");

        ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));

        // Get the Image URL from the database table
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            // Replaced image loader with Glide
            try {
                Glide.with(getActivity()).load(imgUrl).into(fullBookCover);
            } catch (Exception e) {
                fullBookCover.setImageResource(R.drawable.ic_launcher);
            }
            // No need this, because I have replaced this code with Glide.
            //new DownloadImage((ImageView) mRootView.findViewById(R.id.fullBookCover)).execute(imgUrl);

            mRootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);

        /* no Need for the this
        if(mRootView.findViewById(R.id.right_container)!=null){
            mRootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }
        */

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    /* no need this
    @Override
    public void onPause() {
        super.onDestroyView();
        if(MainActivity.IS_TABLET && mRootView.findViewById(R.id.right_container)==null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    } */

}