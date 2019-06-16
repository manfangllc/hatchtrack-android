package com.hatchtrack.app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hatchtrack.app.database.Data;
import com.hatchtrack.app.database.HatchPeepTable;
import com.hatchtrack.app.database.HatchTable;
import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.PeepTable;
import com.hatchtrack.app.database.SpeciesTable;

import java.util.HashMap;
import java.util.Map;

public class HatchFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TextView.OnEditorActionListener,
        View.OnClickListener,
        ChooseSpeciesView.ChooseSpeciesListener,
        DialogEggCount.EggCountListener,
        CompoundButton.OnCheckedChangeListener,
        Util.UtilDoneCallback, DialogEditText.EditTextListener {
    private static final String TAG = HatchFragment.class.getSimpleName();

    private Context context;
    private boolean needLoaders = true;
    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;
    private LoaderManager loaderManager;
    private long hatchId;
    private int speciesId;
    int[] speciesIds = new int[0];
    String[] speciesNames = new String[0];
    float[] speciesDaysArray = new float[0];
    float speciesDays;
    Map<Integer, String> speciesPicMap = new HashMap<>();
    private String name;
    private String newHatchName;
    private int newSpeciesId;
    private int newEggCount;
    private int[] peepIds = new int[0];
    private String[] peepNames = new String[0];
    private int[] freePeepIds = new int[0];
    private String[] freePeepNames = new String[0];
    private String picMapPath;
    private String imagePath;
    private EditText textView;
    private Handler bgHandler;
    private Handler uiHandler;
    private AlertDialog bizzyDialog;
    private View nameContainer;
    private View speciesContainer;
    private String speciesName;
    private TextView speciesDaysValue;
    private View countContainer;
    private TextView countValue;
    private EditText nameText;
    private TextView speciesNameValue;
    private CheckBox notificationsCheckbox;
    private int hatchStatus;
    private DialogEditText nameDialog;
    private Button startHatchButton;
    private boolean needsCalendar;
    private boolean hasTurnReminders;

    public HatchFragment() {
        this.uiHandler = new Handler();
    }

    public static HatchFragment newInstance(CollapsingToolbarLayout ctl, AppBarLayout abl, ImageView iv, FloatingActionButton fab, CoordinatorLayout mc) {
        HatchFragment fragment = new HatchFragment();
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
            this.hatchId = args.getLong(Globals.KEY_DBID, 0);
            if(this.loaderManager != null){
                this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_HATCHTABLE, null, this);
                this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_PEEPTABLE, null, this);
                this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_HATCHPEEPTABLE, null, this);
                this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_SPECIESTABLE, null, this);
            }
            this.picMapPath = HatchFragment.this.speciesPicMap.get(this.speciesId);
            this.refresh();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getContext();
        this.imagePath = null;
        this.peepIds = new int[0];
        this.freePeepIds = new int[0];
        this.peepNames = new String[0];
        this.freePeepNames = new String[0];
        this.newSpeciesId = 0;
        this.newEggCount = 0;
        this.newHatchName = null;
        if(this.loaderManager == null) {
            this.loaderManager = this.getActivity().getSupportLoaderManager();
        }
        if(this.bgHandler == null){
            HandlerThread ht = new HandlerThread("HatchFragment bgThread");
            ht.start();
            this.bgHandler = new Handler(ht.getLooper());
        }
        if(this.needLoaders){
            this.loaderManager.initLoader(Globals.LOADER_ID_HATCH_HATCHTABLE, null, this);
            this.loaderManager.initLoader(Globals.LOADER_ID_HATCH_PEEPTABLE, null, this);
            this.loaderManager.initLoader(Globals.LOADER_ID_HATCH_HATCHPEEPTABLE, null, this);
            this.loaderManager.initLoader(Globals.LOADER_ID_HATCH_SPECIESTABLE, null, this);
            this.needLoaders = false;
        } else {
            this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_HATCHTABLE, null, this);
            this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_PEEPTABLE, null, this);
            this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_HATCHPEEPTABLE, null, this);
            this.loaderManager.restartLoader(Globals.LOADER_ID_HATCH_SPECIESTABLE, null, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!this.needLoaders) {
            this.loaderManager.destroyLoader(Globals.LOADER_ID_HATCH_HATCHTABLE);
            this.loaderManager.destroyLoader(Globals.LOADER_ID_HATCH_PEEPTABLE);
            this.loaderManager.destroyLoader(Globals.LOADER_ID_HATCH_HATCHPEEPTABLE);
            this.loaderManager.destroyLoader(Globals.LOADER_ID_HATCH_SPECIESTABLE);
            this.needLoaders = true;
            this.loaderManager = null;
            this.imagePath = null;
            if(this.bgHandler != null){
                this.bgHandler.postAtFrontOfQueue(new Runnable(){
                    @Override
                    public void run() {
                        Looper.myLooper().quitSafely();
                    }
                });
                this.bgHandler = null;
            }
        }
    }

    private static final String TURN_EVENT = "Turn the eggs!"; //This is event description

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.toolbarLayout.setTitle("Hatch");
        this.appBarLayout.setExpanded(true);

        View rootView = inflater.inflate(R.layout.frag_hatch, container, false);
        // species
        this.speciesContainer = rootView.findViewById(R.id.speciesContainer);
        this.speciesContainer.setOnClickListener(this);
        // egg count
        this.countValue = rootView.findViewById(R.id.countValue);
        this.countContainer = rootView.findViewById(R.id.countContainer);
        this.countContainer.setOnClickListener(this);
        this.speciesNameValue = rootView.findViewById(R.id.speciesNameValue);
        this.speciesDaysValue = rootView.findViewById(R.id.speciesDaysValue);
        // name
        this.nameContainer = rootView.findViewById(R.id.nameContainer);
        this.nameContainer.setOnClickListener(this);
        this.nameText = rootView.findViewById(R.id.nameText);
        this.nameText.setOnEditorActionListener(this);
        // start hatch
        this.startHatchButton = rootView.findViewById(R.id.startHatchButton);
        this.startHatchButton.setOnClickListener(this);
        // notifications
        this.notificationsCheckbox = rootView.findViewById(R.id.notificationsCheckbox);
        this.notificationsCheckbox.setOnCheckedChangeListener(this);
        this.textView = rootView.findViewById(R.id.text);
        rootView.findViewById(R.id.peepButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = HatchFragment.this.getFragmentManager().findFragmentByTag("PeepsDialog");
                if(f == null){
                    DialogChoosePeeps d = new DialogChoosePeeps();
                    int n = HatchFragment.this.peepIds.length + HatchFragment.this.freePeepIds.length;
                    String[] pNames = new String[n];
                    int[] pIds = new int[n];
                    boolean[] pCheck = new boolean[n];
                    int j = 0;
                    for (int i = 0; i < HatchFragment.this.peepIds.length; i++) {
                        pNames[j] = HatchFragment.this.peepNames[i];
                        pIds[j] = HatchFragment.this.peepIds[i];
                        pCheck[j] = true;
                        j++;
                    }
                    for (int i = 0; i < HatchFragment.this.freePeepIds.length; i++) {
                        pNames[j] = HatchFragment.this.freePeepNames[i];
                        pIds[j] = HatchFragment.this.freePeepIds[i];
                        j++;
                    }
                    Bundle b = new Bundle();
                    b.putLong(Globals.KEY_HATCH_ID, HatchFragment.this.hatchId);
                    b.putIntArray(Globals.KEY_PEEP_IDS, pIds);
                    b.putStringArray(Globals.KEY_PEEP_NAMES, pNames);
                    b.putBooleanArray(Globals.KEY_PEEP_IN_HATCH, pCheck);
                    d.setArguments(b);
                    d.show(HatchFragment.this.getFragmentManager(), "PeepsDialog");
                }
            }
        });

