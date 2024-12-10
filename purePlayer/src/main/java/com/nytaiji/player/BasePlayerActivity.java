package com.nytaiji.player;

import static com.nytaiji.core.base.BaseConstants.AUTO_FULLSCREEN_MODE;
import static com.nytaiji.exoplayer.GoogleExoPlayer.getGoogLeMimeType;
import static com.nytaiji.nybase.filePicker.MediaSelection.DEFAULT_MEDIA;
import static com.nytaiji.nybase.filePicker.MediaSelection.getStringValue;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getMessageHandler;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getPreferredServerUrl;
import static com.nytaiji.nybase.model.Constants.ANDROID_PLAYER;
import static com.nytaiji.nybase.model.Constants.FAN_PLAYER;
import static com.nytaiji.nybase.model.Constants.GOOGLE_PLAYER;
import static com.nytaiji.nybase.model.Constants.KEY_PLAYER;
import static com.nytaiji.nybase.model.Constants.VIDEO_INDEX;
import static com.nytaiji.nybase.model.Constants.VIDEO_LIST;
import static com.nytaiji.nybase.model.Constants.VLC_PLAYER;
import static com.nytaiji.nybase.utils.NyFileUtil.getExtension;
import static com.nytaiji.nybase.utils.NyMimeTypes.getMimeType;
import static com.nytaiji.nybase.utils.SystemUtils.hideSystemUI;
import static com.nytaiji.nybase.utils.SystemUtils.hideSystemUIAndActionBar;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.base.IMediaPlayer;
import com.nytaiji.core.base.PlayerConfig;
import com.nytaiji.core.listener.OnMediaPlayerStateListener;
import com.nytaiji.core.player.AndroidPlayer;
import com.nytaiji.nybase.httpShare.WifiShareUtil;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.core.view.AdvancedVideoFrame;
import com.nytaiji.core.view.FloatVideoManager;
import com.nytaiji.exoplayer.GoogleExoPlayer;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.NyMimeTypes;
import com.nytaiji.nybase.utils.PreferenceHelper;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.nybase.view.SizeObservedRelativeLayout;
import com.nytaiji.vlcplayer.RenderersDialog;
import com.nytaiji.vlcplayer.VlcMediaPlayer;
import com.rockcarry.fanplayer.FanPlayer;


import org.jetbrains.annotations.NotNull;
import org.videolan.libvlc.RendererItem;

import java.util.ArrayList;


public class BasePlayerActivity extends AppCompatActivity {

    private static final String TAG = "BasePlayerActivity";

    protected ArrayList<NyVideo> videoList;
    private int vIndex = 0;
    private AdvancedVideoFrame videoView;

    private Uri mUri = null;

    private String mUrl = null;

    private boolean isCasting = false;

    private androidx.mediarouter.app.MediaRouteButton castButton;

    private RendererItem defaultRenderer = null;

    private BasePlayer videoPlayer = null;

    private SessionManagerListener mSessionManagerListener;

    private ImageButton vlcCast;

    private CastContext mCastContext;

    private MediaItem mediaItem;

    private CastPlayer castPlayer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO  important to disable at starting
        // disconnectCaster();

        //先销毁小窗口
        if (FloatVideoManager.getInstance() != null) {
            FloatVideoManager.getInstance().closeVideoView();
        }
        //--------------------------------//
        setContentView(R.layout.activity_baseplayer);

