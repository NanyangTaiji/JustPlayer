package com.nytaiji.player.chromecast;

import static com.nytaiji.exoplayer.GoogleExoPlayer.getGoogLeMimeType;
import static com.nytaiji.nybase.filePicker.MediaSelection.REQUEST_CODE_GET_ALL;
import static com.nytaiji.nybase.filePicker.MediaSelection.REQUEST_CODE_GET_IMAGE;
import static com.nytaiji.nybase.filePicker.MediaSelection.REQUEST_CODE_GET_VIDEO;
import static com.nytaiji.nybase.filePicker.MediaSelection.getMediaLinkDialog;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getMessageHandler;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getPreferredServerUrl;
import static com.nytaiji.nybase.model.Constants.ANDROID_PLAYER;
import static com.nytaiji.nybase.model.Constants.EXTERNAL_PLAYER;
import static com.nytaiji.nybase.model.Constants.FAN_PLAYER;
import static com.nytaiji.nybase.model.Constants.GOOGLE_PLAYER;
import static com.nytaiji.nybase.model.Constants.KEY_PLAYER;
import static com.nytaiji.nybase.model.Constants.VLC_PLAYER;
import static com.nytaiji.nybase.network.VideoPlayNdownload.downloadFromUrl;
import static com.nytaiji.nybase.utils.NyFileUtil.getRealPathFromURI;
import static com.nytaiji.nybase.utils.NyFileUtil.isDocument;
import static com.nytaiji.nybase.utils.NyFileUtil.isImage;
import static com.nytaiji.nybase.utils.NyFileUtil.isMedia;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;
import static com.nytaiji.nybase.utils.NyFileUtil.showSingleChoiceDialog;
import static com.nytaiji.nybase.utils.NyMimeTypes.getMimeType;
//import static com.nytaiji.player.muPdf.FileViewerUtils.getMimeType;
//import static com.nytaiji.player.muPdf.MuPDFActivity.muPdfDisplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.nytaiji.nybase.NyBaseFragment;
import com.nytaiji.nybase.OldPermissionsActivity;
import com.nytaiji.nybase.httpShare.WifiShareUtil;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.GeneralCallback;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.PreferenceHelper;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.player.BasePlayerActivity;
import com.nytaiji.player.R;
import com.nytaiji.player.YoutubeFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CastActivity extends OldPermissionsActivity {

    private static String TAG = "CastActivity";
    public CastContext mCastContext;
    private IntroductoryOverlay mIntroductoryOverlay;

    private SharedPreferences sharedPrefs;

    public String mediaLink = null;

    public String mimeType = null;

    public CastSession mCastSession;
    private SessionManager sessionManager;

    public SessionManagerListener mSessionManagerListener;

    public String mediaStream = null;

    public Uri selectedUri = null;
    private boolean isException = false;

    public String stringTitle = null;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //---------------------------------------//
        SystemUtils.hideSystemUI(this);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        this.setUpCastListener();
        this.mCastContext = CastContext.getSharedInstance((Context) this);

        mCastContext.addCastStateListener((CastStateListener) (new CastStateListener() {
            public void onCastStateChanged(int state) {
                if (state != 1) {
                    //TODO ny
                  //  CastActivity.this.showIntroductoryOverlay();
                }

            }
        }));
        sessionManager = mCastContext.getSessionManager();
        sessionManager.addSessionManagerListener(mSessionManagerListener, CastSession.class);
        //TODO  important to disable at starting
        disconnectCaster();
     //   initYoutube();
    }

    @Override
    protected void postPermissionGranted() {
        if (getIntent() != null && getIntent().getData() != null) intentHandle(getIntent());
    }

    public void setMediaLink(String link) {
        mediaLink = link;
      //  if (menuDownload != null) menuDownload.setVisible(isDistantLink(link));
    }

    private boolean isDistantLink(String link) {
        if (link != null && link.contains("http") && !link.contains("192.168")) return true;
        else return false;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        intentHandle(intent);
    }

    private void intentHandle(Intent intent) {
        //if (intent.getData() == null) return;

        Log.e(TAG, "intent.getData() " + intent.getData().toString());
        mediaLink = externalIntentToPath(this, intent);
        if (mediaLink != null && !mediaLink.contains("smb") && !mediaLink.contains("http")) {
            stopMedia();
            if (mediaLink.contains("/storage/"))
                mediaLink = mediaLink.substring(mediaLink.indexOf("/storage/"));
            Log.e(TAG, "intent mediaLink " + mediaLink);
            selectedUri = Uri.parse(mediaLink);
            // buildNplayMedia(mediaLink);
        } else {
            selectedUri = intent.getData();
        }
        playMedia(selectedUri);
    }


    private MediaInfo mediaInfo = null;

    public void buildNplayMedia(String mediaLink) {
        CastActivity.this.invalidateOptionsMenu();
        if (!isOnline(mediaLink) && !mediaLink.contains("file://"))
            mediaLink = "file://" + mediaLink;
        isException = NyFileUtil.isSpecialMedia(mediaLink);
        playMedia(Uri.parse(mediaLink));
    }


    public void playMedia(Uri mediaUri) {
        if (mediaUri != null && mediaUri.getPath() != null)
            mimeType = getGoogLeMimeType(this, mediaUri.getPath());
        if (this.mCastSession != null) {

            NyHybrid hybrid = new NyHybrid(mediaUri);
            hybrid.setPath(mediaUri.getPath());
            if (!isOnline(mediaLink)) {
                mediaStream = getPreferredServerUrl(false, true);
                WifiShareUtil.stopHttpServer();

                WifiShareUtil.httpShare(this, hybrid, mediaStream, getMessageHandler(this.findViewById(R.id.content)));
                WifiShareUtil.setUnique(false);
            } else mediaStream = mediaLink;
            //for LibmediaStreamer  //not able to stop the previous one
            // mediaStream = getServerLink(hybrid, getMessageHandler(findViewById(R.id.content)));
            setupMediaInfo();
            casting();
        } else{
            playWithPlayer(mediaUri);
        }
    }

    public void playWithPlayer(Uri mediaUri){

    }

    private void casting() {
        CastSession castSession = this.mCastSession;
        RemoteMediaClient mediaClient = castSession.getRemoteMediaClient();
        if (mediaClient != null) {
            this.startService(new Intent((Context) this, CastService.class));
            final RemoteMediaClient remoteMediaClient = mediaClient;

            //for the case that casting from local player
            if ((mediaInfo == null || mediaStream == null) && mediaLink != null) {
                if (!isOnline(mediaLink)) {
                    NyHybrid hybrid = new NyHybrid(mediaLink);
                    mimeType = getGoogLeMimeType(this, mediaLink);
                    mediaStream = getPreferredServerUrl(false, true);
                    WifiShareUtil.stopHttpServer();
                    WifiShareUtil.httpShare(this, hybrid, mediaStream, getMessageHandler(this.findViewById(R.id.content)));
                    WifiShareUtil.setUnique(false);
                } else mediaStream = mediaLink;

                //for LibmediaStreamer  //not able to stop the previous one
                // mediaStream =getServerLink(hybrid, getMessageHandler(findViewById(R.id.content)));
                setupMediaInfo();
            }

            //TODO ny not enter ExpandedControlsActivity directly
                   /* remoteMediaClient.registerCallback((RemoteMediaClient.Callback) (new RemoteMediaClient.Callback() {
                        public void onStatusUpdated() {
                            Intent intent = new Intent((Context) MainActivity.this, ExpandedControlsActivity.class);
                            MainActivity.this.startActivity(intent);
                            remoteMediaClient.unregisterCallback((RemoteMediaClient.Callback) this);
                        }
                    }));*/
            remoteMediaClient.load((new MediaLoadRequestData.Builder()).setMediaInfo(mediaInfo).setAutoplay(true).setCurrentTime(0L).build());
        }

    }


    private void setupMediaInfo() {
        String testImageUrl1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/DesigningForGoogleCast2-480x270.jpg";
        String testImageUrl2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/780x1200/DesigningForGoogleCast-887x1200.jpg";

        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "NyCaster");
