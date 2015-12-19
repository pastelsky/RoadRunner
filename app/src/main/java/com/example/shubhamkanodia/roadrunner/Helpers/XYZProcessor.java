package com.example.shubhamkanodia.roadrunner.Helpers;

/**
 * Created by shubhamkanodia on 19/12/15.
 */
public class XYZProcessor {

    public float x;
    public float y;
    public float z;

    public double normalizedValue;


    public XYZProcessor(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        normalizedValue = Math.pow(x * x + y * y + z * z, 0.5);
    }

}
