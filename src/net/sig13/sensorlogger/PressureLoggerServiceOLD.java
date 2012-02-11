//
//
//
package net.sig13.sensorlogger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

abstract public class PressureLoggerServiceOLD extends IntentService {

    private final static String logName = "BAR:PresLogSer:";
    public static final String LOCK_NAME_STATIC = "net.sig13.barometer.Static";
    private static PowerManager.WakeLock lockStatic = null;

    //private ReadingReceiver rr = new ReadingReceiver();
    private ReadingReceiver rr;

    public static void acquireStaticLock(Context context) {
        getLock(context).acquire();
    }

    synchronized private static PowerManager.WakeLock getLock(Context context) {

        if (lockStatic == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
            lockStatic.setReferenceCounted(true);
        }

        return (lockStatic);
    }

    public PressureLoggerServiceOLD(String name) {
        super(name);
    }

    @Override
    final protected void onHandleIntent(Intent intent) {
        try {
            doWakefulWork(intent);
        } finally {
            getLock(this).release();
        }
    }

    protected void doWakefulWork(Intent intent) {

        File log = new File(Environment.getExternalStorageDirectory(), "PressureLog.txt");

        try {
            
            BufferedWriter out = new BufferedWriter(new FileWriter(log.getAbsolutePath(), log.exists()));

            out.write(new Date().toString());
            out.write("\n");
            out.close();

        } catch (IOException e) {

            Log.e("AppService", "Exception appending to log file", e);

        }

    }

        // Start getting barometer readings.
    public void setUpBarometer() {
        
    	Log.d( logName, "set up barometer");
    	try {
	    	SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    	Sensor bar = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);

	    	if(bar!=null) {
	        	boolean running = sm.registerListener(rr, bar, SensorManager.SENSOR_DELAY_NORMAL);
	        	Log.d(logName, running + "");
	    	}
    	} catch(Exception e) {
    		Log.d( logName, e.getMessage());
    	}
    }
}
