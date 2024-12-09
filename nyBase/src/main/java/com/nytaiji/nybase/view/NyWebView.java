/*
 * Created on 2023-3-9 9:18:59 pm.
 * Copyright © 2023 刘振林. All rights reserved.
 */

package com.nytaiji.nybase.view;

import static com.nytaiji.nybase.utils.SystemUtils.dp2px;
import static com.nytaiji.nybase.utils.SystemUtils.getCurrentOrientation;
import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.nytaiji.nybase.utils.OnOrientationChangeListener;
import com.nytaiji.nybase.utils.Regex;
import com.nytaiji.nybase.utils.SystemUtils;


public class NyWebView extends WebView {

    protected final Context mContext;
    private ProgressView progressView;

    private TextView titleView;
    public boolean loading = false;

    private String title = "";

    public void setTitleText(String title) {
        this.title = title;
        titleView.setText(title);
    }

    public NyWebView(Context context) {
        this(context, null);
    }

    public NyWebView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public NyWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private OnOrientationChangeListener mOnOrientationChangeListener;
    public void init() {
        //把进度条加到WebView中
        progressView = new ProgressView(mContext);
        progressView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mContext, 4)));
        progressView.setColor(Color.RED);
        progressView.setProgress(10);
        addView(progressView);
        //
        titleView = new TextView(mContext);
        titleView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        titleView.setTextSize(dp2px(mContext, 12));
        titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        titleView.setSingleLine(true);
        titleView.setMarqueeRepeatLimit(-1);
        titleView.setSelected(true);
        titleView.setText(title);
        addView(titleView);
        //=============//
        setWebSettings(getSettings());
        setUserAgent(getSettings());
        setWebViewClient(nyWebViewClient());
        setWebChromeClient(nyWebChromeClient());

        //------------//
        mOnOrientationChangeListener = new OnOrientationChangeListener(mContext) {
            @Override
            protected void onOrientationChange(int orientation) {
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    titleView.setVisibility(View.GONE);
                    SystemUtils.hideSystemUI(mContext);
                } else {
                    titleView.setVisibility(View.VISIBLE);
                    SystemUtils.showNavigationBar(mContext);
                }
            }
        };


        mOnOrientationChangeListener.setOrientation(getCurrentOrientation(mContext));
        mOnOrientationChangeListener.setEnabled(true);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = super.onTouchEvent(event);
        if (consumed && event.getAction() == MotionEvent.ACTION_DOWN
                && getScrollY() == 0) {
            // Avoids touch conflicts between this view and SwipeRefreshLayout, where the scrollY
            // is 0 but this view is actually scrolling a child panel up.
            setScrollY(1);
        }
        return consumed;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            titleView.setVisibility(View.GONE);
            if (canGoBack()) {
                goBack();
                return true;
            } else {
                ((Activity) mContext).onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setTitleVisible(boolean trueOrnot) {
        if (titleView != null)
            titleView.setVisibility(trueOrnot ? VISIBLE : INVISIBLE);
    }


    @Override
    public void destroy() {
        loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        clearHistory();
        ((ViewGroup) getParent()).removeView(this);
        mOnOrientationChangeListener.release();
        super.destroy();
    }


    @SuppressLint("SetJavaScriptEnabled")
    protected void setWebSettings(WebSettings webSetting) {
        // 如果访问的页面中要与Javascript交互，则WebView必须设置支持JavaScript
        webSetting.setJavaScriptEnabled(true);
        //  WebSettings.LOAD_DEFAULT 如果本地缓存可用且没有过期则使用本地缓存，否加载网络数据 默认值
        //  WebSettings.LOAD_CACHE_ELSE_NETWORK 优先加载本地缓存数据，无论缓存是否过期
        //  WebSettings.LOAD_NO_CACHE  只加载网络数据，不加载本地缓存
        //  WebSettings.LOAD_CACHE_ONLY 只加载缓存数据，不加载网络数据
        //Tips:有网络可以使用LOAD_DEFAULT 没有网时用LOAD_CACHE_ELSE_NETWORK
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //   webSetting.setAppCacheEnabled(true);    //开启H5(APPCache)缓存功能websettings.setAppCacheMaxSize(1024*1024*8);
        //   String appCachePath = this.getApplicationContext().getCacheDir().getAbsolutePath();
        //   webSetting.setAppCachePath(appCachePath);
        //开启 DOM storage API 功能 较大存储空间，使用简单
        webSetting.setDomStorageEnabled(true);

        //支持通过JS打开新窗口
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);

        // 设置自适应屏幕，两者合用
        webSetting.setUseWideViewPort(true); // 将图片调整到适合WebView的大小
        webSetting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        webSetting.setLoadsImagesAutomatically(true);
        webSetting.setDefaultTextEncodingName("UTF-8");
        webSetting.setMediaPlaybackRequiresUserGesture(false);

        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //允许 WebView 使用 File 协议
        webSetting.setAllowFileAccess(true);
        //允许webview对文件的操作
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setAllowFileAccessFromFileURLs(true);
        //缩放操作
        webSetting.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSetting.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSetting.setDisplayZoomControls(false); //隐藏原生的缩放控件

        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

    }

    protected void setUserAgent(WebSettings webSetting) {
        webSetting.setUserAgentString(UserAgent.getUa(webSetting));
    }

    protected WebChromeClient nyWebChromeClient() {
        return new FullScreenChromeClient(mContext, progressView, getExtraView());
    }

    protected View getExtraView(){
        return null;
    }


    protected WebViewClient nyWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                loading = false;
            }
        };
    }

    // Copied from Fermata
    public static final class UserAgent {
        private UserAgent() {
        }

        private static final String REGEX = ".+ AppleWebKit/(\\S+) .+ Chrome/(\\S+) .+";
        @Nullable
        static String ua;
        @Nullable
        static String uaDesktop;

        private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android {ANDROID_VERSION}) "
                + "AppleWebKit/{WEBKIT_VERSION} (KHTML, like Gecko) "
                + "Chrome/{CHROME_VERSION} Mobile Safari/{WEBKIT_VERSION}";
        private static final String USER_AGENT_DESKTOP = "Mozilla/5.0 (X11; Linux x86_64) "
                + "AppleWebKit/{WEBKIT_VERSION} (KHTML, like Gecko) "
                + "Chrome/{CHROME_VERSION} Safari/{WEBKIT_VERSION}";

        public static String getUa(WebSettings s) {
            if (ua != null) return ua;

            String ua = s.getUserAgentString();
            Regex regex = new Regex(REGEX);
            if (regex.matches(ua)) {
                String av;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    av = Build.VERSION.RELEASE_OR_CODENAME;
//                } else {
                av = Build.VERSION.RELEASE;
//                }
                String wv = requireNonNull(regex.group(1));
                String cv = requireNonNull(regex.group(2));
                UserAgent.ua =
                        USER_AGENT.replace("{ANDROID_VERSION}", av)
                                .replace("{WEBKIT_VERSION}", wv)
                                .replace("{CHROME_VERSION}", cv);
                UserAgent.ua = normalize(UserAgent.ua);
                if (UserAgent.ua.isEmpty()) UserAgent.ua = ua;
            } else {
//                Log.w("User-Agent does not match the regex ", regex, ": " + ua);
                UserAgent.ua = ua;
            }

            return UserAgent.ua;
        }

        public static String getUaDesktop(WebSettings s) {
            if (uaDesktop != null) return uaDesktop;

            String ua = s.getUserAgentString();
            Regex regex = new Regex(REGEX);
            if (regex.matches(ua)) {
                String wv = requireNonNull(regex.group(1));
                String cv = requireNonNull(regex.group(2));
                uaDesktop =
                        USER_AGENT_DESKTOP
                                .replace("{WEBKIT_VERSION}", wv)
                                .replace("{CHROME_VERSION}", cv);
            } else {
//                Log.w("User-Agent does not match the regex ", regex, ": " + ua);
                int i1 = ua.indexOf('(') + 1;
                int i2 = ua.indexOf(')', i1);
                uaDesktop =
                        ua.substring(0, i1) + "X11; Linux x86_64" + ua.substring(i2)
                                .replace(" Mobile ", " ")
                                .replaceFirst(" Version/\\d+\\.\\d+ ", " ");
            }

            return uaDesktop = normalize(uaDesktop);
        }

        private static String normalize(String ua) {
            int cut = 0;
            boolean changed = false;
            StringBuilder b = new StringBuilder();

            for (int i = 0, n = ua.length(); i < n; i++) {
                char c = ua.charAt(i);

                if (c <= ' ') {
                    if ((b.length() == 0) || (ua.charAt(i - 1) == ' ')) {
                        changed = true;
                        continue;
                    } else if (c != ' ') {
                        b.append(' ');
                        changed = true;
                        continue;
                    }
                }

                b.append(c);
            }

            for (int i = b.length() - 1; i >= 0; i--) {
                if (b.charAt(i) == ' ') cut++;
                else break;
            }

            if (cut != 0) {
                changed = true;
                b.setLength(b.length() - cut);
            }

            return changed ? b.toString() : ua;
        }
    }
}
