package com.nytaiji.core.player;

import android.content.Context;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnStateChangedListener;


public class SingletonPlayer {
    private static volatile SingletonPlayer instance = null;
    BasePlayer mediaPlayer;
    private final Context context;

    private SingletonPlayer(Context context) {
        this.context = context;
    }

    public static SingletonPlayer getInstance(Context context) {
        if (instance == null) {
            synchronized (SingletonPlayer.class) {
                if (instance == null) {
                    instance = new SingletonPlayer(context);
                }
            }
        }
        return instance;
    }

    public BasePlayer getMediaPlayer(String url) {
        //  Log.e("SingletonPlayer", "--------------url----------------" + url);
        if (instance.mediaPlayer == null) {
            //SharedPreferences playerPrefs = context.getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);
            //TODO
            // int playerType = playerPrefs.getInt(KEY_PLAYER, 0);
            //  instance.mediaPlayer = playerType == 0 ? new GoogleExoPlayer(context) : playerType == 1 ? new AndroidPlayer(context): new IjkPlayer(context);
            // instance.mediaPlayer = new GoogleExoPlayer(context);
           // if (isSpecialMedia(url)) {
                instance.mediaPlayer = new AndroidPlayer(context);
           // } else {
                //instance.mediaPlayer = new GoogleExoPlayer(context);
           // }
            return instance.mediaPlayer;
        }
        return null;

    }

    public void play(String url) {
        if (instance.mediaPlayer != null) {
            if (instance.mediaPlayer.isPlaying()) {
                instance.mediaPlayer.release();
            }
            instance.mediaPlayer.release();
            instance.mediaPlayer = null;
        }
        instance.mediaPlayer = getMediaPlayer(url);
        // instance.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        instance.mediaPlayer.setVideoUrl(url);
        instance.mediaPlayer.prepare();
        instance.mediaPlayer.play();
    }

    public void play(OnStateChangedListener onStateChangedListener, String url) {
        play(url);
        instance.mediaPlayer.setOnStateChangeListener(onStateChangedListener);
    }

    public synchronized void pause() {
        if (instance.mediaPlayer != null) {
            instance.mediaPlayer.pause();
        }
    }

    public synchronized void resume() {
        if (instance.mediaPlayer != null) {
            instance.mediaPlayer.play();
        }
    }

    public long getCurrentPosition() {
        if (instance.mediaPlayer != null) {
            return instance.mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public long getDuration() {
        if (instance.mediaPlayer != null) {
            return instance.mediaPlayer.getDuration();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (instance.mediaPlayer != null) {
            return instance.mediaPlayer.isPlaying();
        }
        return true;
    }

    public void setMediaPlayer(BasePlayer basePlayer) {
        instance.mediaPlayer = basePlayer;
    }
}

