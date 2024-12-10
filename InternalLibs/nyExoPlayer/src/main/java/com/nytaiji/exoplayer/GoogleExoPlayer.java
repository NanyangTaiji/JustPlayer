package com.nytaiji.exoplayer;

import static com.nytaiji.nybase.filePicker.MediaSelection.DEFAULT_MEDIA;
import static com.nytaiji.nybase.filePicker.MediaSelection.getStringValue;
import static com.nytaiji.nybase.model.Constants.KEY_CACHE;
import static com.nytaiji.nybase.model.Constants.KEY_ENDING;
import static com.nytaiji.nybase.model.Constants.MAIN_SETTINGS;
import static com.nytaiji.nybase.utils.NyFileUtil.getExtension;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.video.VideoListener;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnMediaPlayerStateListener;
import com.nytaiji.nybase.crypt.EncryptUtil;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.exoplayer.encrypt.ExoSourceManager;
import com.nytaiji.nybase.utils.NyMimeTypes;

import java.io.File;
import java.io.IOException;

//import com.google.android.exoplayer2.source.ExtractorMediaSource;

public class GoogleExoPlayer extends BasePlayer implements Player.Listener {

    private SimpleExoPlayer simpleExoPlayer;
    private MediaSource mediaSource = null;
    private DataSource.Factory dataSourceFactory;
    private Handler mHandler;
    private int mBufferProgress;                // 缓冲百分比
    private OnBufferingUpdate mOnBufferingUpdate;
    private int videoWidth;
    private int videoHeight;
    private final Context mContext;
    private float mVolume;
    private OnMediaPlayerStateListener onMediaPlayerStateListener;

    public GoogleExoPlayer(Context context) {
        super(context);
        //  dataSourceFactory = getDataSource();
        mContext = context;
    }


    public GoogleExoPlayer(Context context, OnMediaPlayerStateListener onMediaPlayerStateListener) {
        super(context);
        mContext = context;
        //   dataSourceFactory = getDataSource();
        this.onMediaPlayerStateListener = onMediaPlayerStateListener;
    }


    public GoogleExoPlayer(Context context, SimpleExoPlayer simpleExoPlayer) {
        super(context);
        mContext = context;
        this.simpleExoPlayer = simpleExoPlayer;
        //  dataSourceFactory = getDataSource();
    }

    public GoogleExoPlayer(Context context, SimpleExoPlayer simpleExoPlayer, MediaSource mediaSource) {
        super(context);
        mContext = context;
        this.simpleExoPlayer = simpleExoPlayer;
        this.mediaSource = mediaSource;
    }

    public GoogleExoPlayer(Context context, MediaSource mediaSource) {
        super(context);
        mContext = context;
        this.mediaSource = mediaSource;
    }

    @Override
    public void prepare() {
        mHandler = new Handler();
        mOnBufferingUpdate = new OnBufferingUpdate();
        //----------------------//

        if (simpleExoPlayer == null)
            simpleExoPlayer = new SimpleExoPlayer.Builder(context.getApplicationContext()).build();
        simpleExoPlayer.addListener(playerEventListener);
        //--
        simpleExoPlayer.addVideoListener(videoListener);
        //---
        simpleExoPlayer.setThrowsWhenUsingWrongThread(false);

        simpleExoPlayer.setRepeatMode(isLooping() ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);

        if (mediaSource == null) {
            try {
                setDataSource();
            } catch (Exception e) {

            }
        } else {
            simpleExoPlayer.prepare(mediaSource);
            //simpleExoPlayer.prepare(mediaSource, true, true);
            simpleExoPlayer.setPlayWhenReady(true);
        }

    }

    //TODO
    public void setUri(Uri uri) {
        this.videoUri = uri;
        prepare();
    }


    //
    @Override
    protected void setDataSource() throws IOException {
        Uri currentUri = null;
        MediaSource mediaSource;
        if (!TextUtils.isEmpty(assetFileName)) {//defined in BasePlayer
            String assetFilePath = "file:///android_asset/" + assetFileName;
            currentUri = Uri.parse(assetFilePath);
        } else if (rawId != 0) {//defined in BasePlayer
            RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(context);
            DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(rawId));
            rawResourceDataSource.open(dataSpec);
            currentUri = rawResourceDataSource.getUri();
        } else if (videoUri != null) {
            currentUri = videoUri;
            Log.e("GoogleExoPlayer", "currentUri.getScheme(): " + currentUri.getScheme());
        } else if (videoUrl != null) {
            currentUri = Uri.parse(videoUrl);
        }

