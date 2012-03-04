//
//
//
package net.sig13.sensorlogger.cp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PressureSensorDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "SLogger:SQLiteOpenHelper";
    //
    private static final String DATABASE_NAME = "pressuredata.db";
    private static final int DATABASE_VERSION = 3;

    public PressureSensorDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(TAG, "onCreate");
        PressureDataTable.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade");
        PressureDataTable.onUpgrade(database, oldVersion, newVersion);
    }
}
