package com.nytaiji.nybase;


import static com.nytaiji.nybase.network.VideoPlayNdownload.downloadFromUrl;
import static com.nytaiji.nybase.utils.NyFileUtil.GOOGLE_PHOTOS_PACKAGE_NAME;
import static com.nytaiji.nybase.utils.NyFileUtil.getYoutubeId;
import static com.nytaiji.nybase.utils.SystemUtils.copyToClipboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.playlist.PlaylistDialog;
import com.nytaiji.nybase.utils.ShareUtils;
import com.nytaiji.nybase.view.OldNyWebView;

import java.util.Objects;


public class NyYoutubeFragment extends NyBaseFragment {
    private OldNyWebView youtubeView;

    private boolean inList = false;

    private FloatingActionButton fab;

    private String VID = null;

    private String extractId = null;

    public String extractLink = null;

    public String extractName = null;

    public String mediaLink = null;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {

        activityUiAction(false);

        View root = inflater.inflate(R.layout.fragment_youtube, container, false);
        youtubeView = root.findViewById(R.id.web_view);
        youtubeView.setThirdPartyCookiesEnabled(true);
        youtubeView.setFocusableInTouchMode(true);

    /*    youtubeView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String str, Bitmap bitmap) {
                super.onPageStarted(view, str, bitmap);
            }

            @Override
            public void onPageFinished(WebView view, String str) {
                super.onPageFinished(view, str);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("?app=desktop") && !url.contains("signin?app=desktop")) {
                    Toast.makeText(getContext(), "Desktop View Unavailable", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                String url = String.valueOf(request.getUrl());
                if (url.contains("//m.youtube.com/watch?")) {
                    Log.e(TAG, "url= " + url);
                    //Video Id
                    VID = url.substring(url.indexOf("v=") + 2).split("&")[0];
                    if ( VID != null) {
                        mediaLink = "https://m.youtube.com/" + VID;
                        extractId = VID;
                        Log.e(TAG, "extractId = " + extractId);
                        retrieveVideoStreams();
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });*/

        youtubeView.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                String url = String.valueOf(request.getUrl());

                //Method 1
                if (url.contains("//m.youtube.com/watch?")) {
                    Log.e(TAG, "url= " + url);
                    //Video Id
                    VID = url.substring(url.indexOf("v=") + 2).split("&")[0];
                    if (VID != null) {
                        mediaLink = "https://m.youtube.com/" + VID;
                        extractId = VID;
                        handleExtractId();
                    }
                    return super.shouldInterceptRequest(view, request);
                } //Method 2
                else if (!url.contains("stats") && url.contains("video_id=") && !url.contains("pltype=adhost")) {
                    // Log.e(TAG, "video link = " + url);
                    VID = url.substring(url.indexOf("video_id=") + 9).split("&")[0];
                    if (VID != null) {
                        mediaLink = "https://youtu.be/" + VID;
                        extractId = VID;
                        handleExtractId();
                    }
                    return new WebResourceResponse("text/plain", "utf-8", null);
                }

                // Method 3
                if (isAdsVideo(url))
                    return new WebResourceResponse("text/plain", "utf-8", null);

                if (url.contains("list=")) {
                    inList = true;
                }

                if (!toSkip(url)) {
                    Handler handler = new Handler(requireActivity().getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String load = youtubeView.getUrl();
                            //  Log.e(TAG, "url = " + url);
                            // Log.e(TAG, "view.getUrl() = " + load);
                            VID = getYoutubeId(load);
                            if (VID != null) VID = VID.replace("shorts/", "");
                            if (VID != null && !Objects.equals(extractId, VID)) {
                                extractId = VID;
                                mediaLink = "https://youtu.be/" + extractId;
                                handleExtractId();
                            }
                        }
                    });
                }
                return super.shouldInterceptRequest(view, request);
            }
        });


        youtubeView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    VID = null;
                    fab.setVisibility(View.GONE);

                    if (keyCode == KeyEvent.KEYCODE_BACK && youtubeView.canGoBack()) {

                        // Navigate back by the top of playlist
                        //
                        int steps = youtubeView.copyBackForwardList().getSize();
                        if (inList && steps > 2) {
                            youtubeView.goBackOrForward(-steps + 2);
                            inList = !inList; //TO the top of the list, will be become true once we play another video in the list
                        } else youtubeView.goBackOrForward(-steps + 1);
                        return true;  // Consumes the back button press
                    }
                }
                return false;
            }
        });

        fab = root.findViewById(R.id.floatingActionButton);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(v -> {
            proceedToExtracted();
        });

        youtubeView.canGoBack();
        youtubeView.loadUrl("https://m.youtube.com/");

        return root;
    }

    protected void proceedToExtracted() {

    }

    private boolean isAdsVideo(String url) {
        return url.contains("pagead") || url.contains("adview") || url.contains("ad_status")
                || url.contains("adhost") || url.contains("shop") || url.contains("ad.js");
    }

    private boolean toSkip(String url) {
        return url.contains("log_event") || url.contains("youtubei") || url.contains("log?") || url.contains("adhost") || url.contains("i.ytimg.com")
                || url.contains("yt3") || url.contains("/js/") || url.contains("simgad") || url.contains("ptracking") || url.contains("generate") || url.contains("static")
                || url.contains("googleapis") || url.contains("/s/") || url.contains("stats");
    }


    public void handleExtractId() {
        copyToClipboard(requireActivity(), extractLink);
        fab.setVisibility(View.VISIBLE);
    }


    public void handleSelectedStream() {
        PopupMenu popupMenu = new PopupMenu(requireActivity(), fab);
        // popupMenu.inflate(R.menu.youtube); // Inflate the playlist menu XML
        popupMenu.getMenuInflater().inflate(R.menu.youtube, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.youtube_downloading) {
                    downloadFromUrl(getActivity(), extractLink, extractName + ".mp4", extractName);
                    return true;
                } else if (itemId == R.id.youtube_share) {// Handle web share action
                    //extractLink too long to convert to Uri in WebshareUtil
                    // getWebShareLink(mainActivity, extractLink, false);
                    copyToClipboard(requireActivity(), extractLink);
                    return true;
                } else if (itemId == R.id.youtube_cpy) {//
                    copyToClipboard(requireActivity(), mediaLink);
                    return true;
                } else if (itemId == R.id.youtube_open) {// Handle open in external
                    playOnExternalPlayer(requireActivity(),
                            extractName, extractLink);
                    return true;
                } else if (itemId == R.id.youtube_add_playlist) {
                    new PlaylistDialog(requireActivity()).showDialog(new NyHybrid(OpenMode.ONLINE, mediaLink));
                    return true;
                }
                return false;
            }
        });

        // Show the PopupMenu at the clicked position
        popupMenu.show();

    }


    @Override
    public void onResume() {
        super.onResume();
        activityUiAction(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityUiAction(true);
    }


    protected void activityUiAction(boolean YesOrNo) {
    }


    public static void playOnExternalPlayer(@NonNull final Context context,
                                            @Nullable final String name,
                                            @NonNull final String link) {

        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(link), "video/*");
        intent.putExtra(Intent.EXTRA_TITLE, name);
        intent.putExtra("title", name);
        //   intent.putExtra("artist", artist);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resolveActivityOrAskToInstall(context, intent);
    }


    public static void resolveActivityOrAskToInstall(@NonNull final Context context,
                                                     @NonNull final Intent intent) {
        if (!ShareUtils.tryOpenIntentInApp(context, intent)) {
            if (context instanceof Activity) {
                new AlertDialog.Builder(context)
                        .setMessage("No app found")
                        .setPositiveButton("Installing", (dialog, which) ->
                                ShareUtils.installApp(context, GOOGLE_PHOTOS_PACKAGE_NAME))
                        .setNegativeButton(R.string.cancel, (dialog, which) ->
                                Log.i("NavigationHelper", "You unlocked a secret unicorn."))
                        .show();
            } else {
                Toast.makeText(context, "No app to open intent", Toast.LENGTH_LONG).show();
            }
        }
    }
}