//        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, getStringTitle());
        movieMetadata.addImage(new WebImage(Uri.parse(testImageUrl1)));
        movieMetadata.addImage(new WebImage(Uri.parse(testImageUrl2)));

        // MediaTrack mediaTrack = (new MediaTrack.Builder(1L, MediaTrack.TYPE_TEXT)).setName("English").setSubtype(1).setLanguage("en-US").build();
        Log.e(TAG, "mediaStream = " + mediaStream);
        Log.e(TAG, "mimeType = " + mimeType);
        mediaInfo = new MediaInfo.Builder(mediaStream)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(mimeType)
                .setMetadata(movieMetadata).build();
    }


    public static String externalIntentToPath(Context context, Intent intent) {
        Uri uri;
        String mUrl = null;
        uri = intent.getData();  //from selection of App
        if (uri != null) {
            Log.e(TAG, "Intent.getData()");
        } else {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM); //from share action
            if (uri != null) Log.e(TAG, "Intent.EXTRA_STREAM");
        }

        if (uri != null) {
            String path = uri.getPath();
            Log.e(TAG, "uri.getPath(): " + uri.getPath());

            if (path.contains("/storage")) mUrl = path.substring(path.indexOf("/storage"));
                //for data from pickup
            else {
                mUrl = NyFileUtil.getPath(context, uri);
                Log.e(TAG, "mFileUtil.getPath: " + mUrl);
            }
            //   if (path.contains("external/video")||mUrl.contains("external/video")) {
            if (path.contains("media/") || mUrl.contains("media/") || path.contains("external/") || mUrl.contains("external/")) {
                //for normal local and smb local
                mUrl = getRealPathFromURI(context, uri);
                // mUrl =queryMediaAbsolutePath(context, uri);
                //TODO
                Log.e(TAG, "getRealPathFromURI: " + mUrl);
            }
            if (mUrl.contains("file:")) {
                //for encrypted local
                File file = new File(mUrl);
                mUrl = file.getAbsolutePath();
                Log.e(TAG, "File(mUrl).getAbsolutePath(): " + mUrl);
            }
        } else {
            CharSequence uriCharSequence = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (uriCharSequence != null) {
                mUrl = uriCharSequence.toString();
                Log.e(TAG, "Intent.EXTRA_TEXT: " + mUrl);
            }
        }

        //remove the speical characters
      /*  try {
            mUrl= URLDecoder.decode(mUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

        //
        Log.e(TAG, "mUrl " + mUrl);
        return mUrl;
    }


   /* private void imagesInCurrentAndSubDir(String path) {
        File tempfile = new File(path);
        File[] files = tempfile.listFiles();
        Log.e(TAG, "path " + path);
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    Log.e("InDirectories ", "directory " + path);
                    //  videoDirectories.add(file.getAbsolutePath());
                    //  Log.e("directory Fetch ", file.getName());
                    imagesInCurrentAndSubDir(file.getAbsolutePath());
                } else {
                    if (isImage(file.getName())) {
                        Log.e("ImageFile ", "path " + path + ";" + file.getName());
                        mediaQueue.add(file.getAbsolutePath());
                        if (file.getName().equals(startFile)) vIndex = fileCount;
                        fileCount++;
                    }
                }
            }
        }
    }*/


    public final void stopMedia() {

        //method 1
        //SimpleWebServer.stopServer();
        //mehtod 2
        WifiShareUtil.stopHttpServer();

        CastSession castSession = this.mCastSession;
        if (castSession != null) {
            RemoteMediaClient mediaClient = castSession.getRemoteMediaClient();
            if (mediaClient != null) {
                mediaClient.stop();
            }

        }
    }

    private void disconnectCaster() {
        if (sessionManager != null) sessionManager.endCurrentSession(true);

    }


    private final void setUpCastListener() {
        this.mSessionManagerListener = (SessionManagerListener) (new SessionManagerListener() {
            private final void onApplicationConnected(CastSession castSession) {
                CastActivity.this.mCastSession = castSession;
                CastActivity.this.invalidateOptionsMenu();
                if (mediaLink != null) casting();
                Log.e(TAG, "onApplicationConnected");
            }

            private final void onApplicationDisconnected() {
                CastActivity.this.mCastSession = null;
                CastActivity.this.invalidateOptionsMenu();
                Log.e(TAG, "onApplicationDisconnected");
            }

            public void onSessionStarted(@NotNull CastSession p0, @NotNull String p1) {
                this.onApplicationConnected(p0);
                Log.e(TAG, "onSessionStarted");
            }

            public void onSessionStarted(Session var1, String var2) {
                this.onSessionStarted((CastSession) var1, var2);
            }

            public void onSessionResumeFailed(@NotNull CastSession p0, int p1) {
                this.onApplicationDisconnected();
                Log.e(TAG, "onSessionResumeFailed");
            }

            public void onSessionResumeFailed(Session var1, int var2) {
                this.onSessionResumeFailed((CastSession) var1, var2);
            }

            public void onSessionEnded(@NotNull CastSession p0, int p1) {
                this.onApplicationDisconnected();
                Log.e(TAG, "onSessionEnded");
            }

            public void onSessionEnded(Session var1, int var2) {
                this.onSessionEnded((CastSession) var1, var2);
            }

            public void onSessionResumed(@NotNull CastSession p0, boolean p1) {
                this.onApplicationConnected(p0);
                Log.e(TAG, "onSessionResumed");
            }

            public void onSessionResumed(Session var1, boolean var2) {
                this.onSessionResumed((CastSession) var1, var2);
            }

            public void onSessionStartFailed(@NotNull CastSession p0, int p1) {
                this.onApplicationDisconnected();
            }

            public void onSessionStartFailed(Session var1, int var2) {
                this.onSessionStartFailed((CastSession) var1, var2);
            }

            public void onSessionSuspended(@NotNull CastSession p0, int p1) {
                Log.e(TAG, "onSessionSuspended");
            }

            public void onSessionSuspended(Session var1, int var2) {
                this.onSessionSuspended((CastSession) var1, var2);
            }

            public void onSessionStarting(@NotNull CastSession p0) {
            }

            public void onSessionStarting(Session var1) {
                this.onSessionStarting((CastSession) var1);
            }

            public void onSessionResuming(@NotNull CastSession p0, @NotNull String p1) {
            }

            public void onSessionResuming(Session var1, String var2) {
                this.onSessionResuming((CastSession) var1, var2);
            }

            public void onSessionEnding(@NotNull CastSession p0) {
            }

            public void onSessionEnding(Session var1) {
                this.onSessionEnding((CastSession) var1);
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        SystemUtils.hideSystemUI(this);

        //no use, not able to disconnect google photos from connection to caster
        //killBackgroundProcess(this, GOOGLE_PHOTOS_PACKAGE_NAME);
        //queueHandle();
    }


    @Override
    protected void onDestroy() {
        stopMedia();
        disconnectCaster();
        if (CastService.getInstance() != null) CastService.getInstance().onDestroy();
        super.onDestroy();
    }


    private void showIntroductoryOverlay(MenuItem button) {
        if (mIntroductoryOverlay != null) mIntroductoryOverlay.remove();

      if (button != null && button.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder((Activity) CastActivity.this, button)
                            .setTitleText("Cast media to device")
                            .setSingleTime()
                            .setOnOverlayDismissedListener(new IntroductoryOverlay.OnOverlayDismissedListener() {
                                @Override
                                public void onOverlayDismissed() {
                                    mIntroductoryOverlay = null;
                                }
                            })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }


    //-----------------handle onBackPressed-----------

 /*   private boolean toExit = false;

    @Override
    public void onBackPressed() {

        toExit = !toExit;

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);
        // Or, if you added the fragment with a tag
        if (currentFragment != null && currentFragment.isAdded() && currentFragment instanceof NyBaseFragment) {
            toExit = ((NyBaseFragment) currentFragment).onBackPressed();
            if (toExit) {
                ((NyBaseFragment) currentFragment).onDestroy();
                fragmentManager.beginTransaction().remove(currentFragment).commitAllowingStateLoss();
                toExit = false;
                if (!(currentFragment instanceof YoutubeFragment)) initYoutube();
            }
        }
        if (toExit) {
            super.onBackPressed();
        }
    }*/


    public void SetStreamTitle(String title) {
        this.stringTitle = title;
    }

    public void SetStreamLink(String link) {
        mediaLink = link;
        //TODO ny
        buildMediaInfo(mediaLink, "video/mp4");
    }

    public String getStringTitle() {
        return stringTitle != null ? stringTitle : NyFileUtil.getLastSegmentFromString(mediaLink);
    }

    //for the use feom fragment
    public void buildMediaInfo(String path, String mimeTypeSet) {
        if (path == null && selectedUri != null) path = selectedUri.getPath();
        if (mimeTypeSet == null) mimeType = getMimeType(new File(path));
        else mimeType = mimeTypeSet;

        isException = NyFileUtil.isSpecialMedia(path);
        //the next is tmp fix for tiff
        setMediaLink(path);
        //

        if (!path.contains("http")) {
            //method 1
            // sampleVideoStream = webAddress.append(path).toString();
            //method 2
            //   sampleVideoStream = getStremerPath(this, new NyHybrid(path), false);
            //method 3
            mediaStream = getPreferredServerUrl(false, true);
            //  WifiShareUtil.stopHttpServer();
            WifiShareUtil.httpShare(this, new NyHybrid(path), mediaStream, getMessageHandler(this.findViewById(R.id.content)));
            WifiShareUtil.setUnique(true);

        } else {
            mediaStream = path;
        }

        setupMediaInfo();
    }

}