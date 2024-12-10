package com.nytaiji.nybase.permisssion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.nytaiji.nybase.NyDisplayFragment;
import com.nytaiji.nybase.model.NyHybrid;

public class PermissionFragment extends Fragment implements
        RequestAllFilesAccessRationale.Listener,
        RequestNotificationPermissionRationale.Listener,
        RequestNotificationPermissionInSettingsRationale.Listener,
        RequestStoragePermissionRationale.Listener,
        RequestStoragePermissionInSettingsRationale.Listener {
    protected static int containerId;
    protected final static String TAG = "PermissionActivity";
    private RequestAllFilesAccessContract requestAllFilesAccessContract;
    private RequestPermissionInSettingsContract requestStoragePermissionInSettingsContract;
    private RequestPermissionInSettingsContract requestNotificationPermissionInSettingsContract;

    private ActivityResultLauncher<Boolean> requestAllFilesAccessLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    private ActivityResultLauncher<Boolean> requestStoragePermissionInSettingsLauncher;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;
    private ActivityResultLauncher<Boolean> requestNotificationPermissionInSettingsLauncher;

    private PermissionViewModel viewModel;

    public static void show(FragmentManager fm, int fragmentContainerId) {
        containerId = fragmentContainerId;
        PermissionFragment fragment = new PermissionFragment();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(fragmentContainerId, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public PermissionFragment() {// Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PermissionViewModel.class);


        requestAllFilesAccessContract = new RequestAllFilesAccessContract();
        requestStoragePermissionInSettingsContract = new RequestPermissionInSettingsContract(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestNotificationPermissionInSettingsContract = new RequestPermissionInSettingsContract(Manifest.permission.POST_NOTIFICATIONS);

        requestAllFilesAccessLauncher = registerForActivityResult(requestAllFilesAccessContract, this::onRequestAllFilesAccessResult);
        requestStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onRequestStoragePermissionResult);
        requestStoragePermissionInSettingsLauncher = registerForActivityResult(requestStoragePermissionInSettingsContract, this::onRequestStoragePermissionInSettingsResult);
        requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onRequestNotificationPermissionResult);
        requestNotificationPermissionInSettingsLauncher = registerForActivityResult(requestNotificationPermissionInSettingsContract, this::onRequestNotificationPermissionInSettingsResult);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!viewModel.isNotificationPermissionRequested()) {
            ensureStorageAccess();
        }
        if (!viewModel.isStorageAccessRequested()) {
            ensureNotificationPermission();
        }
    }

    private void ensureStorageAccess() {
        if (viewModel.isStorageAccessRequested()) {
            return;
        }
        if (supportsExternalStorageManager(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!isManageAppAllFilesAccessPermissionIntentResolved(getActivity())) {
                    RequestAllFilesAccessRationale.show(this);
                    viewModel.setStorageAccessRequested(true);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    RequestStoragePermissionRationale.show(this);
                } else {
                    requestStoragePermission();
                }
                viewModel.setStorageAccessRequested(true);
            }
        }
    }

    @Override
    public void onShowRequestAllFilesAccessRationaleResult(boolean shouldRequest) {
        if (shouldRequest) {
            requestAllFilesAccess();
        } else {
            viewModel.setStorageAccessRequested(false);
            ensureNotificationPermission();
        }
    }

    private void requestAllFilesAccess() {
        requestAllFilesAccessLauncher.launch(true);
    }

    private void onRequestAllFilesAccessResult(boolean isGranted) {
        viewModel.setStorageAccessRequested(false);
        if (isGranted) {
            refresh();
        }
    }

    private void requestStoragePermission() {
        requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void onRequestStoragePermissionResult(boolean isGranted) {
        if (isGranted) {
            viewModel.setStorageAccessRequested(false);
            refresh();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            RequestStoragePermissionRationale.show(this);
        } else {
            RequestStoragePermissionInSettingsRationale.show(this);
        }
    }

    @Override
    public void onShowRequestStoragePermissionInSettingsRationaleResult(boolean shouldRequest) {
        if (shouldRequest) {
            requestStoragePermissionInSettings();
        } else {
            viewModel.setStorageAccessRequested(false);
        }
    }

    private void requestStoragePermissionInSettings() {
        requestStoragePermissionInSettingsLauncher.launch(true);
    }

    private void onRequestStoragePermissionInSettingsResult(boolean isGranted) {
        viewModel.setStorageAccessRequested(false);
        if (isGranted) {
            refresh();
        }
    }

    private void ensureNotificationPermission() {
        if (viewModel.isNotificationPermissionRequested()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    RequestNotificationPermissionRationale.show(this);
                } else {
                    requestNotificationPermission();
                }
                viewModel.setNotificationPermissionRequested(true);
            }
        }
    }

    private void refresh() {
        //  viewModel.reload();
    }

    @Override
    public void onShowRequestNotificationPermissionRationaleResult(boolean shouldRequest) {
        if (shouldRequest) {
            requestNotificationPermission();
        } else {
            viewModel.setNotificationPermissionRequested(false);
        }
    }

    private void requestNotificationPermission() {
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void onRequestNotificationPermissionResult(boolean isGranted) {
        if (isGranted) {
            viewModel.setNotificationPermissionRequested(false);
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            RequestNotificationPermissionRationale.show(this);
        } else {
            RequestNotificationPermissionInSettingsRationale.show(this);
        }
    }

    @Override
    public void onShowRequestNotificationPermissionInSettingsRationaleResult(boolean shouldRequest) {
        if (shouldRequest) {
            requestNotificationPermissionInSettings();
        } else {
            viewModel.setNotificationPermissionRequested(false);
        }
    }

    private void requestNotificationPermissionInSettings() {
        requestNotificationPermissionInSettingsLauncher.launch(true);
    }

    private void onRequestNotificationPermissionInSettingsResult(boolean isGranted) {
        if (isGranted) {
            viewModel.setNotificationPermissionRequested(false);
        }
    }

    @Override
    public void onShowRequestStoragePermissionRationaleResult(boolean shouldRequest) {

    }

    private class RequestAllFilesAccessContract extends ActivityResultContract<Boolean, Boolean> {
        @RequiresApi(Build.VERSION_CODES.R)
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Boolean input) {
            return createManageAppAllFilesAccessPermissionIntent(context.getPackageName());
        }

        @RequiresApi(Build.VERSION_CODES.R)
        @Override
        public Boolean parseResult(int resultCode, @Nullable Intent intent) {
            return Environment.isExternalStorageManager();
        }
    }

    private class RequestPermissionInSettingsContract extends ActivityResultContract<Boolean, Boolean> {
        private final String permissionName;

        RequestPermissionInSettingsContract(String permissionName) {
            this.permissionName = permissionName;
        }

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Boolean input) {
            return new Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.getPackageName(), null));
        }

        @Override
        public Boolean parseResult(int resultCode, @Nullable Intent intent) {
            return ActivityCompat.checkSelfPermission(requireContext(), permissionName) ==
                    PackageManager.PERMISSION_GRANTED;
        }
    }


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

