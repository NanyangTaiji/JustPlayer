package com.nanyang.richeditor.util;

import static android.speech.tts.TextToSpeech.QUEUE_ADD;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TTSUtils extends UtteranceProgressListener {

    private Context mContext;
    private static TTSUtils singleton;
    private TextToSpeech textToSpeech; // System voice broadcast class
    private boolean isSuccess = true;
    public boolean isPlaying = false;
    private final SharedPreferences sp;
    private float pitch, speed;
    private Voice voice = null;

    public static TTSUtils getInstance(Context context) {
        if (singleton == null) {
            synchronized (TTSUtils.class) {
                if (singleton == null) {
                    singleton = new TTSUtils(context);
                }
            }
        }
        return singleton;
    }

    private TTSUtils(Context context) {
        this.mContext = context.getApplicationContext();
        sp = context.getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);
        textToSpeech = new TextToSpeech(mContext, i -> {
            //System voice initialization succeeded
            if (i == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.CHINA);
                textToSpeech.setOnUtteranceProgressListener(TTSUtils.this);
                pitch = sp.getFloat("PITCH", 2.0f);
                speed = sp.getFloat("SPEED", 1.0f);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTSUtils", "The system does not support Chinese broadcasting");
                    isSuccess = false;
                }
            }

        });//Baidu's playback engine "com.baidu.duersdk.opensdk"

    }

    public void setPitchNspeed(float pitch, float speed) {
        this.pitch = pitch;
        this.speed = speed;
    }

    public void setVoice(Voice voice) {
        this.voice = voice;
    }

    public void startSpeech(String playText) {

        if (!isSuccess) {
            isPlaying = false;
            Toast.makeText(mContext, "The system does not support Chinese broadcasting", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (textToSpeech != null) {
                    textToSpeech.setPitch(pitch);// Set the tone. The higher the value, the sharper the sound (girls). The lower the value, it will become a male voice. 1.0 is normal
                    textToSpeech.setSpeechRate(speed);
                    textToSpeech.speak(playText, TextToSpeech.QUEUE_ADD, null, null);
                    isPlaying = true;
                }
            }
        }).start();
    }

    public void stopSpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            isPlaying = false;
        }
    }

    public void release() {
        stopSpeech();
        if (textToSpeech != null) textToSpeech.shutdown();
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {

    }

    @Override
    public void onError(String utteranceId) {

    }
}
