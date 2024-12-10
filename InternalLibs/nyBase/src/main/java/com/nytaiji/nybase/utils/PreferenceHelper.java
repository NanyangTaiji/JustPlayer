package com.nytaiji.nybase.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nytaiji.nybase.R;


public class PreferenceHelper {
    private SharedPreferences sharedPreferences;
    private static volatile PreferenceHelper instance;

    private final Context context;

    public static PreferenceHelper getInstance() {
        if (instance == null) {
            synchronized (PreferenceHelper.class) {
                if (instance == null)
                    instance = new PreferenceHelper();
            }
        }
        return instance;
    }

    public static boolean isNotAgreed() {
        return !getInstance().getBoolean(R.string.key_terms_con,false);
    }

    private PreferenceHelper() {
        context = AppContextProvider.getAppContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getSharedPreferences(){
        return sharedPreferences;
    }


    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public String getString(String key, String defvalue) {
        return sharedPreferences.getString(key, defvalue);
    }

    public String getString(int id, String def) {
        return getString(context.getString(id), def);
    }

    public void setString(int id, String value) {
        setString(context.getString(id), value);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setBoolean(int key, boolean value) {
        setBoolean(context.getString(key), value);
    }

    public boolean getBoolean(int id, boolean def) {
        return getBoolean(context.getString(id), def);
    }


    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setInt(int id, int value) {
        setInt(context.getString(id), value);
    }

    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    public int getInt(int id, int def) {
        return getInt(context.getString(id), def);
    }

}