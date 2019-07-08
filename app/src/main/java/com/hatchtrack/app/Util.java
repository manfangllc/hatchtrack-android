package com.hatchtrack.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

class Util {
    private static final String TAG = Util.class.getSimpleName();

    /**
     * Looks for asset "fileName" in "dir" directory.
     * If it's there then returns its File object.
     * If it's not there then copies the asset to dir and returns its File object.
     * If something goes wrong returns null.
     *
     * @param context
     * @param dir target directory on device for this asset to be copied. If the directory
     *            doesn't exist it will be created.
     * @param fileName name of the asset. Same as the name on the device copy.
     * @return a File corresponding to the device version of this asset or null if there's
     *  no asset or file found.
     */
    static File checkAsset(Context context, String dir, String fileName){
        File testFile = new File(context.getExternalFilesDir(dir), fileName);
        if(!testFile.exists()){
            AssetManager assetManager = context.getAssets();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(fileName);
                out = new FileOutputStream(testFile);
                copyFile(in, out);
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy asset file: " + fileName, e);
                testFile = null;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
        return(testFile);
    }

    /**
     *
     * @param in input stream
     * @param out output stream
     * @throws IOException
     */
    static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return;
    }

    /**
     * returns a Bitmap object from a picture file asset
     *
     * @param mgr
     * @param path
     * @return Bitmap or null
     */
    static Bitmap getBitmapFromAsset(AssetManager mgr, String path) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = mgr.open(path);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (final IOException e) {
            bitmap = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
        return(bitmap);
    }

    /**
     * Given an ImageView and an image path on the device
     *  insert the image into the ImageView by crossfading the
     *  new image with the ImageView's old image.
     *
     * @param context
     * @param view the destination ImageView
     * @param imagePath file path to the image
     */
    static void switchImages(Context context, ImageView view, String imagePath){
        Drawable drawables[] = new Drawable[2];
        Drawable d = view.getDrawable();
        if(d instanceof LayerDrawable){
            if(((LayerDrawable) d).getNumberOfLayers() > 1){
                drawables[0] = ((LayerDrawable) d).getDrawable(1);
            }
            else {
                drawables[0] = ((LayerDrawable) d).getDrawable(0);
            }
        }
        else {
            drawables[0] = d;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        drawables[1] = new BitmapDrawable(context.getResources(), bitmap);
        TransitionDrawable crossfader = new TransitionDrawable(drawables);
        crossfader.setCrossFadeEnabled(true);
        view.setImageDrawable(crossfader);
        crossfader.startTransition(1000);
    }

    interface UtilDoneCallback {
        void onDone(int n);
    }

    static void createCalendarTurns(Context context, long hatchId, String hatchName, float days, long startMillis, UtilDoneCallback callback){
        Calendar calNext = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        calNext.setTimeInMillis(startMillis);
        calEnd.setTimeInMillis(startMillis);
        calEnd.add(Calendar.HOUR_OF_DAY, (int)(24 * days));

        String eventTitle = "Hatch:" + hatchName;
        String eventLocation = "Location: Incubator";

        Uri eventUri;
        eventUri = Uri.parse("content://com.android.calendar/events");

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.TITLE, eventTitle);
        values.put(CalendarContract.Events.DESCRIPTION, "Turn eggs! (" + hatchId + ")");
        values.put(CalendarContract.Events.EVENT_LOCATION, eventLocation);
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        java.util.TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.RRULE, "FREQ=DAILY;COUNT=1");

