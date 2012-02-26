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

    private final static String LOG_NAME = "SLogger:MainPrefsActivity";
    //
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

    /**
     *
     * @param preference
     * @param arg1
     * @return
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object arg1) {
        Log.d(LOG_NAME, "onPreferenceChange");
        return true;
    }

    /**
     *
     * @param preference
     * @return
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.d(LOG_NAME, "onPreferenceClic");
        return true;
    }
}
