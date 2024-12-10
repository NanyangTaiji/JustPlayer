package com.nanyang.richeditor;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.view.WindowManager;

import com.nanyang.richeditor.database.DataLib;
import com.nanyang.richeditor.database.Controller;
import com.nanyang.richeditor.database.OpenHelper;
import com.nanyang.richeditor.database.LibHelper;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    public static App instance;

    public static int DEVICE_HEIGHT;
    public static int DEVICE_WIDTH;

    /* Preferences */
    public static boolean smartFab;
    public static int sortCategoriesBy;
    public static int sortNotesBy;

    public static int currentLib = 0;
    public static int defaultLib = 0;

    public static final String BACKUP_EXTENSION = "json";

    /* Preferences' Keys */
    public static final String SMART_FAB_KEY = "a1";
    public static final String SORT_CATEGORIES_KEY = "a2";
    public static final String SORT_NOTES_KEY = "a3";
    public static final String LAST_PATH_KEY = "a4";

    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initHelper();
        initOthers();


    }

    private int sanitizeSort(int sortId) {
        if (sortId < 0 || sortId > 3) return Controller.SORT_DATE_DESC;
        return sortId;
    }

    public void setSortCategoriesBy(int sortCategoriesBy) {
        App.sortCategoriesBy = sortCategoriesBy;
        prefs.edit().putInt(SORT_CATEGORIES_KEY, sortCategoriesBy).apply();
    }


    public static void setCurrentLib(int lib) {
        currentLib = lib;
    }

    public static int getCurrentLib() {
        return currentLib;
    }

    public static int getDefaultLib() {
        return defaultLib;
    }

    public static void setDefaultLib(int lib) {
        defaultLib = lib;
    }


    private ArrayList<OpenHelper> openHelpers =new ArrayList<>();
    private  List<DataLib> allLibs=new ArrayList<>();
    private void initHelper() {
        LibHelper libHelper = new LibHelper(getApplicationContext());
        allLibs = libHelper.getAllDataLibs();
        openHelpers.clear();
        for (int i = 0; i < allLibs.size(); i++) {
            openHelpers.add(new OpenHelper(getApplicationContext(), allLibs.get(i).getName()));
        }
    }

    public static OpenHelper getDbHelper() {
        instance.initHelper();
        return instance.openHelpers.get(currentLib);
    }

    public static List<DataLib> getAllLibs() {
        return instance.allLibs;
    }

    private void initOthers(){
        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        smartFab = prefs.getBoolean(SMART_FAB_KEY, true);
        sortCategoriesBy = sanitizeSort(prefs.getInt(SORT_CATEGORIES_KEY, Controller.SORT_TITLE_ASC));
        sortNotesBy = sanitizeSort(prefs.getInt(SORT_NOTES_KEY, Controller.SORT_DATE_DESC));
        Point size = new Point();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        DEVICE_HEIGHT = size.y;
        DEVICE_WIDTH = size.x;
    }

}
