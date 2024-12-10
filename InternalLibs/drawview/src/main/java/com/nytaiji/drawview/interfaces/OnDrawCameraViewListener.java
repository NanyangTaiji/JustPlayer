package com.nytaiji.drawview.interfaces;

import com.nytaiji.drawview.dictionaries.DrawCapture;

public interface OnDrawCameraViewListener {

    void onDrawCameraViewCaptureStart();
    void onDrawCameraViewCaptureEnd(DrawCapture capture);
    void onDrawCameraViewError(Exception e);
}
