package com.example.shubhamkanodia.roadrunner.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;

import com.example.shubhamkanodia.roadrunner.Activities.AboutActivity;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Services.DataLoggerService;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

// In this case, the fragment displays simple text based on the page
public class MainFragment extends Fragment implements SensorEventListener {

    @Bind(R.id.vDummy)
    View vDummy;
    XYMultipleSeriesDataset multipleSeriesDataset;
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

        chartView = ChartFactory.getCubeLineChartView(getActivity(), multipleSeriesDataset, mRenderer, 0.4f);
        lvChart.addView(chartView);

        senSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        return view;
    }


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

    @Override
    public void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

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
//
//    public void startRecorderService() {
//
//        bRecord.setBackgroundColor(Color.RED);
//        bRecord.setText("Recording data...");
//        bRecord.setTextColor(Color.RED);
//        bRecord.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record, 0, 0, 0);
//        chronometer.setBase(SystemClock.elapsedRealtime());
//        chronometer.start();
//
//
//        Intent intent = new Intent(getActivity().getApplicationContext(), DataLoggerService.class);
//
//        if (!isMyServiceRunning(DataLoggerService.class))
//            getActivity().startService(intent);
//
//    }
//
//    public void endRecorderService() {
//
//        bRecord.setBackgroundColor(Color.parseColor("#1c2b38"));
//        bRecord.setTextColor(0x88ffffff);
//        bRecord.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record_inactive, 0, 0, 0);
//        chronometer.stop();
//
//        bRecord.setText("Start Data Collection");
//        Intent intent = new Intent(getActivity().getApplicationContext(), DataLoggerService.class);
//        getActivity().stopService(intent);
//
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @OnClick(R.id.tvWhat)
    public void clickWhat(View view) {
        startActivity(new Intent(getContext(), AboutActivity.class));
    }

    @OnClick(R.id.bRecord)
    public void toggleRecorder(View v) {
        Button toggleButton = (Button) v;

        if (DataLoggerService.wasStartedSuccessfully)
            getActivity().startService(new Intent(getActivity(), DataLoggerService.class));
        else
            getActivity().stopService(new Intent(getActivity(), DataLoggerService.class));
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
}
