package com.nytaiji.core.base;

import static com.nytaiji.core.base.BaseConstants.AUTO_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.LANDSCAPE_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.PORTRAIT_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.RENDER_GLSURFACE_VIEW;
import static com.nytaiji.core.base.BaseConstants.RENDER_NONE;
import static com.nytaiji.core.base.BaseConstants.RENDER_SURFACE_VIEW;
import static com.nytaiji.core.base.BaseConstants.RENDER_TEXTURE_VIEW;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PlayerConfig {


    @IntDef({LANDSCAPE_FULLSCREEN_MODE, PORTRAIT_FULLSCREEN_MODE, AUTO_FULLSCREEN_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FullScreeMode {
    }



    @IntDef({
            RENDER_TEXTURE_VIEW, RENDER_SURFACE_VIEW, RENDER_GLSURFACE_VIEW, RENDER_NONE
    })
    private @interface RenderView {
    }

    final int renderType;  //渲染类型
    private int fullScreenMode;

  //  private int encryptLevel;
    public final boolean enableMediaCodec;  //是否启用硬解码
    public final boolean enableOpenSLES;   //是否启用OpenSL ES
    public final boolean looping; //是否循环播放
    public final float aspectRatio; //正常模式下播放画面的高宽比
    public final BasePlayer player;  //自定义播放器播放
    public boolean enableAudioManager=true;  //是否启用硬解码
    //TODO
    public String passWord = null;
    public String fileName = null;

    private PlayerConfig(Builder builder) {
        this.fullScreenMode = builder.screenMode;
        this.renderType = builder.renderType;
        this.enableMediaCodec = builder.enableMediaCodec;
        this.enableOpenSLES = builder.enableOpenSLES;
        this.enableAudioManager = builder.enableAudioManager;
        this.player = builder.player;
        this.looping = builder.looping;
        this.aspectRatio = builder.aspectRatio;
    }

    public static class Builder {
        private int screenMode=AUTO_FULLSCREEN_MODE;
        private int renderType=RENDER_TEXTURE_VIEW;
        private boolean enableMediaCodec=true;
        private boolean enableOpenSLES=true;
        //ADD for silent float video in duovideos
        private boolean enableAudioManager=true;
        private boolean looping=false;
        private float aspectRatio; //播放画面高宽比
        private BasePlayer player;
        //---------------------------------add Aug-9-2021------------


        public PlayerConfig build() {
            return new PlayerConfig(this);
        }

        public Builder fullScreenMode(@FullScreeMode int screenMode) {
            this.screenMode = screenMode;
            return this;
        }

        public Builder renderType(@RenderView int renderType) {
            this.renderType = renderType;
            return this;
        }

        public Builder enableMediaCodec(boolean enableMediaCodec) {
            this.enableMediaCodec = enableMediaCodec;
            return this;
        }

        public Builder enableOpenSLES(boolean enableOpenSLES) {
            this.enableOpenSLES = enableOpenSLES;
            return this;
        }

        public Builder enableAudioManager(boolean enableAudioManager) {
            this.enableAudioManager = enableAudioManager;
            return this;
        }

        public Builder looping(boolean isLooping) {
            this.looping = isLooping;
            return this;
        }

        public Builder aspectRatio(float aspectRatio) {
            this.aspectRatio = aspectRatio;
            return this;
        }

        public Builder player(BasePlayer player) {
            this.player = player;
            return this;
        }

    }

    public void setFullScreenMode(int fullScreenMode){
        this.fullScreenMode=fullScreenMode;
    }

    public int getFullScreenMode(){
        return fullScreenMode;
    }


    public void setPassword(String password) {
        passWord = password;
    }

    public String getPassword() {
        return passWord;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getRenderType() {
        return renderType;
    }

}
