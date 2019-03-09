package com.hatchtrack.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.SpeciesTable;

import java.util.HashMap;
import java.util.Map;

public class CreateHatchFragment extends Fragment implements Braggable, LoaderManager.LoaderCallbacks<Cursor>, NumberPicker.OnValueChangeListener, TextView.OnEditorActionListener, View.OnClickListener, ChooseSpeciesView.ChooseSpeciesListener {
    private static final String TAG = CreateHatchFragment.class.getSimpleName();

    public interface CreateHatchListener {
        void onHatchCreated(int species, int eggCount, String name);
    }

    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private CreateHatchFragment.CreateHatchListener createHatchListener;
    private LoaderManager loaderManager;
    private boolean needLoaders = true;
    int[] speciesIds = new int[0];
    String[] speciesNames = new String[0];
    float[] speciesDays = new float[0];
    Map<Integer, String> speciesPicMap = new HashMap<>();
    Button speciesButton;
    Button saveButton;
    private int newSpeciesId;
    private int newEggCount;
    private String newHatchName;

    public CreateHatchFragment() {
        Log.i(TAG, "HatchListFragment(): new");
    }

    public static CreateHatchFragment newInstance(CreateHatchFragment.CreateHatchListener listener, CollapsingToolbarLayout ctl, AppBarLayout abl, ImageView iv) {
        CreateHatchFragment fragment = new CreateHatchFragment();
        fragment.createHatchListener = listener;
        fragment.toolbarLayout = ctl;
        fragment.appBarLayout = abl;
        fragment.imageView = iv;
        return(fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.newSpeciesId = 0;
        this.newEggCount = 0;
        this.newHatchName = null;
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
        // wtf
//        if(container != null){
//            container.removeAllViews();
//        }
        // setup ui contraptions
        if(context != null) {
            // save button
            this.saveButton = rootView.findViewById(R.id.saveButton);
            this.saveButton.setVisibility(View.INVISIBLE);
            this.saveButton.setOnClickListener(this);
            // species
            this.speciesButton = rootView.findViewById(R.id.speciesButton);
            this.speciesButton.setOnClickListener(this);
            // egg count
            NumberPicker eggCountPicker = rootView.findViewById(R.id.eggCount);
            eggCountPicker.setMinValue(0);
            eggCountPicker.setMaxValue(100);
            eggCountPicker.setOnValueChangedListener(this);
            // name
            EditText nameText = rootView.findViewById(R.id.text);
            nameText.setOnEditorActionListener(this);
            // wtf
//            rootView.setOnTouchListener(new View.OnTouchListener() {
//                public boolean onTouch(View v, MotionEvent event) {
//                    return true;
//                }
//            });
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
    // egg count
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        this.newEggCount = newVal;
    }

    // hatch name
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.i(TAG, "onEditorAction()");
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            this.newHatchName = v.getText().toString();
            this.checkSave();
        }
        return false;
    }

    // choose species
    @Override
    public void onClick(View v) {
        if(v == this.speciesButton) {
            Fragment f = CreateHatchFragment.this.getFragmentManager().findFragmentByTag("SpeciesDialog");
            if (f == null) {
                DialogChooseSpecies d = new DialogChooseSpecies();
                d.setOnSpeciesListener(this);
                Bundle b = new Bundle();
                b.putIntArray(Globals.KEY_SPECIES_IDS, CreateHatchFragment.this.speciesIds);
                b.putFloatArray(Globals.KEY_SPECIES_DAYS, CreateHatchFragment.this.speciesDays);
                b.putStringArray(Globals.KEY_SPECIES_NAMES, CreateHatchFragment.this.speciesNames);
                int[] ids = new int[CreateHatchFragment.this.speciesPicMap.size()];
                String[] files = new String[CreateHatchFragment.this.speciesPicMap.size()];
                int i = 0;
                for (Integer id : CreateHatchFragment.this.speciesPicMap.keySet()) {
                    ids[i] = id;
                    files[i] = CreateHatchFragment.this.speciesPicMap.get(id);
                    i++;
                }
                b.putIntArray(Globals.KEY_SPECIES_PICS_IDS, ids);
                b.putStringArray(Globals.KEY_SPECIES_PICS_STRINGS, files);
                d.setArguments(b);
                d.show(CreateHatchFragment.this.getFragmentManager(), "SpeciesDialog");
            }
        }
        else if(v == this.saveButton){
            Log.i(TAG, "onClick(): SAVE");
            if(this.createHatchListener != null){
                this.createHatchListener.onHatchCreated(this.newSpeciesId, this.newEggCount, this.newHatchName);
            }
        }
    }

    @Override
    public void onSpeciesChosen(int speciesId) {
        Util.switchImages(this.getContext(), this.imageView, Uri.parse(this.speciesPicMap.get(speciesId)).getPath());
        this.newSpeciesId = speciesId;
        this.checkSave();
    }

    private void checkSave(){
        if((this.newSpeciesId > 0) && (this.newHatchName != null)){
            this.saveButton.setVisibility(View.VISIBLE);
        }
    }
}
