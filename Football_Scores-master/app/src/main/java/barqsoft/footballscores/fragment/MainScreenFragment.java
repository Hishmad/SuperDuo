package barqsoft.footballscores.fragment;

import android.content.Context;
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
import android.widget.ListView;

import barqsoft.footballscores.R;
import barqsoft.footballscores.activity.MainActivity;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.service.MyFetchService;
import barqsoft.footballscores.support.ScoresAdapter;
import barqsoft.footballscores.support.ViewHolder;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Constant
    private static final String TAG = MainScreenFragment.class.getSimpleName();
    public static final int SCORES_LOADER = 0;

    // Member variable
    public ScoresAdapter mAdapter;
    private String[] fragmentdate = new String[1];
    private int mLastSelectedItem = -1;

    // Empty constructor
    public MainScreenFragment() { /* no code */ }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        updateScores(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView lScoreList = (ListView) rootView.findViewById(R.id.scores_list);

        mAdapter = new ScoresAdapter(getActivity(), null, 0);

        lScoreList.setAdapter(mAdapter);

        getLoaderManager().initLoader(SCORES_LOADER, null, this);

        mAdapter.detail_match_id = MainActivity.selected_match_id;

        lScoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, fragmentdate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.v(TAG, "loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

//        int i = 0;
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            i++;
//            cursor.moveToNext();
//        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    /**
     * This method will start a service then fetch the data.
     * @param context
     */
    private void updateScores(Context context) {
        context.startService(new Intent(getActivity(), MyFetchService.class));
    }

    /**
     * A setter for the element 0.
     * @param date
     */
    public void setFragmentDate(String date) {

        fragmentdate[0] = date;
    }


}
