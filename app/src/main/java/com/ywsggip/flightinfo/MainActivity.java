package com.ywsggip.flightinfo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private final String ORIGIN = "origin";
    private final String DESTINATION = "destination";
    private final String IATA_CODE = "IATA";

    private final int REQUEST_ORIGIN_CODE = 101;
    private final int REQUEST_DESTINATION_CODE = 102;

    private String origin;
    private String destination;
    private static String departDate;
    private static long departDateInMillis;
    private static String returnDate;
    private static long returnDateInMillis;

    private static final long ONE_DAY_IN_MILLIS = 24*60*60*1000;

    private String DESTINATION_IATA_CODE = "";
    private String ORIGIN_IATA_CODE = "";

    private String SALE_COUNTRY = "";

    DatePicker datePicker;

    EditText editOrigin;
    EditText editDestination;

    private boolean withReturn = false;
    private static boolean settingReturnDate = false;
    View    pickReturnDate;

    private SharedPreferences mPref;

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        static SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE dd MMM yyyy");

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            if(!MainActivity.settingReturnDate)
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            else
                datePickerDialog.getDatePicker().setMinDate(MainActivity.departDateInMillis);

            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            long timeInMillis = view.getCalendarView().getDate();
            setDateFields(timeInMillis);
        }

        private void setDateFields(long timeInMillis){
            Date date = new Date(timeInMillis);

            String formattedDate;
            if(timeInMillis > getTodayTime() && timeInMillis < getTomorrowTime()) {
                formattedDate = "Today";
            }
            else if(timeInMillis < getDayAfterTomorrowTime()) {
                formattedDate = "Tomorrow";
            }
            else {
                formattedDate = (viewDateFormat.format(date));
            }

            boolean updateReturnDate = false;
            if(!MainActivity.settingReturnDate) {
                departDateInMillis = timeInMillis;
                if(departDateInMillis > returnDateInMillis){
                    updateReturnDate = true;
                }

                    //updateReturnDateFields(departDateInMillis);
                MainActivity.departDate = MainActivity.getQueryDate(timeInMillis);
                EditText departDate = (EditText)getActivity().findViewById(R.id.pick_depart_date);
                departDate.setText(formattedDate);
;
            }
            if(MainActivity.settingReturnDate || updateReturnDate){ //setting return date
                returnDateInMillis = timeInMillis;
                MainActivity.returnDate = MainActivity.getQueryDate(timeInMillis);
                EditText returnDate = (EditText)getActivity().findViewById(R.id.pick_return_date);
                returnDate.setText(formattedDate);
            }
        }

        private void updateReturnDateFields(long timeInMillis) {
            Date date = new Date(timeInMillis);
            String formattedDate = (viewDateFormat.format(date));
            returnDateInMillis = timeInMillis;
            MainActivity.returnDate = MainActivity.getQueryDate(timeInMillis);
            EditText returnDate = (EditText)getActivity().findViewById(R.id.pick_return_date);
            returnDate.setText(formattedDate);
        }

    }

    public void showDepartDatePickerDialog(View v) {
        settingReturnDate = false;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showReturnDatePickerDialog(View v) {
        settingReturnDate = true;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(SettingsActivity.KEY_PREF_CURRENCY)) {
            SALE_COUNTRY = sharedPreferences.getString(key, getString(R.string.default_sale_country));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor ed = mPref.edit();
        ed.putString("origin", editOrigin.getText().toString());
        ed.putString("destination", editDestination.getText().toString());
        ed.putString("destinationIATA", DESTINATION_IATA_CODE);
        ed.putString("originIATA", ORIGIN_IATA_CODE);
        ed.putBoolean("withReturn", withReturn);
        ed.putLong("departDateInMillis", departDateInMillis);
        ed.putLong("returnDateInMillis", returnDateInMillis);
        ed.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        departDateInMillis = System.currentTimeMillis();
        departDate = getQueryDate(departDateInMillis); //setup todays departDate

        pickReturnDate = findViewById(R.id.pick_return_date);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.addTab(tabLayout.newTab().setText("One way"));
        tabLayout.addTab(tabLayout.newTab().setText("And back"));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().toString().toLowerCase().equals("and back")) {
                    withReturn = true;
                    TextView returnText = (TextView) findViewById(R.id.returnTextView);
                    returnText.setEnabled(true);
                    pickReturnDate.setEnabled(true);
                } else if (tab.getText().toString().toLowerCase().equals("one way")) {
                    withReturn = false;
                    TextView returnText = (TextView) findViewById(R.id.returnTextView);
                    returnText.setEnabled(false);
                    pickReturnDate.setEnabled(false);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        SALE_COUNTRY = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_PREF_CURRENCY, "");

        editOrigin = (EditText) findViewById(R.id.editOrigin);
        editOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AirportActivity.class);
                intent.putExtra("action", ORIGIN);
                startActivityForResult(intent, REQUEST_ORIGIN_CODE);
            }
        });

        editDestination = (EditText) findViewById(R.id.editDestination);
        editDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AirportActivity.class);
                intent.putExtra("action", DESTINATION);
                startActivityForResult(intent, REQUEST_DESTINATION_CODE);
            }
        });


        ImageButton swapButton = (ImageButton) findViewById(R.id.swapButton);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String originString = editOrigin.getText().toString();
                String destinationString = editDestination.getText().toString();

                editOrigin.setText(destinationString);
                editDestination.setText(originString);

                String tempIATA = ORIGIN_IATA_CODE;
                ORIGIN_IATA_CODE = DESTINATION_IATA_CODE;
                DESTINATION_IATA_CODE = tempIATA;
            }
        });

        Button sendRequestButton = (Button) findViewById(R.id.sendRequestButton);
        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String param = "gafds";
                FetchInfoTask infoTask = new FetchInfoTask();
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                if (!hasInternetConnection()) {
                    Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                } else if (DESTINATION_IATA_CODE.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No destination airport chosen", Toast.LENGTH_SHORT).show();
                } else if (ORIGIN_IATA_CODE.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No origin airport chosen", Toast.LENGTH_SHORT).show();
                }
