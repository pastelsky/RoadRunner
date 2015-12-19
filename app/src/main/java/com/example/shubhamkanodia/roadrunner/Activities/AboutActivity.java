package com.example.shubhamkanodia.roadrunner.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.example.shubhamkanodia.roadrunner.R;

import net.grobas.view.MovingImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity {

    @Bind(R.id.tvAbout)
    TextView abt;

    @Bind(R.id.miv)
    MovingImageView miv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        abt.setText(Html.fromHtml(getString(R.string.large_text)), TextView.BufferType.SPANNABLE);
        miv.getMovingAnimator().setInterpolator(new LinearInterpolator());

    }


}
