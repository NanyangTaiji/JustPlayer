package com.nytaiji.player;


import static com.nytaiji.player.DownloaderImpl.RECAPTCHA_COOKIES_KEY;
import static org.schabi.newpipe.extractor.NewPipe.getDownloader;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.vlcplayer.RendererDelegate;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;

public class App extends Application {
    static boolean isLogin = false;

    @Override
    public void onCreate() {
        super.onCreate();
        RendererDelegate.getInstance(AppContextProvider.getAppContext());
        createNotificationChannel();

        NewPipe.init(setDownloader());

    }

    protected Downloader setDownloader() {
        final DownloaderImpl downloader = DownloaderImpl.init(null);
        setCookiesToDownloader(downloader);
        return downloader;
    }

    protected void setCookiesToDownloader(final DownloaderImpl downloader) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        final String key = getApplicationContext().getString(R.string.recaptcha_cookies_key);
        downloader.setCookie(RECAPTCHA_COOKIES_KEY, prefs.getString(key, null));
        downloader.updateYoutubeRestrictedModeCookies(getApplicationContext());

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "Nanyang Taji",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}


