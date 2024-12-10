/*
 * Created on 2023-3-9 9:18:59 pm.
 * Copyright © 2023 刘振林. All rights reserved.
 */

package com.nytaiji.nybase.view;

import static com.nytaiji.nybase.utils.SystemUtils.dp2px;
import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.nytaiji.nybase.R;


public class GestureWebView extends NyWebView implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private int playerWidth, playerHeight;

    private boolean gestureEnabled = true;

    public void enablseGesture(boolean trueOrnot) {
        gestureEnabled = trueOrnot;
    }

    public GestureWebView(Context context) {
        this(context, null);
    }

    public GestureWebView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        super.init();
        // ****************音量/进度/亮度*********************
        root = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.gesture_layout, null);
        gesture_volume_layout = (RelativeLayout) root.findViewById(R.id.gesture_volume_layout);
        gesture_bright_layout = (RelativeLayout) root.findViewById(R.id.gesture_bright_layout);
        gesture_progress_layout = (RelativeLayout) root.findViewById(R.id.gesture_progress_layout);
        geture_tv_progress_time = (TextView) root.findViewById(R.id.geture_tv_progress_time);
        geture_tv_volume_percentage = (TextView) root.findViewById(R.id.geture_tv_volume_percentage);
        geture_tv_bright_percentage = (TextView) root.findViewById(R.id.geture_tv_bright_percentage);
        gesture_iv_progress = (ImageView) root.findViewById(R.id.gesture_iv_progress);
        gesture_iv_player_volume = (ImageView) root.findViewById(R.id.gesture_iv_player_volume);
        gesture_iv_player_bright = (ImageView) root.findViewById(R.id.gesture_iv_player_bright);
        gestureDetector = new GestureDetector(mContext, this);
        setLongClickable(true);
        // 是否启动长按
        gestureDetector.setIsLongpressEnabled(true);
        setOnTouchListener(this);
        audiomanager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
        originalVolume = currentVolume;


        /** 获取视频播放窗口的尺寸 */
        ViewTreeObserver viewObserver = this.getViewTreeObserver();
        viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                GestureWebView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerWidth = root.getWidth();
                playerHeight = root.getHeight();
            }
        });


        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(root);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void destroy() {
        //reset volumne to the original
        audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        audiomanager.abandonAudioFocus(null);
        audiomanager = null;
        setOnTouchListener(null);
        gestureDetector.setIsLongpressEnabled(false);
        gestureDetector.setContextClickListener(null);
        gestureDetector.setOnDoubleTapListener(null);
        gestureDetector = null;
        super.destroy();
    }


    //-------------------------------------------------------//
    private RelativeLayout root;
    private RelativeLayout gesture_volume_layout, gesture_bright_layout;// 音量控制布局,亮度控制布局
    private TextView geture_tv_volume_percentage, geture_tv_bright_percentage;// 音量百分比,亮度百分比
    private ImageView gesture_iv_player_volume, gesture_iv_player_bright;// 音量图标,亮度图标
    private RelativeLayout gesture_progress_layout;// 进度图标
    private TextView geture_tv_progress_time;// 播放时间进度
    private ImageView gesture_iv_progress;// 快进或快退标志
    private GestureDetector gestureDetector;
    private AudioManager audiomanager;

    private int originalVolume;
    private int maxVolume, currentVolume;
    private float mBrightness = -1f; // 亮度
    private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快
    private boolean firstScroll = false;// 每次触摸屏幕后，第一次scroll的标志
    private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量,3.调节亮度
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_VOLUME = 2;
    private static final int GESTURE_MODIFY_BRIGHT = 3;


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 手势里除了singleTapUp，没有其他检测up的方法
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
            gesture_volume_layout.setVisibility(View.GONE);
            gesture_bright_layout.setVisibility(View.GONE);
            //  gesture_progress_layout.setVisibility(View.GONE);
        }
        return gestureDetector.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        if (e.getPointerCount() > 1) return false;
        firstScroll = gestureEnabled;// 设定是单指触摸屏幕后第一次scroll的标志
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float mOldX = e1.getX(),
                mOldY = e1.getY();
        int e2RawY = (int) e2.getRawY();
        if (firstScroll) {// 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
            // 横向的距离变化大则调整进度，纵向的变化大则调整音量
            if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                // gesture_progress_layout.setVisibility(View.VISIBLE);
                gesture_volume_layout.setVisibility(View.GONE);
                gesture_bright_layout.setVisibility(View.GONE);
                GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
            } else {
                if (mOldX > playerWidth * 3.0 / 5) {// 音量
                    gesture_volume_layout.setVisibility(View.VISIBLE);
                    gesture_bright_layout.setVisibility(View.GONE);
                    //  gesture_progress_layout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                } else if (mOldX < playerWidth * 2.0 / 5) {// 亮度
                    gesture_bright_layout.setVisibility(View.VISIBLE);
                    gesture_volume_layout.setVisibility(View.GONE);
                    //  gesture_progress_layout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;
                }
            }
        }
        // 如果每次触摸屏幕后第一次scroll是调节进度，那之后的scroll事件都处理音量进度，直到离开屏幕执行下一次操作
        if (GESTURE_FLAG == GESTURE_MODIFY_PROGRESS) {
            // distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进
           /* if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                if (distanceX >= dp2px(mContext, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调
                    gesture_iv_progress.setImageResource(R.drawable.ic_rewind);
                    if (playingTime > 3) {// 避免为负
                        playingTime -= 3;// scroll方法执行一次快退3秒
                    } else {
                        playingTime = 0;
                    }
                } else if (distanceX <= -DensityUtil.dip2px(this, STEP_PROGRESS)) {// 快进
                    gesture_iv_progress.setImageResource(R.drawable.souhu_player_forward);
                    if (playingTime < videoTotalTime - 16) {// 避免超过总时长
                        playingTime += 3;// scroll执行一次快进3秒
                    } else {
                        playingTime = videoTotalTime - 10;
                    }
                }
                if (playingTime < 0) {
                    playingTime = 0;
                }
                tv_pro_play.seekTo(playingTime);
                geture_tv_progress_time.setText(DateTools.getTimeStr(playingTime) + "/" + DateTools.getTimeStr(videoTotalTime));
            }*/
        }
        // 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
        else if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
            currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                if (distanceY >= dp2px(mContext, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                        currentVolume++;
                    }
                    gesture_iv_player_volume.setImageResource(R.drawable.ic_volume_up_24dp);
                } else if (distanceY <= -dp2px(mContext, STEP_VOLUME)) {// 音量调小
                    if (currentVolume > 0) {
                        currentVolume--;
                        if (currentVolume == 0) {// 静音，设定静音独有的图片
                            gesture_iv_player_volume.setImageResource(R.drawable.ic_volume_off_24dp);
                        }
                    }
                }
                int percentage = (currentVolume * 100) / maxVolume;
                geture_tv_volume_percentage.setText(percentage + "%");
                audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }
        }
        // 如果每次触摸屏幕后第一次scroll是调节亮度，那之后的scroll事件都处理亮度调节，直到离开屏幕执行下一次操作
        else if (GESTURE_FLAG == GESTURE_MODIFY_BRIGHT) {
            gesture_iv_player_bright.setImageResource(R.drawable.ic_brightness_medium_24);
            if (mBrightness < 0) {
                mBrightness = ((Activity) mContext).getWindow().getAttributes().screenBrightness;
                if (mBrightness <= 0.00f) {
                    mBrightness = 0.50f;
                }
                if (mBrightness < 0.01f) {
                    mBrightness = 0.01f;
                }
            }
            WindowManager.LayoutParams lpa = ((Activity) mContext).getWindow().getAttributes();
            lpa.screenBrightness = mBrightness + (mOldY - e2RawY) / playerHeight;
            if (lpa.screenBrightness > 1.0f) {
                lpa.screenBrightness = 1.0f;
            } else if (lpa.screenBrightness < 0.01f) {
                lpa.screenBrightness = 0.01f;
            }
            ((Activity) mContext).getWindow().setAttributes(lpa);
            String brightness = String.valueOf((int) (lpa.screenBrightness * 100)) + "%";
            geture_tv_bright_percentage.setText(brightness);
        }

        firstScroll = false;// 第一次scroll执行完成，修改标志
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    // e1：第1个ACTION_DOWN MotionEvent
    // e2：最后一个ACTION_MOVE MotionEvent
    // velocityX：X轴上的移动速度，像素/秒
    // velocityY：Y轴上的移动速度，像素/秒
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float mOldX = e1.getX();
        float mNewX = e2.getX();
        float mOldY = e1.getY();
        float mNewY = e2.getY();
        float distanceX = mNewX - mOldX;
        float distanceY = mNewY - mOldY;
        // 横向的距离变化大则调整进度，纵向的变化大则调整音量
        if (Math.abs(distanceX) >= Math.abs(distanceY)) {
            //向右快进
            if (mNewX - mOldX > 0) {
                // Toast.makeText(mContext, "快进 10s", Toast.LENGTH_SHORT).show();
            } else { //
                // Toast.makeText(mContext, "快退 10s", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    //TODO ny 2023-6-26 no effect
    @Override
    protected View getExtraView(){
        return root;
    }
}
