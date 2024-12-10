package com.nytaiji.core.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.core.R;
import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BaseVideoFrame;
import com.nytaiji.core.base.IFloatView;


/**
 * 单纯的视频播放控件
 */
/* An extension of original StandardVideoView such that

 */

public class PureVideoFrame extends BaseVideoFrame implements IFloatView {

    private IFloatView.LayoutParams videoLayoutParams;


    public PureVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public PureVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PureVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {

       // surfaceContainer.setOnClickListener(this);
      // surfaceContainer.setOnTouchListener(this);
    }

    @Override
    protected @IdRes
    int getSurfaceContainerId() {
        return R.id.surface_container;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.pure_video_layout;
    }

    @Override
    public boolean onBackKeyPressed() {
        return false;
    }

    @Override
    public void setTitle(String title) {

    }

    //region 根据状态更新UI

    @Override
    public void onStateChange(int state) {
    }


    @Override
    public void setPlayingIcon() {

    }

    @Override
    public void setPausedIcon(){
    }


    @Override
    public void startFullscreenWithOrientation(int orientation) {
        super.startFullscreenWithOrientation(orientation);
    }

    @Override
    public void exitFullscreenWithOrientation(int orientation) {
        super.exitFullscreenWithOrientation(orientation);
    }

    @Override
    public void setVideoScale(BaseConstants.ScaleType scale) {

    }


    @Override
    protected void fullScreenToggle(boolean full) {

    }

    @Override
    public void release() {
        super.release();
      //  seekBar.setProgress(0);
    }

    @Override
    public void destroy() {
        super.destroy();
    }


    @Override
    public void handleMobileData() {
    }

    @Override
    public void destroyPlayerController() {

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

    }
}
