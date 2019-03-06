package com.hatchtrack.app.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 *
 */
public class HatchTable {
    private static final String   TAG = HatchTable.class.getSimpleName();

    // Database table
    public static final String TABLE_NAME     = "HatchTable";
    public static final String ID             = "_id";
    public static final String UUID           = "uuid";
    public static final String NAME           = "Name";
    public static final String SPECIES_ID     = "SpeciesId";
    public static final String EGG_COUNT      = "EggCount";
    public static final String CHICK_COUNT    = "ChickCount";
    public static final String CREATED        = "CreatedTime";
    public static final String LAST_SYNCED    = "LastSynced";
    public static final String LAST_MODIFIED  = "LastModified";
    public static final String START          = "StartTime";
    public static final String END            = "EndTime";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + ID + " integer primary key autoincrement, "
            + UUID + " text not null, "
            + NAME + " text not null, "
            + SPECIES_ID + " integer not null, "
            + EGG_COUNT + " integer not null, "
            + CHICK_COUNT + " integer not null, "
            + CREATED + " integer not null, "
            + LAST_SYNCED + " integer not null, "
            + LAST_MODIFIED + " integer not null, "
            + START + " integer not null, "
            + END + " integer not null "
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