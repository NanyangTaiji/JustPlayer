package com.nytaiji.nybase.utils;

import static android.content.ContentValues.TAG;

import static com.nytaiji.nybase.utils.NyFileUtil.getImageDir;
import static com.nytaiji.nybase.utils.NyFileUtil.getThumbDir;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VideoProperty {

    public static long extractDuration(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationString != null) {
                return Long.parseLong(durationString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IOException ignored) {}
        }
        return -1; // Return -1 if duration extraction fails
    }


    public static String getYoutubeId(String url) {

        String videoId = "";

        if (url != null && url.trim().length() > 0 && url.startsWith("http")) {
            String expression = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??(v=)?([^#\\&\\?]*).*";
            CharSequence input = url;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);//??? some Urls are NG
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(8);
                if (groupIndex1 != null && groupIndex1.length() == 11)
                    videoId = groupIndex1;
            }
        }
        return videoId;
    }


    /**
     * 获取媒体文件播放时间，格式化输出
     *
     * @param ms 毫秒
     * @return 格式化后的结果：hh:mm:ss
     */
    public static String getMediaTime(int ms) {
        int hour, mintue, second;

        //计算小时 1 h = 3600000 ms
        hour = ms / 3600000;

        //计算分钟 1 min = 60000 ms
        mintue = (ms - hour * 3600000) / 60000;

        //计算秒钟 1 s = 1000 ms
        second = (ms - hour * 3600000 - mintue * 60000) / 1000;

        //格式化输出，补零操作
        String sHour, sMintue, sSecond;
        if (hour < 10) {
            sHour = "0" + hour;
        } else {
            sHour = String.valueOf(hour);
        }

        if (mintue < 10) {
            sMintue = "0" + mintue;
        } else {
            sMintue = String.valueOf(mintue);
        }

        if (second < 10) {
            sSecond = "0" + second;
        } else {
            sSecond = String.valueOf(second);
        }

        return sHour + ":" + sMintue + ":" + sSecond;
    }


    public static String stringForTime(long timeMs) {
        return stringForTime(timeMs, false);
    }

    public static String stringForTime(long timeMs, boolean isSeconds) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = timeMs;
        if (!isSeconds) {
            totalSeconds = timeMs / 1000;
        }
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    //--------------------------------------------------

    public static boolean saveBitmap2Jpg(Bitmap bitmap, File file) {
        if (bitmap != null) {
            OutputStream outputStream;
            try {
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                bitmap.recycle();
                outputStream.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean saveBitmap2Png(Bitmap bitmap1, File file) {
        boolean successful = false;
        if (bitmap1 == null) return false;
        Bitmap bitmap = BitmapUtil.createScaledBitmap(bitmap1, bitmap1.getWidth() / 8, bitmap1.getHeight() / 8, true);

        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            bitmap.recycle();
            //  Log.i("utility", "image saved!");
            outputStream.close();
            successful = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!successful) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        return successful;
    }

    public static void saveBitmap(Bitmap bitmap, File file) {
        if (bitmap != null) {
            OutputStream outputStream;
            try {
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                bitmap.recycle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void shotImage(Context context, TextureView view, String title) {
        {
            Bitmap bitmap = view.getBitmap();
            if (bitmap == null) {
                Log.i(TAG, "image null");
                return;
            }

            File file = new File(getImageDir(), System.currentTimeMillis() + "NY.jpg");
            if (saveBitmap2Jpg(bitmap, file)) {
                shootSound(context);
                Log.i(TAG, "image saved!");
            }
            if (!title.contains("NY")) title = title + "_NY1";
            File file2 = new File(getThumbDir(), title);
            if (saveBitmap2Png(bitmap, file2)) {
                Log.i(TAG, "Thumb saved!");
            }
        }
    }

    public static void shotImage(Context context, Bitmap bitmap, String title) {
        {
            if (bitmap == null) {
                Log.e(TAG, "image null");
                return;
            }
            if (title == null) title = String.valueOf(System.currentTimeMillis());
            File file = new File(getImageDir(), title.trim() + ".jpg");
            if (saveBitmap2Jpg(bitmap, file)) {
                shootSound(context);
                // Log.e(TAG, "image saved!");
            }
         /*   if (!title.contains("NY")) title = title + "_NY1";
            File file2 = new File(getThumbDir(), title);
            if (saveBitmap2Png(bitmap, file2)) {
                Log.e(TAG, "Thumb saved!");
            }*/
        }
    }

    public static void shootSound(Context context) {
        MediaPlayer _shootMP = null;
        if (_shootMP == null)
            _shootMP = MediaPlayer.create(context, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
        if (_shootMP != null)
            _shootMP.start();

    }

    //------------------------------------------------------
    @SuppressLint("StaticFieldLeak")
    public static void retrieveFrameAndSaveToDiskIfNeeded(String fileName, TextureView textureView) {
        File file = NyFileUtil.getThumbFile(fileName);
        if (file.exists()) {
            return;
        }
        if (textureView == null) return;
        saveBitmap2Png(textureView.getBitmap(), file);
    }

    //-----------

    public interface FrameBitmapCallback {
        void onResult(Bitmap bitmap);
    }


        /*
    Void onDestroy() {
        copyFrameHandler.removeCallbacksAndMessages(null);
    }*/


    //If you want to take a simple screenshot(no camera feed is needed) the you can use the TakeScreenshot method alone
    //imageView is inside relativeLayout


    //  public void TakeScreenshot(Context context, RelativeLayout relativeLayout, ImageView imageView){    //THIS METHOD TAKES A SCREENSHOT AND SAVES IT AS .jpg
    @SuppressWarnings("deprecation")
    public void TakeScreenshot(Context context, FrameLayout Layout, ImageView imageView) {    //THIS METHOD TAKES A SCREENSHOT AND SAVES IT AS .jpg
        File file = new File(getImageDir(), System.currentTimeMillis() + "NY.jpg");
        //  file .mkdirs();
        /*
        Random num = new Random();
        int nu = num.nextInt(1000); //PRODUCING A RANDOM NUMBER FOR FILE NAME
        String picId = String.valueOf(nu);
        String myfile = "Ghost" + picId + ".jpeg";
        File dir_image = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Ultimate Entity Detector");
        dir_image.mkdirs();
        //^IN THESE 3 LINES YOU SET THE FOLDER PATH/NAME . HERE I CHOOSE TO SAVE
        //THE FILE IN THE SD CARD IN THE FOLDER "Ultimate Entity Detector"
        */
        Layout.setDrawingCacheEnabled(true); //relativeLayout OR THE NAME OF YOUR LAYOUR
        Layout.buildDrawingCache(true);
        Bitmap bmp = Bitmap.createBitmap(Layout.getDrawingCache());
        Layout.setDrawingCacheEnabled(false); // clear drawing cache
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fis.close();
            fos.close();
            Toast.makeText(context.getApplicationContext(),
                    "The file is saved at the default folder!", Toast.LENGTH_LONG).show();
            Bitmap bmp1 = null;
            imageView.setImageBitmap(bmp1); //RESETING THE PREVIEW
            //  camera.startPreview();             //RESETING THE PREVIEW
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String simpleExtraction(String url) {
        //porntext handle
        if (url.lastIndexOf("=") > -1)
            return url.substring(url.lastIndexOf("=") + 1);
        else if (url.lastIndexOf("/") > -1)
            return url.substring(url.lastIndexOf("/") + 1);
        return "online_" + NyFileUtil.timedFileName();
    }

}
