//
// some code stolen from: http://www.vogella.de/articles/AndroidSQLite/article.html
//
package net.sig13.sensorlogger.cp;

//
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import java.util.Arrays;
import java.util.HashSet;

//
//
public class SensorContentProvider extends ContentProvider {

    private final static String TAG = "SL:SensorContentProvider:";
    //
    private static final int READINGS = 1;
    private static final int READING_ID = 2;
    //
    public static final String AUTHORITY = "net.sig13.sensorlogger.sensordataprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    //
    private static final String BASE_PATH = "readings";
    //
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_PATH;
    //
    //
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/reading";
    //
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, READINGS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", READING_ID);
    }
    private PressureSensorDBHelper database;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");

        database = new PressureSensorDBHelper(getContext());

        if (database == null) {
            Log.e(TAG, "Failed to create database");
            return false;
        }

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Log.d(TAG, "query");
        Log.d(TAG, "projection:" + projection);
        Log.d(TAG, "selection:" + selection);
        Log.d(TAG, "selectionArgs:" + selectionArgs);
        Log.d(TAG, "sortOrder:" + sortOrder);

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(PressureDataTable.TABLE_PRESSURE);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case READINGS:
                break;
            case READING_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(PressureDataTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        Log.d(TAG, "insert");

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();

        long id = 0;
        switch (uriType) {
            case READINGS:
                id = sqlDB.insert(PressureDataTable.TABLE_PRESSURE, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);

    }

    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        Log.d(TAG, "delete");

        return 0;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        Log.d(TAG, "update");

        return 0;
    }

    private void checkColumns(String[] projection) {

        String[] available = {
            PressureDataTable.COLUMN_TIME,
            PressureDataTable.COLUMN_VALUE,
            PressureDataTable.COLUMN_ID};

        if (projection != null) {

            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));

            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