        SizeObservedRelativeLayout root = findViewById(R.id.content);
        root.setOnSizeChangedListener(new SizeObservedRelativeLayout.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                videoPlayer.setDisplaySize(w, h);
            }
        });

        FrameLayout videoFrame = findViewById(R.id.advvideo_view);
        videoView = new AdvancedVideoFrame(this) {
            @Override
            public boolean handleBack() {
                release();
                destroy();
                finish();
                return true;
            }
        };

        videoFrame.addView(videoView);

        castButton = findViewById(R.id.cast_button);
        CastButtonFactory.setUpMediaRouteButton(this, castButton);
        vlcCast = findViewById(R.id.vlc_cast);
        vlcCast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCasting();
            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        SystemUtils.keepScreenOn(this);
        hideSystemUIAndActionBar(this);
        setUpPlayer();
        //-------------------------------------//
        intentHandle(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        intentHandle(intent);
    }

    private void intentHandle(Intent intent) {
        mUri = intent.getData();
        if (mUri != null) {
            Log.e(TAG, "--------------------mUri.getScheme()= " + mUri.getScheme());
            mUrl = NyFileUtil.getPath(this, mUri).replace("///", "://");
            Log.e(TAG, "------------------------mUrl = " + mUrl);

            if (!(videoPlayer instanceof GoogleExoPlayer) && mUrl.contains("_NY")) {
                String mediaStream = getPreferredServerUrl(false, true);
                // if isDefault is set true, no need the following line:
                //WifiShareUtil.stopHttpServer();
                if (mUri.getScheme() == null)
                    WifiShareUtil.httpShare(AppContextProvider.getAppContext(), new NyHybrid(Uri.parse("file://" + mUrl)), mediaStream, getMessageHandler(this.findViewById(R.id.content)));
                    //same effect as
                    //WifiShareUtil.httpShare(this, new NyHybrid(mUrl), mediaStream, getMessageHandler(this.findViewById(R.id.content)));
                else
                    WifiShareUtil.httpShare(AppContextProvider.getAppContext(), new NyHybrid(mUri), mediaStream, getMessageHandler(this.findViewById(R.id.content)));

                WifiShareUtil.setUnique(false);
                setVideo(Uri.parse(mediaStream));

            } else setVideo(mUri);

        } else {
            videoList = intent.getParcelableArrayListExtra(VIDEO_LIST);
            vIndex = intent.getIntExtra(VIDEO_INDEX, 0);
            mUrl = videoList.get(vIndex).path;
            mUri = Uri.parse(mUrl);
            setVideo(mUrl);
            //  Log.e(TAG, "---------videoList--mUrl = " + mUrl);
        }

        if (videoPlayer instanceof GoogleExoPlayer) {
            String mimeType = getGoogLeMimeType(this, mUrl);
            mediaItem = new MediaItem.Builder()
                    .setUri(mUri)
                    .setMediaMetadata(new MediaMetadata.Builder().setTitle(NyFileUtil.getLastSegmentFromString(mUrl)).build())
                    .setMimeType(mimeType)
                    .build();
        }
    }


    private void setUpPlayer() {
        /**/
        String player = PreferenceHelper.getInstance().getString(KEY_PLAYER, VLC_PLAYER);
        if (player.equals(VLC_PLAYER))
            videoPlayer = new VlcMediaPlayer(BasePlayerActivity.this);
        else if (player.equals(GOOGLE_PLAYER))
            videoPlayer = new GoogleExoPlayer(BasePlayerActivity.this);
        else if (player.equals(ANDROID_PLAYER))
            videoPlayer = new AndroidPlayer(BasePlayerActivity.this);
        else if (player.equals(FAN_PLAYER)) {
            videoPlayer = new FanPlayer(BasePlayerActivity.this, new OnMediaPlayerStateListener() {
                @Override
                public void onMediaReadyPlaying() {
                    videoView.setPlayerStatus(IMediaPlayer.STATE_PLAYING);
                    // videoView.startProgressTimer();
                    videoView.setTouchable(true);
                    // videoView.resetLockStatus();
                }

                @Override
                public void onMediaAutoCompletion() {
                    videoView.setPlayerStatus(IMediaPlayer.STATE_COMPLETED);
                }

                @Override
                public void onMediaLoadingComplete() {
                    videoView.setPlayerStatus(IMediaPlayer.STATE_PREPARED);
                    videoView.startProgressTimer();
                    videoView.toggleControlView();
                }
            });
        }
        Log.e(TAG, "----------------mUrl = " + mUrl);
        PlayerConfig fullConfig = new PlayerConfig.Builder()
                .fullScreenMode(AUTO_FULLSCREEN_MODE)
                .renderType(BaseConstants.RENDER_SURFACE_VIEW)
                .player(videoPlayer)
                .build();
        videoView.setPlayerConfig(fullConfig);

        if (videoPlayer instanceof VlcMediaPlayer)
            vlcCast.setVisibility(View.VISIBLE);
        else {
            castButton.setVisibility(View.VISIBLE);
            mCastContext = CastContext.getSharedInstance((Context) this);
            setUpCastListener();
            SessionManager sessionManager = mCastContext.getSessionManager();
            sessionManager.addSessionManagerListener(mSessionManagerListener, CastSession.class);
        }

        //TODO replace the instance of videoview
       /* if (videoView.getPlayer() instanceof nyFanPlayer) {
            videoView.setVolume(60);
            AdvancedVideoFrame advancedVideoFrame = (AdvancedVideoFrame) videoView;
            advancedVideoFrame = new AdvancedVideoFrame(this) {
                @Override
                public int getStreamVolume() {
                  //  if (videoView.getPlayer().getPlayerAudioManager() != null)
                        return videoView.getPlayer().getPlayerAudioManager().getStreamVolume();
                   // else return videoView.getPlayer().getVolume();
                }
            };

            ViewGroup parent = (ViewGroup) videoView.getParent();
            int index = parent.indexOfChild(videoView);
            parent.removeViewAt(index);
            parent.addView(advancedVideoFrame, index);
            videoView = advancedVideoFrame;
        }*/
    }

    private void toggleCasting() {
        if (isCasting) {
            // Stop casting and switch to local playback
            stopCasting();
        } else {
            // Start casting and switch to Chromecast playback
            // if (videoPlayer instanceof VlcMediaPlayer)
            startCasting();
            // else finish();
        }
    }

    private void startCasting() {
        if (videoPlayer instanceof VlcMediaPlayer) {
            if (defaultRenderer != null) showCasting();
            else {
              /*  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {*/
                RenderersDialog renderersDialog = new RenderersDialog();
                renderersDialog.show(getSupportFragmentManager(), "RenderersDialog");
                // Set the listener to get the selected renderer
                renderersDialog.setRenderersDialogListener(new RenderersDialog.RenderersDialogListener() {
                    @Override
                    public void onRendererSelected(RendererItem selectedRenderer) {
                        defaultRenderer = selectedRenderer;
                        showCasting();
                    }
                });
                  /*  }
                }, 200);*/
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SystemUtils.keepScreenOn(this);
    }

    private void showCasting() {
        isCasting = true;
        vlcCast.setImageResource(com.nytaiji.nybase.R.drawable.ic_cast_connected);
        if (videoPlayer instanceof VlcMediaPlayer) videoPlayer.setRenderer(defaultRenderer);
        // else if (videoPlayer instanceof GoogleExoPlayer) videoPlayer.setRenderer(isCasting);
    }

    private void stopCasting() {
        isCasting = false;
        vlcCast.setImageResource(com.nytaiji.nybase.R.drawable.ic_cast);
        if (videoPlayer instanceof VlcMediaPlayer) videoPlayer.setRenderer(null);
        //  else if (videoPlayer instanceof GoogleExoPlayer) videoPlayer.setRenderer(isCasting);

    }


    private void setVideo(Uri uri) {
        videoView.setVideoUri(uri);
        videoView.start();
        //  if (!(videoPlayer instanceof FanPlayer))
        // videoPlayer.setVolume((int) (0.6 * videoPlayer.getStreamMaxVolume()));
        // else videoPlayer.setVolume(10);
    }

    private void setVideo(String url) {
        videoView.setVideoUrl(url);
        videoView.start();
        //set 60% maximun volume
        if (!(videoPlayer instanceof FanPlayer))
            videoPlayer.setVolume((int) (0.6 * videoPlayer.getStreamMaxVolume()));
        else videoPlayer.setVolume(10);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SystemUtils.hideNavKey(this);
        hideSystemUI(this);
    }


    @Override
    protected void onDestroy() {
        WifiShareUtil.stopHttpServer();
        videoView.release();
        videoView.destroy();
        videoView = null;
        videoPlayer.destroy();
        videoPlayer = null;
        super.onDestroy();
    }

    //----------------For PiP----------------
    boolean isInPipMode = false;
    boolean isPIPModeeEnabled = true; //Has the user disabled PIP mode in AppOpps?

    //----------internal entries, use either one of them--------------

    @Override
    public void enterPictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                && isPIPModeeEnabled) {
            enterPIPMode();  //this can not be accessed outside the activity
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        isInPipMode = !isInPictureInPictureMode;
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    //Called when the user touches the Home or Recents button to leave the app.
    @Override
    protected void onUserLeaveHint() {

    }


    public void enterPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Rational videoAspectRatio = new Rational(videoView.getWidth(), videoView.getHeight());
                PictureInPictureParams.Builder params = new PictureInPictureParams.Builder().setAspectRatio(videoAspectRatio);
                this.enterPictureInPictureMode(params.build());
            } else {
                this.enterPictureInPictureMode();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPIPPermission();
                }
            }, 30);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public void checkPIPPermission() {
        isPIPModeeEnabled = isInPictureInPictureMode();
        if (!isInPictureInPictureMode()) {
            Log.i("Pip", "Permission error");
            onBackPressed();
        }
    }


    private final void setUpCastListener() {
        this.mSessionManagerListener = (SessionManagerListener) (new SessionManagerListener() {
            private final void onApplicationConnected(CastSession castSession) {
                if (videoPlayer instanceof GoogleExoPlayer) {
                    // Initialize CastPlayer
                    castPlayer = new CastPlayer(mCastContext);
                    castPlayer.addMediaItem(mediaItem);
                    castPlayer.prepare();
                  /*  castPlayer.setSessionAvailabilityListener(new SessionAvailabilityListener() {
                        @Override
                        public void onCastSessionAvailable() {
                            // Switch to CastPlayer when a cast session is available
                            videoPlayer.pause();
                            castPlayer.play();
                            // }
                        }


                        @Override
                        public void onCastSessionUnavailable() {
                            // Switch back to ExoPlayer when the cast session is unavailable
                            if (castPlayer.isPlaying()) {
                                castPlayer.pause();
                                videoPlayer.play();
                            }
                        }
                    });*/
                } else if (videoPlayer instanceof VlcMediaPlayer) {
                } else finish();
                //  Log.e(TAG, "onApplicationConnected");
            }

            private final void onApplicationDisconnected() {
                // mCastSession = null;
                // NyPlayActivity.this.invalidateOptionsMenu();
                Log.e(TAG, "onApplicationDisconnected");
            }

            public void onSessionStarted(@NotNull CastSession p0, @NotNull String p1) {
                this.onApplicationConnected(p0);
                if (videoPlayer != null) videoPlayer.pause();
                castPlayer.play();
                Log.e(TAG, "onSessionStarted");
            }

            // $FF: synthetic method
            // $FF: bridge method
            public void onSessionStarted(Session var1, String var2) {
                this.onSessionStarted((CastSession) var1, var2);
                if (videoPlayer != null) videoPlayer.pause();
                castPlayer.play();
            }

            public void onSessionResumeFailed(@NotNull CastSession p0, int p1) {
                this.onApplicationDisconnected();
                Log.e(TAG, "onSessionResumeFailed");
            }

            // $FF: synthetic method
            // $FF: bridge method
            public void onSessionResumeFailed(Session var1, int var2) {
                this.onSessionResumeFailed((CastSession) var1, var2);
            }

            public void onSessionEnded(@NotNull CastSession p0, int p1) {
                this.onApplicationDisconnected();
                if (videoPlayer != null) videoPlayer.play();
                castPlayer.pause();
                Log.e(TAG, "onSessionEnded");
            }

            // $FF: synthetic method
            // $FF: bridge method
            public void onSessionEnded(Session var1, int var2) {
                this.onSessionEnded((CastSession) var1, var2);
            }

            public void onSessionResumed(@NotNull CastSession p0, boolean p1) {
                this.onApplicationConnected(p0);
                Log.e(TAG, "onSessionResumed");
            }

            // $FF: synthetic method
            // $FF: bridge method
            public void onSessionResumed(Session var1, boolean var2) {
                this.onSessionResumed((CastSession) var1, var2);
            }

            public void onSessionStartFailed(@NotNull CastSession p0, int p1) {
                this.onApplicationDisconnected();
            }

            public void onSessionStartFailed(Session var1, int var2) {
                this.onSessionStartFailed((CastSession) var1, var2);
            }

            public void onSessionSuspended(@NotNull CastSession p0, int p1) {
                Log.e(TAG, "onSessionSuspended");
            }

            public void onSessionSuspended(Session var1, int var2) {
                this.onSessionSuspended((CastSession) var1, var2);
            }

            public void onSessionStarting(@NotNull CastSession p0) {
            }

            public void onSessionStarting(Session var1) {
                this.onSessionStarting((CastSession) var1);
            }

            public void onSessionResuming(@NotNull CastSession p0, @NotNull String p1) {
            }

            public void onSessionResuming(Session var1, String var2) {
                this.onSessionResuming((CastSession) var1, var2);
            }

            public void onSessionEnding(@NotNull CastSession p0) {
            }

            public void onSessionEnding(Session var1) {
                this.onSessionEnding((CastSession) var1);
            }
        });
    }

}
