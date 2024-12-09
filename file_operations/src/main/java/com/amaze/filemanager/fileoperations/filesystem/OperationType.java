package com.amaze.filemanager.fileoperations.filesystem;

import androidx.annotation.IntDef;

public class OperationType {

    public static final int UNDEFINED = -1;
    public static final int DELETE = 0;
    public static final int COPY = 1;
    public static final int MOVE = 2;
    public static final int NEW_FOLDER = 3;
    public static final int RENAME = 4;
    public static final int NEW_FILE = 5;
    public static final int EXTRACT = 6;
    public static final int COMPRESS = 7;
    public static final int SAVE_FILE = 8;

    @IntDef({UNDEFINED, DELETE, COPY, MOVE, NEW_FOLDER, RENAME, NEW_FILE, EXTRACT, COMPRESS, SAVE_FILE})
    public @interface OperationTypeAnnotation {
    }
}

