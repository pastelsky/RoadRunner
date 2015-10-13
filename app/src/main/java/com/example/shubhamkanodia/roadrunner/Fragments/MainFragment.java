package com.example.shubhamkanodia.roadrunner.Fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Events.ServiceStopEvent;
import com.example.shubhamkanodia.roadrunner.Events.UploadChangeEvent;
import com.example.shubhamkanodia.roadrunner.Services.DataLoggerService;
import com.example.shubhamkanodia.roadrunner.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import de.greenrobot.event.EventBus;

// In this case, the fragment displays simple text based on the page
public class MainFragment extends Fragment implements SensorEventListener {


    private TextView tvAccel;
    private TextView tvAccel2;
    private TextView tvLocation;
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


    private boolean isRecodring = false;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        EventBus.getDefault().register(this);


        tvAccel = (TextView) view.findViewById(R.id.tvAccel);
        tvAccel2 = (TextView) view.findViewById(R.id.tvAccel2);
        tvLocation = (TextView) view.findViewById(R.id.tvLocation);
        bRecord = (Button) view.findViewById(R.id.bRecord);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer);

        lvChart = (LinearLayout) view.findViewById(R.id.lvChart);

        Xseries = new XYSeries("X Axis");
        Yseries = new XYSeries("Y Axis");
        Zseries = new XYSeries("Z Axis");


        XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();
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

        chartView = ChartFactory.getCubeLineChartView(getActivity(), multipleSeriesDataset, mRenderer, 0.4f);
        lvChart.addView(chartView);



            bRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!isRecodring) {

                        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                        boolean gps_enabled = false;

                        try {
                            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        } catch (Exception ex) {
                        }


                        if (!gps_enabled) {
                            // notify user
                            final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                            dialog.setMessage("We need your GPS data to be recorded too!");
                            dialog.setIcon(R.drawable.ic_location);
                            dialog.setTitle("Please enable Location");
                            dialog.setPositiveButton("Turn On GPS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    getActivity().startActivity(myIntent);
                                    //get gps
                                }
                            });
                            dialog.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                }
                            });
                            dialog.show();
                        } else {
                            startRecorderService();
                            Toast.makeText(getActivity(), "Recording data in background", Toast.LENGTH_SHORT).show();
                            getActivity().finish();

                        }
                        //gps on


                    }// if not recording
                    else {
                        endRecorderService();

                    }
                }

            });

        senSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        return view;
    }
//    @Override
//    public void onPause(){
//        super.onPause();
//
//        senSensorManager.unregisterListener(this);
//    }

    @Override
    public void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);


    }

    @Override
    public void onStop() {
        super.onStop();
        senSensorManager.unregisterListener(this);

    }

    public void onEvent(ServiceStopEvent event) {
  endRecorderService();

    }


    @Override
    public void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);


        if (isMyServiceRunning(DataLoggerService.class))
            startRecorderService();
        else
            endRecorderService();

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

    public void startRecorderService() {

        bRecord.setBackgroundColor(Color.RED);
        bRecord.setText("Recording data...");
        bRecord.setTextColor(Color.RED);
        bRecord.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record, 0, 0, 0);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();


        Intent intent = new Intent(getActivity().getApplicationContext(), DataLoggerService.class);

        if (!isMyServiceRunning(DataLoggerService.class))
            getActivity().startService(intent);
        isRecodring = true;

    }

    public void endRecorderService() {

        isRecodring = false;

        bRecord.setBackgroundColor(Color.parseColor("#1c2b38"));
        bRecord.setTextColor(0x88ffffff);
        bRecord.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record_inactive, 0, 0, 0);
        chronometer.stop();

        bRecord.setText("Start Data Collection");
        Intent intent = new Intent(getActivity().getApplicationContext(), DataLoggerService.class);
        getActivity().stopService(intent);

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
