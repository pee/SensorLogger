//
//
//
package net.sig13.sensorlogger;

import android.app.Service;
import android.content.*;
import android.hardware.*;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import net.sig13.sensorlogger.SensorLoggerService.LocalBinder;
import net.sig13.sensorlogger.cp.PressureDataTable;
import net.sig13.sensorlogger.cp.SensorContentProvider;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.*;

//
//
//
public class ReadingReceiver extends Service implements SensorEventListener, LocationListener, Runnable {

    private final static String TAG = "SLoggerService:RR";
    //

    public enum PollStatus {

        Paused, Run
    };

    public enum SyncStatus {

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
    private LocationManager lm;
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
    private boolean enableLocation = Constants.PREF_DEFAULT_ENABLE_LOCATION;
    //
    private ContentResolver cr;
    //
    private volatile PollStatus pollStatus = PollStatus.Run;
    //
    private double longitude = 0.0;
    private double latitude = 0.0;
    //
    public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
    //
    private volatile SyncStatus syncStatus = SyncStatus.Run;
    //
    private volatile SyncStatus openSenSeSync = SyncStatus.Run;
    private String openSenSeFeedID = Constants.PREF_DEFAULT_OPENSENSE_FEED_ID;
    private String openSenSeAPIKey = Constants.PREF_DEFAULT_OPENSENSE_API_KEY;
    private String openSenSeAPIUri = Constants.PREF_DEFAULT_OPENSENSE_API_URI;


    /*
     *
     */
    public ReadingReceiver(SensorManager sm, LocationManager lm, Handler handler, ContentResolver cr) {

        if (sm == null) {
            throw new NullPointerException("SensorManager must not be null");
        }
        this.sm = sm;

        if (lm == null) {
            throw new NullPointerException("LocationManager must not be null");
        }
        this.lm = lm;

        if (handler == null) {
            throw new NullPointerException("Handler must not be null");
        }
        this.handler = handler;

        if (cr == null) {
            throw new NullPointerException("ContentResolver must not be null");
        }
        this.cr = cr;

    }

    /**
     *
     * @param pollingDelay
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

    /**
     *
     * @param pollStatus
     */
    public void setPollStatus(PollStatus pollStatus) {

        int newDelay;

        Log.d(TAG, "setPollStatus:" + pollStatus);

        handler.removeCallbacks(this);

        this.pollStatus = pollStatus;

        switch (pollStatus) {

            case Run:
                newDelay = pollingDelay;
                startLocationCollection();
                break;

            case Paused:
                newDelay = Constants.PAUSE_POLLING_DELAY;
                stopLocationCollection();
                break;

            default:
                Log.d(TAG, "***Unhandled enum status***");
                newDelay = Constants.PAUSE_POLLING_DELAY;

        }

        handler.postDelayed(this, newDelay);

    }

    /**
     *
     * @param event
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

    /**
     *
     * @param reading
     */
    private synchronized void addReading(double reading) {

        //Log.d(TAG, "new value:" + reading);

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

    /**
     *
     * @param newReading
     */
    private void updateRecord(double newReading) {

        Uri mNewUri;

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        long now = System.currentTimeMillis();

        mNewValues.put(PressureDataTable.COLUMN_TIME, now);
        mNewValues.put(PressureDataTable.COLUMN_VALUE, newReading);

        if (enableLocation == true) {
            // use 0.0 for data
            mNewValues.put(PressureDataTable.COLUMN_LONGITUDE, 0.0);
            mNewValues.put(PressureDataTable.COLUMN_LATITUDE, 0.0);
        } else {
            mNewValues.put(PressureDataTable.COLUMN_LONGITUDE, longitude);
            mNewValues.put(PressureDataTable.COLUMN_LATITUDE, latitude);
        }

        Uri CONTENT_URI = Uri.parse("content://" + SensorContentProvider.AUTHORITY + "/readings");

        mNewUri = cr.insert(CONTENT_URI, mNewValues);

        Log.d(TAG, "mNewUri:" + mNewUri + ":" + now);

        if (syncStatus == SyncStatus.Run) {

            if (openSenSeSync == SyncStatus.Run) {

                OpenSenSeJDO ojdo = new OpenSenSeJDO(newReading, openSenSeAPIKey, openSenSeFeedID);

                new PushToOpenSenSe().execute(ojdo);
            }
        }

    }

    /**
     *
     */
    private void startLocationCollection() {

        Log.d(TAG, "startingLocation");

        // Register the listener with the Location Manager to receive location updates
        try {

            // According to the docs PASSIVE_PROVIDER will just use readings other
            // apps have requested. We'll stick with that for now.
            //
            // other options are
            // LocationManager.NETWORK_PROVIDER
            // LocationManager.GPS_PROVIDER

            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Constants.DEFAULT_LOCATION_DELAY, 0, this);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    private void stopLocationCollection() {
        lm.removeUpdates(this);
    }

    /**
     *
     * @param location
     */
    public void onLocationChanged(Location location) {
        //Log.d(TAG, "onLocationChanged:" + location);

        latitude = location.getLatitude();
        longitude = location.getLongitude();

//        if (location.hasAltitude()) {
//            Log.d(TAG, "altitude:" + location.getAltitude());
//        }

    }

    /**
     *
     * @param provider
     * @param status
     * @param bundle
     */
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        Log.d(TAG, "onStatusChanged:" + provider + ":status:" + status + ":bundle:" + bundle);
    }

    /**
     *
     * @param provider
     */
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled:" + provider);
    }

