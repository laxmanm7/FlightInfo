package com.ywsggip.flightinfo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ywsggip.flightinfo.ResultsFragment.TripDetails;

import java.util.ArrayList;

/**
 * Created by Admin on 2015-06-09.
 */
public class ResultsActivity extends AppCompatActivity implements ResultsFragment.OnResultSelectedListener {

    private final String LOG_TAG = ResultsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ResultsFragment resultsFragment = new ResultsFragment();
        fragmentTransaction.add(R.id.results_container, resultsFragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onResultSelected(ArrayList<TripDetails> data, ArrayList<TripDetails> returnData) {
        ResultDetailsFragment fragment = ResultDetailsFragment.newInstance(data, returnData);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        //transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.results_container, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
