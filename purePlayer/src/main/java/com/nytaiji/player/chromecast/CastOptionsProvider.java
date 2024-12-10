package com.nytaiji.player.chromecast;

import android.content.Context;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.NotificationOptions;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CastOptionsProvider implements OptionsProvider {
    @NotNull
    public CastOptions getCastOptions(@NotNull Context p0) {
        NotificationOptions notificationOptions = (new NotificationOptions.Builder()).setTargetActivityClassName(ExpandedControlsActivity.class.getName()).build();
        CastMediaOptions mediaOptions = (new CastMediaOptions.Builder()).setNotificationOptions(notificationOptions).setExpandedControllerActivityClassName(ExpandedControlsActivity.class.getName()).build();
        return (new CastOptions.Builder()).setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID).setCastMediaOptions(mediaOptions).build();
    }

    @Nullable
    public List getAdditionalSessionProviders(@NotNull Context p0) {
        return null;
    }
}

