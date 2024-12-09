package com.nytaiji.nybase.playlist;


import android.net.Uri;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;

public class PlaylistFile {

    private String path;
    private boolean secure;
    private OpenMode openMode;

    public PlaylistFile(String path, boolean secure, OpenMode openMode) {
        this.path = path;
        this.secure = secure;
        this.openMode = openMode;
    }

    public String getPath() {
        return path;
    }

    public boolean isSecure() {
        return secure;
    }

    public OpenMode getOpenMode() {
        return openMode;
    }


}
