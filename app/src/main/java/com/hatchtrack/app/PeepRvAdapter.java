package com.hatchtrack.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.PeepTable;


public class PeepRvAdapter extends RvCursorAdapter<PeepItemViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private LoaderManager loaderManager;
    private PeepListFragment.PeepClickListener clickListener;

    public PeepRvAdapter(Context context, LoaderManager lm, PeepListFragment.PeepClickListener listener) {
        super();
        this.clickListener = listener;
        this.context = context;
        this.loaderManager = lm;
        this.loaderManager.initLoader(Globals.LOADER_ID_PEEPLIST_PEEPTABLE, null, this);
    }

    @NonNull
    @Override
    public PeepItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_peep, parent, false);
        PeepItemViewHolder holder = new PeepItemViewHolder(v, this.clickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(PeepItemViewHolder holder, Cursor cursor) {
        String s = "ID=" + cursor.getInt(cursor.getColumnIndex(PeepTable.ID)) + ": " + cursor.getString(cursor.getColumnIndex(PeepTable.NAME));
        holder.title.setText(s);
        holder.dbId = cursor.getInt(cursor.getColumnIndex(PeepTable.ID));
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> result = null;

        switch (id) {
            case Globals.LOADER_ID_PEEPLIST_PEEPTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.context,
                        // Table to query
                        HatchtrackProvider.PEEP_URI,
                        // Projection
                        null,
                        // Selection
                        null,
                        // Selection arguments
                        null,
                        // Sort order
                        null
                );
                break;
            default:
                // An invalid id was passed in. Things are gonna...
                break;
        }
        return (result);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_PEEPLIST_PEEPTABLE:
//                this.activity.invalidateOptionsMenu();
                this.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_PEEPLIST_PEEPTABLE:
                this.swapCursor(null);
                break;
        }
    }

}
