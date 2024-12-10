package com.nytaiji.core.render;

import static com.nytaiji.nybase.utils.VideoProperty.saveBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.View;

import com.nytaiji.core.base.BaseConstants;
import com.nytaiji.core.base.IMediaPlayer;
import com.nytaiji.epf.VideoShotListener;
import com.nytaiji.epf.VideoShotSaveListener;
import com.nytaiji.epf.chooser.EConfigChooser;
import com.nytaiji.epf.contextfactory.EContextFactory;
import com.nytaiji.epf.filter.GlFilter;

import java.io.File;

public class GLRenderView extends GLSurfaceView implements IRenderView, SurfaceTexture.OnFrameAvailableListener {
    private BaseConstants.ScaleType scaleType = BaseConstants.ScaleType.SCALE_DEFAULT;
    protected IMediaPlayer player;

    private int videoWidth, videoHeight;


    public EPlayerRenderer renderer;

    private boolean updateSurface = false;

    public GLRenderView(Context context) {
        super(context);

        setEGLContextFactory(new EContextFactory());
        setEGLConfigChooser(new EConfigChooser());

        renderer = new EPlayerRenderer(this);
        setRenderer(renderer);

    }

    @Override
    public View getRenderView() {
        return this;
    }


    @Override
    public void setVideoSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }

    @Override
    public IMediaPlayer getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(IMediaPlayer player) {
        if (this.player != null) {
            this.player.release();
            this.player = null;
        }
        this.player = player;

        this.renderer.setPlayer(player);
    }

    @Override
    public synchronized void onFrameAvailable(final SurfaceTexture previewTexture) {
        updateSurface = true;
        requestRender();
    }

    @Override
    public void setGlFilter(final GlFilter filter){renderer.setGlFilter(filter);}

    @Override
    public Bitmap initCover() {
        // Debuger.printfLog(getClass().getSimpleName() + " not support initCover now");
        return null;
    }

    @Override
    public Bitmap initCoverHigh() {
       // Debuger.printfLog(getClass().getSimpleName() + " not support initCoverHigh now");
        return null;
    }

    /**
     * 获取截图
     *
     * @param shotHigh 是否需要高清的
     */
    @Override
    public void taskShotPic(VideoShotListener videoShotListener, boolean shotHigh) {
        if (videoShotListener != null) {
            renderer.setVideoShotListener(videoShotListener, shotHigh);
            renderer.takeShotPic();
        }
    }

    /**
     * 保存截图
     *
     * @param high 是否需要高清的
     */
    @Override
    public void saveFrame(final File file, final boolean high, final VideoShotSaveListener gsyVideoShotSaveListener) {
        VideoShotListener gsyVideoShotListener = new VideoShotListener() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                if (bitmap == null) {
                    gsyVideoShotSaveListener.result(false, file);
                } else {
                    saveBitmap(bitmap, file);
                    gsyVideoShotSaveListener.result(true, file);
                }
            }
        };
        setVideoShotListener(gsyVideoShotListener,true);
        takeShotPic();
    }

    public void takeShotPic() {
        renderer.takeShotPic();
    }

    public void setVideoShotListener(VideoShotListener listener, boolean high) {
        this.renderer.setVideoShotListener(listener, high);
    }

    @Override
    public void setDisplayScale(BaseConstants.ScaleType type) {
        scaleType = type;
        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
//                + MeasureSpec.toString(heightMeasureSpec) + ")");
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

    //-----------------------------//


}
