package com.ywsggip.flightinfo;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import com.ywsggip.flightinfo.ResultsFragment.TripDetails;
import com.ywsggip.flightinfo.ResultDetailsAdapter.ListViewItem;
import com.ywsggip.flightinfo.ResultDetailsAdapter.*;

/**
 * Created by Admin on 2016-04-17.
 */
public class ResultDetailsFragment extends ListFragment {

    public static ResultDetailsFragment newInstance(ArrayList<TripDetails> data, ArrayList<TripDetails> returnData) {
        ResultDetailsFragment f = new ResultDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("data", data);
        args.putParcelableArrayList("returnData", returnData);

        f.setArguments(args);
        return f;
    }

    public ArrayList getData() {
        return getArguments().getParcelableArrayList("data");
    }

    public ArrayList getReturnData() {
        return getArguments().getParcelableArrayList("returnData");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_result_details, container, false);

        ArrayList<TripDetails> tripDetails = getData();

        ArrayList<ListViewItem> listViewItems = new ArrayList<>();

        HeaderObject outboundHeader = new HeaderObject("Outbound");
        ListViewItem headerItem = new ListViewItem(ListItemType.HEADER_VIEW, outboundHeader);
        listViewItems.add(headerItem);

        for(TripDetails td : tripDetails) {
            ListViewItem item = new ListViewItem(ListItemType.TRIP_DETAIL_VIEW, td);
            listViewItems.add(item);
            if(!td.getConnectionTime().isEmpty()) {
                WaitingTimeObject waitingTimeObject = new WaitingTimeObject(td.getConnectionTime());
                ListViewItem waitingTimeItem = new ListViewItem(ListItemType.WAITING_TIME_VIEW, waitingTimeObject);
                listViewItems.add(waitingTimeItem);
            }
        }

        ArrayList<TripDetails> returnDetails = getReturnData();
        if(returnDetails != null) {
            HeaderObject returnHeader = new HeaderObject("Return");
            ListViewItem returnHeaderItem = new ListViewItem(ListItemType.HEADER_VIEW, returnHeader);
            listViewItems.add(returnHeaderItem);

            for(TripDetails td : returnDetails) {
                ListViewItem item = new ListViewItem(ListItemType.TRIP_DETAIL_VIEW, td);
                listViewItems.add(item);
                if(!td.getConnectionTime().isEmpty()) {
                    WaitingTimeObject waitingTimeObject = new WaitingTimeObject(td.getConnectionTime());
                    ListViewItem waitingTimeItem = new ListViewItem(ListItemType.WAITING_TIME_VIEW, waitingTimeObject);
                    listViewItems.add(waitingTimeItem);
                }
            }
        }

        ResultDetailsAdapter adapter = new ResultDetailsAdapter(getActivity(), listViewItems);

        setListAdapter(adapter);

        return v;
    }





}
