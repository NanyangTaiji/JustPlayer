package com.nytaiji.core;


import static com.nytaiji.nybase.model.Constants.KEY_RENDER;
import static com.nytaiji.nybase.model.Constants.MAIN_SETTINGS;
import static com.nytaiji.nybase.model.Constants.VIDEO_INDEX;
import static com.nytaiji.nybase.model.Constants.VIDEO_LIST;
import static com.nytaiji.nybase.model.Constants.VIDEO_POSITION;
import static com.nytaiji.nybase.utils.SystemUtils.hideSystemUI;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.core.player.AndroidPlayer;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.core.view.DualVideosFrame;
import com.nytaiji.core.view.FloatVideoManager;

import java.util.ArrayList;


//no DualMediaService
public class DualFrameActivity extends AppCompatActivity {
    //no service
    public static final String TAG = "DualFrameActivity";

    protected ArrayList<NyVideo> videoList;
    private int vIndex = 0;
    protected int renderType;
    protected int playerType;
    private long vposition = 0L;
    private DualVideosFrame dualVideo;
    private BasePlayer fullPlayer;
    private BasePlayer floatPlayer;

    private Uri uri=null;
    //  public static OnPlayActionListener onPlayActionListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri uri=intent.getData();
        if (uri==null) {
            videoList = intent.getParcelableArrayListExtra(VIDEO_LIST);
            if (videoList != null) {
                // We receive the position of the video selected in the playlist or the list of videos
                vIndex = intent.getIntExtra(VIDEO_INDEX, 0);
                vposition = intent.getLongExtra(VIDEO_POSITION, 0L);
            }
        }
        //先销毁小窗口
        if (FloatVideoManager.getInstance() != null) {
            FloatVideoManager.getInstance().closeVideoView();
        }

        //----
        SharedPreferences playerPrefs = getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_dual_frame);

        FrameLayout duallVideoContainer = findViewById(R.id.dualvideo_container);

        setDualPlayers();

        dualVideo = new DualVideosFrame(this, fullPlayer, floatPlayer) {
            @Override
            public void enterSystemPIP() {
                enterPIPMode();
            }

          /*  @Override
            public void playVideo(int vIndex) {
                super.playVideo(vIndex);

            }
            @Override
            public void playVideoUri(Uri uri) {
                super.playVideoUri(uri);
            }*/

        };

        if (videoList!=null) {
            dualVideo.attachList(videoList);
            dualVideo.playVideo(vIndex);
        }  else
            dualVideo.playVideoUri(uri);


        renderType = playerPrefs.getInt(KEY_RENDER, 1);  //
        if (playerType == 2) {//VLC
            renderType = 1;//SURFACEVIEW
        } else renderType = playerPrefs.getInt(KEY_RENDER, 0);//TEXTUREVIEW

        dualVideo.setRenderType(renderType);
        duallVideoContainer.addView(dualVideo);
        //------------------
        SystemUtils.keepScreenOn(this);
    }

    private void setDualPlayers() {
        if (fullPlayer != null) {
            fullPlayer.release();
            fullPlayer = null;
        }
        if (floatPlayer != null) {
            floatPlayer.release();
            floatPlayer = null;
        }

     /*   if (isSpecialMedia(url) || isOnline(url)) {
            fullPlayer = new IjkPlayer(this);
            floatPlayer = new IjkPlayer(this);
        } else {*/
        fullPlayer = new AndroidPlayer(this);
        floatPlayer = new AndroidPlayer(this);
        //   }
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
        dualVideo.destroy();
        finish();
    }

    //----------------For PiP----------------
    boolean isInPipMode = false;
    boolean isPIPModeeEnabled = true; //Has the user disabled PIP mode in AppOpps?

    //----------internal entries, use either one of them--------------

    @Override
    public void enterPictureInPictureMode() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) && isPIPModeeEnabled) {
            enterPIPMode();  //this can not be accessed outside the activity
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {

        isInPipMode = !isInPictureInPictureMode;
        dualVideo.pipExtraHandle(isInPictureInPictureMode);

        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    //Called when the user touches the Home or Recents button to leave the app.
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // enterPIPMode();
    }


    public void enterPIPMode() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            Rational videoAspectRatio = new Rational(dualVideo.getVideoWidth(), dualVideo.getVideoHeight());
            PictureInPictureParams.Builder params = new PictureInPictureParams.Builder().setAspectRatio(videoAspectRatio);
            this.enterPictureInPictureMode(params.build());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPIPPermission();
                }
            }, 30);
        }
    }

    public void checkPIPPermission() {
        isPIPModeeEnabled = isInPictureInPictureMode();
        if (!isInPictureInPictureMode()) {
            // Log.i("Pip", "Permission error");
            onBackPressed();
        }
    }

}
