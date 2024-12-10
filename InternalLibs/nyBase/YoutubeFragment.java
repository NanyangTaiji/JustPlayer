package com.amaze.filemanager.nyAddons;


import static com.amaze.filemanager.nyAddons.ListHelper.getUrlAndNonTorrentStreams;
import static com.amaze.filemanager.nyAddons.NyOpenUtil.getWebShareLink;
import static com.nytaiji.nybase.network.VideoPlayNdownload.downloadFromUrl;
import static com.nytaiji.nybase.utils.NyFileUtil.GOOGLE_PHOTOS_PACKAGE_NAME;
import static com.nytaiji.nybase.utils.NyFileUtil.getYoutubeId;
import static com.nytaiji.nybase.utils.SystemUtils.copyToClipboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.amaze.filemanager.R;
import com.amaze.filemanager.core.HybridFile;
import com.amaze.filemanager.core.MainActivity;
import com.amaze.filemanager.fileoperations.filesystem.OpenMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nytaiji.nybase.NyBaseFragment;
import com.nytaiji.nybase.httpShare.WifiShareUtil;
import com.nytaiji.nybase.playlist.PlaylistDialog;
import com.nytaiji.nybase.utils.ShareUtils;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.nybase.view.NyWebView;
import com.nytaiji.nybase.view.OldNyWebView;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class YoutubeFragment extends NyBaseFragment {
    private OldNyWebView youtubeView;
    private String mediaLink = null;

    private boolean inList = false;
    private MainActivity mainActivity;

    private FloatingActionButton fab;

    private String VID = null;

    private String extractId = null;

    private String extractLink = null;
    private StreamInfo currentInfo = null;

    private Button positiveButton;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
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
                //block ing
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
                            Log.e(TAG, "view.getUrl() = " + load);
                            VID = getYoutubeId(load);
                            if (VID != null) VID = VID.replace("shorts/", "");
                            if (VID != null && !Objects.equals(extractId, VID)) {
                                extractId = VID;
                                Log.e(TAG, "extractId = " + extractId);
                                retrieveVideoStreams();
                            }
                        }
                    });
                  /*  ((MainActivity) requireActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String load = youtubeView.getUrl();
                            //  Log.e(TAG, "url = " + url);
                            Log.e(TAG, "view.getUrl() = " + load);
                            VID = getYoutubeId(load);
                            if (VID != null) VID = VID.replace("shorts/", "");
                            if (VID != null && !Objects.equals(extractId, VID)) {
                                extractId = VID;
                                Log.e(TAG, "extractId = " + extractId);
                                retrieveVideoStreams();
                            }
                        }
                    });*/
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
            showVideoQualityDialog();
            //  retrieveVideoStreams(VID);
        });

        youtubeView.canGoBack();
        youtubeView.loadUrl("https://m.youtube.com/");

        return root;
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


    private StreamInfo getStreamInfo(String url) throws IOException, ExtractionException {
        return StreamInfo.getInfo(NewPipe.getService(0), url);
    }

    private void retrieveVideoStreams() {
        new Thread() {
            @Override
            public void run() {
                currentInfo = null;
                mediaLink = "https://youtu.be/" + extractId;
                //---------------------------------------------------//
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        StreamInfo info = getStreamInfo(mediaLink);
                        if (info != null) {
                            currentInfo = info;
                            Log.e(TAG, "currentInfo " + currentInfo.getName());
                            ((MainActivity) getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //   showVideoQualityDialog();
                                    fab.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        // processInfo(info);
                    } catch (IOException e) {
                        // Handle specific exceptions
                        Log.e(TAG, "Error fetching stream info", (Throwable) e);
                    } catch (Exception e) {
                        // Handle other exceptions
                        Log.e(TAG, "Unexpected error", e);
                    }
                });

                // Wait for the CompletableFuture to complete (blocking operation)
                try {
                    Log.e(TAG, "start future");
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    // Handle exceptions during CompletableFuture.get()
                    Log.e(TAG, "Error waiting for completion", e);
                }

                interrupt();
            }
        }.start();
    }

    private AlertDialog alertDialog;
    private List<VideoStream> videoStreams;

    private int index;

    private void showVideoQualityDialog() {
        if (currentInfo == null) {
            Toast.makeText(getActivity(), "No extracted stream available", Toast.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(currentInfo.getName());

        videoStreams = ListHelper.getSortedStreamVideosList(mActivity,
                getUrlAndNonTorrentStreams(currentInfo.getVideoStreams()),
                getUrlAndNonTorrentStreams(currentInfo.getVideoOnlyStreams()),
                false, false
        );

        if (videoStreams.isEmpty()) {
            builder.setMessage(R.string.no_video_streams);
            builder.setPositiveButton(com.nytaiji.nybase.R.string.confirm, null);

        } else {
            final int selectedVideoStreamIndexForExternalPlayers =
                    ListHelper.getDefaultResolutionIndex(mActivity, videoStreams);
            final CharSequence[] resolutions = videoStreams.stream()
                    .map(VideoStream::getResolution).toArray(CharSequence[]::new);

            builder.setSingleChoiceItems(resolutions, selectedVideoStreamIndexForExternalPlayers,
                    null);
            builder.setNegativeButton(com.nytaiji.nybase.R.string.cancel, null);
            builder.setNeutralButton(R.string.exo_controls_play_description, (dialog, i) -> {
                youtubeView.goBack();

                index = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                extractLink = videoStreams.get(index).getUrl();
                DisplayFragment.show(getParentFragmentManager(), extractLink);

            });
            builder.setPositiveButton(R.string.processing, (dialog, i) -> {
                index = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                extractLink = videoStreams.get(index).getUrl();
                handleSelectedStream();
            });
        }

        alertDialog = builder.create();
        alertDialog.show();

        positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

    }

    private void handleSelectedStream() {

        PopupMenu popupMenu = new PopupMenu(requireActivity(), fab);
        // popupMenu.inflate(R.menu.youtube); // Inflate the playlist menu XML
        popupMenu.getMenuInflater().inflate(R.menu.youtube, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.youtube_downloading) {
                    downloadFromUrl(getActivity(), extractLink, currentInfo.getName() + ".mp4", currentInfo.getName());
                    return true;
                } else if (itemId == R.id.youtube_share) {// Handle web share action
                    //extractLink too long to convert to Uri in WebshareUtil
                   // getWebShareLink(mainActivity, extractLink, false);
                    copyToClipboard(mainActivity, extractLink);
                    return true;
                } else if (itemId == R.id.youtube_cpy) {//
                    copyToClipboard(mainActivity, mediaLink);
                    return true;
                } else if (itemId == R.id.youtube_open) {// Handle open in external
                    playOnExternalPlayer(mainActivity,
                            currentInfo.getName(), videoStreams.get(index));
                    return true;
                } else if (itemId == R.id.youtube_add_playlist) {
                    new PlaylistDialog(mainActivity).showDialog(new HybridFile(OpenMode.ONLINE, mediaLink));
                    return true;
                }
                return false;
            }
        });

        // Show the PopupMenu at the clicked position
        popupMenu.show();

    }

    private String getDefaultStream(StreamInfo info) {
        String url = null;
        final List<VideoStream> videoStreams = info.getVideoStreams();
        final List<VideoStream> videoStreamsForExternalPlayers =
                ListHelper.getSortedStreamVideosList(getActivity(),
                        getUrlAndNonTorrentStreams(videoStreams), null, false, false);
        if (videoStreamsForExternalPlayers.isEmpty()) {
            url = "";
        } else {
            final int index = ListHelper.getDefaultResolutionIndex(getActivity(), videoStreamsForExternalPlayers);
            url = videoStreamsForExternalPlayers.get(index).getUrl();
        }
        return url;
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
        ActionBar actionBar = mainActivity.getSupportActionBar();
        //  Log.e(TAG, "ActionBar actionBar " + actionBar);
        if (actionBar != null) {
            if (YesOrNo) {
                actionBar.show();
                mainActivity.getAppbar().getBottomBar().setVisibility(false);
            } else {
                //TODO ny do not use actionBar.hide()
                SystemUtils.hideSystemUIAndActionBar(mainActivity);
            }
        }
        mainActivity.getAppbar().setVisibility(YesOrNo);
    }


    public static void playOnExternalPlayer(@NonNull final Context context,
                                            @Nullable final String name,
                                            @NonNull final Stream stream) {
        final DeliveryMethod deliveryMethod = stream.getDeliveryMethod();
        final String mimeType;

        switch (deliveryMethod) {
            case PROGRESSIVE_HTTP:
                if (stream.getFormat() == null) {
                    if (stream instanceof AudioStream) {
                        mimeType = "audio/*";
                    } else if (stream instanceof VideoStream) {
                        mimeType = "video/*";
                    } else {
                        // This should never be reached, because subtitles are not opened in
                        // external players
                        return;
                    }
                } else {
                    mimeType = stream.getFormat().getMimeType();
                }
                break;
            case HLS:
                mimeType = "application/x-mpegURL";
                break;
            case DASH:
                mimeType = "application/dash+xml";
                break;
            case SS:
                mimeType = "application/vnd.ms-sstr+xml";
                break;
            default:
                // Torrent streams are not exposed to external players
                mimeType = "";
        }

        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(stream.getContent()), mimeType);
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
                        .setPositiveButton(R.string.install, (dialog, which) ->
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
