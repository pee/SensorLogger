//
//
//
package net.sig13.sensorlogger;

import android.app.ActionBar.Tab;
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
public class SensorLogger extends Activity implements ActionBar.TabListener {

    private final static String TAG = "SLogger";
    //
    public static final String PREFS_NAME = "SensorLoggerPrefs";
    //
    private static final String SENSOR_DATA_LIST_FRAG_TAG = "sdlf";
    //
//    private SimpleCursorAdapter adapter;
//    private LoaderManager lm;
//    private FragmentManager fm;
    private ComponentName cName;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        Log.d(TAG, "onCreate");

        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);

//        fm = getFragmentManager();

        // last argument == false == don't replace known prefs
        PreferenceManager.setDefaultValues(this, R.xml.pref_polling, true);

        Intent intent = new Intent(this, SensorLoggerService.class);

        cName = startService(intent);
        Log.d(TAG, "cName:" + cName);

//        FragmentTransaction ft = fm.beginTransaction();
//
//        SensorDataListFragment sdlf = new SensorDataListFragment();
//        ft.add(android.R.id.content, sdlf, SENSOR_DATA_LIST_FRAG_TAG);
//        ft.commit();

        Tab tab = actionBar.newTab();
        tab.setText("readings");
        tab.setTabListener(new TabListener<SensorDataListFragment>(this, "readings", SensorDataListFragment.class));
        actionBar.addTab(tab);



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

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
}
