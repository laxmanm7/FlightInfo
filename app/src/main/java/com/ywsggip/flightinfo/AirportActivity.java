package com.ywsggip.flightinfo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ywsggip.flightinfo.data.AirportsContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class AirportActivity extends AppCompatActivity {

    RecentAirports mRecentAirports;

    private final String LOG_TAG = AirportActivity.class.getSimpleName();
    private String action;
    private AirportsAdapter mAirportsAdapter;
    private FrameLayout mListCover;
    private ListView mListView;

    private TextView mLastSearchedTextView;
    private View mLastSearchedDivider;

    private int mShortAnimationDuration;

    private String queryString;

    private String IATA_CODE;


    private static final String[] AIRPORT_COLUMNS = {
            AirportsContract.AirportEntry._ID,
            AirportsContract.AirportEntry.COLUMN_IATA_CODE,
            AirportsContract.AirportEntry.COLUMN_AIRPORT_NAME,
            AirportsContract.AirportEntry.COLUMN_CITY_NAME,
            AirportsContract.AirportEntry.COLUMN_COUNTRY_NAME
    };

    public static final int COL_AIRPORT_ID = 0;
    public static final int COL_AIRPORT_IATA = 1;
    public static final int COL_AIRPORT_NAME = 2;
    public static final int COL_AIRPORT_CITY = 3;
    public static final int COL_AIRPORT_COUNTRY = 4;

    private static MatrixCursor mRecentlyChosenAirport;
    private static SharedPreferences mSharedPref;

    public String getQueryString(){
        return queryString;
    }

    // extract IATA from data provided by searchForAirports(String)
    private String getIATACode(String data) {
        String IATACode;
        IATACode = data.substring(data.indexOf("[") + 1, data.indexOf("]"));
        return IATACode;
    }

    private List<String> searchForAirports(String query) {
        List<String> airports = new ArrayList<>();
        InputStream airportsData = getResources().openRawResource(R.raw.airports);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(airportsData));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null)
            {

                String[] airportArray = line.split(",");
                String IATACode = airportArray[4].replace("\"", "");
                String AirportName = airportArray[1].replace("\"", "");
                String Country = airportArray[3].replace("\"", "");
                String City = airportArray[2].replace("\"", "");

                // IATA code is required for request
                if(IATACode.isEmpty()) {
                    continue;
                }

                if(AirportName.toLowerCase().contains(query.toLowerCase())) {
                    airports.add("[" + IATACode + "] " + AirportName + ", " + City + ", " + Country);
                }
                else if (IATACode.toLowerCase().contains(query.toLowerCase())) {
                    airports.add("[" + IATACode + "] " + AirportName + ", " + City + ", " + Country);
                }
                else if (City.toLowerCase().contains(query.toLowerCase())) {
                    airports.add("[" + IATACode + "] " + AirportName + ", " + City + ", " + Country);
                }
                else if (Country.toLowerCase().contains(query.toLowerCase())) {
                    airports.add("[" + IATACode + "] " + AirportName + ", " + City + ", " + Country);
                }

            }
            return airports;
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return airports;
        }
    }

    private void hideLastSearchedLabel() {
        mLastSearchedTextView.setVisibility(View.GONE);
        mLastSearchedDivider.setVisibility(View.GONE);
    }

    private void showLastSearchedLabel() {
        mLastSearchedTextView.setVisibility(View.VISIBLE);
        mLastSearchedDivider.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport);

        mAirportsAdapter = new AirportsAdapter(this, null, 0);

        mRecentAirports = new RecentAirports(this);
        mAirportsAdapter.swapCursor(mRecentAirports.getCursor());

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mListCover = (FrameLayout) findViewById(R.id.listview_airports_cover);
        mListCover.getForeground().setAlpha(0);

        mLastSearchedTextView = (TextView)findViewById(R.id.lastSearchedTextView);
        mLastSearchedDivider = findViewById(R.id.lastSearchedDivider);
        if(mRecentAirports.getCursor() == null) {
            hideLastSearchedLabel();
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        if(Build.VERSION.SDK_INT >= 16)
            searchView.setImeOptions(searchView.getImeOptions()|EditorInfo.IME_FLAG_NO_EXTRACT_UI);


        EditText queryTextView = (EditText)searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        queryTextView.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                int alpha = mListCover.getForeground().getAlpha();
                if (hasFocus && alpha < 150)
                    coverFadeIn();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 1) {

                    hideLastSearchedLabel();
                    new FetchAirportsTask().execute(newText);
                } else {
                    mAirportsAdapter.setQuery(null);

                    if(mRecentAirports.getCursor() != null) {
                        showLastSearchedLabel();
                        mAirportsAdapter.swapCursor(mRecentAirports.getCursor());
                    }

                }
                return true;
            }
        });


        mListView = (ListView) findViewById(R.id.listview_airports);
        mListView.setAdapter(mAirportsAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AirportsAdapter adapter = (AirportsAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
                    IATA_CODE = cursor.getString(COL_AIRPORT_IATA);

                    String airport = String.format("(%s) %s, %s",
                            IATA_CODE,
                            cursor.getString(COL_AIRPORT_CITY),
                            cursor.getString(COL_AIRPORT_COUNTRY));

                    Intent intent = new Intent();
                    intent.putExtra(action, airport);
                    intent.putExtra("IATA", IATA_CODE);
                    setResult(RESULT_OK, intent);
                    searchView.clearFocus();

                    mRecentAirports.addAirport(IATA_CODE, cursor.getString(COL_AIRPORT_NAME), cursor.getString(COL_AIRPORT_CITY), cursor.getString(COL_AIRPORT_COUNTRY));
                    mRecentAirports.save();

                    finish();
                }

            }
        });


        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromInputMethod(v.getWindowToken(), 0);
                searchView.clearFocus();
                mListView.requestFocus();

                return false;
            }
        });
        mListView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int alpha = mListCover.getForeground().getAlpha();
                if (hasFocus && alpha > 0)
                    coverFadeOut();
            }
        });

        mListView.requestFocus();
        Intent intent = getIntent();

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new FetchAirportsTask().execute(query);
        } else {

            action = intent.getStringExtra("action");
            if(action.equals("origin")) {
                setTitle(R.string.select_origin);
            }
            else if (action.equals("destination")) {
                setTitle(R.string.select_destination);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, "fasdfad", Toast.LENGTH_LONG);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query = intent.getStringExtra(SearchManager.QUERY);
            queryString = new String(query);
            new FetchAirportsTask().execute(query);
        }
    }



    public class FetchAirportsTask extends AsyncTask<String, Integer, Cursor>{

        @Override
        protected Cursor doInBackground(String... params) {
            String query = params[0];
            Uri uri = AirportsContract.AirportEntry.buildAirportWithFilter(query);
            Cursor result = getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );

            mAirportsAdapter.setQuery(query);
            return result;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mAirportsAdapter.swapCursor(cursor);
        }
    }

    private void coverFadeIn()
    {
        ValueAnimator animation = new ValueAnimator().ofInt(0, 150);
        animation.setDuration(mShortAnimationDuration);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            Drawable cover = mListCover.getForeground();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                cover.setAlpha((int) animation.getAnimatedValue());
            }
        });
        animation.start();
    }

    private void coverFadeOut()
    {
        ValueAnimator animation = new ValueAnimator().ofInt(150, 0);
        animation.setDuration(mShortAnimationDuration);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            Drawable cover = mListCover.getForeground();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                cover.setAlpha((int) animation.getAnimatedValue());
            }
        });
        animation.start();
    }


}
