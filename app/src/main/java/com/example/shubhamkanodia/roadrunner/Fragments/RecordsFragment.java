package com.example.shubhamkanodia.roadrunner.Fragments;


import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Events.UploadChangeEvent;
import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.example.shubhamkanodia.roadrunner.Models.RecordRow;
import com.example.shubhamkanodia.roadrunner.Models.SensorRecorder;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Models.RecordingItem;
import com.example.shubhamkanodia.roadrunner.RecordingsAdapter;
import com.example.shubhamkanodia.roadrunner.Services.UploadService;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class RecordsFragment extends Fragment {

    Realm realm;
    private TextView tvData;
    private Button bshowData;
    private ListView lvRecords;
    private ArrayList<RecordingItem> records;
    private RecyclerView rvRecords;
    private RecordingsAdapter recordingsAdapter;
    RecordingsAdapter.RecyclerViewClickListener itemListener;


    public static RecordsFragment newInstance() {
        RecordsFragment fragment = new RecordsFragment();
        return fragment;
    }

    public RecordsFragment() {
        // Required empty public constructor
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




        itemListener = new RecordingsAdapter.RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {

                if ((!Helper.isOnline(getActivity()) || !Helper.isOnlineOnWifi(getActivity())) && !records.get(position).isSynced()) {
                    Toast.makeText(getContext(), "No WIFI internet connection. Try later", Toast.LENGTH_SHORT).show();
                } else if (!records.get(position).isSynced()) {

                    Intent serviceIntent = new Intent(getActivity(), UploadService.class);
                    serviceIntent.putExtra("start_time", records.get(position).start_time);
                    getActivity().startService(serviceIntent);

                    records.get(position).isSyncing = true;
                    refreshList(false);
                } else if (records.get(position).isSynced()) {
                    RecordingItem toshow = records.get(position);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("geo:" + toshow.getstart_lat() + "," + toshow.getstart_long()
                                    + "?q=" + toshow.getend_lat() + "," + toshow.getend_long() + "(name)"));
                    intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
                    startActivity(intent);

                }


            }
        };

        EventBus.getDefault().register(this);

        rvRecords = (RecyclerView) view.findViewById(R.id.rvRecords);
        records = new ArrayList<RecordingItem>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.getReverseLayout();
        rvRecords.setLayoutManager(layoutManager);


        recordingsAdapter = new RecordingsAdapter(getContext(), itemListener, records);
        rvRecords.setAdapter(recordingsAdapter);

        RealmQuery<RecordRow> query = realm.where(RecordRow.class);

        RealmResults<RecordRow> results = query.findAll();

        for (RecordRow record : results) {
            records.add(records.size(), new RecordingItem(record));
        }

        recordingsAdapter.notifyDataSetChanged();

        return view;
    }

    public void onEvent(UploadChangeEvent event) {
        rvRecords.invalidate();
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

            RealmQuery<RecordRow> query = realm.where(RecordRow.class);

            RealmResults<RecordRow> results = query.findAll();

            records.clear();

            for (RecordRow record : results) {
                records.add(new RecordingItem(record));
            }
        }
        recordingsAdapter.notifyDataSetChanged();

    }

}
