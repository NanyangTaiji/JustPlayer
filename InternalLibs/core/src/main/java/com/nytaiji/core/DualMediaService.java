package com.nytaiji.core;

import static android.os.SystemClock.elapsedRealtime;
import static com.nytaiji.nybase.model.Constants.ACTION_NEXT;
import static com.nytaiji.nybase.model.Constants.ACTION_PLAY;
import static com.nytaiji.nybase.model.Constants.ACTION_PREV;
import static com.nytaiji.nybase.model.Constants.ACTION_STOP;
import static com.nytaiji.nybase.model.Constants.STATE_BIND;
import static com.nytaiji.nybase.model.Constants.VIDEO_INDEX;
import static com.nytaiji.nybase.model.Constants.VIDEO_LIST;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnPlayActionListener;
import com.nytaiji.nybase.model.Constants;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.core.player.SingletonPlayer;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.util.ArrayList;


public class DualMediaService extends Service /* implements OnlineLinkUtil.onlineCallback*/ {
    public static String TAG = "DualMediaService";
    private BasePlayer mediaPlayer;
    private final IBinder serviceBinder = new serviceBinder();
    public static long state = PlaybackStateCompat.STATE_PAUSED;
    public static int vIndex = 0;
    public static String title = "";
    private ArrayList<NyVideo> videoList;
    private int listSize = 1;
    private String url;
    private NyVideo nyVideo;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        createNotationChannel();
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
    }

    private void createNotationChannel() {
        NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                "NyTaiji", NotificationManager.IMPORTANCE_LOW);

        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action) {
                case ACTION_PREV:
                    playPrev();
                    break;
                case ACTION_PLAY:
                    if (state == PlaybackStateCompat.STATE_PLAYING) {
                        pausePlayer();
                    } else {
                        resume();
                    }
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_STOP:
                    onExit();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Intent onBindIntent = new Intent(STATE_BIND);
        LocalBroadcastManager.getInstance(this).sendBroadcast(onBindIntent);
        return serviceBinder;
    }

    public class serviceBinder extends Binder {
        DualMediaService getService() {
            return DualMediaService.this;
        }
    }

    private OnPlayActionListener onPlayActionListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        videoList = bundle.getParcelableArrayList(VIDEO_LIST);
        vIndex = bundle.getInt(VIDEO_INDEX);
        listSize = videoList.size();
        mediaPlayer = SingletonPlayer.getInstance(DualMediaService.this.getApplicationContext())
                .getMediaPlayer(videoList.get(vIndex).getPath());
        state = PlaybackStateCompat.STATE_PAUSED;
        return START_NOT_STICKY;
    }


    public void stopNotifications() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);
        if (mediaPlayer != null) mediaPlayer.release();
        stopForeground(true);
        stopSelf();
    }

    public void playPrev() {
        vIndex--;
        if (vIndex < 0) vIndex = listSize - 1;
        playVideo(vIndex);
        DualVideosActivity.onPlayActionListener.onIndexChanged(vIndex);
    }


    public void playNext() {
        vIndex++;
        if (vIndex >= listSize - 1) vIndex = 0;
        playVideo(vIndex);
        DualVideosActivity.onPlayActionListener.onIndexChanged(vIndex);

    }

    public void playVideo(int current) {
        vIndex = current;
        nyVideo = videoList.get(vIndex);
        url = nyVideo.getPath();
        if (url == null) Toast.makeText(this, "video path is null", Toast.LENGTH_LONG).show();
        title = nyVideo.getName();
        if (title == null) title = NyFileUtil.getFileNameWithoutExtFromPath(url);
        proceedPlay();
    }

    private void proceedPlay() {
        SingletonPlayer.getInstance(this).play(url);
        notifyResume();
    }

    public void pausePlayer() {
        SingletonPlayer.getInstance(this).pause();
        notifyPause();
    }

    public void resume() {
        SingletonPlayer.getInstance(this).resume();
        notifyResume();
    }

    public void notifyPause() {
        CreateNotification.createNotification(this, videoList.get(vIndex),
                com.nytaiji.nybase.R.drawable.exo_controls_play, vIndex, listSize);
        state = PlaybackStateCompat.STATE_PAUSED;
    }

    public void notifyResume() {
        CreateNotification.createNotification(this, videoList.get(vIndex),
                com.nytaiji.nybase.R.drawable.exo_controls_pause, vIndex, listSize);
        state = PlaybackStateCompat.STATE_PLAYING;
    }

    public void notifyPiP() {
        listSize = 1;
        CreateNotification.createNotification(this, videoList.get(vIndex),
                com.nytaiji.nybase.R.drawable.exo_controls_pause, 0, listSize);
        state = PlaybackStateCompat.STATE_PLAYING;
    }


    public void onExit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) notificationManager.cancelAll();
        }
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
        }
        //  stopNotifications();
        broadcastReceiver = null;
        stopSelf();
    }

    //the following supposed to work for running service after app exits.
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, elapsedRealtime() + 500,
                restartServicePendingIntent);
        // Log.e(TAG, "---------------------task removed ");
        super.onTaskRemoved(rootIntent);
    }

    public static String getTitle() {
        return title;
    }


}
