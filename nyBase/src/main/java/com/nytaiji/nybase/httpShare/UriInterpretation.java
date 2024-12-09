package com.nytaiji.nybase.httpShare;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;


import com.nytaiji.nybase.crypt.EncryptUtil;
import com.nytaiji.nybase.crypt.NyCipherInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.UnknownHostException;

import javax.crypto.Cipher;


public class UriInterpretation {
    private static final String TAG = "UriInterpretation";
    private long size = -1;
    private String name = null;
    private String path = null;
    private String mime;
    private boolean isDirectory = false;
    private Uri uri;
    private ContentResolver contentResolver;

    public UriInterpretation(Uri uri, ContentResolver contentResolver) {
        if (uri == null) return;
        this.uri = uri;

        this.contentResolver = contentResolver;

        Cursor metadataCursor = contentResolver.query(uri, new String[]{
                        OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null,
                null, null);

        if (metadataCursor != null) {
            try {
                if (metadataCursor.moveToFirst()) {
                    path = name = metadataCursor.getString(0);
                    size = metadataCursor.getInt(1);
                }
            } finally {
                metadataCursor.close();
            }
        }

        if (name == null) {
            name = uri.getLastPathSegment();
            path = uri.toString();
        }

        getMime(uri, contentResolver);

        getFileSize(uri);

    }


    public InputStream getInputStream() throws FileNotFoundException, MalformedURLException, UnknownHostException {
        if (!path.contains("smb"))  return inputStreamProvider();
        else return null;
    }


  /*  InputStream smbStreamProvider() {
        //DOES not work
        Log.e(TAG, "smbStreamProvider()  "+path);

        try {
            SmbFile smbFile = new SmbFile(path);
            InputStream inputStream = new BufferedInputStream(new SmbFileInputStream(smbFile));
            Log.e(TAG, "smbStreamProvider()  " + name);
            if (!name.contains("_NY")) return inputStream;
            Cipher ces = EncryptUtil.LevelCipherOnly(EncryptUtil.getPasswordFromFileName(name));
            return new CipherInputStream(inputStream, ces);
        }catch (MalformedURLException ignored) {
            Log.e(TAG, "smbStreamProvider() MalformedURLException "  );
        } catch (UnknownHostException e) {
            Log.e(TAG, "smbStreamProvider() UnknownHostException");
            e.printStackTrace();
        } catch (SmbException e) {
            Log.e(TAG, "smbStreamProvider() SmbException ");
            e.printStackTrace();
        }
        return null;
    }*/

    private InputStream inputStreamProvider() throws FileNotFoundException {
        InputStream inputStream = contentResolver.openInputStream(uri);
       // Log.e(TAG, "inputStreamProvider()  "+name);
        if (!name.contains("_NY")) return inputStream;

      /*  File file=new File(uri.getPath());
        EncryptUtil.EncryptFileHead(contentResolver,1024*1024,file,EncryptUtil.getPasswordFromFileName(name));
        inputStream = contentResolver.openInputStream(Uri.fromFile(file));*/

        Cipher ces = EncryptUtil.LevelCipherOnly(EncryptUtil.getPasswordFromFileName(name));
        try {
            return new NyCipherInputStream(inputStream, ces);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void getFileSize(Uri uri) {
        if (size <= 0) {
            String uriString = uri.toString();
            if (uriString.startsWith("file://")) {
                File f = new File(uriString.substring("file://".length()));
                isDirectory = f.isDirectory();
                if (isDirectory) {
                    // Log.v(Util.myLogName, "We are dealing with a directory.");
                    size = 0;
                    return;
                }
                size = f.length();
                if (size == 0) {
                    uriString = URLDecoder.decode(uriString).substring(
                            "file://".length());
                    f = new File(uriString);
                    size = f.length();
                }
                ///Log.v(Util.myLogName, "zzz" + size);

            } else {
                try {
                    File f = new File(uriString);
                    isDirectory = f.isDirectory();
                    return;
                } catch (Exception e) {
                    Log.v(TAG, "Not a file... " + uriString);
                    e.printStackTrace();
                }
                Log.v(TAG, "Not a file: " + uriString);

            }
        }
    }

    private void getMime(Uri uri, ContentResolver contentResolver) {
        mime = contentResolver.getType(uri);
        if (mime == null || name == null) {
            mime = "application/octet-stream";
            if (name == null) {
                return;
            }
        }
        if (mime.equals("application/octet-stream")) {
            // we can do better than that
            int pos = name.lastIndexOf('.');
            if (pos < 0)
                return;
            String extension = name.substring(pos).toLowerCase();
            if (extension.equals(".jpg")) {
                mime = "image/jpeg";
                return;
            }
            if (extension.equals(".png")) {
                mime = "image/png";
                return;
            }
            if (extension.equals(".gif")) {
                mime = "image/gif";
                return;
            }
            if (extension.equals(".mp4")) {
                mime = "video/mp4";
                return;
            }
            if (extension.equals(".mts")) {
                mime = "video/mts";
                return;
            }
            if (extension.equals(".vob")) {
                mime = "video/vob";
                return;
            }
            if (extension.equals(".avi")) {
                mime = "video/avi";
                return;
            }
            if (extension.equals(".mov")) {
                mime = "video/mov";
                return;
            }
            if (extension.equals(".vcf")) {
                mime = "text/x-vcard";
                return;
            }
            if (extension.equals(".txt")) {
                mime = "text/plain";
                return;
            }
            if (extension.equals(".html")) {
                mime = "text/html";
                return;
            }
            if (extension.equals(".json")) {
                mime = "application/json";
                return;
            }
            if (extension.equals(".epub")) {
                mime = "application/epub+zip";
                return;
            }

        }

    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getMime() {
        return mime;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Uri getUri() {
        return uri;
    }


}