//        rootView.findViewById(R.id.reminderTest).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!HatchFragment.this.checkCalendarPermission()) {
//                    return;
//                }
//                AlertDialog.Builder builder = new AlertDialog.Builder(HatchFragment.this.context, R.style.HatchTrackDialogThemeAnim_NoMinWidth);
//                HatchFragment.this.bizzyDialog = builder.setView(R.layout.dialog_bizzy).setTitle("Updating Calendar").create();
//                HatchFragment.this.bizzyDialog.show();
//                HatchFragment.this.bgHandler.post(new Runnable(){
//                    @Override
//                    public void run() {
//                        Util.createCalendarTurns(
//                                HatchFragment.this.context,
//                                HatchFragment.this.hatchId,
//                                HatchFragment.this.name,
//                                Data.getSpeciesDaysFromHatch(HatchFragment.this.context, HatchFragment.this.hatchId) - 5,
//                                System.currentTimeMillis(),
//                                new Util.UtilDoneCallback(){
//                                    @Override
//                                    public void onDone(int n) {
//                                        if(HatchFragment.this.bizzyDialog != null){
//                                            HatchFragment.this.uiHandler.post(new Runnable(){
//                                                @Override
//                                                public void run() {
//                                                    HatchFragment.this.bizzyDialog.dismiss();
//                                                    HatchFragment.this.bizzyDialog = null;
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//                        );
//                    }
//                });
//            }
//        });
//
//        rootView.findViewById(R.id.reminderTest2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!HatchFragment.this.checkCalendarPermission()){
//                    return;
//                }
//                AlertDialog.Builder builder = new AlertDialog.Builder(HatchFragment.this.context, R.style.HatchTrackDialogThemeAnim_NoMinWidth);
//                HatchFragment.this.bizzyDialog = builder.setView(R.layout.dialog_bizzy).setTitle("Updating Calendar").create();
//                HatchFragment.this.bizzyDialog.show();
//                HatchFragment.this.bgHandler.post(new Runnable(){
//                    @Override
//                    public void run() {
//                        Util.removeTurnEvents(HatchFragment.this.context, HatchFragment.this.hatchId, new Util.UtilDoneCallback(){
//                            @Override
//                            public void onDone(int n) {
//                                if(HatchFragment.this.bizzyDialog != null){
//                                    HatchFragment.this.uiHandler.post(new Runnable(){
//                                        @Override
//                                        public void run() {
//                                            HatchFragment.this.bizzyDialog.dismiss();
//                                            HatchFragment.this.bizzyDialog = null;
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    }
//                });
//            }
//        });
//
//        rootView.findViewById(R.id.reminderTest3).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!HatchFragment.this.checkCalendarPermission()){
//                    return;
//                }
//                AlertDialog.Builder builder = new AlertDialog.Builder(HatchFragment.this.context, R.style.HatchTrackDialogThemeAnim_NoMinWidth);
//                HatchFragment.this.bizzyDialog = builder.setView(R.layout.dialog_bizzy).setTitle("Updating Calendar").create();
//                HatchFragment.this.bizzyDialog.show();
//                HatchFragment.this.bgHandler.post(new Runnable(){
//                    @Override
//                    public void run() {
//                        Util.removeTurnReminders(HatchFragment.this.context, HatchFragment.this.hatchId, new Util.UtilDoneCallback(){
//                            @Override
//                            public void onDone(int n) {
//                                if(HatchFragment.this.bizzyDialog != null){
//                                    HatchFragment.this.uiHandler.post(new Runnable(){
//                                        @Override
//                                        public void run() {
//                                            HatchFragment.this.bizzyDialog.dismiss();
//                                            HatchFragment.this.bizzyDialog = null;
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    }
//                });
//            }
//        });

        this.refresh();
        Log.i(TAG, Data.dumpTable(this.context, HatchtrackProvider.HATCH_URI));
        return(rootView);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.appBarLayout.setOnClickListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.setupFab();
        this.appBarLayout.setOnClickListener(this);
    }

    private void refresh(){
        if(this.textView != null){
            if(this.hatchId == 0){
                this.textView.setText("This should be some hatch info, but there's no database ID for this hatch.");
            }
            else {
                StringBuilder msg = Data.getRowText(this.context, HatchtrackProvider.HATCH_URI, this.hatchId);
                if((this.peepIds != null) && (this.peepIds.length > 0)){
                    msg.append("\nPeep IDs: ");
                    for(int i = 0; i < this.peepIds.length; i++){
                        msg.append(this.peepIds[i]);
                        msg.append(' ');
                    }
                }
                this.textView.setText(msg);
            }
        }
        if(this.nameText != null){
            this.nameText.setText(this.name);
        } else {
            if(this.getFragmentManager() != null) {
                if (this.nameDialog == null) {
                    this.nameDialog = new DialogEditText();
                    this.nameDialog.setEditTextListener(HatchFragment.this);
                    this.nameDialog.setValue(this.name);
                    this.nameDialog.show(this.getFragmentManager(), "EditTextDialog");
                }
            }
        }
        if(this.speciesNameValue != null){
            this.speciesNameValue.setText(this.speciesName);
        }
        if(this.speciesDaysValue != null){
            this.speciesDaysValue.setText(Float.toString(this.speciesDays));
        }
        if(this.toolbarLayout != null){
            this.toolbarLayout.setTitle(this.name);
        }
    }

