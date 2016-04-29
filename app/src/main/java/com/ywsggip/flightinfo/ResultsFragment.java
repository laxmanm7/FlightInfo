package com.ywsggip.flightinfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 2016-04-14.
 */
public class ResultsFragment extends Fragment {

    OnResultSelectedListener mCallback;

    private final String LOG_TAG = ResultsFragment.class.getSimpleName();
    private String DESTINATION_IATA_CODE;
    private String ORIGIN_IATA_CODE;

    //SparseArray<Group> groups;// = new SparseArray<>();
    ArrayList<Group> groups;
    Map<String, String> carriers = new HashMap<String, String>();
    ListView expandableListView;

    boolean carrierInfo;
    boolean withReturn;

    public interface OnResultSelectedListener {
        void onResultSelected(ArrayList<TripDetails> data, ArrayList<TripDetails> returnData);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (OnResultSelectedListener)context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + "must implement OnResultSelectedListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mCallback = (OnResultSelectedListener)activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement OnResultSelectedListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        String jsonData = intent.getStringExtra("json");
        ORIGIN_IATA_CODE = intent.getStringExtra("originIATA");
        DESTINATION_IATA_CODE = intent.getStringExtra("destinationIATA");

        SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.flight_preferences_file_key), Context.MODE_PRIVATE);
        withReturn = preferences.getBoolean("withReturn", false);
        try {
            groups = extractDataFromJson(jsonData);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        expandableListView = (ListView) getView().findViewById(R.id.expandableListView);
        ResultsListAdapter adapter = new ResultsListAdapter(getActivity(), groups);
        expandableListView.setAdapter(adapter);
        TextView emptyView = (TextView)view.findViewById(R.id.empty);
        expandableListView.setEmptyView(getView().findViewById(R.id.empty));

       expandableListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Group group = groups.get(position);
               mCallback.onResultSelected(group.detailsList, group.returnDetailsList);
           }
       });
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_results, container, false);
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

    private ArrayList<Group> extractDataFromJson(String json) throws JSONException {
        ArrayList<Group> result = new ArrayList<>();
        JSONObject request = new JSONObject(json);
        JSONObject data = request.getJSONObject("trips").getJSONObject("data");


        try{
            JSONArray carriers = data.getJSONArray("carrier");
            for(int i = 0; i < carriers.length(); ++i) {
                JSONObject carrier = carriers.getJSONObject(i);
                String code = carrier.getString("code");
                String name = carrier.getString("name");
                Log.d(LOG_TAG, code + " " + name);
                ResultsFragment.this.carriers.put(code, name);
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


            JSONObject departSlice = tripOption.getJSONArray("slice").getJSONObject(0);
            ArrayList<TripDetails> departDetails = getDetailsFromSlice(departSlice);
            group.detailsList = departDetails;

            if(withReturn) {
                JSONObject returnSlice = tripOption.getJSONArray("slice").getJSONObject(1);
                ArrayList<TripDetails> returnDetails = getDetailsFromSlice(returnSlice);
                group.returnDetailsList = returnDetails;
            }

            result.add(group);
        }

        return result;
    }

    public ArrayList<TripDetails> getDetailsFromSlice(JSONObject slice) throws JSONException {
        ArrayList<TripDetails> result;
        JSONArray segments = slice.getJSONArray("segment");
        if(segments.length() == 0)
            return null;
        else
            result = new ArrayList<>();
        for(int j = 0; j < segments.length(); ++j) {
            JSONObject segment = segments.getJSONObject(j);
            String carrier;
            if(carrierInfo) {
                carrier = ResultsFragment.this.carriers.get(segment.getJSONObject("flight").getString("carrier"));
            } else {
                carrier = "No carrier data";
            }
            String connectionTime = "";
            if(segment.has("connectionDuration"))
                connectionTime = minutesToStringTime(segment.getInt("connectionDuration"));

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

            TripDetails details = new TripDetails(carrier, departureDetail, arriveDetail, detailTime);
            details.setConnectionTime(connectionTime);

            result.add(details);
        }
        return result;
    }
    private class ResultsListAdapter implements ListAdapter {

        public LayoutInflater inflater;
        ArrayList<Group> groups;


        public ResultsListAdapter(Activity activity, ArrayList<Group> groups) {
            this.inflater = activity.getLayoutInflater();
            this.groups = groups;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            if(groups != null)
                return groups.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return groups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.list_group_results, null);
            }
            Group group =  groups.get(position);

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
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return getCount() == 0;
        }
    }


    public class Group {
        public String departure;
        public String arrival;
        public String travelTime;
        public String price;

        public ArrayList<TripDetails> detailsList;
        public ArrayList<TripDetails> returnDetailsList = null;
        public Group(String departure, String arrival, String travelTime, String price) {
            this.departure = departure;
            this.arrival = arrival;
            this.travelTime = travelTime;
            this.price = price;
        }



    }

    public class TripDetails implements Parcelable {
        private String carrier;
        private String departureDetail;
        private String arrivalDetail;
        private String detailTime;

        private String connectionTime = "";

        public TripDetails(String carrier, String departureDetail, String arrivalDetail, String detailTime) {
            this.carrier = carrier;
            this.departureDetail = departureDetail;
            this.arrivalDetail = arrivalDetail;
            this.detailTime = detailTime;
        }

        protected TripDetails(Parcel in) {
            carrier = in.readString();
            departureDetail = in.readString();
            arrivalDetail = in.readString();
            detailTime = in.readString();
        }

        public String getCarrier() {
            return carrier;
        }
        public String getDepartureDetail() {
            return departureDetail;
        }
        public String getArrivalDetail() {
            return arrivalDetail;
        }
        public String getDetailTime() {
            return detailTime;
        }

        public String getConnectionTime() {
            return  connectionTime;
        }

        public void setConnectionTime(String connectionTime) {
            this.connectionTime = connectionTime;
        }

        public final Creator<TripDetails> CREATOR = new Creator<TripDetails>() {
            @Override
            public TripDetails createFromParcel(Parcel in) {
                return new TripDetails(in);
            }

            @Override
            public TripDetails[] newArray(int size) {
                return new TripDetails[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(carrier);
            dest.writeString(departureDetail);
            dest.writeString(arrivalDetail);
            dest.writeString(detailTime);
        }
    }

}
