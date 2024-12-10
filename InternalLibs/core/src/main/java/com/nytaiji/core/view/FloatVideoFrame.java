package com.nytaiji.core.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.core.R;
import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.base.BaseVideoFrame;
import com.nytaiji.core.base.IFloatView;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.nybase.utils.SystemUtils;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhouteng on 2019-09-13
 * <p>
 * 小窗口视频UI界面
 */
public class FloatVideoFrame extends BaseVideoFrame implements IFloatView, View.OnClickListener, View.OnTouchListener {

    private IFloatView.LayoutParams videoLayoutParams;
    private FloatViewListener floatViewListener;
    private ImageView playBtn;
    private View replayLayout;
    private View topLayout, bottomLayout;

    protected Timer controlViewTimer;
    protected ControlViewTimerTask controlViewTimerTask;

    private ArrayList<NyVideo> videoList;


    public FloatVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public FloatVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.float_video_layout;
    }

    private void initView(Context context) {

        videoLayoutParams = new IFloatView.LayoutParams();

        //默认位置和大小
        videoLayoutParams.x = 0;
        videoLayoutParams.y = 0;
        videoLayoutParams.width = SystemUtils.dp2px(context, 200);
        videoLayoutParams.height = SystemUtils.dp2px(context, 112);

        View closeView = findViewById(R.id.close);
        closeView.setOnClickListener(this);

        View fullscreenView = findViewById(R.id.bt_fullscreen);
        fullscreenView.setOnClickListener(this);

        surfaceContainer.setOnClickListener(this);
        surfaceContainer.setOnTouchListener(this);

        topLayout = findViewById(R.id.top_layout);
        bottomLayout = findViewById(R.id.bottom_layout);

        playBtn = findViewById(R.id.bt_play_pause);
        playBtn.setOnClickListener(this);

        replayLayout = findViewById(R.id.reply_layout);
        replayLayout.setOnClickListener(this);
    }

    //禁止重力感应旋转
    @Override
    public boolean supportSensorRotate() {
        return false;
    }

    @Override
    protected int getSurfaceContainerId() {
        return R.id.surface_container;
    }


    @Override
    public boolean onBackKeyPressed() {
        return false;
    }

    @Override
    public void setFloatLayoutParams(IFloatView.LayoutParams layoutParams) {
        if (layoutParams != null) {
            videoLayoutParams = layoutParams;
        }
    }

    @Override
    public IFloatView.LayoutParams getFloatLayoutParams() {
        return videoLayoutParams;
    }

    @Override
    public void setFloatViewListener(FloatViewListener listener) {
        this.floatViewListener = listener;
        Log.e("FloatVideoFrame", "--------------setFloatViewListene");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (floatViewListener != null) {
            return floatViewListener.onTouch(event);
        }
        return FloatVideoManager.getInstance().onTouch(event);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.close) {
            if (floatViewListener != null) {
                floatViewListener.closeVideoView();
            } else FloatVideoManager.getInstance().closeVideoView();

        } else if (viewId == R.id.bt_fullscreen) {
            fullScreenToggle(true);
            if (floatViewListener != null) {
                floatViewListener.backToNormalView();
                //for other actions like change display icon
            } else FloatVideoManager.getInstance().backToNormalView();

        } else if (viewId == R.id.bt_play_pause) {
            start();
        } else if (viewId == R.id.reply_layout) {
            replay();
        } else if (viewId == getSurfaceContainerId()) {
            toggleControlView();
            startControlViewTimer();
        }
    }


    protected void toggleControlView() {
        int visi = topLayout.getVisibility() == VISIBLE ? GONE : VISIBLE;
        topLayout.setVisibility(visi);
        bottomLayout.setVisibility(visi);
        playBtn.setVisibility(visi);
    }

    protected void startControlViewTimer() {
        cancelControlViewTimer();
        controlViewTimer = new Timer();
        controlViewTimerTask = new ControlViewTimerTask(this);
        controlViewTimer.schedule(controlViewTimerTask, 2500);
    }

    protected void cancelControlViewTimer() {
        if (controlViewTimer != null) {
            controlViewTimer.cancel();
        }
        if (controlViewTimerTask != null) {
            controlViewTimerTask.cancel();
        }
    }

    protected static class ControlViewTimerTask extends TimerTask {

        private final WeakReference<FloatVideoFrame> weakReference;

        private ControlViewTimerTask(FloatVideoFrame videoView) {
            weakReference = new WeakReference<>(videoView);
        }

        @Override
        public void run() {
            FloatVideoFrame videoView = weakReference.get();
            if (videoView != null) {
                videoView.post(new ControlViewRunnable(videoView));
            }
        }
    }

    private static class ControlViewRunnable implements Runnable {

        private final WeakReference<FloatVideoFrame> weakReference;

        private ControlViewRunnable(FloatVideoFrame videoView) {
            weakReference = new WeakReference<>(videoView);
        }

        @Override
        public void run() {
            FloatVideoFrame videoView = weakReference.get();
            if (videoView != null) {
                videoView.hideControlView();
            }
        }
    }

    private void hideControlView() {
        topLayout.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        playBtn.setVisibility(View.GONE);
    }

    @Override
    public void onStateChange(int state) {
        super.onStateChange(state);
        if (state == BasePlayer.STATE_COMPLETED) {
            replayLayout.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.GONE);
            topLayout.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.GONE);
        } else {
            replayLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void setVideoScale(BaseConstants.ScaleType scale) {
        getRenderContainerFrame().getIRenderView().setDisplayScale(scale);
    }

    @Override
    protected void setPlayingIcon() {
        playBtn.setImageResource(com.nytaiji.nybase.R.drawable.ic_pause);
    }

    @Override
    protected void setPausedIcon() {
        playBtn.setImageResource(com.nytaiji.nybase.R.drawable.ic_play);
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void destroy() {
        super.destroy();
        destroyPlayerController();
    }

    @Override
    public void fullScreenToggle(boolean full) {
       /* if (getPlayer().getVideoWidth() > getPlayer().getVideoHeight()){
            setVideoScale(ScaleType.SCALE_MATCH_PARENT);
            renderContainerFrame.setRotation(90);
        }*/
    }

    @Override
    public void destroyPlayerController() {
        cancelControlViewTimer();
    }
}
