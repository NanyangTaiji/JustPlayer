package com.nanyang.richeditor.editor;


import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.VISIBLE;

import static com.nanyang.richeditor.editor.EditorUtils.formatSaveFile;
import static com.nanyang.richeditor.editor.EditorUtils.removeRedundant;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getPreferredServerUrl;
import static com.nytaiji.nybase.utils.NyFileUtil.getLastSegmentFromString;
import static com.nytaiji.nybase.utils.NyFileUtil.isMedia;
import static com.nytaiji.nybase.utils.SystemUtils.copyToClipboard;
import static com.nytaiji.nybase.utils.VideoJsonUtil.saveListToFile;
import static com.nytaiji.nybase.network.VideoPlayNdownload.downloadFromUrl;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nanyang.richeditor.GestureManager;
import com.nanyang.richeditor.R;
import com.nytaiji.nybase.httpShare.WifiShareUtil;
import com.nytaiji.nybase.utils.OnBackPressedListener;
import com.nytaiji.nybase.view.NyWebView;
//mport com.nytaiji.core.nyExoPlayer.trimView.VideoRangeActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


public class ShowHtmlFragment extends Fragment implements OnBackPressedListener {
    private static final String TAG = "ShowHtmlFragment";
    public boolean isShow = true;
    private static String html = null;
    private static String link = null;
    private static final String reroutedHtml = Environment.getExternalStorageDirectory() + "/r.html";
    // private BottomSheetBehavior sheetBehavior;
    private NyWebView webView;
    private GestureOverlayView mGestureview;
    private String mediaLink = null;
    private List<String> mediasSelected = new ArrayList<>();
    private View fabSave;
    private static int tabIndex = -1;
  //  private AdFilter filter;
    public static void show(FragmentManager fm, String html) {
        Bundle arguments = new Bundle();
        arguments.putString("html", html);
        ShowHtmlFragment fragment = new ShowHtmlFragment();
        fragment.setArguments(arguments);
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static void reload(FragmentManager fm) {
        ShowHtmlFragment fragment = new ShowHtmlFragment();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static ShowHtmlFragment get(FragmentManager fm) {
        return (ShowHtmlFragment) fm.findFragmentByTag(TAG);
    }

    public static void remove(FragmentManager fm) {
        ShowHtmlFragment showhtmlFragment = get(fm);

        if (null != showhtmlFragment) {
            tabIndex = -1;
            html = null;
            link = null;
            showhtmlFragment.onDestroy();
            fm.beginTransaction().remove(showhtmlFragment).commitAllowingStateLoss();
        }
    }

    public static void collapse(FragmentManager fm) {
        ShowHtmlFragment showhtmlFragment = get(fm);
        if (null != showhtmlFragment) {
            //showhtmlFragment.collapse();
        }
    }

    public ShowHtmlFragment() {
    }

    public static int getTabIndex() {
        return tabIndex;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            html = bundle.getString("html");
            link = bundle.getString("link");
            tabIndex = bundle.getInt("tabIndex");
        }

        // Log.e(TAG, "html = " + html);
        isShow = true;
        assert container != null;
        container.setVisibility(VISIBLE);
        return inflater.inflate(R.layout.fragment_show_html, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //  view.setOnKeyListener(this);
        initConfig();

        webView = view.findViewById(R.id.web_view);
        //
        webView.setWebViewClient(new DisplayWebViewClient());

        // Setup AdblockAndroid for your WebView.-------------------------
      /*  filter = AdFilter.Instance.get();
        filter.setupWebView(webView);

        // Add filter list subscriptions on first installation.
        if (!filter.hasInstallation()) {
            Map<String, String> map = new HashMap<>();
            map.put("AdGuard Base", "https://filters.adtidy.org/extension/chromium/filters/2.txt");
            map.put("EasyPrivacy Lite", "https://filters.adtidy.org/extension/chromium/filters/118_optimized.txt");
            map.put("AdGuard Tracking Protection", "https://filters.adtidy.org/extension/chromium/filters/3.txt");
            map.put("AdGuard Annoyances", "https://filters.adtidy.org/extension/chromium/filters/14.txt");
            map.put("AdGuard Chinese", "https://filters.adtidy.org/extension/chromium/filters/224.txt");
            map.put("NoCoin Filter List", "https://filters.adtidy.org/extension/chromium/filters/242.txt");

            for (Map.Entry<String, String> entry : map.entrySet()) {
                filter.getViewModel().download(filter.getViewModel().addFilter(entry.getKey(), entry.getValue()).getId());
            }
        }

        filter.getViewModel().getOnDirty().observe(getViewLifecycleOwner(), new Observer<None>() {
            @Override
            public void onChanged(None none) {
                webView.clearCache(false);
            }
        });*/


        if (link != null) webView.loadUrl(link);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // if (isMedia(url))
                if (!url.contains("time=")) {
                    // Log.e(TAG, "onDownloadStart= " + url);
                    Toast.makeText(getContext(), "The url is added!", Toast.LENGTH_SHORT).show();
                    mediasSelected.add(url);
                    fabSave.setVisibility(View.VISIBLE);
                }
            }
        });
        //-----------------

