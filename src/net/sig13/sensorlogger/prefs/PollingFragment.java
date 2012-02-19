//
//
//
package net.sig13.sensorlogger.prefs;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import net.sig13.sensorlogger.R;

//
//
//
public class PollingFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String LOG_NAME = "SL:PollingFragment";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(LOG_NAME, "onCreate()");

        addPreferencesFromResource(R.xml.pref_polling);

    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object arg1) {
        Log.d(LOG_NAME, "onPreferenceChange()");
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        Log.d(LOG_NAME, "onPreferenceClick()");
        return true;
    }
}
