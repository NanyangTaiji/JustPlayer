package com.nytaiji.core.base;

import static com.nytaiji.core.base.BaseConstants.AUTO_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.LANDSCAPE_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.PORTRAIT_FULLSCREEN_MODE;
import static com.nytaiji.core.base.IMediaPlayer.STATE_COMPLETED;
import static com.nytaiji.core.base.IMediaPlayer.STATE_ERROR;
import static com.nytaiji.core.base.IMediaPlayer.STATE_IDLE;
import static com.nytaiji.core.base.IMediaPlayer.STATE_PREPARING;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.core.listener.OnFullscreenChangedListener;
import com.nytaiji.core.listener.OnStateChangedListener;
import com.nytaiji.core.listener.OnPlayActionListener;
import com.nytaiji.core.listener.OnVideoSizeChangedListener;
import com.nytaiji.core.render.IRenderView;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.SystemUtils;


import java.util.List;
import java.util.Map;


public abstract class BaseVideoFrame extends FrameLayout implements IVideoView {

    protected ViewGroup surfaceContainer;
    protected OrientationHelper orientationHelper;
    protected OnPlayActionListener onPlayActionListener;

    //是否支持重力感应自动横竖屏，默认支持
    private boolean supportSensorRotate = true;

    //是否跟随系统的方向锁定，默认跟随
    private boolean rotateWithSystem = true;

    //播放器配置 make non-final
    protected PlayerConfig playerConfig;

    //播放器播放画面视图
    protected RenderContainerFrame renderContainerFrame;

    protected OnVideoSizeChangedListener onVideoSizeChangedListener;
    protected OnFullscreenChangedListener onFullScreenChangeListener;
    protected OnStateChangedListener onStateChangedListener;

    protected boolean isFullScreen = false;

    private int mSystemUiVisibility;

    //全屏之前，正常状态下控件的宽高
    protected int originWidth;
    protected int originHeight;

    //父视图
    protected ViewParent viewParent;
    //当前view在父视图中的布局参数
    protected ViewGroup.LayoutParams viewLayoutParams;
    //当前view在父视图中的位置
    protected int positionInParent;

    //actionbar可见状态记录
    private boolean actionBarVisible;


    //当前播放器状态
    protected int currentState;

    //TODO
    public String videoUrl;
    public Uri videoUri;
    public List<Uri> multiUris;

    public BaseVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public BaseVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    //TODO to match android player
    public void setVideoUri(Uri uri) {
        videoUri = uri;
        setVideoUrl(NyFileUtil.getPath(getContext(), videoUri));
        renderContainerFrame.setVideoUri(videoUri);
    }

    public void setVideoUrl(String url) {
        videoUrl = url;
        renderContainerFrame.setVideoUrl(url);
        setTitle(NyFileUtil.getLastSegmentFromString(url));
    }

    public void setMultiUris(List<Uri> multiUris) {
        this.multiUris = multiUris;
        // Log.e("BaseVideoFrame ----------------", "setMultiUri");
        renderContainerFrame.setMultiUris(multiUris);
    }


    public Uri getVideoUri() {
        if (videoUri == null && videoUrl != null) videoUri = Uri.parse(videoUrl);
        return videoUri;
    }

    public String getVideoUrl() {
        if (videoUrl == null && videoUri != null)
            videoUrl = NyFileUtil.getPath(getContext(), videoUri);
        return videoUrl;
    }


    public void setVideoRawPath(@RawRes int rawId) {
        renderContainerFrame.setVideoRawPath(rawId);
    }

    public void setVideoAssetPath(String assetFileName) {
        renderContainerFrame.setVideoAssetPath(assetFileName);
    }

    public void setVideoHeaders(Map<String, String> headers) {
        renderContainerFrame.setVideoHeaders(headers);
    }

    @Override
    public RenderContainerFrame getRenderContainerFrame() {
        return renderContainerFrame;
    }

