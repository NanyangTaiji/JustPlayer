package com.nytaiji.nybase.permisssion;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class PermissionViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> _isRequestingStorageAccessLiveData = new MutableLiveData<>(false);

    public boolean isStorageAccessRequested() {
        return _isRequestingStorageAccessLiveData.getValue();
    }

    public void setStorageAccessRequested(boolean value) {
        _isRequestingStorageAccessLiveData.setValue(value);
    }

    private MutableLiveData<Boolean> _isRequestingNotificationPermissionLiveData = new MutableLiveData<>(false);

    public boolean isNotificationPermissionRequested() {
        return _isRequestingNotificationPermissionLiveData.getValue();
    }

    public void setNotificationPermissionRequested(boolean value) {
        _isRequestingNotificationPermissionLiveData.setValue(value);
    }


    public PermissionViewModel(Application applicationContext) {
        super(applicationContext);
    }
}
