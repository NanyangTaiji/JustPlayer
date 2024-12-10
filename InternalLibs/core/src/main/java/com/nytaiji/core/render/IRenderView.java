package com.nytaiji.core.render;

import android.graphics.Bitmap;
import android.view.View;

import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.IMediaPlayer;
import com.nytaiji.epf.VideoShotListener;
import com.nytaiji.epf.VideoShotSaveListener;
import com.nytaiji.epf.filter.GlFilter;

import java.io.File;

public interface IRenderView {

    View getRenderView();

    void setPlayer(IMediaPlayer player);

    IMediaPlayer getPlayer();

    void setVideoSize(int width, int height);

    void setDisplayScale(BaseConstants.ScaleType type);

    void setGlFilter(final GlFilter filter);

    /**
     * 截图
     */
    void taskShotPic(VideoShotListener videoShotListener, boolean qualityHigh);

    /**
     * 保存当前帧
     */
    void saveFrame(final File file, final boolean high, final VideoShotSaveListener videoShotSaveListener);

    /**
     * 获取当前画面的bitmap，没有返回空
     */
    Bitmap initCover();

    /**
     * 获取当前画面的高质量bitmap，没有返回空
     */
    Bitmap initCoverHigh();

}