//    @Override
//    public void onVisible() {
//        this.toolbarLayout.setTitle("New Hatch...");
//        this.setupFab();
//        this.imageView.setImageResource(R.drawable.hatch_1);
//    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> result = null;
        switch (id) {
            case Globals.LOADER_ID_HATCH_HATCHTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.context,
                        // Table to query
                        HatchtrackProvider.HATCH_URI,
                        // Projection
                        null,
                        // Selection
                        HatchTable.ID + " = " + this.hatchId,
                        // Selection arguments
                        null,
                        // Sort order
                        null
                );
                break;
            case Globals.LOADER_ID_HATCH_PEEPTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.context,
                        // Table to query
                        HatchtrackProvider.PEEP_URI,
                        // Projection
                        new String[]{PeepTable.ID, PeepTable.NAME, PeepTable.PEEP_ID},
                        // Selection
                        PeepTable.HATCH_ID + " = 0",
                        // Selection arguments
                        null,
                        // Sort order
                        null
                );
                break;
            case Globals.LOADER_ID_HATCH_HATCHPEEPTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.context,
                        // Table to query
                        HatchtrackProvider.HATCH_PEEP_URI,
                        // Projection
                        new String[]{HatchPeepTable.ID, HatchPeepTable.PEEP_ID},
                        // Selection
                        HatchPeepTable.HATCH_ID + " = " + this.hatchId,
                        // Selection arguments
                        null,
                        // Sort order
                        null
                );
                break;
            case Globals.LOADER_ID_HATCH_SPECIESTABLE:
                result = new CursorLoader(
                        // Parent activity context
                        this.context,
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
            case Globals.LOADER_ID_HATCH_HATCHTABLE:
                // this hatch
                if(cursor.moveToFirst()) {
                    this.hasTurnReminders = cursor.getInt(cursor.getColumnIndex(HatchTable.HAS_TURN_REMINDERS)) != 0;
                    this.name = cursor.getString(cursor.getColumnIndex(HatchTable.NAME));
                    int sid = cursor.getInt(cursor.getColumnIndex(HatchTable.SPECIES_ID));
                    long startTime = cursor.getLong(cursor.getColumnIndex(HatchTable.START));
                    long endTime = cursor.getLong(cursor.getColumnIndex(HatchTable.END));
                    if(startTime == 0L){
                        this.hatchStatus = Globals.STATUS_HATCH_UNSTARTED;
                    } else if(startTime < endTime){
                        this.hatchStatus = Globals.STATUS_HATCH_STARTED;
                    } else {
                        this.hatchStatus = Globals.STATUS_HATCH_FINISHED;
                    }
                    if(sid != this.speciesId) {
                        this.speciesId = sid;
                        if(this.speciesId < this.speciesDaysArray.length) {
                            this.speciesDays = this.speciesDaysArray[this.speciesId];
                        }
                        if(this.speciesId < this.speciesNames.length) {
                            this.speciesName = this.speciesNames[this.speciesId];
                        }
                        this.refreshImage();
                    }
                    this.refresh();
                }
                break;
            case Globals.LOADER_ID_HATCH_PEEPTABLE:
                // free peeps (in no hatch)
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        this.freePeepIds = new int[cursor.getCount()];
                        this.freePeepNames = new String[cursor.getCount()];
                        int i = 0;
                        while(!cursor.isAfterLast()){
                            String name = cursor.getString(cursor.getColumnIndex(PeepTable.NAME));
                            if(name == null){
                                name = cursor.getString(cursor.getColumnIndex(PeepTable.ID));
                            }
                            this.freePeepNames[i] = name;
                            this.freePeepIds[i++] = cursor.getInt(cursor.getColumnIndex(PeepTable.ID));
                            cursor.moveToNext();
                        }
                    }
                }
                break;
            case Globals.LOADER_ID_HATCH_HATCHPEEPTABLE:
                // peeps in this hatch
                this.peepIds = new int[cursor.getCount()];
                this.peepNames = new String[cursor.getCount()];
                if(cursor.moveToFirst()) {
                    int i = 0;
                    while(!cursor.isAfterLast()) {
                        this.peepIds[i++] = cursor.getInt(cursor.getColumnIndex((HatchPeepTable.PEEP_ID)));
                        cursor.moveToNext();
                    }
                }
                String[] projection = new String[]{PeepTable.NAME};
                int j = 0;
                for(int i = 0; i < this.peepIds.length; i++) {
                    Cursor c = this.context.getContentResolver().query(HatchtrackProvider.PEEP_URI,
                            projection,
                            PeepTable.ID + " = " + this.peepIds[i],
                            null,
                            null
                    );
                    if(c != null){
                        if(c.moveToFirst()){
                            this.peepNames[j++] = c.getString(c.getColumnIndex(PeepTable.NAME));
                        }
                        c.close();
                    }
                }
                this.refresh();
                break;
            case Globals.LOADER_ID_HATCH_SPECIESTABLE:
                // all the species
                this.speciesIds = new int[cursor.getCount()];
                this.speciesDaysArray = new float[cursor.getCount()];
                this.speciesNames = new String[cursor.getCount()];
                this.speciesPicMap.clear();
                if(cursor.moveToFirst()) {
                    int i = 0;
                    while(!cursor.isAfterLast()) {
                        this.speciesIds[i] = cursor.getInt(cursor.getColumnIndex((SpeciesTable.ID)));
                        this.speciesNames[i] = cursor.getString(cursor.getColumnIndex((SpeciesTable.NAME)));
                        this.speciesDaysArray[i] = cursor.getFloat(cursor.getColumnIndex((SpeciesTable.DAYS)));
                        String s = cursor.getString(cursor.getColumnIndex((SpeciesTable.PICTURE_URI)));
                        if(s != null) {
                            this.speciesPicMap.put((this.speciesIds[i]), s);
                        }
                        i++;
                        cursor.moveToNext();
                    }
                    this.refresh();
                    this.refreshImage();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Globals.LOADER_ID_HATCH_HATCHTABLE:
                break;
            case Globals.LOADER_ID_HATCH_PEEPTABLE:
                break;
            case Globals.LOADER_ID_HATCH_HATCHPEEPTABLE:
                break;
            case Globals.LOADER_ID_HATCH_SPECIESTABLE:
                break;
        }
    }

    // hatch name
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.i(TAG, "onEditorAction()");
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            this.newHatchName = v.getText().toString();
            Data.setHatchName(this.context, this.hatchId, this.newHatchName);
