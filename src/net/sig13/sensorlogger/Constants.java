//
//
//
package net.sig13.sensorlogger;

// this is ugly, kill me
public class Constants {

    public final static String SHARED_PREFS_FILE = "net.sig13.sensorlogger_preferences";
    public final static String SHARED_PREFS_KEY = "preferences";
    //
    public final static int MIN_POLLING_DELAY = 60000; // 1 minute
    public final static int MAX_POLLING_DELAY = 60 * 60 * 1000; // 1hr
    public final static int DEFAULT_POLLING_DELAY = 60000; // 1 minutes
    public final static String DEFAULT_POLLING_DELAY_STRING = "60000";
    public final static int PAUSE_POLLING_DELAY = 300000;  // 5 minutes
    //
    //
    public final static String PREF_KEY_ENABLE_POLLING = "enablePolling";
    public final static String PREF_KEY_LOW_BATTERY_STOP = "lowBatteryStop";
    public final static String PREF_KEY_ENABLE_SYNC = "enableSync";
    public final static String PREF_KEY_LOW_BATTERY_SYNC_STOP = "lowBatterySyncStop";
    public final static String PREF_KEY_POLLING_INTERVAL = "pollingInterval";
    public final static String PREF_KEY_STORAGE_TIME = "storageTime";
    //
    public final static String PREF_KEY_OPENSENSE_ENABLE = "enableOpenSenSe";
    public final static String PREF_KEY_OPENSENSE_API_KEY = "openSenSeAPIKey";
    public final static String PREF_KEY_OPENSENSE_FEED_ID = "openSenSeFeedID";
    public final static String PREF_KEY_OPENSENSE_API_URI = "openSenSeAPIUri";
    public final static String PREF_DEFAULT_OPENSENSE_API_URI = "http://api.sen.se/events/";
    public final static String PREF_DEFAULT_OPENSENSE_FEED_ID = "00000";
    public final static String PREF_DEFAULT_OPENSENSE_API_KEY = "XXXXXXXXXXXXXXXXX";
    public final static boolean PREF_DEFAULT_OPENSENSE_ENABLE = false;
    //
    public final static String PREF_KEY_ENABLE_LOCATION = "enableLocation";
    public final static boolean PREF_DEFAULT_ENABLE_LOCATION = false;
    public final static boolean PREF_DEFAULT_ENABLE_SYNC = false;
    public final static int DEFAULT_LOCATION_DELAY = 60 * 5 * 1000; // 5 minutes
    //
    //
    //
    public final static int DB_VERSION = 1;
    public final static String DB_NAME = "readings";
}
