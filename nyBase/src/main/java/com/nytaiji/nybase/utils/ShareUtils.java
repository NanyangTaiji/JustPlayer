package com.nytaiji.nybase.utils;


import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

public final class ShareUtils {
    private static final String TAG = ShareUtils.class.getSimpleName();

    private ShareUtils() {
    }

    /**
     * Open an Intent to install an app.
     * <p>
     * This method tries to open the default app market with the package id passed as the
     * second param (a system chooser will be opened if there are multiple markets and no default)
     * and falls back to Google Play Store web URL if no app to handle the market scheme was found.
     * <p>
     * It uses {@link #openIntentInApp(Context, Intent)} to open market scheme and {@link
     * #openUrlInBrowser(Context, String)} to open Google Play Store web URL.
     *
     * @param context   the context to use
     * @param packageId the package id of the app to be installed
     */
    public static void installApp(@NonNull final Context context, final String packageId) {
        // Try market scheme
        final Intent marketSchemeIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageId))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!tryOpenIntentInApp(context, marketSchemeIntent)) {
            // Fall back to Google Play Store Web URL (F-Droid can handle it)
            openUrlInApp(context, "https://play.google.com/store/apps/details?id=" + packageId);
        }
    }

    /**
     * Open the url with the system default browser. If no browser is set as default, falls back to
     * {@link #openAppChooser(Context, Intent, boolean)}.
     * <p>
     * This function selects the package to open based on which apps respond to the {@code http://}
     * schema alone, which should exclude special non-browser apps that are can handle the url (e.g.
     * the official YouTube app).
     * <p>
     * Therefore <b>please prefer {@link #openUrlInApp(Context, String)}</b>, that handles package
     * resolution in a standard way, unless this is the action of an explicit "Open in browser"
     * button.
     *
     * @param context the context to use
     * @param url     the url to browse
     **/
    public static void openUrlInBrowser(@NonNull final Context context, final String url) {
        // Resolve using a generic http://, so we are sure to get a browser and not e.g. the yt app.
        // Note that this requires the `http` schema to be added to `<queries>` in the manifest.
        final ResolveInfo defaultBrowserInfo;
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            defaultBrowserInfo = context.getPackageManager().resolveActivity(browserIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));
        } else {
            defaultBrowserInfo = context.getPackageManager().resolveActivity(browserIntent,
                    PackageManager.MATCH_DEFAULT_ONLY);
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (defaultBrowserInfo == null) {
            // No app installed to open a web URL, but it may be handled by other apps so try
            // opening a system chooser for the link in this case (it could be bypassed by the
            // system if there is only one app which can open the link or a default app associated
            // with the link domain on Android 12 and higher)
            openAppChooser(context, intent, true);
            return;
        }

        final String defaultBrowserPackage = defaultBrowserInfo.activityInfo.packageName;

        if (defaultBrowserPackage.equals("android")) {
            // No browser set as default (doesn't work on some devices)
            openAppChooser(context, intent, true);
        } else {
            try {
                intent.setPackage(defaultBrowserPackage);
                context.startActivity(intent);
            } catch (final ActivityNotFoundException e) {
                // Not a browser but an app chooser because of OEMs changes
                intent.setPackage(null);
                openAppChooser(context, intent, true);
            }
        }
    }

    /**
     * Open a url with the system default app using {@link Intent#ACTION_VIEW}, showing a toast in
     * case of failure.
     *
     * @param context the context to use
     * @param url     the url to open
     */
    public static void openUrlInApp(@NonNull final Context context, final String url) {
        openIntentInApp(context, new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * Open an intent with the system default app.
     * <p>
     * Use {@link #openIntentInApp(Context, Intent)} to show a toast in case of failure.
     *
     * @param context the context to use
     * @param intent  the intent to open
     * @return true if the intent could be opened successfully, false otherwise
     */
    public static boolean tryOpenIntentInApp(@NonNull final Context context,
                                             @NonNull final Intent intent) {
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Open an intent with the system default app, showing a toast in case of failure.
     * <p>
     * Use {@link #tryOpenIntentInApp(Context, Intent)} if you don't want the toast. Use {@link
     * #openUrlInApp(Context, String)} as a shorthand for {@link Intent#ACTION_VIEW} with urls.
     *
     * @param context the context to use
     * @param intent  the intent to
     */
    public static void openIntentInApp(@NonNull final Context context,
                                       @NonNull final Intent intent) {
        if (!tryOpenIntentInApp(context, intent)) {
            Toast.makeText(context, "No app to open intent", Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Open the system chooser to launch an intent.
     * <p>
     * This method opens an {@link Intent#ACTION_CHOOSER} of the intent putted
     * as the intent param. If the setTitleChooser boolean is true, the string "Open with" will be
     * set as the title of the system chooser.
     * For Android P and higher, title for {@link Intent#ACTION_SEND} system
     * choosers must be set on this intent, not on the
     * {@link Intent#ACTION_CHOOSER} intent.
     *
     * @param context         the context to use
     * @param intent          the intent to open
     * @param setTitleChooser set the title "Open with" to the chooser if true, else not
     */
    private static void openAppChooser(@NonNull final Context context,
                                       @NonNull final Intent intent,
                                       final boolean setTitleChooser) {
        final Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, intent);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (setTitleChooser) {
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Open With");
        }

        // Migrate any clip data and flags from the original intent.
        final int permFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (permFlags != 0) {
            ClipData targetClipData = intent.getClipData();
            if (targetClipData == null && intent.getData() != null) {
                final ClipData.Item item = new ClipData.Item(intent.getData());
                final String[] mimeTypes;
                if (intent.getType() != null) {
                    mimeTypes = new String[] {intent.getType()};
                } else {
                    mimeTypes = new String[] {};
                }
                targetClipData = new ClipData(null, mimeTypes, item);
            }
            if (targetClipData != null) {
                chooserIntent.setClipData(targetClipData);
                chooserIntent.addFlags(permFlags);
            }
        }

        try {
            context.startActivity(chooserIntent);
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(context, "No app to open intent", Toast.LENGTH_LONG).show();
        }
    }

}
