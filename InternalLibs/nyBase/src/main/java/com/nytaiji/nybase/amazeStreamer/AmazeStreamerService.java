package com.nytaiji.nybase.amazeStreamer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.nytaiji.nybase.R;
import com.nytaiji.nybase.model.NyHybrid;

public class AmazeStreamerService extends Service {

    private AmazeStreamer amazeStreamer;
    private PowerManager.WakeLock wakeLock;
    private NotificationManagerCompat notificationManager;
    private final IBinder mBinder = new ObtainableServiceBinder(this);
    public static final String TAG_BROADCAST_STREAMER_STOP = "streamer_stop_broadcast";

    public static void runService(Context context) {
        Intent intent = new Intent(context, AmazeStreamerService.class);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (amazeStreamer == null) {
            amazeStreamer = AmazeStreamer.getInstance();
        }
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(stopReceiver, new IntentFilter(TAG_BROADCAST_STREAMER_STOP));
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);
        initNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (amazeStreamer != null) {
            amazeStreamer.stop();
            amazeStreamer = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (notificationManager != null) {
            notificationManager.cancel(AmazeStreamerNotification.NOTIFICATION_ID);
        }
        unregisterReceiver(stopReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setStreamSrc(NyHybrid hybrid) {
        if (amazeStreamer != null) {
            try {
                amazeStreamer.setStreamSrc(
                        hybrid.getDecryptedInputStream(this),
                        hybrid.getName(),
                        hybrid.length(this)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
            if (notificationManager != null) {
                notificationManager.cancel(AmazeStreamerNotification.NOTIFICATION_ID);
            }
            stopForeground(true);
        }
    };

    private void initNotification() {
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        android.app.Notification notification = AmazeStreamerNotification.startNotification(this);
        startForeground(AmazeStreamerNotification.NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(
                AmazeStreamerNotification.NOTIFICATION_CHANNEL_ID
        );
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(
                    AmazeStreamerNotification.NOTIFICATION_CHANNEL_ID,
                    getString(R.string.chromecast),
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.setDescription(getString(R.string.cast_notification_summary));
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
