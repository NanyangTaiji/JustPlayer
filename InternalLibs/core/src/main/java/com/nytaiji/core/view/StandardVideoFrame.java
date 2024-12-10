package com.nytaiji.core.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.nytaiji.core.R;
import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BaseVideoFrame;
import com.nytaiji.core.dialog.BrightnessDialog;
import com.nytaiji.core.dialog.SeekDialog;
import com.nytaiji.core.dialog.VolumeDialog;
import com.nytaiji.core.filter.extraFilters.GlBitmapOverlay;
//import com.nytaiji.core.player.AndroidPlayer;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.nybase.network.VideoPlayNdownload;
import com.nytaiji.nybase.utils.VideoProperty;
import com.nytaiji.epf.filter.GlFilter;
import com.nytaiji.zoom.ZoomNrotateLinear;

import static com.nytaiji.core.base.BaseConstants.ScaleType.SCALE_CENTER_CROP;
import static com.nytaiji.core.base.BaseConstants.ScaleType.SCALE_MATCH_PARENT;
import static com.nytaiji.core.base.IMediaPlayer.STATE_BUFFERING_END;
import static com.nytaiji.core.base.IMediaPlayer.STATE_BUFFERING_START;
import static com.nytaiji.core.base.IMediaPlayer.STATE_COMPLETED;
import static com.nytaiji.core.base.IMediaPlayer.STATE_ERROR;
import static com.nytaiji.core.base.IMediaPlayer.STATE_IDLE;
import static com.nytaiji.core.base.IMediaPlayer.STATE_PAUSED;
import static com.nytaiji.core.base.IMediaPlayer.STATE_PLAYING;
import static com.nytaiji.core.base.IMediaPlayer.STATE_PREPARED;
import static com.nytaiji.core.base.IMediaPlayer.STATE_PREPARING;

/**
 * 标准的视频播放控件
 */
/* An extension of original StandardVideoView such that
 * 2. seekbar,currentTimeText, totalTimeText disabled in live-mode;
 * 3. add setLive() method;
 * 4. add setEnforced(boolean) method to use guesture and click without isInPlaybackState()
 * 5. add setFullscreenmode()

 *
 */

