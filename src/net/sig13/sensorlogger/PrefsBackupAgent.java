//
//
//
package net.sig13.sensorlogger;

import android.app.backup.*;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PrefsBackupAgent extends BackupAgentHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SensorLogger:PrefsBackupAgent";
    //
    private SharedPreferences prefs;
    private BackupManager bm;

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate");

        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, Constants.SHARED_PREFS_FILE);
        addHelper(Constants.SHARED_PREFS_KEY, helper);

        Log.d(TAG, "getSharedPreferences");

        //prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs = this.getSharedPreferences(Constants.SHARED_PREFS_FILE, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);

        bm = new BackupManager(getBaseContext());

    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

        Log.d(TAG, "onSharedPreferenceChanged");
        Log.d(TAG, "prefs:" + pref);
        Log.d(TAG, "key:" + key);

        Log.d(TAG, "calling dataChanged()");
        bm.dataChanged();


    }
}
