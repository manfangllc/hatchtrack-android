package com.hatchtrack.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Data {

    private static final String TAG = Data.class.getSimpleName();

    private Data(){
    }

    /**
     *
     * @param context app context
     * @param uri some database table URI
     * @param id database row id
     * @return a string representation of all the columns in the given row
     */
    public static StringBuilder getRowText(Context context, Uri uri, int id){
        StringBuilder sb = new StringBuilder();
        Cursor c = context.getContentResolver().query(uri,null, "_id = " + id, null, null);
        if(c != null){
            if(c.moveToFirst()){
                String[] cNames = c.getColumnNames();
                for(int i = 0; i < cNames.length; i++) {
                    sb.append(cNames[i]);
                    sb.append(": ");
                    sb.append(c.getString(c.getColumnIndex(cNames[i])));
                    sb.append('\n');
                }
            }
            c.close();
        }
        return(sb);
    }

    /**
     * Who the heck would need to get a String array of the table names in a database?
     *
     * @param context app context
     * @return array of table names
     */
    public static String[] getTableNames(Context context){
        String[] result = new String[0];
        SQLiteDatabase database = (new HatchtrackDatabaseHelper(context)).getReadableDatabase();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name!='android_metadata' AND name!='sqlite_sequence' order by name", null);
        if(c != null){
            if (c.moveToFirst()) {
                result = new String[c.getCount()];
                int i = 0;
                while ( !c.isAfterLast() ) {
                    result[i] = c.getString(c.getColumnIndex("name"));
                    c.moveToNext();
                    i++;
                }
            }
            c.close();
        }
        return(result);
    }

    /**
     * Writes the table name and all the rows in the given data table to a string delimited by newlines
     *
     * @param context app context
     * @param uri Sqlite table
     * @return string of the table's data
     */
    public static String dumpTable(Context context, Uri uri) {
        String s = uri.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(s.substring(s.lastIndexOf('/') + 1));
        sb.append(":\n");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            sb = Data.dumpCursor(cursor, sb);
            cursor.close();
        }
        return(sb.toString());
    }

    /**
     * Writes all the rows of a given cursor to a string delimeted by newlines
     *
     * @param cursor app context
     * @param sb
     * @return string of all the data rows in the cursor
     */
    public static StringBuilder dumpCursor(Cursor cursor, StringBuilder sb) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String[] colNames = cursor.getColumnNames();
                int nCols = colNames.length;
                for (int i = 0; i < nCols; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(colNames[i]);
                }
                sb.append('\n');
                while (!cursor.isAfterLast()) {
                    for (int i = 0; i < nCols; i++) {
                        String value = cursor.getString(cursor.getColumnIndex(colNames[i]));
                        if(value == null){
                            value = "<null>";
                        }
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(value);
                    }
                    sb.append('\n');
                    cursor.moveToNext();
                }
            }
        }
        return(sb);
    }

    /**
     * Sets all the peeps of a hatch to the peeps in the given peepId list preserving timestamps of peeps which already belong to the hatch
     *
     * @param context the app context
     * @param hatchId
     * @param peepIds a list of the peeps for the given hatch
     * @param time current timestamp
     */
    public static void setHatchPeeps(Context context, int hatchId, List<Integer> peepIds, long time) {
        ContentValues cv = new ContentValues();
        // if this hatch has a peep not in the peepIds list remove it from the hatch
        cv.put(PeepTable.HATCH_ID, 0);
        List<Integer> hatchPeeps = Data.getPeepsInHatch(context, hatchId);
        for (int i = 0; i < hatchPeeps.size(); i++) {
            if (!peepIds.contains(hatchPeeps.get(i))) {
                Data.removePeepFromHatch(context, hatchId, hatchPeeps.get(i), time);
            }
        }
        // if the peepIds list has a peep which is not in the hatch add it to the hatch
        for (int i = 0; i < peepIds.size(); i++) {
            if (!hatchPeeps.contains(peepIds.get(i))) {
                Data.addPeepToHatch(context, hatchId, peepIds.get(i), time);
            }
        }
    }

    /**
     * Removes one peep from a hatch.
     *
     * @param context the app context
     * @param hatchId
     * @param peepId
     * @param time current timestamp
     */
    public static void removePeepFromHatch(Context context, int hatchId, int peepId, long time){
        Log.i(TAG, "removePeepFromHatch(hatch=" + hatchId + ", peep=" + peepId + ")");
        ContentValues cv = new ContentValues();
        cv.put(PeepTable.HATCH_ID, 0);
        // remove the peep from the HatchPeep table
        context.getContentResolver().delete(HatchtrackProvider.HATCH_PEEP_URI,
                HatchPeepTable.PEEP_ID + " = " + peepId,
                null
        );
        // clear the hatch column of the peep's row in the Peep table
        context.getContentResolver().update(HatchtrackProvider.PEEP_URI,
                cv,
                PeepTable.ID + " = " + peepId,
                null
        );
        // update modified time for this hatch
        Data.setModifiedHatch(context, hatchId, time);
    }

    /**
     *  Adds one peep to a hatch.
     *
     * @param context app context
     * @param hatchId
     * @param peepId
     * @param time current timestamp
     */
    public static void addPeepToHatch(Context context, int hatchId, int peepId, long time){
        Log.i(TAG, "addPeepToHatch(hatch=" + hatchId + ", peep=" + peepId + ")");
        ContentValues cv = new ContentValues();
        // write the hatch id to this peep's row in the Peep table
        cv.put(PeepTable.HATCH_ID, hatchId);
        context.getContentResolver().update(HatchtrackProvider.PEEP_URI, cv, PeepTable.ID + " = " + peepId, null);
        // insert this hatch/peep combo into the hatch-peep table
        cv.clear();
        cv.put(HatchPeepTable.HATCH_ID, hatchId);
        cv.put(HatchPeepTable.PEEP_ID, peepId);
        cv.put(HatchPeepTable.TIME_ADDED, time);
        context.getContentResolver().insert(HatchtrackProvider.HATCH_PEEP_URI, cv);
        // update modified time for this hatch
        Data.setModifiedHatch(context, hatchId, time);
    }

    /**
     * Sets a hatch's last-modified timestamp.
     *
     * @param context app context
     * @param hatchId
     * @param time
     */
    public static void setModifiedHatch(Context context, int hatchId, long time){
        if(hatchId > 0) {
            ContentValues cv = new ContentValues();
            cv.put(HatchTable.LAST_MODIFIED, time);
            context.getContentResolver().update(HatchtrackProvider.HATCH_URI, cv, HatchTable.ID + " = " + hatchId, null);
        }
    }

    /**
     * Returns a hatchId given a peepId. (Any peep can only be in one hatch at a time.)
     *
     * @param context app context
     * @param peepId
     * @return hatch id of peep
     */
    public static int getHatchForPeep(Context context, int peepId){
        int result = 0;
        // get this peep's hatch
        Cursor c = context.getContentResolver().query(HatchtrackProvider.HATCH_PEEP_URI,
                new String[]{HatchPeepTable.HATCH_ID},
                HatchPeepTable.PEEP_ID + " = " + peepId,
                null,
                null
        );
        if(c != null){
            if(c.moveToFirst()){
                result = c.getInt(c.getColumnIndex(HatchPeepTable.HATCH_ID));
            }
            c.close();
        }
        return(result);
    }

    /**
     * Returns a List of all the peeps in a given hatch.
     *
     * @param context app context
     * @param hatchId
     * @return list of peeps
     */
    public static List<Integer> getPeepsInHatch(Context context, int hatchId){
        List<Integer> result = new ArrayList<>();
        Cursor c = context.getContentResolver().query(HatchtrackProvider.HATCH_PEEP_URI,
                new String[]{HatchPeepTable.PEEP_ID},
                HatchPeepTable.HATCH_ID + " = " + hatchId,
                null,
                null
        );
        if(c != null){
            if(c.moveToFirst()){
                while(!c.isAfterLast()) {
                    result.add(c.getInt(c.getColumnIndex(HatchPeepTable.PEEP_ID)));
                    c.moveToNext();
                }
            }
            c.close();
        }
        return(result);
    }

    /**
     * Removes all the peeps from the database.
     *
     * @param context app context
     */
    public static void removePeeps(Context context) {
        Cursor c = context.getContentResolver().query(HatchtrackProvider.PEEP_URI,
                new String[]{PeepTable.ID},
                null,
                null,
                null
        );
        if(c != null){
            if(c.moveToFirst()){
                while(!c.isAfterLast()) {
                    int peepId = c.getInt(c.getColumnIndex(PeepTable.ID));
                    Data.removePeep(context, peepId);
                    c.moveToNext();
                }
            }
            c.close();
        }
    }

    /**
     * Removes the given peep from the database. Updates last-modified time for the peep's hatch if it exists.
     *  Updates the HatchPeep table.
     *
     * @param context app context
     * @param peepId
     */
    public static void removePeep(Context context, int peepId){
        long now = System.currentTimeMillis();
        Log.i(TAG, "removePeep(peep=" + peepId + ")");
        // delete this peep's row from the peep table
        context.getContentResolver().delete(HatchtrackProvider.PEEP_URI,PeepTable.ID + " = " + peepId, null);
        // get this peep's hatch and update its modified time
        Data.setModifiedHatch(context, Data.getHatchForPeep(context, peepId), now);
        // remove peep from the hatch-peep table
        context.getContentResolver().delete(HatchtrackProvider.HATCH_PEEP_URI, HatchPeepTable.PEEP_ID + " = " + peepId, null);
    }

    /**
     * Removes all the hatches from the database.
     *
     * @param context app context
     */
    public static void removeHatches(Context context) {
        Cursor c = context.getContentResolver().query(HatchtrackProvider.HATCH_URI,
                new String[]{PeepTable.ID},
                null,
                null,
                null
        );
        if(c != null){
            if(c.moveToFirst()){
                while(!c.isAfterLast()) {
                    int hatchId = c.getInt(c.getColumnIndex(PeepTable.ID));
                    Data.removeHatch(context, hatchId);
                    c.moveToNext();
                }
            }
            c.close();
        }
    }

    /**
     * Removes the given hatch from the Hatch table. Updates the HatchPeep table.
     *
     * @param context app context
     * @param hatchId
     */
    public static void removeHatch(Context context, int hatchId){
        long now = System.currentTimeMillis();
        Log.i(TAG, "removeHatch(hatch=" + hatchId + ")");
        // delete this hatch's row from the hatch table
        context.getContentResolver().delete(HatchtrackProvider.HATCH_URI,HatchTable.ID + " = " + hatchId, null);
        // remove this hatch from the hatch-peep table
        context.getContentResolver().delete(HatchtrackProvider.HATCH_PEEP_URI, HatchPeepTable.HATCH_ID + " = " + hatchId, null);
    }

    /**
     *
     * @param context app context
     * @param speciesId
     * @return the number of days of incubation
     */
    public static float getSpeciesDays(Context context, int speciesId){
        float result = 0;
        Cursor c = context.getContentResolver().query(HatchtrackProvider.SPECIES_URI,
                new String[]{SpeciesTable.DAYS},
                SpeciesTable.ID + " = " + speciesId,
                null,
                null);
        if(c != null){
            if(c.moveToFirst()){
                result = c.getFloat(c.getColumnIndex(SpeciesTable.DAYS));
            }
            c.close();
        }
        return(result);
    }

    /**
     *
     * @param context app context
     * @param hatchId
     * @return the number of days of incubation or 0 if there's no species associated with the given hatch
     */
    public static float getSpeciesDaysFromHatch(Context context, int hatchId){
        float result = 0;
        Cursor c = context.getContentResolver().query(HatchtrackProvider.HATCH_URI,
                new String[]{HatchTable.SPECIES_ID},
                HatchTable.ID + " = " + hatchId,
                null,
                null);
        if(c != null){
            if(c.moveToFirst()){
                result = Data.getSpeciesDays(context, c.getInt(c.getColumnIndex(HatchTable.SPECIES_ID)));
            }
            c.close();
        }
        return(result);
    }

    /**
     *
     * @param context app context
     * @param hatchId
     * @param speciesId species for hatch
     * @return number of hatches updated. should be 1 if the hatch exists, else 0
     */
    public static int setHatchSpecies(Context context, int hatchId, int speciesId){
        ContentValues cv = new ContentValues();
        cv.put(HatchTable.SPECIES_ID, speciesId);
        cv.put(HatchTable.LAST_MODIFIED, System.currentTimeMillis());
        return(context.getContentResolver().update(HatchtrackProvider.HATCH_URI, cv, HatchTable.ID + " = " + hatchId, null));
    }
}
