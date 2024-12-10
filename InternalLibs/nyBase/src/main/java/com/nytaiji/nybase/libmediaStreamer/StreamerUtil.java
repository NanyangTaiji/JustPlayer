package com.nytaiji.nybase.libmediaStreamer;

import static com.nytaiji.nybase.network.NetworkServersDialog.needsServer;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;
import static com.nytaiji.nybase.utils.SystemUtils.copyToClipboard;

import android.content.Context;
import android.widget.Toast;

import com.nytaiji.nybase.crypt.NyCipherFactory;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.AppContextProvider;

/*https://libeasy.alwaysdata.net/libmedia/network/#server*/
public class StreamerUtil {
    private static AbsHttpServer mServer = null;

    public static String getStremerPath(final Context context, final NyHybrid hybridFile, boolean isInternal) {
        String url = hybridFile.getPath();

        String reroutedUrl;
        if (!isInternal
                || needsServer(hybridFile)) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            try {
                mServer = isInternal ? new LocalSingleHttpServer(AppContextProvider.getAppContext())
                        : new WifiSingleHttpServer(AppContextProvider.getAppContext());
                reroutedUrl = mServer.getURL(hybridFile);
                mServer.start();
                if (url.contains("_NY")) {
                    mServer.setCipherFactory(new NyCipherFactory(url));
                    //  if (Integer.parseInt(getPasswordFromFileName(url)) > 5)
                    //  mServer.setOffset(ENCRYPT_SKIP);
                    // the above move into Streamer
                }
            } catch (Exception e) {  // exception management is not implemented in this demo code
                reroutedUrl = null;
                Toast.makeText(context, "Server fails = " + e.toString(), Toast.LENGTH_LONG).show();
            }
        } else reroutedUrl = "file://" + url;

        if (reroutedUrl != null) copyToClipboard(context, reroutedUrl);
        return reroutedUrl;
    }

    public static void stopStreamerServer(){
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
    }


}
