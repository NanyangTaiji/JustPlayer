package com.nytaiji.core.base;

import static com.nytaiji.core.base.BaseConstants.RENDER_GLSURFACE_VIEW;
import static com.nytaiji.core.base.BaseConstants.RENDER_SURFACE_VIEW;
import static com.nytaiji.core.base.BaseConstants.RENDER_TEXTURE_VIEW;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

import com.nytaiji.core.listener.OnStateChangedListener;
import com.nytaiji.core.listener.OnVideoSizeChangedListener;
import com.nytaiji.core.player.AndroidPlayer;
import com.nytaiji.core.render.GLRenderView;
import com.nytaiji.core.render.IRenderView;
import com.nytaiji.core.render.SurfaceRenderView;
import com.nytaiji.core.render.TextureRenderView;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.epf.filter.GlFilter;


import java.util.List;
import java.util.Map;

/**
 * 承载播放画面的视图
 */
public class RenderContainerFrame extends FrameLayout implements OnVideoSizeChangedListener, OnStateChangedListener {

    protected Uri videoUri;

    protected String videoUrl;

    protected List<Uri> multiUris;

    protected @RawRes
    int rawId;

    protected String assetFileName;

    protected Map<String, String> headers;

    //播放器配置
    private PlayerConfig playerConfig;

    //播放器核心
    private BasePlayer player;

    //播放器渲染画面视图
    private IRenderView iRenderView;

    //播放器控制层界面
    private IVideoView videoView;

    public RenderContainerFrame(@NonNull Context context) {
        this(context, null);
    }

    public RenderContainerFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenderContainerFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RenderContainerFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void setVideoView(IVideoView videoView) {
        this.videoView = videoView;
    }

    //region 播放器行为

    public void start() {
        //本地视频，wifi连接下直接播放
        if (isLocalVideo() || SystemUtils.isWifiConnected(getContext())) {
            playOrPause();
        } else {
            videoView.handleMobileData();
        }
    }

    public void setVideoUrl(String url) {
        this.videoUrl = url;
    }

    public void setVideoUri(Uri uri) {
        this.videoUri = uri;
    }

    public void setMultiUris(List<Uri> multiUris) {
        // Log.e("RenderContainerFrame ----------------", "setMultiUri");
        this.multiUris = multiUris;
    }

    //设置raw下视频的路径
    public void setVideoRawPath(@RawRes int rawId) {
        this.rawId = rawId;
    }

    //设置assets下视频的路径
    public void setVideoAssetPath(String assetFileName) {
        this.assetFileName = assetFileName;
    }

    public void setVideoHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    protected void playOrPause() {  //original startVideo
        int currentState = player == null ? BasePlayer.STATE_IDLE : player.getCurrentState();
        if (currentState == BasePlayer.STATE_IDLE || currentState == BasePlayer.STATE_ERROR) {
            prepareToPlay();
        } else if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    protected void prepareToPlay() {

        Context context = getContext();

        initPlayer(context);

        removeAllViews();

        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);

        iRenderView = newRenderViewInstance(context);
        if (iRenderView != null) {
            iRenderView.setPlayer(player);
            addView(iRenderView.getRenderView(), layoutParams);
        }
    }

    protected IRenderView newRenderViewInstance(Context context) {
        switch (playerConfig.renderType) {
            case RENDER_TEXTURE_VIEW:
                return new TextureRenderView(context);
            case RENDER_SURFACE_VIEW:
                return new SurfaceRenderView(context);
            case RENDER_GLSURFACE_VIEW:
                return new GLRenderView(context);
        }
        return null;
    }

    public IRenderView getIRenderView() {
        return iRenderView;
    }

    protected void initPlayer(Context context) {
        player = newPlayerInstance(context);
        player.setOnVideoSizeChangedListener(this);
        player.setOnStateChangeListener(this);
        player.setPlayerConfig(playerConfig);
        setDataSource();
        player.initPlayer();
    }

    protected BasePlayer newPlayerInstance(Context context) {
        if (playerConfig != null && playerConfig.player != null) {
            return playerConfig.player;
        }
        return new AndroidPlayer(context);
    }

    public void setDataSource() {
        if (assetFileName != null) {
            player.setVideoAssetPath(assetFileName);
        } else if (rawId != 0) {
            player.setVideoRawPath(rawId);
        } else if (multiUris != null) {
            //  Log.e("RenderContainerFrame ----------------", "setDataSource()");
            player.setMultiUris(multiUris);
        } else if (videoUri != null) {
            player.setVideoUri(videoUri);
        } else player.setVideoUrl(videoUrl, headers);
    }

    protected boolean isLocalVideo() {
        return !isOnline(videoUrl);
       // return !TextUtils.isEmpty(assetFileName) || rawId != 0 || (!TextUtils.isEmpty(videoUrl) && videoUrl.startsWith("file"));
    }

    public void release() {
        if (player != null) {
            player.release();
        }
    }

    public void destroy() {
        if (player != null) {
            player.destroy();
            player = null;
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    //endregion

    @Override
    public void onVideoSizeChanged(int width, int height) {
        videoView.onVideoSizeChanged(width, height);
    }

    @Override
    public void onStateChange(int state) {
        videoView.onStateChange(state);
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        if (playerConfig != null) {
            this.playerConfig = playerConfig;
        }
    }

    public BasePlayer getPlayer() {
        return player;
    }

    public void setGlFilter(final GlFilter filter) {
        // Log.e("RenderContainerFrame", "setGlFilter " + filter);
        iRenderView.setGlFilter(filter);
    }


}
