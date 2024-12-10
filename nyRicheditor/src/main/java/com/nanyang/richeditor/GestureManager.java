package com.nanyang.richeditor;

import android.content.Context;
import android.content.res.AssetManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.util.Log;
import android.widget.Toast;


import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class GestureManager {

    private GestureLibrary mGestureLib;
    private Context mContext;
    private static volatile GestureManager instance;


    private GestureManager(Context context) {
        this.mContext = context;
        //   mGestureLib = GestureLibraries.fromRawResource(mContext, R.raw.gestures);
        File gestureStore = new File(NyFileUtil.getAppDirectory(context), "gestures");
        if (gestureStore.exists() && gestureStore.length() > 0) {
            mGestureLib = GestureLibraries.fromFile(gestureStore);
            mGestureLib.load();
        } else copyFromAsset(gestureStore);
    }

    private void copyFromAsset(File gestureStore) {
        //implemented first time
        AssetManager assetManager = mContext.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open("gestures");
            out = new FileOutputStream(gestureStore);
            NyFileUtil.copyFileStream(in, out);
        } catch (IOException e) {
            Log.e("GestureManager", "Failed to copy asset gestures", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {

                }
            }
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                    out = null;
                } catch (IOException e) {

                }
            }
            mGestureLib = GestureLibraries.fromFile(gestureStore);
            mGestureLib.load();
        }
    }

    public static GestureManager getInstance(Context context) {
        if (instance == null) {
            synchronized (GestureManager.class) {
                if (instance == null) {
                    instance = new GestureManager(context);
                }
            }
        }
        return instance;
    }

    public GestureLibrary getGestureLib() {
        return mGestureLib;
    }

    public void setGestureLib(File file){
        mGestureLib = GestureLibraries.fromFile(file);
        mGestureLib.load();
    }

    public void changeBackGesture(Gesture gesture) {
        mGestureLib.removeEntry("back");
        mGestureLib.addGesture("back", gesture);
        mGestureLib.save();
        Toast.makeText(mContext, "'back' changed successfully", Toast.LENGTH_SHORT).show();
    }

    public void changeRefreshGesture(Gesture gesture) {
        mGestureLib.removeEntry("refresh");
        mGestureLib.addGesture("refresh", gesture);
        mGestureLib.save();
        Toast.makeText(mContext, "'refresh' changed successfully", Toast.LENGTH_SHORT).show();
    }

    public void changeGoLeftGesture(Gesture gesture) {
        mGestureLib.removeEntry("left");
        mGestureLib.addGesture("left", gesture);
        mGestureLib.save();
        Toast.makeText(mContext, "'Go left' changed successfully", Toast.LENGTH_SHORT).show();
    }

    public void changeGoRightGesture(Gesture gesture) {
        mGestureLib.removeEntry("right");
        mGestureLib.addGesture("right", gesture);
        mGestureLib.save();
        Toast.makeText(mContext, "'Go right' changed successfully", Toast.LENGTH_SHORT).show();
    }

    public void changeGoUpGesture(Gesture gesture) {
        mGestureLib.removeEntry("up");
        mGestureLib.addGesture("upt", gesture);
        mGestureLib.save();
        Toast.makeText(mContext, "'Go Up' changed successfully", Toast.LENGTH_SHORT).show();
    }

    public void changeGoDownGesture(Gesture gesture) {
        mGestureLib.removeEntry("down");
        mGestureLib.addGesture("down", gesture);
        mGestureLib.save();
        Toast.makeText(mContext, "'Go Down' changed successfully", Toast.LENGTH_SHORT).show();
    }

}
