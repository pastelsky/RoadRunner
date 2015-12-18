package com.example.shubhamkanodia.roadrunner.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.SampleFragmentPagerAdapter;


public class MainActivity extends AppCompatActivity {

    int activeTab;
    ViewPager viewPager;
    SampleFragmentPagerAdapter sampleFragmentPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         viewPager = (ViewPager) findViewById(R.id.viewpager);
        sampleFragmentPagerAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this);
        viewPager.setAdapter(sampleFragmentPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

public void changeFragment(){
   viewPager.setCurrentItem(1, true);
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
//        if (id == R.id.action_sync_all) {
//
//            Intent serviceIntent = new Intent(getApplicationContext(), UploadService.class);
//            serviceIntent.putExtra("upload_bulk", true);
//            startService(serviceIntent);
//
//
//
//            return true;
//        }

        if (id == R.id.action_settings) {
            Toast.makeText(this, "sddsdds", Toast.LENGTH_SHORT).show();

            Intent serviceIntent = new Intent(this, SettingsActivity.class);
            startActivity(serviceIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

