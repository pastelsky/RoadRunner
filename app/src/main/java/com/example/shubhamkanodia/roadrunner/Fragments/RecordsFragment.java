package com.example.shubhamkanodia.roadrunner.Fragments;


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

import com.example.shubhamkanodia.roadrunner.Events.UploadChangeEvent;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Models.RecordingItem;
import com.example.shubhamkanodia.roadrunner.RecordingsAdapter;


import java.util.ArrayList;

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

        EventBus.getDefault().register(this);

        rvRecords = (RecyclerView) view.findViewById(R.id.rvRecords);
        records = new ArrayList<RecordingItem>();
        rvRecords.setLayoutManager(new LinearLayoutManager(getContext()));

        recordingsAdapter = new RecordingsAdapter(getContext(), records);
        rvRecords.setAdapter(recordingsAdapter);

        RealmQuery<com.example.shubhamkanodia.roadrunner.Models.RecordRow> rq = realm.where(com.example.shubhamkanodia.roadrunner.Models.RecordRow.class);
        RealmResults<com.example.shubhamkanodia.roadrunner.Models.RecordRow> results = rq.findAll();

                for(com.example.shubhamkanodia.roadrunner.Models.RecordRow record : results){
                    records.add( new RecordingItem(record) );
                }

                recordingsAdapter.notifyDataSetChanged();

        return view;
    }

    public void onEvent(UploadChangeEvent event){

        recordingsAdapter.notifyDataSetChanged();

    }

}
