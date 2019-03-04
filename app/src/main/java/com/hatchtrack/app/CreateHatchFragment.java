package com.hatchtrack.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.SpeciesTable;

import java.util.HashMap;
import java.util.Map;

public class CreateHatchFragment extends Fragment implements Braggable, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = CreateHatchFragment.class.getSimpleName();

    public interface CreateHatchListener {
        void onCreateHatch(int dbId);
    }

    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private RecyclerView hatchListView;
    private LoaderManager loaderManager;
    private boolean needLoaders = true;
    int[] speciesIds = new int[0];
    String[] speciesNames = new String[0];
    float[] speciesDays = new float[0];
    Map<Integer, String> speciesPicMap = new HashMap<>();

    public CreateHatchFragment() {
        Log.i(TAG, "HatchListFragment(): new");
    }

    public static CreateHatchFragment newInstance(CollapsingToolbarLayout ctl, AppBarLayout abl, ImageView iv) {
        CreateHatchFragment fragment = new CreateHatchFragment();
        fragment.toolbarLayout = ctl;
        fragment.appBarLayout = abl;
        fragment.imageView = iv;
        return(fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.loaderManager == null) {
            this.loaderManager = this.getActivity().getSupportLoaderManager();
        }
        if(this.needLoaders){
            this.loaderManager.initLoader(Globals.LOADER_ID_CREATEHATCH_SPECIESTABLE, null, this);
            this.needLoaders = false;
        } else {
            this.loaderManager.restartLoader(Globals.LOADER_ID_CREATEHATCH_SPECIESTABLE, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.toolbarLayout.setTitle("Create A Hatch");
        this.imageView.setImageResource(R.drawable.hatch_1);
        this.appBarLayout.setExpanded(true);

        View rootView = inflater.inflate(R.layout.frag_create_hatch, container, false);
        Context context = this.getContext();
        if(context != null) {
            // new hatch button
            rootView.findViewById(R.id.newHatchButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment f = CreateHatchFragment.this.getFragmentManager().findFragmentByTag("SpeciesDialog");
                    if(f == null) {
//                        DialogChooseSpecies d = new DialogCreateHatch();
//                        Bundle b = new Bundle();
//                        b.putInt(Globals.KEY_HATCH_ID, HatchFragment.this.hatchId);
//                        b.putIntArray(Globals.KEY_SPECIES_IDS, HatchFragment.this.speciesIds);
//                        b.putFloatArray(Globals.KEY_SPECIES_DAYS, HatchFragment.this.speciesDays);
//                        b.putStringArray(Globals.KEY_SPECIES_NAMES, HatchFragment.this.speciesNames);
//                        int[] ids = new int[HatchFragment.this.speciesPicMap.size()];
//                        String[] files = new String[HatchFragment.this.speciesPicMap.size()];
//                        int i = 0;
//                        for (Integer id : HatchFragment.this.speciesPicMap.keySet()) {
//                            ids[i] = id;
//                            files[i] = HatchFragment.this.speciesPicMap.get(id);
//                            i++;
//                        }
//                        b.putIntArray(Globals.KEY_SPECIES_PICS_IDS, ids);
//                        b.putStringArray(Globals.KEY_SPECIES_PICS_STRINGS, files);
//                        d.setArguments(b);
//                        d.show(HatchFragment.this.getFragmentManager(), "SpeciesDialog");
                    }
                }
            });
         }
        return(rootView);
    }

    @Override
    public void onVisible() {
        this.toolbarLayout.setTitle("New Hatch...");
        this.imageView.setImageResource(R.drawable.hatch_1);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> result = null;
        switch (id) {
            case Globals.LOADER_ID_CREATEHATCH_SPECIESTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.getContext(),
                        // Table to query
                        HatchtrackProvider.SPECIES_URI,
                        // Projection
                        new String[]{SpeciesTable.ID, SpeciesTable.NAME, SpeciesTable.DAYS, SpeciesTable.PICTURE_URI},
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
        return result;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_CREATEHATCH_SPECIESTABLE:
                // all the species
                this.speciesIds = new int[cursor.getCount()];
                this.speciesDays = new float[cursor.getCount()];
                this.speciesNames = new String[cursor.getCount()];
                this.speciesPicMap.clear();
                if(cursor.moveToFirst()) {
                    int i = 0;
                    while(!cursor.isAfterLast()) {
                        this.speciesIds[i] = cursor.getInt(cursor.getColumnIndex((SpeciesTable.ID)));
                        this.speciesNames[i] = cursor.getString(cursor.getColumnIndex((SpeciesTable.NAME)));
                        this.speciesDays[i] = cursor.getFloat(cursor.getColumnIndex((SpeciesTable.DAYS)));
                        String s = cursor.getString(cursor.getColumnIndex((SpeciesTable.PICTURE_URI)));
                        if(s != null) {
                            this.speciesPicMap.put((this.speciesIds[i]), s);
                        }
                        i++;
                        cursor.moveToNext();
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_CREATEHATCH_SPECIESTABLE:
                break;
        }
    }

}
