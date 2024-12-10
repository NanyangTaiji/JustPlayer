package com.nytaiji.player;

import static com.nytaiji.exoplayer.GoogleExoPlayer.getGoogLeMimeType;
import static com.nytaiji.nybase.filePicker.MediaSelection.REQUEST_CODE_GET_ALL;
import static com.nytaiji.nybase.filePicker.MediaSelection.REQUEST_CODE_GET_IMAGE;
import static com.nytaiji.nybase.filePicker.MediaSelection.REQUEST_CODE_GET_VIDEO;
import static com.nytaiji.nybase.filePicker.MediaSelection.getMediaLinkDialog;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getMessageHandler;
import static com.nytaiji.nybase.model.Constants.ANDROID_PLAYER;
import static com.nytaiji.nybase.model.Constants.EXTERNAL_PLAYER;
import static com.nytaiji.nybase.model.Constants.FAN_PLAYER;
import static com.nytaiji.nybase.model.Constants.GOOGLE_PLAYER;
import static com.nytaiji.nybase.model.Constants.KEY_PLAYER;
import static com.nytaiji.nybase.model.Constants.VLC_PLAYER;
import static com.nytaiji.nybase.network.NetworkServersDialog.getServerLink;
import static com.nytaiji.nybase.network.VideoPlayNdownload.downloadFromUrl;
import static com.nytaiji.nybase.utils.NyFileUtil.isDocument;
import static com.nytaiji.nybase.utils.NyFileUtil.isMedia;
import static com.nytaiji.nybase.utils.NyFileUtil.showSingleChoiceDialog;
//import static com.nytaiji.player.muPdf.MuPDFActivity.muPdfDisplay;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.nytaiji.nybase.NyBaseFragment;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.GeneralCallback;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.PreferenceHelper;
import com.nytaiji.nybase.utils.SystemUtils;
import com.nytaiji.player.chromecast.CastActivity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class NyPlayActivity extends CastActivity {

    private static String TAG = "NyPlayActivity";
    private int vIndex = -1;
    private ArrayList<String> mediaQueue = new ArrayList<String>();

    public MenuItem menuConnectRoute, menuPlayRoute, menuStopRoute;
    private MenuItem menuDownload;

    public boolean toResume = true;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nyplay);
        View contentView = findViewById(android.R.id.content);
        contentView.setFitsSystemWindows(true);
        initYoutube();
    }

    @Override
    public void playWithPlayer(Uri mediaUri){
        if (mediaLink == null) {
            mediaLink = NyFileUtil.getPath(this, mediaUri);
            mimeType = getGoogLeMimeType(this, mediaLink);
        }
       /* //TODO ny tmp out 2024-12-10
        if ((mediaLink != null && NyFileUtil.isMuPdf(mediaLink))
                || (mimeType != null && mimeType.equals("application/pdf"))) {  //exclude png, bmp, jpg
            muPdfDisplay(this, mediaStream, mimeType);
            //not working for
            //muPdfDisplay(mediaStream);
            return;
        } else {*/
            //
            if (PreferenceHelper.getInstance().getString(KEY_PLAYER, VLC_PLAYER).equals(EXTERNAL_PLAYER)) {
                processToShare(mediaUri);
            } else {
                mimeType = getGoogLeMimeType(this, mediaUri.getPath());
                Intent intent = new Intent(this, BasePlayerActivity.class);
                intent.setData(mediaUri);
                startActivity(intent);
            }
      //  }
    }

    protected void processToShare(Uri mediaUri) {
        new Thread() {
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    // if (hybridFile.getPath().equals(redirectedUrl))
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setDataAndType(mediaUri, mimeType);
                    PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
                    if (resInfos != null && resInfos.size() > 0)
                        startActivity(intent);
                    //  Log.e(TAG, "to External Uri =" + hybridFile.getUri().toString());
                } catch (Exception e) {
                    //  routeToExternal(hybridFile);
                    Log.e(TAG, "to External exception =" + e.toString());
                }
            }
        }.start();
    }


    public final void loadMedia() {
        if (mediaLink != null && mediaLink.contains("http")) mediaLink = null;
        getMediaLinkDialog(NyPlayActivity.this, mediaLink, new GeneralCallback() {
                    @Override
                    public void SingleString(String path) {
                        String link = null;
                        if (path.contains("zip") && !path.contains("http"))
                            link = getServerLink(new NyHybrid(NyFileUtil.getLastSegmentFromString(path), path), getMessageHandler(NyPlayActivity.this.findViewById(R.id.content)));
                        else {
                            link = path;
                        }
                        setMediaLink(link);
                        mimeType = getGoogLeMimeType(NyPlayActivity.this, link);
                        if (isMedia(mediaLink) /*|| isDocument(mediaLink)*/) {
                            buildNplayMedia(mediaLink);
                        }
                    }

                    @Override
                    public void SingleBoolean(boolean yesOrNo) {
                    }

                    @Override
                    public void MultiStrings(ArrayList<String> paths) {
                        //no option
                    }
                },
                new GeneralCallback() {
                    @Override
                    public void SingleString(String path) {
                        String link = null;
                        if (path.contains("zip") && !path.contains("http"))
                            link = getServerLink(new NyHybrid(NyFileUtil.getLastSegmentFromString(path), path), getMessageHandler(NyPlayActivity.this.findViewById(R.id.content)));               else {
                            link = path;
                        }
                        setMediaLink(link);
                        mimeType = getGoogLeMimeType(NyPlayActivity.this, link);
                        if (isMedia(mediaLink) /*|| isDocument(mediaLink)*/) {
                            buildNplayMedia(mediaLink);
                        }
                    }

                    @Override
                    public void SingleBoolean(boolean yesOrNo) {

                    }

                    @Override
                    public void MultiStrings(ArrayList<String> paths) {
                        vIndex = -1;
                        mediaQueue.clear();
                        mediaQueue.addAll(paths);
                        queueHandle();
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(@Nullable Menu menu) {

        getMenuInflater().inflate(R.menu.activity_play, menu);
        MenuItem chromecast;
        if (menu != null) {
            chromecast = CastButtonFactory.setUpMediaRouteButton(this.getApplicationContext(), menu, R.id.menu_connect_route);
        } else {
            chromecast = null;
        }
        menuConnectRoute = chromecast;
        menuPlayRoute = menu.findItem(R.id.menu_play_route);
        menuStopRoute = menu.findItem(R.id.menu_stop_route);
        menuDownload = menu.findItem(R.id.menu_download);
        menuDownload.setVisible(stringTitle != null);
        menuPlayRoute.setVisible(mediaLink != null);
        menuStopRoute.setVisible(mCastSession != null && mediaLink != null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.open) {
            loadMedia();
        } else if (itemId == R.id.menu_stop_route) {
            stopMedia();
        } else if (itemId == R.id.menu_play_route) {
            if (mCastSession == null) {
                loadMedia();
            } else {
                if (selectedUri != null) playMedia(selectedUri);
                else buildNplayMedia(mediaLink);
            }
        } else if (itemId == R.id.menu_download) {
            proceedDownload();
        } else if (itemId == R.id.menu_player) {
            playerToggle();
        }
        return true;
    }


    private void playerToggle() {
        String[] items = {GOOGLE_PLAYER, VLC_PLAYER, FAN_PLAYER, ANDROID_PLAYER, EXTERNAL_PLAYER};
        showSingleChoiceDialog(this, items, new GeneralCallback() {
            @Override
            public void SingleString(String player) {
                if (player != null) {
                    PreferenceHelper.getInstance().setString(KEY_PLAYER, player);
                }
            }

            @Override
            public void SingleBoolean(boolean yesOrNo) {

            }

            @Override
            public void MultiStrings(ArrayList<String> paths) {

            }
        });
    }


    public void proceedDownload() {
        Toast.makeText(NyPlayActivity.this, "Downloading " + getStringTitle(), Toast.LENGTH_SHORT).show();
        downloadFromUrl(NyPlayActivity.this, mediaLink, getStringTitle(), getStringTitle() + ".mp4");

      /*  checkForExternalPermission(new OnPermissionGranted() {
            @Override
            public void onPermissionGranted() {*
                Toast.makeText(NyPlayActivity.this, "Downloading "+getStringTitle(), Toast.LENGTH_SHORT).show();
                // fetchDownload(YoutubePlaybackActivity.this, link, NyFileUtil.timedFileName());
                downloadFromUrl(NyPlayActivity.this, mediaLink, getStringTitle(), NyFileUtil.timedFileName());
          }
        });*/
    }

   /*  static final int RENDER_REQUEST_CODE = 111;

    private void display() {

        if (toExternal && !isException && isGooglePhotosInstalled(this)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(Uri.parse(mediaStream), mimeType);
            intent.setPackage(GOOGLE_PHOTOS_PACKAGE_NAME);
            startActivity(intent);
            return;
        } else if (toExternal && isVLCInstalled(this)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(Uri.parse(mediaStream), mimeType);
            intent.setPackage(VLC_PACKAGE_NAME);
            startActivity(intent);
            return;
        }

        if (mimeType.contains("video") || mimeType.contains("image") || mimeType.contains("audio")) {
            //   DisplayFragment.show(getSupportFragmentManager(), R.id.display_container, new NyHybrid(mediaLink));

            Intent intent = new Intent(this, BasePlayerActivity.class);
            intent.setData(Uri.parse(mediaLink));
            startActivity(intent);
        } else
            muPdfDisplay(this, mediaStream, mimeType);
    }

   private void initSmb() {
        if (findViewById(R.id.fragment_container) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SMBContentFragment(), "SMBContentFragment");
            transaction.commitAllowingStateLoss();
        }
    }*/

    private void initYoutube() {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, new YoutubeFragment(), "YoutubeFragment");
        transaction.commitAllowingStateLoss();
    }


    @Override
    public void onResume() {
        super.onResume();
        SystemUtils.hideSystemUI(this);

        //no use, not able to disconnect google photos from connection to caster
        //killBackgroundProcess(this, GOOGLE_PHOTOS_PACKAGE_NAME);
        queueHandle();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GET_ALL || requestCode == REQUEST_CODE_GET_VIDEO || requestCode == REQUEST_CODE_GET_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                selectedUri = data.getData();
                playMedia(selectedUri);
               /* ArrayList<String> allMedias = new ArrayList<>();

                String filePath = NyFileUtil.getPath(this, selectedUri);
                Log.e(TAG, "data.getData()= " + selectedUri.getPath());
                Log.e(TAG, "filePath = " + filePath);

                int index = filePath.indexOf("/storage/");
                if (index > -1) {
                    filePath = filePath.substring(index);
                    setMediaLink(filePath);
                    if (isMedia(filePath) || isDocument(filePath)) {
                        Log.e(TAG, "isMedia = " + filePath);

                        playMedia(mediaLink);
                    } else if (filePath.endsWith("lnk")) {
                        // saveLink = filePath;
                        Scanner s = null;
                        try {
                            s = new Scanner(new File(filePath));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        allMedias.clear();
                        while (s.hasNext()) {
                            //  Log.e(TAG, "filePath =" + s.next());
                            allMedias.add(s.next());
                        }
                        s.close();
                        //  List<String> savedLinks = readListFromPath(this, filePath);
                        // Log.e(TAG, "savedLinks=" + savedLinks.toString());
                        getSavedMediaDialog(NyPlayActivity.this, allMedias, new GeneralCallback() {
                            @Override
                            public void SingleString(String path) {
                                setMediaLink(path);
                                playMedia(mediaLink);
                            }

                            @Override
                            public void SingleBoolean(boolean yesOrNo) {

                            }

                            @Override
                            public void MultiStrings(ArrayList<String> paths) {
                                vIndex = -1;
                                mediaQueue.clear();
                                mediaQueue.addAll(paths);
                                queueHandle();
                            }
                        });
                    } else {
                        Log.e(TAG, "isMedia else " + mediaLink);
                        playMedia(mediaLink);
                    }
                } else {// else is not local file
                    playMedia(selectedUri);
                }*/
            } else {
                // Handle this case as needed
                Toast.makeText(this, "Selection canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void queueHandle() {
        if (vIndex < mediaQueue.size() - 1) {
            vIndex++;
            buildNplayMedia(mediaQueue.get(vIndex));
        } else if (toResume) loadMedia();
    }


    //-----------------handle onBackPressed-----------

    private boolean toExit = false;

    @Override
    public void onBackPressed() {

        toExit = !toExit;

        FragmentManager fragmentManager = getSupportFragmentManager();

     /*   Fragment displayFragment = fragmentManager.findFragmentById(R.id.display_container);
        // Or, if you added the fragment with a tag
        if (displayFragment != null && displayFragment.isAdded()) {
            ((NyBaseFragment) displayFragment).onBackPressed();
            SystemUtils.hideSystemUI(this);
            return;
        }*/


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
    }

}