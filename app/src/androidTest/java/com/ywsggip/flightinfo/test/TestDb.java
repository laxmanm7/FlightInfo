package com.ywsggip.flightinfo.test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.ywsggip.flightinfo.data.AirportsContract.AirportEntry;
import com.ywsggip.flightinfo.data.AirportsDbHelper;

/**
 * Created by Admin on 2015-07-29.
 */
public class TestDb extends AndroidTestCase {

    private final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(AirportsDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new AirportsDbHelper(this.mContext)
                .getReadableDatabase();

        assertEquals(true, db.isOpen());
        db.close();
    }

    static public String TEST_CITY_NAME = "Warsaw";

    public void testReadDb() {
        AirportsDbHelper dbHelper = new AirportsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                AirportEntry.TABLE_NAME,
                null,
                AirportEntry.COLUMN_CITY_NAME + " = ?",
                new String[] {TEST_CITY_NAME},
                null,
                null,
                null
        );

        if(cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(AirportEntry.COLUMN_CITY_NAME);
            assertFalse(-1 == index);
            String city = cursor.getString(index);
            assertEquals(TEST_CITY_NAME, cursor.getString(index));
            Log.d(LOG_TAG, "City name: " + city);

            int indexIATA = cursor.getColumnIndex(AirportEntry.COLUMN_IATA_CODE);
            assertFalse(-1 == indexIATA);
            String IATACode = cursor.getString(indexIATA);
            assertEquals("WAW", cursor.getString(indexIATA));
            Log.d(LOG_TAG, "Airport code: " + IATACode);
        }

    }


}
