package com.amaze.filemanager.fileoperations.filesystem;

import androidx.annotation.IntDef;

public class FolderState {

    public static final int DOESNT_EXIST = 0;
    public static final int WRITABLE_OR_ON_SDCARD = 1;

    // For Android 5
    public static final int CAN_CREATE_FILES = 2;
    public static final int WRITABLE_ON_REMOTE = 3;

    @IntDef({DOESNT_EXIST, WRITABLE_OR_ON_SDCARD, CAN_CREATE_FILES, WRITABLE_ON_REMOTE})
    public @interface FolderStateAnnotation {
    }
}

