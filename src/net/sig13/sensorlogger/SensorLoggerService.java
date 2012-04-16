//
//
//
//
package net.sig13.sensorlogger;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import java.util.List;

/*
 *
 *
 *
 */
public class SensorLoggerService extends IntentService implements OnSharedPreferenceChangeListener {

    private static final String TAG = "SLoggerService";
    //
    private static final int NOTIFICATION_ID = 0xdeadbeef;
    //
    private static final int POLLER_START_DELAY = 5000; //  5 seconds
    //
    private SensorManager sm;
    private LocationManager lm;
    //
    int mStartMode = START_STICKY;       // indicates how to behave if the service is killed
    //
    private final IBinder localBinder = new LocalBinder();
    //
    boolean mAllowRebind; // indicates whether onRebind should be used
    //
    public static final String LOCK_NAME_STATIC = "net.sig13.sensorlogger.Static";
    private static PowerManager.WakeLock lockStatic = null;
    //
    private Handler handler = new Handler();
    private ReadingReceiver rr;
    //
    private SharedPreferences prefs;
    private int pollingInterval;
    private int storageTime;
    private boolean enableLocation;
    //
    private ContentResolver cr;
    //
    private List<Sensor> ambientTemp;
    private List<Sensor> light;
    private List<Sensor> pressure;
    private List<Sensor> humidity;
    //
    private Notification notification;
    //
    private AlarmManager mgr;

    /*
     *
     *
     */
    public SensorLoggerService() {
        super("SensorLoggerService");
        Log.d(TAG, "SensorLoggerService()");

    }

    /*
     *
     *
     */
    public SensorLoggerService(String name) {
        super(name);
        Log.d(TAG, "SensorLoggerService(" + name + ")");
    }

    /*
     *
     *
     */
    public static void acquireStaticLock(Context context) {

        Log.d(TAG, "acquireStaticLock");
        getLock(context).acquire();
    }

    /*
     *
     *
     */
    synchronized private static PowerManager.WakeLock getLock(Context context) {

        Log.d(TAG, "getLock");
        if (lockStatic == null) {
            Log.d(TAG, "getLock:null");
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
            lockStatic.setReferenceCounted(true);
        }

        return (lockStatic);
    }

    /**
     *
     *
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate:");

        super.onCreate();

        Log.d(TAG, "getSharedPreferences");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        prefs.registerOnSharedPreferenceChangeListener(this);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        acquireSensors();
        dumpSensors();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        cr = getContentResolver();

        getReadingReceiver();

        Toast.makeText(this, "starting sensor logger service", Toast.LENGTH_SHORT).show();

        mkForegroundNotification();

        startForeground(NOTIFICATION_ID, notification);

        getAlarmManager();
        scheduleAlarm();

    }

    /**
     *
     */
    private void getReadingReceiver() {

        assert (sm != null);
        assert (handler != null);
        assert (cr != null);

        rr = new ReadingReceiver(sm, lm, handler, cr);

        String piString = prefs.getString(Constants.PREF_KEY_POLLING_INTERVAL, Constants.DEFAULT_POLLING_DELAY_STRING);
        setPollingInterval(Integer.parseInt(piString));
        rr.setPollingDelay(getPollingInterval());

        boolean el = prefs.getBoolean(Constants.PREF_KEY_ENABLE_LOCATION, Constants.PREF_DEFAULT_ENABLE_LOCATION);
        setEnableLocation(el);
        rr.setEnableLocation(el);


        // clear pending scheduled handle events for rr
        handler.removeCallbacks(rr);

        // schedule run of rr in POLLER_START_DELAY ms
        handler.postDelayed(rr, POLLER_START_DELAY);
    }

    /**
     *
     */
    private void getAlarmManager() {
        Context context = getBaseContext();
        mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     *
     */
    private void scheduleAlarm() {

        Context context = getBaseContext();
        Intent i = new Intent(context, OnAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, 60000, pi);
    }

    // FIXME: do i need this anymore ? ;p
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");

        return mStartMode;

    }

    /*
     *
     *
     */
    private void mkForegroundNotification() {

        Intent notificationIntent = new Intent(this, SensorLogger.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(mkLargeIcon());
        builder.setSmallIcon(R.drawable.ic_stat_notif_small_icon);
        builder.setOngoing(true);

        notification = builder.getNotification();
        notification.setLatestEventInfo(this, getText(R.string.notification_title), getText(R.string.notification_message), pendingIntent);

    }

    /*
     *
     *
     */
    private synchronized void acquireSensors() {

        assert (sm != null);

        ambientTemp = sm.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        light = sm.getSensorList(Sensor.TYPE_LIGHT);
        pressure = sm.getSensorList(Sensor.TYPE_PRESSURE);
        humidity = sm.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);

    }

