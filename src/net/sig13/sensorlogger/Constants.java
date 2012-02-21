//
//
//
package net.sig13.sensorlogger;

// this is ugly, kill me
public class Constants {

    public final static String SHARED_PREFS_FILE = "user_preferences";
    public final static String SHARED_PREFS_BACKUP_KEY = "prefs";
    //
    public final static int MIN_POLLING_DELAY = 60000; // 1 minute
    public final static int MAX_POLLING_DELAY = 60 * 60 * 1000; // 1hr
    public final static int DEFAULT_POLLING_DELAY = 60000; // 1 minutes
    public final static String DEFAULT_POLLING_DELAY_STRING = "60000";

    //public final static int DEFAULT_POLLING_DELAY = 300000; // 5 minutes - change me for not debug
    public final static int PAUSE_POLLING_DELAY = 300000;  // 5 minutes
    //public final static String POLLING_DELAY_PREFNAME = "pollingDelay";
    public final static String PREF_KEY_ENABLE_POLLING = "enable_polling";
    public final static String PREF_KEY_POLLING_INTERVAL = "polling_interval";

    public final static int DB_VERSION = 1;
    public final static String DB_NAME = "readings";

}
