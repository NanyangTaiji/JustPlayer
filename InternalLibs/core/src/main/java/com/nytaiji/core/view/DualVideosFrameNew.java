package com.nytaiji.core.view;

import static com.nytaiji.core.base.BaseConstants.AUTO_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.RENDER_SURFACE_VIEW;
import static com.nytaiji.core.base.BaseConstants.RENDER_TEXTURE_VIEW;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenHeight;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenWidth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
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
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.zoom.ZoomNrotateLinear;
import com.nytaiji.zoom.collegeView.MultiTouchListener;

import java.util.ArrayList;


public class DualVideosFrameNew extends FrameLayout /*implements OnlineLinkUtil.onlineCallback*/ {
    public static final String TAG = "DualVideoFrame";
    private final Context context;

    public ArrayList<NyVideo> videoList;
    private int vIndex = 0;
    private String fullPath = null;
    private String floatPath = null;

    private final String mTitle = "";
    private long vposition = 0L;
    protected int renderType = 1;
    private AdvancedVideoFrame fullVideo;
    private PureVideoFrame floatVideo;
    private AppCompatActivity activityCompat;
    private BasePlayer fullPlayer, floatPlayer;
    private boolean floatVideoSet = false;

    private boolean autoOff = false;

    public void setAutoOff(boolean autoOff) {
        this.autoOff = autoOff;
    }


    protected int getLayoutId() {
        return R.layout.advanced_dual_video;
    }

    public DualVideosFrameNew(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public DualVideosFrameNew(@NonNull Context context, BasePlayer fullPlayer, BasePlayer floatPlayer) {
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
                if (floatVideo != null && delayOn == 0) floatVideo.start();
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

    public void playVideo(int vIndex) {
        //---------------------------
        this.vIndex = vIndex;
        fullVideo.release();
        floatVideo.release();

        fullVideo.setVideoIndex(vIndex);
        nyVideo = videoList.get(vIndex);

        fullPath = nyVideo.getPath();
        Log.e(TAG, "fullPath   " + fullPath);

        floatPath = nyVideo.getPassWord();
        //  Log.e(TAG, "floatPath   " + floatPath);
        if (floatPath == null) floatPath = fullPath;


   /* if (NyFileUtil.isOnline(mUrl) && !mUrl.contains("smb") && nyVideo.httpRedirect) {
      OnlineLinkUtil onlineLinkUtil = new OnlineLinkUtil();
      onlineLinkUtil.init(context, "guest");
      onlineLinkUtil.onlinePlayEnquiry(nyVideo, EXTRACT_ONLY, (OnlineLinkUtil.onlineCallback) this);
    } else */
        proceedPlay();
    }

    private void proceedPlay() {
        //TODO 2021-8-9
        String passWord = nyVideo.getPassWord();
        String fileName = nyVideo.getName();
        if (fileName == null) fileName = NyFileUtil.getFileNameWithoutExtFromPath(fullPath);
        PlayerConfig fullConfig = new PlayerConfig.Builder()
                .fullScreenMode(AUTO_FULLSCREEN_MODE)
                .renderType(renderType == 0 ? RENDER_TEXTURE_VIEW : RENDER_SURFACE_VIEW)
                .player(fullPlayer)
                .build();
        fullConfig.setPassword(passWord);
        fullConfig.setFileName(fileName);
        fullVideo.setPlayerConfig(fullConfig);

        PlayerConfig floatConfig = new PlayerConfig.Builder()
                .fullScreenMode(AUTO_FULLSCREEN_MODE)
                .renderType(renderType == 0 ? RENDER_TEXTURE_VIEW : RENDER_SURFACE_VIEW)
                .player(floatPlayer)
                .enableAudioManager(false)
                .build();
        floatConfig.setPassword(passWord);
        floatConfig.setFileName(fileName);
        floatVideo.setPlayerConfig(floatConfig);
        //-----------------------------------------------------//

        fullVideo.setPassWord(passWord);
        fullVideo.setFileName(fileName);
        fullVideo.setTitle(fileName);

        floatVideo.setVideoUrl(floatPath);
        floatVideo.setVisibility(View.VISIBLE);
        floatVideo.setMute(false);

        fullVideo.setVideoUrl(fullPath);

        //    fullVideo.seekTo(vposition);//this will lead to floatVideo.seekTo(vposition) indirectly.
        //TODO tmpfix
      //  SharedPreferences playerPrefs = context.getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);
      //  int playerType = playerPrefs.getInt(KEY_PLAYER, 0);
      //  if (playerType < 2)

        //


        delayOn = 0;
        if (fullPath.contains("http")) delayOn = 600;
        fullVideo.start();
        floatViewAutoOn(delayOn);
    }

    int delayOn = 0;

    private void floatViewAutoOn(int delay) {

        // fullVideo.pause();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                long current = fullVideo.getCurrentPosition();
                Log.e(TAG, "current----------------------" + current);
                floatVideo.start();

              //  if (current > 0L) floatVideo.seekTo(current);
                delayOn = 0;
            }
        }, delay);
    }

    private void floatViewAutoOff() {
        // floatVideo.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                floatVideo.setVisibility(View.GONE);
            }
        }, 5000);
    }

 /* @Override
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

    private void setPipVideo() {
        int screenWidth = getScreenWidth(activityCompat);
        int screenHeight = getScreenHeight(activityCompat);
        int videoWidth = fullVideo.getPlayer().getVideoWidth();
        int videoHeight = fullVideo.getPlayer().getVideoHeight();

        //determine the size of floatVideoView and Position

        int floatHeight, floatWidth;
        if (videoWidth > videoHeight) {
            floatWidth = (int) screenWidth / 3;
            floatHeight = (int) floatWidth * videoHeight / videoWidth;
        } else {
            floatHeight = (int) screenHeight / 3;
            floatWidth = (int) floatHeight * videoWidth / videoHeight;
        }

        SharedPreferences playerPrefs = getContext().getSharedPreferences("MAIN_SETTINGS", Context.MODE_PRIVATE);

        //  floatWidth=playerPrefs.getInt("PLAYER_W",floatWidth);
        //  floatHeight=playerPrefs.getInt("PLAYER_H",floatHeight);
        IFloatView.LayoutParams layoutParams = new IFloatView.LayoutParams(floatWidth, floatHeight);
        layoutParams.width = floatWidth;
        layoutParams.height = floatHeight;
        layoutParams.x = playerPrefs.getInt("PLAYER_X", screenWidth - layoutParams.width - 5);
        layoutParams.y = playerPrefs.getInt("PLAYER_Y", screenHeight - layoutParams.height - 5);

        //  layoutParams.x = screenWidth - floatWidth - 5;
        //  layoutParams.y = screenHeight - floatHeight - 5;
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
