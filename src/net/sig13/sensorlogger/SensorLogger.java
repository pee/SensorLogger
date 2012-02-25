//
//
//
package net.sig13.sensorlogger;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import net.sig13.sensorlogger.cp.PressureDataTable;
import net.sig13.sensorlogger.cp.SensorContentProvider;
import net.sig13.sensorlogger.prefs.MainPrefsActivity;
import net.sig13.sensorlogger.prefs.PollingFragment;

//
//
//
public class SensorLogger extends Activity {

    private final static String TAG = "SensorLogger";
    //
    public static final String PREFS_NAME = "SensorLoggerPrefs";
    //
    private SimpleCursorAdapter adapter;
    private LoaderManager lm;
    private FragmentManager fm;
    private ComponentName cName;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(TAG, "onCreate");

        // last argument == false == don't replace known prefs
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        PreferenceManager.setDefaultValues(this, R.xml.pref_polling, true);

        Intent intent = new Intent(this, SensorLoggerService.class);

        cName = startService(intent);
        Log.d(TAG, "cName:" + cName);

    }

    /*
     *
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(TAG, "onCreateOptionsMenu");

        menu.add(Menu.NONE, 0, 0, "settings");
        return super.onCreateOptionsMenu(menu);
    }

    /*
     *
     *
     */
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

        // The activity is about to become visible.

        Log.d(TAG, "onStart");

        super.onStart();

        fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        SensorDataListFragment sdlf = new SensorDataListFragment();
        ft.add(android.R.id.content, sdlf, "sdlf");

        ft.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        // The activity is about to be destroyed.
    }
}
