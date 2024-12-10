package com.nanyang.richeditor.editor;

import static com.nanyang.richeditor.util.FilesUtils.saveBitmap;
import static com.nytaiji.nybase.utils.BitmapUtil.getVideoThumbnail;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.FrameLayout;


import java.util.List;


public class NyRichEditor extends RichEditor {

    private OnConsoleMessageListener mOnConsoleMessageListener;
    /**
     * 用于在 ontextchange中执行操作标识避免循环
     */
    private boolean isUnableTextChange = false;
    private boolean isNeedSetNewLineAfter = false;
    //视频缩略图
    private boolean isNeedAutoPosterUrl = false;
    private Context context;

    public NyRichEditor(Context context) {
        super(context);
        this.context = context;
        initConfig();
    }

    public NyRichEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initConfig();
    }

    public NyRichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initConfig();
    }

    public void initView(RichEditorCallback richEditorCallback) {
        addJavascriptInterface(richEditorCallback, "MRichEditor");
    }

    public boolean isNeedAutoPosterUrl() {
        return isNeedAutoPosterUrl;
    }

    public void setNeedAutoPosterUrl(boolean needAutoPosterUrl) {
        isNeedAutoPosterUrl = needAutoPosterUrl;
    }

    public void insertLine() {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertLine();");
    }

    public void getCurrChooseParams() {
        exec("javascript:RE.getSelectedNode();");
    }

    //TODO 2021-10-27
    public void loadRichEditorCode(String html) {
        loadDataWithBaseURL("file://",
                html + CommonJs.IMG_CLICK_JS, "text/html", "utf-8", null);
    }

    public interface OnConsoleMessageListener {
        void onTextChange(String message, int lineNumber, String sourceID);
    }


    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mTextChangeListener = listener;
    }

    public void setOnConsoleMessageListener(OnConsoleMessageListener listener) {
        this.mOnConsoleMessageListener = listener;
    }

    private void initConfig() {

        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int currentApiVersion = Build.VERSION.SDK_INT;
        final int flags = SYSTEM_UI_FLAG_LAYOUT_STABLE;
            /*    | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;*/

        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = ((Activity) context).getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }


        setWebChromeClient(new WebChromeClient() {
            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                super.onConsoleMessage(message, lineNumber, sourceID);
                if (mOnConsoleMessageListener != null) {
                    mOnConsoleMessageListener.onTextChange(message, lineNumber, sourceID);
                }

            }
        });

       /* setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return true;
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (canGoBack()) {
                    goBack();
                    return true;
                }
                return false;
            }
            return false;
        });*/

        setWebChromeClient(new WebChromeClient() {
            final int orientation = getResources().getConfiguration().orientation;
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;
            private int mOriginalSystemUiVisibility;


            public Bitmap getDefaultVideoPoster() {
                if (mCustomView == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(context.getResources(), 2130837573);
            }


            public void onHideCustomView() {
                ((FrameLayout) ((Activity) context).getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                ((Activity) context).setRequestedOrientation(orientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }

            public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
                if (this.mCustomView != null) {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = ((Activity) context).getWindow().getDecorView().getSystemUiVisibility();
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout) ((Activity) context).getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(3846 | SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        });


        //set Zoomability
        final WebSettings webSettings = getSettings();
        webSettings.setLoadWithOverviewMode(true); //
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //其他细节操作
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; smbRecycle:21.0.0) Gecko/20121011 Firefox/21.0.0");
    }


    public void insertImage(String url, String alt) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertImage('" + url + "', '" + alt + "');");
        // exec("javascript:RE.insertImageW('" + url + "', '" + alt + "','" + screenWidth + "');");
    }

    /**
     * the image according to the specific width of the image automatically
     *
     * @param url
     * @param alt
     * @param width
     */
    public void insertImage(String url, String alt, int width) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertImageW('" + url + "', '" + alt + "','" + width + "');");
    }

    /**
     * {@link NyRichEditor#insertImage(String, String)} will show the original size of the image.
     * So this method can manually process the image by adjusting specific width and height to fit into different mobile screens.
     *
     * @param url
     * @param alt
     * @param width
     * @param height
     */
    public void insertImage(String url, String alt, int width, int height) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertImageWH('" + url + "', '" + alt + "','" + width + "', '" + height + "');");
    }


    public void insertVideo(String url, int width) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertVideoW('" + url + "', '" + width + "');");
    }

    public void insertVideo(String url, int width, int height) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertVideoWH('" + url + "', '" + width + "', '" + height + "');");
    }


    public void insertVideo(String videoUrl, int width, String posterUrl) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertVideo('" + videoUrl + "', '" + width + "', '" + posterUrl + "');");
    }


    public void insertYoutubeVideo(String url) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertYoutubeVideo('" + url + "');");
    }

    public void insertYoutubeVideo(String url, int width) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertYoutubeVideoW('" + url + "', '" + width + "');");
    }

    public void insertYoutubeVideo(String url, int width, int height) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertYoutubeVideoWH('" + url + "', '" + width + "', '" + height + "');");
    }

    public void insertHtml(String html) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertHTML('" + html + "');");
    }


    public void setNewLine() {
        isNeedSetNewLineAfter = false;
        isUnableTextChange = true;
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertHTML('<br></br>');");
    }

    public void setHint(String placeholder) {
        setPlaceholder(placeholder);
    }

    public void setHintColor(String placeholderColor) {
        exec("javascript:RE.setPlaceholderColor('" + placeholderColor + "');");
    }

    public void setNeedSetNewLineAfter(boolean needSetNewLineAfter) {
        isNeedSetNewLineAfter = needSetNewLineAfter;
    }

    public boolean isNeedSetNewLineAfter() {
        return isNeedSetNewLineAfter;
    }


    public void insertUrlWithDown(String downloadUrl, String title) {
        if (TextUtils.isEmpty(downloadUrl)) {
            return;
        }

        String fileName;
        try {
            String[] split = downloadUrl.split("/");
            fileName = split[split.length - 1];
        } catch (Exception e) {
            fileName = "rich" + System.currentTimeMillis();
        }

        title += fileName;
        insertHtml("<a href=\"" + downloadUrl + "\" download=\"" + fileName + "\">" +
                title +
                "</a><br></br>");
    }

    public void insertAudio(String audioUrl) {
        insertAudio(audioUrl, "");
    }

    public void insertAudio(String audioUrl, String custom) {
        if (TextUtils.isEmpty(custom)) {
            custom =             //增加进度控制
                    "controls=\"controls\"" +
                            //宽高
                            "height=\"300\" " +
                            //样式
                            " style=\"margin-top:10px;max-width:100%;\"";
        }
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertAudio('" + audioUrl + "', '" + custom + "');");
    }


    public void insertVideo(String videoUrl) {
        insertVideo(videoUrl, "", "");
    }

    /**
     * @param videoUrl
     * @param customStyle
     * @param posterUrl   视频默认缩略图
     */
    public void insertVideo(String videoUrl, String customStyle, String posterUrl) {
        if (TextUtils.isEmpty(customStyle)) {
            customStyle =             //增加进度控制
//                    "controls=\"controls\"" + //已修改到video标签里面
                    //宽高
                    "height=\"300\" " +
                            //样式
                            " style=\"margin-top:10px;max-width:100%;\"";
        }

        if (TextUtils.isEmpty(posterUrl) && isNeedAutoPosterUrl) {
            Bitmap videoThumbnail = getVideoThumbnail(videoUrl);
            if (videoThumbnail != null) {
                String videoThumbnailUrl = saveBitmap(videoThumbnail);
                if (!TextUtils.isEmpty(videoThumbnailUrl)) {
                    posterUrl = videoThumbnailUrl;
                }
            }

        }

        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertVideo('" + videoUrl + "', '" + customStyle + "', '" + posterUrl + "');");
    }


    // 获取html本地的地址 方便上传的时候转为在线的地址
    public List<String> getAllSrcAndHref() {
        return EditorUtils.getHtmlSrcOrHrefList(getHtml());
    }

    public void clearLocalRichEditorCache() {
        clearLocalRichEditorCache();
    }

}
