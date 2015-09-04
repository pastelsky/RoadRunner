package com.example.shubhamkanodia.roadrunner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private TextView tvAccel;
    private TextView tvAccel2;
    private TextView tvLocation;

    private LinearLayout lvChart;
    private XYSeries Xseries;
    private XYSeries Yseries;
    private XYSeries Zseries;

    private GraphicalView chartView;
    private XYMultipleSeriesRenderer mRenderer;


    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAccel = (TextView) findViewById(R.id.tvAccel);
        tvAccel2 = (TextView) findViewById(R.id.tvAccel2);
        tvLocation = (TextView) findViewById(R.id.tvLocation);

        lvChart = (LinearLayout) findViewById(R.id.lvChart);


        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                tvLocation.setText("Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
        LocationListener mlocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

         Xseries = new XYSeries("X Axis");
        Yseries = new XYSeries("Y Axis");
        Zseries = new XYSeries("Z Axis");



        XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();
        multipleSeriesDataset.addSeries(0,Xseries);
        multipleSeriesDataset.addSeries(1, Yseries);
        multipleSeriesDataset.addSeries(2, Zseries);



        XYSeriesRenderer Xrenderer = new XYSeriesRenderer();
        Xrenderer.setLineWidth(1);
        Xrenderer.setColor(Color.RED);

        XYSeriesRenderer Yrenderer = new XYSeriesRenderer();
        Yrenderer.setLineWidth(1);
        Yrenderer.setColor(Color.BLUE);


        XYSeriesRenderer Zrenderer = new XYSeriesRenderer();
        Zrenderer.setLineWidth(1);
        Zrenderer.setColor(Color.GREEN);

        mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(0,Xrenderer);
        mRenderer.addSeriesRenderer(1,Yrenderer);
        mRenderer.addSeriesRenderer(2, Zrenderer);



        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
// Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(12);
        mRenderer.setYAxisMin(-12);
        mRenderer.setZoomEnabled(true, false);
        mRenderer.setShowGrid(true); // we show the grid

         chartView = ChartFactory.getLineChartView(this, multipleSeriesDataset, mRenderer);
        lvChart.addView(chartView);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                tvAccel.setText("X: " + x + "Y: " + y + "Z: " + z);

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                tvAccel2.setText("Speed: " + speed);
                Xseries.add(System.currentTimeMillis(), x);
                Yseries.add(System.currentTimeMillis(), y);
                Zseries.add(System.currentTimeMillis(), z);

                mRenderer.setXAxisMin(System.currentTimeMillis() -5000);
                chartView.repaint();

                last_x = x;
                last_y = y;
                last_z = z;

            }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
