package com.nytaiji.epf;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;

import com.nytaiji.epf.filter.GlFilter;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public abstract class GlFrameBufferObjectRenderer implements GLSurfaceView.Renderer {

    private GlFrameBufferObject framebufferObject;
    private GlFilter normalShader;

    private final Queue<Runnable> runOnDraw;


    public GlFrameBufferObjectRenderer() {
        runOnDraw = new LinkedList<Runnable>();
    }


    @Override
    public final void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        framebufferObject = new GlFrameBufferObject();
        normalShader = new GlFilter();
        normalShader.setup();
        onSurfaceCreated(config);
    }

    @Override
    public final void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        framebufferObject.setup(width, height);
        normalShader.setFrameSize(width, height);
        onSurfaceChanged(width, height);
    }

    @Override
    public final void onDrawFrame(final GL10 gl) {
        synchronized (runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDraw.poll().run();
            }
        }
        framebufferObject.enable();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        onDrawFrame(framebufferObject);

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        normalShader.draw(framebufferObject.getTexName(), null);

        takeBitmap(gl);

    }

    @Override
    protected void finalize() throws Throwable {

    }

    public abstract void onSurfaceCreated(EGLConfig config);

    public abstract void onSurfaceChanged(int width, int height);

    public abstract void onDrawFrame(GlFrameBufferObject fbo);


    //---------------------------//
    private boolean mTakeShotPic = false;

    protected boolean mHighShot = false;
    private VideoShotListener videoShotListener;
    public void takeShotPic() {
        mTakeShotPic = true;
    }

    /**
     * 截图监听
     */
    public void setVideoShotListener(VideoShotListener listener, boolean high) {
        this.videoShotListener = listener;
        this.mHighShot = high;
    }

    protected void takeBitmap(GL10 glUnused) {
        if (mTakeShotPic) {
            mTakeShotPic = false;
            if (videoShotListener != null) {
                Bitmap bitmap = createBitmapFromGLSurface(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight(), glUnused);
                videoShotListener.getBitmap(bitmap);
            }
        }
    }

    /**
     * 创建bitmap截图
     */
    protected Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.
                            GL_UNSIGNED_BYTE,
                    intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }
        if (mHighShot) {
            return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
        } else {
            return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.RGB_565);
        }
    }

}
