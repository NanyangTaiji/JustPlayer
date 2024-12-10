package com.nanyang.richeditor.util;

import static android.speech.tts.TextToSpeech.QUEUE_ADD;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Spanned;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.nanyang.richeditor.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SpeechHelper {
    private Context context;
    private TextToSpeech tts;
    private View root;
    private int language;
    private int gender;
    private String[] voiceName;
    private SharedPreferences sp;
    private SpeechCallback callback;
    private Dialog dialog;
    private Voice voice = null;
    private Set<String> genderSet = new HashSet<>();
    public static boolean isSpeaking = false;

    public SpeechHelper(Context context, TextToSpeech tts, SpeechCallback callback) {
        this.context = context;
        this.tts = tts;
        this.callback = callback;
    }


    public void SpeechDialog() {
        root = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_speech, null);

        sp = context.getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);

        final RadioGroup rgg = root.findViewById(R.id.setting_gender);
        gender = sp.getInt("GENDER", 0);
        rgg.check(gender == 0 ? R.id.l_male : R.id.l_female);
        rgg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.l_male) {
                    gender = 0;
                } else {
                    gender = 1;
                }
                sp.edit().putInt("GENDER", gender).apply();
                dialog.dismiss();
                callback.voiceChosen(null);
            }
        });

        genderSet.add(gender == 0 ? "male" : "female");

        final RadioGroup rgl = root.findViewById(R.id.setting_language);
        language = sp.getInt("LANGUAGE", 0);
        rgl.check(language == 0 ? R.id.l_chinese : R.id.l_english);

        String voiceChinese = sp.getString("CHINESEVOICE", "cmn-cn-x-ccc-local");
        String voiceEnglish = sp.getString("ENGLISHVOICE", "en-us-x-sfg#male_2-local");

        if (language == 0) {
            voice = new Voice(voiceChinese, new Locale("cmn", "CHINA"), 400, 200, false, genderSet);
        } else {
            voice = new Voice(voiceEnglish, new Locale("en", "US"), 400, 200, false, genderSet);
        }

        voiceName = getVoiceList(tts);
        rgl.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.l_chinese) {
                    language = 0;
                } else {
                    language = 1;
                }
                sp.edit().putInt("LANGUAGE", language).apply();
                dialog.dismiss();
                callback.voiceChosen(null);
            }
        });

        final SeekBar pitchSeeker = root.findViewById(R.id.seek_bar_pitch);
        float pitch = sp.getFloat("PITCH", 0.5f);
        pitchSeeker.setMin(5);
        pitchSeeker.setMax(100);
        pitchSeeker.setProgress((int) pitch * 50);

        final SeekBar speedSeeker = root.findViewById(R.id.seek_bar_speed);
        float speed = sp.getFloat("SPEED", 0.5f);
        speedSeeker.setMin(5);
        speedSeeker.setMax(100);
        speedSeeker.setProgress((int) speed * 50);


        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Speech Parameters");
        builder.setView(root);
        int prefChinese = sp.getInt("PREFCHINESE", 1);
        int prefEnglish = sp.getInt("PREFENGLISH", 1);

        builder.setSingleChoiceItems(voiceName, language == 0 ? prefChinese : prefEnglish, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (rgg.getCheckedRadioButtonId() == R.id.l_male) {
                    sp.edit().putInt("GENDER", 0).apply();
                } else sp.edit().putInt("GENDER", 1).apply();

                if (language == 0) {
                    sp.edit().putInt("PREFCHINESE", which).apply();
                    sp.edit().putString("CHINESEVOICE", voiceName[which]).apply();
                    voice = new Voice(voiceName[which], new Locale("cmn", "CHINA"), 400, 200, false, genderSet);
                } else {
                    sp.edit().putInt("PREFENGLISH", which).apply();
                    sp.edit().putString("ENGLISHVOICE", voiceName[which]).apply();
                    voice = new Voice(voiceName[which], new Locale("en", "US"), 400, 200, false, genderSet);
                }

            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sp.edit().putFloat("PITCH", (float) pitchSeeker.getProgress() / 50).apply();
                sp.edit().putFloat("SPEED", (float) speedSeeker.getProgress() / 50).apply();
                callback.voiceChosen(voice);
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", null);
        dialog = builder.create();
        dialog.show();
    }


    private String[] getVoiceList(TextToSpeech tts) {
        List<String> voiceL = new ArrayList<>();
        for (Voice tmpVoice : tts.getVoices()) {
            String tv = tmpVoice.getName();
            if (!tv.contains("network")) {
                if ((tv.contains("cmn-cn") && language == 0) || (tv.contains("en-us") && language == 1)) {
                    voiceL.add(tv);
                }
            }
            Collections.sort(voiceL);
        }
        return voiceL.toArray(new String[0]);
    }

    public static void speak(Context context, TextToSpeech tts, String str) {
        isSpeaking = true;
        SharedPreferences sp = context.getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);
        float pitch = sp.getFloat("PITCH", 0.5f);
        float speed = sp.getFloat("SPEED", 0.5f);
        int gender = sp.getInt("GENDER", 0);
        int language = sp.getInt("LANGUAGE", 0);
        Locale locale = new Locale("cmn", "CHINA");
        String voiceName = sp.getString("CHINESEVOICE", "cmn-cn-x-ccc-local");
        if (language == 1) {
            locale = new Locale("en", "US");
            voiceName = sp.getString("ENGLISHVOICE", "en-us-x-sfg#male_2-local");
        }

        speak(tts, voiceName, gender, locale, pitch, speed, str);
    }

    public static Voice getVoice(Context context) {

        SharedPreferences sp = context.getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);
        int gender = sp.getInt("GENDER", 0);
        int language = sp.getInt("LANGUAGE", 0);
        Locale locale = new Locale("cmn", "CHINA");
        String voiceName = sp.getString("CHINESEVOICE", "cmn-cn-x-ccc-local");
        if (language == 1) {
            locale = new Locale("en", "US");
            voiceName = sp.getString("ENGLISHVOICE", "en-us-x-sfg#male_2-local");
        }
        Set<String> genderSet = new HashSet<>();
        genderSet.add(gender == 0 ? "male" : "female");
        return new Voice(voiceName, locale, 400, 200, false, genderSet);
    }

    public static void speak(TextToSpeech tts, String voiceName, int gender, Locale locale, float pitch, float speed, String str) {
        isSpeaking = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                tts.setPitch(pitch);
                tts.setSpeechRate(speed);
                Set<String> genderSet = new HashSet<>();
                genderSet.add(gender == 0 ? "male" : "female");
                Voice voice = new Voice(voiceName, locale, 400, 200, false, genderSet);
                tts.setVoice(voice);
                tts.speak(str, QUEUE_ADD, null, null);
                tts.playSilentUtterance(2000, QUEUE_ADD, null);
            }
        }).start();

    }

    public static void stop(TextToSpeech tts) {
        isSpeaking=false;
        if (tts != null) tts.stop();
    }

    public static void release(TextToSpeech tts) {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public static void speak(Context context, TextToSpeech tts, Voice voice, String str) {
        isSpeaking = true;
        SharedPreferences sp = context.getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);
        float pitch = sp.getFloat("PITCH", 0.5f);
        float speed = sp.getFloat("SPEED", 0.5f);
        int language = sp.getInt("LANGUAGE", 0);
      /*  new Thread(new Runnable() {
            @Override
            public void run() {*/
        tts.setPitch(pitch);
        tts.setSpeechRate(speed);
        if (language == 0) tts.setLanguage(Locale.CHINA);
        else tts.setVoice(voice);
        tts.speak(str, QUEUE_ADD, null, null);
        tts.playSilentUtterance(2000, QUEUE_ADD, null);
       /*     }
        }).start();*/


    }
    // https://stackoverflow.com/questions/24758698/paste-without-rich-text-formatting-into-edittext
    //A perfect and easy way: Override the EditText's onTextContextMenuItem and intercept the android.R.id.paste to be android.R.id.pasteAsPlainText

   /*
    @Override
    public boolean onTextContextMenuItem(int id) {
        if (id == android.R.id.paste) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                id = android.R.id.pasteAsPlainText;
            } else {
                onInterceptClipDataToPlainText();
            }
        }
        return super.onTextContextMenuItem(id);
    }
   */

    public static void onInterceptClipDataToPlainText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) {
                final CharSequence paste;
                // Get an item as text and remove all spans by toString().
                final CharSequence text = clip.getItemAt(i).coerceToText(context);
                paste = (text instanceof Spanned) ? text.toString() : text;
                if (paste != null) {
                    copyToClipBoard(context, paste);
                }
            }
        }
    }

    public static void copyToClipBoard(@NonNull Context context, @NonNull CharSequence text) {
        ClipData clipData = ClipData.newPlainText("rebase_copy", text);
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(clipData);
    }

}