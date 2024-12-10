package com.nytaiji.exoplayer.exoview;



import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.base.IMediaPlayer;
import com.nytaiji.core.base.PlayerConfig;
import com.nytaiji.core.listener.OnStateChangedListener;
import com.nytaiji.nybase.utils.VideoProperty;
import com.nytaiji.core.view.PureVideoFrame;
import com.nytaiji.exoplayer.GoogleExoPlayer;


public class PureVideoView extends PureVideoFrame {
    public PlayerConfig playerConfig;
    private final Context mContext;
    private boolean toReroute=true;
    private boolean firstTime = true;
    private String passWord=null;

    public PureVideoView(@NonNull Context context) {
        this(context, null);
    }

    public PureVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PureVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
       firstTime = true;
    }

    @Override
    public void setVideoUrl(String url) {
        super.setVideoUrl(url);
        setVideoViewConfig(mContext);
        setTitle(VideoProperty.simpleExtraction(url));
        playOrPause();
    }



    public void setOnlineReroute(boolean toReroute) {
        this.toReroute = toReroute;
    }

    private void setVideoViewConfig(Context context) {

        playerConfig = new PlayerConfig.Builder()
                .fullScreenMode(BaseConstants.AUTO_FULLSCREEN_MODE)
                .renderType(BaseConstants.RENDER_GLSURFACE_VIEW)
                .looping(false)
                .player(setPlayer(context))
                .build();
        if (passWord != null) playerConfig.setPassword(passWord);
        //no effects
        //  setSupportSensorRotate(true);
        //  setRotateWithSystem(true);
        setPlayerConfig(playerConfig);
        setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void onStateChange(int state) {
                // Log.e(TAG, fileName + " State " + state);
                if (state == IMediaPlayer.STATE_PLAYING && firstTime) {
                    //stop the playing when loaded in html
                    // Log.e(TAG, "State " + state);
                    pause();
                    firstTime = false;
                }
                if (state == IMediaPlayer.STATE_ERROR) {
                  /*  if (NyFileUtil.isOnline(videoUrl) && toReroute) {
                        NyVideo nyVideo = new NyVideo();
                        nyVideo.setPath(videoUrl);
                        OnlineLinkUtil onlineLinkUtil = new OnlineLinkUtil();
                        onlineLinkUtil.init(mContext, "guest");
                        onlineLinkUtil.onlinePlayEnquiry(nyVideo, EXTRACT_ONLY, new OnlineLinkUtil.onlineCallback() {
                            @Override
                            public void onlineCallback(Map<String, Object> params) {
                                String fileName=null;
                                videoUrl = params.get("filePath").toString();
                                try {
                                    videoUrl = URLDecoder.decode(videoUrl, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                if (params.get("fileName") != null && !TextUtils.isEmpty(params.get("fileName").toString())) {
                                    //  Log.e(TAG, "fileName "+fileName);
                                    fileName = Objects.requireNonNull(params.get("fileName")).toString();
                                } else fileName = simpleExtraction(videoUrl);

                                renderContainerFrame.setVideoUrl(videoUrl);
                                passWord = EncryptUtil.getPasswordFromFileName(fileName);
                                setTitle(fileName);
                                setVideoViewConfig(mContext);
                                // playOrPause.setVisibility(VISIBLE);
                                start();
                            }
                        });

                    }*/
                }
            }
        });
    }

    //TODO tobe overrided by inherrited class
    public BasePlayer setPlayer(Context context) {
        //TODO 2021-11-29
        if (getPlayer() != null) release();
        //
        return new GoogleExoPlayer(context);
    }
}
