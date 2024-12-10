package com.rockcarry.fanplayer;


import static com.rockcarry.fanplayer.MediaPlayer.PARAM_PLAY_SPEED;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnMediaPlayerStateListener;
import com.nytaiji.nybase.utils.NyFileUtil;


import java.io.IOException;

public class FanPlayer extends BasePlayer {

    private static final String TAG = "nyFanPlayer";
    protected MediaPlayer mPlayer;
    protected Context mContext;
    protected OnMediaPlayerStateListener onMediaPlayerStateListener;
    private int videoWidth;
    private int videoHeight;
    private Surface mVideoSurface = null;
    private SurfaceView mSurfaceView = null;

    private int mVolume = 50;

    private boolean mIsPlaying = false;
    // private boolean mIsLive = false;

    public FanPlayer(Context context) {
        super(context);
        mContext = context;
    }


    public FanPlayer(Context context, OnMediaPlayerStateListener onMediaPlayerStateListener) {
        super(context);
        mContext = context;
        this.onMediaPlayerStateListener = onMediaPlayerStateListener;
    }


    @Override
    public void setSurface(Surface surface) {
        mPlayer.setDisplaySurface(surface);
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        //
    }

    @Override
    public void setTextureView(TextureView textureView) {
        videoHeight = textureView.getHeight();
        videoWidth = textureView.getWidth();
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (mPlayer != null) {
                    mPlayer.setDisplayTexture(surfaceTexture);
                    mPlayer.initVideoSize(videoWidth, videoHeight, textureView);
                }
            }
        });


    }

    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {

    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        videoHeight = mSurfaceView.getHeight();
        videoWidth = mSurfaceView.getWidth();
        mSurfaceView.getHolder().addCallback(
                new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                        mVideoSurface = holder.getSurface();
                        if (mVideoSurface != null && mPlayer != null) {
                            mPlayer.setDisplaySurface(mVideoSurface);
                            mPlayer.initVideoSize(width, height, mSurfaceView);
                        }
                    }

                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        mVideoSurface = holder.getSurface();
                        if (mPlayer != null) {
                            mPlayer.setDisplaySurface(mVideoSurface);
                            mPlayer.initVideoSize(videoWidth, videoHeight, mSurfaceView);
                        }
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        mVideoSurface = null;
                        if (mPlayer != null) {
                            mPlayer.setDisplaySurface(mVideoSurface);
                            //TODO ny
                            mPlayer.close();
                        }
                    }
                });

    }


    @Override
    public void setDualVolume(float leftx, float rightx) {

    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    protected void setDataSource() throws IOException {

    }

    @Override
    public void prepare() {
        if (videoUri != null) {
            videoUrl = NyFileUtil.getPath(mContext, videoUri);
            if (videoUrl.indexOf("/storage/") > 0)
                videoUrl = videoUrl.substring(videoUrl.indexOf("/storage/"));
        }

        // mIsLive = (videoUrl.startsWith("http://") && videoUrl.endsWith(".m3u8")) || videoUrl.startsWith("rtmp://")
        //     || videoUrl.startsWith("rtsp://") || videoUrl.startsWith("avkcp://") || videoUrl.startsWith("ffrdp://");

        mPlayer = new MediaPlayer(videoUrl, mHandler, MediaPlayer.PLAYER_INIT_PARAMS);
    }

    @Override
    protected boolean isPlayingImpl() {
        if (mPlayer == null) {
            return false;
        }
        return mIsPlaying;
    }

    @Override
    protected void onSeekTo(long position) {
        mPlayer.seek(position);
    }

    @Override
    public float getAspectRation() {
        return videoWidth / videoHeight;
    }

    @Override
    public int getVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getVideoHeight() {
        return videoHeight;
    }

    @Override
    public long getCurrentPosition() {
        return mPlayer.getParam(MediaPlayer.PARAM_MEDIA_POSITION);
    }

    @Override
    public long getDuration() {
        return mPlayer.getParam(MediaPlayer.PARAM_MEDIA_DURATION);
        //  return mPlayer != null ? mPlayer.getParam(MediaPlayer.PARAM_MEDIA_DURATION) : VideoProperty.extractDuration(videoUrl);
    }

    @Override
    public void setOptions() {

    }

    @Override
    protected void setEnableMediaCodec(boolean isEnable) {

    }

    @Override
    protected void setEnableOpenSLES(boolean isEnable) {

    }


    @Override
    public void setNewState(int newState) {

    }

    @Override
    public void setSpeed(float speed) {

    }

    @Override
    public float getPlaySpeed() {
        return mPlayer.getParam(PARAM_PLAY_SPEED);
    }


    @Override
    public int getVolume() {
       Log.e(TAG, "playerAudioManager volume "+playerAudioManager.getStreamMaxVolume());
      //  Log.e(TAG, "Player volume "+mPlayer.getParam(MediaPlayer.PARAM_AUDIO_VOLUME));
        return playerAudioManager != null ? playerAudioManager.getStreamVolume() : (int) mPlayer.getParam(MediaPlayer.PARAM_AUDIO_VOLUME);

    }

    @Override
    //设置音量
    public void setVolume(int value) {
        Log.e(TAG, "setVolume "+value);
        if (playerAudioManager != null) playerAudioManager.setStreamVolume(value);
    }

    @Override
    public void OpenVolume() {
        setVolume(mVolume);
    }

    @Override
    public void CloseVolume() {
        mVolume = getVolume();
        setVolume(0);
    }

    @Override
    public int getBufferProgress() {
        return 100;
    }

    @Override
    public void setRenderer(Object rendererItem) {

    }

    @Override
    public void setDisplaySize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        if (mPlayer != null && mPlayer.initVideoSize(videoWidth, videoHeight, mSurfaceView)) {
            mHandler.sendEmptyMessage(MSG_UDPATE_VIEW_SIZE);
        }
    }

    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int MSG_UDPATE_VIEW_SIZE = 2;
    private static final int MSG_HIDE_BUTTONS = 3;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS: {
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 200);
                    if (onMediaPlayerStateListener != null)
                        onMediaPlayerStateListener.onMediaReadyPlaying();

                }
                break;
                case MSG_HIDE_BUTTONS: {

                }
                break;
                case MSG_UDPATE_VIEW_SIZE: {
                    if (mPlayer != null && mPlayer.initVideoSize(videoWidth, videoHeight, mSurfaceView)) {
                        mSurfaceView.setVisibility(View.VISIBLE);
                    }
                }
                break;
                case MediaPlayer.MSG_OPEN_DONE: {
                    if (mPlayer != null) {
                        mPlayer.setDisplaySurface(mVideoSurface);
                        mHandler.sendEmptyMessage(MSG_UDPATE_VIEW_SIZE);
                        mPlayer.play();
                        mIsPlaying = true;
                        if (onMediaPlayerStateListener != null)
                            onMediaPlayerStateListener.onMediaLoadingComplete();
                    }
                }
                break;
                case MediaPlayer.MSG_OPEN_FAILED: {
                    String str = String.format(mContext.getString(R.string.open_video_failed), videoUrl);
                    Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
                }
                break;
                case MediaPlayer.MSG_PLAY_COMPLETED: {
                    //  if (!mIsLive) mPlayer.close();
                    mIsPlaying = false;
                    if (onMediaPlayerStateListener != null)
                        onMediaPlayerStateListener.onMediaAutoCompletion();
                }
                break;
                case MediaPlayer.MSG_VIDEO_RESIZED: {
                    mPlayer.initVideoSize(videoWidth, videoHeight, mSurfaceView);
                    mHandler.sendEmptyMessage(MSG_UDPATE_VIEW_SIZE);
                }
                break;
            }
        }
    };
}