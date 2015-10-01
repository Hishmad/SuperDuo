package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    // Constant
    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";
    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    // Constructor
    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();
            final String ean = intent.getStringExtra(EAN);

            if (FETCH_BOOK.equals(action)) {
                Utility.fetchBook(ean, getBaseContext());
            } else if (DELETE_BOOK.equals(action)) {
                Utility.deleteBook(ean, getBaseContext());
            }
        }
    }

}