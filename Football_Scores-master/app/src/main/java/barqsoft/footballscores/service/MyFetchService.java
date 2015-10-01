package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class MyFetchService extends IntentService {

    public static final String LOG_TAG = "myFetchService";
    private static final int CONVERT = 86400000;
    private static final String PAST_DAYS = "p2";
    private static final String NEXT_DAYS = "n2";
    private static final String SIMPLE_DATE_FORMAT_ONE = "yyyy-MM-ddHH:mm:ss";
    private static final String SIMPLE_DATE_FORMAT_TWO = "yyyy-MM-dd:HH:mm";
    private static final String SIMPLE_DATE_FORMAT_THREE = "yyyy-MM-dd";

    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.ACTION_DATA_UPDATED";

    public MyFetchService() {
        super("myFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData(NEXT_DAYS);
        getData(PAST_DAYS);

        return;
    }

    private void getData(String timeFrame) {
        //Creating fetch URL
        final String BASE_URL = getString(R.string.base_URL); //Base URL
        final String QUERY_TIME_FRAME = getString(R.string.query_time_frame); //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();

        Log.v(LOG_TAG, fetch_build.toString()); //log spam

        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;

        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            JSON_data = buffer.toString();
        } catch (Exception e) {
            //Log.e(LOG_TAG, "Exception here" + e.getMessage());
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //Log.e(LOG_TAG, "Error Closing Stream");
                }
            }
        }
        try {
            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data).getJSONArray(getString(R.string.fixture));
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONdata(getString(R.string.dummy_data),
                            getApplicationContext(), false);
                    return;
                }

                processJSONdata(JSON_data, getApplicationContext(), true);
            } else {
                //Could not Connect
                //Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch (Exception e) {
            //Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void processJSONdata(String JSONdata, Context mContext, boolean isReal) {

        //Log.d(LOG_TAG, "Json: " + JSONdata);

        //JSON data
        final String SERIE_A = getString(R.string.serie_a);
        final String PREMIER_LEGAUE = getString(R.string.premier_league);
        final String CHAMPIONS_LEAGUE = getString(R.string.champion_league);
        final String PRIMERA_DIVISION = getString(R.string.primera_division);
        final String BUNDESLIGA = getString(R.string.bundesliga_liga);
        final String SEASON_LINK = getString(R.string.season_link_url);
        final String MATCH_LINK = getString(R.string.match_link_url);
        final String FIXTURES = getString(R.string.fixture);
        final String LINKS = getString(R.string.links);
        final String SOCCER_SEASON = getString(R.string.soccer_season);
        final String SELF = getString(R.string.self);
        final String MATCH_DATE = getString(R.string.match_date);
        final String HOME_TEAM = getString(R.string.home_team);
        final String AWAY_TEAM = getString(R.string.away_team);
        final String RESULT = getString(R.string.result);
        final String HOME_GOALS = getString(R.string.home_goals);
        final String AWAY_GOALS = getString(R.string.away_goals);
        final String MATCH_DAY = getString(R.string.match_day);

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {
                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString(getString(R.string.href));
                League = League.replace(SEASON_LINK, "");
                if (isReal || League.equals(PREMIER_LEGAUE) ||
                        League.equals(SERIE_A) ||
                        League.equals(CHAMPIONS_LEAGUE) ||
                        League.equals(BUNDESLIGA) ||
                        League.equals(PRIMERA_DIVISION)) {
                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString(getString(R.string.href));
                    match_id = match_id.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf(getString(R.string.t)) + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf(getString(R.string.t)));
                    SimpleDateFormat match_date = new SimpleDateFormat(SIMPLE_DATE_FORMAT_ONE);
                    match_date.setTimeZone(TimeZone.getTimeZone(getString(R.string.time_zone_utc)));
                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TWO);
                        newSimpleDateFormat.setTimeZone(TimeZone.getDefault());
                        mDate = newSimpleDateFormat.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date lDate = new Date(System.currentTimeMillis() + ((i - 2) * CONVERT));
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT_THREE);
                            mDate = simpleDateFormat.format(lDate);
                        }
                    } catch (Exception e) {
                        //Log.d(LOG_TAG, "error here!");
                        //Log.e(LOG_TAG, e.getMessage());
                    }
                    Home = match_data.getString(HOME_TEAM);
                    Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);

                    //log spam
                    //Log.v(LOG_TAG,match_id);
                    //Log.v(LOG_TAG,mDate);
                    //Log.v(LOG_TAG,mTime);
                    //Log.v(LOG_TAG,Home);
                    //Log.v(LOG_TAG,Away);
                    //Log.v(LOG_TAG,Home_goals);
                    //Log.v(LOG_TAG,Away_goals);

                    values.add(match_values);
                }
            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);

            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));

            getWidgets(mContext);
        } catch (JSONException e) {
            //Log.e(LOG_TAG, e.getMessage());
        }

    }

    private void getWidgets(Context context) {
        Intent intent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}