    /**
     * 从父控件中剥离出来，将变量置空后，返回，防止内存泄漏
     * <p>
     * 用于将播放器画面剥离出来，添加到另外的控制层界面上，实现窗口画面的转移，比如小窗口播放
     *
     * @return
     */
    @Override
    public RenderContainerFrame getRenderContainerViewOffParent() {

        ViewParent parent = renderContainerFrame.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(renderContainerFrame);
        }
        RenderContainerFrame result = renderContainerFrame;

        //renderContainerView和playConfig会被剥离出去的renderContainerView实例持有引用，因此置空，防止内存泄漏
        renderContainerFrame = null;
        playerConfig = null;

        return result;
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutId(), this);
        surfaceContainer = findViewById(getSurfaceContainerId());
        playerConfig = new PlayerConfig.Builder().build();
        orientationHelper = new OrientationHelper(this);

        renderContainerFrame = newRenderContainerView();
        addRenderContainer(renderContainerFrame);

    }

    //添加播放器画面视图，到播放器界面上
    @Override
    public void addRenderContainer(RenderContainerFrame renderContainerFrame) {
        this.renderContainerFrame = renderContainerFrame;
        surfaceContainer.removeAllViews();
        surfaceContainer.addView(renderContainerFrame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        renderContainerFrame.setVideoView(this);
        renderContainerFrame.setPlayerConfig(playerConfig);
    }

    protected RenderContainerFrame newRenderContainerView() {
        return new RenderContainerFrame(getContext());
    }

    @Override
    public BaseVideoFrame getPlayView() {
        return this;
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
        resizeRenderView(width, height);
    }

    public int getCurrentState() {
        return currentState;
    }

    //重新设置播放器状态
    public void setPlayerStatus(int status) {
        currentState = status;
        onStateChange(status);
    }

    @Override
    public void onStateChange(int state) {
        currentState = state;
        if (onStateChangedListener != null) {
            onStateChangedListener.onStateChange(state);
        }

        Context context = getContext();
        switch (state) {
            case BasePlayer.STATE_PLAYING:
            case BasePlayer.STATE_BUFFERING_START:
                SystemUtils.keepScreenOn(context);
                break;
            default:
                //  SystemUtils.removeScreenOn(context);
                break;
        }
        updatePlayIcon();
    }

    protected void updatePlayIcon() {
        if (isPlaying()) {
            setPlayingIcon();
        } else {
            setPausedIcon();
        }
    }

    //设置播放时，播放按钮图标
    protected abstract void setPlayingIcon();

    //设置暂停时，播放按钮图标
    protected abstract void setPausedIcon();

    public void setPlayerConfig(PlayerConfig playerConfig) {
        if (playerConfig != null) {
            this.playerConfig = playerConfig;
            renderContainerFrame.setPlayerConfig(playerConfig);
        }
    }

    protected abstract @IdRes
    int getSurfaceContainerId();

    protected abstract @LayoutRes
    int getLayoutId();

    public abstract boolean onBackKeyPressed();

    public abstract void setTitle(String title);

    /**
     * @return 控制是否支持重力感旋转屏幕来全屏等操作，竖向全屏模式和智能全屏模式下不开启重力感应旋转屏幕，避免造成奇怪的交互。
     */
    @Override
    public boolean supportSensorRotate() {
        return supportSensorRotate && playerConfig.getFullScreenMode() == LANDSCAPE_FULLSCREEN_MODE;
    }

    public void setSupportSensorRotate(boolean supportSensorRotate) {
        this.supportSensorRotate = supportSensorRotate;
    }

    @Override
    public boolean rotateWithSystem() {
        return rotateWithSystem;
    }

    public void setRotateWithSystem(boolean rotateWithSystem) {
        this.rotateWithSystem = rotateWithSystem;
    }

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    public void setOnFullscreenChangeListener(OnFullscreenChangedListener onFullScreenChangeListener) {
        this.onFullScreenChangeListener = onFullScreenChangeListener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    public void setOnPlayActionListener(OnPlayActionListener onPlayActionListener) {
        this.onPlayActionListener = onPlayActionListener;
    }

    //region 播放器相关


    public void replay() {
        BasePlayer player = getPlayer();
        if (player != null) {
            player.replay();
        }
    }

    public void start() {
        orientationHelper.start();
        renderContainerFrame.start();
        if (onPlayActionListener != null)
            onPlayActionListener.start();
        updatePlayIcon();
        //TODO
        //   renderContainerView.startVideo();
    }

    public void pause() {
        BasePlayer player = getPlayer();
        if (player != null) {
            player.pause();
        }
        if (onPlayActionListener != null)
            onPlayActionListener.pause();
    }


    public void playOrPause() {
        renderContainerFrame.playOrPause();
        updatePlayIcon();
        if (onPlayActionListener != null) {
            if (isPlaying()) onPlayActionListener.start();
            else onPlayActionListener.pause();

        }
    }

    public boolean isInPlaybackState() {
        return currentState != STATE_ERROR
                && currentState != STATE_IDLE
                && currentState != STATE_PREPARING
                && currentState != STATE_COMPLETED;
    }

    public boolean isPlaying() {
        BasePlayer player = getPlayer();
        return player != null && isInPlaybackState() && player.isPlaying();
    }

    public void release() {
        BasePlayer player = getPlayer();
        if (player != null) {
            player.release();
        }
        if (onPlayActionListener != null)
            onPlayActionListener.release();
    }


    public void destroy() {
        BasePlayer player = getPlayer();
        if (player != null) {
            player.destroy();
        }
        orientationHelper.setOrientationEnable(false);
    }

    public long getDuration() {
        BasePlayer player = getPlayer();
        if (player==null){
            Log.e("BaseVideoFragme", "player null in getDuration");
            return -1L;
        }
        return player.getDuration();
    }

    public void seekTo(long position) {
        BasePlayer player = getPlayer();
        if (player != null) {
            player.seekTo(position);
        }
    }

    public long getCurrentPosition() {
        BasePlayer player = getPlayer();
        return player == null ? 0 : player.getCurrentPosition();
    }

    public int getStreamMaxVolume() {
        BasePlayer player = getPlayer();
        return player == null ? 0 : player.getStreamMaxVolume();
    }


    public int getStreamVolume() {
        BasePlayer player = getPlayer();
        return player != null ? player.getVolume() : 0;
    }

    public void setVolume(int value) {
        BasePlayer player = getPlayer();
        if (player != null) {
            player.setVolume(value);
        }
    }

    public BasePlayer getPlayer() {
        return renderContainerFrame == null ? null : renderContainerFrame.getPlayer();
    }
    //endregion

    /**
     * 数据网络下，默认情况下直接播放
     */
    @Override
    public void handleMobileData() {
        playOrPause();
    }

    //region 全屏处理

    /**
     * 正常情况下，通过点击全屏按钮来全屏
     */
    public void startFullscreen() {
        startFullscreenWithOrientation(getFullScreenOrientation());
    }

    /**
     * 通过重力感应，旋转屏幕来全屏
     *
     * @param orientation
     */
    @Override
    public void startFullscreenWithOrientation(int orientation) {

        isFullScreen = true;

        Activity activity = SystemUtils.getActivity(getContext());
        if (activity != null) {
            actionBarVisible = SystemUtils.isActionBarVisible((AppCompatActivity) activity);

            mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();

            activity.setRequestedOrientation(orientation);
            SystemUtils.hideSystemUI(getContext());
         /*   SystemUtils.hideSupportActionBar((AppCompatActivity) activity, true);
            SystemUtils.addFullScreenFlag((AppCompatActivity) activity);
            SystemUtils.hideNavKey((AppCompatActivity) activity);*/
        }

        changeToFullScreen();

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(true);
        }
    }

    private void postRunnableToResizeTexture() {
        post(new Runnable() {
            @Override
            public void run() {
                resizeRenderView(getVideoWidth(), getVideoHeight());
            }
        });
    }

    private int getVideoWidth() {
        BasePlayer player = getPlayer();
        return player == null ? 0 : player.getVideoWidth();
    }

    private int getVideoHeight() {
        BasePlayer player = getPlayer();
        return player == null ? 0 : player.getVideoHeight();
    }

    //根据视频内容重新调整视频渲染区域大小
    private void resizeRenderView(int width, int height) {
        IRenderView renderView = renderContainerFrame.getIRenderView();
        if (renderView == null || renderView.getRenderView() == null || height == 0 || width == 0) {
            return;
        }

        float aspectRation = playerConfig.aspectRatio == 0 ? (float) height / width : playerConfig.aspectRatio;

        int parentWidth = getWidth();
        int parentHeight = getHeight();

        float parentAspectRation = (float) parentHeight / parentWidth;

        int w, h;

        if (aspectRation < parentAspectRation) {
            w = parentWidth;
            h = (int) (w * aspectRation);
        } else {
            h = parentHeight;
            w = (int) (h / aspectRation);
        }
        renderView.setVideoSize(w, h);
    }

    /**
     * 通过获取到Activity的ID_ANDROID_CONTENT根布局，来添加视频控件，并全屏
     */
    protected void changeToFullScreen() {
        isFullScreen = true;
        originWidth = getWidth();
        originHeight = getHeight();

        viewParent = getParent();
        viewLayoutParams = getLayoutParams();

        if (viewParent != null) {
            ((ViewGroup) viewParent).indexOfChild(this);
            ((ViewGroup) viewParent).removeView(this);
        }
        //----
        ViewGroup vp = getRootViewGroup();

        LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getContext());

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);
        fullScreenToggle(true);
    }

    /**
     * 获取到Activity的ID_ANDROID_CONTENT根布局
     *
     * @return
     */
    protected ViewGroup getRootViewGroup() {
        AppCompatActivity activity = (AppCompatActivity) NyFileUtil.getActivity(getContext());
        if (activity != null) {
            return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        }
        return null;
    }

    protected void removePlayerFromParent() {
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    public void exitFullscreen() {
        exitFullscreenWithOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void exitFullscreenWithOrientation(int orientation) {

        isFullScreen = false;

        Activity activity = SystemUtils.getActivity(getContext());

        activity.setRequestedOrientation(orientation);

        SystemUtils.showSupportActionBar(activity);
        SystemUtils.clearFullScreenFlag(activity);

        activity.getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);

        changeToNormalScreen();

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(false);
        }
    }

    /**
     * 对应上面的全屏模式，来恢复到全屏之前的样式
     */
    protected void changeToNormalScreen() {
        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originWidth, originHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this, positionInParent, viewLayoutParams);
        }
        //TODO
        fullScreenToggle(false);
    }

    abstract protected void fullScreenToggle(boolean full);

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    /**
     * 视频全屏策略，竖向全屏，横向全屏，还是根据宽高比来智能选择
     */
    public int getFullScreenOrientation() {
        if (playerConfig.getFullScreenMode() == PORTRAIT_FULLSCREEN_MODE) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        BasePlayer player = getPlayer();
        if (playerConfig.getFullScreenMode() == AUTO_FULLSCREEN_MODE && player != null) {
            return player.getAspectRation() < 1 ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        }
        return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    }

    //-------------------------------add June-6-2021----------------
    protected float mSpeed = 1.0f;

    public void setSpeed(float speed) {
        mSpeed = speed;
        if (getPlayer() != null) getPlayer().setSpeed(mSpeed);
        //TODO
        setMute(mSpeed != 1.0f);
    }

    protected boolean isMute = false;

    public void setMute(boolean yesOrno) {
        isMute = yesOrno;
        if (getPlayer() != null) getPlayer().setMute(isMute);
    }


    public void setVideoScale(BaseConstants.ScaleType scale) {
        getRenderContainerFrame().getIRenderView().setDisplayScale(scale);
        if (getPlayer().getOnScaleChangeListener()!=null)
            getPlayer().getOnScaleChangeListener().onScaleChanged(scale);
    }

}