public class StandardVideoFrame extends BaseVideoFrame implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
    //TODO

    public static String TAG = "StandardVideoFrame";

    public static int DEFAULT_CONTROLLER_DELAY = 3000;
    private boolean isEnforced = false;
    private boolean isZoomable = true;
    private boolean isTouchable = true;

    public void setGestureEnforced(boolean isEnforced) {
        this.isEnforced = isEnforced;
    }

    public void setZoomable(boolean isZoomable) {
        this.isZoomable = isZoomable;
        zoom.setEnabled(isZoomable);
    }

    public void setTouchable(boolean isTouchable) {
        this.isTouchable = isTouchable;
        this.setClickable(isTouchable);
        this.setFocusable(isTouchable);
        if (!isTouchable) setZoomable(false);
    }

    public boolean isZoomable() {
        return isZoomable;
    }

    public String videoTitle = null;
    public ZoomNrotateLinear zoom;
    protected ImageView thumbView;
    protected ViewGroup bottomLayout;
    protected ImageView bottomMore;
    protected ViewGroup bottomExtra;
    protected ViewGroup topLayout;
    protected ViewGroup centerLayout;
    protected TextView txtPosition;
    protected TextView txtDuration;
    protected SeekBar seekBar;
    protected ImageView fullScreen;
    protected ImageView buttonBack;
    protected TextView titleView;
    protected ProgressBar loadingProgressBar;
    protected ImageView playOrPause;
    protected ViewGroup failedLayout;
    protected ViewGroup replayLayout;

    protected ViewGroup lockStatusLayout;
    protected ImageView lockStatus;
    private PlayerTimer playerTimer;
    private boolean isShowMobileDataDialog = false;

    public StandardVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public StandardVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StandardVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public GlFilter filter;

    private long lastTouchTime = System.currentTimeMillis();

    protected void initView(Context context) {
        filter = new GlBitmapOverlay(context, BitmapFactory.decodeResource(this.getResources(), com.nytaiji.nybase.R.drawable.banner), GlBitmapOverlay.Position.RIGHT_BOTTOM);

        //TODO 2021-12-10 the above require context

        //  surfaceContainer.setFocusable(isEnforced);
        // surfaceContainer.setFocusableInTouchMode(isEnforced);
        zoom = findViewById(R.id.zoom);
        //---
        surfaceContainer.setOnClickListener(this);
        surfaceContainer.setOnTouchListener(this);

        thumbView = findViewById(R.id.thumb);
        bottomLayout = findViewById(R.id.bottom_layout);
        topLayout = findViewById(R.id.top_layout);
        centerLayout = findViewById(R.id.center_layout);

        txtPosition = findViewById(R.id.current);
        txtDuration = findViewById(R.id.total);

        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);

        fullScreen = findViewById(R.id.bt_fullscreen);
        fullScreen.setOnClickListener(this);

        //     bottomMore = findViewById(R.id.bt_bottom_more);
        //     bottomMore.setOnClickListener(this);

      /*  bottomExtra = findViewById(R.id.bottom_extra);
        bottomExtra.setVisibility(isFullScreen() ? VISIBLE : GONE);*/

        buttonBack = findViewById(R.id.bt_back);
        buttonBack.setOnClickListener(this);
        buttonBack.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isDownloadable()) {
                    proceedDownload();
                    onBackKeyPressed();
                }
                return true;
            }
        });

        titleView = findViewById(R.id.title);
        loadingProgressBar = findViewById(R.id.loading);

        failedLayout = findViewById(R.id.failed_layout);
        replayLayout = findViewById(R.id.reply_layout);
        replayLayout.setOnClickListener(this);

        playOrPause = findViewById(R.id.bt_play_pause);
        playOrPause.setOnClickListener(this);

        lockStatus = findViewById(R.id.lock_status);
        lockStatusLayout = findViewById(R.id.lock_status_layout);
        lockStatusLayout.setOnClickListener(this);
    }


    public void proceedDownload() {
        VideoPlayNdownload.backgroundDownload(getContext(), getVideoUrl());
    }


    @Override
    protected @IdRes
    int getSurfaceContainerId() {
        return R.id.surface_container;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.standard_video_frame;
    }

    //region 根据状态更新UI

    @Override
    public void onStateChange(int state) {
        super.onStateChange(state);
        switch (state) {
            case STATE_IDLE:
                changeUIWithIdle();
                break;
            case STATE_PREPARING:
                changeUIWithPreparing();
                break;
            case STATE_PREPARED:
                changeUIWithPrepared();
                if (maxVolume < 0) maxVolume = getPlayer().getPlayerAudioManager().getStreamMaxVolume();
                break;
            case STATE_PLAYING:
                if (maxVolume < 0) maxVolume = getPlayer().getPlayerAudioManager().getStreamMaxVolume();
                verticalDistance = getHeight();
                changeUIWithPlaying();
                break;
            case STATE_PAUSED:
                changeUIWithPause();
                break;
            case STATE_COMPLETED:
                changeUIWithComplete();
                break;
            case STATE_ERROR:
                changeUIWithError();
                break;
            case STATE_BUFFERING_START:
                changeUiWithBufferingStart();
                break;
            case STATE_BUFFERING_END:
                changeUiWithBufferingEnd();
                break;
        }
        //  updateProgressStatus();
    }

    //TODO
   /* public void updateProgressStatus() {
        if (isPlaying() && !isLive()) {
            startProgressTimer();
        } else {
            cancelProgressTimer();
        }
    }*/

    @Override
    public void setPlayingIcon() {
        playOrPause.setImageResource(com.nytaiji.nybase.R.drawable.exo_ic_pause_circle_filled);
    }

    @Override
    public void setPausedIcon() {
        playOrPause.setImageResource(com.nytaiji.nybase.R.drawable.exo_ic_play_circle_filled);
    }

    protected void resetProgressAndTime() {
        txtPosition.setText(VideoProperty.stringForTime(0));
        txtDuration.setText(VideoProperty.stringForTime(0));
    }


    protected void changeUiWithBufferingStart() {
        setViewsVisible(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
    }

    protected void changeUiWithBufferingEnd() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    public void changeUIWithPlaying() {
        // startProgressTimer();
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    public void changeUIWithPause() {
        updateLastTouch();
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    protected void changeUIWithError() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
    }

    public void changeUIWithComplete() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
        //TODO
        // if (!isLive()) {
        seekBar.setProgress(0);
        txtPosition.setText("00:00");
        //  }
    }

    protected void changeUIWithIdle() {
        setViewsVisible(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
    }

    public void changeUIWithPreparing() {
        resetProgressAndTime();
        setViewsVisible(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
    }

    //TODO
    protected void changeUIWithPrepared() {
        //TODO
        setVideoScale(mScale);
        setGlFilter(filter);
        //--
        int visible = View.VISIBLE;
        if (isLive()) visible = View.INVISIBLE;

        //TODO ny
        startProgressTimer();  //Live also need timer
        txtPosition.setVisibility(visible);
        txtDuration.setVisibility(visible);
        seekBar.setVisibility(visible);

        setViewsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
        //start controller timer
    }

    public void setViewsVisible(int topLayoutVisi, int centerLayoutVisi, int bottomLayoutVisi, int failedLayoutVisi, int loadingVisi, int thumbVisi, int replayLayoutVisi) {
        setTopVisi(topLayoutVisi);
        setCenterVisi(centerLayoutVisi);
        setBottomVisi(bottomLayoutVisi);

        failedLayout.setVisibility(failedLayoutVisi);
        loadingProgressBar.setVisibility(loadingVisi);
        thumbView.setVisibility(thumbVisi);
        replayLayout.setVisibility(replayLayoutVisi);
    }

   /* private void startControlViewTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideControlView();
            }
        }, 3000); // 5000ms delay
    }*/
    //endregion

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_play_pause) {
            playOrPause();
        } else if (v.getId() == R.id.reply_layout) {
            replay();
            if (onPlayActionListener != null) onPlayActionListener.replay();
        } else if (v.getId() == R.id.surface_container) {
            if (!delayShow) surfaceContainerClick();
        } else if (v.getId() == R.id.bt_fullscreen) {
            fullScreenHandle();
        } else if (v.getId() == R.id.bt_back) {
            handleBack();
        } else if (v.getId() == R.id.lock_status_layout) {
            toggleVideoLockStatus();
        } else if (v.getId() == R.id.bt_bottom_more) {
            bottomToggle();
        } else if (v.getId() == R.id.bt_scale) {
            resolveScaleUI();
        }
        updateLastTouch();
    }

    public void updateLastTouch() {
        lastTouchTime = System.currentTimeMillis();
    }

    private boolean bottomMoreShown = false;

    protected void bottomToggle() {
        bottomMoreShown = !bottomMoreShown;
        if (bottomMoreShown) bottomExtra.setVisibility(VISIBLE);
        else bottomExtra.setVisibility(GONE);
    }


    public boolean handleBack() {
        if (isFullScreen()) {
            //  bottomExtra.setVisibility(GONE);
            fullScreen.setImageResource(com.nytaiji.nybase.R.drawable.exo_ic_fullscreen_enter);
            exitFullscreen();
            return false;
        } else {
            release();
            destroy();
            buttonBack.setVisibility(GONE);
            SystemUtils.showNavigationBar(getContext());
            return true;
            //the following will run into endless deadlock
            // NyFileUtil.getActivity(getContext()).onBackPressed();
        }
    }

    public void fullScreenHandle() {
        if (isFullScreen()) exitFullscreen();
        else startFullscreen();
    }

    @Override
    protected void fullScreenToggle(boolean full) {
        seekBar.setVisibility(INVISIBLE);
        if (!full) {
            fullScreen.setImageResource(com.nytaiji.nybase.R.drawable.exo_ic_fullscreen_enter);
            //  bottomExtra.setVisibility(GONE);
            //   bottomMore.setVisibility(GONE);
        } else {
            fullScreen.setImageResource(com.nytaiji.nybase.R.drawable.exo_ic_fullscreen_exit);
            //  bottomMore.setVisibility(VISIBLE);
            if (!isLive()) seekBar.setVisibility(VISIBLE);
            //  bottomExtra.setVisibility(VISIBLE);
        }
    }


    @Override
    public boolean onBackKeyPressed() {
        if (isLocked) {
            return true;
        }
        if (isFullScreen()) {
            if (bottomExtra != null) bottomExtra.setVisibility(GONE);
            fullScreen.setImageResource(com.nytaiji.nybase.R.drawable.exo_ic_fullscreen_enter);
            exitFullscreen();
            return true;
        }

        return false;
    }


    @Override
    public void setTitle(String titleText) {
        videoTitle = titleText;
        titleView.setText(titleText);
    }

    @Override
    public void startFullscreenWithOrientation(int orientation) {
        super.startFullscreenWithOrientation(orientation);
        resetLockStatus();
    }

    @Override
    public void exitFullscreenWithOrientation(int orientation) {
        super.exitFullscreenWithOrientation(orientation);
        resetLockStatus();
    }


    @Override
    public void release() {
        super.release();
        seekBar.setProgress(0);
        if (brightnessDialog != null) brightnessDialog.dismiss();
        if (volumeDialog != null) volumeDialog.dismiss();
        if (seekDialog != null) seekDialog.dismiss();
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyPlayerController();
    }

    /**
     * 仅仅销毁播放器控制层逻辑，但是不去销毁RenderContainer层
     */
    public void destroyPlayerController() {
        cancelProgressTimer();
        orientationHelper.setOrientationEnable(false);
    }

    @Override
    public void handleMobileData() {
        if (isShowMobileDataDialog) {
            return;
        }
        isShowMobileDataDialog = true;
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.mobile_data_tips));
        builder.setPositiveButton(context.getString(R.string.continue_playing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                playOrPause();
            }
        });
        builder.setNegativeButton(context.getString(R.string.stop_play), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    boolean controlViewShown = false;

    public void toggleControlView() {
        controlViewShown = !controlViewShown;
        setVideoLockLayoutVisi(lockStatusLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        if (isLocked) {
            return;
        }
        int visi = controlViewShown ? View.VISIBLE : View.GONE;
        setMainVisi(visi);
        // if (controlViewShown) startControlViewTimer();
    }

    protected void setMainVisi(int visi) {
        setBottomVisi(visi);
        setCenterVisi(visi);
        setTopVisi(visi);
    }

    protected void setBottomVisi(int visi) {
        bottomLayout.setVisibility(isLocked ? View.GONE : visi);
    }

    protected void setCenterVisi(int visi) {
        centerLayout.setVisibility(isLocked ? View.GONE : visi);
    }

    protected void setTopVisi(int visi) {
        topLayout.setVisibility(isLocked ? View.GONE : visi);
    }

    //endregion

    //region top,bottom控制栏隐藏任务

    public void hideControlView() {
        if (isInPlaybackState() || !isEnforced) {
            setMainVisi(View.GONE);
            setVideoLockLayoutVisi(View.INVISIBLE);
            if (isFullScreen) SystemUtils.hideSystemUI(getContext());
        }
    }


    //TODO
    public void setFullScreenMode(int screenMode) {
        playerConfig.setFullScreenMode(screenMode);
    }


    protected boolean isLiveSetted;

    public void setLive(boolean isLiveSetted) {
        this.isLiveSetted = isLiveSetted;
        //Log.e("StandardVideoView", "setLive" + isLiveSetted);
        setSupportSeek(!isLiveSetted);
    }


    //是否是直播类视频
    protected boolean isLive() {
        return isLiveSetted || getDuration() < 0L;
    }

    protected boolean isDownloadable() {
        return isOnline() && !isLive();
    }

    protected boolean isOnline() {
        return NyFileUtil.isOnline(getVideoUrl());
    }

    public void startProgressTimer() {
        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {
                //  Log.e(TAG, "lastTouchTime = "+lastTouchTime);
                if (System.currentTimeMillis() - lastTouchTime > DEFAULT_CONTROLLER_DELAY)
                    hideControlView();
                if (!isLive())
                    setProgressAndText();
            }
        });
        playerTimer.start();
        updateLastTouch();
    }

    protected void cancelProgressTimer() {
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }

    public void setProgressAndText() {

        int position = (int) getCurrentPosition();
        int duration = (int) getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        //  Log.e(TAG, "setProgressAndText() " + position + "/" + duration);
        if (progress != 0 && !touchScreen) {
            seekBar.setProgress(progress);
        }
        txtPosition.setText(VideoProperty.stringForTime(position));
        txtDuration.setText(" / " + VideoProperty.stringForTime(duration));
    }
    //endregion

    //region 锁定屏幕

    protected boolean isSupportLock = true;

    protected boolean isLocked = false; //是否处于锁定屏幕状态

    protected boolean isSupportLock() {
        return isFullScreen() && isSupportLock;
    }

    //全屏情况下，是否支持锁定播放器
    public void setSupportLock(boolean supportLock) {
        isSupportLock = supportLock;
    }

    protected void toggleVideoLockStatus() {
        isLocked = !isLocked;
        if (isLocked) {
            setVideoLockedIcon();
        } else {
            setVideoUnlockedIcon();
        }
        orientationHelper.setOrientationEnable(!isLocked);
        setMainVisi(isLocked ? View.GONE : View.VISIBLE);
    }

    protected void setVideoLockedIcon() {
        lockStatus.setImageResource(com.nytaiji.nybase.R.drawable.ic_locked);
    }

    protected void setVideoUnlockedIcon() {
        lockStatus.setImageResource(com.nytaiji.nybase.R.drawable.ic_unlocked);
    }

    protected void setVideoLockLayoutVisi(int visi) {
        if (isSupportLock()) {
            lockStatusLayout.setVisibility(visi);
        } else {
            lockStatusLayout.setVisibility(View.INVISIBLE);
        }
    }

    protected void resetLockStatus() {
        isLocked = false;
        setVideoLockLayoutVisi(bottomLayout.getVisibility());
    }

    //endregion

    //region 音量，亮度，进度调整

    protected boolean isSupportVolume = true;   //默认支持手势调节音量
    protected boolean isSupportBrightness = true;  //默认支持手势调节亮度
    protected boolean isSupportSeek = true;   //默认支持手势调节进度

    protected VolumeDialog volumeDialog;
    protected BrightnessDialog brightnessDialog;
    protected SeekDialog seekDialog;

    protected boolean touchScreen;

    private float downX;
    private float downY;

    private int downVolume = -1;  //触摸屏幕时的当前音量
    private float downBrightness;  //触摸屏幕时的当前亮度

    private long downVideoPosition; //触摸屏幕时的当前播放进度
    private long newVideoPosition; //手势操作拖动后的新的进度

    private boolean isChangedProgress;  //是否手势操作拖动了进度条

    private boolean isSeekGesture = false; //是否触发了进度条拖拽的手势
    private boolean isVolumeGesture = false; //是否触发了音量调整的手势
    private boolean isBrightnessGesture = false; //是否触发了亮度调整的手势

    private int maxVolume = -1;

    private int verticalDistance;

    protected static final int MINI_GESTURE_DISTANCE = 60; // 手势的最小触发范围;

    //手动设置是否支持手势调节音量
    public void setSupportVolume(boolean supportVolume) {
        isSupportVolume = supportVolume;
    }

    //手动设置是否支持手势调节亮度
    public void setSupportBrightness(boolean supportBrightness) {
        isSupportBrightness = supportBrightness;
    }

    //手动设置是否支持手势拖动播放进度(不支持直播类视频流)
    public void setSupportSeek(boolean supportSeek) {
        isSupportSeek = supportSeek;
        if (!supportSeek || isLive()) {
            txtDuration.setVisibility(INVISIBLE);
            txtPosition.setVisibility(INVISIBLE);
            seekBar.setVisibility(INVISIBLE);
        }
    }

    //TODO
    //region 点击屏幕，显示隐藏控制栏
    protected void surfaceContainerClick() {
        if (!isTouchable || (zoom != null && zoom.getScale() > 1f)) return;
        if (isInPlaybackState() || isEnforced) {
            if (isLive()) setSupportSeek(false);
            if (!delayShow) toggleControlView();
        }
    }


    boolean delayShow = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //TODO
        //--To avoid two-fingers operation or in scale
        if (!isTouchable || (zoom != null && zoom.getScale() > 1f) || event.getPointerCount() > 1) {
            delayShow = true;
            hideControlView();
            return false;   //TODO zoom=null in case that the video is not inbeded in zoomliner
        }
        delayShow = false;
        if (isLive()) setSupportSeek(false);

        //--
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchScreen = true;

                downX = event.getX();
                downY = event.getY();
                if (downVolume < 0) downVolume = getPlayer().getPlayerAudioManager().getStreamVolume();
                downBrightness = SystemUtils.getScreenBrightness(getContext());
                downVideoPosition = getCurrentPosition();
                isChangedProgress = false;

                isSeekGesture = false;
                isVolumeGesture = false;
                isBrightnessGesture = false;

                gestureScrollY = 0;
                gestureScrollX = 0;

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                touchMove(dx, dy, event.getX());
                break;
            case MotionEvent.ACTION_UP:
                touchScreen = false;
                hideVolumeDialog();
                hideBrightnewssDialog();
                hideSeekDialog();
                seekToNewVideoPosition();
                setMainVisi(View.VISIBLE);
                break;
        }
        return false;
    }

    //音量，亮度，播放进度等手势判断
    protected void touchMove(float dx, float dy, float x) {

        if (isLocked) {
            // Log.e(TAG, "touch locked");
            return;
        }

        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        int screenWidth = getWidth();

        if (!isSeekGesture && !isVolumeGesture && !isBrightnessGesture) {

            //触发了播放进度拖拽手势
            if (absDx > absDy && absDx >= MINI_GESTURE_DISTANCE) {
                //TODO
                isSeekGesture = isInPlaybackState();
            }
            //触发了亮度调节手势
            else if (absDy > absDx && x <= screenWidth / 2 && absDy >= MINI_GESTURE_DISTANCE) {
                isBrightnessGesture = true;
            }
            //触发了音量调节手势
            else if (absDy > absDx && x > screenWidth / 2 && absDy >= MINI_GESTURE_DISTANCE) {
                isVolumeGesture = true;
            }
        }

        if (isSeekGesture && !isLive() && isSupportSeek) {
            changeProgress(dx);
            return;
        }

        if (isBrightnessGesture && isSupportBrightness) {
            changeBrightness(dy);
            return;
        }

        if (isVolumeGesture && isSupportVolume) {
            changeVolume(dy);
        }
    }

    //region 播放进度手势处理
    protected void seekToNewVideoPosition() {
        if (isChangedProgress) {
            seekTo(newVideoPosition);
            if (onPlayActionListener != null)
                onPlayActionListener.onPositionChanged(newVideoPosition);
            isChangedProgress = false;

            // startProgressTimer();
        }
    }

    protected void changeProgress(float dx) {
        cancelProgressTimer();

        int distance = getWidth();
        long videoDuration = getDuration();
        newVideoPosition = downVideoPosition + (int) (dx / distance * videoDuration);
        if (newVideoPosition >= videoDuration) {
            newVideoPosition = videoDuration;
        }
        //  String progressText = VideoProperty.stringForTime(newVideoPosition) + "/" + VideoProperty.stringForTime(videoDuration);
        //  int progress = (int) ((float) newVideoPosition / videoDuration * 100);
        showSeekDialog(dx, (long) newVideoPosition, (long) videoDuration);
        isChangedProgress = true;
    }

    protected void showSeekDialog(float deltaX, long seekTimePosition, long totalTimeDuration) {
        if (seekDialog == null) {
            seekDialog = new SeekDialog(getContext(), this);
        }
        setMainVisi(View.GONE);
        seekDialog.show(deltaX, seekTimePosition, totalTimeDuration);
        setProgressTextWithTouch((int) ((int) seekTimePosition / totalTimeDuration));
        isLocked = true;
    }

    protected void hideSeekDialog() {
        if (seekDialog != null) {
            seekDialog.dismiss();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isLocked = false;
            }
        }, 500); // 500ms delay
    }

    //用于SeekDialog的继承扩展
   /* protected SeekDialog newSeekDialogInstance() {
        return new SeekDialog(getContext(), R.style.volume_brightness_theme);
    }*/

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        downVideoPosition = getCurrentPosition();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //TODO
        if (fromUser && !isLive()) {
            long videoDuration = getDuration();
            newVideoPosition = progress * videoDuration / 100;
            //  String progressText = VideoProperty.stringForTime(newVideoPosition) + "/" + VideoProperty.stringForTime(videoDuration);
            //  showSeekDialog(progressText, progress);

        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (!isLive()) {
            seekTo(newVideoPosition);
            if (onPlayActionListener != null)
                onPlayActionListener.onPositionChanged(newVideoPosition);
        }
        hideSeekDialog();
        updateLastTouch();
        startProgressTimer();
        // startControlViewTimer();
    }

    protected void setProgressTextWithTouch(int progress) {
        txtPosition.setText(VideoProperty.stringForTime(newVideoPosition));
        seekBar.setProgress(progress);
    }

    //endregion

    //region 亮度手势操作处理
    protected void changeBrightness(float dy) {
        //屏幕亮度区间0.0 ~ 1.0
        //  Log.e(TAG, "changeBrightness = " + downBrightness + " dy/verticalDistance=" + dy / verticalDistance);
        float newBrightness = downBrightness - dy / verticalDistance;
        if (newBrightness < 0.0f) {
            newBrightness = 0.0f;
        }
        if (newBrightness > 1.0f) {
            newBrightness = 1.0f;
        }
        SystemUtils.setScreenBrightness(getContext(), newBrightness);
        showBrightnewssDialog((int) (newBrightness * 100));
    }

    protected void showBrightnewssDialog(int progress) {
        if (brightnessDialog == null) {
            brightnessDialog = new BrightnessDialog();
        }
        isLocked = true;
        setMainVisi(View.GONE);
        brightnessDialog.show(getContext(), progress, this);
    }

    protected void hideBrightnewssDialog() {
        if (brightnessDialog != null) {
            brightnessDialog.dismiss();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isLocked = false;
            }
        }, 500); // 500ms delay
    }

    private float gestureScrollY = 0f;
    private float gestureScrollX = 0f;
    protected void changeVolume(float dy) {
        gestureScrollY += dy;
        // Log.e(TAG, "downVolume = " + downVolume + " maxVolume = " + maxVolume);
        // Log.e(TAG, "changeVolume dy= " + dy + " /verticalDistance=" + verticalDistance);
        float newVolume = downVolume -  dy / verticalDistance * maxVolume; //dy is negative

        if (newVolume < 0f) {
            newVolume = 0f;
        }
        if (newVolume > maxVolume) {
            newVolume = maxVolume;
        }
        downVolume = (int) newVolume;
        getPlayer().getPlayerAudioManager().adjustVolume(gestureScrollY < 0);
      //  setVolume((int) newVolume);
      //  Log.e(TAG, "newVolume= " + newVolume + " /maxVolume=" + maxVolume);
        showVolumeDialog((int) (newVolume / maxVolume * 100));
    }

    protected void showVolumeDialog(int volumeProgress) {
        if (volumeDialog == null) {
            volumeDialog = new VolumeDialog();
        }
        isLocked = true;
        setMainVisi(View.GONE);
        volumeDialog.show(getContext(), volumeProgress, this);
    }

    protected void hideVolumeDialog() {
        if (volumeDialog != null) {
            volumeDialog.dismiss();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isLocked = false;
            }
        }, 500); // 500ms delay

    }

    //starting
    protected BaseConstants.ScaleType mScale = BaseConstants.ScaleType.SCALE_16_9;
    //Ijkplayer and Exoplayer have different default behaviors

    private void resolveScaleUI() {

        if (mScale == BaseConstants.ScaleType.SCALE_DEFAULT) {
            mScale = BaseConstants.ScaleType.SCALE_16_9;
            //  scaleToggle.setText("16:9");
        } else if (mScale == BaseConstants.ScaleType.SCALE_16_9) {
            mScale = BaseConstants.ScaleType.SCALE_4_3;
            //  scaleToggle.setText("4:3");
        } else if (mScale == BaseConstants.ScaleType.SCALE_4_3) {
            mScale = SCALE_MATCH_PARENT;
            //  scaleToggle.setText("Match");
        } else if (mScale == SCALE_MATCH_PARENT) {
            mScale = SCALE_CENTER_CROP;
            //  scaleToggle.setText("Crop");
        } else if (mScale == SCALE_CENTER_CROP) {
            mScale = BaseConstants.ScaleType.SCALE_ORIGINAL;
            // scaleToggle.setText("Origin");
        } else if (mScale == BaseConstants.ScaleType.SCALE_ORIGINAL) {
            mScale = BaseConstants.ScaleType.SCALE_DEFAULT;
            //  scaleToggle.setText("Default");
        }
        setVideoScale(mScale);
        if (onPlayActionListener != null)
            onPlayActionListener.onScaleChanged(mScale);
        //   Log.e(TAG, "mScale " + mScale);
    }


    public void setGlFilter(GlFilter filter) {
        getRenderContainerFrame().setGlFilter(filter);
        this.filter = filter;
    }
}
