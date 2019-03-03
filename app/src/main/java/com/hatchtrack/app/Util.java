package com.hatchtrack.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
