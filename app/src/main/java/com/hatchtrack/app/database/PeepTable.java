package com.hatchtrack.app.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 *
 */
public class PeepTable {
    private static final String   TAG = PeepTable.class.getSimpleName();

    // Database table
    public static final String TABLE_NAME     = "PeepTable";
    public static final String ID             = "_id";
    public static final String PEEP_ID        = "id";
    public static final String HATCH_ID       = "HatchId";
    public static final String NAME           = "Name";
    public static final String BATTERY        = "Battery";
    public static final String TEMPERATURE    = "Temperature";
    public static final String HUMIDITY       = "Humidity";
    public static final String AIR_PRESSURE   = "AirPressure";
    public static final String AIR_QUALITY    = "AirQuality";
    public static final String IS_CONNECTED   = "IsConnected";
    public static final String LAST_SYNCED    = "LastSynced";
    public static final String LAST_MODIFIED  = "LastModified";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + ID + " integer primary key autoincrement, "
            + PEEP_ID + " integer not null, "
            + BATTERY + " integer, "
            + TEMPERATURE + " integer, "
            + HUMIDITY + " integer, "
            + AIR_PRESSURE + " integer, "
            + AIR_QUALITY + " integer, "
            + IS_CONNECTED + " integer, "
            + LAST_SYNCED + " integer not null, "
            + LAST_MODIFIED + " integer not null, "
            + HATCH_ID + " integer not null, "
            + NAME + " text"
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