package com.nytaiji.epf.mp4Composer.FdSource;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;

public interface FdInterface {
    @NonNull
    FileDescriptor getFileDescriptor();

    interface Listener {
        void onError(Exception e);
    }
}
