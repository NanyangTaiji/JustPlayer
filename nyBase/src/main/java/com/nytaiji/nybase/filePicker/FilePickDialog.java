package com.nytaiji.nybase.filePicker;


import static com.nytaiji.nybase.filePicker.utily.storages;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nytaiji.nybase.R;
import com.nytaiji.nybase.utils.GeneralCallback;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.PermissionHelper;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilePickDialog extends DialogFragment {

    public static final String LAST_READ_PATH = "LAST_READ_PATH";
    @StringRes
    private int title;
    @Nullable
    private String[] extensions;
    private static String current_path;
    private ImportListener listener;
    private ArrayList<Folder> items;
    private FolderAdapter adapter = null;
    private FixedHeightRecyclerView recyclerView;

    private View spinner;
    private SharedPreferences prefs;
    private String[] storages;

    private String parentPath = null;
    private boolean toDismiss = true;

    private boolean isInZip = false; // Flag to track if inside a ZIP

    private String zipPassword = "";
    private String zipParentPath = null; // To track parent of the ZIP if navigating inside

    public FilePickDialog() {
    }


    public static void pickupFile(Context context, boolean toDismiss, GeneralCallback generalCallback) {
        if (PermissionHelper.checkStoragePermissions((Activity) context,
                PermissionHelper.DOWNLOAD_DIALOG_REQUEST_CODE)) {
            FilePickDialog.newInstance(
                            R.string.selectOnlyOne,
                            null,
                            new ImportListener() {
                                @Override
                                public void onSelect(final String path) {
                                    if (generalCallback != null) generalCallback.SingleString(path);
                                }

                                @Override
                                public void onError(String msg) {
                                }
                            }
                            , toDismiss)
                    .show(((FragmentActivity) context).getSupportFragmentManager(), "");
        }


    }


    //TODO ny   when doDismiss set false, this dialog can serve as a simple file explorer for repeated action
    public static FilePickDialog newInstance(@StringRes int title, @Nullable String[] extensions, ImportListener listener, boolean toDismiss) {
        FilePickDialog dialog = new FilePickDialog();
        dialog.title = title;
        dialog.extensions = extensions;
        dialog.listener = listener;
        dialog.toDismiss = toDismiss;
        return dialog;
    }


    public static FilePickDialog newInstance(@StringRes int title, @Nullable String[] extensions, ImportListener listener) {
        return newInstance(title, extensions, listener, true);
    }

    public static FilePickDialog newInstance(@StringRes int title, ImportListener listener) {
        return newInstance(title, null, listener, true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        getDialog().setCanceledOnTouchOutside(toDismiss);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        storages = storages(getActivity());
        return inflater.inflate(R.layout.dialog_file_pick, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinner = view.findViewById(R.id.loading_spinner);
        if (storages.length == 0) {
            listener.onError(getString(R.string.no_mounted_sd));
            dismiss();
        } else {
            current_path = prefs.getString(LAST_READ_PATH, "/storage");

            ((TextView) view.findViewById(R.id.item_title)).setText(title);
            recyclerView = (FixedHeightRecyclerView) view.findViewById(R.id.recyclerView);
            items = new ArrayList<>();
            reload();

            view.findViewById(R.id.positive_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            view.findViewById(R.id.neutral_btn).setOnClickListener(btnview -> {
                if (isInZip) {
                    // Exit ZIP navigation
                    current_path = zipParentPath; // Restore the parent path
                    isInZip = false; // Reset ZIP navigation flag
                    zipParentPath = null; // Clear ZIP parent path
                } else {
                    // Go up in the normal file hierarchy
                    current_path = parentPath;
                }
                reload(); // Reload with the updated path
            });
        }
    }

    private void reload() {
        spinner.setVisibility(View.VISIBLE);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            items.clear();

            if (isInZip) {
                //TODO ny
                //  items.add(new Folder(NyFileUtil.getLastSegmentFromString(current_path), zipParentPath, true));

                // Handling ZIP file navigation
                try {
                    ZipFile zipFile = new ZipFile(current_path);

                    // Check if the ZIP file is encrypted
                    if (zipFile.isEncrypted()) {
                        // Show password dialog and pass the entered password to the callback
                        new Handler(Looper.getMainLooper()).post(() -> {
                            showPasswordDialog(password -> {
                                if (password != null && !password.isEmpty()) {
                                    // Set the password for the encrypted ZIP file
                                  //  zipFile.setPassword(password.toCharArray());
                                    zipPassword =password;
                                    // Now proceed with reading the file headers
                                    loadZipContents(zipFile);
                                } else {
                                    // Handle the case where password is not provided or cancelled
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        spinner.setVisibility(View.GONE);
                                        listener.onError("Password is required to open the ZIP file.");
                                    });
                                }
                            });
                        });
                    } else {
                        // If no password is required, just load the contents
                        loadZipContents(zipFile);
                    }

                } catch (IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        spinner.setVisibility(View.GONE);
                        listener.onError("Failed to read ZIP file: " + e.getMessage());
                    });
                }
            } else {
                // Regular file system logic
                File folder = new File(current_path);
                if (!folder.exists() || Objects.equals(current_path, "/storage")) {
                    // Fallback logic for storage
                    if (storages.length == 1) {
                        current_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    } else {
                        for (String drive : storages) {
                            items.add(new Folder(drive, new File(drive).getAbsolutePath(), false));
                        }
                    }
                }

                // Add parent folder logic
                if (folder.getParentFile() != null) {
                    parentPath = folder.getParentFile().getAbsolutePath();
                    items.add(new Folder("..", parentPath, true));
                }

                // Add folder/file logic
                File[] folders = folder.listFiles(file -> {
                    if (file.isDirectory() || (extensions == null || file.getName().endsWith(".zip"))) {
                        return true;
                    }
                    for (String extension : extensions) {
                        if (file.getName().endsWith(extension)) return true;
                    }
                    return false;
                });

                if (folders != null) {
                    Arrays.sort(folders, (f1, f2) -> {
                        if (f1.isDirectory() && !f2.isDirectory()) return -1;
                        if (!f1.isDirectory() && f2.isDirectory()) return 1;
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    });

                    for (File file : folders) {
                        items.add(new Folder(file.getName(), file.getAbsolutePath(), false, file.isDirectory()));
                    }
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                spinner.setVisibility(View.GONE);
                if (adapter == null) {
                    adapter = new FolderAdapter(getContext(), items, item -> {
                        if (item.isDirectory) {
                            current_path = item.path;
                            reload();
                        } else if (item.path.endsWith(".zip")) {
                            zipParentPath = current_path; // Store current path as ZIP parent
                            current_path = item.path; // Set ZIP as the current path
                            isInZip = true; // Mark as inside a ZIP
                            reload();
                        } else {
                            prefs.edit().putString(LAST_READ_PATH, current_path).apply();
                            // Log.e("FilePickDialog", "--------------current_path = "+current_path);
                            //  Log.e("FilePickDialog", "-------------item_path = "+item.path);
                            if (current_path.endsWith("zip"))
                                listener.onSelect(current_path + "/p=" + zipPassword + "/e=" + item.path.replace("/",""));
                            else listener.onSelect(item.path);
                            if (toDismiss) dismiss();
                        }
                    });
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
                executorService.shutdown();
            });
        });
    }


    private void loadZipContents(ZipFile zipFile) {
        try {
            List<FileHeader> headers = zipFile.getFileHeaders();

            // Process the file headers (files inside the ZIP)
            for (FileHeader header : headers) {
                if (!header.isDirectory()) {
                    items.add(new Folder(
                            header.getFileName(),
                            header.getFileName(), // Relative path inside ZIP
                            false,
                            false // Not a directory inside the ZIP
                    ));
                }
            }

            // Update UI on the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                spinner.setVisibility(View.GONE);

                // Notify the adapter of the new items
                if (adapter == null) {
                    adapter = new FolderAdapter(getContext(), items, new FolderAdapter.ClickListener() {
                        @Override
                        public void onClick(Folder item) {
                            // Handle file/folder click actions
                        }
                    });

                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() -> {
                spinner.setVisibility(View.GONE);
                listener.onError("Failed to read ZIP file: " + e.getMessage());
            });
        }
    }


    private void showPasswordDialog(PasswordCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter ZIP Password");

        final EditText passwordInput = new EditText(getContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = passwordInput.getText().toString();
            callback.onPasswordEntered(password);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            callback.onPasswordEntered(null); // Handle cancel
        });

        builder.show();
    }

    public interface PasswordCallback {
        void onPasswordEntered(String password);
    }


    public interface ImportListener {
        void onSelect(String path);

        void onError(String msg);
    }
}
