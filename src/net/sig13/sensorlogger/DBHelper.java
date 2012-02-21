//
//
//
package net.sig13.sensorlogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String LOG_NAME = "SL:SQLiteOpenHelper";
    private static final String DB_CREATE =
            "create table readings (_id integer primary key autoincrement, "
            + "time integer not null, "
            + "value real not null"
            + " );";

    DBHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);

        Log.d(LOG_NAME, "constructor");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(LOG_NAME, "onCreate");
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d(LOG_NAME, "onUpgrade:" + oldVersion + ":" + newVersion);

    }
}
