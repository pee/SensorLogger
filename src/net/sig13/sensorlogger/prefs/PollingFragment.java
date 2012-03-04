//
//
//
package net.sig13.sensorlogger.prefs;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.*;
import android.util.Log;
import net.sig13.sensorlogger.Constants;
import net.sig13.sensorlogger.R;

//
//
//  TODO: There are only 2 kinds of changed prefs here int/boolean right now we can combine the logic in he
//  changed prefs calls and and an argument for the name
//
//
public class PollingFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "SLogger:PollingFragment";
    //
    private CheckBoxPreference enablePolling;
    private CheckBoxPreference lowBatteryStop;
    private CheckBoxPreference enableSync;
    private CheckBoxPreference lowBatterySyncStop;
    private CheckBoxPreference enableLocation;
    private ListPreference pollingInterval;
    private ListPreference storageTime;
    //
    private PreferenceManager pm;
    private SharedPreferences prefs;
    private BackupManager bm;
    private Activity activity;
    private Context context;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d(TAG, "onCreate");

        addPreferencesFromResource(R.xml.pref_polling);

        pm = getPreferenceManager();
        prefs = pm.getSharedPreferences();

        activity = getActivity();
        context = activity.getBaseContext();

        bm = new BackupManager(context);


    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        enablePolling = (CheckBoxPreference) findPreference(Constants.PREF_KEY_ENABLE_POLLING);
        enablePolling.setOnPreferenceChangeListener(this);

        lowBatteryStop = (CheckBoxPreference) findPreference(Constants.PREF_KEY_LOW_BATTERY_STOP);
        lowBatteryStop.setOnPreferenceChangeListener(this);

        enableSync = (CheckBoxPreference) findPreference(Constants.PREF_KEY_ENABLE_SYNC);
        enableSync.setOnPreferenceChangeListener(this);

        lowBatterySyncStop = (CheckBoxPreference) findPreference(Constants.PREF_KEY_LOW_BATTERY_SYNC_STOP);
        lowBatterySyncStop.setOnPreferenceChangeListener(this);

        enableLocation = (CheckBoxPreference) findPreference(Constants.PREF_KEY_ENABLE_LOCATION);
        enableLocation.setOnPreferenceChangeListener(this);

        pollingInterval = (ListPreference) findPreference(Constants.PREF_KEY_POLLING_INTERVAL);
        pollingInterval.setOnPreferenceChangeListener(this);

        storageTime = (ListPreference) findPreference(Constants.PREF_KEY_STORAGE_TIME);
        storageTime.setOnPreferenceChangeListener(this);

    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {

        String key = pref.getKey();
        Log.d(TAG, "onPreferenceChange:" + key + ":");

        boolean worked = true;

        // can't have a null here
        if (key == null) {
            return false;
        }

        // can't have a null here either in this instance so fuck that
        if (newValue == null) {
            return false;
        }

        if (key.equalsIgnoreCase(Constants.PREF_KEY_ENABLE_POLLING)) {

            worked = prefChangedBoolean(pref, newValue, Constants.PREF_KEY_ENABLE_POLLING);

        } else if (key.equalsIgnoreCase(Constants.PREF_KEY_ENABLE_SYNC)) {

            worked = prefChangedBoolean(pref, newValue, Constants.PREF_KEY_ENABLE_SYNC);

        } else if (key.equalsIgnoreCase(Constants.PREF_KEY_LOW_BATTERY_STOP)) {

            worked = prefChangedBoolean(pref, newValue, Constants.PREF_KEY_LOW_BATTERY_STOP);

        } else if (key.equalsIgnoreCase(Constants.PREF_KEY_LOW_BATTERY_SYNC_STOP)) {

            worked = prefChangedBoolean(pref, newValue, Constants.PREF_KEY_LOW_BATTERY_SYNC_STOP);

        } else if (key.equalsIgnoreCase(Constants.PREF_KEY_ENABLE_LOCATION)) {

            worked = prefChangedBoolean(pref, newValue, Constants.PREF_KEY_ENABLE_LOCATION);

        } else if (key.equalsIgnoreCase(Constants.PREF_KEY_POLLING_INTERVAL)) {

            worked = prefChangedInt(pref, newValue, Constants.PREF_KEY_POLLING_INTERVAL);

        } else if (key.equalsIgnoreCase(Constants.PREF_KEY_STORAGE_TIME)) {

            worked = prefChangedInt(pref, newValue, Constants.PREF_KEY_STORAGE_TIME);

        } else {
            Log.w(TAG, "Unknown onPreferenceKeyChange:" + key + ":" + newValue + ":");
        }

        if (worked == false) {
            Log.e(TAG, "Failed to commit preference changes");
        }

        return true;
    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    private boolean prefChangedBoolean(Preference pref, Object newValue, String updateKey) {

        String prefName = pref.getKey();
        Log.d(TAG, prefName + ":" + newValue);

        if (!(newValue instanceof Boolean)) {
            Log.e(TAG, prefName + " wanted a boolean preference got:" + newValue.getClass());
            return false;
        }

        Editor editor = pref.getEditor();

        Log.d(TAG, "updating " + prefName + " preference");
        editor.putBoolean(updateKey, Boolean.getBoolean(newValue.toString()));

        boolean commit = editor.commit();

        Log.d(TAG, "calling dataChanged() for " + prefName);
        bm.dataChanged();

        return commit;

    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    private boolean prefChangedLowBatterySyncStop(Preference pref, Object newValue) {

        String prefName = pref.getKey();
        Log.d(TAG, prefName + ":" + newValue);

        if (!(newValue instanceof Boolean)) {
            Log.e(TAG, prefName + " wanted a boolean preference got:" + newValue.getClass());
            return false;
        }

        Editor editor = pref.getEditor();

        Log.d(TAG, "updating " + prefName + " preference");
        editor.putBoolean(Constants.PREF_KEY_LOW_BATTERY_SYNC_STOP, Boolean.getBoolean(newValue.toString()));

        boolean commit = editor.commit();

        Log.d(TAG, "calling dataChanged() for " + prefName);
        bm.dataChanged();

        return commit;

    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    private boolean prefChangedEnableSync(Preference pref, Object newValue) {

        String prefName = pref.getKey();
        Log.d(TAG, prefName + ":" + newValue);

        if (!(newValue instanceof Boolean)) {
            Log.e(TAG, prefName + " wanted a boolean preference got:" + newValue.getClass());
            return false;
        }

        Editor editor = pref.getEditor();

        Log.d(TAG, "updating " + prefName + " preference");
        editor.putBoolean(Constants.PREF_KEY_ENABLE_SYNC, Boolean.getBoolean(newValue.toString()));

        boolean commit = editor.commit();

        Log.d(TAG, "calling dataChanged() for " + prefName);
        bm.dataChanged();

        return commit;

    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    private boolean prefChangedLowBatteryStop(Preference pref, Object newValue) {

        String prefName = pref.getKey();
        Log.d(TAG, prefName + ":" + newValue);

        if (!(newValue instanceof Boolean)) {
            Log.e(TAG, prefName + " wanted a boolean preference got:" + newValue.getClass());
            return false;
        }

        Editor editor = pref.getEditor();

        Log.d(TAG, "updating " + prefName + " preference");
        editor.putBoolean(Constants.PREF_KEY_LOW_BATTERY_STOP, Boolean.getBoolean(newValue.toString()));

        boolean commit = editor.commit();

        Log.d(TAG, "calling dataChanged() for " + prefName);
        bm.dataChanged();

        return commit;

    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    private boolean prefChangedStorageTime(Preference pref, Object newValue) {

        Log.d(TAG, "storageTime:" + newValue);

        try {
            int newValueAsInt = Integer.parseInt(newValue.toString());
        } catch (Exception e) {
            Log.e(TAG, "storageTime couldn't parse integer from :" + newValue.getClass() + ":");
            return false;
        }

        Editor editor = pref.getEditor();

        Log.d(TAG, "updateing storageTime preference");
        editor.putString(Constants.PREF_KEY_STORAGE_TIME, newValue.toString());

        boolean commit = editor.commit();

        Log.d(TAG, "calling dataChanged() on storageTime");
        bm.dataChanged();

        return commit;
    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    private boolean prefChangedPollingInterval(Preference pref, Object newValue) {

        Log.d(TAG, "pollingInterval:" + newValue);

        try {
            int newValueAsInt = Integer.parseInt(newValue.toString());
        } catch (Exception e) {
            Log.e(TAG, "pollingInterval couldn't parse integer from :" + newValue.getClass() + ":");
            return false;
        }

        Editor editor = pref.getEditor();

        Log.d(TAG, "updateing pollingInterval preference");
        editor.putString(Constants.PREF_KEY_POLLING_INTERVAL, newValue.toString());

        boolean commit = editor.commit();

        Log.d(TAG, "calling dataChanged() on pollingInterval");
        bm.dataChanged();

        return commit;
    }

    /**
     *
     * @param pref
     * @param newValue
     * @return
     */
    private boolean prefChangedEnablePolling(Preference pref, Object newValue) {

        Log.d(TAG, "enablePolling:" + newValue);
        if (!(newValue instanceof Boolean)) {
            Log.e(TAG, "enablePolling wanted a boolean preference got:" + newValue.getClass());
            return false;
        }

        Editor editor = pref.getEditor();

        Log.d(TAG, "updateing enablePolling preference");
        editor.putBoolean(Constants.PREF_KEY_ENABLE_POLLING, Boolean.getBoolean(newValue.toString()));

        boolean commit = editor.commit();

        Log.d(TAG, "calling dataChanged() for enablPolling");
        bm.dataChanged();

        return commit;
    }

    /**
     *
     * @param pref
     * @return
     */
    @Override
    public boolean onPreferenceClick(Preference pref) {
        Log.d(TAG, "onPreferenceClick()");
        return true;
    }
}
