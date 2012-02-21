//
//
//
//
package net.sig13.sensorlogger.prefs;

import android.R;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.*;
import android.util.Log;

//
public class MainPrefsActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private final static String LOG_NAME = "SL:MainPrefsActivity";
    private FragmentManager fm;
    private PreferenceManager pm;
    private PreferenceScreen root;

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(LOG_NAME, "onCreate()");

        // ToDo add your GUI initialization code here
        fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        PollingFragment pf = new PollingFragment();
        ft.add(R.id.content, pf, "polling");

        ft.commit();

        pm = getPreferenceManager();
        root = pm.createPreferenceScreen(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(LOG_NAME, "onStart");
        // The activity is about to become visible.
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_NAME, "onResume");
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_NAME, "onPause");
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(LOG_NAME, "onStop");
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(LOG_NAME, "onDestroy");
        // The activity is about to be destroyed.
    }

    public boolean onPreferenceChange(Preference preference, Object arg1) {
        Log.d(LOG_NAME, "onPreferenceChange");
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        Log.d(LOG_NAME, "onPreferenceClic");
        return true;
    }
}
