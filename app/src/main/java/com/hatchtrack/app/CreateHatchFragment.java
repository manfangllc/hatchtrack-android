package com.hatchtrack.app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.SpeciesTable;

import java.util.HashMap;
import java.util.Map;

public class CreateHatchFragment extends Fragment implements Stackable, LoaderManager.LoaderCallbacks<Cursor>, TextView.OnEditorActionListener, View.OnClickListener, ChooseSpeciesView.ChooseSpeciesListener, DialogEggCount.EggCountListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = CreateHatchFragment.class.getSimpleName();


    public interface CreateHatchListener {
        void onHatchCreated(int species, int eggCount, String name);
    }

    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;
    private CreateHatchFragment.CreateHatchListener createHatchListener;
    private LoaderManager loaderManager;
    private boolean needLoaders = true;
    int[] speciesIds = new int[0];
    String[] speciesNames = new String[0];
    float[] speciesDays = new float[0];
    Map<Integer, String> speciesPicMap = new HashMap<>();
    private int newSpeciesId;
    private int newEggCount;
    private String newHatchName;
    private View nameContainer;
    private View speciesContainer;
    private View countContainer;
    private TextView countValue;
    private EditText nameText;
    private TextView speciesValue;
    private TextView daysValue;
    private CheckBox notificationsCheckbox;

    public CreateHatchFragment() {
        Log.i(TAG, "HatchListFragment(): new");
    }

    public static CreateHatchFragment newInstance(CreateHatchFragment.CreateHatchListener listener, CollapsingToolbarLayout ctl, AppBarLayout abl, ImageView iv, FloatingActionButton fab, CoordinatorLayout mc) {
        CreateHatchFragment fragment = new CreateHatchFragment();
        fragment.createHatchListener = listener;
        fragment.toolbarLayout = ctl;
        fragment.appBarLayout = abl;
        fragment.imageView = iv;
        fragment.fab = fab;
        fragment.mainCoordinator = mc;
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
        // setup ui contraptions
        if(context != null) {
            // species
            this.speciesContainer = rootView.findViewById(R.id.speciesContainer);
            this.speciesContainer.setOnClickListener(this);
            // egg count
            this.countValue = rootView.findViewById(R.id.countValue);
            this.countContainer = rootView.findViewById(R.id.countContainer);
            this.countContainer.setOnClickListener(this);
            this.speciesValue = rootView.findViewById(R.id.speciesNameValue);
            this.daysValue = rootView.findViewById(R.id.speciesDaysValue);
            // name
            this.nameContainer = rootView.findViewById(R.id.nameContainer);
            this.nameContainer.setOnClickListener(this);
            this.nameText = rootView.findViewById(R.id.nameText);
            this.nameText.setOnEditorActionListener(this);
            // notifications
            this.notificationsCheckbox = rootView.findViewById(R.id.notificationsCheckbox);
            this.notificationsCheckbox.setOnCheckedChangeListener(this);
         }
         this.fab.hide();
         return(rootView);
    }

    @Override
    public void onVisible() {
        this.toolbarLayout.setTitle("New Hatch...");
        this.setupFab();
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
        if(v == this.speciesContainer) {
            Log.i(TAG, "onClick(): SPECIES");
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
        else if(v == this.nameContainer) {
            Log.i(TAG, "onClick(): NAME");
            this.nameText.post(new Runnable() {
                public void run() {
                    CreateHatchFragment.this.nameText.requestFocus();
                    InputMethodManager lManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(CreateHatchFragment.this.nameText, 0);
                }
            });
        }
         else if(v == this.countContainer){
            Log.i(TAG, "onClick(): COUNT");
            Fragment f = CreateHatchFragment.this.getFragmentManager().findFragmentByTag("EggCountDialog");
            if(f == null) {
                DialogEggCount d = new DialogEggCount();
                d.setEggCountListener(CreateHatchFragment.this);
                d.setValue(this.newEggCount);
                d.show(CreateHatchFragment.this.getFragmentManager(), "EggCountDialog");
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(this.getActivity() != null){
            if(buttonView == this.notificationsCheckbox){
                if(isChecked){
                    if(ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        // we don't have have permission so make sure checkbox stays off
                        this.notificationsCheckbox.setChecked(false);
                        // try to get permission
                        requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, Globals.PERMISSION_WRITE_CALENDAR);
                    } else {
                        // we have permission so show the notification options
                    }
                } else {
                    // hide options and remove notifications
                }
            }
        }
    }

    @Override
    public void onSpeciesChosen(int speciesId, String speciesName, float speciesDays) {
        Util.switchImages(this.getContext(), this.imageView, Uri.parse(this.speciesPicMap.get(speciesId)).getPath());
        this.newSpeciesId = speciesId;
        this.speciesValue.setText(speciesName);
        this.daysValue.setText(Float.toString(speciesDays));
        this.checkSave();
    }

    @Override
    public void onEggCount(int count) {
        Log.i(TAG, "onEggCount(" + count + ")");
        this.countValue.setText(Integer.toString(count));
        this.newEggCount = count;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case Globals.PERMISSION_WRITE_CALENDAR:
                if ((grantResults.length > 0 ) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    // turn notifications on
                    this.notificationsCheckbox.setChecked(true);
                } else {
                    // permission denied...eediots!
                    this.notificationsCheckbox.setChecked(false);
                    // 'splain why we need it
                    (new AlertDialog.Builder(this.getActivity(), R.style.HatchTrackDialogThemeAnim))
                            .setTitle(this.getActivity().getResources().getString(R.string.calendar_permission_title))
                            .setMessage(this.getActivity().getResources().getString(R.string.calendar_permission_text))
                            .setPositiveButton(this.getActivity().getResources().getString(R.string.neutral), new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
                break;
        }
    }

    private void checkSave(){
        if((this.newSpeciesId > 0) && (this.newHatchName != null)){
            this.fab.show();
            Snackbar.make(
                    this.mainCoordinator,
                    Html.fromHtml("<font color=\"#ffff00\">" + this.getResources().getText(R.string.snackbar_fab_createhatch) + "</font>"),
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }

    private void setupFab(){
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CreateHatchFragment.this.createHatchListener != null){
                    CreateHatchFragment.this.createHatchListener.onHatchCreated(CreateHatchFragment.this.newSpeciesId, CreateHatchFragment.this.newEggCount, CreateHatchFragment.this.newHatchName);
                }
            }
        });
    }

}
