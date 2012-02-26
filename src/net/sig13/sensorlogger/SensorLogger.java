//
//
//
package net.sig13.sensorlogger;

import android.app.*;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import net.sig13.sensorlogger.prefs.MainPrefsActivity;

//
//
//
public class SensorLogger extends Activity {

    private final static String TAG = "SLogger";
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
        PreferenceManager.setDefaultValues(this, R.xml.pref_polling, true);

        Intent intent = new Intent(this, SensorLoggerService.class);

        cName = startService(intent);
        Log.d(TAG, "cName:" + cName);

        fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        SensorDataListFragment sdlf = new SensorDataListFragment();
        ft.add(android.R.id.content, sdlf, "sdlf");
        ft.commit();

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
}
