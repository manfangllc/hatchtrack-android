package com.hatchtrack.app.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class HatchtrackProvider extends ContentProvider {

    private static final String AUTHORITY = "com.hatchtrack.app.database.HatchtrackProvider";

    private static final int HATCH = 11;
    private static final String HATCH_BASE = "hatch";
    private static final int PEEP = 12;
    private static final String PEEP_BASE = "peep";
    private static final int HATCH_PEEP = 13;
    private static final String HATCH_PEEP_BASE = "hatch_peep";
    private static final int SPECIES = 14;
    private static final String SPECIES_BASE = "species";
    private static final int HATCH_SPECIES = 15;
    private static final String HATCH_SPECIES_BASE = "hatch_species";

    public static final Uri HATCH_URI = Uri.parse("content://" + AUTHORITY + "/" + HATCH_BASE);
    public static final Uri PEEP_URI = Uri.parse("content://" + AUTHORITY + "/" + PEEP_BASE);
    public static final Uri HATCH_PEEP_URI = Uri.parse("content://" + AUTHORITY + "/" + HATCH_PEEP_BASE);
    public static final Uri SPECIES_URI = Uri.parse("content://" + AUTHORITY + "/" + SPECIES_BASE);
    public static final Uri HATCH_SPECIES_URI = Uri.parse("content://" + AUTHORITY + "/" + HATCH_SPECIES_BASE);

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, HATCH_BASE, HATCH);
        uriMatcher.addURI(AUTHORITY, PEEP_BASE, PEEP);
        uriMatcher.addURI(AUTHORITY, HATCH_PEEP_BASE, HATCH_PEEP);
        uriMatcher.addURI(AUTHORITY, SPECIES_BASE, SPECIES);
        uriMatcher.addURI(AUTHORITY, HATCH_SPECIES_BASE, HATCH_SPECIES);
    }

    // database
    private HatchtrackDatabaseHelper database = null;

    @Override
    public boolean onCreate() {
        this.database = new HatchtrackDatabaseHelper(getContext());
        return (false);
    }

    @Override
    synchronized public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        String id = uri.getLastPathSegment();
        switch (uriType) {
            case HATCH:
                rowsDeleted = sqlDB.delete(HatchTable.TABLE_NAME, selection, selectionArgs);
                break;
            case PEEP:
                rowsDeleted = sqlDB.delete(PeepTable.TABLE_NAME, selection, selectionArgs);
                break;
            case HATCH_PEEP:
                rowsDeleted = sqlDB.delete(HatchPeepTable.TABLE_NAME, selection, selectionArgs);
                break;
            case SPECIES:
                rowsDeleted = sqlDB.delete(SpeciesTable.TABLE_NAME, selection, selectionArgs);
                break;
            case HATCH_SPECIES:
                rowsDeleted = sqlDB.delete(HatchSpeciesTable.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        ContentResolver cr;
        if ((cr = getContext().getContentResolver()) != null) {
            cr.notifyChange(uri, null);
        }
        return (rowsDeleted);
    }

    @Override
    synchronized public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // check uri
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case HATCH:
                queryBuilder.setTables(HatchTable.TABLE_NAME);
                break;
            case PEEP:
                queryBuilder.setTables(PeepTable.TABLE_NAME);
                break;
            case HATCH_PEEP:
                queryBuilder.setTables(HatchPeepTable.TABLE_NAME);
                break;
            case SPECIES:
                queryBuilder.setTables(SpeciesTable.TABLE_NAME);
                break;
            case HATCH_SPECIES:
                queryBuilder.setTables(HatchSpeciesTable.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // query
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        ContentResolver cr;
        if ((cr = getContext().getContentResolver()) != null) {
            cursor.setNotificationUri(cr, uri);
        }
        return (cursor);
    }

    @Override
    public String getType(Uri arg0) {
        return (null);
    }

    @Override
    synchronized public Uri insert(Uri uri, ContentValues values) {
        int uriType = HatchtrackProvider.uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        String basePath = "";
        switch (uriType) {
            case HATCH:
                id = sqlDB.insert(HatchTable.TABLE_NAME, null, values);
                basePath = HATCH_BASE;
                break;
            case PEEP:
                id = sqlDB.insert(PeepTable.TABLE_NAME, null, values);
                basePath = PEEP_BASE;
                break;
            case HATCH_PEEP:
                id = sqlDB.insert(HatchPeepTable.TABLE_NAME, null, values);
                basePath = HATCH_PEEP_BASE;
                break;
            case SPECIES:
                id = sqlDB.insert(SpeciesTable.TABLE_NAME, null, values);
                basePath = SPECIES_BASE;
                break;
            case HATCH_SPECIES:
                id = sqlDB.insert(HatchSpeciesTable.TABLE_NAME, null, values);
                basePath = HATCH_SPECIES_BASE;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        ContentResolver cr;
        if ((cr = getContext().getContentResolver()) != null) {
            cr.notifyChange(uri, null);
        }
        return (Uri.parse(basePath + "/" + id));
    }

    @Override
    synchronized public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = HatchtrackProvider.uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        String id = null;
        switch (uriType) {
            case HATCH:
                rowsUpdated = sqlDB.update(HatchTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PEEP:
                rowsUpdated = sqlDB.update(PeepTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case HATCH_PEEP:
                rowsUpdated = sqlDB.update(HatchPeepTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SPECIES:
                rowsUpdated = sqlDB.update(SpeciesTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case HATCH_SPECIES:
                rowsUpdated = sqlDB.update(HatchSpeciesTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        ContentResolver cr;
        if ((cr = getContext().getContentResolver()) != null) {
            cr.notifyChange(uri, null);
        }
        return (rowsUpdated);
    }
}