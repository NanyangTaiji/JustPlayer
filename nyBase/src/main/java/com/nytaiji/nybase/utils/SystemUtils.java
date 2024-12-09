package com.nytaiji.nybase.utils;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.WINDOW_SERVICE;
import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;
import static android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nathen
 * On 2016/02/21 12:25
 */
public class SystemUtils {
    public static final String TAG = "SystemUtils";
    public static int SYSTEM_UI = 0;


    public static boolean isLandscape(Activity activity) {
        int orientation = activity.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static void adjustSystemUIVisibility(Activity activity) {
        View decorView = activity.getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (isLandscape(activity)) {
            // Hide status bar, action bar, and navigation bar in landscape mode
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static float getCenteredAxis(MotionEvent event, InputDevice device, int axis) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public static void cleanupMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcessesList = getRunningAppProcessInfo(context);
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcessesList) {
            try {
                activityManager.killBackgroundProcesses(processInfo.processName);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns a list of application processes that are running on the device.
     *
     * @return a list of RunningAppProcessInfo records, or null if there are no
     * running processes (it will not return an empty list).  This list ordering is not
     * specified.
     */
    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessInfo(Context ctx) {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = new ArrayList<>();
        String prevProcess = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PackageManager packageManager = ctx.getPackageManager();
            List<PackageInfo> allAppList = packageManager.getInstalledPackages(getAppListFlag());
            for (PackageInfo packageInfo : allAppList) {
                ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo(
                        packageInfo.packageName, packageInfo.applicationInfo.uid, null
                );
                info.uid = packageInfo.applicationInfo.uid;
                info.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED;
                appProcessInfos.add(info);
            }
            return appProcessInfos;
        } else {
            List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(1000);
            for (ActivityManager.RunningServiceInfo process : runningServices) {
                ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo(
                        process.process, process.pid, null
                );
                info.uid = process.uid;
                info.importance = process.foreground ? ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND : ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED;

                if (!prevProcess.equals(process.process)) {
                    prevProcess = process.process;
                    appProcessInfos.add(info);
                }
            }
            return appProcessInfos;
        }
    }

    private static int getAppListFlag() {
        return  (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)? MATCH_UNINSTALLED_PACKAGES : GET_UNINSTALLED_PACKAGES;
    }
    //-------------------------//


    public static int dp2Px(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_MEDIUM);
        return Math.round(px);
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int dp2px(Context c, float dpValue) {
        final float scale = c.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static int sp2px(Context c, float spValue) {
        float fontScale = c.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }



    public static int px2dp(Context c, float pxValue) {
        final float scale = c.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int px2sp(Context c, float pxValue) {
        float fontScale = c.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /** Lightweight choice to {@link Math#round(double)} */
    public static long roundDouble(double value) {
        return (long) (value > 0 ? value + 0.5 : value - 0.5);
    }


    /** Lightweight choice to {@link Math#round(float)} */
    public static int roundFloat(float value) {
        return (int) (value > 0 ? value + 0.5f : value - 0.5f);
    }


  /*  @SuppressLint("DefaultLocale")
    public static String timeConvert(long duration) {
        if (duration < 3600000) {
            return String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        } else {
            return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        }
    }*/

    public static String timeConvert(long timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = timeMs / 1000;
        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }



    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }



    /**
     * This method requires the caller to hold the permission ACCESS_NETWORK_STATE.
     *
     * @param context context
     * @return if wifi is connected,return true
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }



    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        if (networkCapabilities == null) return false;
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true;
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            return true;
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);

    }
    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static Activity getActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    /** 获取当前屏幕方向 */
    @SuppressLint("SwitchIntDef")
    public static int getCurrentOrientation(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        switch (wm.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            case Surface.ROTATION_180:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            case Surface.ROTATION_270:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            default:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    public static int getScreenOrientation(Context context){
        Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        return display.getOrientation();
    }

    public static void setRequestedOrientation(Context context, int orientation) {
        if ( getActivity(context) != null) {
             getActivity(context).setRequestedOrientation(orientation);
        } else {
             getActivity(context).setRequestedOrientation(orientation);
        }
    }

    private static Window getWindow(Context context) {
        if (context instanceof Activity) {
            return ((Activity) context).getWindow();
        } else {
            // Handle the case when the context is not an instance of Activity
            return getActivity(context).getWindow();
        }
    }

    public static boolean isActionBarVisible(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            ActionBar ab = ((AppCompatActivity) activity).getSupportActionBar();
            return ab != null && ab.isShowing();
        }
        return false;
    }

    @SuppressLint("RestrictedApi")
    public static void showSupportActionBar(Activity activity) {

            if (activity instanceof AppCompatActivity) {
                ActionBar ab = ((AppCompatActivity) activity).getSupportActionBar();
                if (ab != null) {
                    ab.setShowHideAnimationEnabled(false);
                    ab.show();
                }
            }

    }

    @SuppressLint("RestrictedApi")
    public static void hideSupportActionBar(Activity activity) {

            if (activity instanceof AppCompatActivity) {
                ActionBar ab = ((AppCompatActivity) activity).getSupportActionBar();
                if (ab != null) {
                    ab.setShowHideAnimationEnabled(false);
                    ab.hide();
                }
            }

    }

    public static void showStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= 14) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    public static void hideStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= 14) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public static void addFullScreenFlag(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN; //添加FLAG_FULLSCREEN
        activity.getWindow().setAttributes(attrs);
    }

    public static void clearFullScreenFlag(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN; //移除FLAG_FULLSCREEN
        activity.getWindow().setAttributes(attrs);
    }

    public static void keepScreenOn(Context context) {
        if (getActivity(context)!=null) getActivity(context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static void keepScreenSecure(Context context) {
        getActivity(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    public static void removeScreenOn(Context context) {
        //TODO
        if (getActivity(context)!=null) getActivity(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //是否是横屏
    public static boolean isScreenLand(Context context) {
        Activity activity = getActivity(context);
        return activity.getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_90 ||
                activity.getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_270;

    }

    /**
     * Force disables screen rotation. Useful when we're temporarily in activity because of external
     * intent, and don't have to really deal much with filesystem.
     */
    public static void disableScreenRotation(@NonNull Activity activity) {
        int screenOrientation = activity.getResources().getConfiguration().orientation;

        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void enableScreenRotation(@NonNull Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static boolean isDeviceInLandScape(Activity activity) {
        return activity.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 设置当此窗口对用户可见时是否保持设备的屏幕打开且明亮
     */
    public static void setKeepWindowBright(Window window, boolean keepBright) {
        int flags = window.getAttributes().flags;
        int keepScreenOnFlag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        if ((flags & keepScreenOnFlag) == (keepBright ? 0 : keepScreenOnFlag)) {
            flags ^= keepScreenOnFlag;
            window.setFlags(flags, keepScreenOnFlag);
        }
    }
    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else {
            display.getMetrics(dm);
        }
        int realHeight = dm.heightPixels;
        return realHeight;
    }



    public static float getScreenBrightness(Context context) {
        float brightness = getActivity(context).getWindow().getAttributes().screenBrightness;

        //如果是默认的系统亮度，则取系统屏幕亮度
        if (brightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            return getSystemBrightness(context);
        }
        return brightness;
    }

    public static void setScreenBrightness(Context context, float screenBrightness) {
        Activity activity = getActivity(context);
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = screenBrightness;
        activity.getWindow().setAttributes(layoutParams);
    }

    //获取手动亮度模式下，系统的屏幕亮度，并转化成0.0f - 1.0f区间的数值
    public static float getSystemBrightness(Context context) {
        int brightness = 0;
        try {
            brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return (float) brightness / 255;
    }

    public static void hideNavKey(Activity activity) {
        View decoderView = activity.getWindow().getDecorView();
        int systemUiVisiblity = decoderView.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //       设置屏幕始终在前面，不然点击鼠标，重新出现虚拟按键
            decoderView.setSystemUiVisibility(
                    systemUiVisiblity
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            decoderView.setSystemUiVisibility(
                    systemUiVisiblity
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
            );
        }
    }

    public static void hideSysStausUI(Context context) {
        int uiOptions =  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow(context).getDecorView().setSystemUiVisibility(uiOptions);
    }


    public static void hideSystemUI(Context context) {
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow(context).getDecorView().setSystemUiVisibility(uiOptions);
    }

    public static void hideSystemUIAndActionBar(Activity activity) {
        // Hide the system UI
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        // Hide the action bar
        android.app.ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public static void showNavigationBar(Context context) {
        showSystemUI(context);
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        getWindow(context).getDecorView().setSystemUiVisibility(uiOptions);
    }

    @SuppressLint("NewApi")
    public static void showSystemUI(Context context) {
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
         getWindow(context).getDecorView().setSystemUiVisibility(SYSTEM_UI);
    }

    //获取状态栏的高度
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    //获取NavigationBar的高度
    public static int getNavigationBarHeight(Context context) {
        boolean var1 = ViewConfiguration.get(context).hasPermanentMenuKey();
        int var2;
        return (var2 = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android")) > 0 && !var1 ? context.getResources().getDimensionPixelSize(var2) : 0;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getScreenRealHeight(Context context) {
        int h;
        WindowManager winMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = winMgr.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealMetrics(dm);
            h = dm.heightPixels;
        } else {
            try {
                Method method = Class.forName("android.view.Display").getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
                h = dm.heightPixels;
            } catch (Exception e) {
                display.getMetrics(dm);
                h = dm.heightPixels;
            }
        }
        return h;
    }

    public static int getScreenOriginHeight(Context c) {
        return getScreenHeight(c)-getStatusBarHeight(c);
    }

    //----------------打开输入法的时候点击以外区域自动关闭------------------


    public static boolean confirm(Activity activity, MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = activity.getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                return hideInputMethod(activity, v); //隐藏键盘时，其他控件不响应点击事件==》注释则不拦截点击事件
            }
        }
        return false;
    }


    /**
     * 点击屏幕非输入框区域关闭软键盘
     *
     * @param context
     * @param v
     * @return
     */
    public static Boolean hideInputMethod(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        return false;
    }


    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText ||v instanceof WebView) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0], top = leftTop[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            // 保留点击EditText的事件
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        return false;
    }

    /**
     * Return whether service is running.
     *
     * @param cls The service class.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isServiceRunning(@NonNull Context context,@NonNull final Class<?> cls) {
        return isServiceRunning(context,cls.getName());
    }

    /**
     * Return whether service is running.
     *
     * @param className The name of class.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isServiceRunning(@NonNull Context context, @NonNull final String className) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> info = am.getRunningServices(0x7FFFFFFF);
            if (info == null || info.size() == 0) return false;
            for (ActivityManager.RunningServiceInfo aInfo : info) {
                if (className.equals(aInfo.service.getClassName())) return true;
            }
            return false;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static boolean copyToClipboard(Context context, String text) {
        try {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip =
                    android.content.ClipData.newPlainText("Path copied to clipboard", text);
            clipboard.setPrimaryClip(clip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static CharSequence getClipboardItem(Context context) {
        CharSequence clipboardText = null;
        ClipboardManager clipboardManager = ContextCompat.getSystemService(context, ClipboardManager.class);

        // if the clipboard contain data ...
        if (clipboardManager != null  &&  clipboardManager.hasPrimaryClip()) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

            // gets the clipboard as text.
            clipboardText = item.coerceToText(context);
        }

        return clipboardText;
    }
}
