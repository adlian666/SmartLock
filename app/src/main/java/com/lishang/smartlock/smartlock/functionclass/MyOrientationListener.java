package com.lishang.smartlock.smartlock.functionclass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MyOrientationListener implements SensorEventListener {
    private SensorManager mSensorManager;
    private Context mContext;
    private Sensor mSensor;
    private float lastX;

    public MyOrientationListener( Context context ) {
        this.mContext = context;
    }

    public void start() {
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager != null) {
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ) {

    }

    @Override
    public void onSensorChanged( SensorEvent event ) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float x = event.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX) > 1.0) {
                if (mOnOrientationListner != null) {
                    mOnOrientationListner.onOrientationChanged(lastX);
                }
            }
            lastX = x;
        }
    }

    public void setmOnOrientationListner( OnOrientationListner mOnOrientationListner ) {
        this.mOnOrientationListner = mOnOrientationListner;
    }

    private OnOrientationListner mOnOrientationListner;

    public interface OnOrientationListner {
        void onOrientationChanged( float x );
    }

}
