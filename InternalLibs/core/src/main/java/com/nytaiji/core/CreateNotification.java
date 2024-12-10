package com.nytaiji.core;

import static com.nytaiji.nybase.model.Constants.ACTION_NEXT;
import static com.nytaiji.nybase.model.Constants.ACTION_PLAY;
import static com.nytaiji.nybase.model.Constants.ACTION_PREV;
import static com.nytaiji.nybase.model.Constants.ACTION_STOP;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.nybase.utils.NyFileUtil;


public class CreateNotification {

    public static final String CHANNEL_ID = "channel1";

    public static Notification notification;

    public static void createNotification(Context context, NyVideo nyVideo, int playbutton, int vIndex, int listSize) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), com.nytaiji.nybase.R.drawable.ny);

            PendingIntent pendingIntentPrevious;
            int drw_previous;
            if (vIndex <= 0 || listSize == 1) {
                pendingIntentPrevious = null;
                drw_previous = 0;
            } else {
                Intent intentPrevious = new Intent(context, NotificationActionReceiver.class).setAction(ACTION_PREV);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_previous = com.nytaiji.nybase.R.drawable.exo_controls_previous;
            }

            Intent intentPlay = new Intent(context, NotificationActionReceiver.class).setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pendingIntentNext;
            int drw_next;
            //  Log.e("CreateNotification   ", "vIndex="+vIndex+"listSize="+listSize);
            if (vIndex >= listSize - 1 || listSize == 1) {
                pendingIntentNext = null;
                drw_next = 0;
            } else {
                Intent intentNext = new Intent(context, NotificationActionReceiver.class).setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_next = com.nytaiji.nybase.R.drawable.exo_controls_next;
            }

            PendingIntent pendingIntentStop;
            int drw_stop;
            Intent intentStop = new Intent(context, NotificationActionReceiver.class).setAction(ACTION_STOP);
            pendingIntentStop = PendingIntent.getBroadcast(context, 0, intentStop, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_stop = com.nytaiji.nybase.R.drawable.exo_notification_stop;

            String title = NyFileUtil.getFileNameWithoutExtFromPath(nyVideo.getPath());
            //create notification
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(com.nytaiji.nybase.R.drawable.ny)
                    .setContentTitle(title)
                    .setContentText("NyTaiji")
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)//show notification for only first time
                    .setShowWhen(false)
                    .addAction(drw_previous, "Previous", pendingIntentPrevious)
                    .addAction(playbutton, "Play", pendingIntentPlay)
                    .addAction(drw_next, "Next", pendingIntentNext)
                    .addAction(drw_stop, "Stop", pendingIntentStop)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
              //  ActivityCompat.requestPermissions(context,Manifest.permission.POST_NOTIFICATIONS,123);
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManagerCompat.notify(1, notification);
        }
    }
}
