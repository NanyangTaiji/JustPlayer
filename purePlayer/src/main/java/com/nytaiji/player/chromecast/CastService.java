package com.nytaiji.player.chromecast;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

public final class CastService extends IntentService {
    private static CastService webservice;

    public static CastService getInstance() {
        return webservice;
    }

    private final String TAG = this.getClass().getSimpleName();

    public void onStart(@Nullable Intent intent, int startId) {
        // SimpleWebServer.stopServer();
        super.onStart(intent, startId);
        webservice = this;
    }

    protected void onHandleIntent(@Nullable Intent intent) {

    }

    public void onDestroy() {
        // SimpleWebServer.stopServer();
        Log.d(this.TAG, "Service destroyed");
        super.onDestroy();
    }

    public CastService() {
        super("blank");
    }
}
