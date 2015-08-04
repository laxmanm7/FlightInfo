package com.ywsggip.flightinfo.data;

/**
 * Created by Admin on 2015-07-29.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ywsggip.flightinfo.R;
import com.ywsggip.flightinfo.data.AirportsContract.AirportEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AirportsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "airports.db";
    Context mContext;

    public AirportsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        final String SQL_CREATE_AIRPORT_TABLE =
               "CREATE TABLE " + AirportEntry.TABLE_NAME + " (" +
                       AirportEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                       AirportEntry.COLUMN_IATA_CODE + " TEXT NOT NULL, " +
                       AirportEntry.COLUMN_AIRPORT_NAME + " TEXT NOT NULL, " +
                       AirportEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                       AirportEntry.COLUMN_COUNTRY_NAME + " TEXT NOT NULL);";

        database.execSQL(SQL_CREATE_AIRPORT_TABLE);
        //database.execSQL("CREATE TABLE wisdom (myId int );");
        //SQLiteDatabase database = getWritableDatabase();

        ArrayList<ContentValues> airports = new ArrayList<>();
        InputStream airportsData = mContext.getResources().openRawResource(R.raw.airports);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(airportsData));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null)
            {
                ContentValues airport = new ContentValues();

                String[] airportArray = line.split(",");
                String IATACode = airportArray[4].replace("\"", "");
                String AirportName = airportArray[1].replace("\"", "");
                String Country = airportArray[3].replace("\"", "");
                String City = airportArray[2].replace("\"", "");

                // IATA code is required for request
                if(IATACode.isEmpty()) {
                    continue;
                }
                else {
                    airport.put(AirportEntry.COLUMN_IATA_CODE, IATACode);
                    airport.put(AirportEntry.COLUMN_AIRPORT_NAME, AirportName);
                    airport.put(AirportEntry.COLUMN_CITY_NAME, City);
                    airport.put(AirportEntry.COLUMN_COUNTRY_NAME, Country);
                    airports.add(airport);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            database.beginTransaction();
            for( ContentValues value : airports) {
                long _id = database.insert(AirportEntry.TABLE_NAME, null, value);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AirportEntry.TABLE_NAME);
        onCreate(db);
    }
}
