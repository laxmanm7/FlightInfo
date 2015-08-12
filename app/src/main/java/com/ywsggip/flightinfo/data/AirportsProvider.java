package com.ywsggip.flightinfo.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Admin on 2015-07-31.
 */
public class AirportsProvider extends ContentProvider {

    private static final int AIRPORT_WITH_ANY_MATCH = 100;
    private static final int AIRPORT_WITH_COUNTRY = 101;
    private static final int AIRPORT_WITH_FILTER = 200;

    private static UriMatcher sUriMatcher = buildUriMatcher();

    private AirportsDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AirportsContract.CONTENT_AUTHORITY, AirportsContract.PATH_AIRPORT, AIRPORT_WITH_ANY_MATCH);
        uriMatcher.addURI(AirportsContract.CONTENT_AUTHORITY, AirportsContract.PATH_AIRPORT +
                "/" + AirportsContract.AirportEntry.COLUMN_COUNTRY_NAME + "/*", AIRPORT_WITH_COUNTRY);

        uriMatcher.addURI(AirportsContract.CONTENT_AUTHORITY, AirportsContract.PATH_AIRPORT +
                "/" + AirportsContract.PATH_FILTER + "/*", AIRPORT_WITH_FILTER);

        return uriMatcher;
    }

    private static final String sAirportNameSelection =
            AirportsContract.AirportEntry.TABLE_NAME +
                    "." + AirportsContract.AirportEntry.COLUMN_AIRPORT_NAME + " = ?";

    private static final String sAirportCountrySelection =
            AirportsContract.AirportEntry.TABLE_NAME +
                    "." + AirportsContract.AirportEntry.COLUMN_COUNTRY_NAME + " = ?";

    private static final String sAirportIATASelection =
            AirportsContract.AirportEntry.TABLE_NAME +
                    "." + AirportsContract.AirportEntry.COLUMN_IATA_CODE + " = ?";

    private static final String sAirportSelection =
            AirportsContract.AirportEntry.COLUMN_AIRPORT_NAME + " LIKE ? OR " +
                    AirportsContract.AirportEntry.COLUMN_COUNTRY_NAME + " LIKE ? OR " +
                    AirportsContract.AirportEntry.COLUMN_CITY_NAME + " LIKE ? OR " +
                    AirportsContract.AirportEntry.COLUMN_IATA_CODE + " LIKE ?";


    @Override
    public boolean onCreate() {
        mOpenHelper = new AirportsDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;

        switch(sUriMatcher.match(uri)) {
            case AIRPORT_WITH_COUNTRY: {
                String country = AirportsContract.AirportEntry.getCountryFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AirportsContract.AirportEntry.TABLE_NAME,
                        projection,
                        sAirportCountrySelection,
                        new String[] {country},
                        null,
                        null,
                        sortOrder
                );
            }
                break;
            case AIRPORT_WITH_FILTER: {
                String filter =  /*"%" +*/ AirportsContract.AirportEntry.getFilterFromUri(uri) + "%";
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AirportsContract.AirportEntry.TABLE_NAME,
                        projection,
                        sAirportSelection,
                        new String[] {filter, filter, filter, filter},
                        null,
                        null,
                        sortOrder
                );
            }
                break;
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case AIRPORT_WITH_COUNTRY:
                return AirportsContract.AirportEntry.CONTENT_TYPE;
            case AIRPORT_WITH_FILTER:
                return AirportsContract.AirportEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Couldn't resolve uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
