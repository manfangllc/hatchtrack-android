package com.hatchtrack.app.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 *
 */
public class HatchPeepTable {
    private static final String   TAG = HatchPeepTable.class.getSimpleName();

    // Database table
    public static final String TABLE_NAME   = "HatchPeepTable";
    public static final String ID           = "_id";
    public static final String HATCH_ID     = "HatchId";
    public static final String PEEP_ID      = "PeepId";
    public static final String TIME_ADDED   = "TimeAdded";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + ID + " integer primary key autoincrement, "
            + HATCH_ID + " integer not null,"
            + TIME_ADDED + " integer not null,"
            + PEEP_ID + " integer not null "
            + ");";

    static void onCreate(SQLiteDatabase database) {
        Log.v(TAG, "onCreate(): " + DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE);
//        database.execSQL("PRAGMA journal_mode = WAL;");
        return;
    }

    static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
        return;
    }
}