        mGestureview = (GestureOverlayView) view.findViewById(R.id.gestureview);
        initGestureOverlay();
        //---------------------
        Toolbar toolBar = (Toolbar) getActivity().findViewById(R.id.youtube_toolbar);
        if (toolBar != null) {
            fabSave = toolBar.findViewById(R.id.fabSaveList);
            fabSave.setVisibility(View.GONE);
            fabSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediasSelected.size() > 0 && saveListToFile(removeRedundant(mediasSelected), formatSaveFile("SavedList", "lnk"))) {
                        Toast.makeText(getContext(), "The selected list saved!", Toast.LENGTH_SHORT).show();
                        mediasSelected.clear();
                        fabSave.setVisibility(View.GONE);
                    }
                }
            });

            toolBar.findViewById(R.id.fabPlay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaLink == null) {
                        Toast.makeText(getContext(), "No playable link selected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mediaLink.contains("?download="))
                        mediaLink = mediaLink.substring(0, mediaLink.indexOf("?download="));

                    String link = getPreferredServerUrl(false);

                    WifiShareUtil.httpShare(getActivity(), Uri.parse(mediaLink), link, ((EditorActivity) getActivity()).shareHandle);
                    Toast.makeText(getActivity(), "Server:" + link + " url=" + mediaLink, Toast.LENGTH_LONG).show();
                   /* PackageManager pm = getActivity().getPackageManager();
                    try {
                        pm.getPackageInfo("org.adblockplus.browser", 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        Toast.makeText(getActivity(), "Package Adblock Browser did not installed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("org.adblockplus.browser");
                    intent.putExtra(Intent.EXTRA_TEXT, link);
                    intent.setData(Uri.parse(link));
                    getActivity().startActivity(intent);
                    getActivity().finish();*/
                }
            });

            toolBar.findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exit = true;
                    getActivity().onBackPressed();
                }
            });

            toolBar.findViewById(R.id.fabDownload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaLink != null) {
                        downloadFromUrl(getActivity(), mediaLink, getLastSegmentFromString(mediaLink), getLastSegmentFromString(mediaLink));
                        Toast.makeText(getContext(), mediaLink + " is downloading", Toast.LENGTH_SHORT).show();
                        mediaLink = null;
                    } else {
                        Toast.makeText(getContext(), "No link is downloaded", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            toolBar.findViewById(R.id.fabCopy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaLink != null) {
                        copyToClipboard(requireActivity(), mediaLink);
                        Toast.makeText(getContext(), mediaLink + " copied to the clipboard", Toast.LENGTH_SHORT).show();
                        mediaLink = null;
                    } else {
                        Toast.makeText(getContext(), "No link is copied to the clipboard", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


     protected class DisplayWebViewClient extends WebViewClient {
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
            //TODO ny
          /*  if (filter.shouldIntercept(webView, request) != null) {
                WebResourceResponse YON = filter.shouldIntercept(webView, request).getResourceResponse();
                return YON;
            }*/
            return super.shouldInterceptRequest(webView,request);
        }


        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            super.onPageStarted(webView, url, favicon);
            //filter.performScript(webView, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (entryUrl != null && !url.contains(entryUrl)) return true;
            String decode;
            try {
                decode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // No handling
                return false;
            }
            if (isMedia(decode)) {
                mediaLink = decode;
            }
            if (decode.contains("mp4") && !decode.contains("time=")) {
                mediaLink = decode;
            }

            if (mediaLink != null) {
                mediasSelected.add(mediaLink);
                Toast.makeText(getContext(), "The url is added!", Toast.LENGTH_SHORT).show();
                fabSave.setVisibility(View.VISIBLE);
                //  Log.e(TAG, "media link= " + mediaLink);
                //save for copy or downloading
                // mediaLink = null;

            }

            return super.shouldOverrideUrlLoading(view, url);
        }


        String entryUrl = null;
    }


    private void initConfig() {

        ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int currentApiVersion = Build.VERSION.SDK_INT;
        final int flags = SYSTEM_UI_FLAG_LAYOUT_STABLE
                | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            ((Activity) getContext()).getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = ((Activity) getContext()).getWindow().getDecorView();
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
    }

    private boolean exit = false;

    @Override
    public boolean onBackPressed() {
        if (exit) {
            if (mediasSelected.size() > 0)
                return !saveListToFile(removeRedundant(mediasSelected), formatSaveFile("SavedList", "lnk"));
            else return false;
        }

        if (webView.canGoBack()) {
            webView.goBack();
            exit = false;
        } else {
            //  currUrl = "https://m.youtube.com/";
            // youtubeView.loadUrl(currUrl);
            exit = true;
        }
        return true;
    }


    private void initGestureOverlay() {
        GestureLibrary mGestureLib = GestureManager.getInstance(getActivity()).getGestureLib();
        if (mGestureview == null) return;
        mGestureview.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE);
        mGestureview.setFadeOffset(0);
        mGestureview.setGestureStrokeWidth(10);
        mGestureview.setEventsInterceptionEnabled(false);
        mGestureview.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
                ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = (Prediction) predictions.get(0);
                    if (prediction.score > 1.0) {
                        if (prediction.name.equals("back")) onBackPressed();
                        if (prediction.name.equals("refresh")) {
                            //TODO ny copy link
                            if (mediaLink != null) {
                                mediasSelected.add(mediaLink);
                                mediaLink = null;
                                onBackPressed();
                            }
                        }
                      /*  if (prediction.name.equals("left") || prediction.name.equals("right")) {
                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }*/
                    }
                }

            }
        });
    }

}
