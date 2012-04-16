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
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.SearchView.OnQueryTextListener;
import net.sig13.sensorlogger.cp.PressureDataTable;
import net.sig13.sensorlogger.cp.SensorContentProvider;

public class SensorDataListFragment extends ListFragment
        implements
        OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = "SLogger:SDataListFrag";
    // This is the Adapter being used to display the list's data.
    private SimpleCursorAdapter mAdapter;
    //
    private LoaderManager lm;
    //
    //
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
                R.layout.reading_row, null,
                new String[]{PressureDataTable.COLUMN_TIME, PressureDataTable.COLUMN_VALUE},
                new int[]{R.id.rr_text1, R.id.rr_text2}, 0);

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

    /**
     *
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {

        Log.d(TAG, "onQueryTextChange");

        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        //mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    /**
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit");
        // Don't care about this.
        return true;
    }

//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//
//        Log.d(TAG, "oonListItemClick");
//
//        // Insert desired behavior here.
//        Log.i(TAG, "Item clicked: " + id);
//    }
    /**
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(TAG, "onCreateLoader");

        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;

        baseUri = Uri.parse("content://" + SensorContentProvider.AUTHORITY + "/readings");

        //CursorLoader cl = new CursorLoader(getActivity(), baseUri, SENSORDATA_SUMMARY_PROJECTION, null, null, null);
        CursorLoader cl = new CursorLoader(getActivity(), baseUri, null, null, null, null);

        return cl;

    }

    /**
     *
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        Log.d(TAG, "onLoadFinished");

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(cursor);

        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    /**
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        Log.d(TAG, "onLoaderReset");

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "OnDestroyView()");
        
        lm.destroyLoader(0);

    }
}