package com.example.ilai.minesweeper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;

public class RotateService extends Service implements SensorEventListener{

    private final IBinder mBinder = new RotateBinder();
    private RotationListener mRotationListener;
    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor;
    private float[] mRotationMatrix = new float[16];
    private float[] mStartingPhoneOrientation = null;
    private final int MAX_ALLOWED_DEVIATION = 45;
    private boolean deviationIsFine = true;
    private Handler handler;
    private int interval = 1000;
    private boolean shouldRead = false;

    private final Runnable processSensors = new Runnable() {
        @Override
        public void run() {
            shouldRead = true;
            handler.postDelayed(this, interval);
        }
    };


    public class RotateBinder extends Binder {
        void registerListener(RotationListener listener) {
            mRotationListener = listener;
        }
    }

    public interface RotationListener {
        void thereIsHighAngleDeviation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        handler = new Handler();
        sutupRotationVectorSensor();
        mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        handler.post(processSensors);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        handler.removeCallbacks(processSensors);
        mSensorManager.unregisterListener(this, mRotationVectorSensor);
        return super.onUnbind(intent);
    }

    private void sutupRotationVectorSensor() {
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        if(sensorList.size() > 0) {
            mRotationVectorSensor = sensorList.get(0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && shouldRead) {
            if(mStartingPhoneOrientation == null){
                mStartingPhoneOrientation = new float[3];
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                float[] orientation = new float[3];
                SensorManager.getOrientation(mRotationMatrix, orientation);
                mStartingPhoneOrientation[0] = (float)Math.toDegrees(orientation[0]);
                mStartingPhoneOrientation[1] = (float)Math.toDegrees(orientation[1]);
                mStartingPhoneOrientation[2] = (float)Math.toDegrees(orientation[2]);
            }
            else {
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                float[] orientation = new float[3];
                SensorManager.getOrientation(mRotationMatrix, orientation);
                float[] bearing = {(float)Math.toDegrees(orientation[0]) - mStartingPhoneOrientation[0],
                        (float)Math.toDegrees(orientation[1]) - mStartingPhoneOrientation[1],
                        (float)Math.toDegrees(orientation[2]) - mStartingPhoneOrientation[2]};
                if(Math.abs(bearing[1]) > MAX_ALLOWED_DEVIATION){
                    deviationIsFine = false;
                    mRotationListener.thereIsHighAngleDeviation();
                }
                else if(!deviationIsFine){
                    deviationIsFine = true;
                }
            }
            shouldRead = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}

