package com.nytaiji.core.view;

import static com.nytaiji.nybase.model.Constants.VIDEO_POSITION;
import static com.nytaiji.nybase.utils.NyFileUtil.formatSaveFile;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenHeight;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenWidth;
import static com.nytaiji.nybase.utils.VideoFetchUtils.updateNyVideo;
import static com.nytaiji.nybase.utils.VideoProperty.shootSound;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.core.R;
import com.nytaiji.core.base.IFloatView;
import com.nytaiji.core.base.RenderContainerFrame;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.VideoFetchUtils;
import com.nytaiji.epf.VideoShotSaveListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 * An extension of StandardVideoView such that
 */

public class IntermediateVideoFrame extends StandardVideoFrame {
    public static final String TAG = "IntermediateVideoFrame";

    //  private LinearLayout bottomBasic;

    public ArrayList<NyVideo> videoList;


    public IntermediateVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public IntermediateVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntermediateVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    @Override
    protected @IdRes
    int getSurfaceContainerId() {
        return R.id.surface_container;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.inter_video_frame;
    }


    public void setFullScreenMode(int screenMode) {
        playerConfig.setFullScreenMode(screenMode);
    }

    //---------------------------


    //  private ImageView encryptToggle;
    private ImageView muteToggle, speedToggle;
    //  private ImageView share, downloadBtn;
    private ImageView next, prev;
    private ViewGroup speedLayout;
    private TextView textSpeed;
    private TextView positionD;
    protected int listSize = 1;
    protected int vIndex = 0;

    protected boolean autoFull = false;
    protected boolean autoPiP = false;
    //   private int mRotate = 0;
    private boolean speedSeen = false;

    private static final long fastMove = 15000L;


    protected void initView(Context context) {
        super.initView(context);

        //-----------------
        // bottomBasic = findViewById(R.id.bottom_basic_layout);
        muteToggle = findViewById(R.id.bt_mute);
        muteToggle.setOnClickListener(this);
        speedToggle = findViewById(R.id.bt_speed);
        speedToggle.setOnClickListener(this);
        speedLayout = findViewById(R.id.speed_layout);
        textSpeed = findViewById(R.id.speed_txt);
        ImageView speedPlus = findViewById(R.id.speed_plus);
        speedPlus.setOnClickListener(this);
        ImageView speedMinus = findViewById(R.id.speed_minus);
        speedMinus.setOnClickListener(this);
        //    positionD = findViewById(R.id.position_divider);
        next = findViewById(R.id.exo_next);
        next.setOnClickListener(this);
        prev = findViewById(R.id.exo_prev);
        prev.setOnClickListener(this);
        ImageView scaleToggle = findViewById(R.id.bt_scale);
        scaleToggle.setOnClickListener(this);
     /*   ImageView rotateToggle = findViewById(R.id.bt_rotate);
        rotateToggle.setOnClickListener(this);
        muteToggle = findViewById(R.id.bt_mute);
        muteToggle.setOnClickListener(this);
        ImageView toPiP = findViewById(R.id.bt_pip);
        toPiP.setOnClickListener(this);
        ImageView shot = findViewById(R.id.bt_shot);
        shot.setOnClickListener(this);
        ImageView cast = findViewById(R.id.bt_cast);
        cast.setOnClickListener(this);
        share = findViewById(R.id.bt_share);
        share.setOnClickListener(this);
        downloadBtn = findViewById(R.id.bt_download);
        downloadBtn.setOnClickListener(this);*/
        // setUpZoomBehavior();
    }


