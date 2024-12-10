package com.nytaiji.core.player;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.RestrictTo;

import com.nytaiji.core.base.BasePlayer;


import java.io.IOException;

/**
 * 原生mediaplayer实现的封装的播放器
 */

public class AndroidPlayer extends BasePlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    protected MediaPlayer mediaPlayer;
    private static double fadeTime = 0.7;
    private SharedPreferences preferences;

    public AndroidPlayer(Context context) {
        super(context);
    }

    @Override
    public void prepare() {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        fadeInFadeOut = preferences.getBoolean("FadeInFadeOut", true);
        fadeTime = (double) (preferences.getInt("fadeTime", 700)) / 1000;

        try {
            if (mediaPlayer != null) { mediaPlayer.release(); }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(isLooping());
            setDataSource();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {

        }
    }

    @Override
    protected void setDataSource() throws IOException {
        if (!TextUtils.isEmpty(assetFileName)) {
            AssetFileDescriptor afd = context.getAssets().openFd(assetFileName);
            mediaPlayer.setDataSource(afd.getFileDescriptor()
                    , afd.getStartOffset(), afd.getLength());
        } else if (rawId != 0) {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(rawId);
            mediaPlayer.setDataSource(afd.getFileDescriptor()
                    , afd.getStartOffset(), afd.getLength());
        } else if (videoUri!=null){
            mediaPlayer.setDataSource(context, videoUri, headers);
        } else if (videoUrl!=null){
            mediaPlayer.setDataSource(context, Uri.parse(videoUrl), headers);
        }
    }

    @Override
    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.destroy();
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.release();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        onPrepared();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onCompletion();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        onBufferingUpdate(percent);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        onSeekComplete();
        onBufferingEnd();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return onError();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                onBufferingStart();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                onBufferingEnd();
                break;
        }
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        onVideoSizeChangedImpl(width, height);
    }

    @Override
    public void setSurface(Surface surface) {
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(surface);
        }
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        setSurface(new Surface(surfaceTexture));
    }

    @Override
    public void setTextureView(TextureView textureView) {

    }

    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(holder);
        }
    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {

    }

    @Override
    protected void onSeekTo(long msec) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int) msec);
            onBufferingStart();
        }
    }

    @Override
    protected boolean isPlayingImpl() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public long getDuration() {
        long duration = -1;
        try {
            duration = mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
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
    public float getAspectRation() {
        return mediaPlayer == null || mediaPlayer.getVideoWidth() == 0 ? 1.0f : (float) mediaPlayer.getVideoHeight() / mediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoWidth() {
        return mediaPlayer == null ? 0 : mediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mediaPlayer == null ? 0 : mediaPlayer.getVideoHeight();
    }

    @Override
    public void setOptions() {

    }

    @Override
    public void setEnableMediaCodec(boolean isEnable) {

    }

    @Override
    protected void setEnableOpenSLES(boolean isEnable) {

    }

    //@Override
    //public long getTcpSpeed() {
      //  return 0;
   // }



    //------------------------
    private String mId;


  //  @Override
   /* public void playFromMedia(MediaMetadataCompat metadata) {
        if (mediaPlayer == null)
            prepare();

      /*  if (isCrossfade) {
            crossFadeCurrent = mCrossfadeDuration;
            isCrossfade = false;
            Log.e("CrossfadeCurrent", "run: " + crossFadeCurrent);
        } else {
            crossFadeCurrent = manualCrossfadeDuration;
        }*/

       /* mCurrentMedia = metadata;
        try {
            playFile(mCurrentMedia.getDescription().getMediaId());
        } catch (Throwable e) {
            e.printStackTrace();
            // Toast.makeText(get, "Failed to Play Media!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }*/



 ///   private final Handler crossFadeHandler = new Handler();

    private static float mFadeInVolume = 0.0f;
    private static float mFadeOutVolume = 1.0f;
    private static final double mCrossfadeDuration = 15;


    private static boolean fadeInFadeOut = true;

    /*@Override
    protected void onPlay(boolean supressFade) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            //  Log.d("OnPlayReady", "onPlayReady:SeekTime is: " + mMediaPlayer.getCurrentPosition());
            // Log.d("MediaPlayerValues", "onPlay: supressFade: +" + supressFade + "FadeInFadeOut: +" + fadeInFadeOut);
            if (!supressFade && fadeInFadeOut) {
                mFadeInVolume = 0.0f;
                mediaPlayer.setVolume(0.0f, 0.0f);
                mediaPlayer.start();
             ///   crossFadeHandler.postDelayed(FadeInRunnable, 0);
            } else {
                mediaPlayer.start();
                // MainActivity.supressPlay = false;
            }
            setNewState(STATE_PLAYING);
            //  Log.d("OnPlayInvoked", "onPlay:SeekTime is: " + mMediaPlayer.getCurrentPosition());
        }
    }*/

    public Runnable FadeInRunnable = new Runnable() {

        @Override
        public void run() {
         ///   crossFadeHandler.removeCallbacksAndMessages(FadeInRunnable);
            try {
                mediaPlayer.setVolume(mFadeInVolume, mFadeInVolume);
                mFadeInVolume = mFadeInVolume + (float) (1.0f / (((float) fadeTime) * 10.0f));
                Log.d("FadeInRunnable", "run: Volume=" + mFadeInVolume);
                if (mFadeInVolume > 0.95) {
                    setChannelBalance(context, AndroidPlayer.this, left, right);
                    //   MainActivity.supressPlay = false;
                 ///   crossFadeHandler.removeCallbacksAndMessages(FadeInRunnable);
                } ///else
                 ///  crossFadeHandler.postDelayed(FadeInRunnable, 100);

            } catch (Exception e) {
                // MainActivity.supressPlay = false;
              ///  crossFadeHandler.removeCallbacks(FadeInRunnable);
                e.printStackTrace();
            }
        }
    };

    public Runnable FadeOutRunnable = new Runnable() {
        @Override
        public void run() {
         ///   crossFadeHandler.removeCallbacksAndMessages(FadeOutRunnable);
            try {
                mediaPlayer.setVolume(mFadeOutVolume, mFadeOutVolume);
                mFadeOutVolume = mFadeOutVolume - (float) (1.0f / (((float) fadeTime) * 10.0f));
                Log.d("FadeOutRunnable", "run: Volume=" + mFadeOutVolume);
                if (mFadeOutVolume <= 0.09) {
                    mediaPlayer.setVolume(0.0f, 0.0f);
                    mediaPlayer.pause();
                  //  MainActivity.supressPlay = false;
                ///    crossFadeHandler.removeCallbacks(FadeOutRunnable);
                } ///else
                ///    crossFadeHandler.postDelayed(this, 100);
            } catch (Exception e) {
              //  MainActivity.supressPlay = false;
              ///  crossFadeHandler.removeCallbacks(FadeOutRunnable);
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onPlay() {
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        mediaPlayer.pause();
    }

   /* @Override
    protected void onPause(boolean supressFade) {
        mFadeOutVolume = 1.0f;

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                if (!supressFade && fadeInFadeOut) {
                  ///  crossFadeHandler.postDelayed(FadeOutRunnable, 0);
                } else {
                    mediaPlayer.pause();
                   // MainActivity.supressPlay = false;
                }
                setNewState(STATE_PAUSED);
                Log.d("OnPauseInvoked", "onPause:SeekTime is: " + mediaPlayer.getCurrentPosition());
            }
    }*/

    private float mVolume;
    @Override
    public void setDualVolume(float volL, float volR) {
        if ( volL * volR != 0) mVolume = (volR + volR) / 2;
        else mVolume = Math.max(volR, volR);

        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volL, volR);

        }
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }


    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Override
    public void setSpeed(float speed) {
        Log.i("System", "Speed");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && speed != mPlaybackSpeed) {
            // When video is not playing or has no tendency of to be started, we prefer recording
            // the user request to forcing the player to startPlay at that given speed.
            if (isPlaying()) {
                PlaybackParams pp = mediaPlayer.getPlaybackParams().allowDefaults();
                pp.setSpeed(speed);
                try {
                    mediaPlayer.setPlaybackParams(pp);
                    mPlaybackSpeed=speed;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                //  super.setPlaybackSpeed(speed);
            }
        }
    }

    @Override
    public float getPlaySpeed() {
        return mPlaybackSpeed;
    }

    @Override
    public  void OpenVolume() {
        try {
            if (mediaPlayer != null) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
                if (audioManager == null) return;
                float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                float volume = streamVolume * 1.000f / maxVolume;
                mediaPlayer.setVolume(volume, volume);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void CloseVolume() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getBufferProgress() {
        return 0;
    }

    @Override
    public void setRenderer(Object rendererItem) {

    }

    @Override
    public int getVolume() {
        return (int) mVolume;
    }


    // This is the main reducer for the player state machine.
    public void setNewState(int newPlayerState) {
        currentState = newPlayerState;
        // Whether playback goes to completion, or whether it is stopped, the
        // mCurrentMediaPlayedToCompletion is set to true.
      /*  if (mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true;
        } else {
            mCurrentMediaPlayedToCompletion = false;
        }

        // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
        final long reportPosition;
        if (mSeekWhileNotPlaying >= 0) {
            reportPosition = mSeekWhileNotPlaying;

            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                mSeekWhileNotPlaying = -1;
            }
        } else {
            if (isFirstMediaAcive)
                reportPosition = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
            else
                reportPosition = xMediaPlayer == null ? 0 : xMediaPlayer.getCurrentPosition();
        }

        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(mState, reportPosition,
                1.0f,
                SystemClock.elapsedRealtime());
        mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build());

        if (com.nytaiji.sonaplayer.MediaService.isActivityDestroyed && mState == PlaybackStateCompat.STATE_PLAYING) {
            SharedPreferences currentState = com.nytaiji.sonaplayer.App.getContext().getSharedPreferences("com.nytaiji.sonaplayer", Context.MODE_PRIVATE);
            setTempo(currentState.getFloat("tempo", 1.0f));
        }

        if (com.nytaiji.sonaplayer.MediaService.isActivityDestroyed && com.nytaiji.sonaplayer.MediaService.isMediaButtonStopped && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            com.nytaiji.sonaplayer.MediaService.isMediaButtonStopped = false;
            Log.e("KillServiceInvoked", "onStop: ServiceKilled");
            com.nytaiji.sonaplayer.App.getContext().stopService(new Intent(com.nytaiji.sonaplayer.App.getContext(), MediaService.class));
            //System.exit(0);

            if (!MainActivity.isActivityRunning) {
                Intent serviceIntent = new Intent(mContext, HeadsetTriggerService.class);
                mContext.startService(serviceIntent);
                Log.d("HeadSetTriggerService", "onDestroy: HeadsetWatchdogStarted");
            }
        }*/
    }

    private static float left = 1.0f;
    private static float right = 1.0f;
    public static void setChannelBalance(Context context, BasePlayer mediaPlayer, float leftx, float rightx) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setDualVolume(leftx, rightx);
                left = leftx;
                right = rightx;
                SharedPreferences currentState = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = currentState.edit();
                editor.putFloat("left", leftx);
                editor.putFloat("right", rightx);
                editor.commit();
                Log.d("MainChannelMethod", "channelBalance: Left: " + leftx + " right: " + rightx);
            }
        } catch (Throwable e) {
            Log.e("ExceptionRaised", "channelBalance: MediaPlayerAdapter " + e);
        }
    }

}
