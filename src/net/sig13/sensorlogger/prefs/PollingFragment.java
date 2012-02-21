//
//
//
package net.sig13.sensorlogger.prefs;

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import net.sig13.sensorlogger.Constants;
import net.sig13.sensorlogger.R;

//
//
//
public class PollingFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String LOG_NAME = "SL:PollingFragment";
    private CheckBoxPreference enablePolling;
    private ListPreference pollingInterval;
    private PreferenceManager pm;
    private SharedPreferences prefs;
    private BackupManager bm;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(LOG_NAME, "onCreate()");

        addPreferencesFromResource(R.xml.pref_polling);
        pm = this.getPreferenceManager();
        prefs = pm.getSharedPreferences();


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(LOG_NAME, "onActivityCreated");

        enablePolling = (CheckBoxPreference) findPreference(Constants.PREF_KEY_ENABLE_POLLING);
        enablePolling.setOnPreferenceChangeListener(this);

        pollingInterval = (ListPreference) findPreference(Constants.PREF_KEY_POLLING_INTERVAL);
        pollingInterval.setOnPreferenceChangeListener(this);


    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {

        Log.d(LOG_NAME, "onPreferenceChange()");

        String key = pref.getKey();
        Editor editor;

        // can't have a null here
        if (key == null) {
            return false;
        }

        // can't have a null here either in this instance so fuck that
        if (newValue == null) {
            return false;
        }

        editor = prefs.edit();
        if (key.equalsIgnoreCase(Constants.PREF_KEY_ENABLE_POLLING)) {
            Log.d(LOG_NAME, "enablePolling:" + newValue);
            if (newValue instanceof Boolean) {
                Log.d(LOG_NAME, "updateing enablePolling preference");
                editor.putBoolean(Constants.PREF_KEY_ENABLE_POLLING, Boolean.getBoolean(newValue.toString()));

            } else {
                Log.d(LOG_NAME, "wanted a boolean preference got:" + newValue.getClass());
            }

        } else if (key.equalsIgnoreCase(Constants.PREF_KEY_POLLING_INTERVAL)) {
            Log.d(LOG_NAME, "pollingInterval:" + newValue);
        } else {
            Log.w(LOG_NAME, "Unknown onPreferenceKeyChange:" + key + ":" + newValue);
        }

        editor.commit();

        
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        Log.d(LOG_NAME, "onPreferenceClick()");
        return true;
    }
}
