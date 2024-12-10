package com.nytaiji.vlcplayer;


import static com.nytaiji.nybase.utils.SystemUtils.getScreenHeight;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenWidth;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnScaleChangeListener;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.NyFileUtil;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.RendererItem;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.util.VLCUtil;
import org.videolan.libvlc.util.VLCVideoLayout;

public class VlcMediaPlayer extends BasePlayer {
    private static String TAG = "VlcMediaPlayer--------------------";

    private LibVLC mLibVLC = null;
    private MediaPlayer mediaPlayer = null;
    private final Context mContext;
    private int videoWidth, videoHeight, videoVisibleWidth, videoVisibleHeight, mSarNum, mSarDen;
    private VLCVideoLayout vlcVideoLayout;


    private SurfaceView surfaceView = null;

    private long playeTime = 0L;


    public VlcMediaPlayer(Context context) {
        super(context);
        if (context instanceof Activity) {
            mContext = (Activity) context;
        } else {
            mContext = null;
            Log.e(TAG, "context is not activity");
        }
        setOnScaleChangeListener(new OnScaleChangeListener() {
            @Override
            public void onScaleChanged(BaseConstants.ScaleType scaleType) {
                setVideoScale(scaleType);
           }
        });
    }



    @Override
    public void prepare() {
        mLibVLC = (LibVLC) VLCInstance.getInstance(AppContextProvider.getAppContext());
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer(mLibVLC);
        mediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering:
                        //TODO  most important
                        if (event.getBuffering() >= 100.0f) {
                            onStateChange(BasePlayer.STATE_PREPARED);
                            onPrepared();
                        }
                        break;

                    case MediaPlayer.Event.Opening:
                        onBufferingUpdate((int) event.getBuffering());
                        // ULog.d("onEvent: opening...");
                        //  onLoading();
                        break;

                    case MediaPlayer.Event.Playing:
                        // onStateChange(BasePlayer.STATE_PLAYING);
                        break;

                    case MediaPlayer.Event.EndReached:
                        onCompletion();
                        break;

                    case MediaPlayer.Event.EncounteredError:
                        onError();
                        break;
                    case MediaPlayer.Event.PositionChanged:
                        break;
                }
            }
        });

        setDataSource();
    }

    private boolean isCasting = false;

    @Override
    protected void setDataSource() {
        Media media;
        // uri defined in BaseVideoPlayer
        String url = null;
        if (videoUri != null) {
            url = NyFileUtil.getPath(context, videoUri).replace("content///","content://");
            Log.e(TAG, "url " + url);
           // if (url.contains("/storage")) url = url.substring(url.indexOf("/storage"));
        } else url = videoUrl;
        Log.e(TAG, "videoUrl " + url);
        if (url.toLowerCase().contains("content")||url.toLowerCase().contains("http") || url.toLowerCase().contains("smb")) {
            media =  new Media(mLibVLC,  Uri.parse(VLCUtil.encodeVLCUri(videoUri)));
         //   media = new Media(mLibVLC, Uri.parse(url));
        } else {
            media = new Media(mLibVLC, url);
        }
        //--------------options-------------
        //   media.addOption(":sout-udp-caching=" + 100);
        //   media.addOption(":network-caching=" + 256);
        //  media.addOption(":file-caching=" + 3000);
        //  media.addOption(":sout-mux-caching=" + 3000);
        //-----------------------------------------------
        //  media.addOption(":live-caching=" + 64);
        //  media.addOption(":codec=mediacodec,iomx,all");
        //  Toast.makeText(context, url, Toast.LENGTH_LONG).show();

        mediaPlayer.setMedia(media);
        VLCOptions.setMediaOptions(media, mContext, 1, isCasting);
        media.release();
        mediaPlayer.play();
        mediaPlayer.setTime(playeTime);
    }


    //the following is needed for VLC only
    @Override
    public void replay() {
        prepare();
        play();
    }

    @Override
    protected boolean isPlayingImpl() {
        if (mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    @Override
    protected void onPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void setDualVolume(float leftx, float rightx) {

    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioTrack();
    }


    @Override
    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mLibVLC.release();
        super.destroy();
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mLibVLC.release();
        super.release();
    }


    @Override
    public float getAspectRation() {
        return mediaPlayer == null || getVideoWidth() == 0 ? 1.0f : (float) getVideoHeight() / getVideoWidth();
    }

    @Override
    public int getVideoWidth() {
        if (videoWidth > 0) return videoWidth;
        else {
            Media.VideoTrack vtrack;
            if (mediaPlayer != null) {
                vtrack = mediaPlayer.getCurrentVideoTrack();
                videoWidth = vtrack.width;
                return videoWidth;
            }
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        //
        if (videoHeight > 0) return videoHeight;
        else {
            Media.VideoTrack vtrack;
            if (mediaPlayer != null) {
                vtrack = mediaPlayer.getCurrentVideoTrack();
                videoHeight = vtrack.height;
                return videoHeight;
            }
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        long position = 0;
        try {
            //TODO
            position = (long) mediaPlayer.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position;
    }

    @Override
    public long getDuration() {
        long duration = -1;
        if (mediaPlayer == null) return duration;
        try {
            duration = mediaPlayer.getLength();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return duration;
    }

    @Override
    protected void onSeekTo(long position) {
        //TODO very important
        mediaPlayer.setTime(position);
    }


    //SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
    //Surface surface = new Surface(surfaceTexture);

    @Override
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
      /*  if (mediaPlayer == null) mediaPlayer = new MediaPlayer(mLibVLC);
        setSurface(new Surface(surfaceTexture));*/
    }

    @Override
    public void setTextureView(TextureView textureView) {
      /*  if (mediaPlayer == null) mediaPlayer = new MediaPlayer(mLibVLC);
        mediaPlayer.getVLCVout().setVideoView(textureView);
        mediaPlayer.getVLCVout().attachViews();
        //   if (getVideoWidth()*getVideoHeight()>0)  mediaPlayer.getVLCVout().setWindowSize(getVideoWidth(), getVideoHeight());
        //  else
        mediaPlayer.getVLCVout().setWindowSize(textureView.getWidth(), textureView.getHeight());
        videoVisibleWidth = textureView.getWidth();
        videoVisibleHeight = textureView.getHeight();*/
    }


    @Override
    public void setSurface(Surface surface) {
        //FOR TextureView
        Log.e(TAG, "------------------------ setSurface");
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer(mLibVLC);
        mediaPlayer.getVLCVout().setVideoSurface(surface, null);
        mediaPlayer.getVLCVout().setWindowSize(getScreenWidth(mContext), getScreenHeight(mContext));
        mediaPlayer.getVLCVout().attachViews();

    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        Log.e(TAG, "------------------------SetSurfaceView");
        this.surfaceView = surfaceView;
        //  if (mediaPlayer == null) mediaPlayer = new MediaPlayer(mLibVLC);
        mediaPlayer.getVLCVout().setVideoView(surfaceView);
        mediaPlayer.getVLCVout().attachViews();
        //   mediaPlayer.attachViews(vlcVideoLayout, null, false, false);
        //   if (getVideoWidth()*getVideoHeight()>0)  mediaPlayer.getVLCVout().setWindowSize(getVideoWidth(), getVideoHeight());
        //  else
        // mediaPlayer.getVLCVout().setWindowSize(surfaceView.getWidth(), surfaceView.getHeight());
        // videoVisibleWidth = surfaceView.getWidth();
        //  videoVisibleHeight = surfaceView.getHeight();

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        //   surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //TODO ny
                // tmp solve black surface come back from history and overview buttons
                setSurfaceView(surfaceView);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mediaPlayer.getVLCVout().setWindowSize(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // destroy();
            }
        });

    }


    @Override
    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        //TODO ny
        // This one is called before setSurfaceView

       /* Log.e(TAG, "------------------------setSurfaceHolder");
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer(mLibVLC);

        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mediaPlayer.getVLCVout().setVideoSurface(surfaceHolder.getSurface(), surfaceHolder);
                mediaPlayer.getVLCVout().attachViews();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer(mLibVLC);
                }
                mediaPlayer.getVLCVout().setWindowSize(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                destroy();
            }
        });*/
    }

    //@Override
    public void setVideoScale(BaseConstants.ScaleType scaleType) {
        int sw;
        int sh;
        // get screen size
        sw = videoVisibleWidth;
        sh = videoVisibleHeight;
        Log.e(TAG, "video scale changed----------- " + scaleType);
        // Change the video placement using MediaPlayer API
        switch (scaleType) {
            case SCALE_ORIGINAL:
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(1);
                break;
            case SCALE_DEFAULT:
                Media.VideoTrack vtrack = mediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                //  if (mCurrentSize == SURFACE_FIT_SCREEN) {
                int videoW = vtrack.width;
                int videoH = vtrack.height;

                if (videoSwapped) {
                    int swap = videoW;
                    videoW = videoH;
                    videoH = swap;
                }
                if (vtrack.sarNum != vtrack.sarDen)
                    videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                float ar = videoW / (float) videoH;
                float dar = sw / (float) sh;

                float scale;
                if (dar >= ar)
                    scale = sw / (float) videoW;
                else
                    scale = sh / (float) videoH;
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(scale);
                break;

            case SCALE_CENTER_CROP:
                vtrack = mediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;

                mediaPlayer.setAspectRatio(!videoSwapped ? "" + sw + ":" + sh
                        : "" + sh + ":" + sw);
                mediaPlayer.setScale(2);
                break;
            case SCALE_16_9:
                mediaPlayer.setAspectRatio("16:9");
                mediaPlayer.setScale(0);
                break;
            case SCALE_4_3:
                mediaPlayer.setAspectRatio("4:3");
                mediaPlayer.setScale(0);
                break;
            case SCALE_MATCH_PARENT:
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(1);
                break;
        }
    }
    // public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    // public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;

    @Override
    public void setOptions() {

    }

 /*   @Override
    public void directExecution(int code) {

    }*/

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
        if (speed != mPlaybackSpeed) {
            //  mUserPlaybackSpeed = speed;
            if (mediaPlayer != null) {
                mediaPlayer.setRate(speed);
                mPlaybackSpeed = speed;
                //  super.setPlaybackSpeed(speed);
            }
        }
    }

    @Override
    public float getPlaySpeed() {
        return mPlaybackSpeed;
    }


    @Override
    public int getBufferProgress() {
        return 0;
    }

    @Override
    public void setRenderer(Object rendererItem) {
            if (rendererItem != null&& rendererItem instanceof RendererItem ) {
                mediaPlayer.setRenderer((RendererItem) rendererItem);
                isCasting = true;
            } else {
                playeTime = mediaPlayer.getTime();
                mediaPlayer.setRenderer(null);
                isCasting = false;
                setDataSource();
            }
    }

    @Override
    public int getVolume() {
        return currentVolume;
    }

    @Override
    public void setVolume(int value) {
        currentVolume = value;
        mediaPlayer.setVolume(value);
    }

    private int currentVolume = 50;

    @Override
    public void OpenVolume() {
        mediaPlayer.setVolume(100);
    }

    @Override
    public void CloseVolume() {
       // currentVolume = mediaPlayer.getVolume();
        mediaPlayer.setVolume(1);
    }


    /**
     * This listener is called when the "android-display" "vout display" module request a new
     * video layout. The implementation should take care of changing the surface
     * LayoutsParams accordingly. If width and height are 0, LayoutParams should be reset to the
     * initial state (MATCH_PARENT).
     * <p>
     * By default, "android-display" is used when doing HW decoding and if Video and Subtitles
     * surfaces are correctly attached. You could force "--vout=android-display" from LibVLC
     * arguments if you want to use this module without subtitles. Otherwise, the "opengles2"
     * module will be used (for SW and HW decoding) and this callback will always send a size of
     * 0.
     *
     * @param vlcVout       vlcVout
     * @param width         Frame width
     * @param height        Frame height
     * @param visibleWidth  Visible frame width
     * @param visibleHeight Visible frame height
     * @param sarNum        Surface aspect ratio numerator
     * @param sarDen        Surface aspect ratio denominator
     */
    //   @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        // store video size
        videoWidth = width;
        videoHeight = height;
        videoVisibleWidth = visibleWidth;
        videoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;
        Toast.makeText(mContext, "videoWidth " + width + "visibleWidth " + visibleWidth, Toast.LENGTH_LONG).show();
        vlcVout.setWindowSize(visibleWidth, visibleHeight);
        //  changeSurfaceLayout();
    }

}