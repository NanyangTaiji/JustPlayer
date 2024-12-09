package com.nytaiji.nybase.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import java.lang.reflect.InvocationTargetException;

public class AppContextProvider {

    private static Context context;
    // Property to get the new locale only on restart to prevent changing the locale partially on runtime

    @SuppressLint("DiscouragedPrivateApi")
    public static Context getAppContext() {
        if (context != null) {
            return context;
        } else {
            try {
                context = (Application) Class.forName("android.app.ActivityThread")
                        .getDeclaredMethod("currentApplication")
                        .invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | ClassCastException ignored) {
            }
            return context;
        }
    }

    public static void init(Context context) {
        AppContextProvider.context = context;
    }


    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources() {
        return getAppContext().getResources();
    }
}

