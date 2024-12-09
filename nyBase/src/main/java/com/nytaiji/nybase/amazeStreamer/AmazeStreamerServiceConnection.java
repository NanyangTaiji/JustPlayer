package com.nytaiji.nybase.amazeStreamer;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nytaiji.nybase.NewPermissionsActivity;

import java.lang.ref.WeakReference;

public class AmazeStreamerServiceConnection implements ServiceConnection {

    private WeakReference<NewPermissionsActivity> activityRef;
    private AmazeStreamerService specificService;

    public AmazeStreamerServiceConnection(WeakReference<NewPermissionsActivity> activityRef) {
        this.activityRef = activityRef;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ObtainableServiceBinder<? extends AmazeStreamerService> binder =
                (ObtainableServiceBinder<? extends AmazeStreamerService>) service;
        specificService = binder.getService();
        if (specificService != null) {
            NewPermissionsActivity activity = activityRef.get();
            if (activity != null) {
                activity.amazeStreamerService = specificService;
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        NewPermissionsActivity activity = activityRef.get();
        if (activity != null) {
            activity.amazeStreamerService = null;
        }
    }
}

