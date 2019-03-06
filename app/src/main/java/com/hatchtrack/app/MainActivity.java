package com.hatchtrack.app;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.hatchtrack.app.database.Data;
import com.hatchtrack.app.database.HatchTable;
import com.hatchtrack.app.database.HatchtrackProvider;
import com.hatchtrack.app.database.PeepTable;
import com.hatchtrack.app.database.SpeciesTable;

import java.io.File;
import java.util.List;
import java.util.Random;

public class MainActivity
        extends
        AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        PeepListFragment.PeepClickListener,
        HatchListFragment.HatchClickListener,
        DialogChoosePeeps.ChoosePeepsDialogListener,
        CreateHatchFragment.CreateHatchListener
{

    private static final String TAG = MainActivity.class.getSimpleName();

    private CollapsingToolbarLayout toolbarLayout;
    private ActionBarDrawerToggle drawerToggle;
    private AppBarLayout appBarLayout;
    private ImageView imageView;
    private DrawerLayout drawerLayout;
    private boolean toolBarNavigationListenerIsRegistered;

    private HatchListFragment hatchListFrag;
    private HatchFragment hatchFrag;
    private CreateHatchFragment createHatchFrag;
    private PeepListFragment peepListFrag;
    private PeepFragment peepFrag;
    private WebFragment webFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get various ui contraptions
        this.toolbarLayout = this.findViewById(R.id.collapsingLayout);
        this.appBarLayout = this.findViewById(R.id.appBarId);
        this.imageView = this.findViewById(R.id.imageView);
        this.drawerLayout = findViewById(R.id.drawerLayout);
        // stuff to make nutty collapsible toolbar work
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // stuff to make drawer work
        this.drawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.drawerLayout.addDrawerListener(this.drawerToggle);
        this.drawerToggle.syncState();
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        this.drawerLayout.openDrawer(Gravity.START);
        // setup crazy fragment backstack listener
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragContainer);
                try {
                    ((Braggable) f).onVisible();
                } catch (ClassCastException e){
                    Log.i(TAG, f.toString() + " fragment doesn't implement Braggable");
                }
            }
        });
        // possibly load the default species data from assets to filesystem and database
        ContentValues cv = new ContentValues();
        for(int i = 1; i < 6; i++){
            File assetFile = Util.checkAsset(this, "pictures", "species_" + i + ".png");
            if(assetFile != null){
                cv.clear();
                Uri uri = Uri.fromFile(assetFile);
                cv.put(SpeciesTable.PICTURE_URI, uri.toString());
                this.getContentResolver().update(HatchtrackProvider.SPECIES_URI, cv, SpeciesTable.ID + " = " + i, null);
                Log.i(TAG, "URI=" + uri.toString());
            }
        }
        // create and display the default fragment
        if(this.hatchListFrag == null){
            this.hatchListFrag = HatchListFragment.newInstance(this, this.toolbarLayout, this.appBarLayout, this.imageView);
        }
        this.clearBackStack();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragContainer, this.hatchListFrag)
                .commit();
        // debug: dump the database
        Log.i(TAG, Data.dumpTable(this, HatchtrackProvider.SPECIES_URI));
        Log.i(TAG, Data.dumpTable(this, HatchtrackProvider.HATCH_URI));
        Log.i(TAG, Data.dumpTable(this, HatchtrackProvider.PEEP_URI));
        Log.i(TAG, Data.dumpTable(this, HatchtrackProvider.HATCH_PEEP_URI));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        int stackSize = getSupportFragmentManager().getBackStackEntryCount();
        Log.i(TAG, "onBackPressed(): stackSize=" + stackSize + ", isDrawerOpen()=" + drawer.isDrawerOpen(GravityCompat.START));
        if(stackSize > 1){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else if (!drawer.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed();
            this.showBackButton(false);
        } else {
            drawer.closeDrawer(GravityCompat.START);
            this.showBackButton(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        if(this.drawerToggle.onOptionsItemSelected(item)){
//            return(true);
//        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_hatches) {
            Random random = new Random(System.currentTimeMillis());
            for(int i = 1; i < 6; i++){
                Data.createHatch(this, "Hatch Name " + i, random.nextInt(20), random.nextInt(5));
            }
            Log.i(TAG, Data.dumpTable(this, HatchtrackProvider.HATCH_URI));
            return true;
        }
        else if (id == R.id.action_peeps) {
            Random random = new Random();
            for(int i = 1; i < 6; i++){
                ContentValues cv = new ContentValues();
                long now = System.currentTimeMillis();
                cv.put(PeepTable.NAME, "Peep #" + i);
                cv.put(PeepTable.PEEP_ID, System.currentTimeMillis());
                cv.put(PeepTable.BATTERY, random.nextInt(100));
                cv.put(PeepTable.TEMPERATURE, 27 + random.nextInt(38));
                cv.put(PeepTable.HUMIDITY, 50 + random.nextInt(50));
                cv.put(PeepTable.LAST_MODIFIED, now);
                cv.put(PeepTable.LAST_SYNCED, 0);
                cv.put(PeepTable.HATCH_ID, 0);
                this.getContentResolver().insert(HatchtrackProvider.PEEP_URI, cv);
            }
            return true;
        }
        else if (id == R.id.action_remove_peeps) {
            Data.removePeeps(this);
            return true;
        }
        else if (id == R.id.action_remove_hatches) {
            Data.removeHatches(this);
            return true;
        }
        else if (id == R.id.action_dump_db) {
            SimpleTextPopup d = new SimpleTextPopup();
            String s = Data.dumpTable(this, HatchtrackProvider.HATCH_URI);
            s += "\n" + Data.dumpTable(this, HatchtrackProvider.PEEP_URI);
            s += "\n" + Data.dumpTable(this, HatchtrackProvider.HATCH_PEEP_URI);
            s += "\n" + Data.dumpTable(this, HatchtrackProvider.SPECIES_URI);
            Bundle b = new Bundle();
            b.putString("Title", "database");
            b.putString("Text", s);
            d.setArguments(b);
            d.show(this.getSupportFragmentManager(), "SimpleTextPopup");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.navHatches) {
            if(this.hatchListFrag == null){
                this.hatchListFrag = HatchListFragment.newInstance(this, this.toolbarLayout, this.appBarLayout, this.imageView);
            }
            this.clearBackStack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragContainer, this.hatchListFrag)
                    .commit();
        } else if (id == R.id.navPeeps) {
            if(this.peepListFrag == null){
                this.peepListFrag = PeepListFragment.newInstance(this, this.toolbarLayout, this.appBarLayout, this.imageView);
            }
            this.clearBackStack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragContainer, this.peepListFrag)
                    .commit();
        } else if (id == R.id.navBuy) {
            if(this.webFrag == null){
                this.webFrag = WebFragment.newInstance();
            }
            this.toolbarLayout.setTitle("Buy Peeps");
            this.imageView.setImageResource(R.drawable.logo_black);
            this.appBarLayout.setExpanded(false);
            Bundle b = new Bundle();
            b.putString(Globals.KEY_URL, "http://www.hatchtrack.com");
            this.webFrag.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragContainer, this.webFrag)
                    .commit();
        } else if (id == R.id.navPrivacy) {
            if(this.webFrag == null){
                this.webFrag = WebFragment.newInstance();
            }
            this.toolbarLayout.setTitle("HatchTrack");
            this.imageView.setImageResource(R.drawable.logo_black);
            this.appBarLayout.setExpanded(false);
            Bundle b = new Bundle();
            b.putString(Globals.KEY_URL, "http://policies.google.com/privacy");
            this.webFrag.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragContainer, this.webFrag)
                    .commit();
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnPeepClicked(int dbId) {
        Log.i(TAG, "MainActivity.OnPeepClicked(): dbId=" + dbId);
        if(this.peepFrag == null){
            this.peepFrag = PeepFragment.newInstance(this.toolbarLayout, this.appBarLayout, this.imageView);
        }
        if(!this.peepFrag.isAdded()) {
            Bundle b = new Bundle();
            b.putInt(Globals.KEY_DBID, dbId);
            this.peepFrag.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragContainer, this.peepFrag)
                    .addToBackStack(null)
                    .commit();
            this.showBackButton(true);
        }
    }

    @Override
    public void onHatchClicked(int dbId) {
        Log.i(TAG, "MainActivity.onHatchClicked(): dbId=" + dbId);
        if(this.hatchFrag == null){
            this.hatchFrag = HatchFragment.newInstance(this.toolbarLayout, this.appBarLayout, this.imageView);
        }
        if(!this.hatchFrag.isAdded()) {
            Bundle b = new Bundle();
            b.putInt(Globals.KEY_DBID, dbId);
            this.hatchFrag.setArguments(b);
//            this.hatchFrag.setEnterTransition(new Fade());
            this.hatchFrag.setExitTransition(new Slide(Gravity.LEFT));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragContainer, this.hatchFrag)
                    .addToBackStack(null)
                    .commit();
            this.showBackButton(true);
        }
    }

    @Override
    public void onCreateHatch() {
        Log.i(TAG, "MainActivity.onCreateHatch()");
        this.createHatchFrag = CreateHatchFragment.newInstance(
                this,
                this.toolbarLayout,
                this.appBarLayout,
                this.imageView
        );
        if(!this.createHatchFrag.isAdded()) {
            this.createHatchFrag.setExitTransition(new Slide(Gravity.LEFT));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragContainer, this.createHatchFrag)
                    .addToBackStack(null)
                    .commit();
            this.showBackButton(true);
        }
    }

    @Override
    public void onHatchCreated(int species, int eggCount, String hatchName) {
        Log.i(TAG, "onHatchCreated(): species=" + species + ", eggCount=" + eggCount + ", name=" + hatchName);
        this.getSupportFragmentManager().popBackStack();
        Data.createHatch(this, hatchName, eggCount, species);
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void showBackButton(boolean enable) {
        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when we enable on one, we disable on the other.
        // And the order for this operation is disable first, then enable - VERY IMPORTANT.
        if (enable) {
            // may not want to open the drawer on swipe from the left in this case
            this.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            // remove hamburger
            this.drawerToggle.setDrawerIndicatorEnabled(false);
            // show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            if (!this.toolBarNavigationListenerIsRegistered) {
                this.drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // doesn't have to be onBackPressed
                        MainActivity.this.onBackPressed();
                    }
                });
                this.toolBarNavigationListenerIsRegistered = true;
            }
        } else {
            // regain the power of swipe for the drawer.
            this.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            // remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // show hamburger
            this.drawerToggle.setDrawerIndicatorEnabled(true);
            // remove the/any drawer toggle listener
            this.drawerToggle.setToolbarNavigationClickListener(null);
            this.toolBarNavigationListenerIsRegistered = false;
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    public void onPeepsChosen(int hatchId, List<Integer> peepIds) {
        Log.i(TAG, "onPeepsChosen(): hatchId=" + hatchId);
        Data.setHatchPeeps(this, hatchId, peepIds, System.currentTimeMillis());
    }
}
