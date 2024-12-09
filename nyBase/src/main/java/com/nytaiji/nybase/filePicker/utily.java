package com.nytaiji.nybase.filePicker;

import android.content.Context;
import android.os.StatFs;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class utily {

    public static String[] storages(Context context) {
        List<String> storages = new ArrayList<>();

        try {
            File[] externalStorageFiles = ContextCompat.getExternalFilesDirs(context.getApplicationContext(), null);

            String base = String.format("/Android/data/%s/files", context.getPackageName());

            for (File file : externalStorageFiles) {
                try {
                    if (file != null) {
                        String path = file.getAbsolutePath();

                        if (path.contains(base)) {
                            String finalPath = path.replace(base, "");

                            if (validPath(finalPath)) {
                                storages.add(finalPath);
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
        }

        String[] result = new String[storages.size()];
        storages.toArray(result);

        return result;
    }

    private static boolean validPath(String path) {
        try {
            StatFs stat = new StatFs(path);
            stat.getBlockCount();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //------------------------------------------------//
}
