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
import android.hardware.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import java.util.List;

public class SensorLoggerService extends IntentService implements OnSharedPreferenceChangeListener {

    private static final String TAG = "SensorLoggerService";
    //
    private static final int NOTIFICATION_ID = 0xdeadbeef;
    //
    private SensorManager sm;
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
    //
    private ContentResolver cr;
    //
    private List<Sensor> ambientTemp;
    private List<Sensor> light;
    private List<Sensor> pressure;
    private List<Sensor> humidity;

    /*
     *
     *
     */
    public SensorLoggerService() {
        super("SensorLoggerService");
        Log.d(TAG, "SensorLoggerService()");

        _init();
    }

    /*
     *
     *
     */
    public SensorLoggerService(String name) {
        super(name);
        Log.d(TAG, "SensorLoggerService(" + name + ")");

        _init();
    }

    /*
     *
     * fucking lazy!
     *
     */
    private void _init() {
//        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        rr = new ReadingReceiver(sm, handler);
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

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate:");

        super.onCreate();

        Log.d(TAG, "getSharedPreferences");
        //prefs = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        prefs.registerOnSharedPreferenceChangeListener(this);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        acquireSensors();
        dumpSensors();

        cr = this.getContentResolver();
        rr = new ReadingReceiver(sm, handler, cr);
        String piString = prefs.getString(Constants.PREF_KEY_POLLING_INTERVAL, Constants.DEFAULT_POLLING_DELAY_STRING);
        pollingInterval = Integer.parseInt(piString);
        rr.setPollingDelay(pollingInterval);

        Context context = getBaseContext();
        // The service is being created
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, OnAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, 60000, pi);



        // clear pending scheduled handle events for rr
        handler.removeCallbacks(rr);

        // schedule run of rr
        handler.postDelayed(rr, 0);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");
        //super.onStartCommand(intent, flags, startId);

        Notification notification = mkForegroundNotification();
        startForeground(NOTIFICATION_ID, notification);

        Toast.makeText(this, "sensor logger service starting", Toast.LENGTH_SHORT).show();
        //return super.onStartCommand(intent, flags, startId);

        return mStartMode;

    }

    /*
     *
     *
     */
    private Notification mkForegroundNotification() {

        Intent notificationIntent = new Intent(this, SensorLogger.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(mkLargeIcon());
        builder.setSmallIcon(R.drawable.ic_stat_notif_small_icon);
        builder.setOngoing(true);

        Notification notification = builder.getNotification();
        notification.setLatestEventInfo(this, getText(R.string.notification_title), getText(R.string.notification_message), pendingIntent);

        return notification;

    }

    private synchronized void acquireSensors() {

        //SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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

    private Bitmap mkLargeIcon() {

        Bitmap raw = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        return raw;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        // A client is binding to the service with bindService()


        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        Log.d(TAG, "unregistering for shared pref changes");
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();

    }

    /*
     *
     *
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

    public void onSharedPreferenceChanged(SharedPreferences sPref, String key) {

        Log.d(TAG, "onSharedPreferenceChanged");
        Log.d(TAG, "sPref:" + sPref);
        Log.d(TAG, "key:" + key);

        if (key == null) {
            return;
        }

        if (key.compareToIgnoreCase(Constants.PREF_KEY_POLLING_INTERVAL) == 0) {

            Log.d(TAG, "Updating polling interval");

            String pi = sPref.getString(Constants.PREF_KEY_POLLING_INTERVAL, Constants.DEFAULT_POLLING_DELAY_STRING);

            pollingInterval = Integer.parseInt(pi);

            Log.d(TAG, "Updating polling interval to " + pollingInterval);
            rr.setPollingDelay(pollingInterval);

            return;

        }

        if (key.compareToIgnoreCase(Constants.PREF_KEY_ENABLE_POLLING) == 0) {

            Log.d(TAG, "Updating polling status");
            boolean pauseStatus = sPref.getBoolean(Constants.PREF_KEY_ENABLE_POLLING, false);

            Log.d(TAG, "Updating polling status to " + pauseStatus);
            rr.pausePoll(pauseStatus);

            return;
        }


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