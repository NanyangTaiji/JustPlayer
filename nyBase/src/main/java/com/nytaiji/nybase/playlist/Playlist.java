package com.nytaiji.nybase.playlist;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;

public class Playlist {

    private int id;
    private String name;
    private boolean secure;

    public Playlist(int id, String name, boolean secure) {
        this.id = id;
        this.secure = secure;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isSecure() {
        return secure;
    }

    public int getId() {
        return id;
    }
}
