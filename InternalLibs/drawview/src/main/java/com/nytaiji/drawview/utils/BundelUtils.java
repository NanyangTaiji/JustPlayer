package com.nytaiji.drawview.utils;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by IngMedina on 29/04/2017.
 */

public class BundelUtils {
    /**
            * Convert bundle to readable string
     *
             * @param bundle The bundle to convert
     * @return String representation of bundle
     */
    public static String BundleToString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            stringBuilder.append(
                    String.format("%s %s (%s)\n", key, value, value == null ? "null" : value.getClass().getName()));
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public static void saveStrToNormalFile(String str, File file) {
        //  File f = new File("gallerydump_img.txt");
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.println(str);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // file not found
        }
    }
}
