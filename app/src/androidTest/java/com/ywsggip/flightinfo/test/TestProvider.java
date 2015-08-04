package com.ywsggip.flightinfo.test;

import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.ywsggip.flightinfo.data.AirportsContract.AirportEntry;

/**
 * Created by Admin on 2015-08-01.
 */
public class TestProvider extends AndroidTestCase {

    private final String LOG_TAG = TestProvider.class.getSimpleName();

    private final String TEST_COUNTRY_NAME = "Poland";
    private final String TEST_FILTER = "aux";

    public void testGetType() {
        String type = mContext.getContentResolver().getType(AirportEntry.buildAirportWithCountry(TEST_COUNTRY_NAME));

        assertEquals(AirportEntry.CONTENT_TYPE, type);
    }

    public void testReadProvider() {


        Cursor cursor = mContext.getContentResolver().query(
                AirportEntry.buildAirportWithCountry(TEST_COUNTRY_NAME),
                null,
                null,
                null,
                null
        );

//        cursor = mContext.getContentResolver().query(
//                Uri.parse("content://com.ywsggip.flightinfo/airports/country/Poland"),
//                null,null,null,null
//        );

        if(cursor == null){
            Log.e(LOG_TAG, "cursor is null");
        }

        if(cursor.moveToFirst()) {
            Log.d(LOG_TAG, "coursor count: " + cursor.getCount());
            //there are 18 airports in data file that are in Poland
            assertEquals(18, cursor.getCount());
        }

        cursor.close();

        cursor = mContext.getContentResolver().query(
                AirportEntry.buildAirportWithFilter(TEST_FILTER),
                null,
                null,
                null,
                null
        );

        //assertNull(cursor);
        if(cursor == null){
            Log.e(LOG_TAG, "cursor aux is null");
        }
        if(cursor.moveToFirst()) {
            Log.d(LOG_TAG, "coursor count: " + cursor.getCount());
            assertEquals(4, cursor.getCount());

            cursor.moveToPosition(-1);
            while(cursor.moveToNext()) {
                int IATAindex = cursor.getColumnIndex(AirportEntry.COLUMN_IATA_CODE);
                int nameIndex  =cursor.getColumnIndex(AirportEntry.COLUMN_AIRPORT_NAME);
                String IATA = cursor.getString(IATAindex);
                String name = cursor.getString(nameIndex);

                String info = "[" + IATA + "]" + " " + name;

                Log.d(LOG_TAG, info);

            }

        }

        cursor.close();
    }
}
