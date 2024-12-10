package com.nytaiji.exoplayer.exoview;


import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Size;

import com.nytaiji.core.filter.FilterType;
import com.nytaiji.epf.filter.GlFilter;


/**
 * @brief: this model will hold the values of different edit parameters
 * eg, trimStart, trimEnd, currentFilter, etc...
 */
public class VideoEditModel {
    // todo: add variables accordingly. Nothing stays in the VideoEditFragment
  //  private float ratioX = 1;
  //  private float ratioY = 1;
  //  private float aspectRatio = ratioX / ratioY;  // todo: maybe add getter, setter for aspect ratio to be used with onClick changeAspectRatio...
    //       making them static for convenience/ testing. change if necessary
    private Size resolution = null;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private Uri videoUri = null;
    private String sourcePath = null;
    private String destPath = null; // "// some path";

    long mStartTimeMs = 0;
    long mEndTimeMs = 0;
    long mDurationMs = 0;

    //----------------------------------------//
    private boolean beFiltered = false;
    private boolean beMirrored = false;
    private boolean beCompressed = false;
    private boolean beTrimmed = false;
    private boolean beRotated = false;
    private boolean beQuite = false;

    private final float baseWidthSize = 0;



    private FilterType currentFilterType = FilterType.DEFAULT; //  = filters[0];
    private GlFilter composerGlFilter = null;  // <-- use with the mp4composer
    private GlFilter previewGlFilter = null;  // <-- use with trimView
    private int imageMaxHeight = 0;
    private int imageMaxWidth = 0;
    public Bitmap thumbnail;

    public enum CropType {
        FULL, SQUARE, PORTRAIT_3_4, LANDSCAPE_4_3, PORTRAIT_9_16,LANDSCAPE_16_9
    }

    private CropType cropType= CropType.FULL;

}
