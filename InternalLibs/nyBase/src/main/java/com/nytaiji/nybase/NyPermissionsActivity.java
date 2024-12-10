package com.nytaiji.nybase;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.nytaiji.nybase.view.TermsDialog;

@RequiresApi(api = Build.VERSION_CODES.M)
public abstract class NyPermissionsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppPref.isNotAgreed(getApplication())) {
            showTermsDialog();
        } else {
            invokePermissionCheck();
        }
    }

    private void showTermsDialog() {
        new TermsDialog(this, yesOrNo -> {
            if (yesOrNo) {
                invokePermissionCheck();
            } else {
                finish();
            }
        }).show();
    }

    public void invokePermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !checkStoragePermission()) {
                requestStoragePermission(onPermissionGranted, true);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requestAllFilesAccess(onPermissionGranted);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !checkNotificationPermission()) {
                requestNotificationPermission(onPermissionGranted, true);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !checkInstallApkPermission()) {
                requestInstallApkPermission(onPermissionGranted, true);
            }
        }
    }

    private boolean checkNotificationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkInstallApkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getPackageManager().canRequestPackageInstalls();
        }
        return true; // Permissions below Oreo do not require explicit install permission
    }


    public boolean haveStoragePermissions() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkStoragePermission();
    }

    private static final int PERMISSION_LENGTH = 5;
    private static final int STORAGE_PERMISSION = 0;
    private static final int ALL_FILES_PERMISSION = 1;
    private static final int LOCATION_PERMISSION = 2;
    private static final int NOTIFICATION_PERMISSION = 3;
    private static final int INSTALL_APK_PERMISSION = 4;

    private final OnPermissionGranted[] permissionCallbacks = new OnPermissionGranted[PERMISSION_LENGTH + 1];

    private final OnPermissionGranted onPermissionGranted = isGranted -> {
        if (isGranted) {
            postPermissionGranted();
        } else {
            Toast.makeText(NyPermissionsActivity.this, R.string.grantfailed, Toast.LENGTH_SHORT).show();
            requestStoragePermission(permissionCallbacks[STORAGE_PERMISSION], false);
        }
        permissionCallbacks[STORAGE_PERMISSION] = null;
    };

    protected abstract void postPermissionGranted();

    public void checkForExternalPermission(OnPermissionGranted permissionGranted) {
        if (!checkStoragePermission()) {
            requestStoragePermission(permissionGranted, true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestAllFilesAccess(permissionGranted);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            handlePermissionResult(permissionCallbacks[STORAGE_PERMISSION], grantResults);
        } else if (requestCode == LOCATION_PERMISSION) {
            handlePermissionResult(permissionCallbacks[LOCATION_PERMISSION], grantResults);
        } else if (requestCode == NOTIFICATION_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handlePermissionResult(permissionCallbacks[NOTIFICATION_PERMISSION], grantResults);
        }
    }

    private void handlePermissionResult(OnPermissionGranted callback, int[] grantResults) {
        if (callback != null) {
            callback.onPermissionGranted(isGranted(grantResults));
        }
    }

    public boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager() ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void isLocationEnabled(OnPermissionGranted onPermissionGranted) {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(onPermissionGranted);
            onPermissionGranted.onPermissionGranted(false);
        } else {
            onPermissionGranted.onPermissionGranted(true);
        }
    }

    private void buildAlertMessageNoGps(OnPermissionGranted onPermissionGranted) {
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage(getResources().getString(R.string.gps_disabled))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    dialog.cancel();
                })
                .setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {
                    onPermissionGranted.onPermissionGranted(false);
                    dialog.cancel();
                })
                .create()
                .show();
    }

    public void initLocationResources(OnPermissionGranted onPermissionGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkLocationPermission()) {
            requestLocationPermission(onPermissionGranted, true);
        } else {
            onPermissionGranted.onPermissionGranted(true);
        }
    }

    public boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    public void requestLocationPermission(OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION,
                R.string.grant_location_permission, onPermissionGranted, isInitialStart);
    }


    public void requestStoragePermission(OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION,
                R.string.grant_storage_read_permission, onPermissionGranted, isInitialStart);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void requestNotificationPermission(OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        requestPermission(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION,
                R.string.grant_notification_permission, onPermissionGranted, isInitialStart);
    }

    private void requestPermission(String permission, int code, int rationaleMessageId, OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        permissionCallbacks[code] = onPermissionGranted;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showPermissionRationaleDialog(permission, code, rationaleMessageId);
        } else if (isInitialStart) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, code);
        } else {
            showToastInCenter(getString(R.string.grantfailed));
            finish();
        }
    }

    private void showPermissionRationaleDialog(String permission, int code, int rationaleMessageId) {
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage(rationaleMessageId)
                .setTitle(R.string.grant_permission)
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .setPositiveButton(R.string.grant, (dialog, which) -> {
                    ActivityCompat.requestPermissions(NyPermissionsActivity.this, new String[]{permission}, code);
                    dialog.cancel();
                })
                .setCancelable(false)
                .show();
    }

    public void requestAllFilesAccess(OnPermissionGranted onPermissionGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            new AlertDialog.Builder(this, R.style.DialogTheme)
                    .setMessage(R.string.grant_all_files_permission)
                    .setTitle(R.string.grant_permission)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                    .setPositiveButton(R.string.grant, (dialog, which) -> {
                        requestAllFilesAccessPermission(onPermissionGranted);
                        dialog.cancel();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestAllFilesAccessPermission(OnPermissionGranted onPermissionGranted) {
        permissionCallbacks[ALL_FILES_PERMISSION] = onPermissionGranted;
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            showToastInCenter(getString(R.string.grantfailed));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void requestInstallApkPermission(OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        permissionCallbacks[INSTALL_APK_PERMISSION] = onPermissionGranted;
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage(R.string.grant_apkinstall_permission)
                .setTitle(R.string.grant_permission)
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .setPositiveButton(R.string.grant, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            .setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, INSTALL_APK_PERMISSION);
                    dialog.cancel();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INSTALL_APK_PERMISSION) {
            boolean isGranted = checkInstallApkPermission();
            permissionCallbacks[INSTALL_APK_PERMISSION].onPermissionGranted(isGranted);
        }
    }

    private boolean isGranted(int[] grantResults) {
        return grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    public interface OnPermissionGranted {
        void onPermissionGranted(boolean isGranted);
    }

    public void showToastInCenter(String message) {
        Toast.makeText(NyPermissionsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}


