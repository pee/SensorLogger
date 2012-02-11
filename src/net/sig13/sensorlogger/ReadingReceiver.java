/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sig13.sensorlogger;

import android.app.Service;
import android.content.*;
import android.hardware.*;
import android.os.*;
import android.util.Log;
import net.sig13.sensorlogger.SensorLoggerService.LocalBinder;

/**
 *
 * @author pee
 */
public class ReadingReceiver extends Service implements SensorEventListener, Runnable {

    private final static String LOG_NAME = "SensorLoggerService:RR";
    //
    private final static int READINGS_SIZE = 5;
    private final static double[] bReadings = new double[READINGS_SIZE];
    private double lastReading = 0;
    private int readCount = 0;
    private double average;
    private SensorManager sm;
    private boolean listenerRegistered = false;
    private Handler handler;
    //
    private SensorLoggerService sls;
    //
    private boolean mBound;

    /*
     *
     */
    public ReadingReceiver(SensorManager sm, Handler handler) {

        if (sm == null) {
            throw new NullPointerException("SensorManager must not be null");
        }
        this.sm = sm;

        if (handler == null) {
            throw new NullPointerException("Handler must not be null");
        }
        this.handler = handler;

      


    }

    public void onSensorChanged(SensorEvent event) {

        Log.d(LOG_NAME, "onSensorChanged:");
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE:
                lastReading = event.values[0];
                addReading(lastReading);
                break;
        }
    }

    private synchronized void addReading(double reading) {

        Log.d(LOG_NAME, "new value:" + reading);

        if (readCount >= READINGS_SIZE) {
            Log.e(LOG_NAME, "Tried to add too many readings:" + readCount + ":" + reading);
            sm.unregisterListener(this);
            readCount = 0;
            return;
        }

        bReadings[readCount++] = reading;

        if (readCount == READINGS_SIZE) {

            average = 0.0;

            for (double value : bReadings) {
                average += value;
            }
            average = average / READINGS_SIZE;

            Log.i(LOG_NAME, "Average:" + average);

            sm.unregisterListener(this);
            readCount = 0;
        }

    }

    public void onAccuracyChanged(Sensor sensor, int value) {

        Log.d(LOG_NAME, "onAccuracyChanged:" + sensor + ":value:" + value);

    }

    public synchronized void run() {

        Log.d(LOG_NAME, "run()*******************************");

//          Intent intent = new Intent(this, SensorLoggerService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setupBarometer();
        long base = SystemClock.uptimeMillis();

//        while (readCount < READINGS_SIZE) {
//            Log.d(LOG_NAME, "readCount:" + readCount);
//            try {
//                wait(1000);
//            } catch (Exception e) {
//                Log.d(LOG_NAME, "Exception in wait:" + e);
//            }
//        }
        readCount = 0;

        handler.postAtTime(this, base + (60 * 1000));


    }

    private void setupBarometer() {

        Log.d(LOG_NAME, "setUpBarometer");

        try {
            //sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor bar = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);

            if (bar != null) {
                boolean running = sm.registerListener(this, bar, SensorManager.SENSOR_DELAY_NORMAL);
                listenerRegistered = true;
                Log.d(LOG_NAME, "running:" + running);

            } else {
                Log.e(LOG_NAME, "Unable to get sensor device =/");
            }

        } catch (Exception e) {
            Log.e(LOG_NAME, "failed to setup barometer", e);
        }

        Log.d(LOG_NAME, "setupBarometer():exit");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

        /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            sls = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
