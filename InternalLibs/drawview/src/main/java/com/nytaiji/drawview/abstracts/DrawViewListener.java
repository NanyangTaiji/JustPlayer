package com.nytaiji.drawview.abstracts;

import android.graphics.Bitmap;

import com.nytaiji.drawview.dictionaries.DrawCapture;
import com.nytaiji.drawview.enums.BackgroundType;
import com.nytaiji.drawview.interfaces.OnDrawViewListener;

public abstract class DrawViewListener implements OnDrawViewListener{

    @Override
    public void onStartDrawing() {

    }

    @Override
    public void onEndDrawing() {

    }

    @Override
    public void onClearDrawing() {

    }

    @Override
    public void onRequestText() {

    }

    @Override
    public void onAllMovesPainted() {

    }

    @Override
    public void onDrawBackgroundStart() {

    }

    @Override
    public void onDrawBackgroundEnds(Bitmap bitmap, BackgroundType originBackgroundType) {

    }

    @Override
    public void onDrawBackgroundEnds(byte[] bytes, BackgroundType originBackgroundType) {

    }

    @Override
    public void onDrawingError(Exception e) {

    }

    @Override
    public void onCaptureCreated(DrawCapture drawCapture) {

    }
}
