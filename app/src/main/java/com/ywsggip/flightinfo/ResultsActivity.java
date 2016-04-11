package com.ywsggip.flightinfo;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 2015-06-09.
 */
public class ResultsActivity extends ActionBarActivity {

    private final String LOG_TAG = ResultsActivity.class.getSimpleName();
    private String DESTINATION_IATA_CODE;
    private String ORIGIN_IATA_CODE;

    SparseArray<Group> groups;// = new SparseArray<>();
    Map<String, String> carriers = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        String jsonData = intent.getStringExtra("json");
        ORIGIN_IATA_CODE = intent.getStringExtra("originIATA");
        DESTINATION_IATA_CODE = intent.getStringExtra("destinationIATA");

        try {
            groups = extractDataFromJson(jsonData);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        ResultsExpandableListAdapter adapter = new ResultsExpandableListAdapter(this, groups);
        expandableListView.setAdapter(adapter);
        expandableListView.setEmptyView(findViewById(R.id.empty));
    }

    String minutesToStringTime(int min) {
        int hours = min / 60;
        int minutes = min % 60;
        String result = "0h";
        if(hours > 0) {
            result = hours + "h";
        }
        if(minutes > 0) {
            result += " " + minutes + "m";
        }
        return result;
    }

    int firstNumberInString(String string) {
        if(Character.isDigit(string.charAt(0))) {
            return 0;
        }
        int i = 1;
        while (!Character.isDigit(string.charAt(i))) {
            i++;
        }
        if(i > 0)
            return i;
        else
            return -1;
    }

    private SparseArray<Group> extractDataFromJson(String json) throws JSONException {
        SparseArray<Group> result = new SparseArray<>();
        JSONObject request = new JSONObject(json);
        JSONObject data = request.getJSONObject("trips").getJSONObject("data");

        boolean carrierInfo;
        try{
            JSONArray carriers = data.getJSONArray("carrier");
            for(int i = 0; i < carriers.length(); ++i) {
                JSONObject carrier = carriers.getJSONObject(i);
                String code = carrier.getString("code");
                String name = carrier.getString("name");
                Log.d(LOG_TAG, code + " " + name);
                ResultsActivity.this.carriers.put(code, name);
            }
            carrierInfo = true;
        }
        catch (Exception e) {
            carrierInfo = false;
        }



        JSONArray tripOptions = request.getJSONObject("trips").getJSONArray("tripOption");
        for(int i = 0; i < tripOptions.length(); ++i) {
            JSONObject tripOption = tripOptions.getJSONObject(i);

            String departure = ORIGIN_IATA_CODE;
            String arrival = DESTINATION_IATA_CODE;
            String travelTime = minutesToStringTime(tripOption.getJSONArray("slice").getJSONObject(0).getInt("duration"));
            String price = tripOption.getString("saleTotal");
            String value = price.substring(firstNumberInString(price), price.length());
            String current = price.substring(0, firstNumberInString(price));
            price = value + " " + current;
            Group group = new Group(departure, arrival, travelTime, price);

            JSONArray segments = tripOption.getJSONArray("slice").getJSONObject(0).getJSONArray("segment");
            for(int j = 0; j < segments.length(); ++j) {
                JSONObject segment = segments.getJSONObject(j);
                String carrier;
                if(carrierInfo) {
                    carrier = ResultsActivity.this.carriers.get(segment.getJSONObject("flight").getString("carrier"));
                } else {
                    carrier = "No carrier data";
                }


                JSONObject detailInfo = segment.getJSONArray("leg").getJSONObject(0);

                String departureTime = detailInfo.getString("departureTime");
                String arrivalTime = detailInfo.getString("arrivalTime");

                String departureDetail = detailInfo.getString("origin") + " "
                        + departureTime.substring(departureTime.indexOf("T")+1, departureTime.indexOf("+")) + " "
                        + departureTime.substring(departureTime.indexOf("-")+1, departureTime.indexOf("T"));

                String arriveDetail = detailInfo.getString("destination") + " "
                        + arrivalTime.substring(arrivalTime.indexOf("T")+1, arrivalTime.indexOf("+")) + " "
                        + arrivalTime.substring(arrivalTime.indexOf("-")+1, arrivalTime.indexOf("T"));

                String detailTime = minutesToStringTime(detailInfo.getInt("duration"));

                Details details = new Details(carrier, departureDetail, arriveDetail, detailTime);
                group.detailsList.add(details);
            }
            result.put(i, group);
        }

        return result;
    }

    private class ResultsExpandableListAdapter extends BaseExpandableListAdapter {

        private final SparseArray<Group> groups;
        public LayoutInflater inflater;
        public Activity activity;

        public ResultsExpandableListAdapter(Activity activity, SparseArray<Group> groups) {
            this.activity = activity;
            this.groups = groups;
            inflater = activity.getLayoutInflater();
        }

        @Override
        public int getGroupCount() {
            if(groups != null)
                return groups.size();
            else
                return 0;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groups.get(groupPosition).detailsList.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groups.get(groupPosition).detailsList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.list_group_results, null);
            }
            Group group = (Group) getGroup(groupPosition);

            TextView departure = (TextView) convertView.findViewById(R.id.departure_textView);
            TextView arrival = (TextView) convertView.findViewById(R.id.arrival_textView);
            TextView travelTime = (TextView) convertView.findViewById(R.id.travel_time_textView);
            TextView price = (TextView) convertView.findViewById(R.id.price_textView);

            departure.setText(group.departure);
            arrival.setText(group.arrival);
            travelTime.setText(group.travelTime);
            price.setText(group.price);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.list_details_results, null);
            }
            TextView departureDetail = (TextView) convertView.findViewById(R.id.departure_detail_textView);
            TextView arrivalDetail = (TextView) convertView.findViewById(R.id.arrival_detail_textView);
            TextView travelTimeDetail = (TextView) convertView.findViewById(R.id.travel_time_detail_textView);
            TextView carrier = (TextView) convertView.findViewById(R.id.carrier_detail_textView);

            Details details = (Details) getChild(groupPosition, childPosition);

            departureDetail.setText(details.departureDetail);
            arrivalDetail.setText(details.arrivalDetail);
            travelTimeDetail.setText(details.detailTime);
            carrier.setText(details.carrier);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private class Group {
        public String departure;
        public String arrival;
        public String travelTime;
        public String price;

        public final List<Details> detailsList = new ArrayList<Details>();

        public Group(String departure, String arrival, String travelTime, String price) {
            this.departure = departure;
            this.arrival = arrival;
            this.travelTime = travelTime;
            this.price = price;
        }

    }

    private class Details {
        private String carrier;
        private String departureDetail;
        private String arrivalDetail;
        private String detailTime;

        public Details(String carrier, String departureDetail, String arrivalDetail, String detailTime) {
            this.carrier = carrier;
            this.departureDetail = departureDetail;
            this.arrivalDetail = arrivalDetail;
            this.detailTime = detailTime;
        }
    }
}
