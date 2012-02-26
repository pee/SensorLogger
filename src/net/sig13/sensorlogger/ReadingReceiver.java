//
//
//
package net.sig13.sensorlogger;

import android.app.Service;
import android.content.*;
import android.hardware.*;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import net.sig13.sensorlogger.SensorLoggerService.LocalBinder;
import net.sig13.sensorlogger.cp.PressureDataTable;
import net.sig13.sensorlogger.cp.SensorContentProvider;

//
//
//
public class ReadingReceiver extends Service implements SensorEventListener, Runnable {

    private final static String TAG = "SLogger:SLoggerService:RR";
    //

    public enum PollStatus {

        Paused, Run
    };
    //
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
    //private volatile boolean pausePoll = false;
    //
    private SensorLoggerService sls;
    //
    private volatile int readCount = 0;
    //
    private boolean mBound;
    // polling delay in milliseconds
    private int pollingDelay = DEFAULT_POLLING_DELAY;
    //
    private ContentResolver cr;
    //
    private volatile PollStatus pollStatus = PollStatus.Run;

    /*
     *
     */
    public ReadingReceiver(SensorManager sm, Handler handler, ContentResolver cr) {

        if (sm == null) {
            throw new NullPointerException("SensorManager must not be null");
        }
        this.sm = sm;

        if (handler == null) {
            throw new NullPointerException("Handler must not be null");
        }
        this.handler = handler;

        if (cr == null) {
            throw new NullPointerException("ContentResolver must not be null");
        }
        this.cr = cr;

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        //cr = getContentResolver();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    /*
     *
     *
     */
    public void setPollingDelay(int pollingDelay) {

        Log.d(TAG, "setPollingDelay");
        handler.removeCallbacks(this);

        if (pollingDelay < 0) {
            Log.d(TAG, "polling delay cannot be negative");
            throw new IllegalArgumentException("polling delay cannot be negative");
        }

        if (pollingDelay < Constants.MIN_POLLING_DELAY) {
            Log.w(TAG, "setting polling delay to min:" + Constants.MIN_POLLING_DELAY);
            pollingDelay = Constants.MIN_POLLING_DELAY;
        }

        if (pollingDelay > Constants.MAX_POLLING_DELAY) {
            Log.w(TAG, "setting polling delay to max:" + Constants.MAX_POLLING_DELAY);
            pollingDelay = Constants.MAX_POLLING_DELAY;
        }

        this.pollingDelay = pollingDelay;
        Log.i(TAG, "pollingDelay set to:" + pollingDelay);

        //
        if (pollingDelay == 0) {
            //pausePoll = true;
            pollStatus = PollStatus.Paused;
        }

        if (pollingDelay > 0) {
            //pausePoll = false;
            pollStatus = PollStatus.Run;
            handler.postDelayed(this, pollingDelay);

        }
    }

//    /*
//     *
//     *
//     */
//    public void setPausePoll(boolean status) {
//
//        Log.d(TAG, "pausePoll:" + pausePoll + ":" + status);
//
//
//        handler.removeCallbacks(this);
//
//        pausePoll = status;
//
//    }

    /*
     *
     *
     */
    public void setPollStatus(PollStatus pollStatus) {

        int newDelay = 0;

        Log.d(TAG, "setPollStatus:" + pollStatus);

        handler.removeCallbacks(this);

        this.pollStatus = pollStatus;

        switch (pollStatus) {
            case Run:
                newDelay = pollingDelay;
                break;
            case Paused:
                newDelay = Constants.PAUSE_POLLING_DELAY;
                break;
            default:
                Log.d(TAG, "***Unhandled enum status***");
                newDelay = Constants.PAUSE_POLLING_DELAY;

        }

        handler.postDelayed(this, newDelay);

    }

//    public boolean isPaused() {
//        return pausePoll;
//    }

    /*
     *
     *
     *
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        //Log.d(TAG, "onSensorChanged:");
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

        Log.d(TAG, "new value:" + reading);

        if (readCount >= READINGS_SIZE) {
            Log.e(TAG, "Tried to add too many readings:" + readCount + ":" + reading);
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

            Log.i(TAG, "Average:" + average);

            sm.unregisterListener(this);

            updateRecord(average);
            readCount = 0;
        }

    }

    private void updateRecord(double newReading) {

        Uri mNewUri;

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        long now = System.currentTimeMillis();

        mNewValues.put(PressureDataTable.COLUMN_TIME, now);
        mNewValues.put(PressureDataTable.COLUMN_VALUE, newReading);

        Uri CONTENT_URI = Uri.parse("content://" + SensorContentProvider.AUTHORITY + "/readings");

        mNewUri = cr.insert(CONTENT_URI, mNewValues);

        Log.d(TAG, "mNewUri:" + mNewUri + ":" + now);

    }

    /*
     *
     *
     *
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int value) {

        Log.d(TAG, "onAccuracyChanged:" + sensor.getName() + ":" + sensor.getVendor() + ":value:" + value);

    }

    /*
     *
     *
     *
     */
    @Override
    public synchronized void run() {

        Log.d(TAG, "run()*******************************");

        long now = SystemClock.uptimeMillis();

        if (pollStatus == PollStatus.Run) {

            setupBarometer();

            readCount = 0;
            Log.d(TAG, "polling in:" + pollingDelay + "ms");
            handler.postAtTime(this, now + pollingDelay);

        } else {

            // Poll is paused, check again in a few minutes
            Log.d(TAG, "polling paused: checking in:" + Constants.PAUSE_POLLING_DELAY + "ms");
            handler.postAtTime(this, now + Constants.PAUSE_POLLING_DELAY);

        }


    }

    /*
     *
     *
     */
    private boolean setupBarometer() {

        Log.d(TAG, "setUpBarometer");

        try {
            Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);

            if (barometer != null) {

                boolean running = sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);

                if (running == false) {
                    Log.e(TAG, "failed to register listener with sensor manager");
                    return false;
                }

            } else {
                Log.e(TAG, "Unable to get sensor device =/");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "failed to setup barometer", e);
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
