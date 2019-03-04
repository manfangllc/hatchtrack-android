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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.hatchtrack.app.database.HatchTable;
import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.SpeciesTable;

import java.util.HashMap;
import java.util.Map;

public class HatchListFragment extends Fragment implements Braggable, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = HatchListFragment.class.getSimpleName();

    private static final String[] SELECTS = new String[]{
            null,
            HatchTable.START + " = 0",
            "(" + HatchTable.START + " > 0) AND (" + HatchTable.END + " = 0)",
            HatchTable.END + " > 0"
    };
    private static final String[] SORTS = new String[]{
            HatchTable.CREATED + " DESC",
            HatchTable.CREATED + " ASC"
    };

    public interface HatchClickListener {
        void onHatchClicked(int dbId);
    }

    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private RecyclerView hatchListView;
    private HatchClickListener clickListener;
    private LoaderManager loaderManager;
    private boolean needLoaders = true;
    private HatchRvAdapter rvAdapter;
    private String selection = SELECTS[0];
    private String sort = SORTS[0];
    int[] speciesIds = new int[0];
    String[] speciesNames = new String[0];
    float[] speciesDays = new float[0];
    Map<Integer, String> speciesPicMap = new HashMap<>();
    private CreateHatchFragment createHatchFrag;

    public HatchListFragment() {
        Log.i(TAG, "HatchListFragment(): new");
    }

    public static HatchListFragment newInstance(HatchClickListener listener, CollapsingToolbarLayout ctl, AppBarLayout abl, ImageView iv) {
        HatchListFragment fragment = new HatchListFragment();
        fragment.clickListener = listener;
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
            this.loaderManager.initLoader(Globals.LOADER_ID_HATCHLIST_SPECIESTABLE, null, this);
            this.needLoaders = false;
        } else {
            this.loaderManager.restartLoader(Globals.LOADER_ID_HATCHLIST_SPECIESTABLE, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.toolbarLayout.setTitle("Hatches");
        this.imageView.setImageResource(R.drawable.hatch_1);
        this.appBarLayout.setExpanded(true);

        View rootView = inflater.inflate(R.layout.frag_hatch_list, container, false);
        Context context = this.getContext();
        if(context != null) {
            // new hatch button
            rootView.findViewById(R.id.newHatchButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(HatchListFragment.this.createHatchFrag == null){
                        HatchListFragment.this.createHatchFrag = CreateHatchFragment.newInstance(
                                HatchListFragment.this.toolbarLayout,
                                HatchListFragment.this.appBarLayout,
                                HatchListFragment.this.imageView
                        );
                    }
                }
            });
            // the recycler
            this.hatchListView = rootView.findViewById(R.id.hatchListId);
            this.hatchListView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
            this.rvAdapter = new HatchRvAdapter(this.getActivity(), this.loaderManager, this.clickListener);
            this.hatchListView.setAdapter(this.rvAdapter);
            // selection spinner
            Spinner spinnerSelect = rootView.findViewById(R.id.spinnerSelect);
            spinnerSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private int prevPosition;
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(this.prevPosition != position) {
                        this.prevPosition = position;
                        HatchListFragment.this.selection = SELECTS[position];
                        HatchListFragment.this.reloadData();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    HatchListFragment.this.selection = SELECTS[0];
                    HatchListFragment.this.reloadData();
                }
            });
            ArrayAdapter aa = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, this.getResources().getStringArray(R.array.selection_choices));
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSelect.setAdapter(aa);
            // sort spinner
            Spinner spinnerSort = rootView.findViewById(R.id.spinnerSort);
            spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private int prevPosition;
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(this.prevPosition != position) {
                        this.prevPosition = position;
                        HatchListFragment.this.sort = SORTS[position];
                        HatchListFragment.this.reloadData();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    HatchListFragment.this.sort = SORTS[0];
                    HatchListFragment.this.reloadData();
                }
            });
            aa = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, this.getResources().getStringArray(R.array.sort_choices));
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSort.setAdapter(aa);
        }
        return(rootView);
    }

    @Override
    public void onVisible() {
        this.toolbarLayout.setTitle("Hatches");
        this.imageView.setImageResource(R.drawable.hatch_1);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> result = null;
        switch (id) {
            case Globals.LOADER_ID_HATCHLIST_SPECIESTABLE:
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
            case Globals.LOADER_ID_HATCHLIST_SPECIESTABLE:
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
            case Globals.LOADER_ID_HATCH_SPECIESTABLE:
                break;
        }
    }

    private void reloadData(){
        Bundle b = new Bundle();
        b.putString(Globals.KEY_SELECT, this.selection);
        b.putString(Globals.KEY_SORT, this.sort);
        this.loaderManager.restartLoader(Globals.LOADER_ID_HATCHLIST_HATCHTABLE, b, this.rvAdapter);
    }
}