//            this.checkStart();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v == this.speciesContainer) {
            // choose species
            Log.i(TAG, "onClick(): SPECIES");
            Fragment f = HatchFragment.this.getFragmentManager().findFragmentByTag("SpeciesDialog");
            if (f == null) {
                DialogChooseSpecies d = new DialogChooseSpecies();
                d.setOnSpeciesListener(this);
                Bundle b = new Bundle();
                b.putIntArray(Globals.KEY_SPECIES_IDS, HatchFragment.this.speciesIds);
                b.putFloatArray(Globals.KEY_SPECIES_DAYS, HatchFragment.this.speciesDaysArray);
                b.putStringArray(Globals.KEY_SPECIES_NAMES, HatchFragment.this.speciesNames);
                int[] ids = new int[HatchFragment.this.speciesPicMap.size()];
                String[] files = new String[HatchFragment.this.speciesPicMap.size()];
                int i = 0;
                for (Integer id : HatchFragment.this.speciesPicMap.keySet()) {
                    ids[i] = id;
                    files[i] = HatchFragment.this.speciesPicMap.get(id);
                    i++;
                }
                b.putIntArray(Globals.KEY_SPECIES_PICS_IDS, ids);
                b.putStringArray(Globals.KEY_SPECIES_PICS_STRINGS, files);
                d.setArguments(b);
                d.show(HatchFragment.this.getFragmentManager(), "SpeciesDialog");
            }
        }
        else if(v == this.nameContainer) {
            Log.i(TAG, "onClick(): NAME");
            this.nameText.post(new Runnable() {
                public void run() {
                    HatchFragment.this.nameText.requestFocus();
                    InputMethodManager lManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(HatchFragment.this.nameText, 0);
                }
            });
        }
        else if(v == this.startHatchButton) {
            // start hatch
            Log.i(TAG, "onClick(): START HATCH");
            if((this.speciesDays <= 0) || (this.speciesName == null)){
                (new AlertDialog.Builder(this.getActivity(), R.style.HatchTrackDialogThemeAnim))
                        .setTitle(this.getActivity().getResources().getString(R.string.oops_title))
                        .setMessage(this.getActivity().getResources().getString(R.string.need_species_text))
                        .setPositiveButton(this.getActivity().getResources().getString(R.string.neutral), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                if (this.needsCalendar && ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    // we don't have have permission so can't start the hatch
                    // try to get permission
                    requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, Globals.PERMISSION_WRITE_CALENDAR);
                } else {
                    // we have permission, or we don't need to touch the calendar, so start the hatch
//                    this.mungTurnReminders(false);
                }
            }
        }
        else if(v == this.countContainer){
            Log.i(TAG, "onClick(): COUNT");
            Fragment f = HatchFragment.this.getFragmentManager().findFragmentByTag("EggCountDialog");
            if(f == null) {
                DialogEggCount d = new DialogEggCount();
                d.setEggCountListener(HatchFragment.this);
                d.setValue(this.newEggCount);
                d.show(HatchFragment.this.getFragmentManager(), "EggCountDialog");
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (this.getActivity() != null) {
            if (buttonView == this.notificationsCheckbox) {
                if(this.hatchStatus == Globals.STATUS_HATCH_UNSTARTED) {
                }
                else if(this.hatchStatus == Globals.STATUS_HATCH_STARTED) {
                    if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        // we don't have have permission so make sure checkbox stays off
                        this.notificationsCheckbox.setChecked(!isChecked);
                        // try to get permission
                        requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, Globals.PERMISSION_WRITE_CALENDAR);
                    } else if (isChecked) {
                        // we have permission so add the turn reminders
                        this.mungTurnReminders(true);
                    } else {
                        // we have permission so remove the turn reminders
                        this.mungTurnReminders(false);
                    }
                }
            }
        }
    }

    private void mungTurnReminders(final boolean add){
        if (!HatchFragment.this.checkCalendarPermission()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(HatchFragment.this.context, R.style.HatchTrackDialogThemeAnim_NoMinWidth);
        HatchFragment.this.bizzyDialog = builder.setView(R.layout.dialog_bizzy).setTitle("Updating Calendar").create();
        HatchFragment.this.bizzyDialog.show();
        HatchFragment.this.bgHandler.post(new Runnable(){
            @Override
            public void run() {
                if(add) {
                    Util.createCalendarTurns(
                            HatchFragment.this.context,
                            HatchFragment.this.hatchId,
                            HatchFragment.this.name,
                            Data.getSpeciesDaysFromHatch(HatchFragment.this.context, HatchFragment.this.hatchId) - 5,
                            System.currentTimeMillis(),
                            HatchFragment.this
                    );
                } else {
                        Util.removeTurnEvents(HatchFragment.this.context, HatchFragment.this.hatchId, HatchFragment.this);
                }
            }
        });
    }

    @Override
    public void onSpeciesChosen(int speciesId, String speciesName, float speciesDays) {
        Util.switchImages(this.getContext(), this.imageView, Uri.parse(this.speciesPicMap.get(speciesId)).getPath());
        this.newSpeciesId = speciesId;
        this.speciesName = speciesName;
        this.speciesDays = speciesDays;
        this.speciesNameValue.setText(speciesName);
        this.speciesDaysValue.setText(Float.toString(this.speciesDays));
        this.checkStart();
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


    private void refreshImage(){
        String s = this.speciesPicMap.get(this.speciesId);
        if(s != null){
            Uri u = Uri.parse(s);
            String s2 = u.getPath();
            if((s2 != null) && (!s2.equals(this.imagePath))) {
                this.imagePath = s2;
                this.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Util.switchImages(this.context, this.imageView, this.imagePath);
            }
        }
    }

    private void setupFab(){
//        this.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });
//        this.fab.show();
//        Snackbar.make(
//                this.mainCoordinator,
//                Html.fromHtml("<font color=\"#ffff00\">" + this.getResources().getText(R.string.snackbar_fab_hatch) + "</font>"),
//                Snackbar.LENGTH_LONG
//        ).show();
        this.fab.hide();
    }

    private boolean checkCalendarPermission(){
        boolean result = (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED);
        return(result);
    }

    private void checkStart(){
        if((this.newSpeciesId > 0) && (this.newHatchName != null)){
            this.fab.show();
            Snackbar.make(
                    this.mainCoordinator,
                    Html.fromHtml("<font color=\"#ffff00\">" + this.getResources().getText(R.string.snackbar_fab_createhatch) + "</font>"),
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public void onDone(int n) {
        if(HatchFragment.this.bizzyDialog != null){
            HatchFragment.this.uiHandler.post(new Runnable(){
                @Override
                public void run() {
                    HatchFragment.this.bizzyDialog.dismiss();
                    HatchFragment.this.bizzyDialog = null;
                }
            });
        }
    }

    @Override
    public void onText(String text) {
        this.nameDialog.dismiss();
        this.nameDialog = null;
    }
}
