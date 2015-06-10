package com.ywsggip.flightinfo;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class AirportActivity extends ActionBarActivity {


    private final String LOG_TAG = AirportActivity.class.getSimpleName();
    private String action;
    private ArrayAdapter<String> mAirportsAdapter;

    private String IATA_CODE;

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
                String AirportName = airportArray[1].replace("\"", "");;
                String Country = airportArray[3].replace("\"", "");
                String City = airportArray[2].replace("\"", "");;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport);

        mAirportsAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.list_item_airport,
                        R.id.list_item_airport_textview,
                        new ArrayList<String>());

        ListView listView = (ListView) findViewById(R.id.listview_airports);
        listView.setAdapter(mAirportsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String airport = mAirportsAdapter.getItem(position);
                IATA_CODE = getIATACode(airport);
                Intent intent = new Intent();
                intent.putExtra(action, airport);
                intent.putExtra("IATA", IATA_CODE);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //init searchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        Intent intent = getIntent();

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            List<String> airportsList = searchForAirports(query);
            //Log.d(LOG_TAG, "Query is: " + query);
            mAirportsAdapter.clear();
            mAirportsAdapter.addAll(airportsList);
        } else {

            action = intent.getStringExtra("action");
            final TextView pointText = (TextView) findViewById(R.id.pointTextView);
            if(action.equals("origin")) {
                pointText.setText(R.string.origin_point);
            }
            else if (action.equals("destination")) {
                pointText.setText(R.string.destination_point);
            }
        }

//        Button acceptButton = (Button) findViewById(R.id.accept_button);
//        acceptButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditText originEditText = (EditText) findViewById(R.id.airport_code);
//
//                Intent intent = new Intent();
//                intent.putExtra(action, originEditText.getText().toString());
//                setResult(RESULT_OK, intent);
//                finish();
//            }
//        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, "fasdfad", Toast.LENGTH_LONG);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            List<String> airportsList = searchForAirports(query);
            Log.d(LOG_TAG, "Query is: " + query);
            mAirportsAdapter.clear();
            mAirportsAdapter.addAll(airportsList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_airport, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
