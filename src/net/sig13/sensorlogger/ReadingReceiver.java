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
    public final static int DEFAULT_POLLING_DELAY = 60000;
    //
    private final static int READINGS_SIZE = 5;
    private final static double[] bReadings = new double[READINGS_SIZE];
    private double lastReading = 0;
    private double average;
    private SensorManager sm;
    private boolean listenerRegistered = false;
    private Handler handler;
    //
    private SensorLoggerService sls;
    //
    private volatile int readCount = 0;
    //
    private boolean mBound;
    // polling delay in milliseconds
    private int pollingDelay = DEFAULT_POLLING_DELAY;

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

    /*
     *
     *
     *
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.d(LOG_NAME, "onSensorChanged:");
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE:
                lastReading = event.values[0];
                addReading(lastReading);
                break;
        }
    }

    /*
     *
     *
     */
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

    /*
     *
     *
     *
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int value) {

        Log.d(LOG_NAME, "onAccuracyChanged:" + sensor + ":value:" + value);

    }

    /*
     *
     *
     *
     */
    @Override
    public synchronized void run() {

        Log.d(LOG_NAME, "run()*******************************");

        setupBarometer();

        long now = SystemClock.uptimeMillis();

        readCount = 0;

        handler.postAtTime(this, now + (pollingDelay));


    }

    /*
     *
     *
     */
    private boolean setupBarometer() {

        Log.d(LOG_NAME, "setUpBarometer");

        try {
            Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);

            if (barometer != null) {

                boolean running = sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);

                if (running == false) {
                    Log.e(LOG_NAME, "failed to register listener with sensor manager");
                    return false;
                }

                listenerRegistered = true;

            } else {
                Log.e(LOG_NAME, "Unable to get sensor device =/");
            }

        } catch (Exception e) {
            Log.e(LOG_NAME, "failed to setup barometer", e);
            return false;
        }


        Log.d(LOG_NAME, "setupBarometer():exit");
        return true;

    }

    /*
     *
     *
     */
    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        /*
         *
         *
         */
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            sls = binder.getService();
            mBound = true;
        }

        /*
         *
         *
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
