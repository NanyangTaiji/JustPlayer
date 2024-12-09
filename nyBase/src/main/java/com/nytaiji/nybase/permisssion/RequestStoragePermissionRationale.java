package com.nytaiji.nybase.permisssion;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nytaiji.nybase.R;

public class RequestStoragePermissionRationale extends AppCompatDialogFragment {

    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext(), getTheme())
                .setMessage(R.string.storage_permission_rationale_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getListener().onShowRequestStoragePermissionRationaleResult(true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getListener().onShowRequestStoragePermissionRationaleResult(false);
                    }
                })
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getListener().onShowRequestStoragePermissionRationaleResult(false);
    }

    public static void show(Fragment fragment) {
        new RequestStoragePermissionRationale().show(fragment.getChildFragmentManager(), null);
    }

    public static void show(AppCompatActivity activity) {
        new RequestStoragePermissionRationale().show(activity.getSupportFragmentManager(), null);
    }

    public interface Listener {
        void onShowRequestStoragePermissionRationaleResult(boolean shouldRequest);
    }
}

