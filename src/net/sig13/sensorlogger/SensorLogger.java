//
//
//
package net.sig13.sensorlogger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.util.Map;
import java.util.Set;
import net.sig13.sensorlogger.prefs.MainPrefsActivity;

//
//
//
public class SensorLogger extends Activity {

    private final static String LN = "SensorLogger";
    public static final String PREFS_NAME = "SensorLoggerPrefs";

    //private PreferenceManager pm;
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(LN, "onCreate");

        // last argument == false == don't replace known prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Map<String, ?> allPrefs = prefs.getAll();

//        Log.d(LN, "onCreate:dumping keys");
//        Set<String> keys = allPrefs.keySet();
//        for (String key : keys) {
//            Log.i(LN, key + ":" + allPrefs.get(key).getClass()); // Dummy , put break point here
//        }
//        Log.d(LN, "onCreate:done dumping keys");


        PreferenceManager.setDefaultValues(this, R.xml.pref_polling, true);

//        Log.d(LN, "onCreate:dumping keys");
//        allPrefs = prefs.getAll();
//
//        keys = allPrefs.keySet();
//        for (String key : keys) {
//            Log.i(LN, key + ":" + allPrefs.get(key).getClass()); // Dummy , put break point here
//        }
//        Log.d(LN, "onCreate:done dumping keys");

        Intent intent = new Intent(this, SensorLoggerService.class);

        startService(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(Menu.NONE, 0, 0, "settings");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {

            case 0:
                showOptions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showOptions() {
        Intent intent = new Intent(this, MainPrefsActivity.class);
        this.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(LN, "onStart");
        // The activity is about to become visible.
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LN, "onResume");
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LN, "onPause");
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(LN, "onStop");
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(LN, "onDestroy");
        // The activity is about to be destroyed.
    }
}
