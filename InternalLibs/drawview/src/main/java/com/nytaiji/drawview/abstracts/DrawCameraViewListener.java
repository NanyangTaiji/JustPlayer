package com.nytaiji.drawview.abstracts;

import com.nytaiji.drawview.dictionaries.DrawCapture;
import com.nytaiji.drawview.interfaces.OnDrawCameraViewListener;

public abstract class DrawCameraViewListener implements OnDrawCameraViewListener {

    @Override
    public void onDrawCameraViewCaptureStart() {

    }

    @Override
    public void onDrawCameraViewCaptureEnd(DrawCapture capture) {

    }

    @Override
    public void onDrawCameraViewError(Exception e) {

    }
}
