//
//
//
package net.sig13.sensorlogger;

import android.app.backup.*;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefsBackupAgent extends BackupAgentHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_NAME = "SensorLogger:PrefsBackupAgent";
    private SharedPreferences prefs;
    private BackupManager bm;

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {

        Log.d(LOG_NAME, "onCreate");

        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, Constants.SHARED_PREFS_BACKUP_KEY);
        addHelper(Constants.SHARED_PREFS_FILE, helper);

        Log.d(LOG_NAME, "getSharedPreferences");
        //prefs = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        //prefs.registerOnSharedPreferenceChangeListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);



        bm = new BackupManager(this.getBaseContext());

    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

        Log.d(LOG_NAME, "onSharedPreferenceChanged");
        Log.d(LOG_NAME, "prefs:" + pref);
        Log.d(LOG_NAME, "key:" + key);

        bm.dataChanged();



    }
}
