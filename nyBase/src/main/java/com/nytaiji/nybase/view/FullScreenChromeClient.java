package com.nytaiji.nybase.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class FullScreenChromeClient extends WebChromeClient {

    //TODO 2023-4-1 add FullScreenVideoWebView functions


    private Context context;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private int mOriginalSystemUiVisibility;

    private View extraView;

    private ProgressView progressView;

    public FullScreenChromeClient(Context context, ProgressView progressView, View extraView) {
        this.context = context;
        this.progressView = progressView;
        this.extraView = extraView;
    }

      /*  public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(context.getResources(), 2130837573);
        }*/


    public void onHideCustomView() {
        ((FrameLayout) ((Activity) context).getWindow().getDecorView()).removeView(this.mCustomView);
        this.mCustomView = null;
        ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
        int orientation = ((Activity) context).getResources().getConfiguration().orientation;
        ((Activity) context).setRequestedOrientation(orientation);
        this.mCustomViewCallback.onCustomViewHidden();
        this.mCustomViewCallback = null;
    }


    public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
        if (this.mCustomView != null) {
            onHideCustomView();
            return;
        }
        View windowView = ((Activity) context).getWindow().getDecorView();
        this.mCustomView = paramView;
        this.mOriginalSystemUiVisibility = windowView.getSystemUiVisibility();
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.mCustomViewCallback = paramCustomViewCallback;
        ((FrameLayout) windowView).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));

        //TODO ny 2023-6-26 no effect
        if (extraView != null)
            ((FrameLayout) windowView).addView(extraView, new FrameLayout.LayoutParams(-1, -1));

        windowView.setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
            //加载完毕进度条消失
            if (progressView != null) progressView.setVisibility(View.GONE);
        } else {
            //更新进度
            if (progressView != null) progressView.setProgress(newProgress);
        }
        super.onProgressChanged(view, newProgress);
    }

}
