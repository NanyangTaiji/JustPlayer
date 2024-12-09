package com.nytaiji.nybase;

import static com.nytaiji.nybase.httpShare.WifiShareUtil.getMessageHandler;
import static com.nytaiji.nybase.network.NetworkServersDialog.getServerLink;
import static com.nytaiji.nybase.network.NetworkServersDialog.stopServer;
import static com.nytaiji.nybase.utils.NyFileUtil.WEBVIEW_EXT;
import static com.nytaiji.nybase.utils.NyFileUtil.isAudio;
import static com.nytaiji.nybase.utils.NyFileUtil.isVideo;
import static com.nytaiji.nybase.utils.NyFileUtil.launchActivity;
import static com.nytaiji.nybase.utils.SystemUtils.dp2px;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.cast.framework.CastButtonFactory;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.NyMimeTypes;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.nybase.view.GestureWebView;
import com.nytaiji.nybase.R;

import java.util.List;


/**
 * <activity
 * android:name="com.nytaiji.nybase.WebViewActivity"
 * android:theme="@style/Theme.AppCompat.FullScreen"
 * android:configChanges="keyboardHidden|screenSize|smallestScreenSize|screenLayout|orientation"
 * android:supportsPictureInPicture="true"
 * android:screenOrientation="fullSensor" />
 */

public class WebViewActivity extends AppCompatActivity {

    private static String TAG = "WebViewActivity";

    private GestureWebView mWebView;
    private boolean isCasting = false;
    private ImageButton vlcCast;
    private androidx.mediarouter.app.MediaRouteButton castButton;

    //  private static boolean isInPip = false;

    public static void webDisplayUrl(Context context, String title, String link) {
        webDisplayUrl(context, title, link, false, false);
    }

    public static void webDisplayUrl(Context context, String title, String link, boolean toPip, boolean newTask) {
        boolean display = NyFileUtil.isOnline(link);
        String ext = NyFileUtil.getFileExtension(link.toLowerCase());
        if (ext != null) display = display || WEBVIEW_EXT.contains(ext);
        if (display) {
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("link", link);
            args.putBoolean("toPip", toPip);

            // Use Intent to launch the activity
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtras(args);
            // Add flags to clear the current task and start a new one
            if (newTask)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else {
            Uri uri = Uri.parse(link);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, NyMimeTypes.getMimeTypeFromPath(title));
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
            if (resInfos != null && resInfos.size() > 0)
                context.startActivity(intent);
            else
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "No apps can handle this file", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(initPageLayoutID());
        castButton = findViewById(R.id.cast_button);
        CastButtonFactory.setUpMediaRouteButton(this, castButton);
        vlcCast = findViewById(R.id.vlc_cast);
        //TODO
        vlcCast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCasting();
            }
        });

        SystemUtils.hideSystemUI(WebViewActivity.this);
        SystemUtils.hideSupportActionBar(WebViewActivity.this);
        initPageView();
    }


    protected int initPageLayoutID() {
        return R.layout.activity_webview;
    }

    public void initPageView() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String link = intent.getStringExtra("link");
        boolean toPip = intent.getBooleanExtra("toPip", false);

        String routedLink = link;
        Log.e(TAG, "Link = " + link);
        if (link.contains("_NY") && !link.contains("http"))
            routedLink = getServerLink(new NyHybrid(link), getMessageHandler(findViewById(R.id.content)));

        // Clean up the existing WebView
        if (mWebView != null) {
            mWebView.onPause();
            mWebView.destroy();
            mWebView = null;
        }
        mWebView = findViewById(R.id.web_view);
        mWebView.setTitleText(title);
        mWebView.setTitleText(title);
        if (!isVideo(link) && !isAudio(link)) mWebView.enablseGesture(false);

        mWebView.loadUrl(routedLink);
       /* String htmlContent = "<html><head><style>"
                + "body, html { height: 100%; margin: 0; }"
                + "video { width: 100%; height: auto; }"  // Adjusted video styling
                + "</style><script>"
                + "</script></head><body>"
                + "<video id='video' controls autoplay>"
                + "<source src='" + routedLink + "' type='" + "video/mp4" + "'>"
                + "</video>"
                + "</body></html>";

        mWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
        */

        if (toPip) onBackPressed();
    }


   /* private void exitPipMode() {
        if (isInPip) {
            Log.d("WebViewActivity", "Exiting PiP mode");
            this.moveTaskToBack(true);
            this.finish();
            isInPip = false;
        }
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //防止webView内存泄漏
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        stopServer();
    }


    @Override
    public void onBackPressed() {
        //  if (mWebView.loading)
        //mWebView.hideTitle();
        enterPIPMode();
        // else super.onBackPressed();
    }

    //Called when the user touches the Home or Recents button to leave the app.
    //  @Override
    //  protected void onUserLeaveHint() {
    //  super.onUserLeaveHint(); //no difference
    //  enterPIPMode();
    //no effect if in androidManifest, the activity is tagged with
    // android:excludeFromRecents="true"
    //  }

    //----------------For PiP----------------

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        //TODO ny

        mWebView.setTitleVisible(!isInPictureInPictureMode);
    }


    public void enterPIPMode() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            // Rational videoAspectRatio = new Rational(getVideoWidth(), getVideoHeight());
            Rational videoAspectRatio = new Rational(16, 9);
            PictureInPictureParams.Builder params = new PictureInPictureParams.Builder().setAspectRatio(videoAspectRatio);
            //isInPip = true;
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
        if (!isInPictureInPictureMode()) {
            Log.i("Pip", "Permission error");
            finish();
        }
    }


    private void toggleCasting() {
        if (isCasting) {
            // Stop casting and switch to local playback
            stopCasting();
        } else {
            // Start casting and switch to Chromecast playback
            startCasting();
        }
    }

    private void startCasting() {
        isCasting = true;
        vlcCast.setImageResource(com.nytaiji.nybase.R.drawable.ic_cast_connected);
    }

    private void stopCasting() {
        isCasting = false;
        vlcCast.setImageResource(com.nytaiji.nybase.R.drawable.ic_cast);
    }
}