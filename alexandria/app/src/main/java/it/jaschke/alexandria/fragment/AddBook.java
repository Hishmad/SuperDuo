package it.jaschke.alexandria.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import it.jaschke.alexandria.CameraPreview.BarcodeScanner;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Constant
    private static final int LOADER_ID = 1;
    public static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    public static final String SCAN_FORMAT = "scanFormat";
    public static final String SCAN_CONTENTS = "scanContents";
    private static final String EAN_CONTENT = "eanContent";
    private static final String ISBN_START_NUMBER = "978";
    private static final String SPLIT_SIGN = ",";
    private static final String LINE_FEED_SIGN = "\n";


    // Member variables.
    private EditText mEanTextView;
    private View mRootView;

    // Constructor
    public AddBook() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    /**
     * Save user input
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /*
        if (mEanTextView != null) {
            outState.putString(EAN_CONTENT, mEanTextView.getText().toString());
        } */

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Moving this code from onAttach() for screen rotation
        getActivity().setTitle(R.string.scan);

        // Initializing
        mRootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEanTextView = (EditText) mRootView.findViewById(R.id.ean);

        // Restore user input when configuration changed
        /*
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EAN_CONTENT)) {
                mEanTextView.setText(savedInstanceState.getString(EAN_CONTENT));
                //mEanTextView.setHint(""); No need this
            }
        } */

        mEanTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith(ISBN_START_NUMBER)) {
                    ean = ISBN_START_NUMBER + ean;
                }

                if (ean.length() == 0) {
                    clearFields();
                    return;
                }

                if (ean.length() < 13) {
                    //clearFields(); no need
                    return;
                }
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
            }
        });

        mRootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startScanner = new Intent(getActivity(), BarcodeScanner.class);
                startActivityForResult(startScanner, LOADER_ID);
            }
        });

        mRootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This is another bug which can cause a crash when hit back button and hit delete.
                //((MainActivity) getActivity()).navigationDrawerFragment.selectItem(0);

                // Better change to this line of code.
                if (mEanTextView.length() > 0) {
                    clearFields();
                    Toast.makeText(getActivity(), R.string.book_saved_toast, Toast.LENGTH_SHORT).show();
                    mEanTextView.setText("");
                } else {
                    clearFields();
                }

            }
        });

        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEanTextView.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEanTextView.setText("");
                clearFields();
            }
        });


        return mRootView;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mEanTextView != null && mEanTextView.getText().length() > 0) {
            clearFields();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (LOADER_ID): {
                if (resultCode == Activity.RESULT_OK) {
                    String lScanContents = data.getStringExtra(SCAN_CONTENTS);
                    //String lScanFormat = data.getStringExtra(SCAN_FORMAT); no need
                    mEanTextView.setText(lScanContents);
                }
                break;
            }
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mEanTextView.getText().length() == 0) {
            return null;
        }
        String eanStr = mEanTextView.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith(ISBN_START_NUMBER)) {
            eanStr = ISBN_START_NUMBER + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));

        // Avoid crash
        if (authors != null) {
            String[] authorsArr = authors.split(SPLIT_SIGN);
            ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(SPLIT_SIGN, LINE_FEED_SIGN));
        }

        ImageView bookCover = (ImageView) mRootView.findViewById(R.id.bookCover);
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (imgUrl != null && imgUrl.length() > 0) {
            if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                // Replaced image loader with Glide and add getActivity to prevent crash.
                if (getActivity() != null && !getActivity().isFinishing()) {
                    Glide.with(getActivity()).load(imgUrl).into(bookCover);
                    bookCover.setVisibility(View.VISIBLE);
                }
            }
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);

        mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.authors)).setText("");
        ((TextView) mRootView.findViewById(R.id.categories)).setText("");
        mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    /**
     * No need this code
     * @param activity

     @Override public void onAttach(Activity activity) {
     super.onAttach(activity);
     }
     */
}
