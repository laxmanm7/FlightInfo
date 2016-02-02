package com.ywsggip.flightinfo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private final String ORIGIN = "origin";
    private final String DESTINATION = "destination";
    private final String IATA_CODE = "IATA";

    private final int REQUEST_ORIGIN_CODE = 101;
    private final int REQUEST_DESTINATION_CODE = 102;

    private String origin;
    private String destination;
    private String date;

    private String DESTINATION_IATA_CODE = "";
    private String ORIGIN_IATA_CODE = "";

    private String SALE_COUNTRY = "";

    DatePicker datePicker;

    EditText editOrigin;
    EditText editDestination;


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(SettingsActivity.KEY_PREF_CURRENCY)) {
            SALE_COUNTRY = sharedPreferences.getString(key, getString(R.string.default_sale_country));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        datePicker = (DatePicker) findViewById(R.id.datePicker);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        //datePicker.setCalendarViewShown(false);
        //datePicker.setMinDate(System.currentTimeMillis());

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
                if(!hasInternetConnection()) {
                    Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                }
                else if(DESTINATION_IATA_CODE.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No destination airport chosen", Toast.LENGTH_SHORT).show();
                }
                else if(ORIGIN_IATA_CODE.isEmpty()){
                    Toast.makeText(MainActivity.this, "No origin airport chosen", Toast.LENGTH_SHORT).show();
                }
                else if(!isChosenDateCorrect()) {
                    Toast.makeText(MainActivity.this, "Invalid date chosen", Toast.LENGTH_SHORT).show();
                }
                else {
                    infoTask.execute(param);
                }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }
        });
    }


    private boolean hasInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return isConnected;
    }

    private String getReadableDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formated = dateFormat.format(date).toString();
        return formated;
    }

    private long getTodayTime(){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long time = today.getTimeInMillis() - (24*60*60*100);
        return time;
    }

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

        @Override
        protected void onPreExecute() {
            //progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.setTitle(R.string.please_wait);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {

            JSONObject jsonRequest = new JSONObject();
            try {
                String DATE = getReadableDate(getChosenTime());

                JSONObject requestObject = new JSONObject();

                JSONObject originDestination = new JSONObject();
//////////////////////////////////////////////////////////////////////////////////////////
                originDestination.put("origin", ORIGIN_IATA_CODE);
                originDestination.put("destination", DESTINATION_IATA_CODE);
//////////////////////////////////////////////////////////////////////////////////////////
//                originDestination.put("origin", "GDN");
//                originDestination.put("destination", "OSL");
                originDestination.put("date", DATE);
                JSONArray slice = new JSONArray();
                slice.put(originDestination);
                requestObject.put("slice", slice);

                JSONObject passengers = new JSONObject();
                passengers.put("adultCount", 1);
                requestObject.put("passengers", passengers);

                requestObject.put("refundable", false);
                requestObject.put("saleCountry", SALE_COUNTRY);
                requestObject.put("solutions", 7);

                jsonRequest.put("request", requestObject);

                String url = "https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyBg7GwjlxwbKZU7cVSIS3NnzrtjuKXPDNc";

                String json = jsonRequest.toString();

                StringEntity stringEntity = new StringEntity(json);

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(stringEntity);

                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                output = getResources().getString(R.string.example);
////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////////////
                HttpResponse httpResponse = httpclient.execute(httpPost);

                InputStream inputStream = httpResponse.getEntity().getContent();
                if(inputStream != null){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                        output += line;
                    inputStream.close();
                }
                else {
                    output = "something went wrong";
                }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
            progressDialog.dismiss();
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
