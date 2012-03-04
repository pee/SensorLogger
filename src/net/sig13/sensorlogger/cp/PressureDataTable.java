//
// http://www.vogella.de/articles/AndroidSQLite/article.html
//
package net.sig13.sensorlogger.cp;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//
//
//
public class PressureDataTable {

    private final static String TAG = "SLogger:PressureDataTable";
    // Database table
    public static final String TABLE_PRESSURE = "pressureData";
    //
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "logngitude";
    //
    // Database creation SQL statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_PRESSURE
            + " ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TIME + " integer not null, "
            + COLUMN_VALUE + " real not null,"
            + COLUMN_LATITUDE + " real, "
            + COLUMN_LONGITUDE + " real"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        Log.d(TAG, "onCreate:" + DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        Log.w(PressureDataTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESSURE);
        onCreate(database);

    }
}
