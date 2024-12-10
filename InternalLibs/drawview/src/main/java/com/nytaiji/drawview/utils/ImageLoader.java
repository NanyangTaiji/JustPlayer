package com.nytaiji.drawview.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.nytaiji.drawview.enums.ImageType;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class ImageLoader {
    /**
     * Load image into ImageView using Picasso library
     *
     * @param context               Application context
     * @param imageView             ImageView that contain the image
     * @param imageType             {@link com.nytaiji.drawview.enums.ImageType} type
     * @param imageObj              image object of type {@link com.nytaiji.drawview.enums.ImageType}
     * @param transformationList    list of transformations of type {@link com.squareup.picasso.Transformation}
     */
    public static void LoadImage(Context context, ImageView imageView, ImageType imageType,
                                 Object imageObj, List<Transformation> transformationList) {
        RequestCreator requestCreator = null;
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });

        switch (imageType) {
            case RESOURCE:
                if (imageObj instanceof Integer) {
                    requestCreator = builder.build()
                            .load((int) imageObj);
                } else if (imageObj instanceof String){
                    int resourceId = context.getResources()
                            .getIdentifier(String.valueOf(imageObj), "drawable",
                                    context.getPackageName());
                    requestCreator = builder.build().load(resourceId);
                }
                break;
            case URL:
                requestCreator = builder.build()
                        .load(String.valueOf(imageObj));
                break;
            case FILE:
                requestCreator = builder.build()
                        .load((File) imageObj);
                break;
            case ASSET:
                requestCreator = builder.build()
                        .load("file:///android_asset/" + imageObj);
                break;
        }

        if (requestCreator != null) {
            requestCreator
                    .fit().centerCrop();

            if (transformationList != null) {
                requestCreator.transform(transformationList);
            }

            requestCreator.into(imageView);
        }
    }

    /**
     * Load image into ImageView using Picasso library including scale type
     *
     * @param context               Application context
     * @param imageView             ImageView that contain the image
     * @param imageType             {@link com.nytaiji.drawview.enums.ImageType} type
     * @param scaleType             image scale of type {@link android.widget.ImageView.ScaleType}
     * @param imageObj              image object of type {@link com.nytaiji.drawview.enums.ImageType}
     * @param transformationList    list of transformations of type {@link com.squareup.picasso.Transformation}
     */
    public static void LoadImage(Context context, ImageView imageView, ImageType imageType,
                                 ImageView.ScaleType scaleType, Object imageObj,
                                 List<Transformation> transformationList) {
        RequestCreator requestCreator = null;
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });

        switch (imageType) {
            case RESOURCE:
                if (imageObj instanceof Integer) {
                    requestCreator = builder.build()
                            .load((int) imageObj);
                } else if (imageObj instanceof String){
                    int resourceId = context.getResources()
                            .getIdentifier(String.valueOf(imageObj), "drawable",
                                    context.getPackageName());
                    requestCreator = builder.build().load(resourceId);
                }
                break;
            case URL:
                requestCreator = builder.build()
                        .load(String.valueOf(imageObj));
                break;
            case FILE:
                requestCreator = builder.build()
                        .load((File) imageObj);
                break;
            case ASSET:
                requestCreator = builder.build()
                        .load("file:///android_asset/" + imageObj);
                break;
        }

        if (requestCreator != null) {
            requestCreator
                    .fit();

            if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                requestCreator.centerCrop();
            } else if (scaleType == ImageView.ScaleType.CENTER_INSIDE) {
                requestCreator.centerInside();
            }

            if (transformationList != null) {
                requestCreator.transform(transformationList);
            }

            requestCreator.into(imageView);
        }
    }

    /**
     * Load image into ImageView using Picasso library including scale type and callback
     *
     * @param context               Application context
     * @param imageView             ImageView that contain the image
     * @param imageType             {@link com.nytaiji.drawview.enums.ImageType} type
     * @param scaleType             image scale of type {@link android.widget.ImageView.ScaleType}
     * @param imageObj              image object of type {@link com.nytaiji.drawview.enums.ImageType}
     * @param transformationList    list of transformations of type {@link com.squareup.picasso.Transformation}
     * @param callback              Picasso callback of type {@link com.squareup.picasso.Callback}
     */
    public static void LoadImage(Context context, ImageView imageView, ImageType imageType,
                                 ImageView.ScaleType scaleType, Object imageObj,
                                 List<Transformation> transformationList, Callback callback) {
        RequestCreator requestCreator = null;
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });

        switch (imageType) {
            case RESOURCE:
                if (imageObj instanceof Integer) {
                    requestCreator = builder.build()
                            .load((int) imageObj);
                } else if (imageObj instanceof String){
                    int resourceId = context.getResources()
                            .getIdentifier(String.valueOf(imageObj), "drawable",
                                    context.getPackageName());
                    requestCreator = builder.build().load(resourceId);
                }
                break;
            case URL:
                requestCreator = builder.build()
                        .load(String.valueOf(imageObj));
                break;
            case FILE:
                requestCreator = builder.build()
                        .load((File) imageObj);
                break;
            case ASSET:
                requestCreator = builder.build()
                        .load("file:///android_asset/" + imageObj);
                break;
        }

        if (requestCreator != null) {
            requestCreator
                    .fit();

            if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                requestCreator.centerCrop();
            } else if (scaleType == ImageView.ScaleType.CENTER_INSIDE) {
                requestCreator.centerInside();
            }

            if (transformationList != null) {
                requestCreator.transform(transformationList);
            }

            if (callback == null) {
                requestCreator.into(imageView);
            } else {
                requestCreator.into(imageView, callback);
            }
        }
    }

    /**
     * Load image into ImageView using Picasso library including callback
     *
     * @param context               Application context
     * @param imageView             ImageView that contain the image
     * @param imageType             {@link com.nytaiji.drawview.enums.ImageType} type
     * @param imageObj              image object of type {@link com.nytaiji.drawview.enums.ImageType}
     * @param transformationList    list of transformations of type {@link com.squareup.picasso.Transformation}
     * @param callback              Picasso callback of type {@link com.squareup.picasso.Callback}
     */
    public static void LoadImage(Context context, ImageView imageView, ImageType imageType,
                                 Object imageObj, List<Transformation> transformationList,
                                 Callback callback) {
        RequestCreator requestCreator = null;
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });

        switch (imageType) {
            case RESOURCE:
                if (imageObj instanceof Integer) {
                    requestCreator = builder.build()
                            .load((int) imageObj);
                } else if (imageObj instanceof String){
                    int resourceId = context.getResources()
                            .getIdentifier(String.valueOf(imageObj), "drawable",
                                    context.getPackageName());
                    requestCreator = builder.build().load(resourceId);
                }
                break;
            case URL:
                requestCreator = builder.build()
                        .load(String.valueOf(imageObj));
                break;
            case FILE:
                requestCreator = builder.build()
                        .load((File) imageObj);
                break;
            case ASSET:
                requestCreator = builder.build()
                        .load("file:///android_asset/" + imageObj);
                break;
        }

        if (requestCreator != null) {
            requestCreator
                    .fit().centerCrop();

            if (transformationList != null) {
                requestCreator.transform(transformationList);
            }

            if (callback == null) {
                requestCreator.into(imageView);
            } else {
                requestCreator.into(imageView, callback);
            }
        }
    }

    /**
     * Load image into ImageView using Picasso library
     *
     * @param context               Application context
     * @param target                {@link Target} to load the image
     * @param imageType             {@link com.nytaiji.drawview.enums.ImageType} type
     * @param imageObj              image object of type {@link com.nytaiji.drawview.enums.ImageType}
     * @param transformationList    list of transformations of type {@link com.squareup.picasso.Transformation}
     */
    public static void LoadImage(Context context, Target target, ImageType imageType,
                                 Object imageObj, List<Transformation> transformationList) {
        RequestCreator requestCreator = null;
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });

        switch (imageType) {
            case RESOURCE:
                if (imageObj instanceof Integer) {
                    requestCreator = builder.build()
                            .load((int) imageObj);
                } else if (imageObj instanceof String){
                    int resourceId = context.getResources()
                            .getIdentifier(String.valueOf(imageObj), "drawable",
                                    context.getPackageName());
                    requestCreator = builder.build().load(resourceId);
                }
                break;
            case URL:
                requestCreator = builder.build()
                        .load(String.valueOf(imageObj));
                break;
            case FILE:
                requestCreator = builder.build()
                        .load((File) imageObj);
                break;
            case ASSET:
                requestCreator = builder.build()
                        .load("file:///android_asset/" + imageObj);
                break;
        }

        if (requestCreator != null) {
            if (transformationList != null) {
                requestCreator.transform(transformationList);
            }

            requestCreator.into(target);
        }
    }

    /**
     * Load image into ImageView using Picasso library
     *
     * @param context               Application context
     * @param target                {@link Target} to load the image
     * @param imageType             {@link com.nytaiji.drawview.enums.ImageType} type
     * @param imageObj              image object of type {@link com.nytaiji.drawview.enums.ImageType}
     * @param transformationList    list of transformations of type {@link com.squareup.picasso.Transformation}
     * @param callback              Picasso callback of type {@link com.squareup.picasso.Callback}
     */
    public static void LoadImage(Context context, Target target, ImageType imageType,
                                 Object imageObj, List<Transformation> transformationList,
                                 Callback callback) {
        RequestCreator requestCreator = null;
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });

        switch (imageType) {
            case RESOURCE:
                if (imageObj instanceof Integer) {
                    requestCreator = builder.build()
                            .load((int) imageObj);
                } else if (imageObj instanceof String){
                    int resourceId = context.getResources()
                            .getIdentifier(String.valueOf(imageObj), "drawable",
                                    context.getPackageName());
                    requestCreator = builder.build().load(resourceId);
                }
                break;
            case URL:
                requestCreator = builder.build()
                        .load(String.valueOf(imageObj));
                break;
            case FILE:
                requestCreator = builder.build()
                        .load((File) imageObj);
                break;
            case ASSET:
                requestCreator = builder.build()
                        .load("file:///android_asset/" + imageObj);
                break;
        }

        if (requestCreator != null) {
            if (transformationList != null) {
                requestCreator.transform(transformationList);
            }

            if (callback != null){
                requestCreator.fetch(callback);
            }

            requestCreator.into(target);
        }
    }

    public static final int REQUEST_CODE_CHOOSE_IMAGE = 10090;
    public static final int REQUEST_CODE_CHOOSE_VIDEO = 10091;
    public static final int REQUEST_CODE_CHOOSE_HTML = 100900;
    public static final int REQUEST_CODE_GET_DRAWING = 100892;
    public static void openDirChooseFile(Activity context, String mimeType, int requestcode) {
        if (requestcode==REQUEST_CODE_CHOOSE_HTML){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(mimeType);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            context.startActivityForResult(intent, requestcode);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI); // todo: this thing might need some work :/, eg open from google drive, stuff like that
            intent.setTypeAndNormalize(mimeType);
            context.startActivityForResult(Intent.createChooser(intent, "Select Media"), requestcode);}
    }
    /**
     * Return the last item stored in the clipboard.
     *
     * @return	{@link CharSequence}
     */
    public static CharSequence getClipboardItem(Context context) {
        CharSequence clipboardText = null;
        ClipboardManager clipboardManager = ContextCompat.getSystemService(context, ClipboardManager.class);

        // if the clipboard contain data ...
        if (clipboardManager != null  &&  clipboardManager.hasPrimaryClip()) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

            // gets the clipboard as text.
            clipboardText = item.coerceToText(context);
        }

        return clipboardText;
    }

    /* @param context 上下文
     * @param uri     待解析的 Uri
     * @return 真实路径
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String url = null;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    url = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                url = getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                url = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (uri.getScheme() != null) {
            // MediaStore (and general)
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Return the remote address
                if (isGooglePhotosUri(uri)) url = uri.getLastPathSegment();
                else url = getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                url = uri.getPath();
            }
        } else url = uri.toString();

        if (url == null)  url=uri.toString();

        if (url.contains("%")) {
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToNext()) {
                String result = cursor.getString(0);
                //TODO most important
                return TextUtils.isEmpty(result) ? result : getRealPathFromURI(context, uri);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        //TODO
        return uri.toString();
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

 /*   public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        //TODO
        return uri.toString();
    }*/

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