        /* 06, 12, 18 */
        int h = calNext.get(Calendar.HOUR_OF_DAY);
        if(h < 6){
            h = 6 - h;
        } else if( h < 12){
            h = 12 - h;
        } else if(h < 18){
            h = 18 - h;
        } else {
            h = 30 - h;
        }
        calNext.add(Calendar.HOUR_OF_DAY, h);
        final long tEnd = calEnd.getTimeInMillis();
        long tNext = calNext.getTimeInMillis();
        Uri uri;
        while(tNext < tEnd){
            Log.e(TAG, "cal=" + calNext.toString());
            // write the event and reminder
            values.put(CalendarContract.Events.DTSTART, tNext);
            values.put(CalendarContract.Events.DTEND, tNext);
            uri = context.getContentResolver().insert(eventUri, values);
            if (uri != null) {
                Util.createCalendarReminder(context, Long.parseLong(uri.getLastPathSegment()));
            }
            // compute the next time
            if(calNext.get(Calendar.HOUR_OF_DAY) >= 18){
                calNext.add(Calendar.HOUR_OF_DAY, 12);
            } else {
                calNext.add(Calendar.HOUR_OF_DAY, 6);
            }
            tNext = calNext.getTimeInMillis();
        }
        if(callback != null){
            callback.onDone(0);
        }
    }

    static void createCalendarReminder(Context context, long eventId){
        try {
            Uri remindersUri;
            remindersUri = Uri.parse("content://com.android.calendar/reminders");
            ContentValues cv = new ContentValues();
            cv.put(CalendarContract.Reminders.EVENT_ID, eventId);
            //METHOD_DEFAULT = 0, METHOD_ALERT = 1, METHOD_EMAIL = 2, METHOD_SMS = 3, METHOD_ALARM = 4
            cv.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            cv.put(CalendarContract.Reminders.MINUTES, 1);
            context.getContentResolver().insert(remindersUri, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void removeTurnEvents(Context context, long hatchId, UtilDoneCallback callback){
        Uri eventUri = Uri.parse("content://com.android.calendar/events");
        String select = "(" + CalendarContract.Events.CALENDAR_ID + " = " + 1 + ") AND (" + CalendarContract.Events.DESCRIPTION + " LIKE '%Turn eggs! (" + hatchId + ")') AND (" + CalendarContract.Events.DELETED + " == 0)";
        int nrecs = context.getContentResolver().delete(
                eventUri,
                select,
                null
        );
        Log.e(TAG, "removeTurnEvents: deleted=" + nrecs + " records");
        if(callback != null){
            callback.onDone(0);
        }
    }

    static void removeTurnReminders(Context context, int hatchId, UtilDoneCallback callback){
        Uri eventUri = Uri.parse("content://com.android.calendar/events");
        Uri remindersUri = Uri.parse("content://com.android.calendar/reminders");
        String select = "(" + CalendarContract.Events.CALENDAR_ID + " = " + 1 + ") AND (" + CalendarContract.Events.DESCRIPTION + " LIKE '%Turn eggs! (" + hatchId + ")') AND (" + CalendarContract.Events.DELETED + " = 0)";
        Cursor cursor = context.getContentResolver().query(
                eventUri,
                new String[]{CalendarContract.Events._ID},
                select,
                null,
                null
        );
        int nrecs = 0;
        if(cursor != null){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String w = CalendarContract.Reminders.EVENT_ID + " = " + cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
                nrecs += context.getContentResolver().delete(remindersUri, w, null);
                cursor.moveToNext();
            }
            cursor.close();
        }
        Log.e(TAG, "removeTurnReminders: deleted=" + nrecs + " records");
        if(callback != null){
            callback.onDone(0);
        }
    }

    static void addTurnReminders(Context context, int hatchId, UtilDoneCallback callback){
        Uri eventUri = Uri.parse("content://com.android.calendar/events");
        String select = "(" + CalendarContract.Events.CALENDAR_ID + " = " + 1 + ") AND (" + CalendarContract.Events.DESCRIPTION + " LIKE '%Turn eggs! (" + hatchId + ")')  AND (" + CalendarContract.Events.DELETED + " = 0))";
        Cursor cursor = context.getContentResolver().query(
                eventUri,
                new String[]{CalendarContract.Reminders._ID},
                select,
                null,
                null
        );
        int nrecs = 0;
        if(cursor != null){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                Util.createCalendarReminder(context, cursor.getLong(cursor.getColumnIndex(CalendarContract.Reminders._ID)));
                cursor.moveToNext();
            }
            cursor.close();
        }
        Log.e(TAG, "removeTurnReminders: deleted=" + nrecs + " records");
        if(callback != null){
            callback.onDone(0);
        }
    }

    static String getLocalTimeString(long millis){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy hh:mm a");
        sdf.setTimeZone(cal.getTimeZone());
        return(sdf.format(new Date(millis)));
    }
}
