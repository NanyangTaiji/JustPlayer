package com.nytaiji.exoplayer;


import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.RawResourceDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnMediaPlayerStateListener;
import com.nytaiji.exoplayer.encrypt.ExoSourceManager;
import com.nytaiji.nybase.model.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GoogleExoPlayer extends BasePlayer {

    protected ExoPlayer mediaPlayer;
    protected Context mContext;
    protected MediaSource mediaSource = null;
    // protected String passWord = null;
    protected String fileName = null;
    protected boolean cachEnabled;
    protected int isLooping = 2;
    protected Handler mHandler;
    protected OnMediaPlayerStateListener onMediaPlayerStateListener;
    private int mBufferProgress;                // 缓冲百分比
    private OnBufferingUpdate mOnBufferingUpdate;
    private int videoWidth;
    private int videoHeight;
    private float mVolume;

    private long duration = -1L;

    private PlayerListener playerListener;
    SharedPreferences playerPrefs = context.getSharedPreferences(Constants.MAIN_SETTINGS, Context.MODE_PRIVATE);
    public GoogleExoPlayer(Context context) {
        super(context);
        mContext = context;
    }


    public GoogleExoPlayer(Context context, OnMediaPlayerStateListener onMediaPlayerStateListener) {
        super(context);
        mContext = context;
        this.onMediaPlayerStateListener = onMediaPlayerStateListener;
    }

    public GoogleExoPlayer(Context context, MediaSource mediaSource) {
        super(context);
        mContext = context;
        this.mediaSource = mediaSource;
    }
    public GoogleExoPlayer(Context context, ExoPlayer mediaPlayer) {
        super(context);
        mContext = context;
        this.mediaPlayer = mediaPlayer;
    }

    public GoogleExoPlayer(Context context, ExoPlayer mediaPlayer, MediaSource mediaSource) {
        super(context);
        mContext = context;
        this.mediaPlayer = mediaPlayer;
        this.mediaSource = mediaSource;
    }


    @OptIn(markerClass = UnstableApi.class) @Override
    public void prepare() {
        mHandler = new Handler();
        mOnBufferingUpdate = new OnBufferingUpdate();

        //----------------------//
        RenderersFactory renderersFactory = new DefaultRenderersFactory(mContext)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);


        if (mediaPlayer == null)
            mediaPlayer = new ExoPlayer.Builder(mContext, renderersFactory).build();

        playerListener = new PlayerListener();
        mediaPlayer.addListener(playerListener);
        mediaPlayer.setRepeatMode(isLooping() ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);

        if (mediaSource == null) {
            try {
                setDataSource();
            } catch (Exception ignored) {

            }
        } else {
            mediaPlayer.addMediaSource(mediaSource);
            mediaPlayer.prepare();
            mediaPlayer.setPlayWhenReady(true);
        }
    }

    //
    @OptIn(markerClass = UnstableApi.class) @Override
    protected void setDataSource() throws IOException {

        if (multiUris == null) multiUris = new ArrayList<Uri>();

        if (!TextUtils.isEmpty(assetFileName)) {//defined in BasePlayer
            String assetFilePath = "file:///android_asset/" + assetFileName;
            multiUris.add(Uri.parse(assetFilePath));
       } else if (rawId != 0) {//defined in BasePlayer
            RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(context);
            DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(rawId));
            rawResourceDataSource.open(dataSpec);
            multiUris.add(rawResourceDataSource.getUri());
        }else if (videoUri != null) {
            multiUris.add(videoUri);
        } else if (videoUrl != null) {
            multiUris.add(Uri.parse(videoUrl));
        }


        //----------gate to mediasource

        fileName = playerConfig.getFileName();
        isLooping = playerPrefs.getInt(Constants.KEY_ENDING, 2);

        if (multiUris == null) {
            Log.e("GoogleExoPlayer", "null resource! ");

        } else startPlayer(createSources(multiUris, null));
    }

    private List<MediaSource> createSources
            (List<Uri> multiUris, List<MediaSource> subtitles) {
        Objects.requireNonNull(multiUris, "videoUri is required");
        // Logger.e(this, "Create datasources for video=%s \n\taudio= %s and %s subtitles", videoUri, audioUri, subtitles);
        List<MediaSource> sources = new ArrayList<MediaSource>();

        for (int i = 0; i < multiUris.size(); i++) {
            sources.add(createSource(multiUris.get(i)));
        }

        if (subtitles != null) {
            sources.addAll(subtitles);
        }
        return sources;
    }

    @OptIn(markerClass = UnstableApi.class)
    protected MediaSource createSource(Uri uri) {
        return ExoSourceManager.getInstance(mContext).buildMediaSource(uri, isLooping == 1);
    }

    @OptIn(markerClass = UnstableApi.class) private void startPlayer(List<MediaSource> sources) {
        Objects.requireNonNull(sources, "sources");

        if (sources.isEmpty()) {
            return;
        }
        if (sources.size() == 1) {
            mediaPlayer.addMediaSource(sources.get(0));
            mediaPlayer.prepare();
        } else {
            MergingMediaSource merged = new MergingMediaSource(sources.toArray(new MediaSource[sources.size()]));
            mediaPlayer.addMediaSource(merged);
            mediaPlayer.prepare();
        }
        mediaPlayer.setPlayWhenReady(true);
    }

    @Override
    protected boolean isPlayingImpl() {
        if (mediaPlayer == null) {
            return false;
        }
        int playbackState = mediaPlayer.getPlaybackState();
        return playbackState != Player.STATE_IDLE
                && playbackState != Player.STATE_ENDED
                && mediaPlayer.getPlayWhenReady();
    }

    @Override
    protected void onPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null) {
            mediaPlayer.setPlayWhenReady(false);
        }
    }


    @Override
    public void release() {
        super.release();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.removeListener(playerListener);
        }
        ExoSourceManager.getInstance(mContext).releaseCache();
    }


    @Override
    public void destroy() {
        super.destroy();
        if (mediaPlayer != null) {
            mediaPlayer.removeListener(playerListener);
        }
    }

    @Override
    public float getAspectRation() {
        return mediaPlayer == null || videoWidth == 0 ? 1.0f : (float) videoHeight / videoWidth;
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
            position = mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position;
    }

    @Override
    public long getDuration() {
        try {
            duration = mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    @Override
    protected void onSeekTo(long position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public void setSurface(Surface surface) {
        if (mediaPlayer != null) {
            //  new Handler(Looper.getMainLooper()).post(() -> {
            mediaPlayer.setVideoSurface(surface);
            // });
        }
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture surface) {
        // if (simpleExoPlayer != null) {
        mediaPlayer.setVideoSurface(new Surface(surface));
        // }
    }

    @Override
    public void setTextureView(TextureView textureView) {

    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        if (mediaPlayer != null) {
            mediaPlayer.setVideoSurfaceHolder(surfaceView.getHolder());
        }
    }


    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            // new Handler(Looper.getMainLooper()).post(() -> {
            mediaPlayer.setVideoSurfaceHolder(holder);
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
        mediaPlayer.setVolume(mVolume);
    }

    @OptIn(markerClass = UnstableApi.class) @Override
    public int getAudioSessionId() {
        if (mediaPlayer!=null) return mediaPlayer.getAudioSessionId();
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
        return (int) mediaPlayer.getVolume();
    }


    @Override
    public void OpenVolume() {
        try {
            if (mediaPlayer != null) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
                if (audioManager == null) return;
                float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                float volume = streamVolume / maxVolume;
                mediaPlayer.setVolume(volume);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void CloseVolume() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setSpeed(float speed) {
        //  Log.i("EXOPLAYER", "Speed");
        if (mediaPlayer != null && speed != mPlaybackSpeed) {
            mediaPlayer.setPlaybackParameters(new PlaybackParameters(speed));
            mPlaybackSpeed = speed;
        }
    }

    @Override
    public float getPlaySpeed() {
        return mPlaybackSpeed;
    }


    //--------------------------------------------


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
            if (mediaPlayer == null) return;
            //
            int percent = mediaPlayer.getBufferedPercentage();
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

    private class PlayerListener implements Player.Listener {
        @Override
        public void onPlaybackStateChanged(@PlaybackStateCompat.State int state) {
            switch (state) {
                case Player.STATE_READY:
                    if (isPrepared) { // 准备好了，先暂停
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
                        onBufferingUpdate(mediaPlayer.getBufferedPercentage());
                        mHandler.post(mOnBufferingUpdate);
                    }
                    break;
            }
        }

        @Override
        public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
            videoWidth = videoSize.width;
            videoHeight = videoSize.height;
            onVideoSizeChangedImpl(videoWidth, videoHeight);
        }
    }

}