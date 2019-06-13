package com.hatchtrack.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.hatchtrack.app.database.Data;
import com.hatchtrack.app.database.HatchTable;
import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.PeepTable;

public class PeepFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = PeepFragment.class.getSimpleName();

    private Context context;
    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;

    LoaderManager loaderManager;
    private int dbId;
    private String name;

    private EditText textView;


    public PeepFragment() {
    }

    public static PeepFragment newInstance(CollapsingToolbarLayout ctl, AppBarLayout abl, ImageView iv, FloatingActionButton fab, CoordinatorLayout mc) {
        PeepFragment fragment = new PeepFragment();
        fragment.toolbarLayout = ctl;
        fragment.appBarLayout = abl;
        fragment.imageView = iv;
        fragment.fab = fab;
        fragment.mainCoordinator = mc;
        return(fragment);
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        if(args != null){
            this.dbId = args.getInt(Globals.KEY_DBID, 0);
            if(this.loaderManager != null){
                this.loaderManager.restartLoader(Globals.LOADER_ID_PEEP_PEEPTABLE, null, this);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getContext();
        if(this.loaderManager == null){
            this.loaderManager = this.getActivity().getSupportLoaderManager();
            this.loaderManager.initLoader(Globals.LOADER_ID_PEEP_PEEPTABLE, null, this);
        } else {
            this.loaderManager.restartLoader(Globals.LOADER_ID_PEEP_PEEPTABLE, null, this);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.toolbarLayout.setTitle("Peep");
        this.imageView.setImageResource(R.drawable.peep_1);
        this.appBarLayout.setExpanded(true);

        View rootView = inflater.inflate(R.layout.frag_peep, container, false);
        this.textView = rootView.findViewById(R.id.text);
        this.refresh();
        Log.i(TAG, Data.dumpTable(this.context, HatchtrackProvider.PEEP_URI));
        return(rootView);
    }

    private void refresh(){
        if(this.textView != null){
            if(this.dbId == 0){
                this.textView.setText("This should be some peep info, but there's no database ID for this peep.");
            }
            else {
//                String tables[] = Data.getTableNames(this.context);
                this.textView.setText(Data.getRowText(this.context, HatchtrackProvider.PEEP_URI, this.dbId));
            }
        }
        if(this.toolbarLayout != null){
            this.toolbarLayout.setTitle(this.name);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> result = null;

        switch (id) {
            case Globals.LOADER_ID_PEEP_PEEPTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.context,
                        // Table to query
                        HatchtrackProvider.PEEP_URI,
                        // Projection
                        null,
                        // Selection
                        PeepTable.ID + " = " + this.dbId,
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
        return result;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_PEEP_PEEPTABLE:
                if(cursor.moveToFirst()) {
                    this.name = cursor.getString(cursor.getColumnIndex((HatchTable.NAME)));
                    this.refresh();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_PEEP_PEEPTABLE:
                break;
        }
    }
}
