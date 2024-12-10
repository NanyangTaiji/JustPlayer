package com.nytaiji.core.base;

public class BaseConstants {
    public enum ScaleType {
        SCALE_DEFAULT,
        SCALE_ORIGINAL,
        SCALE_16_9,
        SCALE_4_3,
        SCALE_MATCH_PARENT,
        SCALE_CENTER_CROP,
        SCALE_32_9,
    }
    
    //PlayerAudioManager
    public static final float MEDIA_VOLUME_DEFAULT = 1.0f;
    public static final float MEDIA_VOLUME_DUCK = 0.3f;

    //PlayerConfig
    public static final int LANDSCAPE_FULLSCREEN_MODE = 0;  //横向的全屏模式
    public static final int PORTRAIT_FULLSCREEN_MODE = 1;  //竖向的全屏模式
    public static final int AUTO_FULLSCREEN_MODE = 2;      //根据视频内容宽高比，自动判定全屏模式, 宽>高（横屏全屏), 宽 < 高(竖屏全屏)

    public static final int RENDER_TEXTURE_VIEW = 0;  //用texture渲染播放界面
    public static final int RENDER_SURFACE_VIEW = 1;  //用surfaceview渲染播放界面
    public static final int RENDER_GLSURFACE_VIEW = 2;
    public static final int RENDER_NONE = 3;          //没有渲染界面

    //HeadSetBroadCastReceiver
    public static final String MEDIA_KEY = "media_key";
    public static final int MEDIA_PLAY = 1;
    public static final int MEDIA_PAUSE = 2;

}
