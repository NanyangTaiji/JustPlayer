package com.nytaiji.nybase;

import static android.view.View.VISIBLE;
import static com.nytaiji.nybase.WebViewActivity.webDisplayUrl;
import static com.nytaiji.nybase.filePicker.MediaSelection.DEFAULT_MEDIA;
import static com.nytaiji.nybase.filePicker.MediaSelection.getStringValue;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getMessageHandler;
import static com.nytaiji.nybase.network.NetworkServersDialog.getServerLink;
import static com.nytaiji.nybase.network.NetworkServersDialog.needsServer;
import static com.nytaiji.nybase.network.VideoPlayNdownload.downloadFromUrl;
import static com.nytaiji.nybase.utils.MIMEType.isFileExtractable;
import static com.nytaiji.nybase.utils.NyFileUtil.GOOGLE_PHOTOS_PACKAGE_NAME;
import static com.nytaiji.nybase.utils.NyFileUtil.VLC_PACKAGE_NAME;
import static com.nytaiji.nybase.utils.NyFileUtil.getLastSegmentFromString;
import static com.nytaiji.nybase.utils.NyFileUtil.getYoutubeId;
import static com.nytaiji.nybase.utils.NyFileUtil.isGooglePhotosInstalled;
import static com.nytaiji.nybase.utils.NyFileUtil.isImage;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;
import static com.nytaiji.nybase.utils.NyFileUtil.isVLCInstalled;
import static com.nytaiji.nybase.utils.NyMimeTypes.getMimeTypeFromPath;
import static com.nytaiji.nybase.utils.SystemUtils.dp2px;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenWidth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.mediarouter.app.MediaRouteButton;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.playlist.PlaylistDialog;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.nybase.view.nyBottomSheetBehavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class NyDisplayFragment extends NyBaseFragment implements View.OnClickListener {
    protected final static String TAG = "NyDisplayFragment";

    private boolean backwardSlide = false;
    private boolean SlideOn = false;
    private WebView nyWebView;
    public String currUrl, reroutedUrl;

    public View root;
    private final int slideInterval = 5000;
    public NyHybrid hybridFile = null;

    private nyBottomSheetBehavior sheetBehavior;
    //   private boolean videoDirect = false;  //whether to start with videoview directly
    private ImageView miniPrev, miniPlay, miniNext, miniEnd;
    private ImageView mainPrev, mainPlay, mainNext, mainEnd;
    private ImageView mainShuffle,/* miniShuffle,*/
            miniThumb, mainThumb;
    private TextView miniTitle, mainTitle, mainInfo, miniInfo;//, playerType;

    private View miniPlayer, mainPlayer;
    private ListView mListSub;
    public ArrayList<NyHybrid> fileList;

    private boolean playOn = true;
    private Timer swipeTimer = null;
    private int vIndex = 0;
    private static boolean noPip = true;
    public OpenMode mOpenMode;

    private int semiTransparentColor;

    private LongClickListner longClickListner;

    public NyDisplayFragment() {// Required empty public constructor
    }

    protected static int containerId;


    public static void show(FragmentManager fm, NyHybrid baseFile) {
        if (get(fm) != null) {
            remove(get(fm));
        }
        Bundle arguments = new Bundle();
        NyDisplayFragment fragment = new NyDisplayFragment();
        fragment.setBaseFile(baseFile);
        fragment.setArguments(arguments);
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static void show(FragmentManager fm, int fragmentContainerId, NyHybrid baseFile) {
        containerId = fragmentContainerId;
        if (get(fm) != null) {
            remove(get(fm));
        }
        Bundle arguments = new Bundle();
        NyDisplayFragment fragment = new NyDisplayFragment();
        fragment.setBaseFile(baseFile);
        fragment.setArguments(arguments);
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(fragmentContainerId, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static void show(FragmentManager fm, int fragmentContainerId, String path) {
        containerId = fragmentContainerId;
        if (get(fm) != null) {
            remove(get(fm));
        }
        Bundle arguments = new Bundle();
        NyDisplayFragment fragment = new NyDisplayFragment();
        fragment.setPath(path);
        fragment.setArguments(arguments);
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(fragmentContainerId, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static NyDisplayFragment get(FragmentManager fm) {
        return (NyDisplayFragment) fm.findFragmentByTag(TAG);
    }

    public static void remove(NyDisplayFragment nyDisplayFragment) {
        // Ensure that the fragment is not null
        if (nyDisplayFragment != null) {
            // Destroy the fragment
            nyDisplayFragment.destroy();

            // Check if the fragment is added before attempting to remove it
            if (nyDisplayFragment.isAdded()) {
                // Perform the fragment transaction
                nyDisplayFragment.getParentFragmentManager()
                        .beginTransaction()
                        .remove(nyDisplayFragment)
                        .commitAllowingStateLoss();
            }
        }
    }


    public void setBaseFile(NyHybrid baseFile) {
        if (baseFile == null) {
            Toast.makeText(getContext(), "Invalid NyHybrid", Toast.LENGTH_SHORT).show();
            return;
        }
        hybridFile = baseFile;
        //  mOpenMode = hybridFile.getMode();
    }

    public void setPath(String path) {
        hybridFile = null;
        currUrl = path;
    }

    public void setLongClickListner(LongClickListner longClickListner) {
        this.longClickListner = longClickListner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //needed for efficient loading of pdf file
        destroy();
        SystemUtils.hideNavKey(mActivity);
        activityUiAction(false);
        SystemUtils.keepScreenOn(mActivity);
        //   caster = Caster.create(mActivity);

        //
        root = inflater.inflate(R.layout.fragment_display, container, false);

        //   sharedPreferences = PreferenceHelper.getInstance();
        setupWebView();
        initBottomSheet();
        setHasOptionsMenu(true);
        setUpMediaRouteButton();
        initDisplay();
        return root;
    }

    private void setUpMediaRouteButton() {
        MediaRouteButton mainCast = root.findViewById(R.id.main_route);
        MediaRouteButton miniCast = root.findViewById(R.id.mini_route);
        // caster.setupMediaRouteButton(mainCast, true);
        //caster.setupMediaRouteButton(miniCast, true);
    }


    @SuppressLint("SetJavaScriptEnabled")
    protected void setWebSettings() {
        WebSettings webSetting = nyWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);// this one used will destroy roation when adjustVideoSize() is implemented

        webSetting.setAllowFileAccess(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setAllowFileAccessFromFileURLs(true);
        webSetting.setMediaPlaybackRequiresUserGesture(false);  //necessary for auto video play
        //缩放操作
        webSetting.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSetting.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSetting.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //  if (NyFileUtil.isOnline(hybridFile.getUri()))
        //     webSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSetting.setDomStorageEnabled(true);

        //支持通过JS打开新窗口
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setLoadsImagesAutomatically(true);
        webSetting.setDefaultTextEncodingName("UTF-8");
        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        // 设置自适应屏幕，两者合用
        //  if (hybridFile.getMimeType().contains("image")) {
        // webSetting.setUseWideViewPort(true); // 将图片调整到适合WebView的大小
        //  webSetting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //  }
    }

    protected void initDisplay() {
        if (hybridFile == null || hybridFile.getName(mActivity) == null) {
            if (currUrl == null) {
                remove(this);
            } else {
                loadVideoInTop(currUrl, getMimeTypeFromPath(currUrl));
                hideButtons();
            }
            return;
        }

        if (isFileExtractable(hybridFile.getName(mActivity))) {
            Toast.makeText(mActivity, "Decompressed or download it first", Toast.LENGTH_LONG).show();
            remove(NyDisplayFragment.this);
        } else if (hybridFile != null && hybridFile.getUri() != null) {
            if (hybridFile.isLocal()) {
                hybridFile.setPath(NyFileUtil.getPath(getContext(), hybridFile.getUri()));
                ArrayList<NyHybrid> nyVideoList = hybridFile.commonrades();
                Log.e(TAG, "hybridFile.setPath()=" + hybridFile.getPath());
                if (!nyVideoList.isEmpty()) shuffleDisplay(nyVideoList);
                else displayMedia();
            } else displayMedia();
        } else if (hybridFile != null && hybridFile.getPath() != null) {// ArrayList<NyHybrid> nyVideoList = hybridFile.commonrades();
            // hybridFile.setPath(hybridFile.getPath());
            //  Log.e(TAG, "hybridFile.getPath()=" + hybridFile.getPath());
            ArrayList<NyHybrid> nyVideoList = hybridFile.commonrades();
            shuffleDisplay(nyVideoList);
        }
    }

    private void shuffleDisplay(ArrayList<NyHybrid> nyVideoList) {
        fileList = nyVideoList;
        vIndex = 0; //initiate
        currUrl = fileList.get(vIndex).getPath();
        //  boolean toShuffle = sharedPreferences.getBoolean(KEY_SHUFFLE, false);
        //  if (toShuffle) Collections.shuffle(fileList, new Random());
        if (fileList.isEmpty()) {
            Log.e(TAG, "fileList.add =" + hybridFile.getPath());
            fileList.add(hybridFile);
        }
        if (fileList.size() > 1) initListSub();  //recalculate vIndex in this method;
        else hideButtons();

       /* if (!isMedia(currUrl)) {
            //TODO
            // mainPlay.setVisibility(View.INVISIBLE);
            // miniPlay.setVisibility(View.INVISIBLE);
        }*/

        if (isImage(currUrl)) {
            slideTiming(new Handler(), Update, 0, slideInterval);
        } else processList();
    }


    private void setupWebView() {
        nyWebView = root.findViewById(R.id.web_view);
        setWebSettings();
        nyWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mainPlayerVisibility(View.VISIBLE);
                return false;
            }
        });

        CookieManager.getInstance().setAcceptCookie(false);

    }

    private void mainPlayerVisibility(int Visibility) {

        mainPlayer.setVisibility(Visibility);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mainPlayer.setVisibility(View.INVISIBLE);
            }
        }, 3000);
    }

    private Uri reroutedUri;

    protected void displayMedia() {
        playOn = true;
        adjustPlayerIcon();
        String mimeType = hybridFile.getMimeType();
        if (mimeType == null) {
            mimeType = getStringValue(AppContextProvider.getAppContext(), DEFAULT_MEDIA, "video/*");
            hybridFile.setMimeType(mimeType);
        }
        if (fileList == null || fileList.size() == 1) hideButtons();
        //  Log.e(TAG, "displayUri uri = " + hybridFile.getUri().toString());
        if (needsServer(hybridFile)) {
            hybridFile.setPath(hybridFile.getPath());
            reroutedUrl = getWebServerLink(hybridFile, true);
            if (mimeType.contains("video")) {
                reroutedUri = Uri.parse(reroutedUrl);
                hybridFile.setUri(reroutedUri);
                loadVideoInTop(reroutedUri, hybridFile.getMimeType());
            } else nyWebView.loadUrl(reroutedUrl);
        } else {
            if (mimeType.contains("video")) {
                reroutedUri = hybridFile.getUri();
                loadVideoInTop(reroutedUri, hybridFile.getMimeType());
            } else nyWebView.loadUrl(NyFileUtil.getPath(getContext(), hybridFile.getUri()));
        }
    }


    public String getWebServerLink(final NyHybrid hybridFile, boolean isInternal) {
        return getServerLink(hybridFile, getMessageHandler(getActivity().findViewById(containerId)));
    }


    public void routeToExternal(Uri reroutedUri) {
      /*  boolean isException = NyFileUtil.isSpecialMedia(hybridFile.getPath());
        if (!isException && isGooglePhotosInstalled(AppContextProvider.getAppContext())) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(hybridFile.getUri(), hybridFile.getMimeType());
            intent.setPackage(GOOGLE_PHOTOS_PACKAGE_NAME);
            getActivity().startActivity(intent);
        } else if (isVLCInstalled(AppContextProvider.getAppContext())) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(hybridFile.getUri(), hybridFile.getMimeType());
            intent.setPackage(VLC_PACKAGE_NAME);
            getActivity().startActivity(intent);
        } else {*/
            processToShare(reroutedUri);
       // }
    }


    String youtubeId = null;

    public void loadVideoInTop(String path, String mimeType) {
        if (isOnline(path)) download.setVisibility(VISIBLE);
        else download.setVisibility(View.GONE);
        youtubeId = getYoutubeId(path);
        if (youtubeId != null) {
            loadYouTubeVideoInTop(youtubeId);
            return;
        } else if (Objects.equals(mimeType, "null")) mimeType = "video/*";

        String htmlContent = "<html><head><style>"
                + "body, html { height: 100%; margin: 0; }"
                + "video { width: 100%; height: auto; }"  // Adjusted video styling
                + "</style><script>"
                + "</script></head><body>"
                + "<video id='video' controls autoplay>"
                + "<source src='" + path + "' type='" + mimeType + "'>"
                + "</video>"
                + "</body></html>";

        nyWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
    }


    public void loadVideoInTop(Uri videoUri, String mimeType) {
        if (isOnline(videoUri)) download.setVisibility(VISIBLE);
        else download.setVisibility(View.GONE);
        youtubeId = getYoutubeId(videoUri.toString());
        if (youtubeId != null) {
            loadYouTubeVideoInTop(youtubeId);
            return;
        }

        String htmlContent = "<html><head><style>"
                + "body, html { height: 100%; margin: 0; }"
                + "video { width: 100%; height: auto; }"  // Adjusted video styling
                + "</style><script>"
                + "</script></head><body>"
                + "<video id='video' controls autoplay>"
                + "<source src='" + videoUri + "' type='" + mimeType + "'>"
                + "</video>"
                + "</body></html>";

        nyWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
    }

    public void loadYouTubeVideoInTop(String videoId) {
        //  Log.e(TAG, "loadYouTubeVideoInTop id =" + videoId);
        // Construct the YouTube embed URL
        int screenWidth = getScreenWidth(getActivity());
        int displayHeight = screenWidth * 9 / 16;

        String embedUrl = "https://www.youtube.com/embed/" + videoId;
        String width = String.valueOf(screenWidth);
        String height = String.valueOf(displayHeight);

        String htmlContent = "<html><head><style>"
                + "body, html { height: 100%; margin: 0; }"
                + "</style></head><body>"
                + "<iframe width=\"" + width + "\" height=\"" + height + "\" src=\"" + embedUrl + "\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>"
                + "</body></html>";


        nyWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
    }


    /*
    Log.e(TAG, "loadYouTubeVideoInTop id =" + videoId);
        // Construct the YouTube embed URL
        int screenWidth = getScreenWidth(getActivity());
        int displayHeight = screenWidth * 9 / 16;

        String embedUrl = "https://www.youtube.com/embed/" + videoId;
        String width = String.valueOf(screenWidth);
        String height = String.valueOf(displayHeight);

        String htmlContent = "<html><head><style>"
                + "body, html { height: 100%; margin: 0; display: flex; justify-content: center; align-items: center; }"
                + "iframe { max-width: 100%; max-height: 100%; }"
                + "</style></head><body>"
                + "<iframe width=\"" + width + "\" height=\"" + height + "\" src=\"" + embedUrl + "\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>"
                + "</body></html>";


        nyWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);

     */


    private void hideButtons() {
        mainShuffle.setVisibility(View.INVISIBLE);
        //miniShuffle.setVisibility(View.INVISIBLE);
        mainPrev.setVisibility(View.INVISIBLE);
        miniPrev.setVisibility(View.INVISIBLE);
        mainNext.setVisibility(View.INVISIBLE);
        miniNext.setVisibility(View.INVISIBLE);
    }


    private void processList() {
        if (fileList != null) {
            if (vIndex < 0) vIndex = fileList.size() - 1;
            else if (vIndex > fileList.size() - 1) vIndex = 0;
            currUrl = fileList.get(vIndex).getPath();
            //TO highight for more than one item
            if (adapter != null) adapter.setSelectedItem(vIndex);
        }

        //always start with autoplay
        playOn = true;
        adjustPlayerIcon();
        if (!isOnline(currUrl) && !currUrl.contains("file://")) currUrl = "file://" + currUrl;
        releaseWebResources();
        nyWebView.onResume(); //necessary for continuation of from pause to play
        if (currUrl.contains("e=")) {//zip file
            hybridFile = new NyHybrid(currUrl);
        } else {
            hybridFile = new NyHybrid(Uri.parse(currUrl));
        }
        displayMedia();
    }

    private void releaseWebResources() {
        // Stop video playback
        nyWebView.evaluateJavascript("document.getElementById('video').pause();", null);

        // Remove video element from DOM
        nyWebView.evaluateJavascript("document.getElementById('video').parentNode.removeChild(document.getElementById('video'));", null);

        // Clear WebView cache (optional)
        nyWebView.clearCache(true); // Pass true to clear the entire cache
        // Pass false to clear only the application cache
    }


    @Override
    public void onResume() {
        super.onResume();
        nyWebView.requestFocus();
        //to showup when it is back from toExternal
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private TextView caster, download, popup, audio, toWeb, addPlaylist, property, share;

    protected void initBottomSheet() {
        mainPlayer = root.findViewById(R.id.main_player_layout);
        //  playerType = root.findViewById(R.id.nyplayer);
        mainTitle = root.findViewById(R.id.main_title);
        mainInfo = root.findViewById(R.id.main_info);
        mainThumb = root.findViewById(R.id.main_thumb);
        mainPrev = root.findViewById(R.id.main_prev);
        mainNext = root.findViewById(R.id.main_next);
        mainPlay = root.findViewById(R.id.main_play);
        mainEnd = root.findViewById(R.id.main_end);
        mainShuffle = root.findViewById(R.id.main_shuffle);

        miniPlayer = root.findViewById(R.id.mini_player_layout);
        miniTitle = root.findViewById(R.id.mini_title);
        miniInfo = root.findViewById(R.id.mini_info);
        miniThumb = root.findViewById(R.id.mini_thumb);
        miniPrev = root.findViewById(R.id.mini_prev);
        miniNext = root.findViewById(R.id.mini_next);
        miniPlay = root.findViewById(R.id.mini_play);
        miniEnd = root.findViewById(R.id.mini_end);
        //controls
        caster = root.findViewById(R.id.play_with_caster);
        caster.setOnClickListener(this);
        download = root.findViewById(R.id.controls_download);
        download.setOnClickListener(this);
        popup = root.findViewById(R.id.controls_popup);
        popup.setOnClickListener(this);
        addPlaylist = root.findViewById(R.id.controls_playlist);
        addPlaylist.setOnClickListener(this);
        audio = root.findViewById(R.id.controls_audio);
        audio.setOnClickListener(this);
        property = root.findViewById(R.id.controls_infor);
        property.setOnClickListener(this);
        toWeb = root.findViewById(R.id.controls_browser);
        toWeb.setOnClickListener(this);
        share = root.findViewById(R.id.controls_share);
        share.setOnClickListener(this);
        //  miniShuffle = root.findViewById(R.id.mini_shuffle);

        // Define the desired alpha value (from 0 to 255)
        int alphaValue = 50; // For example, set to 50 for approximately 20% opacity

        // Create a color with the desired alpha value
        semiTransparentColor = Color.argb(alphaValue, 255, 255, 255); // Set RGB to white (255, 255, 255)

        // Set the background color of the ConstraintLayout
        // mainPlayer.setBackgroundColor(semiTransparentColor);
        miniPlayer.setBackgroundColor(semiTransparentColor);


        //marqueAnim = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
        ConstraintLayout layoutBottomSheet = (ConstraintLayout) root.findViewById(R.id.lDisplay);
        // layoutBottomSheet.setSoundEffectsEnabled(true);
        sheetBehavior = (nyBottomSheetBehavior) BottomSheetBehavior.from(layoutBottomSheet);
        //------------------
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //TODO ny
        sheetBehavior.setPeekHeight(dp2px(mActivity, 55));

        miniPlayer.setOnClickListener(this);
        miniPrev.setOnClickListener(this);
        miniNext.setOnClickListener(this);
        miniPlay.setOnClickListener(this);
        miniEnd.setOnClickListener(this);
        //  miniList.setOnClickListener(this);
        //   miniShuffle.setOnClickListener(this);

        mainThumb.setOnClickListener(this);
        mainThumb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e(TAG, "mainThumb long clicked");
                routeToExternal(reroutedUri);
                return true;
            }
        });

        mainPlayer.setOnClickListener(this);
        mainPrev.setOnClickListener(this);
        mainNext.setOnClickListener(this);
        mainPlay.setOnClickListener(this);
        mainEnd.setOnClickListener(this);
        //  mainList.setOnClickListener(this);
        mainShuffle.setOnClickListener(this);

        //----------------
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        activityUiAction(true);
                        //TODO do not end
                        //  remove(NyDisplayFragment.this);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        activityUiAction(false);
                        miniPlayer.setVisibility(View.GONE);
                        mainPlayer.setVisibility(VISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        activityUiAction(true);
                        mainPlayer.setVisibility(View.GONE);
                        miniPlayer.setVisibility(VISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        activityUiAction(true);
                        miniPlayer.setVisibility(View.INVISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        activityUiAction(false);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // if (nyWebView != null) nyWebView.setAlpha(slideOffset);
            }
        });

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.main_thumb) {
            playOn = false;
            adjustPlayerIcon();
            if (nyWebView != null) nyWebView.onPause();
            routeToInternal(reroutedUri);
            // remove(this);
        } else if (viewId == R.id.mini_player_layout) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (viewId == R.id.main_player_layout) {//video switch btw frames
            if (nyWebView != null) nyWebView.onPause();
        } else if (viewId == R.id.mini_prev || viewId == R.id.main_prev) {
            backwardSlide = true;
            // the above for imageslide
            vIndex--;
            processList();
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (viewId == R.id.mini_next || viewId == R.id.main_next) {
            backwardSlide = false;
            // the above for imageslide
            vIndex++;
            processList();
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (viewId == R.id.main_play || viewId == R.id.mini_play) {
            playOn = !playOn;
            //TODO ny
            if (nyWebView != null) {
                if (playOn) nyWebView.onResume();
                else nyWebView.onPause();
            }
            adjustPlayerIcon();
            if (nyWebView != null && isImage(currUrl) && playOn)
                slideTiming(new Handler(), Update, 0, slideInterval);
        } else if (viewId == R.id.main_end || viewId == R.id.mini_end) {
            playOn = false;
            remove(this);
        } else if (viewId == R.id.main_shuffle) {
            Collections.shuffle(fileList, new Random());
            processList();
            //TODO ny process controls
        } else if (viewId == R.id.controls_download) {
            processToDownload();
        } else if (viewId == R.id.controls_playlist) {
            processToPlaylist();
        } else if (viewId == R.id.play_with_caster) {
            processToCaster();
        } else if (viewId == R.id.controls_popup) {
            processToPopup();
        } else if (viewId == R.id.controls_share) {
            //non-decription
            processToShare(hybridFile.getUri());
        } else if (viewId == R.id.controls_browser) {
            processToBrowser();
        } else if (viewId == R.id.controls_audio) {
            processToAudio();
        } else if (viewId == R.id.controls_infor) {
            processToInfor();
        }
    }

    protected void processToPlaylist() {
        if (hybridFile != null)
            new PlaylistDialog(requireActivity()).showDialog(hybridFile);
        else
            new PlaylistDialog(requireActivity()).showDialog(currUrl, NyHybrid.generateMode(requireActivity(), currUrl));
    }

    protected void processToCaster() {
    }

    protected void processToPopup() {
        //  nyWebView.onPause();
        //use nondiscrypted source link
        webDisplayUrl(requireActivity(), "", currUrl, false, true);
    }

    protected void processToShare(Uri mediaUri) {
        new Thread() {
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    // if (hybridFile.getPath().equals(redirectedUrl))
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setDataAndType(mediaUri, hybridFile.getMimeType());
                    PackageManager packageManager = getActivity().getPackageManager();
                    List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
                    if (resInfos != null && resInfos.size() > 0)
                        getActivity().startActivity(intent);
                    Log.e(TAG, "to External Uri =" + hybridFile.getUri().toString());
                } catch (Exception e) {
                  //  routeToExternal(hybridFile);
                    Log.e(TAG, "to External exception =" + e.toString());
                }
            }
        }.start();

    }

    protected void processToBrowser() {
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo("org.adblockplus.browser", 0);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity(), "Package Adblock Browser did not installed", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage("org.adblockplus.browser");
        intent.putExtra(Intent.EXTRA_TEXT, reroutedUri);
        intent.setData(Uri.parse(reroutedUrl));
        getActivity().startActivity(intent);
    }

    protected void processToAudio() {
    }

    protected void processToInfor() {
    }

    protected void processToDownload() {
        //  Log.e(TAG, "YoutubeId = " + youtubeId);
        if (youtubeId != null) {
            String link = "https://www.000tube.com/watch?v=" + youtubeId;
            // nyWebView.loadUrl(link);

            PackageManager pm = getActivity().getPackageManager();
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
        } else { //non-youtube video
            if (reroutedUrl != null)
                downloadFromUrl(getActivity(), reroutedUrl, getLastSegmentFromString(currUrl), NyFileUtil.getFileNameWithoutExtFromPath(currUrl));
        }
    }

    public void routeToInternal(Uri mediaUri) {
        routeToExternal(mediaUri);
    }

    private void slideTiming(final Handler h, final Runnable run, int delay, int period) {
        setupWebView();
        if (fileList == null || fileList.size() == 1) {
            processList();
            return;
        }
        if (swipeTimer != null) swipeTimer.cancel();
        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (playOn && fileList.size() > 1) h.post(run);
            }
        }, delay, period);
    }

    private Runnable Update = new Runnable() {
        public void run() {
            processList();
            if (backwardSlide) vIndex--;
            else vIndex++;
            SlideOn = true;
        }
    };


    private void adjustPlayerIcon() {
        if (playOn) {
            mainPlay.setImageResource(R.drawable.ic_pause);
            miniPlay.setImageResource(R.drawable.ic_pause);
        } else {
            mainPlay.setImageResource(R.drawable.ic_play);
            miniPlay.setImageResource(R.drawable.ic_play);
        }
    }

    private CustomAdapter adapter;

    public void initListSub() {
        ArrayList<String> listItems = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            String name = fileList.get(i).getName();
            if (hybridFile.getName().equals(name)) vIndex = i;
            listItems.add(name.replace("e=", "")); //fix for zip entries
        }

        mListSub = (ListView) root.findViewById(R.id.list_sub);
        adapter = new CustomAdapter(mActivity, listItems);
        //  ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, listItems);
        mListSub.setAdapter(adapter);
        adapter.setSelectedItem(vIndex);
        // Set choice mode to single
        //  mListSub.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //  mListSub.setItemChecked(vIndex, true);
        mListSub.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Clear previous selections
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                vIndex = position;
                //   mListSub.clearChoices();
                // Highlight the selected item
                //    mListSub.setItemChecked(position, true);
                processList();
            }
        });


        mListSub.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //  mListSub.clearChoices();
                // Highlight the selected item
                adapter.setSelectedItem(position);
                vIndex = position;
                if (longClickListner != null) {
                    //   Log.e(TAG, "longClickListner for "+fileList.get(vIndex).getPath());
                    if (hybridFile != null)
                        longClickListner.onFilePressed(new NyHybrid(hybridFile.getMode(), fileList.get(vIndex).getPath()), view);
                    else
                        longClickListner.onFilePressed(new NyHybrid(OpenMode.ONLINE, fileList.get(vIndex).getPath()), view);
                    // initListSub();
                    //   adapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });

        mainShuffle.setVisibility(fileList.size() > 1 ? VISIBLE : View.GONE);
        //  miniShuffle.setVisibility(fileList.size() > 1 ? VISIBLE : View.GONE);
    }


    // @Override
    public void destroy() {
        //must be executed first
        if (sheetBehavior != null) sheetBehavior = null;
        if (swipeTimer != null) swipeTimer.cancel();
        if (nyWebView != null) {
            nyWebView.onPause();
            nyWebView.clearHistory();
            nyWebView.clearCache(true);
            nyWebView.clearFormData();
            nyWebView.removeJavascriptInterface("channel");
            nyWebView.destroy();
            //  maybeCloseInputStream();
            nyWebView = null;
        }
        activityUiAction(true);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //  if (hybridFile.getMimeType().contains("video")) adjustVideoSize();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainPlayer.setVisibility(View.VISIBLE);
            mainPlayer.setBackgroundColor(getContext().getResources().getColor(R.color.colorSilver));
            mainShuffle.setVisibility(View.GONE);
        } else if (newConfig.orientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) { //inlandscape
            SystemUtils.hideSystemUI(getContext());
            mainShuffle.setVisibility(VISIBLE);
            mainPlayer.setBackgroundColor(semiTransparentColor);
            mainPlayer.setVisibility(View.INVISIBLE);
        }

        activityUiAction(sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public boolean onBackPressed() {
        //TODO ny handle fullscreen goback
        remove(this);
        return true;
    }


    protected void activityUiAction(boolean YesOrNo) {
      /*  ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        Log.e(TAG, "ActionBar actionBar " + actionBar);
        if (actionBar != null) {
            if (YesOrNo) {
                actionBar.show();
            } else {
                // actionBar.hide();
                //TODO ny do not use actionBar.hide()
                SystemUtils.hideSystemUIAndActionBar(requireActivity());
            }
        }*/
    }


    public interface LongClickListner {
        void onFilePressed(NyHybrid file, View view);
    }

    //--------------------------------------------------------------------//

    /*   private MediaPlaybackService mediaService;
       private ServiceConnection mConnection = new ServiceConnection() {

           @Override
           public void onServiceConnected(ComponentName className, IBinder service) {
               MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
               mediaService = binder.getService();
               mediaService.setWebView(nyWebView); // Pass the WebView reference to the Service
           }

           @Override
           public void onServiceDisconnected(ComponentName arg0) {
           }
       };

       private void bindToService() {
           Intent intent = new Intent(getActivity(), MediaPlaybackService.class);
           getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
       }

       // Method to unbind from the Service
       private void unbindFromService() {
           getActivity().unbindService(mConnection);
       }

       @Override
       public void onStart() {
           super.onStart();
           bindToService();
       }

       @Override
       public void onStop() {
           super.onStop();
           unbindFromService();
       }*/
    class CustomAdapter extends ArrayAdapter<String> {

        private int selectedItem = -1; // Initially no item selected

        public CustomAdapter(Context context, List<String> items) {
            super(context, android.R.layout.simple_list_item_1, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // Highlight the selected item
            if (position == selectedItem) {
                view.setBackgroundColor(getResources().getColor(R.color.md_light_blue_200)); // Set your desired highlight color
            } else {
                view.setBackgroundColor(Color.TRANSPARENT); // Reset background color
            }

            return view;
        }

        public void setSelectedItem(int position) {
            selectedItem = position;
            notifyDataSetChanged(); // Notify ListView to update views
        }
    }

}
