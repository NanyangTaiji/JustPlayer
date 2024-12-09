package com.nytaiji.nybase.network;

import static com.nytaiji.nybase.httpShare.WifiShareUtil.getPreferredServerUrl;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_BOX;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_DROPBOX;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_GOOGLE_DRIVE;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_ONE_DRIVE;
import static com.nytaiji.nybase.model.NyHybridMode.FTPS_URI_PREFIX;
import static com.nytaiji.nybase.model.NyHybridMode.FTP_URI_PREFIX;
import static com.nytaiji.nybase.model.NyHybridMode.SMB_URI_PREFIX;
import static com.nytaiji.nybase.model.NyHybridMode.SSH_URI_PREFIX;
import static com.nytaiji.nybase.libmediaStreamer.StreamerUtil.getStremerPath;
import static com.nytaiji.nybase.utils.NyFileUtil.containZip;
import static com.nytaiji.nybase.utils.NyFileUtil.getLastSegmentFromString;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.nybase.R;
import com.nytaiji.nybase.httpShare.WifiShareUtil;
import com.nytaiji.nybase.libmediaStreamer.StreamerUtil;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.PreferenceHelper;

public class NetworkServersDialog extends Dialog implements View.OnClickListener {
    public static final String SERVER_STREAM_TYPE = "server_stream_type";
    public static final String SERVER_RANGE_TYPE = "server_range_type";
    private TextView dialogTitleTv;
    private RadioGroup serverRg;
    private RadioGroup rangeRg;
    private ServerCallback callback;

    public NetworkServersDialog(@NonNull Context context, ServerCallback callback) {
        super(context, R.style.DialogTheme);
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_server);
        findViewById(R.id.confirm_tv).setOnClickListener(this);
        findViewById(R.id.cancel_tv).setOnClickListener(this);

        dialogTitleTv=findViewById(R.id.server_title);
        serverRg=findViewById(R.id.server_rg);
        rangeRg=findViewById(R.id.range_rg);

        dialogTitleTv.setText(R.string.select_streamer);
        switch (getStreamer()) {
            case 0:
                serverRg.check(R.id.streamer_rb);
                break;
            case 1:
                serverRg.check(R.id.webshare_rb);
                break;
            case 2:
                serverRg.check(R.id.smb_server_rb);
                break;
        }
        if (getRange()) rangeRg.check(R.id.internal_rb);
        else rangeRg.check(R.id.external_rb);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.cancel_tv) {
            NetworkServersDialog.this.dismiss();
        } else if (id == R.id.confirm_tv) {
            setStreamer(getServer(serverRg.getCheckedRadioButtonId()));
            setRange(getRange(rangeRg.getCheckedRadioButtonId()));
            if (callback != null)
                callback.onSelected(getServer(serverRg.getCheckedRadioButtonId()), getRange(rangeRg.getCheckedRadioButtonId()));
            NetworkServersDialog.this.dismiss();
        }
    }

    private int getServer(@IdRes int radioButtonId) {
        if (radioButtonId == R.id.streamer_rb) {
            return 0;
        } else if (radioButtonId == R.id.webshare_rb) {
            return 1;
        } else if (radioButtonId == R.id.smb_server_rb) {
            return 2;
        }
        return 1;
    }

    private boolean getRange(@IdRes int radioButtonId) {
        if (radioButtonId == R.id.internal_rb) {
            return true;
        } else if (radioButtonId == R.id.external_rb) {
            return false;
        }
        return true;
    }

    public interface ServerCallback {
        void onSelected(int serverType, boolean isInternal);
    }

    /**
     * 上次使用的SMB连接工具
     */
    public static int getStreamer() {
        return PreferenceHelper.getInstance().getInt(SERVER_STREAM_TYPE, 0);
    }

    public static void setStreamer(int serverType) {
        PreferenceHelper.getInstance().setInt(SERVER_STREAM_TYPE, serverType);
    }

    public static boolean getRange() {
        return PreferenceHelper.getInstance().getBoolean(SERVER_RANGE_TYPE, false);
    }

    public static void setRange(boolean isInternal) {
        PreferenceHelper.getInstance().setBoolean(SERVER_RANGE_TYPE, isInternal);
    }

    public static String getServerLink(NyHybrid hybridFile, @Nullable Handler handler) {

       // SmbServer.SMB_FILE_NAME = NyFileUtil.getLastSegmentFromString(path);  //need for all methods
        String videoUrl = hybridFile.getPath();
        switch (getStreamer()) {
            case 0:
                videoUrl = getStremerPath(AppContextProvider.getAppContext(), hybridFile, getRange());
                break;
            case 1:
                videoUrl = getPreferredServerUrl(getRange(),true);
                WifiShareUtil.setUnique(getRange()); //true for internal
                WifiShareUtil.httpShare(AppContextProvider.getAppContext(), hybridFile, videoUrl, handler);
                break;
            case 3:

                break;

            case 4:

                break;

            case 5:

                break;

        }
        return videoUrl;
    }

    public static void stopServer(){
        switch (getStreamer()) {
            case 0:
                StreamerUtil.stopStreamerServer();
                break;
            case 1:
                WifiShareUtil.stopHttpServer();
                break;
        }
    }


    public static boolean needsServer(NyHybrid hybridFile) {
        String path = hybridFile.getPath();
        return (getLastSegmentFromString(path).contains("_NY") && !path.contains("http"))
                || containZip(path)
                || (isUnservedNetwork(path) && !path.contains("http"));
    }

    private static boolean isUnservedNetwork(String path) {
        return path.startsWith(SMB_URI_PREFIX)
                || path.startsWith(SSH_URI_PREFIX)
                || path.startsWith(FTP_URI_PREFIX)
                || path.startsWith(FTPS_URI_PREFIX)
                || path.startsWith(CLOUD_PREFIX_BOX)
                || path.startsWith(CLOUD_PREFIX_ONE_DRIVE)
                || path.startsWith(CLOUD_PREFIX_GOOGLE_DRIVE)
                || path.startsWith(CLOUD_PREFIX_DROPBOX);
    }
}
