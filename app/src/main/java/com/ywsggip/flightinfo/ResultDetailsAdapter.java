package com.ywsggip.flightinfo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.ywsggip.flightinfo.ResultsFragment.TripDetails;

/**
 * Created by Admin on 2016-04-28.
 */
public class ResultDetailsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<ListViewItem> mListViewItems;

    public ResultDetailsAdapter(Activity activity, ArrayList<ListViewItem> list)
    {
        mInflater = activity.getLayoutInflater();
        mListViewItems = list;
    }

    @Override
    public int getCount() {
        return mListViewItems.size();
    }

    @Override
    public int getViewTypeCount() {
        return ListItemType.TYPE_COUNT;
    }

    @Override
    public Object getItem(int position) {
        return mListViewItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mListViewItems.get(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        switch(getItemViewType(position)) {
            case ListItemType.HEADER_VIEW: {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_detail_header, null);
                    ViewHolderHeader viewHolder = new ViewHolderHeader(convertView);
                    convertView.setTag(viewHolder);
                }
                ViewHolderHeader viewHolder = (ViewHolderHeader) convertView.getTag();
                HeaderObject headerObject = (HeaderObject) mListViewItems.get(position).getObject();

                viewHolder.headerTextView.setText(headerObject.header);
            }
                break;
            case ListItemType.TRIP_DETAIL_VIEW: {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_details_results, null);
                    ViewHolderTripDetail viewHolder = new ViewHolderTripDetail(convertView);
                    convertView.setTag(viewHolder);
                }
                ViewHolderTripDetail viewHolder = (ViewHolderTripDetail)convertView.getTag();
                TripDetails tripDetails = (TripDetails) mListViewItems.get(position).getObject();

                viewHolder.arrivalDetailTextView.setText(tripDetails.getArrivalDetail());
                viewHolder.departureDetailTextView.setText(tripDetails.getDepartureDetail());
                viewHolder.travelTimeDetailTextView.setText(tripDetails.getDetailTime());
                viewHolder.carrierTextView.setText(tripDetails.getCarrier());
            }
                break;
            case ListItemType.WAITING_TIME_VIEW: {
                if(convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_waiting_time, null);
                    ViewHolderWaitingTime viewHolder = new ViewHolderWaitingTime(convertView);
                    convertView.setTag(viewHolder);
                }
                ViewHolderWaitingTime viewHolder = (ViewHolderWaitingTime) convertView.getTag();
                WaitingTimeObject waitingTimeObject = (WaitingTimeObject)mListViewItems.get(position).getObject();

                viewHolder.waitingTimeTextView.setText(waitingTimeObject.getWaitingTime());
            }
                break;
        }

        return convertView;
    }

    private static class ViewHolderHeader {
        public TextView headerTextView;

        public ViewHolderHeader(View view)
        {
            headerTextView = (TextView)view.findViewById(R.id.list_item_detail_header_textview);
        }
    }

    private static class ViewHolderWaitingTime {
        public TextView waitingTimeTextView;

        public ViewHolderWaitingTime(View view)
        {
            waitingTimeTextView = (TextView)view.findViewById(R.id.list_item_waiting_time_textview);
        }
    }

    private static class ViewHolderTripDetail {
        TextView departureDetailTextView;
        TextView arrivalDetailTextView;
        TextView travelTimeDetailTextView;
        TextView carrierTextView;

        public ViewHolderTripDetail(View view) {
            departureDetailTextView = (TextView) view.findViewById(R.id.departure_detail_textView);
            arrivalDetailTextView = (TextView) view.findViewById(R.id.arrival_detail_textView);
            travelTimeDetailTextView = (TextView) view.findViewById(R.id.travel_time_detail_textView);
            carrierTextView = (TextView) view.findViewById(R.id.carrier_detail_textView);
        }
    }

    public static class ListItemType {
        final public static int HEADER_VIEW = 0;
        final public static int TRIP_DETAIL_VIEW = 1;
        final public static int WAITING_TIME_VIEW = 2;
        final public static int TYPE_COUNT = WAITING_TIME_VIEW + 1;
    }

    public static class ListViewItem {
        private int type;
        private Object object;

        public ListViewItem(int type, Object object) {
            this.type = type;
            this.object = object;
        }

        public int getType() {
            return type;
        }

        public Object getObject() {
            return object;
        }
    }

    public static class HeaderObject {
        private String header;

        public HeaderObject(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public static class WaitingTimeObject {
        private String waitingTime;

        public WaitingTimeObject(String waitingTime) {
            this.waitingTime = waitingTime + " waiting time";
        }

        public String getWaitingTime() {
            return waitingTime;
        }
    }
}