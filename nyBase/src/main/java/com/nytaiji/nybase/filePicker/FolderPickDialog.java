package com.nytaiji.nybase.filePicker;


import static com.nytaiji.nybase.filePicker.utily.storages;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.nytaiji.nybase.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

    /*
    used after permission check

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                    ((MainActivity) context).checkForExternalPermission();
                       return;
                }
            }

    */

public class FolderPickDialog extends DialogFragment {

    public static final String LAST_SAVE_PATH = "LAST_SAVE_PATH";
    @StringRes
    private int title;
    private String current_path;
    private SaveListener listener;
    private ArrayList<Folder> items;
    private FolderAdapter adapter = null;
    private FixedHeightRecyclerView recyclerView;
    private boolean isWorking = false;
    private boolean canceled = true;

    private SharedPreferences prefs;

    private String[] storages;

    private String parentPath = null;

    public static FolderPickDialog newInstance(@StringRes int title, SaveListener listener) {
        FolderPickDialog dialog = new FolderPickDialog();
        dialog.title = title;
        dialog.listener = listener;
        return dialog;
    }

    public FolderPickDialog() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        storages = storages(getActivity());
        return inflater.inflate(R.layout.dialog_folder_pick, container);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean isDirWritable() throws Exception {
        File temp_file = new File(current_path, "temp.tmp");
        if (temp_file.exists()) temp_file.delete();
        if (temp_file.createNewFile()) {
            temp_file.delete();
            return true;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            new Thread() {
                @Override
                public void run() {
                    isWorking = true;
                    current_path = getContext().getFilesDir().getAbsolutePath();
                    try {
                        if (isDirWritable()) {
                            prefs.edit().putString(LAST_SAVE_PATH, current_path).apply();
                            listener.onSelect(current_path);
                        }
                    } catch (Exception ignored) {
                        listener.onError();
                    } finally {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dismiss();
                            }
                        });
                        interrupt();
                    }
                }
            }.start();
        } else {
            current_path = prefs.getString(LAST_SAVE_PATH, "/storage");

            ((TextView) view.findViewById(R.id.item_title)).setText(getString(title));
            recyclerView = (FixedHeightRecyclerView) view.findViewById(R.id.recyclerView);
            items = new ArrayList<>();
            reload();

            view.findViewById(R.id.positive_btn).setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override
                public void onClick(View view) {
                    if (isWorking) return;
                    isWorking = true;
                    new Thread() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void run() {
                            try {
                                try {
                                    if (isDirWritable()) {
                                        prefs.edit().putString(LAST_SAVE_PATH, current_path).apply();
                                        listener.onSelect(current_path);
                                    }
                                } catch (Exception ignored) {
                                    try {
                                        current_path = getContext().getExternalFilesDir(null).getAbsolutePath();
                                        if (isDirWritable()) {
                                            prefs.edit().putString(LAST_SAVE_PATH, current_path).apply();
                                            listener.onSelect(current_path);
                                        }
                                    } catch (Exception ignored2) {
                                        current_path = getContext().getFilesDir().getAbsolutePath();
                                        try {
                                            if (isDirWritable()) {
                                                prefs.edit().putString(LAST_SAVE_PATH, current_path).apply();
                                                listener.onSelect(current_path);
                                            }
                                        } catch (Exception e) {
                                            listener.onError();
                                        }
                                    }
                                }
                            } finally {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        canceled = false;
                                        dismiss();
                                    }
                                });
                                interrupt();
                            }
                        }
                    }.start();
                }
            });

            view.findViewById(R.id.negative_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isWorking) return;
                    canceled = true;
                    dismiss();
                }
            });

            view.findViewById(R.id.neutral_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isWorking) return;
                    current_path = parentPath;
                    reload();
                }
            });

            view.findViewById(R.id.new_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContentDialog.newInstance(
                            R.string.new_folder,
                            R.string.create,
                            R.string.cancel,
                            -1,
                            R.layout.dialog_new_folder,
                            new ContentDialog.DialogListener() {
                                private EditText name_txt;
                                private boolean isCreating = false;

                                @Override
                                public void onPositive(ContentDialog dialog, View content) {
                                    if (isCreating || !dialog.checkEditText(name_txt)) return;
                                    isCreating = true;

                                    try {
                                        String folder_name = name_txt.getText().toString();
                                        File folder = new File(current_path, folder_name);

                                        int counter = 2;
                                        while (folder.exists()) {
                                            folder = new File(current_path, String.format(Locale.US, "%s(%d)", folder_name, counter));
                                            counter++;
                                        }

                                        //noinspection ResultOfMethodCallIgnored
                                        folder.mkdirs();
                                        reload();
                                    } catch (Exception ignored) {
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void onNegative(ContentDialog dialog, View content) {
                                    if (isCreating) return;
                                    dialog.dismiss();
                                }

                                @Override
                                public void onNeutral(ContentDialog dialog, View content) {
                                }

                                @Override
                                public void onInit(View content) {
                                    name_txt = (EditText) content.findViewById(R.id.name_txt);
                                }
                            }
                    ).show(getFragmentManager(), "");
                }
            });
        }
    }

    private void reload() {
        new Thread() {
            @Override
            public void run() {
                items.clear();
                //  Log.e("FilePicker", "current_path" + current_path);
                File folder;
                try {
                    folder = new File(current_path);
                } catch (Exception e) {
                    current_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    folder = new File(current_path);
                }

                if (Objects.equals(current_path, "/storage") || !folder.exists()) {
                    if (storages.length == 1) {
                        current_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                        folder = new File(current_path);
                    } else { //storages.length > 1
                        for (String drive : storages) {
                            items.add(new Folder(drive.replace("/storage/", ""), new File(drive).getAbsolutePath(), false));
                        }
                    }
                }

                if (folder.getParentFile() != null && !Objects.equals(current_path, "/storage")) {
                    parentPath = folder.getParentFile().getAbsolutePath();
                    items.add(new Folder(current_path.replace("/storage/", ""), parentPath, true));
                }

                File[] folders = folder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });

                if (folders != null && !Objects.equals(current_path, "/storage")) {
                    Arrays.sort(folders, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return f1.getName().compareToIgnoreCase(f2.getName());
                        }
                    });

                    for (File file : folders) {
                        items.add(new Folder(file.getName(), file.getAbsolutePath(), false));
                    }
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter == null) {
                            adapter = new FolderAdapter(getContext(), items, new FolderAdapter.ClickListener() {
                                @Override
                                public void onClick(Folder item) {
                                    if (isWorking) return;
                                    current_path = item.path;
                                    reload();
                                }
                            });

                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

                interrupt();
            }
        }.start();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (canceled) listener.onCancel();
        super.onDismiss(dialog);
    }

    public interface SaveListener {
        void onSelect(String path);

        void onError();

        void onCancel();
    }
}