    private void goRewind() {
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
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.exo_next) {
            playNext();
        } else if (v.getId() == R.id.exo_prev) {
            playPrev();
        } else if (v.getId() == R.id.bt_speed) {
            toSpeedToggle();
        } else if (v.getId() == R.id.speed_plus) {
            speedUp();
        } else if (v.getId() == R.id.speed_minus) {
            speedDown();
        } else if (v.getId() == R.id.bt_mute) {
            isMute = !isMute;
            toMuteToggle(isMute);
        }
    }

    private void toSpeedToggle() {
        speedSeen = !speedSeen;
        if (speedSeen) {
            speedLayout.setVisibility(VISIBLE);
        } else {
            speedLayout.setVisibility(GONE);
            speedNormal();
        }
    }

    private void speedUp() {
        mSpeed = 2 * mSpeed;
        textSpeed.setText("速度：" + mSpeed);
        toMuteToggle(mSpeed!=1f);
        setSpeed(mSpeed);
    }

    private void speedNormal() {
        mSpeed = 1f;
        textSpeed.setText("速度：" + mSpeed);
        toMuteToggle(false);
        setSpeed(mSpeed);
    }

    private void speedDown() {
        mSpeed = mSpeed / 2;
        textSpeed.setText("速度：" + mSpeed);
        toMuteToggle(mSpeed!=1f);
        setSpeed(mSpeed);
    }

    protected void toMuteToggle(boolean mute) {
        setMute(mute);
        muteToggle.setImageResource(mute ? com.nytaiji.nybase.R.drawable.ic_volume_up : com.nytaiji.nybase.R.drawable.ic_volume_off);
        if (onPlayActionListener != null) {
            onPlayActionListener.onSpeedChanged(mSpeed);
            onPlayActionListener.onMuteChanged();
        }
    }

    protected void muteSystemToggle(boolean mute) {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        if (mute) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            // To set full volume
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
        }

    }

    protected void playPrev() {
        vIndex--;
        next.setVisibility(VISIBLE);
        if (vIndex == 0) prev.setVisibility(INVISIBLE);
        playIndex(vIndex);
        if (onPlayActionListener != null) onPlayActionListener.onIndexChanged(vIndex);
    }

    protected void playNext() {
        vIndex++;
        prev.setVisibility(VISIBLE);
        if (vIndex == listSize - 1) next.setVisibility(INVISIBLE);
        playIndex(vIndex);
        if (onPlayActionListener != null) onPlayActionListener.onIndexChanged(vIndex);
    }

    //TODO
  /*  @Override
    public void replay(){
      playIndex(vIndex);
    }*/

    public void playIndex(int vIndex) {
        this.vIndex = vIndex;
        videoUrl = videoList.get(vIndex).path;
        setVideoUrl(videoUrl);
        getPlayer().play();
        if (onPlayActionListener != null) onPlayActionListener.onIndexChanged(vIndex);

    }

    public void setVideoList(ArrayList<NyVideo> vList) {
        videoList = vList;
        // if (onVideoReplicationListener != null) onVideoReplicationListener.onListChanged(videoList);
        listSize = vList.size();
        Log.e(TAG, "attachlst " + listSize);
        if (listSize >= 1) {
            prev.setVisibility(vIndex == 0 ? INVISIBLE : VISIBLE);
            next.setVisibility(vIndex == listSize - 1 ? INVISIBLE : VISIBLE);
        }
    }

    public void setVideoIndex(int index) {   //starting from 1
        vIndex = index;
        prev.setVisibility(vIndex == 0 ? INVISIBLE : VISIBLE);
        next.setVisibility(vIndex == listSize - 1 ? INVISIBLE : VISIBLE);
        if (onPlayActionListener != null) onPlayActionListener.onIndexChanged(vIndex);
    }

    protected String fileName = null;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    protected String passWord = null;

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getPassWord() {
        return passWord;
    }

    public int getListSize() {
        return listSize;
    }

    public int getVideoIndex() {
        return vIndex;
    }

    public void gotoPiP() {
        int screenWidth = getScreenWidth(getContext());
        int screenHeight = getScreenHeight(getContext());
        int videoWidth = getPlayer().getVideoWidth();
        int videoHeight = getPlayer().getVideoHeight();

        //注意使用application的context构建，防止内存泄露
        FloatVideoFrame floatVideoView = new FloatVideoFrame(getContext().getApplicationContext());

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

        //
        // method 1 将正在播放的VideoView的RenderContainer层从原来界面剥离出来，添加到自定义的悬浮窗VideoView上
        RenderContainerFrame renderContainerFrame = getRenderContainerViewOffParent();
        floatVideoView.addRenderContainer(renderContainerFrame);
        floatVideoView.setPlayerStatus(getCurrentState());

        //  method 2 start a new floatview completely
        // RenderContainerFrame renderContainerFrame = new RenderContainerFrame(getContext().getApplicationContext());
        // floatVideoView.addRenderContainer(renderContainerFrame);
       /* floatVideoView.setPlayerConfig(playerConfig);
        floatVideoView.setVideoUrlPath(videoUrlPath);
        floatVideoView.start();*/
        //------------------------

        FloatVideoManager.getInstance().startFloatVideo(floatVideoView, callBackIntent());
        NyFileUtil.getActivity(getContext()).finish();
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


    public TextureView getTextureView() {
        return (TextureView) renderContainerFrame.getIRenderView();
    }


    public String getVideoUrl() {
        return videoUrl;
    }

    //----------------------------------------------------------//
    //--------------------------------------------------------------
    public static final int START_NORMAL = 0;
    public static final int START_FULL = 1;
    public static final int START_PIP = 2;

    public void start(int mode) {
        autoFull = mode == START_FULL;
        autoPiP = mode == START_PIP;
        start();
    }

    @Override
    public void start() {
        super.start();
    }


    //TODO important for start with different modes
    //
    @Override
    protected void changeUIWithPrepared() {
        super.changeUIWithPrepared();
        if (autoFull) startFullscreen();
        if (autoPiP) gotoPiP();

    }

    @Override
    public void changeUIWithPlaying() {
        super.changeUIWithPlaying();
    }

    @Override
    protected void changeUiWithBufferingStart() {
        changeUIWithPrepared();
        if (autoFull) startFullscreen();
        if (autoPiP) gotoPiP();
        // setViewsVisible(View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

  /*  @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isFullScreen() && getCurrentState() != BasePlayer.STATE_COMPLETED) {
            return false;
        }
        return super.onTouch(v, event);
    }*/

    @Override
    public void setTitle(String titleText) {
        //TO enable marque effect for short title
        for (int i = 0; i < 5; i++) {
            titleText = titleText + "  ";
        }
        super.setTitle(titleText);
    }

    //-------------------------------------------------------//
    public void setUseController(boolean showing) {
        if (showing) {
            setVideoUnlockedIcon();
            setSupportVolume(true);
            setSupportBrightness(true);
            //  showUI(topLayout);
        } else {
            setVideoLockedIcon();
            setSupportVolume(false);
            setSupportBrightness(false);
            // public void setViewsVisible(int topLayoutVisi, int centerLayoutVisi, int bottomLayoutVisi, int failedLayoutVisi, int loadingVisi, int thumbVisi, int replayLayoutVisi) {
            setViewsVisible(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);
        }
    }


    public void updateVideoInfo(Context context, String url) {
        //  //to avoid append hidden video, the above line should not be used;
        VideoFetchUtils videoFetchUtils = new VideoFetchUtils(context);
        NyVideo video = VideoFetchUtils.queryNyVideoByPath(context, url);
        if (video != null) {
            //   if (video.getWidth() * video.getHeight() * video.getDuration() == 0) {
            video.info.size = new File(video.getPath()).length();
            video.info.width = getPlayer().getVideoWidth();
            video.info.height = getPlayer().getVideoHeight();
            //TODO
            video.info.duration = (int) getPlayer().getDuration();
            video.setName(NyFileUtil.getFileNameWithoutExtFromPath(url));
            video.setPath(url);
            updateNyVideo(context, video);
        }
    }

    //---------------------------------------------------------//
    public void saveFrameBitmap(boolean high) {
        setSaveTitle();
        File saved = formatSaveFile(NyFileUtil.getImageDir(), videoTitle, "jpg");
        Log.e(TAG, "saved  " + saved.getAbsolutePath());
        getRenderContainerFrame().getIRenderView().saveFrame(saved,
                high,
                new VideoShotSaveListener() {
                    @Override
                    public void result(boolean success, File file) {
                        ((AppCompatActivity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (success && file.length() > 0) {
                                    shootSound(getContext());
                                    Toast.makeText(getContext(), "Image shot:" + saved.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(getContext(), "Image shot failed!", Toast.LENGTH_SHORT).show();
                                getPlayer().play();
                            }
                        });
                    }
                });
    }

    public static String getDateString(long dateTimeMills) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HH", Locale.ENGLISH);
        return formatter.format(dateTimeMills);
    }

    public void setSaveTitle() {
        if (videoTitle == null || videoTitle.trim().isEmpty())
            videoTitle = NyFileUtil.getFileNameWithoutExtFromPath(getVideoUrl());
        if (videoTitle.contains("_NY")) videoTitle = videoTitle.replace("_NY", "_");
        if (videoTitle.trim().equals("") || videoTitle.trim().isEmpty())
            videoTitle = getDateString(System.currentTimeMillis());
    }


}
