package com.nytaiji.epf.mp4Composer.FdSource;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;

public class UriFd implements FdInterface {

    private final static String TAG = UriFd.class.getSimpleName();

    private FileDescriptor fileDescriptor;

    public UriFd(@NonNull Uri uri, @NonNull Context context,  @NonNull Listener listener) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to find file", e);
            listener.onError(e);
            return;
        }
        fileDescriptor = parcelFileDescriptor.getFileDescriptor();
    }

    @NonNull
    @Override
    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }
}
