package com.nytaiji.epf.mp4Composer.FdSource;

import android.util.Log;

import androidx.annotation.NonNull;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FilePathFd implements FdInterface {

    private final static String TAG = FilePathFd.class.getSimpleName();

    private FileDescriptor fileDescriptor;

    public FilePathFd(@NonNull String filePath, @NonNull Listener listener) {

        final File srcFile = new File(filePath);
        final FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(srcFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to find file", e);
            listener.onError(e);
            return;
        }

        try {
            fileDescriptor = fileInputStream.getFD();
        } catch (IOException e) {
            Log.e(TAG, "Unable to read input file", e);
            listener.onError(e);
        }
    }

    @NonNull
    @Override
    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }
}
