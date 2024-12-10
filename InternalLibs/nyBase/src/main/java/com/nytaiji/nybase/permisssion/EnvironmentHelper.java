package com.nytaiji.nybase.permisssion;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

public class EnvironmentHelper {

    public static boolean supportsExternalStorageManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return isManageAppAllFilesAccessPermissionIntentResolved(context);
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Intent createManageAppAllFilesAccessPermissionIntent(String packageName) {
        return new Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.fromParts("package", packageName, null)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    static boolean isManageAppAllFilesAccessPermissionIntentResolved(Context context) {
        try {
            Intent intent = createManageAppAllFilesAccessPermissionIntent(context.getPackageName());
            return intent.resolveActivity(context.getPackageManager()) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
