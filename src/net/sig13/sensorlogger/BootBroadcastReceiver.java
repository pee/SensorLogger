//
//
//
package net.sig13.sensorlogger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.os.SystemClock;
import android.util.Log;

/**
 *
 * @author pee
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = "SLogger:BBR";
    private static final int PERIOD = 60000;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "onReceive");

        // Start the pressure logger service when the phone boots
        context.startService(new Intent(context, SensorLoggerService.class));

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, OnAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        Log.i(TAG, "scheduling alarms");
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, PERIOD, pi);


    }
}
