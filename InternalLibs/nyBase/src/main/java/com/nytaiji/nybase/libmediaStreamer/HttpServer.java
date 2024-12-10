package com.nytaiji.nybase.libmediaStreamer;

import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;


import com.nytaiji.nybase.model.NyHybrid;

import javax.crypto.Cipher;

public interface HttpServer {
    String getURL(String path);

    String getURL(DocumentFile documentFile);

    String getURL(String zippath, String entry, String password);

    String getURL(int mainversion, int patchversion, String entry);

    String getURL(NyHybrid nyHybrid);

    @NonNull
    JsInterface getJsInterfaceObject();

    @NonNull
    HttpServer setDataSource(HttpSource dataSource);

    @NonNull
    HttpServer setCipher(Cipher cipher);

    void start();

    void stop();

    public interface JsInterface {
        @JavascriptInterface
        String getURL(String path);

        @JavascriptInterface
        String getURL(String path, String entry, String password);

        @JavascriptInterface
        String getURL(int mainversion, int patchversion, String entry);
    }
}

