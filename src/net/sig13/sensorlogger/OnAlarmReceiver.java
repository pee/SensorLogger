//
//
//
package net.sig13.sensorlogger;

import android.content.*;
import android.util.Log;

/**
 *
 *
 */
public class OnAlarmReceiver extends BroadcastReceiver {

    private final static String TAG = "SLoggerService:OAReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive");

        SensorLoggerService.acquireStaticLock(context);

        Log.d(TAG, "starting SensorLoggerService");
        context.startService(new Intent(context, SensorLoggerService.class));

    }
}
