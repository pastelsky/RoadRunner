package com.example.shubhamkanodia.roadrunner.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Activities.AboutActivity;
import com.example.shubhamkanodia.roadrunner.Events.ServiceStopEvent;
import com.example.shubhamkanodia.roadrunner.Helpers.Constants;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Services.DataLoggerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

// In this case, the fragment displays simple text based on the page
public class MainFragment extends Fragment implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQ_ENABLE_GPS = 3;
    @Bind(R.id.vDummy)
    View vDummy;
    XYMultipleSeriesDataset multipleSeriesDataset;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private Chronometer chronometer;
    private Button bRecord;
    private LinearLayout lvChart;
    private XYSeries Xseries;
    private XYSeries Yseries;
    private XYSeries Zseries;
    private GraphicalView chartView;
    private XYMultipleSeriesRenderer mRenderer;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, view);


        bRecord = (Button) view.findViewById(R.id.bRecord);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer);

        lvChart = (LinearLayout) view.findViewById(R.id.lvChart);


        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(vDummy)
                .setDismissText("GOT IT")
                .setContentText("When you're ready to begin your journey, start recording.")
                .setDelay(200) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse("show_showcase1") // provide a unique ID used to ensure it is only shown once
                .show();

        prepareChart();
        initGoogleServices();

        chartView = ChartFactory.getCubeLineChartView(getActivity(), multipleSeriesDataset, mRenderer, 0.4f);
        lvChart.addView(chartView);

        senSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        if (!DataLoggerService.wasStartedSuccessfully) {
            setEndUI();
        } else {
            setStartUI();
        }

        return view;
    }

    private void initGoogleServices() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOCATION_FETCH_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.LOCATION_FETCH_INTERVAL_FASTEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);


    }

    public void onEvent(ServiceStopEvent event) {
        /* Do something */
        setEndUI();
    }

    ;

    @Override
    public void onStop() {
        super.onStop();
        senSensorManager.unregisterListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!DataLoggerService.isRunning) {
            if (DataLoggerService.wasStartedSuccessfully)
                setStartUI();
            else
                setEndUI();
        }


        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 80) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                Xseries.add(System.currentTimeMillis(), Math.abs(x));
                Yseries.add(System.currentTimeMillis(), Math.abs(y));
                Zseries.add(System.currentTimeMillis(), Math.abs(z));
                mRenderer.setXAxisMin(System.currentTimeMillis() - 350);
                chartView.repaint();


            }
        }
    }

    public void setStartUI() {

        bRecord.setBackgroundColor(Color.RED);
        bRecord.setText("Recording data...");
        bRecord.setTextColor(Color.RED);
        bRecord.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record, 0, 0, 0);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();


    }

    public void setEndUI() {

        bRecord.setBackgroundColor(Color.parseColor("#1c2b38"));
        bRecord.setTextColor(0x88ffffff);
        bRecord.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record_inactive, 0, 0, 0);
        chronometer.stop();

        bRecord.setText("Start Data Collection");
        Intent intent = new Intent(getActivity().getApplicationContext(), DataLoggerService.class);
        getActivity().stopService(intent);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @OnClick(R.id.tvWhat)
    public void clickWhat(View view) {
        startActivity(new Intent(getContext(), AboutActivity.class));
    }

    @OnClick(R.id.bRecord)
    public void toggleRecorder(View v) {
        checkForGPS();
    }

    public void toggleRecordingAndUpdateUI() {

        if (!DataLoggerService.wasStartedSuccessfully) {

            getActivity().startService(new Intent(getActivity().getApplicationContext(), DataLoggerService.class));
            setStartUI();
        } else {
            getActivity().stopService(new Intent(getActivity().getApplicationContext(), DataLoggerService.class));
            setEndUI();
        }
    }

    public void prepareChart() {

        Xseries = new XYSeries("X Axis");
        Yseries = new XYSeries("Y Axis");
        Zseries = new XYSeries("Z Axis");


        multipleSeriesDataset = new XYMultipleSeriesDataset();
        multipleSeriesDataset.addSeries(0, Xseries);
        multipleSeriesDataset.addSeries(1, Yseries);
        multipleSeriesDataset.addSeries(2, Zseries);


        XYSeriesRenderer Xrenderer = new XYSeriesRenderer();
        Xrenderer.setLineWidth(2);
        Xrenderer.setColor(Color.parseColor("#AA4862EB"));
        XYSeriesRenderer.FillOutsideLine xfill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ABOVE);
        xfill.setColor(Color.parseColor("#334862EB"));
        Xrenderer.addFillOutsideLine(xfill);

        XYSeriesRenderer Yrenderer = new XYSeriesRenderer();
        Yrenderer.setLineWidth(2);
        Yrenderer.setColor(Color.parseColor("#AA48EB60"));
        XYSeriesRenderer.FillOutsideLine yfill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ABOVE);
        yfill.setColor(Color.parseColor("#3348EB60"));
        Yrenderer.addFillOutsideLine(yfill);


        XYSeriesRenderer Zrenderer = new XYSeriesRenderer();
        Zrenderer.setLineWidth(2);
        Zrenderer.setColor(Color.parseColor("#AA48E3EB"));
        XYSeriesRenderer.FillOutsideLine zfill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ABOVE);
        zfill.setColor(Color.parseColor("#3348E3EB"));
        Zrenderer.addFillOutsideLine(zfill);


        mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(0, Xrenderer);
        mRenderer.addSeriesRenderer(1, Yrenderer);
        mRenderer.addSeriesRenderer(2, Zrenderer);


        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(15);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowLegend(false);
        int margins[] = {0, 0, 0, 0};
        mRenderer.setMargins(margins);
        mRenderer.setShowAxes(false);
        mRenderer.setShowLabels(false);
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setShowGrid(false); // we show the grid


    }


    private void checkForGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates s = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        toggleRecordingAndUpdateUI();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(getActivity(), REQ_ENABLE_GPS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getActivity(), "Location Services are not accurate enough to record.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "GOOGLE API Connection failed", Toast.LENGTH_SHORT).show();

    }

}
