package com.nytaiji.core.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.IMediaPlayer;
import com.nytaiji.nybase.utils.BitmapUtil;
import com.nytaiji.epf.VideoShotListener;
import com.nytaiji.epf.VideoShotSaveListener;
import com.nytaiji.epf.filter.GlFilter;

import java.io.File;

public class SurfaceRenderView extends SurfaceView implements IRenderView, SurfaceHolder.Callback {

    protected IMediaPlayer player;
    private BaseConstants.ScaleType scaleType = BaseConstants.ScaleType.SCALE_DEFAULT;
    private int videoWidth, videoHeight;

    public SurfaceRenderView(Context context) {
        super(context);
        setZOrderMediaOverlay(true);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
    }

    @Override
    public void setVideoSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }


    @Override
    public View getRenderView() {
        return this;
    }

    @Override
    public IMediaPlayer getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(IMediaPlayer player) {
        this.player = player;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setSurfaceHolder(holder); //for androidplayer
        player.setSurfaceView(this);//for exoplayer, vlcplayer
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //TODO seem useless
        holder.removeCallback(this);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //  super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // if (videoWidth > 0 && videoHeight > 0) {
        //    setMeasuredDimension(videoWidth, videoHeight);
        //  }
        //TODO NY
        if (getRotation() == 90 || getRotation() == 270) { // 软解码时处理旋转信息，交换宽高
            widthMeasureSpec = widthMeasureSpec + heightMeasureSpec;
            heightMeasureSpec = widthMeasureSpec - heightMeasureSpec;
            widthMeasureSpec = widthMeasureSpec - heightMeasureSpec;
        }

        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);

//        Log.d("@@@@", "onMeasure: width" + width + "    height:" + height);

        //如果设置了比例
        switch (scaleType) {
            case SCALE_ORIGINAL:
                width = videoWidth;
                height = videoHeight;
                break;
            case SCALE_16_9:
                if (height > width / 16 * 9) {
                    height = width / 16 * 9;
                } else {
                    width = height / 9 * 16;
                }
                break;
            case SCALE_32_9:
                if (height > width / 32 * 9) {
                    height = width / 32 * 9;
                } else {
                    width = height / 9 * 32;
                }
                break;
            case SCALE_4_3:
                if (height > width / 4 * 3) {
                    height = width / 4 * 3;
                } else {
                    width = height / 3 * 4;
                }
//                Log.d("@@@@", "onMeasure 4:3 : width" + width + "    height:" + height);
                break;
            case SCALE_MATCH_PARENT:
                width = widthMeasureSpec;
                height = heightMeasureSpec;
                break;
            case SCALE_CENTER_CROP:
                if (videoWidth > 0 && videoHeight > 0) {
                    if (videoWidth * height > width * videoHeight) {
                        width = height * videoWidth / videoHeight;
                    } else {
                        height = width * videoHeight / videoWidth;
                    }
                }
                break;
            case SCALE_DEFAULT:
                if (videoWidth > 0 && videoHeight > 0) {

                    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
                    int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
                    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
                    int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

                    if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                        // the size is fixed
                        width = widthSpecSize;
                        height = heightSpecSize;

                        // for compatibility, we adjust size based on aspect ratio
                        if (videoWidth * height < width * videoHeight) {
                            //Log.i("@@@", "image too wide, correcting");
                            width = height * videoWidth / videoHeight;
                        } else if (videoWidth * height > width * videoHeight) {
                            //Log.i("@@@", "image too tall, correcting");
                            height = width * videoHeight / videoWidth;
                        }
                    } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                        // only the width is fixed, adjust the height to match aspect ratio if possible
                        width = widthSpecSize;
                        height = width * videoHeight / videoWidth;
                        if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                            // couldn't match aspect ratio within the constraints
                            height = heightSpecSize;
                        }
                    } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                        // only the height is fixed, adjust the width to match aspect ratio if possible
                        height = heightSpecSize;
                        width = height * videoWidth / videoHeight;
                        if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                            // couldn't match aspect ratio within the constraints
                            width = widthSpecSize;
                        }
                    } else {
                        // neither the width nor the height are fixed, try to use actual video size
                        width = videoWidth;
                        height = videoHeight;
                        if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                            // too tall, decrease both width and height
                            height = heightSpecSize;
                            width = height * videoWidth / videoHeight;
                        }
                        if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                            // too wide, decrease both width and height
                            width = widthSpecSize;
                            height = width * videoHeight / videoWidth;
                        }
                    }
                } else {
                    // no size yet, just adopt the given spec sizes
                }
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void setDisplayScale(BaseConstants.ScaleType type) {
        scaleType = type;
        requestLayout();
    }

    @Override
    public void setGlFilter(GlFilter filter) {

    }

    /**
     * 暂停时初始化位图
     */
    @Override
    public Bitmap initCover() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(
                getWidth(), getHeight(), Bitmap.Config.RGB_565);
        return bitmap;

    }

    /**
     * 暂停时初始化位图
     */
    @Override
    public Bitmap initCoverHigh() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(
                getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        return bitmap;

    }

    /**
     * 获取截图
     *
     * @param shotHigh 是否需要高清的
     */
    public void taskShotPic(VideoShotListener videoShotListener, boolean shotHigh) {
        Bitmap bitmap;
        if (shotHigh) {
            bitmap = initCoverHigh();
        } else {
            bitmap = initCover();
        }
        try {
            HandlerThread handlerThread = new HandlerThread("PixelCopier");
            handlerThread.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PixelCopy.request(this, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
                    @Override
                    public void onPixelCopyFinished(int copyResult) {
                        if (copyResult == PixelCopy.SUCCESS) {
                            videoShotListener.getBitmap(bitmap);
                        }
                        handlerThread.quitSafely();
                    }
                }, new Handler());
            } else {
                // Debuger.printfLog(getClass().getSimpleName() +
                //  " Build.VERSION.SDK_INT < Build.VERSION_CODES.N not support taskShotPic now");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存截图
     *
     * @param high 是否需要高清的
     */
    public void saveFrame(final File file, final boolean high, final VideoShotSaveListener videoShotSaveListener) {
        taskShotPic(new VideoShotListener() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                boolean success = BitmapUtil.saveBitmap(file.getAbsolutePath(), bitmap, Bitmap.CompressFormat.JPEG, 100);
                if (videoShotSaveListener != null) videoShotSaveListener.result(success, file);
            }
        }, true);
        // Debuger.printfLog(getClass().getSimpleName() + " not support saveFrame now, use taskShotPic");
    }

}
