//
//
//
package net.sig13.sensorlogger;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class PrefsBackupAgent extends BackupAgentHelper {


    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, Constants.SHARED_PREFS_BACKUP_KEY);
        addHelper(Constants.SHARED_PREFS_FILE , helper);
    }
}
