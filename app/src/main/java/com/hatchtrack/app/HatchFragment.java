package com.hatchtrack.app;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.CalendarContract;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.hatchtrack.app.database.Data;
import com.hatchtrack.app.database.HatchPeepTable;
import com.hatchtrack.app.database.HatchTable;
import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.PeepTable;
import com.hatchtrack.app.database.SpeciesTable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class HatchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = HatchFragment.class.getSimpleName();

    private Context context;
    private boolean needLoaders = true;
    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private FloatingActionButton fab;
    private CoordinatorLayout mainCoordinator;
    private LoaderManager loaderManager;
    private int hatchId;
    private int speciesId;
    int[] speciesIds = new int[0];
    String[] speciesNames = new String[0];
    float[] speciesDays = new float[0];
    Map<Integer, String> speciesPicMap = new HashMap<>();
    private String name;
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
            this.hatchId = args.getInt(Globals.KEY_DBID, 0);
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
                    b.putInt(Globals.KEY_HATCH_ID, HatchFragment.this.hatchId);
                    b.putIntArray(Globals.KEY_PEEP_IDS, pIds);
                    b.putStringArray(Globals.KEY_PEEP_NAMES, pNames);
                    b.putBooleanArray(Globals.KEY_PEEP_IN_HATCH, pCheck);
                    d.setArguments(b);
                    d.show(HatchFragment.this.getFragmentManager(), "PeepsDialog");
                }
            }
        });

        rootView.findViewById(R.id.reminderTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!HatchFragment.this.checkCalendarPermission()) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(HatchFragment.this.context, R.style.HatchTrackDialogThemeAnim_NoMinWidth);
                HatchFragment.this.bizzyDialog = builder.setView(R.layout.dialog_bizzy).setTitle("Updating Calendar").create();
                HatchFragment.this.bizzyDialog.show();
                HatchFragment.this.bgHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        Util.createCalendarTurns(
                                HatchFragment.this.context,
                                HatchFragment.this.hatchId,
                                HatchFragment.this.name,
                                Data.getSpeciesDaysFromHatch(HatchFragment.this.context, HatchFragment.this.hatchId) - 5,
                                System.currentTimeMillis(),
                                new Util.UtilDoneCallback(){
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
                                }
                        );
                    }
                });
            }
        });

        rootView.findViewById(R.id.reminderTest2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!HatchFragment.this.checkCalendarPermission()){
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(HatchFragment.this.context, R.style.HatchTrackDialogThemeAnim_NoMinWidth);
                HatchFragment.this.bizzyDialog = builder.setView(R.layout.dialog_bizzy).setTitle("Updating Calendar").create();
                HatchFragment.this.bizzyDialog.show();
                HatchFragment.this.bgHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        Util.removeTurnEvents(HatchFragment.this.context, HatchFragment.this.hatchId, new Util.UtilDoneCallback(){
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
                        });
                    }
                });
            }
        });

        rootView.findViewById(R.id.reminderTest3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!HatchFragment.this.checkCalendarPermission()){
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(HatchFragment.this.context, R.style.HatchTrackDialogThemeAnim_NoMinWidth);
                HatchFragment.this.bizzyDialog = builder.setView(R.layout.dialog_bizzy).setTitle("Updating Calendar").create();
                HatchFragment.this.bizzyDialog.show();
                HatchFragment.this.bgHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        Util.removeTurnReminders(HatchFragment.this.context, HatchFragment.this.hatchId, new Util.UtilDoneCallback(){
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
                        });
                    }
                });
            }
        });

        this.refresh();
        Log.i(TAG, Data.dumpTable(this.context, HatchtrackProvider.HATCH_URI));
        return(rootView);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.setupFab();
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
        if(this.toolbarLayout != null){
            this.toolbarLayout.setTitle(this.name);
        }
    }

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
                    this.name = cursor.getString(cursor.getColumnIndex(HatchTable.NAME));
                    int sid = cursor.getInt(cursor.getColumnIndex(HatchTable.SPECIES_ID));
                    if(sid != this.speciesId) {
                        this.speciesId = sid;
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
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        this.fab.show();
        Snackbar.make(
                this.mainCoordinator,
                Html.fromHtml("<font color=\"#ffff00\">" + this.getResources().getText(R.string.snackbar_fab_hatch) + "</font>"),
                Snackbar.LENGTH_LONG
        ).show();
    }

    private boolean checkCalendarPermission(){
        boolean result = (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED);
        return(result);
    }
}
