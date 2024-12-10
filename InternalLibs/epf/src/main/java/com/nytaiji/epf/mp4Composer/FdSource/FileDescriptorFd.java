package com.nytaiji.epf.mp4Composer.FdSource;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;

public class FileDescriptorFd implements FdInterface {

    private final FileDescriptor fileDescriptor;

    public FileDescriptorFd(FileDescriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    @NonNull
    @Override
    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }
}
