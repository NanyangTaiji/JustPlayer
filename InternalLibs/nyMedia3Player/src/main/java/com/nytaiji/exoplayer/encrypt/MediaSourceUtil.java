package com.nytaiji.exoplayer.encrypt;

import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.nytaiji.nybase.crypt.EncryptUtil;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class MediaSourceUtil {
    @OptIn(markerClass = UnstableApi.class)

    public static MediaSource getMediaSource(Context context, Uri mediaUri) {
        MediaItem mediaItem=MediaItem.fromUri(mediaUri);
        ExtendedExtractorsFactory extractorsFactory = new ExtendedExtractorsFactory();
        String path = NyFileUtil.getPath(context, mediaUri);

        DataSource.Factory encryptedFactory = null;
        if (path.contains("_NY")) encryptedFactory = getEncryptedMediaSource(context, path);

        if (encryptedFactory != null)
            return new DefaultMediaSourceFactory(encryptedFactory, extractorsFactory).createMediaSource(mediaItem);
        else if (mediaUri.getScheme().toLowerCase().startsWith("http")) {
            HashMap<String, String> headers = new HashMap<>();
            String userInfo = mediaUri.getUserInfo();
            if (userInfo != null && userInfo.length() > 0 && userInfo.contains(":")) {
                headers.put("Authorization", "Basic " + Base64.encodeToString(userInfo.getBytes(), Base64.NO_WRAP));
                DefaultHttpDataSource.Factory defaultHttpDataSourceFactory = new DefaultHttpDataSource.Factory();
                defaultHttpDataSourceFactory.setDefaultRequestProperties(headers);
                return new DefaultMediaSourceFactory(defaultHttpDataSourceFactory, extractorsFactory).createMediaSource(mediaItem);
            }
        }
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "YourAppName"));
            ProgressiveMediaSource.Factory progressiveMediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory);
            return progressiveMediaSourceFactory.createMediaSource(mediaItem);
    }

    @OptIn(markerClass = UnstableApi.class)
    public static androidx.media3.datasource.DataSource.Factory getEncryptedMediaSource(Context context, String path) {
        String password = EncryptUtil.getPasswordFromFileName(path);
        EncryptUtil.CTRnoPadding mCES = EncryptUtil.LevelCipherPackage(password);
        //  LevelCipherPackage(encryptLevel);
        DataSource.Factory dataSourceFactory;
        String useragent = Util.getUserAgent(context, "com.nytaiji.app");

        if (isOnline(path)) {
            dataSourceFactory = new OnlineEncryptedDataSourceFactory(mCES.cipher, mCES.secretKeySpec, mCES.ivParameterSpec, (Call.Factory) new OkHttpClient(), useragent, null);
        } else {
            dataSourceFactory = new EncryptedDataSourceFactory(context, mCES.cipher, mCES.secretKeySpec, mCES.ivParameterSpec, null);
        }
        return dataSourceFactory;
    }

}
