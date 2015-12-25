package com.example.shubhamkanodia.roadrunner.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.shubhamkanodia.roadrunner.Adapters.SampleFragmentPagerAdapter;
import com.example.shubhamkanodia.roadrunner.Fragments.MainFragment;
import com.example.shubhamkanodia.roadrunner.R;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 3;
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

            Intent serviceIntent = new Intent(this, SettingsActivity.class);
            startActivity(serviceIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        MainFragment fragment = (MainFragment) SampleFragmentPagerAdapter.getCurrentFragment(viewPager,sampleFragmentPagerAdapter);
                        fragment.toggleRecordingAndUpdateUI();

                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        break;
                }
                break;
        }
    }


}

