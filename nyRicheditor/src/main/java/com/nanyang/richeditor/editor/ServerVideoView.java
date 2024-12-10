package com.nanyang.richeditor.editor;

import android.content.Context;
import android.util.AttributeSet;

import com.nytaiji.nybase.crypt.NyCipherFactory;
import com.nytaiji.nybase.libmediaStreamer.CipherFactory;
import com.nytaiji.nybase.libmediaStreamer.LocalSingleHttpServer;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.IOException;


public class ServerVideoView extends NyRichEditor /*implements OnlineLinkUtil.onlineCallback*/ {
    private String absolutePath;
    private String serverPath;
    private LocalSingleHttpServer mServer = null;

    public ServerVideoView(Context context) {
        super(context);
    }

    public ServerVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void stopServer() {
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
    }

    public void resumeServer() {
        if (mServer != null) {
            mServer.start();
        }
    }

    public String getTitle(String url) {
        String title = NyFileUtil.getLastSegmentFromString(url);
        if (title.contains("=")) title = title.substring(title.lastIndexOf("=") + 1);
        return title;
    }

    public void loadVideo() {
        String html = "<p><hr><div style=\"text-align:center;\"><b>"
                + getTitle(absolutePath)
                + "</p ><video src=\"" + serverPath
                + "\" width=\"640\" controls=\"controls\"></video></div><div <br=\"\"></div><p></p >";
        setHtml(html);
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
        serverPath = absolutePath;

        if (NyFileUtil.isOnline(absolutePath) && !absolutePath.contains("http://127.0.0.1:")) {
           /* OnlineLinkUtil onlineLinkUtil = new OnlineLinkUtil();
            onlineLinkUtil.init(getContext(), "server");
            NyVideo nyVideo = new NyVideo();
            nyVideo.setPath(absolutePath);
            onlineLinkUtil.onlinePlayEnquiry(nyVideo, EXTRACT_ONLY, (OnlineLinkUtil.onlineCallback) this);
            return;*/
        } else if (absolutePath.contains("_NY")) {
            try {
                mServer = new LocalSingleHttpServer(getContext().getApplicationContext());
                if (absolutePath.contains("_NY")) {  //no need to set a decryption cipher for zip
                    mServer.setCipherFactory((CipherFactory) new NyCipherFactory(absolutePath));
                   // if (Integer.parseInt(getPasswordFromFileName(absolutePath)) > 5)
                     //   mServer.setOffset(ENCRYPT_SKIP);
                }
                mServer.start();
                serverPath = mServer.getURL(absolutePath);
                //  Toast.makeText(EditorActivity.this, "html = "+html, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                // Log.e("EditorContainer", "IOException e:" + e.toString());
            }
        }
        loadVideo();
    }

   /* @Override
    public void onlineCallback(Map<String, Object> params) {
        serverPath = params.get("filePath").toString();
        try {
            serverPath = URLDecoder.decode(serverPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        loadVideo();
    }*/

}
