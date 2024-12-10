package com.nanyang.richeditor.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class RichToolEditor extends WebView {



//------------------------------------------------------------------------------//
   private final String htmlContent = "<p>#nytaiji<br><br><br><br><br><br>@nytaiji</p>";
    private static final String BASE_HTML = "file:///android_asset/richEditor.html";
    private boolean isReady = false;
    private String mContents;


    public RichToolEditor(Context context) {
        this(context, null);
    }

    public RichToolEditor(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public RichToolEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new EditorWebViewClient());
        getSettings().setJavaScriptEnabled(true);

        //--------------------//
        loadUrl(BASE_HTML);
        insertHtml(htmlContent);
        mContents=htmlContent;
    }

    public void insertHtml(String html) {
        load("javascript:pasteHTML('" + html + "')");
    }


    public void setText(String contents) {
        if (contents == null) {
            contents = "";
        }
        // try {

        load("javascript:pasteHTML('" + contents + "')");


        //   loadScript("javascript:RE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
        // } catch (UnsupportedEncodingException e) {
        // No handling
        // }
        mContents += contents;
    }

    public void focusEditor() {
        requestFocus();
       // exec("javascript:RE.focus();");
    }

    public String getText() {
        return mContents;
    }

    public void showButtons(boolean toShow){
       if (toShow) {
           load("javascript:initToolbar()");
       }
       else {
           load("javascript:initNoToolbar()");
       }
    }

    protected void loadScript(final String script) {
        if (isReady) {
            load(script);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadScript(script);
                }
            }, 100);
        }
    }

    private void load(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(script, null);
        } else {
            loadUrl(script);
        }
    }

    protected class EditorWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            isReady = url.equalsIgnoreCase(BASE_HTML);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String decode;
            try {
                decode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // No handling
                return false;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }

}
