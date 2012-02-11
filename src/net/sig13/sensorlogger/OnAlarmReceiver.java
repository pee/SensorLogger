//
//
//
package net.sig13.sensorlogger;

import android.content.*;
import android.util.Log;

/**
 *
 * @author pee
 */
public class OnAlarmReceiver extends BroadcastReceiver {

    private final static String LOG_NAME = "SensorLoggerService:OAReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LOG_NAME, "onReceive");

        SensorLoggerService.acquireStaticLock(context);

        Log.d(LOG_NAME, "starting SensorLoggerService");
        context.startService(new Intent(context, SensorLoggerService.class));

    }
}
