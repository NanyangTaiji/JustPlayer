package com.nytaiji.epf;

import android.graphics.SurfaceTexture;

import com.nytaiji.epf.filter.GlPreviewFilter;


/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class GlSurfaceTexture implements SurfaceTexture.OnFrameAvailableListener {

    private final SurfaceTexture surfaceTexture;
    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener;

    public GlSurfaceTexture(final int texName) {
        surfaceTexture = new SurfaceTexture(texName);
        surfaceTexture.setOnFrameAvailableListener(this);
    }

    public void setOnFrameAvailableListener(final SurfaceTexture.OnFrameAvailableListener l) {
        onFrameAvailableListener = l;
    }


    public int getTextureTarget() {
        return GlPreviewFilter.GL_TEXTURE_EXTERNAL_OES;
    }

    public void updateTexImage() {
        surfaceTexture.updateTexImage();
    }

    public void getTransformMatrix(final float[] mtx) {
        surfaceTexture.getTransformMatrix(mtx);
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFrameAvailable(this.surfaceTexture);
        }
    }

    public void release() {
        surfaceTexture.release();
    }
}
