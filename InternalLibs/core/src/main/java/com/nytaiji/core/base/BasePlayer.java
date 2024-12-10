package com.nytaiji.core.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.RawRes;

import com.nytaiji.core.listener.OnScaleChangeListener;
import com.nytaiji.core.listener.OnStateChangedListener;
import com.nytaiji.core.listener.OnVideoSizeChangedListener;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class BasePlayer implements IMediaPlayer {

    protected static final int MSG_RELEASE = 101;
    protected static final int MSG_DESTORY = 102;

    protected int currentState = STATE_IDLE;

    protected Uri videoUri;
    protected List<Uri> multiUris;
    protected String videoUrl;
    protected Map<String, String> headers;
    protected @RawRes
    int rawId;
    protected String assetFileName;

    protected OnStateChangedListener onStateChangeListener;
    protected OnVideoSizeChangedListener onVideoSizeChangedListener;

    protected OnScaleChangeListener onScaleChangeListener;

    protected Context context;

    protected boolean isPrepared = false; //播放器是否已经prepared了

    protected PlayerConfig playerConfig;

    //   protected MediaPlayerHandler mediaPlayerHandler; //用于处理release等耗时操作

    protected PlayerAudioManager playerAudioManager;

    protected float mPlaybackSpeed = 1f;


    public BasePlayer() {
    }

    public BasePlayer(Context context) {

        //使用application的context避免内存泄露
        this.context = context.getApplicationContext();
        if (this.context == null) this.context = context;
        //  HandlerThread handlerThread = new HandlerThread(this.getClass().getName());
        //  handlerThread.start();
        //  mediaPlayerHandler = new MediaPlayerHandler(handlerThread.getLooper());
    }

    public Context getContext(){
        return this.context;
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {

        this.playerConfig = playerConfig;
        if (playerConfig.enableAudioManager)
            playerAudioManager = new PlayerAudioManager(this.context, this);
        else playerAudioManager = null;
    }

    public PlayerAudioManager getPlayerAudioManager(){
        return playerAudioManager;
    }

    //region DataSource
    public void setMultiUris(List<Uri> multiUris) {
        this.multiUris = multiUris;
        //  Log.e("BasePlayer----------------", "setMultiUris");
    }


    //设置视频播放路径 (网络路径和本地文件路径)
    public void setVideoUri(Uri uri) {
        videoUri = uri;
        videoUrl = NyFileUtil.getPath(context, videoUri);
    }

    //设置视频播放路径 (网络路径和本地文件路径)
    public void setVideoUrl(String url) {
        setVideoUrl(url, null);
    }

    public void setVideoUrl(String url, Map<String, String> headers) {
        if (!TextUtils.isEmpty(url)) {
            //TODO
            videoUrl = url;
            videoUri = Uri.parse(url);
        }
        this.headers = headers;
    }

    public Uri getVideoUri() {
        return videoUri;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    //设置raw下视频的路径
    protected void setVideoRawPath(@RawRes int rawId) {
        this.rawId = rawId;
    }

    //设置assets下视频的路径
    protected void setVideoAssetPath(String assetFileName) {
        this.assetFileName = assetFileName;
    }

    //endregion

    public void setOnStateChangeListener(OnStateChangedListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public void setOnScaleChangeListener(OnScaleChangeListener onScaleChangeListener) {
        this.onScaleChangeListener = onScaleChangeListener;
    }

    public OnScaleChangeListener getOnScaleChangeListener() {
        return this.onScaleChangeListener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    public int getCurrentState() {
        return currentState;
    }

    //获取最大音量
    public int getStreamMaxVolume() {
        return playerAudioManager != null ? playerAudioManager.getStreamMaxVolume() : 0;
    }

    //获取当前音量
    public int getVolume() {
        return playerAudioManager != null ? playerAudioManager.getStreamVolume() : 0;
    }

    //设置音量
    public void setVolume(int value) {
        if (playerAudioManager != null) playerAudioManager.setStreamVolume(value);
    }


    protected void onStateChange(int state) {
        currentState = state;
        if (onStateChangeListener != null) {
            onStateChangeListener.onStateChange(state);
        }
    }

    public void play() {
        //TODO NY
        // playerAudioManager.requestAudioFocus();
        onPlay();
        if (onStateChangeListener != null) onStateChange(STATE_PLAYING);
    }

    //not working with VLC player, need to be overrided in VLC
    public void replay() {
        seekTo(0);
        play();
    }

    //播放
    protected void onPlay() {
      //  onPlay(false);
    }


    public void pause() {
        if (!isPlaying()) {
            return;
        }
        if (playerAudioManager != null && !playerAudioManager.isPlayOnAudioFocus()) {
            playerAudioManager.abandonAudioFocus();
        }
        unregisterAudioNoisyReceiver();
        onPause();
        if (onStateChangeListener != null) onStateChange(STATE_PAUSED);
    }

  /*  @Override
    public final void pause(boolean supressFade) {
        if (playerAudioManager != null && !playerAudioManager.isPlayOnAudioFocus()) {
            playerAudioManager.abandonAudioFocus();
        }
        unregisterAudioNoisyReceiver();
        onPause();
       // Log.e("playerAdapterOnPause", "pause:Invoked ");
    }*/

    //暂停
    protected void onPause() {
    }

    /**
     * Called when media must be paused.
     */

   // @Override
   // public abstract void setDualVolume(float leftx, float rightx);

    public void initPlayer() {
        isPrepared = false;
        if (noDataSource()) {
            return;
        }
        prepare();
        if (onStateChangeListener != null) onStateChange(STATE_PREPARING);
    }

    //TODO
    protected boolean noDataSource() {
        return multiUris == null && videoUri == null && videoUrl == null && rawId == 0 && TextUtils.isEmpty(assetFileName);
    }

    @Override
    public boolean isPlaying() {
        return isPrepared && isPlayingImpl();
    }

    @Override
    public void release() {
        if (playerAudioManager != null) playerAudioManager.abandonAudioFocus();
        isPrepared = false;
        if (onStateChangeListener != null) onStateChange(STATE_IDLE);

       /* Message message = Message.obtain();
        message.what = MSG_RELEASE;
        mediaPlayerHandler.sendMessage(message);*/
    }

    @Override
    public void destroy() {
        if (playerAudioManager != null) playerAudioManager.destroy();
        isPrepared = false;
        if (onStateChangeListener != null) onStateChange(STATE_IDLE);

      /*  Message message = Message.obtain();
        message.what = MSG_DESTORY;
        mediaPlayerHandler.sendMessage(message);*/
    }

    @Override
    public void seekTo(long position) {
        onSeekTo(position);
    }

    //prepare成功后的具体实现
    protected void onPrepared() {
        isPrepared = true;
        if (onStateChangeListener != null) onStateChange(STATE_PREPARED);
        play();
    }

    //onCompletion的具体实现
    protected void onCompletion() {
        //the following is removed for replay use
        //  if (playerAudioManager != null) playerAudioManager.abandonAudioFocus();
        if (onStateChangeListener != null) onStateChange(STATE_COMPLETED);
    }

    //onBufferingUpdate具体实现
    protected void onBufferingUpdate(int percent) {
        bufferedPercentage = percent;
    }

    protected void onSeekComplete() {
    }

    protected boolean onError() {
        if (onStateChangeListener != null) onStateChange(STATE_ERROR);
        return true;
    }

    protected void onBufferingStart() {
        if (onStateChangeListener != null) onStateChange(STATE_BUFFERING_START);
    }

    protected void onBufferingEnd() {
        if (onStateChangeListener != null) onStateChange(STATE_BUFFERING_END);
    }

    protected void onVideoSizeChangedImpl(int width, int height) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
    }

    protected boolean isLooping() {
        return playerConfig != null && playerConfig.looping;
    }

    //设置播放数据源
    protected abstract void setDataSource() throws IOException;

    //初始化播放器
    public abstract void prepare();

    //是否正在播放
    protected abstract boolean isPlayingImpl();

    //跳转到指定播放位置
    protected abstract void onSeekTo(long position);

    //获取视频内容高宽比
    public abstract float getAspectRation();

    //获取视频内容宽度
    public abstract int getVideoWidth();

    //获取视频内容高度
    public abstract int getVideoHeight();

    //获取当前播放进度
    public abstract long getCurrentPosition();

    //获取播放总进度
    public abstract long getDuration();


    //针对某些播放器内核，比如IjkPlayer，进行的一些额外设置
    public abstract void setOptions();

    //是否支持硬解码
    protected abstract void setEnableMediaCodec(boolean isEnable);

    //是否启用OpenSL ES
    protected abstract void setEnableOpenSLES(boolean isEnable);


    //----------------TODO---------------

    protected int bufferedPercentage;

    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean mAudioNoisyReceiverRegistered = false;
    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPlaying()) {
                            pause();
                        }
                    }
                }
            };

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            context.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            context.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

  /*  @Override
    public final void play(boolean supressFade) {
        if (playerAudioManager != null && playerAudioManager.audioFocusObtained()) {
            registerAudioNoisyReceiver();
            onPlay(supressFade);
        }
    }*/

    //TODO ny specially designed for fanplayer
    public void setDisplaySize(int width, int height){};


    public void setMute(boolean isMute) {
        if (isMute) {
            CloseVolume();
        } else {
            OpenVolume();
        }
    }

    /**
     * Called when media is ready to be played and indicates the app has audio focus.
     */
  /*  protected abstract void onPlay(boolean supressFade);*/


    public abstract void setNewState(int newState);

    // the following two added by NYTaiji for player speed and muting

    public abstract void setSpeed(float speed);

    public abstract float getPlaySpeed();

    public abstract void OpenVolume();

    public abstract void CloseVolume();

    public abstract int getBufferProgress();

    public abstract void setRenderer(Object rendererItem);
}