//                else if (!isChosenDateCorrect()) {
//                    Toast.makeText(MainActivity.this, "Invalid date chosen", Toast.LENGTH_SHORT).show();
//                }
                else {
                    infoTask.execute(param);
                }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }
        });


        mPref = getPreferences(Context.MODE_PRIVATE);
        editOrigin.setText(mPref.getString("origin", ""));
        editDestination.setText(mPref.getString("destination", ""));
        ORIGIN_IATA_CODE = mPref.getString("originIATA", "");
        DESTINATION_IATA_CODE = mPref.getString("destinationIATA", "");
        withReturn = mPref.getBoolean("withReturn", false);
        if(withReturn) {
            TabLayout.Tab t = tabLayout.getTabAt(1);
            t.select();
        }
        departDateInMillis = mPref.getLong("departDateInMillis", System.currentTimeMillis());
        returnDateInMillis = mPref.getLong("returnDateInMillis", departDateInMillis);
        //DatePickerFragment.set
        updateDepartDate(departDateInMillis);
        updateReturnDate(returnDateInMillis);
    }


    private void updateDepartDate(long timeInMillis){
        String formattedDate = getFormattedDate(timeInMillis);
        MainActivity.departDate = MainActivity.getQueryDate(timeInMillis);
        EditText departDate = (EditText) findViewById(R.id.pick_depart_date);
        departDate.setText(formattedDate);
    }


    private void updateReturnDate(long timeInMillis) {
        String formattedDate = getFormattedDate(timeInMillis);
        MainActivity.returnDate = MainActivity.getQueryDate(timeInMillis);
        EditText returnDate = (EditText) findViewById(R.id.pick_return_date);
        returnDate.setText(formattedDate);
    }

    private String getFormattedDate(long timeInMillis) {
        SimpleDateFormat viewDateFormat = new SimpleDateFormat("EEE dd MMM yyyy");
        Date date = new Date(timeInMillis);
        String formattedDate;
        if(timeInMillis < getTomorrowTime()) {
            formattedDate = "Today";
        }
        else if(timeInMillis < getDayAfterTomorrowTime()) {
            formattedDate = "Tomorrow";
        }
        else {
            formattedDate = (viewDateFormat.format(date));
        }
        return formattedDate;
    }
    private boolean hasInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return isConnected;
    }

    private static String getQueryDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formated = dateFormat.format(date).toString();
        return formated;
    }

    private static long getTodayTime(){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long time = today.getTimeInMillis();// - (24*60*60*100);
        return time;
    }

    private static long getTomorrowTime(){
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        long time = tomorrow.getTimeInMillis() + (ONE_DAY_IN_MILLIS);
        return time;
    }

    private static long getDayAfterTomorrowTime(){
        Calendar day = Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        long time = day.getTimeInMillis() + (2*ONE_DAY_IN_MILLIS);
        return time;
    }

    @Deprecated
    private long getChosenTime() {
        return datePicker.getCalendarView().getDate();
    }

    private boolean isChosenDateCorrect() {
        long timeSince1970 = getChosenTime();
        long currentTime = getTodayTime();
        if(timeSince1970 < currentTime) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ORIGIN_CODE && resultCode == RESULT_OK) {
            String origin = data.getStringExtra(ORIGIN);
            ORIGIN_IATA_CODE = data.getStringExtra(IATA_CODE);
            EditText editOrigin = (EditText) findViewById(R.id.editOrigin);
            editOrigin.setText(origin);

        }
        else if (requestCode == REQUEST_DESTINATION_CODE && resultCode == RESULT_OK) {
            String destination = data.getStringExtra(DESTINATION);
            DESTINATION_IATA_CODE = data.getStringExtra(IATA_CODE);
            EditText editOrigin = (EditText) findViewById(R.id.editDestination);
            editOrigin.setText(destination);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchInfoTask extends AsyncTask <String, Void, Void> {

        String output = "";
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        MaterialDialog materialProgressDialog;
        @Override
        protected void onPreExecute() {
            //progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);

//            progressDialog.setTitle(R.string.please_wait);
//            progressDialog.show();
           materialProgressDialog = new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.please_wait)
                    .content("Searching...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {

            JSONObject jsonRequest = new JSONObject();
            try {
                //String DATE = getQueryDate(getChosenTime());

                JSONObject requestObject = new JSONObject();

                JSONObject originDestination = new JSONObject();
//////////////////////////////////////////////////////////////////////////////////////////
                originDestination.put("origin", ORIGIN_IATA_CODE);
                originDestination.put("destination", DESTINATION_IATA_CODE);
//////////////////////////////////////////////////////////////////////////////////////////
//                originDestination.put("origin", "GDN");
//                originDestination.put("destination", "OSL");
                originDestination.put("date", departDate);
                JSONArray slice = new JSONArray();
                slice.put(originDestination);
                if(withReturn) {
                    JSONObject returnOriginDestination = new JSONObject();
                    returnOriginDestination.put("origin", DESTINATION_IATA_CODE);
                    returnOriginDestination.put("destination", ORIGIN_IATA_CODE);
                    returnOriginDestination.put("date", returnDate);
                    slice.put(returnOriginDestination);
                }


                requestObject.put("slice", slice);

                JSONObject passengers = new JSONObject();
                passengers.put("adultCount", 1);
                requestObject.put("passengers", passengers);

                requestObject.put("refundable", false);
                requestObject.put("saleCountry", SALE_COUNTRY);
                requestObject.put("solutions", 8);

                jsonRequest.put("request", requestObject);

                String url = "https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyBg7GwjlxwbKZU7cVSIS3NnzrtjuKXPDNc";

                String json = jsonRequest.toString();
                URL urlAddress = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection)urlAddress.openConnection();
                try {

                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setChunkedStreamingMode(0);

                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    OutputStream outputStream = urlConnection.getOutputStream();
                    outputStream.write(json.getBytes());
                    outputStream.flush();
                    outputStream.close();



                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = bufferedReader.readLine()) != null)
                            output += line;
                        inputStream.close();
                    } else {
                        output = "something went wrong";
                    }
                }
                finally {
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //TextView resultText = (TextView) findViewById(R.id.serverAnswer);
            //resultText.setText(output);
            //progressDialog.dismiss();
            materialProgressDialog.dismiss();

            Intent intent = new Intent(MainActivity.this, ResultsActivity.class );
            intent.putExtra("json", output);
            intent.putExtra("originIATA", ORIGIN_IATA_CODE);
            intent.putExtra("destinationIATA", DESTINATION_IATA_CODE);
            startActivity(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}

