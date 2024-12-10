package com.nytaiji.core;


import static com.nytaiji.core.base.BaseConstants.RENDER_GLSURFACE_VIEW;
import static com.nytaiji.nybase.model.Constants.MAIN_SETTINGS;
import static com.nytaiji.nybase.model.Constants.VIDEO_INDEX;
import static com.nytaiji.nybase.model.Constants.VIDEO_LIST;
import static com.nytaiji.nybase.model.Constants.VIDEO_POSITION;
import static com.nytaiji.nybase.utils.NyFileUtil.isSpecialMedia;
import static com.nytaiji.nybase.utils.SystemUtils.hideSystemUI;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.Rational;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnPlayActionListener;
import com.nytaiji.core.listener.OnStateChangedListener;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.core.player.AndroidPlayer;
import com.nytaiji.core.player.SingletonPlayer;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.core.view.DualVideosFrame;
import com.nytaiji.core.view.FloatVideoManager;

import java.util.ArrayList;


public class DualVideosActivity extends AppCompatActivity {

    public static final String TAG = "DualVideosActivity";
    private static ArrayList<NyVideo> videoList;
    public static int vIndex = 0;
    protected int renderType;
    protected int playerType;
    private long vposition = 0L;
    private DualVideosFrame dualVideo;
    private BasePlayer fullPlayer;
    private BasePlayer floatPlayer;

    private Intent playIntent;
    public static DualMediaService mediaService;
    private boolean serviceBound = false;

    public static OnPlayActionListener onPlayActionListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //先销毁小窗口
        if (FloatVideoManager.getInstance() != null) {
            // FloatVideoManager.getInstance().destroyVideoView();
            FloatVideoManager.getInstance().closeVideoView();
        }

        getIntentNbindService();

        //----
        setupView();

        //------------------

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        //  SystemUtils.hideSystemUI(this);
        SystemUtils.keepScreenOn(this);
        //the following listener is from service
        onPlayActionListener = new OnPlayActionListener() {
            @Override
            public void start() {
                //mediaService.notifyResume();
                dualVideo.start();
            }

            @Override
            public void pause() {
                //  mediaService.notifyPause();
                dualVideo.pause();
            }

            @Override
            public void replay() {
                dualVideo.replay();
            }

            @Override
            public void release() {
                dualVideo.release();
            }

            @Override
            public void onPositionChanged(long position) {

            }

            @Override
            public void onSpeedChanged(float speed) {

            }

            @Override
            public void onMuteChanged() {

            }

            @Override
            public void onScaleChanged(BaseConstants.ScaleType scaleType) {

            }

            @Override
            public void onIndexChanged(int vIndex) {
                if (DualVideosActivity.vIndex != vIndex) {
                    DualVideosActivity.vIndex = vIndex;
                    dualVideo.playVideo(vIndex);
                }
            }
        };

    }

    //---------------------------------------

    private void getIntentNbindService() {
        Intent intent = getIntent();
        videoList = intent.getParcelableArrayListExtra(VIDEO_LIST);
        if (videoList != null) {
            // We receive the position of the video selected in the playlist or the list of videos
            vIndex = intent.getIntExtra(VIDEO_INDEX, 0);
            vposition = intent.getLongExtra(VIDEO_POSITION, 0L);
        }
        if (mediaService != null) {
            mediaService.stopNotifications();
            mediaService.onExit();
        }
        bindMediaService();
    }

    public void bindMediaService() {
        if (playIntent == null) {
            playIntent = new Intent(getApplicationContext(), DualMediaService.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(VIDEO_LIST, videoList);
            bundle.putInt(VIDEO_INDEX, vIndex);
            // bundle.putSerializable(ACTION_LISTENER,onPlayActionListener);
            playIntent.putExtras(bundle);
            startService(playIntent); // starting the service and then binding it - so it will continue running after closing app
            bindService(playIntent, serviceConnection, Context.BIND_ABOVE_CLIENT);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DualMediaService.serviceBinder binder = (DualMediaService.serviceBinder) service;
            //get service
            mediaService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    void doUnbindService() {
        if (serviceBound) {
            // Release information about the service's state
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }
    //--------------Setup Videoview--------------------------//

    private void setupView() {
        setContentView(R.layout.activity_dual_frame);
        FrameLayout duallVideoContainer = findViewById(R.id.dualvideo_container);

        dualVideo = new DualVideosFrame(this, fullPlayer, floatPlayer) {
            //  @Override
            public void enterSystemPIP() {
                enterPIPMode();
            }

            @Override
            public void playVideo(int current) {
                if (vIndex != current) {
                    vIndex = current;
                    mediaService.playVideo(vIndex);
                }
                setDualPlayers();
                super.playVideo(current);

             /*  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enterPIP();
                    }
                }, 1000);*/
            }
        };

        SharedPreferences playerPrefs = getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);

    /*renderType = playerPrefs.getInt(KEY_RENDER, 1);  //
    if (playerType == 2) {//VLC
      renderType = 1;//SURFACEVIEW
    } else renderType = playerPrefs.getInt(KEY_RENDER, 0);//TEXTUREVIEW*/
        renderType = RENDER_GLSURFACE_VIEW;
        dualVideo.setRenderType(renderType);

        //   Collections.shuffle(videoList, new Random());
        dualVideo.attachList(videoList);
        dualVideo.playVideo(vIndex);
        dualVideo.setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void onStateChange(int state) {
                switch (state) {
                    case BasePlayer.STATE_PLAYING:
                        if (mediaService != null) mediaService.notifyResume();
                        break;
                    case BasePlayer.STATE_PAUSED:
                        if (mediaService != null) mediaService.notifyPause();
                        break;
                }
            }
        });

        duallVideoContainer.addView(dualVideo);
    }

    //--------------Setup players----------------------//

    private void setDualPlayers() {
        if (fullPlayer != null) {
            fullPlayer.release();
            fullPlayer = null;
        }

        String fullPath = videoList.get(vIndex).getPath();

       // fullPlayer = SingletonPlayer.getInstance(getApplicationContext()).getMediaPlayer(fullPath);
        fullPlayer = SingletonPlayer.getInstance(getApplicationContext()).getMediaPlayer(fullPath);
        dualVideo.setFullPayer(fullPlayer);

        if (floatPlayer != null) {
            floatPlayer.release();
            floatPlayer = null;
        }

        if (isSpecialMedia(fullPath)) {
            floatPlayer = new AndroidPlayer(this);
        } else {
            floatPlayer = new AndroidPlayer(this);
        }
        dualVideo.setFloatPayer(floatPlayer);

    }

    public static ArrayList<NyVideo> getMediaList() {
        return videoList;
    }

    public static int getCurrentIndex() {
        return vIndex;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SystemUtils.hideNavKey(this);
        hideSystemUI(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaService != null) {
            doUnbindService();
        }
        dualVideo.release();
        dualVideo.destroy();
        dualVideo = null;
        if (onPlayActionListener != null) onPlayActionListener = null;
        finish();
    }

    //--------------------------


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
        //  if (newConfig != null) {
        isInPipMode = !isInPictureInPictureMode;
        dualVideo.pipExtraHandle(isInPictureInPictureMode);
        //   }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    //Called when the user touches the Home or Recents button to leave the app.
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // enterPIPMode();
    }


    public void enterPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Rational videoAspectRatio = new Rational(dualVideo.getVideoWidth(), dualVideo.getVideoHeight());
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

}
