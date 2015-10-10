package com.example.shubhamkanodia.roadrunner;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;


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

        rvRecords = (RecyclerView) view.findViewById(R.id.rvRecords);
        records = new ArrayList<RecordingItem>();
        rvRecords.setLayoutManager(new LinearLayoutManager(getContext()));

        records.add(new RecordingItem(System.currentTimeMillis() - 9000, System.currentTimeMillis() + 9000, false, false, 11.2, 77.3, 11.3, 77.5, 0));
        records.add( new RecordingItem(System.currentTimeMillis()-999000, System.currentTimeMillis()+9000, true, false, 11, 77.3, 11.3, 77,0));
        records.add( new RecordingItem(System.currentTimeMillis()-999000, System.currentTimeMillis()+9000, true, true, 11, 77.3, 11.3, 77,0));


        recordingsAdapter = new RecordingsAdapter(getContext(), records);
        rvRecords.setAdapter(recordingsAdapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("RecordingsList");
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                for(ParseObject record : list){

                    records.add( new RecordingItem(
                            record.getLong("start_time"),
                            record.getLong("end_time"),
                                    record.getBoolean("isSynced"),
                                    false,
                                    record.getDouble("start_lat"),
                                    record.getLong("start_long"),
                                    record.getLong("end_lat"),
                                    record.getLong("end_long"),
                                    0
                    )
                    );

                }

                recordingsAdapter.notifyDataSetChanged();


            }
        });

        return view;
    }


}
