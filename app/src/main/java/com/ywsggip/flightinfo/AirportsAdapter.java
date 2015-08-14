package com.ywsggip.flightinfo;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ywsggip.flightinfo.data.AirportsContract;

/**
 * Created by Admin on 2015-08-13.
 */
public class AirportsAdapter extends CursorAdapter {

    private static final String LOG_TAG = AirportsAdapter.class.getSimpleName();
    private String queryString;
    private AirportsAdapter mAirportsAdapter;
    public AirportsAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    public void setQuery(String query) {
        queryString = query;
    }

    private Spannable highlightQuery(String text) {

        if(text.toLowerCase().startsWith(queryString.toLowerCase())) {
            Spannable spannable = new SpannableString(text);

            ColorStateList blueColor = new ColorStateList(new int[][] { new int[] {}}, new int[] { Color.rgb(0, 153, 213) });
            TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.NORMAL, -1, blueColor, null);

            spannable.setSpan(highlightSpan, 0, queryString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return spannable;
        }
        return null;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        //Log.v(LOG_TAG, "Query is " + queryString);
        return super.swapCursor(newCursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_airport_2, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

//        Bundle bundle = cursor.getExtras();
//        String query = bundle.getString("query");
        Spannable spannable;


        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String IATAString = cursor.getString(AirportActivity.COL_AIRPORT_IATA);
        spannable = highlightQuery(IATAString);
        if( null != spannable) {
            viewHolder.IATAView.setText(spannable);
        }
        else {
            viewHolder.IATAView.setText(IATAString);
        }


        String nameString = cursor.getString(AirportActivity.COL_AIRPORT_NAME);
        spannable = highlightQuery(nameString);
        if( null != spannable) {
            viewHolder.nameView.setText(spannable);
        }
        else {
            viewHolder.nameView.setText(nameString);
        }

        String cityString = cursor.getString(AirportActivity.COL_AIRPORT_CITY);
        spannable = highlightQuery(cityString);
        if( null != spannable) {
            viewHolder.cityView.setText(spannable);
        }
        else {
            viewHolder.cityView.setText(cityString);
        }

        String countryString = cursor.getString(AirportActivity.COL_AIRPORT_COUNTRY);
        spannable = highlightQuery(countryString);
        if( null != spannable) {
            viewHolder.countryView.setText(spannable);
        }
        else {
            viewHolder.countryView.setText(countryString);
        }
    }

    public static class ViewHolder {

        public final TextView IATAView;
        public final TextView nameView;
        public final TextView cityView;
        public final TextView countryView;

        public ViewHolder(View view) {
            IATAView = (TextView) view.findViewById(R.id.list_item_iata_textview);
            nameView = (TextView) view.findViewById(R.id.list_item_name_textview);
            cityView = (TextView) view.findViewById(R.id.list_item_city_textview);
            countryView= (TextView) view.findViewById(R.id.list_item_country_textview);

        }
    }
}