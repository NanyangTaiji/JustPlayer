package com.nytaiji.core.view;

import static com.nytaiji.core.base.BaseConstants.ScaleType.SCALE_32_9;
import static com.nytaiji.nybase.model.Constants.REQUEST_DRAWOVERLAYS_CODE;
import static com.nytaiji.nybase.model.Constants.VIDEO_POSITION;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenHeight;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenWidth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.core.R;
import com.nytaiji.core.base.IFloatView;
import com.nytaiji.core.base.RenderContainerFrame;
import com.nytaiji.core.listener.VideoGifSaveListener;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.SystemUtils;

import java.io.File;


/**
 * An extension of StandardVideoView such that
 * 1. set autoFull mode
 */

public class AdvancedVideoFrame extends IntermediateVideoFrame {
    public static final String TAG = "AdvancedVideoFrame";


    public AdvancedVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public AdvancedVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvancedVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.advanced_video_frame;
    }

    //---------------------------
    private IFloatView.FloatViewListener floatlistener = null;
    private ImageView share, downloadBtn;

    // private TextView positionD;
    protected int listSize = 1;
    protected int vIndex = 0;
    protected boolean autoFull = false;
    private FloatVideoFrame floatVideoView;
    private boolean newPip = false;

    @Override
    protected void initView(Context context) {
        super.initView(context);

        //-----------------

        ImageView toPiP = findViewById(R.id.bt_pip);
        toPiP.setOnClickListener(this);
        ImageView shot = findViewById(R.id.bt_shot);
        shot.setOnClickListener(this);
        ImageView cast = findViewById(R.id.bt_cast);
        cast.setOnClickListener(this);
        share = findViewById(R.id.bt_share);
        share.setOnClickListener(this);
        downloadBtn = findViewById(R.id.bt_download);
        downloadBtn.setOnClickListener(this);
        // setUpZoomBehavior();
        ImageView toRotate = findViewById(R.id.bt_rotate);
        toRotate.setOnClickListener(this);

    }

  /*  private void goRewind() {
        seekTo(Math.max(getCurrentPosition() - fastMove, 0));
    }

    private void goForward() {
        seekTo(Math.min(getCurrentPosition() + fastMove, getDuration()));
    }


    private DisplayPortion getMyDisplayPortion(MotionEvent e) {
        int distance = getWidth();
        if (e.getX() < distance / 3.0) return DisplayPortion.LEFT;
        else if (e.getX() > distance * 2.0 / 3.0) return DisplayPortion.RIGHT;
        else return DisplayPortion.MIDDLE;
    }*/


    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v.getId() == R.id.bt_shot) {
            getPlayer().pause();
            saveFrameBitmap(true);
            //\ updateVideoInfo(getContext(), videoUrl);
        } else if (v.getId() == R.id.bt_pip) {
            if (!Settings.canDrawOverlays(getContext())) {
                // androidManefest must have <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + NyFileUtil.getActivity(getContext()).getPackageName()));
                NyFileUtil.getActivity(getContext()).startActivityForResult(intent, REQUEST_DRAWOVERLAYS_CODE);
            }
            gotoPiP();
            afterPiP();
        } else if (v.getId() == R.id.bt_share) {
            shareHandle();
        } else if (v.getId() == R.id.bt_cast) {
            castHandle();
        } else if (v.getId() == R.id.bt_download) {
            proceedDownload();
        } else if (v.getId() == R.id.bt_rotate) {
            rotateHandle();
        }
    }

    private int mRotate = 0;

    protected void rotateHandle() {
        //TODO
        if (playerConfig.getRenderType() == 0) {
            mRotate += 90;
            if (mRotate == 360) mRotate = 0;
            renderContainerFrame.setRotation(mRotate);
        }
    }


    protected void SBSHandle() {
        setVideoScale(SCALE_32_9);
        if (onPlayActionListener != null)
            onPlayActionListener.onScaleChanged(mScale);
        zoom.setScale(2f, 0, SystemUtils.getScreenHeight(getContext()) / 2);
        zoom.setEnabled(false);
        fullScreenHandle();
    }

    public void setNewPip() {
        newPip = true;
    }

    public void gotoPiP() {
        if (!Settings.canDrawOverlays(getContext())) {
            // Toast.makeText(context, "Please get overlay permission first and request again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + ((Activity) getContext()).getPackageName()));
            ((Activity) getContext()).startActivityForResult(intent, REQUEST_DRAWOVERLAYS_CODE);
            handleBack();
            return;
        }

        int screenWidth = getScreenWidth(getContext());
        int screenHeight = getScreenHeight(getContext());
        int videoWidth = getPlayer().getVideoWidth();
        int videoHeight = getPlayer().getVideoHeight();

        //注意使用application的context构建，防止内存泄露
        floatVideoView = new FloatVideoFrame(getContext());

        //determine the size of floatVideoView and Position
        int floatHeight, floatWidth;
        if (videoWidth > videoHeight) {
            floatWidth = isFullScreen() ? (int) screenWidth / 3 : (int) screenWidth / 2;
            floatHeight = (int) floatWidth * videoHeight / videoWidth;
        } else {
            floatHeight = (int) screenHeight / 3;
            floatWidth = (int) floatHeight * videoWidth / videoHeight;
        }

        IFloatView.LayoutParams layoutParams = new IFloatView.LayoutParams(floatWidth, floatHeight);
        SharedPreferences playerPrefs = getContext().getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);
        layoutParams.x = playerPrefs.getInt("PLAYER_X", screenWidth - floatWidth - 5);
        layoutParams.y = playerPrefs.getInt("PLAYER_Y", screenHeight - floatHeight - 5);
        floatVideoView.setFloatLayoutParams(layoutParams);
        //------------------------
        if (floatlistener != null) floatVideoView.setFloatViewListener(floatlistener);
        FloatVideoManager.getInstance().startFloatVideo(floatVideoView, callBackIntent());
        RenderContainerFrame floatContainerFrame = null;
        if (newPip) {
            //   start a new floatview completely, multiple floatviews
            floatContainerFrame = new RenderContainerFrame(getContext().getApplicationContext());
            floatVideoView.addRenderContainer(floatContainerFrame);
            floatVideoView.setPlayerConfig(playerConfig);
            if (multiUris != null) floatContainerFrame.setMultiUris(multiUris);
            else if (videoUrl != null) floatVideoView.setVideoUrl(videoUrl);
            else if (videoUri != null) floatVideoView.setVideoUri(videoUri);
            floatVideoView.start();
        } else {
            // Default 将正在播放的VideoView的RenderContainer层从原来界面剥离出来，添加到自定义的悬浮窗VideoView上
            floatContainerFrame = getRenderContainerViewOffParent();
            floatVideoView.addRenderContainer(floatContainerFrame);
            floatVideoView.setPlayerStatus(getCurrentState());
        }

    }

    //TODO 2022-5-1
    public void afterPiP() {
        NyFileUtil.getActivity(getContext()).finish();
    }

    //TODO 2022-5-1
    public void setFloatViewListener(IFloatView.FloatViewListener floatlistener) {
        this.floatlistener = floatlistener;
    }

    private Intent callBackIntent() {
        if (videoList == null) return null;
        //构建跳回来的intent, 为了防止内存泄漏，注意使用application的context，加上flags
        Intent intent = new Intent(getContext().getApplicationContext(), NyFileUtil.getActivity(getContext()).getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //   intent.putParcelableArrayListExtra(VIDEO_LIST, (ArrayList<? extends Parcelable>) videoList);
        //   intent.putExtra(VIDEO_INDEX, vIndex);
        intent.putExtra(VIDEO_POSITION, getCurrentPosition());
        //  Log.e(TAG,"vposition  "+getCurrentPosition());
        return intent;
    }

    private void shareHandle() {
        if (NyFileUtil.isOnline(getVideoUrl())) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("data", 0);
          /*  String ip = sharedPreferences.getString("ip", null);
            if (ip != null) {
                share.setVisibility(INVISIBLE);
                push(ip, getVideoUrl(), null);
            }*/
            NyFileUtil.copy_shareFilePath(getContext(), getVideoUrl());
        } else NyFileUtil.shareMedia(getContext(), getVideoUrl(), "video/*");

    }

    //--------------------------------------------


    @Override
    public void start() {
        super.start();
        if (isDownloadable()) downloadBtn.setVisibility(View.VISIBLE);
        else downloadBtn.setVisibility(View.GONE);
    }

    //TODO important for start with different modes
    //
    @Override
    public void changeUIWithPrepared() {
        super.changeUIWithPrepared();
        if (autoFull) startFullscreen();
        if (autoPiP) gotoPiP();

    }


    @Override
    public void changeUiWithBufferingStart() {
        changeUIWithPrepared();
    }

    public static final int START_NORMAL = 0;
    public static final int START_FULL = 1;
    public static final int START_PIP = 2;

    public void start(int mode) {
        autoFull = mode == START_FULL;
        autoPiP = mode == START_PIP;
        start();
    }


    /*******************************竖屏全屏结束************************************/
    boolean gifOn = false;
    private GifCreateHelper mGifCreateHelper;

    protected void castHandle() {
        gifOn = !gifOn;
        if (gifOn) {
            setSaveTitle();
            initGifHelper();
            startGif();
        } else stopGif();
    }

    private void initGifHelper() {
        mGifCreateHelper = new GifCreateHelper(getRenderContainerFrame().getIRenderView(), new VideoGifSaveListener() {
            @Override
            public void result(boolean success, File file) {

            }

            @Override
            public void process(int curPosition, int total) {
                // Debuger.printfError(" current " + curPosition + " total " + total);
            }
        });
    }


    /**
     * 开始gif截图
     */
    void startGif() {
        //开始缓存各个帧
        mGifCreateHelper.startGif(new File(NyFileUtil.getImageDir()));
        Toast.makeText(getContext(), "Gif starts", Toast.LENGTH_SHORT).show();

    }

    /**
     * 生成gif
     */
    void stopGif() {
        Toast.makeText(getContext(), "Gif stops", Toast.LENGTH_SHORT).show();
        mGifCreateHelper.stopGif(new File(NyFileUtil.getImageDir(), videoTitle + System.currentTimeMillis() + ".gif"));
    }

}
