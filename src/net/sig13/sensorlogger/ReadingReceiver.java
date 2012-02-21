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
    private final static double[] readings = new double[READINGS_SIZE];
    private double lastReading = 0;
    private double average;
    private SensorManager sm;
//
    private Handler handler;
    //
    private boolean pausePoll = false;
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
     */
    public void setPollingDelay(int pollingDelay) {

        Log.d(LOG_NAME, "setPollingDelay");
        handler.removeCallbacks(this);

        if (pollingDelay < 0) {
            Log.d(LOG_NAME, "polling delay cannot be negative");
            throw new IllegalArgumentException("polling delay cannot be negative");
        }

        if (pollingDelay < Constants.MIN_POLLING_DELAY) {
            Log.w(LOG_NAME, "setting polling delay to min:" + Constants.MIN_POLLING_DELAY);
            pollingDelay = Constants.MIN_POLLING_DELAY;
        }

        if (pollingDelay > Constants.MAX_POLLING_DELAY) {
            Log.w(LOG_NAME, "setting polling delay to max:" + Constants.MAX_POLLING_DELAY);
            pollingDelay = Constants.MAX_POLLING_DELAY;
        }

        this.pollingDelay = pollingDelay;
        Log.i(LOG_NAME, "pollingDelay set to:" + pollingDelay);

        //
        if (pollingDelay == 0) {
            pausePoll = true;
        }

        if (pollingDelay > 0) {
            pausePoll = false;
            handler.postDelayed(this, pollingDelay);

        }
    }

    /*
     *
     *
     */
    public void pausePoll(boolean status) {
        pausePoll = status;
    }

    public boolean isPaused() {
        return pausePoll;
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

        readings[readCount++] = reading;

        if (readCount == READINGS_SIZE) {

            average = 0.0;

            for (double value : readings) {
                average += value;
            }

            assert (READINGS_SIZE > 0);
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

        long now = SystemClock.uptimeMillis();

        if (pausePoll == false) {
            setupBarometer();

            readCount = 0;
            Log.d(LOG_NAME, "polling in:" + pollingDelay + "ms");
            handler.postAtTime(this, now + pollingDelay);

        } else {

            // Poll is paused, check again in a few minutes
            Log.d(LOG_NAME, "polling paused: checking in:" + Constants.PAUSE_POLLING_DELAY + "ms");
            handler.postAtTime(this, now + Constants.PAUSE_POLLING_DELAY);

        }


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

            } else {
                Log.e(LOG_NAME, "Unable to get sensor device =/");
                return false;
            }

        } catch (Exception e) {
            Log.e(LOG_NAME, "failed to setup barometer", e);
            return false;
        }

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
