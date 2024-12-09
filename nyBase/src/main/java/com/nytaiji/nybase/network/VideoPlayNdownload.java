package com.nytaiji.nybase.network;

import static com.nytaiji.nybase.utils.NyFileUtil.getActivity;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.nytaiji.nybase.crypt.EncryptUtil;
import com.nytaiji.nybase.crypt.EncryptedDownloadTask;
import com.nytaiji.nybase.model.Constants;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

//import com.nytaiji.gen.core.GenVideoPlayActivity;

//import com.nytaiji.gen.core.FilterEditActivity;
//import com.nytaiji.application.core.VlcPlayActivity;
//import com.nytaiji.gen.core.VlcPlayActivity;

public class VideoPlayNdownload {
    private static final String TAG = "VideoPlayNdownload";
    private static Class PrintActivity = null;
    private Context context;
    private String userid;
    private int memberLevel = 1;


    public void init(Context context, String userid) {
        // Log.e(TAG, "init----" + userid);
        this.context = context;
        if (userid == null || userid.contains("guest") || userid.contains("访客")) {
            this.userid = "guest";
            memberLevel = 1;
        } else {
            this.userid = userid;
            Log.e(TAG, "userid != guest" + userid);
            memberLevel = Character.getNumericValue(userid.charAt(2));
        }
    }


    //--------------------------------------
    public void setPrintActivity(Class printActivity) {
        PrintActivity = printActivity;
    }

    //--------------------------------------------------------------

    public void directListPlay(List<NyVideo> videoModelList, int position) {
        Log.e(TAG, "directListPlay:-----------" + videoModelList.get(position).getPath());
        if (videoModelList != null && videoModelList.size() > 0) {
            Intent intent;
            if (PrintActivity != null) {
                intent = new Intent(getActivity(context), PrintActivity);
            } else {
                intent = new Intent(Intent.ACTION_MAIN);
                //the following line needed to " startActivity() from outside of an Activity"
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName("com.nytaiji.player", "com.nytaiji.player.core.CastPlayActivity"));
            }
            intent.putExtra(Constants.KEY_ID, userid);
            intent.putExtra(Constants.KEY_MEMBER, memberLevel);
            //--------------------
            intent.putParcelableArrayListExtra(Constants.VIDEO_LIST, (ArrayList<? extends Parcelable>) videoModelList);
            intent.putExtra(Constants.VIDEO_INDEX, position);
            // Add extra data to the intent
            getActivity(context).startActivity(intent);
            //  getActivity(context).finish();
        } else {
            Toast.makeText(context, "Invalid Link", Toast.LENGTH_LONG).show();
        }
    }


    public void directListPlay(List<NyVideo> videoModelList) {
        directListPlay(videoModelList, 0);
    }

    public void DirectPlayPath(String url) {
        DirectPlayVideo(Url2Nyvideo(url));
    }

    public void DirectPlayVideo(NyVideo nyVideo) {
        List<NyVideo> videoModelList = new ArrayList<>();
        videoModelList.add(nyVideo);
        //  Log.e(TAG, "DirectPlayVideo:-----------" + nyVideo.getPath());
        //  JsonUtility.AppendVideoToList(nyVideo, context.getResources().getString(R.string.history));
        directListPlay(videoModelList);
    }

    public void DirectPlayUri(Context context, Uri uri) {
        Intent intent;
        if (PrintActivity != null) {
            intent = new Intent(getActivity(context), PrintActivity);
        } else {
            intent = new Intent(Intent.ACTION_MAIN);
            //the following line needed to " startActivity() from outside of an Activity"
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("com.nytaiji.player", "com.nytaiji.player.core.CastPlayActivity"));
        }
        intent.putExtra(Constants.KEY_ID, userid);
        intent.putExtra(Constants.KEY_MEMBER, memberLevel);
        // String mUrl=mFileUtil.getPath(context, uri);
        intent.setDataAndType(uri, "video/*");
        //  Log.e(TAG, "DirectPlayUri:----------" + uri.toString());
        getActivity(context).startActivity(intent);
        //  getActivity(context).finish();
        //  JsonUtility.AppendVideoToList(Url2Nyvideo(uri.getPath()), context.getResources().getString(R.string.history));

    }

    private NyVideo Url2Nyvideo(String url) {
        NyVideo nyVideo = new NyVideo();
        nyVideo.setPath(url);
        String fileName = NyFileUtil.getLastSegmentFromString(url);
        String passWord = EncryptUtil.getPasswordFromFileName(fileName);
        nyVideo.setName(fileName);
        nyVideo.setPassWord(passWord);
        return nyVideo;
    }

    //------------------------the following two do not need initiate---------------
    public static void backgroundDecryptedDownload(Context context, String onlineUrl, String downloadPath) {
        backgroundDecryptedDownload(context, onlineUrl, new File(downloadPath));
    }

    public static void backgroundDecryptedDownload(Context context, String onlineUrl, File downloadFile) {
        int encryptLevel = EncryptUtil.encryptLevelFromFileName(onlineUrl);
        try {
            Cipher mCipher = null;
            if (encryptLevel > 0) mCipher = EncryptUtil.LevelCipherOnly(encryptLevel);
            //    Toast.makeText(context, R.string.encryptedDownload, Toast.LENGTH_LONG).show();
            EncryptedDownloadTask encryptedDownloadTask = new EncryptedDownloadTask(context, onlineUrl, downloadFile, mCipher);
            encryptedDownloadTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void backgroundEncryptedDownload(Context context, String onlineUrl, File downloadFile, int encryptLevel, String cookie) {
        try {
            Cipher mCipher = null;
            if (encryptLevel > 0) mCipher = EncryptUtil.LevelCipherOnly(encryptLevel);
            //    Toast.makeText(context, R.string.encryptedDownload, Toast.LENGTH_LONG).show();
            EncryptedDownloadTask encryptedDownloadTask = new EncryptedDownloadTask(context, onlineUrl, downloadFile, mCipher);
            encryptedDownloadTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backgroundDownload(Context context, String onlineUrl, File downloadFile) {
        if (NyFileUtil.isOnline(onlineUrl))
            backgroundEncryptedDownload(context, onlineUrl, downloadFile, -1, null);
        else Toast.makeText(context, "Not-downloadable!", Toast.LENGTH_SHORT);
    }

    public static void backgroundEncryptedDownload(Context context, String onlineUrl, int encryptLevel) {
        File destination = new File(NyFileUtil.getVideoDir(), NyFileUtil.timedFileName());
        backgroundEncryptedDownload(context, onlineUrl, destination, encryptLevel, null);
    }

    public static void backgroundDownload(Context context, String onlineUrl) {
        File destination = new File(NyFileUtil.getDownloadedDir(context), NyFileUtil.timedFileName());
        backgroundDownload(context, onlineUrl, destination);
    }

    public static void backgroundDownload(Context context, String onlineUrl, String title) {
        if (NyFileUtil.isOnline(onlineUrl))
            backgroundEncryptedDownload(context, onlineUrl, NyFileUtil.setOutputFile(title, -1), -1, null);
        else Toast.makeText(context, "Not-downloadable!", Toast.LENGTH_SHORT);
    }


    public static long downloadFromUrl(Context context, String youtubeDlUrl, String downloadTitle, String fileName) {
        return downloadFromUrl(context, youtubeDlUrl, downloadTitle, fileName, false);
    }

    public static long downloadFromUrl(Context context, String youtubeDlUrl, String downloadTitle, String fileName, boolean hide) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);
        if (hide) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
            request.setVisibleInDownloadsUi(false);
        } else
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }



}
