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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.nytaiji.nybase.view.TermsDialog;


public abstract class OldPermissionsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppPref.isNotAgreed(getApplication())) {
            new TermsDialog(this, new TermsDialog.TermCallback() {
                @Override
                public void agreedTerms(boolean yesOrno) {
                    if (yesOrno) {
                        invokePermissionCheck();
                    } else finish();
                }
            }).show();

        } else invokePermissionCheck();
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
        }
    }

    private boolean checkNotificationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean haveStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkStoragePermission();
        }
        return true;
    }

    private static final int PERMISSION_LENGTH = 5;
    private static final int STORAGE_PERMISSION = 0;
    private static final int ALL_FILES_PERMISSION = 1;
    private static final int LOCATION_PERMISSION = 2;
    private static final int NOTIFICATION_PERMISSION = 3;

    private final OnPermissionGranted[] permissionCallbacks = new OnPermissionGranted[PERMISSION_LENGTH];

    private final OnPermissionGranted onPermissionGranted = isGranted -> {
        if (isGranted) {
            postPermissionGranted();
        } else {
            Toast.makeText(this, R.string.grantfailed, Toast.LENGTH_SHORT).show();
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
            permissionCallbacks[STORAGE_PERMISSION].onPermissionGranted(isGranted(grantResults));
        } else if (requestCode == LOCATION_PERMISSION) {
            if (isGranted(grantResults)) {
                permissionCallbacks[LOCATION_PERMISSION].onPermissionGranted(true);
                permissionCallbacks[LOCATION_PERMISSION] = null;
            } else if (requestCode == NOTIFICATION_PERMISSION &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!isGranted(grantResults)) {
                    Toast.makeText(this, R.string.grantfailed, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.grant_location_failed, Toast.LENGTH_SHORT).show();
                requestStoragePermission(permissionCallbacks[LOCATION_PERMISSION], false);
                permissionCallbacks[LOCATION_PERMISSION].onPermissionGranted(false);
                permissionCallbacks[LOCATION_PERMISSION] = null;
            }
        }
    }

    public boolean checkStoragePermission() {
        // Verify that all required contact permissions have been granted.
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return (ActivityCompat.checkSelfPermission(
                    this, Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED)
                    || (ActivityCompat.checkSelfPermission(
                    this, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED)
                    || Environment.isExternalStorageManager();
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.gps_disabled))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    dialog.cancel();
                })
                .setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {
                    onPermissionGranted.onPermissionGranted(false);
                    dialog.cancel();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void initLocationResources(OnPermissionGranted onPermissionGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkLocationPermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
            builder.setMessage(R.string.grant_location_permission)
                    .setTitle(R.string.grant_permission)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    }).setCancelable(false);
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION, builder, onPermissionGranted, true);
            onPermissionGranted.onPermissionGranted(false);
        } else {
            onPermissionGranted.onPermissionGranted(true);
        }
    }

    private boolean checkLocationPermission() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission(OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setMessage(R.string.grant_storage_read_permission)
                .setTitle(R.string.grant_permission)
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish()).setCancelable(false);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION, builder, onPermissionGranted, isInitialStart);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission(OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setMessage(R.string.grant_notification_permission)
                .setTitle(R.string.grant_permission)
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish()).setCancelable(false);
        requestPermission(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION, builder, onPermissionGranted, isInitialStart);
    }

    private void requestPermission(String permission, int code, AlertDialog.Builder rationale, OnPermissionGranted onPermissionGranted, boolean isInitialStart) {
        permissionCallbacks[code] = onPermissionGranted;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            rationale.setPositiveButton(R.string.grant, (dialog, which) -> {
                ActivityCompat.requestPermissions(this, new String[]{permission}, code);
                dialog.cancel();
            });
            rationale.show();
        } else if (isInitialStart) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, code);
        } else {
            showToastInCenter(getString(R.string.grantfailed));
            finish();
        }
    }

    private void requestAllFilesAccess(OnPermissionGranted onPermissionGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
            builder.setMessage(R.string.grant_all_files_permission)
                    .setTitle(R.string.grant_permission)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                    .setPositiveButton(R.string.grant, (dialog, which) -> {
                        requestAllFilesAccessPermission(onPermissionGranted);
                        dialog.cancel();
                    }).setCancelable(false).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestAllFilesAccessPermission(
            @NonNull final OnPermissionGranted onPermissionGranted) {
      //  Utils.disableScreenRotation(this);
        permissionCallbacks[ALL_FILES_PERMISSION] = onPermissionGranted;
        try {
            Intent intent =
                    new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException anf) {
            // fallback
            try {
                Intent intent =
                        new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                .setData(Uri.parse("package:$packageName"));
                startActivity(intent);
            } catch (Exception e) {
                showToastInCenter(getString(R.string.grantfailed));
            }
        } catch (Exception e) {
           // Log.e(TAG, "Failed to initial activity to grant all files access", e);
            showToastInCenter(getString(R.string.grantfailed));
        }
    }

    private boolean isGranted(int[] grantResults) {
        return grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    public interface OnPermissionGranted {
        void onPermissionGranted(boolean isGranted);
    }

/*
    public static void showToastInCenter(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }*/

    public void showToastInCenter(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

