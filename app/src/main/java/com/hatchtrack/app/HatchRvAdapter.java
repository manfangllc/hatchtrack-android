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

import com.hatchtrack.app.database.HatchTable;
import com.hatchtrack.app.database.HatchtrackProvider;


public class HatchRvAdapter extends RvCursorAdapter<HatchItemViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private LoaderManager loaderManager;
    private HatchListFragment.HatchClickListener clickListener;

    HatchRvAdapter(Context context, LoaderManager lm, HatchListFragment.HatchClickListener listener) {
        super();
        this.clickListener = listener;
        this.context = context;
        this.loaderManager = lm;
        this.loaderManager.initLoader(Globals.LOADER_ID_HATCHLIST_HATCHTABLE, null, this);
    }

    @NonNull
    @Override
    public HatchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_hatch, parent, false);
        HatchItemViewHolder holder = new HatchItemViewHolder(v, this.clickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(HatchItemViewHolder holder, Cursor cursor) {
        holder.setDbId(cursor.getInt(cursor.getColumnIndex(HatchTable.ID)));
        holder.name = cursor.getString(cursor.getColumnIndex(HatchTable.NAME));
        holder.refresh();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> result = null;
        String select = null;
        String sort = HatchTable.CREATED + " DESC";
        if(args != null){
            select = args.getString(Globals.KEY_SELECT);
            sort = args.getString(Globals.KEY_SORT);
        }
        switch (id) {
            case Globals.LOADER_ID_HATCHLIST_HATCHTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.context,
                        // Table to query
                        HatchtrackProvider.HATCH_URI,
                        // Projection
                        null,
                        // Selection
                        select,
                        // Selection arguments
                        null,
                        // Sort order
                        sort
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
            case Globals.LOADER_ID_HATCHLIST_HATCHTABLE:
//                this.activity.invalidateOptionsMenu();
                this.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_HATCHLIST_HATCHTABLE:
                this.swapCursor(null);
                break;
        }
    }

}
