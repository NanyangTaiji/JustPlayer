package com.nytaiji.core.view;


import static com.nytaiji.core.base.BaseConstants.AUTO_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.RENDER_SURFACE_VIEW;
import static com.nytaiji.core.base.BaseConstants.RENDER_TEXTURE_VIEW;
import static com.nytaiji.nybase.model.Constants.KEY_PLAYER;
import static com.nytaiji.nybase.model.Constants.MAIN_SETTINGS;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenHeight;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenWidth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.core.R;
import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.base.IFloatView;
import com.nytaiji.core.base.PlayerConfig;
import com.nytaiji.core.listener.OnPlayActionListener;
import com.nytaiji.core.listener.OnStateChangedListener;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.nybase.crypt.EncryptUtil;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.zoom.ZoomNrotateLinear;
import com.nytaiji.zoom.collegeView.MultiTouchListener;

import java.util.ArrayList;


public class DualVideosFrame extends FrameLayout /*implements OnlineLinkUtil.onlineCallback^*/ {
    public static final String TAG = "DualVideoFrame";
    private final Context context;

    public ArrayList<NyVideo> videoList;
    private int vIndex = 0;
    private String mUrl = "";
    private final String mTitle = "";
    private long vposition = 0L;
    protected int renderType = 1;
    private AdvancedVideoFrame fullVideo;
    private PureVideoFrame floatVideo;
    private AppCompatActivity activityCompat;
    private BasePlayer fullPlayer, floatPlayer;
    private boolean floatVideoSet = false;
    private String passWord = null;
    private String fileName = null;
    private float widthToheightRatio = -1f;

    private Uri uri=null;


    protected int getLayoutId() {
        return R.layout.advanced_dual_video;
    }

    public DualVideosFrame(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public DualVideosFrame(@NonNull Context context, BasePlayer fullPlayer, BasePlayer floatPlayer) {
        super(context);
        this.context = context;
        this.fullPlayer = fullPlayer;
        this.floatPlayer = floatPlayer;
        activityCompat = NyFileUtil.getActivity(context);
        initView();
    }

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        fullVideo.setOnStateChangedListener(onStateChangedListener);
    }

    public void setFullPayer(BasePlayer fullPlayer) {
        this.fullPlayer = fullPlayer;
    }

    public void setFloatPayer(BasePlayer floatPlayer) {
        this.floatPlayer = floatPlayer;
    }

