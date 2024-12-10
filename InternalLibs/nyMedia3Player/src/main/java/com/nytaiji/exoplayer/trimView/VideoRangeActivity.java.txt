package com.nytaiji.exoplayer.trimView;


import static com.google.common.reflect.Reflection.getPackageName;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Rational;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.listener.OnPlayActionListener;
import com.nytaiji.core.R;
import com.nytaiji.exoplayer.exoview.nyEditVideoView;
import com.nytaiji.nybase.model.Constants;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.SystemUtils;


import java.util.ArrayList;

public class VideoRangeActivity extends AppCompatActivity {

    public static final String TAG = "FullFrameActivity";
    private BasePlayer basePlayer;
    protected ArrayList<NyVideo> videoList;
    private final int vIndex = 0;
    protected int renderType;
    protected int playerType;
    private final long vposition = 0L;
    private nyEditVideoView videoView;

    public static OnPlayActionListener onPlayActionListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //----
        SharedPreferences playerPrefs = getSharedPreferences(Constants.MAIN_SETTINGS, Context.MODE_PRIVATE);
        playerType = playerPrefs.getInt(Constants.KEY_PLAYER, 0);

        setContentView(R.layout.activity_edit_view);


        videoView = findViewById(R.id.adv_filter_view);
        //------------------

        //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        SystemUtils.hideSystemUI(this);
        SystemUtils.keepScreenOn(this);
        overlayCheck();
        videoView.setRange(0);   //0 is fullrange
        Intent intent = getIntent();
        Uri mUri = intent.getData();
        if (mUri != null) videoView.setVideoUrl(NyFileUtil.getPath(this, mUri));
    }

    private void overlayCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Constants.REQUEST_DRAWOVERLAYS_CODE);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SystemUtils.hideNavKey(this);
        SystemUtils.hideSystemUI(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // videoView.release();
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
