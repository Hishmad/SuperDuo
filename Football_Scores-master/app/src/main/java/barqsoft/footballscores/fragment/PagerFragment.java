package barqsoft.footballscores.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.activity.MainActivity;
import barqsoft.footballscores.support.Utilies;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {

    // Constant
    public static final int NUM_PAGES = 5;
    private static final int MILIS_CONVERT = 86400000;
    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    // Member variables
    public ViewPager mPagerHandler;
    private PageAdapter mPagerAdapter;
    private MainScreenFragment[] mViewFragmentsArray = new MainScreenFragment[5];

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);

        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new PageAdapter(getChildFragmentManager());

        for (int i = 0; i < NUM_PAGES; i++) {

            Date lFragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * MILIS_CONVERT));
            SimpleDateFormat lSimpleFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

            mViewFragmentsArray[i] = new MainScreenFragment();
            mViewFragmentsArray[i].setFragmentDate(lSimpleFormat.format(lFragmentDate));
        }

        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.current_fragment);

        return rootView;
    }

    /**
     * Inner Class
     */
    private class PageAdapter extends FragmentStatePagerAdapter {

        @Override
        public Fragment getItem(int i) {
            return mViewFragmentsArray[i];
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Returns the page title for the top indicator
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return Utilies.getNameOfTheDay(getActivity(), System.currentTimeMillis() + ((position - 2) * MILIS_CONVERT));
        }
    }
}