    /**
     *
     * @param provider
     */
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled:" + provider);
    }

    /**
     *
     * @param sensor
     * @param value
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int value) {

        Log.d(TAG, "onAccuracyChanged:" + sensor.getName() + ":" + sensor.getVendor() + ":value:" + value);

    }

    /**
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

    /**
     *
     * @return
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

    /**
     * @return the enableLocation
     */
    public boolean isEnableLocation() {
        return enableLocation;
    }

    /**
     * @param enableLocation the enableLocation to set
     */
    public void setEnableLocation(boolean enableLocation) {
        Log.d(TAG, "setEnableLocation:" + enableLocation);
        this.enableLocation = enableLocation;

        if (enableLocation == true) {
            startLocationCollection();
        } else {
            stopLocationCollection();
        }
    }

    void setEnableSync(boolean enableSync) {
        if (enableSync) {
            syncStatus = SyncStatus.Run;
        } else {
            syncStatus = SyncStatus.Paused;
        }
    }

    void setEnableOpenSenSe(boolean enableOpenSenSe) {
        if (enableOpenSenSe) {
            openSenSeSync = SyncStatus.Run;
        } else {
            openSenSeSync = SyncStatus.Paused;
        }
    }

    void setOpenSenSeFeedID(String openSenSeFeedID) {
        this.openSenSeFeedID = openSenSeFeedID;
    }

    void setOpenSenSeAPIKey(String openSenSeAPIKey) {
        this.openSenSeAPIKey = openSenSeAPIKey;
    }

    void setOpenSenSeAPIUri(String openSenSeAPIUri) {
        this.openSenSeAPIUri = openSenSeAPIUri;
    }

    /*
     *
     *
     */
    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static HttpClient getHttpClient() {
        HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        return httpClient;
    }

    /**
     *
     */
    class OpenSenSeJDO {

        public final double reading;
        public final String apiKey;
        public final String feedId;

        public OpenSenSeJDO(double reading, String apiKey, String feedId) {

            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("APIKEY cannot be null");
            }

            if (feedId == null || feedId.isEmpty()) {
                throw new IllegalArgumentException("FEED_ID cannot be null");
            }

            this.reading = reading;
            this.apiKey = apiKey;
            this.feedId = feedId;
        }
    }

    /**
     *
     */
    class PushToOpenSenSe extends AsyncTask<OpenSenSeJDO, Void, Void> {

        @Override
        protected Void doInBackground(OpenSenSeJDO... arg0) {

            try {
                doPush(arg0[0]);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }

            return null;
        }

        public void doPush(OpenSenSeJDO jdo) throws JSONException, UnsupportedEncodingException, IOException, AuthenticationException {
            Log.i(TAG, "Pushing to open.sen.se");

            if (jdo == null) {
                throw new IllegalArgumentException("JDO cannot be null");
            }

            if (jdo.apiKey == null || jdo.apiKey.isEmpty()) {
                throw new IllegalArgumentException("APIKEY cannot be null");
            }

            if (jdo.feedId == null || jdo.feedId.isEmpty()) {
                throw new IllegalArgumentException("FEED_ID cannot be null");
            }

            Log.i(TAG, "APIKEY:" + jdo.apiKey);
            Log.i(TAG, "FEEDID:" + jdo.feedId);
            Log.i(TAG, "APIURI:" + openSenSeAPIUri);

            HttpPost post = new HttpPost(openSenSeAPIUri);
            post.addHeader("sense_key", jdo.apiKey);

            JSONObject jo = new JSONObject();
            jo.put("feed_id", jdo.feedId);
            jo.put("value", jdo.reading);


            StringEntity se;
            se = new StringEntity(jo.toString());


            //sets the post request as the resulting string
            post.setEntity(se);
            //sets a request header so the page receving the request will know what to do with it
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            HttpResponse resp = getHttpClient().execute(post);

            Log.d(TAG, "StatusCode:" + resp.getStatusLine().getStatusCode());
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    Log.e(TAG, "Authentication exception in sending to open.sen.se");
                    throw new AuthenticationException();
                } else {
                    Log.e(TAG, "Server error in sending to open.sen.se: " + resp.getStatusLine());
                    throw new IOException();
                }
            }

//        200 (OK)Request processed successfully
//400 (Bad Request)Any case where a parameter is invalid, or a required parameter is missing. Additionnal information might be available, like when you try to access a resource that doesn’t belong to you ::
//{"failed_events": [[{"feed_id": 1470, "value": 1432101829}, "(' - feed does not belong to user.',)"]]}
//401 (Authorization Required)The key (sense_key or application_key) provided in the request was incorrect.
//404 (Not found)Ressource does not exist (bad URL, path...)
//500 (Internal Server Error)Sen.se servers are not in good shape. The request might be valid, but needs to be retrieved later .


            String response = EntityUtils.toString(resp.getEntity());

            Log.d(TAG, response);

            final JSONArray jsonResponse = new JSONArray(response);

            // do something with response


        }
    }
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        /**
         *
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            sls = binder.getService();
            mBound = true;
        }

        /**
         *
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
}
