//
//
//
//
package net.sig13.sensorlogger;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.*;
import android.os.*;
import android.util.Log;

public class SensorLoggerService extends IntentService {

    private static final String SERVICE_NAME = "SensorLoggerService";
    //
    private static final int NOTIFICATION_ID = 0xdeadbeef;
    //
    private SensorManager sm;
    private boolean listenerRegistered = false;
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


    /*
     *
     *
     */
    public SensorLoggerService() {
        super("SensorLoggerService");
        Log.d(SERVICE_NAME, "SensorLoggerService()");

        _init();
    }

    /*
     *
     *
     */
    public SensorLoggerService(String name) {
        super(name);
        Log.d(SERVICE_NAME, "SensorLoggerService(" + name + ")");

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

        Log.d(SERVICE_NAME, "acquireStaticLock");
        getLock(context).acquire();
    }

    /*
     *
     *
     */
    synchronized private static PowerManager.WakeLock getLock(Context context) {

        Log.d(SERVICE_NAME, "getLock");
        if (lockStatic == null) {
            Log.d(SERVICE_NAME, "getLock:null");
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
            lockStatic.setReferenceCounted(true);
        }

        return (lockStatic);
    }

    @Override
    public void onCreate() {
        Log.d(SERVICE_NAME, "onCreate:");

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rr = new ReadingReceiver(sm, handler);

        Context context = getBaseContext();
        // The service is being created
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, OnAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, 60000, pi);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(SERVICE_NAME, "onStartCommand");

        //super.onStartCommand(intent, flags, startId);


        Notification notification = mkForegroundNotification();
        startForeground(NOTIFICATION_ID, notification);

        handler.removeCallbacks(rr);
        handler.postDelayed(rr, 0);
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

    private Bitmap mkLargeIcon() {

        Bitmap raw = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        return raw;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(SERVICE_NAME, "onBind");
        // A client is binding to the service with bindService()


        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(SERVICE_NAME, "onUnbind");
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(SERVICE_NAME, "onRebind");
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        Log.d(SERVICE_NAME, "onDestroy");
        // The service is no longer used and is being destroyed
//        if (listenerRegistered == true) {
//            Log.d(SERVICE_NAME, "unregistering sensor listener");
//            sm.unregisterListener(this);
//        }
    }

    /*
     *
     *
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            //doWakefulWork(intent);
            Log.d(SERVICE_NAME, "intent:" + intent);
        } finally {
            getLock(this).release();
        }
        throw new UnsupportedOperationException("Not supported yet.");
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