package com.nytaiji.nybase.libmediaStreamer;

public class SourceType {
    //the order should not be rearranged
    enum Source {
        DEFAULT,    //URI
        FILE,
        DOCUMENT_FILE,
        ZIP_ENTRY,
        SMB,
        ASSET_FILE,
        REMOTE,
        HYBRIDFILE;
    }
}
