package com.nytaiji.core.view;

import static com.nytaiji.nybase.model.Constants.REQUEST_DRAWOVERLAYS_CODE;
import static com.nytaiji.nybase.model.Constants.VIDEO_POSITION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.base.IFloatView;
import com.nytaiji.core.base.RenderContainerFrame;
import com.nytaiji.nybase.utils.SystemUtils;


/**
 * <p>
 * 悬浮视频管理
 */
public class FloatVideoManager {

    private IFloatView floatView;

    private WindowManager windowManager;

    private IFloatView.LayoutParams layoutParams;

    private static FloatVideoManager instance;
    private WindowManager.LayoutParams wmParams;

    //用于从悬浮小窗口模式，跳回正常的Activity界面的Intent
    private static Intent intent = null;
    private Context context;
    private SharedPreferences playerPrefs;

    public static FloatVideoManager getInstance() {
        if (instance == null) {
            instance = new FloatVideoManager();
        }
        return instance;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        FloatVideoManager.intent = intent;
    }

    public void startFloatVideo(IFloatView videoView, Intent newintent) {
        this.floatView = videoView;
        intent = newintent;
        layoutParams = videoView.getFloatLayoutParams();
        createFloatVideo();
    }


    private void createFloatVideo() { //entry point
        if (floatView == null) {
            return;
        }
        //------
        // videoView.setFloatViewListener(this);

        context = floatView.getPlayView().getContext();
        if (!Settings.canDrawOverlays(context)){
           // Toast.makeText(context, "Please get overlay permission first and request again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + ((Activity) context).getPackageName()));
            ((Activity) context).startActivityForResult(intent, REQUEST_DRAWOVERLAYS_CODE);
            return;
        }

        playerPrefs = context.getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);

        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();
        wmParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.TOP | Gravity.LEFT | Gravity.START;
        wmParams.width = layoutParams.width;
        wmParams.height = layoutParams.height;
        wmParams.x = layoutParams.x;
        wmParams.y = layoutParams.y;

