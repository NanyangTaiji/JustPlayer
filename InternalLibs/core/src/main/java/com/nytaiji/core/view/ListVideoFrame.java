package com.nytaiji.core.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.core.listener.OnFullscreenChangedListener;

/**
 * 专为列表视频播放定制的播放器,可以自己按需实现
 */
public class ListVideoFrame extends StandardVideoFrame {

    public ListVideoFrame(@NonNull Context context) {
        super(context);
    }

    public ListVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ListVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        buttonBack.setVisibility(View.GONE);
        isSupportVolume = false;
        isSupportBrightness = false;
        isSupportSeek = false;

        setOnFullscreenChangeListener(new OnFullscreenChangedListener() {
            @Override
            public void onFullscreenChange(boolean isFullscreen) {
                buttonBack.setVisibility(isFullscreen ? View.VISIBLE : View.GONE);
                isSupportVolume = isFullscreen;
                isSupportBrightness = isFullscreen;
                isSupportSeek = isFullscreen;
            }
        });
    }
}
