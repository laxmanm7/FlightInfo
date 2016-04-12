package com.ywsggip.flightinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.MatrixCursor;

import com.ywsggip.flightinfo.data.AirportsContract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Admin on 2016-01-04.
 */
class Airport
{
    public String getIATACode() {
        return this.mIATACode;
    }

    public String getName() {
        return this.mAirportName;
    }

    public String getCity() {
        return this.mCity;
    }

    public String getCountry() {
        return this.mCountry;
    }

    private String mIATACode;
    private String mAirportName;
    private String mCity;
    private String mCountry;
    public Airport(String IATACode, String airportName, String city, String country)
    {
        mIATACode = IATACode;
        mAirportName = airportName;
        mCity = city;
        mCountry = country;
    }
    public Airport(String bundle)
    {
        String[] airportData = bundle.split(",");
        mIATACode = airportData[0];
        mAirportName = airportData[1];
        mCity = airportData[2];
        mCountry = airportData[3];
    }

    @Override
    public String toString() {
        return mIATACode + "," + mAirportName + "," + mCity + "," + mCountry;
    }
}

public class RecentAirports {
    private final int mRecentNumber = 5;
    private MatrixCursor mCursor;
    private SharedPreferences mSharedPref;
    private static final String[] AIRPORT_COLUMNS = {
            AirportsContract.AirportEntry._ID,
            AirportsContract.AirportEntry.COLUMN_IATA_CODE,
            AirportsContract.AirportEntry.COLUMN_AIRPORT_NAME,
            AirportsContract.AirportEntry.COLUMN_CITY_NAME,
            AirportsContract.AirportEntry.COLUMN_COUNTRY_NAME
    };

    ArrayList<Airport> mAirports;

    MatrixCursor getCursor(){
        return mCursor;
    }

    void addAirport(String IATACode, String AirportName, String City, String Country)
    {
        if(mAirports.size() > 0)
        {
            Iterator<Airport> iterator = mAirports.iterator();
            while(iterator.hasNext())
            {
                if(iterator.next().getIATACode().equals(IATACode))
                {
                    iterator.remove();
                    break;
                }
            }
        }

        mAirports.add(new Airport(IATACode, AirportName, City, Country));
        if(mAirports.size() > mRecentNumber)
        {
            mAirports.remove(0);
        }

    }

    public void save()
    {
        SharedPreferences.Editor editor = mSharedPref.edit();
        for(int i = 0; i < mAirports.size(); ++i)
        {
            editor.putString(String.valueOf(i), mAirports.get(i).toString());
        }
        editor.commit();
    }

    public RecentAirports(Context context)
    {
        mSharedPref = context.getSharedPreferences("RecentAirports", Context.MODE_PRIVATE);
        mAirports = new ArrayList<>(mRecentNumber);
        int i = 0;
        while(i < mRecentNumber)
        {
            String recentAirportBundle = mSharedPref.getString(String.valueOf(i), "DEFAULT");
            if(!recentAirportBundle.equals("DEFAULT"))
            {
                Airport recent = new Airport(recentAirportBundle);
                mAirports.add(recent);
            }
            else
            {
                break;
            }
            ++i;
        }

        if(mAirports.size() >= 1)
        {
            mCursor = new MatrixCursor(AIRPORT_COLUMNS, 1);
            for(i -= 1; i >= 0; --i)
            {
                MatrixCursor.RowBuilder rowBuilder = mCursor.newRow();
                rowBuilder.add(i);
                rowBuilder.add(mAirports.get(i).getIATACode());
                rowBuilder.add(mAirports.get(i).getName());
                rowBuilder.add(mAirports.get(i).getCity());
                rowBuilder.add(mAirports.get(i).getCountry());
            }
        }

    }
}
