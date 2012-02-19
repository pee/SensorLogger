//
//
//
//
package net.sig13.sensorlogger.prefs;

import android.R;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.*;
import android.util.Log;

//
public class MainPrefsActivity extends PreferenceActivity {

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
}