    public void setRenderType(int rendertype) {
        this.renderType = rendertype;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void start() {
        fullVideo.start();
    }

    public void pause() {
        fullVideo.pause();
    }

    public void replay() {
        fullVideo.replay();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        LayoutInflater.from(context).inflate(getLayoutId(), this);

        fullVideo = new AdvancedVideoFrame(activityCompat) {
            @Override
            public void gotoPiP() {
                //either
                enterSystemPIP();
                //or
                // floatVideo.setVisibility(View.GONE);
                // super.gotoPiP();
            }
        };

        floatVideo = findViewById(R.id.floatvideo);

        fullVideo.zoom.setmZoomStatusListener(new ZoomNrotateLinear.ZoomStatusListener() {
            @Override
            public void zoomStatus(boolean yesOrNo) {
                // Log.e(TAG, "zoomStatus:----"+yesOrNo);
                if (!floatVideoSet) {
                    setPipVideo();
                    floatVideoSet = !floatVideoSet;
                }
                floatVideo.setVisibility(View.GONE);
                if (yesOrNo) floatVideo.setVisibility(View.VISIBLE);
                fullVideo.setUseController(!yesOrNo);
            }
        });

        fullVideo.setOnPlayActionListener(new OnPlayActionListener() {
            @Override
            public void start() {
                if (floatVideo != null) floatVideo.start();
            }

            @Override
            public void pause() {
                if (floatVideo != null) floatVideo.pause();
            }

            @Override
            public void replay() {
                if (floatVideo != null) floatVideo.replay();
            }

            @Override
            public void release() {
                if (floatVideo != null) floatVideo.release();
            }

            @Override
            public void onPositionChanged(long position) {
                if (floatVideo != null) floatVideo.seekTo(position);
                //   Log.e(TAG, "progress" + position);
            }

            @Override
            public void onSpeedChanged(float speed) {
                if (floatVideo != null) floatVideo.setSpeed(speed);
            }

            @Override
            public void onMuteChanged() {
                if (floatVideo != null) floatVideo.setMute(true);
            }

            @Override
            public void onScaleChanged(BaseConstants.ScaleType scaleType) {
                if (floatVideo != null) floatVideo.setVideoScale(scaleType);
            }

            @Override
            public void onIndexChanged(int Index) {
                //overlap with
                if (vIndex != Index) playVideo(Index);
            }
        });

        fullVideo.setSupportSensorRotate(true);
        fullVideo.setRotateWithSystem(true);

        MultiTouchListener zoomOut = new MultiTouchListener();
        zoomOut.setScaleFactor(0.5f, 5.0f);
        zoomOut.setRotation(false);
        floatVideo.setOnTouchListener(zoomOut);
        //------------------

        FrameLayout fgContainer = findViewById(R.id.video_view_container);
        fgContainer.addView(fullVideo, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    public void attachList(ArrayList<NyVideo> videoList) {
        this.videoList = videoList;
        fullVideo.setVideoList(videoList);
    }

    public void setVideoIndex(int vIndex) {
        this.vIndex = vIndex;
        fullVideo.setVideoIndex(vIndex);
    }


    public void setCurrentPosition(long position) {
        vposition = position;
        fullVideo.seekTo(vposition);
    }

    public void playMinVideo(int vIndex) {
        enterSystemPIP();
        playVideo(vIndex);
    }

    private NyVideo nyVideo;


    public void playVideoUri(Uri uri){
        setUri(uri);
        fullVideo.release();
        floatVideo.release();
        mUrl=NyFileUtil.getPath(context, uri);
        if (fileName == null)  fileName = NyFileUtil.getLastSegmentFromString(mUrl);
        passWord = EncryptUtil.getPasswordFromFileName(fileName);
        configPlayer();
        //-----------------------------------------------------//
        fullVideo.setVideoUri(uri);
        floatVideo.setVideoUri(uri);
        fullVideo.start();
        fullVideo.seekTo(vposition);//this will lead to floatVideo.seekTo(vposition) indirectly.
        //TODO tmpfix
        SharedPreferences playerPrefs = context.getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);

        int playerType = playerPrefs.getInt(KEY_PLAYER, 0);
        if (playerType < 2) floatVideo.setMute(true);
        //
        floatViewAutoOff();

    }

    public void playVideo(int vIndex) {
        //---------------------------
        this.vIndex = vIndex;
        fullVideo.release();
        floatVideo.release();

        fullVideo.setVideoIndex(vIndex);
        nyVideo = videoList.get(vIndex);
        mUrl = nyVideo.getPath();
        proceedPlay();
    }

    private void proceedPlay() {
        //TODO 2021-8-9
        passWord = nyVideo.getPassWord();
        fileName = nyVideo.getName();
        if (fileName == null) fileName = NyFileUtil.getFileNameWithoutExtFromPath(mUrl);

        configPlayer();
        //-----------------------------------------------------//
        fullVideo.setVideoUrl(mUrl);
        floatVideo.setVideoUrl(mUrl);
        fullVideo.start();
        fullVideo.seekTo(vposition);//this will lead to floatVideo.seekTo(vposition) indirectly.
        //TODO tmpfix
        SharedPreferences playerPrefs = context.getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);

        int playerType = playerPrefs.getInt(KEY_PLAYER, 0);
        if (playerType < 2) floatVideo.setMute(true);
        //
        floatViewAutoOff();
    }

    private void configPlayer(){
        PlayerConfig fullConfig = new PlayerConfig.Builder()
                .fullScreenMode(AUTO_FULLSCREEN_MODE)
                .renderType(renderType == 0 ? RENDER_TEXTURE_VIEW : RENDER_SURFACE_VIEW)
                .player(fullPlayer)
                .build();
        fullConfig.setPassword(passWord);
        fullConfig.setFileName(fileName);

        fullVideo.setPlayerConfig(fullConfig);
        fullVideo.setPassWord(passWord);
        fullVideo.setFileName(fileName);
        fullVideo.setTitle(fileName);

        PlayerConfig floatConfig = new PlayerConfig.Builder()
                .fullScreenMode(AUTO_FULLSCREEN_MODE)
                .renderType(renderType == 0 ? RENDER_TEXTURE_VIEW : RENDER_SURFACE_VIEW)
                .player(floatPlayer)
                .enableAudioManager(false)
                .build();
        floatConfig.setPassword(passWord);
        floatConfig.setFileName(fileName);

        floatVideo.setPlayerConfig(floatConfig);
    }

    //the following to be excuted by the program
    public void floatViewAutoOff() {
        floatVideo.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                floatVideo.setVisibility(View.GONE);
            }
        }, 5000);
    }

  /*@Override
  public void onlineCallback(Map<String, Object> params) {
    mUrl = params.get("filePath").toString();
    try {
      mUrl = URLDecoder.decode(mUrl, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (params.get("fileName") != null) {
      fileName = Objects.requireNonNull(params.get("fileName")).toString();
    } else fileName = simpleExtraction(mUrl);
    passWord = EncryptUtil.getPasswordFromFileName(fileName);
    nyVideo.setPath(mUrl);
    nyVideo.setName(fileName);
    nyVideo.setPassWord(passWord);
    nyVideo.httpRedirect = false;
    //   Log.e(TAG, "onlineCallback mUrl  " + mUrl);
    proceedPlay();
  }*/

    public void release() {
        floatVideo.release();
        fullVideo.release();
    }

    public void destroy() {
        release();
        floatVideo.destroy();
        fullVideo.destroy();
    }


    public void pipExtraHandle(boolean isPip) {
        vposition = fullVideo.getPlayer().getCurrentPosition();
        fullVideo.setUseController(!isPip);
        floatVideo.setEnabled(!isPip);
        if (isPip) floatVideo.setVisibility(View.GONE);
        else floatViewAutoOff();
    }

    private void findVideoRatio() {
        if (widthToheightRatio < 0f) {
            widthToheightRatio = (float) fullVideo.getPlayer().getVideoWidth() / fullVideo.getPlayer().getVideoHeight();
        }
    }

    private void setPipVideo() {
        int screenWidth = getScreenWidth(activityCompat);
        int screenHeight = getScreenHeight(activityCompat);
        findVideoRatio();

        //determine the size of floatVideoView and Position

        int floatHeight, floatWidth;
        if (widthToheightRatio > 1f) {
            floatWidth = (int) screenWidth / 3;
            floatHeight = (int) (floatWidth / widthToheightRatio);
        } else {
            floatHeight = (int) screenHeight / 3;
            floatWidth = (int) (floatHeight * widthToheightRatio);
        }

        //get float position
        SharedPreferences playerPrefs = getContext().getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);

        IFloatView.LayoutParams layoutParams = new IFloatView.LayoutParams(floatWidth, floatHeight);
        layoutParams.width = floatWidth;
        layoutParams.height = floatHeight;
        layoutParams.x = playerPrefs.getInt("PLAYER_X", screenWidth - layoutParams.width - 5);
        layoutParams.y = playerPrefs.getInt("PLAYER_Y", screenHeight - layoutParams.height - 5);
        floatVideo.setFloatLayoutParams(layoutParams);
    }

    public void enterSystemPIP() {
        //NOTE enterPIP should be override with    <!--  android:supportsPictureInPicture="true"-->
        //  pipExtraHandle(true);
    }

    public int getVideoWidth() {
        return fullVideo.getPlayer().getVideoWidth();
    }

    public int getVideoHeight() {
        return fullVideo.getPlayer().getVideoHeight();
    }
}
