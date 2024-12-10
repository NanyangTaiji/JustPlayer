package com.nytaiji.nybase.permisssion;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nytaiji.nybase.R;

public class RequestAllFilesAccessRationale extends DialogFragment {

    private Listener listener;

    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), getTheme());
        builder.setMessage(R.string.all_files_access_rationale_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getListener().onShowRequestAllFilesAccessRationaleResult(true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getListener().onShowRequestAllFilesAccessRationaleResult(false);
                    }
                });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getListener().onShowRequestAllFilesAccessRationaleResult(false);
    }

    public static void show(Fragment fragment) {
        new RequestAllFilesAccessRationale().show(fragment.getChildFragmentManager(), null);
    }

    public static void show(AppCompatActivity activity) {
        new RequestAllFilesAccessRationale().show(activity.getSupportFragmentManager(), null);
    }

    public interface Listener {
        void onShowRequestAllFilesAccessRationaleResult(boolean shouldRequest);
    }
}

