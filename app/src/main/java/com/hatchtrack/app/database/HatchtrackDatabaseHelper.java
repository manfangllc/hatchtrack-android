package com.hatchtrack.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HatchtrackDatabaseHelper extends SQLiteOpenHelper {
    private static final String   TAG = HatchtrackDatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME    = "hatchtrack.db";
    private static final int    DATABASE_VERSION = 17;

    HatchtrackDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HatchTable.onCreate(db);
        PeepTable.onCreate(db);
        HatchPeepTable.onCreate(db);
        SpeciesTable.onCreate(db);
        HatchSpeciesTable.onCreate(db);
        return;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HatchTable.onUpgrade(db, oldVersion, newVersion);
        PeepTable.onUpgrade(db, oldVersion, newVersion);
        HatchPeepTable.onUpgrade(db, oldVersion, newVersion);
        SpeciesTable.onUpgrade(db, oldVersion, newVersion);
        HatchSpeciesTable.onUpgrade(db, oldVersion, newVersion);
        return;
    }
}