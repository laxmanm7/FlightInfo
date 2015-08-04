package com.ywsggip.flightinfo.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Airport contract
 */
public class AirportsContract {

    public static final String CONTENT_AUTHORITY = "com.ywsggip.flightinfo";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_AIRPORT = "airport";
    public static final String PATH_FILTER = "filter";

    public static final class AirportEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_AIRPORT).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_AIRPORT;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_AIRPORT;

        public static final String TABLE_NAME = "airports";
        public static final String COLUMN_IATA_CODE = "iata";
        public static final String COLUMN_AIRPORT_NAME = "airport_name";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_COUNTRY_NAME = "country_name";



        public static Uri buildAirportUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildAirportIATA(String IATA) {
            return CONTENT_URI.buildUpon().appendPath(IATA).build();
        }

        public static Uri buildAirportWithCity(String city) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_CITY_NAME, city).build();
        }

        public static Uri buildAirportWithCountry(String country) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_COUNTRY_NAME).appendPath(country).build();
            //return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_COUNTRY_NAME, country).build();
        }

        public static Uri buildAirportWithFilter(String filter) {
            return CONTENT_URI.buildUpon().appendPath(PATH_FILTER).appendPath(filter).build();
        }

        public static String getIATAFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getCountryFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
            //return uri.getQueryParameter(COLUMN_COUNTRY_NAME);
        }

        public static String getCityFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_CITY_NAME);
        }

        public static String getFilterFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }
}