        windowManager.addView(floatView.getPlayView(), wmParams);
        windowManager.updateViewLayout(floatView.getPlayView(), wmParams);
        //--------
    }


    public int getCurrentPlayState() {
        return floatView != null ? floatView.getPlayView().getCurrentState() : BasePlayer.STATE_IDLE;
    }

    public BasePlayer getPlayer() {
        return getRenderContainerViewOffParent().getPlayer();
    }

    //销毁当前小窗口的控制层,并从窗体移除掉
    public void destroyVideoView() {
        if (getPlayer() != null) getPlayer().release();
        if (floatView != null) {
            floatView.destroyPlayerController();
            windowManager.removeViewImmediate(floatView.getPlayView());
            floatView = null;
            windowManager = null;
            intent = null;
        }
    }

    //将当前小窗口播放器的画面层剥离出来
    public RenderContainerFrame getRenderContainerViewOffParent() {
        if (floatView == null) {
            return null;
        }
        return floatView.getRenderContainerViewOffParent();
    }


    //-----------------default FloatViewListener--------------------------//
    // if no FloatViewListener is specified the following actions will be taken

    public void setFloatViewListener(IFloatView.FloatViewListener listener) {
        if (floatView != null) floatView.setFloatViewListener(listener);
    }

    public void closeVideoView() {
        if (floatView != null) {
            windowManager.removeViewImmediate(floatView.getPlayView());
            floatView.getPlayView().release();
            floatView.getPlayView().destroy();
            floatView = null;
            windowManager = null;
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }


    public void backToNormalView() {
        if (intent != null) {
            //
            intent.putExtra(VIDEO_POSITION, floatView.getPlayView().getCurrentPosition());
            //
            context.startActivity(intent);
            // closeVideoView();
        } else if (floatView != null) {
            ((Activity) context).setRequestedOrientation(floatView.getPlayView().getFullScreenOrientation());
            wmParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            wmParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            wmParams.gravity = Gravity.CENTER;
            floatView.getRenderContainerFrame().getIRenderView().setVideoSize(wmParams.width, wmParams.height);
            windowManager.updateViewLayout(floatView.getPlayView(), wmParams);
        }
    }

    //region  移动小窗体

    private float xInScreen;
    private float yInScreen;

    private float xInView;
    private float yInView;


    // [popup] initial coordinates and distance between fingers
    private double initPointerDistance = -1;
    private float initFirstPointerX = -1;
    private float initFirstPointerY = -1;
    private float initSecPointerX = -1;
    private float initSecPointerY = -1;

    private boolean isResizing = false;

    public boolean onTouch(MotionEvent event) {

        if (floatView == null || floatView.getPlayView() == null) {
            return false;
        }
        if (event.getPointerCount() == 2 && !isResizing) {
            //record coordinates of fingers
            initFirstPointerX = event.getX(0);
            initFirstPointerY = event.getY(0);
            initSecPointerX = event.getX(1);
            initSecPointerY = event.getY(1);
            //record distance between fingers
            initPointerDistance = Math.hypot(initFirstPointerX - initSecPointerX, initFirstPointerY - initSecPointerY);
            isResizing = true;
        }

        xInScreen = event.getRawX();
        //TODO for portait mode only
        yInScreen = event.getRawY();
        //- SystemUtils.getStatusBarHeight(videoView.getPlayView().getContext());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // updateViewPosition();
                handleMultiDrag(event);
                break;
            case MotionEvent.ACTION_UP:
                if (isResizing) {
                    isResizing = false;
                    initPointerDistance = -1;
                    initFirstPointerX = -1;
                    initFirstPointerY = -1;
                    initSecPointerX = -1;
                    initSecPointerY = -1;
                }
                playerPrefs.edit().putInt("PLAYER_X", wmParams.x).apply();
                playerPrefs.edit().putInt("PLAYER_Y", wmParams.y).apply();
                // playerPrefs.edit().putInt("PLAYER_W", wmParams.width).apply();
                // playerPrefs.edit().putInt("PLAYER_H", wmParams.height).apply();
                break;
        }
        return false;
    }

    private void updateViewPosition() {
        wmParams.x = (int) (xInScreen - xInView);
        wmParams.y = (int) (yInScreen - yInView);
        //TODO the following is needed for floatview that is not getofftheparent when its size is changed
        //
        floatView.getRenderContainerFrame().getIRenderView().setVideoSize(wmParams.width, wmParams.height);
        //
        windowManager.updateViewLayout(floatView.getPlayView(), wmParams);
    }

    private void handleMultiDrag(final MotionEvent event) {
        if (isResizing && initPointerDistance != -1 && event.getPointerCount() == 2) {
            // get the movements of the fingers
            final double firstPointerMove = Math.hypot(event.getX(0) - initFirstPointerX,
                    event.getY(0) - initFirstPointerY);
            final double secPointerMove = Math.hypot(event.getX(1) - initSecPointerX,
                    event.getY(1) - initSecPointerY);

            // minimum threshold beyond which pinch gesture will work
            //TODO
            final int minimumMove = 100;

            if (Math.max(firstPointerMove, secPointerMove) > minimumMove) {
                // calculate current distance between the pointers
                final double currentPointerDistance =
                        Math.hypot(event.getX(0) - event.getX(1),
                                event.getY(0) - event.getY(1));

                final double popupWidth = floatView.getPlayView().getWidth();
                final double popupHeight = floatView.getPlayView().getHeight();
                // change co-ordinates of popup so the center stays at the same position
                final double newWidth = (popupWidth * currentPointerDistance / initPointerDistance);
                final double newHeight = (popupHeight * currentPointerDistance / initPointerDistance);
                initPointerDistance = currentPointerDistance;
                xInView += (popupWidth - newWidth) / 2;
                yInView += (popupHeight - newHeight) / 2;
                wmParams.width = (int) Math.min(SystemUtils.getScreenWidth(context), newWidth);
                wmParams.height = (int) Math.min(SystemUtils.getScreenHeight(context), newHeight);
                //
            }
        }
        updateViewPosition();
    }

    public void setFullScreen() {
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = SystemUtils.getScreenWidth(context);
        wmParams.height = SystemUtils.getScreenHeight(context);
        //TODO the following is needed for floatview that is not getofftheparent when its size is changed
        //
        floatView.getRenderContainerFrame().getIRenderView().setVideoSize(wmParams.width, wmParams.height);
        //
        windowManager.updateViewLayout(floatView.getPlayView(), wmParams);
    }
}
