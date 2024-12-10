package com.nytaiji.exoplayer.encrypt;


import static com.nytaiji.nybase.utils.NyFileUtil.getPath;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.CacheSpan;
import androidx.media3.datasource.cache.ContentMetadata;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.dash.DefaultDashChunkSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.smoothstreaming.DefaultSsChunkSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.LoopingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;


import com.nytaiji.nybase.crypt.EncryptUtil;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;

import okhttp3.Call;
import okhttp3.OkHttpClient;


public class ExoSourceManager {

    private static final String TAG = "ExoSourceManager";

    private static final long DEFAULT_MAX_SIZE = 512 * 1024 * 1024;

    public static final int TYPE_RTMP = 4;

    public static final int TYPE_ENCRYPT = 99;

    protected static Cache mCache;

    protected Context mContext;

    protected Map<String, String> mMapHeadData;

    protected String mDataSource;

    protected int encryptLevel;

    protected String passWord;

    private boolean isCached = false;

    private static ExoSourceManager exoSourceManager = null;

    public static ExoSourceManager getInstance(Context context) {
        if (exoSourceManager == null)
            exoSourceManager = new ExoSourceManager(context);
        return exoSourceManager;
    }

    //add @Nullable to the next statement
    private ExoSourceManager(Context context) {
        mContext = context;
    }

    public MediaSource buildMediaSource(Uri contentUri, boolean isLooping) {
        mMapHeadData = new HashMap<>();
        String userInfo = contentUri.getUserInfo();
        if (userInfo != null && userInfo.length() > 0 && userInfo.contains(":")) {
            mMapHeadData.put("Authorization", "Basic " + Base64.encodeToString(userInfo.getBytes(), Base64.NO_WRAP));
        }
       // boolean cacheEnable = isOnline(getPath(mContext, contentUri));
        //preview must not be set for local file
        return buildMediaSource(contentUri, false, false, isLooping, new File(NyFileUtil.getCacheFolder()));
    }

