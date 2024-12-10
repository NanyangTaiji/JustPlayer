package com.nytaiji.vlcplayer;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;

import com.nytaiji.nybase.utils.AppContextProvider;

import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.util.HWDecoderUtil;

import java.util.ArrayList;


public class VLCOptions {
    private static final String TAG = "VLC/VLCConfig";
    private static final int AOUT_AUDIOTRACK = 0;
    private static final int AOUT_OPENSLES = 1;

    private static final int HW_ACCELERATION_AUTOMATIC = -1;
    private static final int HW_ACCELERATION_DISABLED = 0;
    private static final int HW_ACCELERATION_DECODING = 1;
    private static final int HW_ACCELERATION_FULL = 2;

    private static int audiotrackSessionId = 0;

    // TODO should return List<String>
    /* generate an audio session id so as to share audio output with external equalizer *//* CPU intensive plugin, setting for slow devices *//* XXX: why can't the default be fine ? #7792 *//* Configure keystore *///Chromecast
    public static ArrayList<String> getLibOptions() {
        Context context = AppContextProvider.getAppContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audiotrackSessionId == 0) {
            AudioManager audioManager = context.getSystemService(AudioManager.class);
            audiotrackSessionId = audioManager.generateAudioSessionId();
        }

        ArrayList<String> options = new ArrayList<>(50);

        int networkCaching = Math.max(0, Math.min(pref.getInt("network_caching_value", 0), 60000));

        options.add("--stats");
        if (networkCaching > 0) options.add("--network-caching=" + networkCaching);
        options.add("--android-display-chroma");
        options.add("--audio-resampler");
        options.add("soxr");
        options.add("--audiotrack-session-id=" + audiotrackSessionId);

        if (pref.getBoolean("casting_passthrough", false))
            options.add("--sout-chromecast-audio-passthrough");
        else
            options.add("--no-sout-chromecast-audio-passthrough");
        options.add("--sout-chromecast-conversion-quality=" + pref.getString("casting_quality", "2"));
        options.add("--sout-keep");


        return options;
    }


    public static String getAout(SharedPreferences pref) {
        int aout = -1;
        try {
            aout = Integer.parseInt(pref.getString("aout", "-1"));
        } catch (NumberFormatException ignored) {
        }

        HWDecoderUtil.AudioOutput hwaout = HWDecoderUtil.getAudioOutputFromDevice();
        if (hwaout == HWDecoderUtil.AudioOutput.AUDIOTRACK || hwaout == HWDecoderUtil.AudioOutput.OPENSLES)
            aout = (hwaout == HWDecoderUtil.AudioOutput.OPENSLES) ? AOUT_OPENSLES : AOUT_AUDIOTRACK;

        return (aout == AOUT_OPENSLES) ? "opensles_android" : null; /* audiotrack is the default */
    }


    public static void setMediaOptions(IMedia media, Context context, int flags, boolean hasRenderer) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (hasRenderer) {
            media.addOption(":sout-chromecast-audio-passthrough=" + prefs.getBoolean("casting_passthrough", true));
            media.addOption(":sout-chromecast-conversion-quality=" + prefs.getString("casting_quality", "2"));
        }
    }

}