package com.nytaiji.nybase;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.File;
import java.util.Arrays;

/**
 * Helper class to access shared preferences
 */
public class AppPref {
    private static String TAG = "AppPref";

    /**
     * This is database path
     */
    public static final String STATIC_CACHE = ".historyDB";
    /**
     * Contains temp thumbnails and short time video files
     */
    public static final String DYNAMIC_CACHE = ".essential";
    /**
     * Contains SO files for FFmpeg
     */
    public static final String LIBRARY_PATH = "libs";
    /**
     * Folder to store temporary spliced videos
     */

    public static final String SPLICE_PATH = "spliced";

    public static final String db_name = "historyCache";
    private static volatile AppPref instance;

    final SharedPreferences sharedPreferences;
    final Context context;


    private AppPref(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static AppPref getInstance(Context context) {
        if (instance == null) {
            synchronized (AppPref.class) {
                if (instance == null)
                    instance = new AppPref(context.getApplicationContext());
            }
        }
        return instance;
    }

    public static AppPref getInstance() {
        if (instance == null) {
            synchronized (AppPref.class) {
                if (instance == null)
                    instance = new AppPref(AppContextProvider.getAppContext());
            }
        }
        return instance;
    }

    public SharedPreferences getSharedPreferences(){
        return sharedPreferences;
    }


    public static boolean isNotAgreed(@NonNull Application application) {
        return !AppPref.getInstance(application).getBoolean(R.string.key_terms_con, false);
    }


    public void setString(int key, String value) {
        setString(context.getString(key), value);
    }

    public String getString(int id, String def) {
        return getString(context.getString(id), def);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String id, String def) {
        return sharedPreferences.getString(id, def);
    }

    public void setBoolean(int key, boolean value) {
        setBoolean(context.getString(key), value);
    }

    public boolean getBoolean(int id, boolean def) {
        return getBoolean(context.getString(id), def);
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String id, boolean def) {
        return sharedPreferences.getBoolean(id, def);
    }

    public void setInt(int key, int value) {
        setInt(context.getString(key), value);
    }

    public int getInt(int id, int def) {
        return getInt(context.getString(id), def);
    }

    public void setInt(String id, int def) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(id, def);
        editor.apply();
    }

    public int getInt(String id, int def) {
        return sharedPreferences.getInt(id, def);
    }

    /**
     * save path got from DOCUMENT_TREE
     *
     * @param //intent intent that we got from document tree
     *               displayable string eg: External Storage>folder> etc..
     */

    public void setSavePath(String path) {
      //  Log.e(TAG, "setSavePath = "+path);
        if (path != null) {
            setString(R.string.key_download_path, path);
        } else {
            path = Arrays.toString(context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS)) + "/";
            setString(R.string.key_download_path, new File(path).getAbsolutePath());
        }


    }

    public String getSavePath() {
        return getString(R.string.key_download_path, null);
    }

    public String getCachePath(String folderName) {
        int index = 0;
        String currentState = getString(R.string.key_cache_path, null);
        if (currentState != null && currentState.equals("external") && NyFileUtil.isSDPresent(context)) {
            index = 1;
        }
        return context.getExternalFilesDirs(folderName)[index] + "/";
    }

    public void setWhatsAppUri(Intent intent) {
        Uri uri = intent.getData();
        int flag = intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
        context.getContentResolver().takePersistableUriPermission(uri, flag);
        setString(R.string.whatsappUri, uri.toString());
    }

    public Uri getWhatsAppUri() {
        String uri = getString(R.string.whatsappUri, null);
        if (uri == null) return null;
        return Uri.parse(uri);
    }

    public void saveFcmToken(@NonNull String token) {
        setString(R.string.key_fcm_token, token);
    }

    @Nullable
    public String getFcmToken() {
        return getString(R.string.key_fcm_token, null);
    }

}
