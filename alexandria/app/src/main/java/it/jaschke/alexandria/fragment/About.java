package it.jaschke.alexandria.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.jaschke.alexandria.R;

/**
 * Done some minor changes in Line 26,27 and remove onAttach() method.
 */
public class About extends Fragment {

    public About(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Moving this code from onAttach() for screen rotation
        getActivity().setTitle(R.string.about);


        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        return rootView;
    }

    /**
     * No need this code.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

}
