package com.nytaiji.core.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nytaiji.core.listener.HeadsetBroadcastReceiver;

import static android.content.Context.AUDIO_SERVICE;
import static com.nytaiji.core.base.BaseConstants.MEDIA_KEY;
import static com.nytaiji.core.base.BaseConstants.MEDIA_PAUSE;
import static com.nytaiji.core.base.BaseConstants.MEDIA_PLAY;
import static com.nytaiji.core.base.BaseConstants.MEDIA_VOLUME_DUCK;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PlayerAudioManager {
    private final Context context;
    private AudioManager audioManager;
    private final IMediaPlayer mediaPlayer;
    private final HeadsetBroadcastReceiver headsetBroadcastReceiver;
    private static float left = 1.0f;
    private static float right = 1.0f;
    private static boolean pFocus_pause = true;
    private static boolean tFocus_pause = true;
    private static boolean can_duck = true;
    private int music_vol_level = 8;
    private int phone_vol_level = 8;

    public static int boostLevel = 0;
    public int volumeUpsInRow = 0;
    public LoudnessEnhancer loudnessEnhancer = null;

    public int audioSessionId = -1;
    //-----------------
    private boolean mPlayOnAudioFocus = false;

    public boolean isPlayOnAudioFocus() {
        return mPlayOnAudioFocus;
    }

    private boolean isMediaActionRegister = false;
    private boolean isHeadSetRegister = false;

    public void setPlayOnAudioFocus(boolean audioFocus) {
        this.mPlayOnAudioFocus = audioFocus;
    }
    //-----------------------------

    public PlayerAudioManager(Context context, IMediaPlayer mediaPlayer) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        headsetBroadcastReceiver = new HeadsetBroadcastReceiver();
        if (loudnessEnhancer != null) {
            loudnessEnhancer.release();
        }
        registerHeadsetReceiver();
        registerMediaActionReceiver();

    }

    //TODO ny the above booleans needed to avoid multi-unregisters with floatVideoview and videoview

    protected final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            SharedPreferences currentState = PreferenceManager.getDefaultSharedPreferences(context);
            left = currentState.getFloat("left", 1.0f);
            right = currentState.getFloat("right", 1.0f);
            pFocus_pause = currentState.getBoolean("pFocusPause", true);
            tFocus_pause = currentState.getBoolean("tFocusPause", true);
            can_duck = currentState.getBoolean("canDuck", true);

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPlayOnAudioFocus && !mediaPlayer.isPlaying()) {
                        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                        phone_vol_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, music_vol_level, 0);
                        mediaPlayer.play();
                    } else if (mediaPlayer.isPlaying()) {
                        mediaPlayer.setDualVolume(left, right);
                    }
                    mPlayOnAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (can_duck)
                        mediaPlayer.setDualVolume(MEDIA_VOLUME_DUCK, MEDIA_VOLUME_DUCK);
                    Log.e("AudioFocusCANDuck", "onAudioDuckChange: Invoked: val:" + can_duck);

                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mediaPlayer.isPlaying() && tFocus_pause) {
                        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                        music_vol_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        mPlayOnAudioFocus = true;
                        mediaPlayer.pause();
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, phone_vol_level, 0);
                        Log.e("AudioFocusLossTrans", "onAudioFocusChange: Invoked");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (mediaPlayer.isPlaying()) {
                        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                        music_vol_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                        if (pFocus_pause) {
                            mPlayOnAudioFocus = true;
                            mediaPlayer.pause();
                        } else {
                            mPlayOnAudioFocus = false;
                            audioManager.abandonAudioFocus(this);
                            mediaPlayer.pause();
                            if (isHeadsetOn(context)) {
                                SharedPreferences.Editor editor = currentState.edit();
                                editor.putInt("earVolLevel", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                                editor.commit();
                            } else {
                                SharedPreferences.Editor editor = currentState.edit();
                                editor.putInt("volLevel", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                                editor.commit();
                            }
                        }
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, phone_vol_level, 0);
                        SharedPreferences.Editor editor = currentState.edit();
                        editor.putInt("volLevel", music_vol_level);
                        editor.commit();
                        Log.e("AudioFocusLossPerma", "onAudioFocusChange: Invoked");
                    }

                    audioManager.abandonAudioFocus(this);
                    mPlayOnAudioFocus = false;
                //    stop();
                    break;
            }
        }
    };

    private boolean isHeadsetOn(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (am == null)
            return false;

        AudioDeviceInfo[] devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo device : devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
                    || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                    || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver mediaActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HeadsetBroadcastReceiver.MEDIA_ACTION.equals(intent.getAction())) {
                int mediaStatus = intent.getIntExtra(MEDIA_KEY, -1);
                switch (mediaStatus) {
                    case MEDIA_PAUSE:
                        if (mediaPlayer != null) {
                            mediaPlayer.pause();
                        }
                        break;
                    case MEDIA_PLAY:
                        if (mediaPlayer != null) {
                            mediaPlayer.play();
                        }
                        break;
                }
            }
        }
    };

    private void registerMediaActionReceiver() {
        IntentFilter intentFilter = new IntentFilter(HeadsetBroadcastReceiver.MEDIA_ACTION);
        context.registerReceiver(mediaActionReceiver, intentFilter);
        isMediaActionRegister = true;
    }

    private void unregisterMediaActionReceiver() {
        if (context != null && isMediaActionRegister) {
            context.unregisterReceiver(mediaActionReceiver);
            isMediaActionRegister = false;
        }
    }

    private void registerHeadsetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        //  if (headsetBroadcastReceiver == null)
        context.registerReceiver(headsetBroadcastReceiver, intentFilter);
        isHeadSetRegister = true;
        ComponentName componentName = new ComponentName(context.getPackageName(), HeadsetBroadcastReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(componentName);
    }

    public void unregisterHeadsetReceiver() {
        ComponentName componentName = new ComponentName(context.getPackageName(), HeadsetBroadcastReceiver.class.getName());
        audioManager.unregisterMediaButtonEventReceiver(componentName);
        if (isHeadSetRegister && headsetBroadcastReceiver != null) {
            context.unregisterReceiver(headsetBroadcastReceiver);
            isHeadSetRegister = false;
        }
    }

    public boolean audioFocusObtained() {
        final int result = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    //获取音频焦点
    public void requestAudioFocus() {
        audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    //丢弃音频焦点
    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    //获取最大音量
    public int getStreamMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //获取当前音量
    public int getStreamVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    //设置音量
    public void setStreamVolume(int value) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
    }

    public void destroy() {
        abandonAudioFocus();
        unregisterHeadsetReceiver();
        unregisterMediaActionReceiver();
        if (loudnessEnhancer != null) {
            loudnessEnhancer.release();
            loudnessEnhancer = null;
        }
        notifyAudioSessionUpdate(false);
    }

    //-------------------------------------------------------------------------------//
    public static int getVolume(final Context context, final boolean max, final AudioManager audioManager) {
        if (Build.VERSION.SDK_INT >= 30 && Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            try {
                Method method;
                Object result;
                Class<?> clazz = Class.forName("com.samsung.android.media.SemSoundAssistantManager");
                Constructor<?> constructor = clazz.getConstructor(Context.class);
                final Method getMediaVolumeInterval = clazz.getDeclaredMethod("getMediaVolumeInterval");
                result = getMediaVolumeInterval.invoke(constructor.newInstance(context));
                if (result instanceof Integer) {
                    int mediaVolumeInterval = (int) result;
                    if (mediaVolumeInterval < 10) {
                        method = AudioManager.class.getDeclaredMethod("semGetFineVolume", int.class);
                        result = method.invoke(audioManager, AudioManager.STREAM_MUSIC);
                        if (result instanceof Integer) {
                            if (max) {
                                return 150 / mediaVolumeInterval;
                            } else {
                                int fineVolume = (int) result;
                                return fineVolume / mediaVolumeInterval;
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if (max) {
            return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } else {
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
    }


    public static boolean isVolumeMax(final AudioManager audioManager) {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public static boolean isVolumeMin(final AudioManager audioManager) {
        int min = Build.VERSION.SDK_INT >= 28 ? audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC) : 0;
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == min;
    }

    public void adjustVolume(final boolean raise) {
        boolean canBoost = isVolumeMax(audioManager);

        if (loudnessEnhancer == null && mediaPlayer.getAudioSessionId() != -1) {
            audioSessionId = mediaPlayer.getAudioSessionId();
            try {
                loudnessEnhancer = new LoudnessEnhancer(mediaPlayer.getAudioSessionId());
                notifyAudioSessionUpdate(true);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        // playerView.removeCallbacks(playerView.textClearRunnable);

        final int volume = getVolume(context, false, audioManager);
        final int volumeMax = getVolume(context, true, audioManager);
        boolean volumeActive = volume != 0;

        // Handle volume changes outside the app (lose boost if volume is not maxed out)
        if (volume != volumeMax) {
            boostLevel = 0;
        }

        if (loudnessEnhancer == null)
            canBoost = false;

        if (volume != volumeMax || (boostLevel == 0 && !raise)) {
            if (loudnessEnhancer != null)
                loudnessEnhancer.setEnabled(false);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, raise ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            final int volumeNew = getVolume(context, false, audioManager);
            // Custom volume step on Samsung devices (Sound Assistant)
            if (raise && volume == volumeNew) {
                volumeUpsInRow++;
            } else {
                volumeUpsInRow = 0;
            }
            if (volumeUpsInRow > 4 && !isVolumeMin(audioManager)) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
            } else {
                volumeActive = volumeNew != 0;
                //  playerView.setIconVolume(volumeActive);
                // playerView.setCustomErrorMessage(volumeActive ? " " + volumeNew : "");
            }
        } else {
            if (canBoost && raise && boostLevel < 10)
                boostLevel++;
            else if (!raise && boostLevel > 0)
                boostLevel--;

            if (loudnessEnhancer != null) {
                try {
                    loudnessEnhancer.setTargetGain(boostLevel * 200);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            // playerView.setCustomErrorMessage(" " + (volumeMax + PlayerActivity.boostLevel));
        }
        if (loudnessEnhancer != null)
            loudnessEnhancer.setEnabled(boostLevel > 0);

        // playerView.setHighlight(PlayerActivity.boostLevel > 0);

       /* if (clear) {
            playerView.postDelayed(playerView.textClearRunnable, CustomPlayerView.MESSAGE_TIMEOUT_KEY);
        }*/
    }


    private void notifyAudioSessionUpdate(final boolean active) {
        if (audioSessionId == -1) return;
        final Intent intent = new Intent(active ? AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
                : AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.getPackageName());
        if (active) {
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MOVIE);
        }
        try {
            context.sendBroadcast(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}