    @OptIn(markerClass = UnstableApi.class)
    public MediaSource buildMediaSource(Uri contentUri, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
        String filePath = getPath(mContext, contentUri);
        //TODO ny
        //  if (filePath.contains("smb:")) return getSmbMediaSource(mContext, contentUri);
        // Log.e(TAG, "--------------------password--:" + passWord);

        if (passWord == null) passWord = EncryptUtil.getPasswordFromFileName(filePath);
        if (passWord != null && !filePath.contains("http://127.0") && !filePath.contains("http://192.168")) {
            Log.e(TAG, "--------------------password--:" + passWord);
            return getEncryptedMediaSource(mContext, contentUri, passWord);
        }

        @C.ContentType int type = Util.inferContentType(contentUri, null);
        MediaSource mediaSource;

        switch (type) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(getDataSourceFactoryCache(mContext, cacheEnable, preview, cacheDir)),
                        new DefaultDataSourceFactory(mContext, null,
                                getHttpDataSourceFactory(mContext, preview))).createMediaSource(MediaItem.fromUri(contentUri));
                break;
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSourceFactoryCache(mContext, cacheEnable, preview, cacheDir)),
                        new DefaultDataSourceFactory(mContext, null,
                                getHttpDataSourceFactory(mContext, preview))).createMediaSource(MediaItem.fromUri(contentUri));
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(getDataSourceFactoryCache(mContext, cacheEnable, preview, cacheDir)).createMediaSource(MediaItem.fromUri(contentUri));
                break;
            case C.TYPE_OTHER:
            default:
                mediaSource = new ProgressiveMediaSource.Factory(getDataSourceFactoryCache(mContext, cacheEnable, preview, cacheDir), new ExtendedExtractorsFactory()).createMediaSource(MediaItem.fromUri(contentUri));
                break;
        }
        if (isLooping) {
            return new LoopingMediaSource(mediaSource);
        }
        return mediaSource;
    }

  /*  public MediaSource getSmbMediaSource(Context context, Uri contentUri) {
        DataSource.Factory dataSourceFactory;
      //  String useragent = Util.getUserAgent(context, "com.nytaiji.app");

        dataSourceFactory = new SmbDataSourceFactory();
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(contentUri);
    }*/


    @OptIn(markerClass = UnstableApi.class)
    public MediaSource getEncryptedMediaSource(Context context, Uri contentUri, String password) {
        EncryptUtil.CTRnoPadding mCES = EncryptUtil.LevelCipherPackage(password);
        //  LevelCipherPackage(encryptLevel);
        DataSource.Factory dataSourceFactory;
        String useragent = Util.getUserAgent(context, context.getPackageName());

        if (isOnline(contentUri)) {
            dataSourceFactory = new OnlineEncryptedDataSourceFactory(mCES.cipher, mCES.secretKeySpec, mCES.ivParameterSpec, (Call.Factory) new OkHttpClient(), useragent, null);
        } else {
            dataSourceFactory = new EncryptedDataSourceFactory(mContext, mCES.cipher, mCES.secretKeySpec, mCES.ivParameterSpec, null);
        }
        return new ProgressiveMediaSource.Factory(dataSourceFactory, new ExtendedExtractorsFactory()).createMediaSource(MediaItem.fromUri(contentUri));
    }


    //本地缓存目录
    @OptIn(markerClass = UnstableApi.class)
    public static synchronized Cache getCacheSingleInstance(Context context, File cacheDir) {
        String dirs = NyFileUtil.getCacheFolder();
        if (cacheDir != null) {
            dirs = cacheDir.getAbsolutePath();
        }
        if (mCache == null) {
            String path = dirs + File.separator + "exo";
            boolean isLocked = SimpleCache.isCacheFolderLocked(new File(path));
            if (!isLocked) {
                mCache = new SimpleCache(new File(path), new LeastRecentlyUsedCacheEvictor(DEFAULT_MAX_SIZE));
            }
        }
        return mCache;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void releaseCache() {
        isCached = false;
        if (mCache != null) {
            mCache.release();
            mCache = null;
        }
    }

    //Cache需要release之后才能clear
    /* public static void clearCache(Context context, File cacheDir, String url) {
         try {
             Cache cache = getCacheSingleInstance(context, cacheDir);
             if (!TextUtils.isEmpty(url)) {
                 if (cache != null) {
                     remove(cache, generateKey(Uri.parse(url)));
                 }
             } else {
                 if (cache != null) {
                     for (String key : cache.getKeys()) {
                         remove(cache, key);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }*/

    public static boolean cachePreView(Context context, File cacheDir, String url) {
        return resolveCacheState(getCacheSingleInstance(context, cacheDir), url);
    }

    public boolean hadCached() {
        return isCached;
    }


    /**
     * 获取SourceFactory，是否带Cache
     */
    @OptIn(markerClass = UnstableApi.class)
    private DataSource.Factory getDataSourceFactoryCache(Context context, boolean cacheEnable, boolean preview, File cacheDir) {
        if (cacheEnable) {
            mCache = getCacheSingleInstance(context, cacheDir);
            if (mCache != null) {
                isCached = resolveCacheState(mCache, mDataSource);
                return new CacheDataSource.Factory();
            }
        }
        return getDataSourceFactory(context, preview);
    }

    /**
     * 获取SourceFactory
     */
    @OptIn(markerClass = UnstableApi.class)
    private DataSource.Factory getDataSourceFactory(Context context, boolean preview) {
        return new DefaultDataSourceFactory(context, getHttpDataSourceFactory(context, preview));
    }

    @OptIn(markerClass = UnstableApi.class)
    private DataSource.Factory getHttpDataSourceFactory(Context context, boolean preview) {
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
        if (mMapHeadData != null && mMapHeadData.size() > 0) {
            for (Map.Entry<String, String> header : mMapHeadData.entrySet()) {
                dataSourceFactory.setDefaultRequestProperties((Map<String, String>) header);
            }
        }
        return dataSourceFactory;
    }

    /**
     * 根据缓存块判断是否缓存成功
     *
     * @param cache
     */
    @OptIn(markerClass = UnstableApi.class)
    private static boolean resolveCacheState(Cache cache, String url) {
        boolean isCache = true;
        if (!TextUtils.isEmpty(url)) {
            String key = generateKey(Uri.parse(url));
            if (!TextUtils.isEmpty(key)) {
                NavigableSet<CacheSpan> cachedSpans = cache.getCachedSpans(key);
                if (cachedSpans.size() == 0) {
                    isCache = false;
                } else {
                    long contentLength = cache.getContentMetadata(key).get(ContentMetadata.KEY_CONTENT_LENGTH, C.LENGTH_UNSET);
                    long currentLength = 0;
                    for (CacheSpan cachedSpan : cachedSpans) {
                        currentLength += cache.getCachedLength(key, cachedSpan.position, cachedSpan.length);
                    }
                    isCache = currentLength >= contentLength;
                }
            } else {
                isCache = false;
            }
        }
        return isCache;
    }

    /**
     * Generates a cache key out of the given {@link Uri}.
     *
     * @param uri Uri of a content which the requested key is for.
     */
    public static String generateKey(Uri uri) {
        return uri.toString();
    }

    /*
    private static HttpProxyCacheServer cacheServer;
    public static HttpProxyCacheServer getCacheServer(Context context) {
        if (cacheServer == null) cacheServer = buildHttpCacheServer(context);
        return cacheServer;
    }

    private static HttpProxyCacheServer buildHttpCacheServer(Context context) {
        return new HttpProxyCacheServer.Builder(context)
                .cacheDirectory(new File (NyFileUtil.getCacheFolder()))
                .build();
    }*/

}

