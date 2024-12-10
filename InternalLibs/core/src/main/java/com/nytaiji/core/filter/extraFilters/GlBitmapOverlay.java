package com.nytaiji.core.filter.extraFilters;

import static com.nytaiji.nybase.utils.SystemUtils.getScreenHeight;
import static com.nytaiji.nybase.utils.SystemUtils.getScreenWidth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.nytaiji.nybase.utils.BitmapUtil;
import com.nytaiji.epf.filter.GlOverlayFilter;


public class GlBitmapOverlay extends GlOverlayFilter {

    private final Bitmap bitmap;
    private Position position = Position.LEFT_TOP;

    public GlBitmapOverlay(Context context, Bitmap bitmap, Position position) {
        int swidth = getScreenWidth(context);
        int sheight = getScreenHeight(context);
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float ratio = width / height;

        int standard;
        if (swidth > sheight) {
            this.bitmap = BitmapUtil.zoom(bitmap, (int )ratio*sheight/9,sheight/9);
        } else {
            this.bitmap = BitmapUtil.zoom(bitmap, swidth/9,(int )ratio*swidth/9);
        }
        this.position = position;
    }
/*
    public GlBitmapOverlay(Bitmap bitmap, Position position, float scale) {
        this.bitmap = BitmapUtils.scale(bitmap, scale, scale);
        this.position = position;
    }*/

    @Override
    protected void drawCanvas(Canvas canvas) {
        if (bitmap != null && !bitmap.isRecycled()) {
            switch (position) {
                case LEFT_TOP:
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    break;
                case LEFT_BOTTOM:
                    canvas.drawBitmap(bitmap, 0, canvas.getHeight() - bitmap.getHeight(), null);
                    break;
                case RIGHT_TOP:
                    canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth()-30, 15, null);
                    break;
                case RIGHT_BOTTOM:
                    canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth()-20, canvas.getHeight() - bitmap.getHeight()-10, null);
                    break;
            }
        }
    }

    public enum Position {
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_BOTTOM
    }
}
