package com.example.shubhamkanodia.roadrunner.Fragments;


import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Adapters.RecordingsAdapter;
import com.example.shubhamkanodia.roadrunner.Events.UploadChangeEvent;
import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.example.shubhamkanodia.roadrunner.Models.Journey;
import com.example.shubhamkanodia.roadrunner.Models.RecordRow;
import com.example.shubhamkanodia.roadrunner.Models.RecordingItem;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Services.UploadService;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class RecordsFragment extends Fragment {

    Realm realm;
    RecordingsAdapter.RecyclerViewClickListener itemListener;
    private TextView tvData;
    private Button bshowData;
    private ListView lvjourneyArrayList;
    private ArrayList<Journey> journeyArrayList;
    private RecyclerView rvjourneyArrayList;
    private RecordingsAdapter recordingsAdapter;
    RealmResults<Journey> journeyRealmResults;


    public RecordsFragment() {
        // Required empty public constructor
    }

    public static RecordsFragment newInstance() {
        RecordsFragment fragment = new RecordsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        realm = Realm.getInstance(getActivity().getApplicationContext());

//        Toast.makeText(getContext(), " Fragment being called", Toast.LENGTH_SHORT).show();

        itemListener = new RecordingsAdapter.RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {

                if ((!Helper.isOnline(getActivity()) || !Helper.isOnlineOnWifi(getActivity())) && !journeyArrayList.get(position).isSynced()) {
                    Toast.makeText(getContext(), "No WIFI internet connection. Try later", Toast.LENGTH_SHORT).show();
                }
//                else if (!journeyArrayList.get(position).isSynced()) {
//
//                    Intent serviceIntent = new Intent(getActivity(), UploadService.class);
//                    serviceIntent.putExtra("start_time", journeyArrayList.get(position).getStartTime());
//                    getActivity().startService(serviceIntent);
//
//                    journeyArrayList.get(position).setSynced(true);
//                    refreshList(false);
//                }
                else if (journeyArrayList.get(position).isSynced()) {
                    Journey toshow = journeyArrayList.get(position);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("geo:" + toshow.getStartLat() + "," + toshow.getStartLong()
                                    + "?q=" + toshow.getEndLat() + "," + toshow.getEndLong() + "(name)"));
                    intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
                    startActivity(intent);

                }


            }
        };

        EventBus.getDefault().register(this);

        rvjourneyArrayList = (RecyclerView) view.findViewById(R.id.rvRecords);
        journeyArrayList = new ArrayList<Journey>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.getReverseLayout();
        rvjourneyArrayList.setLayoutManager(layoutManager);


        recordingsAdapter = new RecordingsAdapter(getContext(), itemListener, journeyArrayList);
        rvjourneyArrayList.setAdapter(recordingsAdapter);

        journeyRealmResults = realm.where(Journey.class)
                .equalTo("isSynced", false)
                .findAll();


        for (Journey journey : journeyRealmResults) {
            journeyArrayList.add(journeyArrayList.size(), journey);
            Toast.makeText(getContext(), " adding", Toast.LENGTH_SHORT).show();
        }

        recordingsAdapter.notifyDataSetChanged();

        return view;
    }

    public void onEvent(UploadChangeEvent event) {
        rvjourneyArrayList.invalidate();
        refreshList(true);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            refreshList(true);
        } else {
        }
    }

    public void refreshList(boolean repopulate) {

        if (repopulate) {

            journeyRealmResults = realm.where(Journey.class)
                    .findAll();

            journeyArrayList.clear();


            for (Journey record : journeyRealmResults) {
                journeyArrayList.add(record);
            }
        }
        recordingsAdapter.notifyDataSetChanged();

    }

}
