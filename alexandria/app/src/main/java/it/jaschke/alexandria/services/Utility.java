package it.jaschke.alexandria.services;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.activity.MainActivity;
import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Created by hishmadabubakaralamudi on 9/26/15.
 */
public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

    }

    /**
     * Insert into Database
     *
     * @param ean
     * @param title
     * @param subtitle
     * @param desc
     * @param imgUrl
     */
    public static void writeBackBook(String ean,
                                     String title,
                                     String subtitle,
                                     String desc,
                                     String imgUrl,
                                     Context context) {

        ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        context.getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);


    }

    /**
     * Insert Author into Database
     *
     * @param ean
     * @param jsonArray
     * @throws JSONException
     */
    public static void writeBackAuthors(String ean, JSONArray jsonArray, Context context)
            throws JSONException {
        ContentValues values = new ContentValues();

        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                values.put(AlexandriaContract.AuthorEntry._ID, ean);
                values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
                context.getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
                values = new ContentValues();
            }
        } else if (jsonArray.length() == 0) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR,
                    context.getString(R.string.not_available_any_message));
            context.getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
        }
    }

    /**
     * Insert Category into Database
     *
     * @param ean
     * @param jsonArray
     * @throws JSONException
     */
    public static void writeBackCategory(String ean, JSONArray jsonArray, Context context)
            throws JSONException {
        ContentValues values = new ContentValues();

        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                values.put(AlexandriaContract.CategoryEntry._ID, ean);
                values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
                context.getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
                values = new ContentValues();
            }
        } else if (jsonArray.length() == 0) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY,
                    context.getString(R.string.not_available_any_message));
            context.getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    public static void deleteBook(String ean, Context context) {
        if (ean != null) {
            // Type safety for long ean, to prevent crash.
            try {
                context.getContentResolver()
                        .delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                                null,
                                null);
            } catch (Exception e) {
                return;
            }

        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    public static void fetchBook(String ean, Context context) {

        if (ean.length() != 13) {
            return;
        }

        Cursor bookEntry = null;

        // Must have type safety for ean before parse to long, maybe better to use try/catch block
        try {
            bookEntry = context.getContentResolver().query(
                    AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );
        } catch (Exception e) {
            return;
        } finally {
            if (bookEntry.getCount() > 0) {
                bookEntry.close();
                return;
            }
            bookEntry.close();
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        // This checking isNetworkAvailable() will prevent app from crash, while offline and the user
        // wishes to scan/type a barcode. so this line of code is very important.
        if (isNetworkAvailable(context)) {
            try {
                final String FORECAST_BASE_URL = context.getString(R.string.google_api_book_url);
                final String QUERY_PARAM = context.getString(R.string.query_param);

                final String ISBN_PARAM = context.getString(R.string.isbn_param) + ean;

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(context.getString(R.string.set_request_method));
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append("\n");
                }

                if (buffer.length() == 0) {
                    return;
                }

                bookJsonString = buffer.toString();

            } catch (Exception e) {
                Log.e(LOG_TAG, context.getString(R.string.error_tag), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, context.getString(R.string.error_tag), e);
                    }
                }

            }

            // JSON Keys
            final String ITEMS = context.getString(R.string.json_key_items);
            final String VOLUME_INFO = context.getString(R.string.json_key_volume_info);
            final String TITLE = context.getString(R.string.json_key_title);
            final String SUBTITLE = context.getString(R.string.json_key_subtitle);
            final String AUTHORS = context.getString(R.string.json_key_author);
            final String DESC = context.getString(R.string.json_key_desc);
            final String CATEGORIES = context.getString(R.string.json_key_categories);
            final String IMG_URL_PATH = context.getString(R.string.json_key_image_url_path);
            final String IMG_URL = context.getString(R.string.json_key_image_url);

            try {
                JSONObject bookJson = new JSONObject(bookJsonString);
                JSONArray bookArray;

                if (bookJson.has(ITEMS)) {
                    bookArray = bookJson.getJSONArray(ITEMS);
                } else {
                    Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                    messageIntent.putExtra(MainActivity.MESSAGE_KEY,
                            context.getResources().getString(R.string.not_found));
                    LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(messageIntent);
                    return;
                }

                JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

                String title = bookInfo.getString(TITLE);

                String subtitle = "";
                if (bookInfo.has(SUBTITLE)) {
                    subtitle = bookInfo.getString(SUBTITLE);
                } else if (!bookInfo.has(SUBTITLE) || subtitle.length() == 0) {
                    subtitle = context.getString(R.string.not_available_any_message);
                }

                String desc = "";
                if (bookInfo.has(DESC)) {
                    desc = bookInfo.getString(DESC);
                } else if (!bookInfo.has(DESC) || desc.length() == 0) {
                    desc = context.getString(R.string.not_available_any_message);
                }

                String imgUrl = "";
                if (bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                    imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
                } else if (imgUrl.length() == 0) {
                    imgUrl = context.getString(R.string.not_available_any_message);
                }

                writeBackBook(ean, title, subtitle, desc, imgUrl, context);

                if (bookInfo.has(AUTHORS)) {
                    writeBackAuthors(ean, bookInfo.getJSONArray(AUTHORS), context);
                }


                if (bookInfo.has(CATEGORIES)) {
                    writeBackCategory(ean, bookInfo.getJSONArray(CATEGORIES), context);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, context.getString(R.string.error_tag), e);
            }
        } else {
            // Notify the user when there is no network connection with a toast.
            Toast.makeText(context, R.string.no_network_connection, Toast.LENGTH_SHORT).show();
        }
    }


}
