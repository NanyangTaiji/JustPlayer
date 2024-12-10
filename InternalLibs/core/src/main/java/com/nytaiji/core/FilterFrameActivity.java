package com.nytaiji.core;

import static com.nytaiji.core.base.BaseConstants.AUTO_FULLSCREEN_MODE;
import static com.nytaiji.core.base.BaseConstants.RENDER_GLSURFACE_VIEW;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.decodeUrl;
import static com.nytaiji.nybase.model.Constants.REQUEST_DRAWOVERLAYS_CODE;
import static com.nytaiji.nybase.model.Constants.VIDEO_INDEX;
import static com.nytaiji.nybase.model.Constants.VIDEO_LIST;
import static com.nytaiji.nybase.utils.SystemUtils.hideSystemUI;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Rational;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.base.PlayerConfig;
import com.nytaiji.core.listener.OnPlayActionListener;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.core.player.SingletonPlayer;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.core.view.FilterVideoFrame;

import java.util.ArrayList;

public class FilterFrameActivity extends AppCompatActivity {

    public static final String TAG = "FullFrameActivity";
    private BasePlayer basePlayer;
    protected ArrayList<NyVideo> videoList;
    private int vIndex = 0;
    protected int renderType;
    protected int playerType;
    private final long vposition = 0L;
    private FilterVideoFrame videoView;

    public static OnPlayActionListener onPlayActionListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //----
        // SharedPreferences playerPrefs = getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);
        // playerType = playerPrefs.getInt(KEY_PLAYER, 0);

        setContentView(R.layout.activity_adv_view);


        videoView = findViewById(R.id.adv_filter_frame);
        //------------------

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        hideSystemUI(this);
        SystemUtils.keepScreenOn(this);
        overlayCheck();

        Intent intent = getIntent();
        Uri mUri = intent.getData();
        String mUrl = null;
        if (mUri != null) mUrl = NyFileUtil.getPath(this, mUri);
        else {
            videoList = intent.getParcelableArrayListExtra(VIDEO_LIST);
            vIndex = intent.getIntExtra(VIDEO_INDEX, 0);
            videoView.setVideoList(videoList);
            mUrl = videoList.get(vIndex).path;
        }
        mUrl = decodeUrl(mUrl);
        PlayerConfig fullConfig = new PlayerConfig.Builder()
                .fullScreenMode(AUTO_FULLSCREEN_MODE)
                .renderType(RENDER_GLSURFACE_VIEW)
                .player(SingletonPlayer.getInstance(getApplicationContext()).getMediaPlayer(mUrl))
                .build();
        videoView.setPlayerConfig(fullConfig);
        videoView.setVideoUrl(mUrl);
        videoView.start();
    }

    private void overlayCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_DRAWOVERLAYS_CODE);
            }
        }
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
        videoView.release();
        videoView.destroy();
        videoView = null;
        finish();
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
        //  if (newConfig != null) {
        isInPipMode = !isInPictureInPictureMode;
        //    videoView.pipExtraHandle(isInPictureInPictureMode);
        //   }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    //Called when the user touches the Home or Recents button to leave the app.
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPIPMode();
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

}
