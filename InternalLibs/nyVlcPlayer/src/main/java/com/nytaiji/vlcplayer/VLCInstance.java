package com.nytaiji.vlcplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.nytaiji.nybase.utils.AppContextProvider;

import org.videolan.libvlc.FactoryManager;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.ILibVLCFactory;
import org.videolan.libvlc.util.VLCUtil;


public class VLCInstance {
    private static final String TAG = "VLCInstance";

    @SuppressLint("StaticFieldLeak")
    private static ILibVLC sLibVLC;

    public static final ILibVLCFactory libVLCFactory = (ILibVLCFactory) FactoryManager.getFactory(ILibVLCFactory.factoryId);

    public static ILibVLC getInstance(Context context) {
        if (sLibVLC == null) {
            init(context.getApplicationContext());
        }
        return sLibVLC;
    }

    private static void init(Context ctx) {
        Thread.setDefaultUncaughtExceptionHandler(null);

        if (!VLCUtil.hasCompatibleCPU(ctx)) {
            Log.e(TAG, VLCUtil.getErrorMsg());
            throw new IllegalStateException("LibVLC initialization failed: " + VLCUtil.getErrorMsg());
        }

        sLibVLC = libVLCFactory.getFromOptions(ctx, VLCOptions.getLibOptions());
    }

    public static void restart() throws IllegalStateException {
        if (sLibVLC != null) {
            sLibVLC.release();
        }
        sLibVLC = libVLCFactory.getFromOptions(AppContextProvider.getAppContext(), VLCOptions.getLibOptions());
       // Medialibrary.getInstance().setLibVLCInstance(((LibVLC) sLibVLC).getInstance());
    }

}