        //TODO most important
        //----------gate to mediasource
        String passWord = null;
        String fileName = null;
        passWord = playerConfig.getPassword();
        Log.e("GoogleExoPlayer", "playerConfig.getPassword(): " + passWord);
        fileName = playerConfig.getFileName();
        if (passWord == null && fileName != null)
            passWord = EncryptUtil.getPasswordFromFileName(fileName);

        SharedPreferences playerPrefs = context.getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);
        boolean cachEnabled = playerPrefs.getBoolean(KEY_CACHE, false);
        int isLooping = playerPrefs.getInt(KEY_ENDING, 2);
        mediaSource = ExoSourceManager.getInstance(mContext).buildMediaSource(
                currentUri, false, cachEnabled, isLooping == 1, new File(NyFileUtil.getCacheFolder()));
        //----------------------
        simpleExoPlayer.setMediaSource(mediaSource);
        simpleExoPlayer.prepare();
        // player.play();
        // simpleExoPlayer.prepare(mediaSource);
        //  simpleExoPlayer.prepare(mediaSource, true, true);
        simpleExoPlayer.setPlayWhenReady(true);
    }

    public MediaSource getMediaSource() {
        return mediaSource;
    }

    /*
    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (!isLoading) {
            onMediaPlayerStateListener.onMediaLoadingComplete();
        }
    }*/

    @Override
    protected boolean isPlayingImpl() {
        if (simpleExoPlayer == null) {
            return false;
        }
        int playbackState = simpleExoPlayer.getPlaybackState();
        return playbackState != Player.STATE_IDLE
                && playbackState != Player.STATE_ENDED
                && simpleExoPlayer.getPlayWhenReady();
    }

    @Override
    protected void onPlay() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onPause() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void release() {
        super.release();
        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
            simpleExoPlayer.removeListener(playerEventListener);
            simpleExoPlayer.removeVideoListener(videoListener);
        }
    }


    protected void onRelease() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (simpleExoPlayer != null) {
            simpleExoPlayer.removeListener(playerEventListener);
            simpleExoPlayer.removeVideoListener(videoListener);
        }
    }


    protected void onDestroy() {
        onRelease();
        simpleExoPlayer = null;
    }

    @Override
    public float getAspectRation() {
        return simpleExoPlayer == null || videoWidth == 0 ? 1.0f : (float) videoHeight / videoWidth;
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
        long position = 0;
        try {
            position = simpleExoPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position;
    }

    @Override
    public long getDuration() {
        long duration = -1;
        try {
            duration = simpleExoPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    @Override
    protected void onSeekTo(long position) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.seekTo(position);
        }
    }

    @Override
    public void setSurface(Surface surface) {
        if (simpleExoPlayer != null) {
            //  new Handler(Looper.getMainLooper()).post(() -> {
            simpleExoPlayer.setVideoSurface(surface);
            // });
        }
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture surface) {
        // if (simpleExoPlayer != null) {
        simpleExoPlayer.setVideoSurface(new Surface(surface));
        // }
    }

    @Override
    public void setTextureView(TextureView textureView) {

    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setVideoSurfaceHolder(surfaceView.getHolder());
        }
    }


    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {
        if (simpleExoPlayer != null) {
            // new Handler(Looper.getMainLooper()).post(() -> {
            simpleExoPlayer.setVideoSurfaceHolder(holder);
            //  });
        }
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


    //---------------------------------------------
    @Override
    public void setDualVolume(float leftx, float rightx) {
        if (leftx * rightx != 0) mVolume = (leftx + rightx) / 2;
        else mVolume = Math.max(leftx, rightx);
        simpleExoPlayer.setVolume(mVolume);
    }

    @Override
    public int getAudioSessionId() {
        if (simpleExoPlayer != null) return simpleExoPlayer.getAudioComponent().getAudioSessionId();
        else return -1;
    }

    @Override
    public void setNewState(int newState) {
        currentState = newState;
    }


    @Override
    public int getBufferProgress() {
        return mBufferProgress;
    }

    @Override
    public void setRenderer(Object rendererItem) {

    }

    @Override
    public int getVolume() {
        return (int) simpleExoPlayer.getVolume();
    }


    @Override
    public void OpenVolume() {
        try {
            if (simpleExoPlayer != null) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
                if (audioManager == null) return;
                float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                float volume = streamVolume / maxVolume;
                simpleExoPlayer.setVolume(volume);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void CloseVolume() {
        try {
            if (simpleExoPlayer != null) {
                simpleExoPlayer.setVolume(0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setSpeed(float speed) {
        //  Log.i("EXOPLAYER", "Speed");
        if (simpleExoPlayer != null && speed != mPlaybackSpeed) {
            simpleExoPlayer.setPlaybackParameters(new PlaybackParameters(speed));
            mPlaybackSpeed = speed;
        }
    }

    @Override
    public float getPlaySpeed() {
        return mPlaybackSpeed;
    }


    //--------------------------------------------

    private final Player.EventListener playerEventListener = new Player.EventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_READY:
                    if (playWhenReady) { // 准备好了，先暂停
                        if (onMediaPlayerStateListener != null)
                            onMediaPlayerStateListener.onMediaReadyPlaying();
                    }
                    onBufferingEnd();
                    if (!isPrepared) {
                        onPrepared();
                    }
                    break;
                case Player.STATE_ENDED:
                    if (onMediaPlayerStateListener != null)
                        onMediaPlayerStateListener.onMediaAutoCompletion();
                    onBufferingEnd();
                    onCompletion();
                    break;
                case Player.STATE_IDLE:
                    break;
                case Player.STATE_BUFFERING:
                    if (isPrepared) {
                        onBufferingStart();
                        onBufferingUpdate(simpleExoPlayer.getBufferedPercentage());
                        mHandler.post(mOnBufferingUpdate);
                    }
                    break;
            }
        }

        //@Override
        public void onPlayerError(ExoPlaybackException error) {
            onError();
        }
    };

    private final VideoListener videoListener = new VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            videoWidth = width;
            videoHeight = height;
            onVideoSizeChangedImpl(width, height);
        }
    };

  /*  protected DataSource.Factory getDataSource() {
        String userAgent = Util.getUserAgent(context, context.getPackageName());
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();

        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(userAgent, defaultBandwidthMeter);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                dataSourceFactory.getDefaultRequestProperties().set(header.getKey(), header.getValue());
            }
        }

        return new DefaultDataSourceFactory(context, new DefaultBandwidthMeter(), dataSourceFactory);
    }*/

    private class OnBufferingUpdate implements Runnable {

        @Override
        public void run() {
            //the following line is temp correction of exit crash of online video
            if (simpleExoPlayer == null) return;
            //
            int percent = simpleExoPlayer.getBufferedPercentage();
            mBufferProgress = percent;
            // Log.d(TAG, "OnBufferingUpdate:" + percent + "hashcode=" +
            // hashCode());
            if (percent < 100) {
                mHandler.postDelayed(mOnBufferingUpdate, 300);
            } else {
                mHandler.removeCallbacks(mOnBufferingUpdate);
            }
        }

    }

    public static final String MIME_TYPE_DASH = MimeTypes.APPLICATION_MPD;
    public static final String MIME_TYPE_HLS = MimeTypes.APPLICATION_M3U8;
    public static final String MIME_TYPE_SS = MimeTypes.APPLICATION_SS;
    public static final String MIME_TYPE_VIDEO_MP4 = MimeTypes.VIDEO_MP4;

    public static String getGoogLeMimeType(Context context, String url) {
        String mimeType = null;
        if (url.endsWith("mpd")) mimeType = MIME_TYPE_DASH;
        else if (url.endsWith("m3u8")) mimeType = MIME_TYPE_HLS;
        else if (getExtension(url).contains("ism")) mimeType = MIME_TYPE_SS;
        else if (mimeType == null) mimeType = NyMimeTypes.getMimeTypeFromPath(url);
        else if (mimeType == null)
            mimeType = getStringValue(context, DEFAULT_MEDIA, MIME_TYPE_VIDEO_MP4);
        else if (mimeType == null) mimeType = MIME_TYPE_VIDEO_MP4;
        return mimeType;
    }

}