    /*
     *
     *
     */
    private void dumpSensors() {

        acquireSensors();

        for (Sensor sensor : ambientTemp) {
            Log.d(TAG, "ambientTemp:" + sensor.getName() + ":" + sensor.getVendor());
        }

        for (Sensor sensor : light) {
            Log.d(TAG, "light:" + sensor.getName() + ":" + sensor.getVendor());
        }

        for (Sensor sensor : pressure) {
            Log.d(TAG, "pressure:" + sensor.getName() + ":" + sensor.getVendor());
        }

        for (Sensor sensor : humidity) {
            Log.d(TAG, "humidity:" + sensor.getName() + ":" + sensor.getVendor());
        }

    }

    /*
     *
     *
     */
    private Bitmap mkLargeIcon() {

        Bitmap raw = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        return raw;
    }

    /*
     *
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        // A client is binding to the service with bindService()


        return localBinder;
    }

    /*
     *
     *
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    /*
     *
     *
     */
    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    /*
     *
     *
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        Log.d(TAG, "unregistering for shared pref changes");
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();

    }

    /*
     *
     * FIXME: do I need this?
     */
    @Override
    protected void onHandleIntent(Intent intent) {
//        try {
//            //doWakefulWork(intent);
//            Log.d(TAG, "intent:" + intent);
//        } finally {
//            getLock(this).release();
//        }
        Log.d(TAG, "onHandleIntent:" + intent);
    }

    /*
     *
     *
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sPref, String key) {

        Log.d(TAG, "onSharedPreferenceChanged:" + key + ":");

        if (key == null) {
            return;
        }

        if (key.compareToIgnoreCase(Constants.PREF_KEY_POLLING_INTERVAL) == 0) {

            Log.d(TAG, "Updating polling interval");

            String pi = sPref.getString(Constants.PREF_KEY_POLLING_INTERVAL, Constants.DEFAULT_POLLING_DELAY_STRING);

            setPollingInterval(Integer.parseInt(pi));

            Log.d(TAG, "Updating polling interval to " + getPollingInterval());
            rr.setPollingDelay(getPollingInterval());

            return;

        }

        if (key.compareToIgnoreCase(Constants.PREF_KEY_ENABLE_POLLING) == 0) {

            Log.d(TAG, "Updating polling status");
            boolean pollStatus = sPref.getBoolean(Constants.PREF_KEY_ENABLE_POLLING, true);

            Log.d(TAG, "Updating polling status to " + pollStatus);

            if (pollStatus == true) {
                rr.setPollStatus(ReadingReceiver.PollStatus.Run);
            } else {
                rr.setPollStatus(ReadingReceiver.PollStatus.Paused);
            }

            return;
        }

        if (key.compareToIgnoreCase(Constants.PREF_KEY_ENABLE_LOCATION) == 0) {

            Log.d(TAG, "Updating location polling status");
            boolean el = sPref.getBoolean(Constants.PREF_KEY_ENABLE_LOCATION, Constants.PREF_DEFAULT_ENABLE_LOCATION);

            Log.d(TAG, "Updating location polling to:" + el);

            setEnableLocation(el);

            return;
        }

        if ( key.compareToIgnoreCase(Constants.PREF_KEY_ENABLE_SYNC) == 0 ) {

            Log.d(TAG, "Updating synchronization to online service");
            boolean es = sPref.getBoolean(Constants.PREF_KEY_ENABLE_SYNC, Constants.PREF_DEFAULT_ENABLE_SYNC);

            Log.d(TAG, "Setting sync to network to:" + es);

            Log.w(TAG, "#######################################");
            Log.w(TAG, "ENABLE CODE TO TURN SYNC ON/OFF DUMBASS");

            return;
        }

        Log.e(TAG, "Didn't handle key:" + key + ":");

    }

    /**
     * @return the pollingInterval
     */
    public int getPollingInterval() {
        return pollingInterval;
    }

    /**
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(int pollingInterval) {
        Log.d(TAG, "setPollingInterval:" + pollingInterval + ":");
        this.pollingInterval = pollingInterval;
    }

    /**
     * @return the storageTime
     */
    public int getStorageTime() {
        return storageTime;
    }

    /**
     * @param storageTime the storageTime to set
     */
    public void setStorageTime(int storageTime) {
        Log.d(TAG, "setStorageTime:" + storageTime + ":");
        this.storageTime = storageTime;
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
        Log.d(TAG, "enableLocation:" + enableLocation + ":");
        this.enableLocation = enableLocation;
    }

    /**
     * Class used for the client Binder. Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
     *
     * from: http://developer.android.com/guide/topics/fundamentals/bound-services.html
     *
     */
    public class LocalBinder extends Binder {

        SensorLoggerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SensorLoggerService.this;
        }
    }
}





//