package com.nytaiji.nybase.crypt;

import static com.nytaiji.nybase.crypt.EncryptUtil.AnnouncedAndRegister;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;


public class EncryptedDownloadTask extends AsyncTask<Void, Void, Void> {
    private final Context mContext;
    private final String mUrl;
    private final File mFile;
    private final Cipher mCipher;

    public EncryptedDownloadTask(Context context, String url, File file, Cipher cipher) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("You need to supply a url to a clear MP4 file to download and encrypt, or modify the code to use a local encrypted mp4");
        }
        //  Log.e(getClass().getCanonicalName(), "mUrl:---"+url);
        //  Log.e(getClass().getCanonicalName(), "file:---"+file.getPath());
        mContext = context;
        mUrl = url;
        mFile = file;
        mCipher = cipher;
    }

    @Override
    protected Void doInBackground(Void... params) {


        try {
            downloadAndEncrypt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //  Log.d(getClass().getCanonicalName(), "downloaded");
        AnnouncedAndRegister(mContext, mFile);
    }

    private void downloadAndEncrypt() throws Exception {

        URL url = new URL(mUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("server error: " + connection.getResponseCode() + ", " + connection.getResponseMessage());
        }

        InputStream inputStream = connection.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream(mFile);

        CipherOutputStream cipherOutputStream = null;

        if (mCipher != null) cipherOutputStream = new CipherOutputStream(fileOutputStream, mCipher);

        byte[] buffer = new byte[1024 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            //  Log.e(getClass().getCanonicalName(), "reading from http...");
            if (cipherOutputStream != null) cipherOutputStream.write(buffer, 0, bytesRead);
            else fileOutputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        if (cipherOutputStream != null) cipherOutputStream.close();
        else fileOutputStream.close();
        connection.disconnect();
    }


}
