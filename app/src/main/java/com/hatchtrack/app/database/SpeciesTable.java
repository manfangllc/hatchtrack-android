package com.hatchtrack.app.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;

/**
 *
 */
public class SpeciesTable {
    private static final String   TAG = SpeciesTable.class.getSimpleName();

    // Database table
    public static final String TABLE_NAME     = "SpeciesTable";
    public static final String ID             = "_id";
    public static final String NAME           = "Name";
    public static final String PICTURE_URI    = "PicUri";
    public static final String DAYS           = "Days";
    public static final String LAST_SYNCED    = "LastSynced";
    public static final String LAST_MODIFIED  = "LastModified";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + ID + " integer primary key autoincrement, "
            + DAYS + " real not null, "
            + NAME + " text not null, "
            + LAST_SYNCED + " integer not null, "
            + LAST_MODIFIED + " integer not null, "
            + PICTURE_URI + " text"
            + ");";

    static void onCreate(SQLiteDatabase database) {
        Log.v(TAG, "onCreate(): " + DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE);
//        database.execSQL("PRAGMA journal_mode = WAL;");
        database.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (1, 21, 'chicken', 0, 0, NULL)");
        database.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (2, 28, 'duck', 0, 0, NULL)");
        database.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (3, 17, 'quail', 0, 0, NULL)");
        database.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (4, 30, 'goose', 0, 0, NULL)");
        database.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (5, 27.5, 'turkey', 0, 0, NULL)");
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