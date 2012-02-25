//
//
//
//
//
//
//
//
//
//
package net.sig13.sensorlogger;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.*;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import net.sig13.sensorlogger.cp.PressureDataTable;
import net.sig13.sensorlogger.cp.SensorContentProvider;

public class SensorDataListFragment extends ListFragment implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = "SL:SensorDataListFrag";
    // This is the Adapter being used to display the list's data.
    private SimpleCursorAdapter mAdapter;
    // If non-null, this is the current filter the user has provided.
    private String mCurFilter;
    //
    private LoaderManager lm;
    //
    //
    // These are the Contacts rows that we will retrieve.
    static final String[] SENSORDATA_SUMMARY_PROJECTION = new String[]{
        PressureDataTable.COLUMN_ID,
        PressureDataTable.COLUMN_TIME,
        PressureDataTable.COLUMN_VALUE
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("No sensor data found");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_2, null,
                new String[]{PressureDataTable.COLUMN_TIME, PressureDataTable.COLUMN_VALUE},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);

        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        lm = getLoaderManager();
        lm.initLoader(0, null, this);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);

        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");

        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView sv = new SearchView(getActivity());
        sv.setOnQueryTextListener(this);

        item.setActionView(sv);
    }

    public boolean onQueryTextChange(String newText) {

        Log.d(TAG, "onQueryTextChange");

        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit");
        // Don't care about this.
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Log.d(TAG, "oonListItemClick");

        // Insert desired behavior here.
        Log.i(TAG, "Item clicked: " + id);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(TAG, "onCreateLoader");

        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
//        if (mCurFilter != null) {
//            baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(mCurFilter));
//        } else {
//            baseUri = Contacts.CONTENT_URI;
//        }
        baseUri = Uri.parse(SensorContentProvider.AUTHORITY + "/readings");

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
//        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
//                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
//                + Contacts.DISPLAY_NAME + " != '' ))";

//        return new CursorLoader(getActivity(), baseUri,
//                SENSORDATA_SUMMARY_PROJECTION, select, null,
//                Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

        //CursorLoader cl = new CursorLoader(getActivity(), baseUri, SENSORDATA_SUMMARY_PROJECTION, null, null, null);
        CursorLoader cl = new CursorLoader(getActivity(), baseUri, null, null, null, null);

        return cl;

    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.d(TAG, "onLoadFinished");

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {

        Log.d(TAG, "onLoaderReset");

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}