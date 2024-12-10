package com.nytaiji.core.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nytaiji.core.R;
import com.nytaiji.core.base.BasePlayer;
import com.nytaiji.core.filter.FilterAdjuster;
import com.nytaiji.core.filter.FilterListener;
import com.nytaiji.core.filter.FilterType;
import com.nytaiji.core.filter.FilterViewAdapter;
import com.nytaiji.epf.filter.GlFilter;
import com.nytaiji.epf.filter.GlFilterGroup;

import java.util.ArrayList;


/**
 * An extension of StandardVideoView such that
 */

public class FsFilterVideoFrame extends FilterVideoFrame  {
    public static final String TAG = "FsFilterVideoFrame";

    private Context context;


    public FsFilterVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public FsFilterVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FsFilterVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAllEnabled(true);
        setLive(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
       // Log.e("FsFilterVideoView", "onTouch");
        if (!isFullScreen() && getCurrentState() != BasePlayer.STATE_COMPLETED) {
            return false;
        }
        return super.onTouch(v, event);
    }

    @Override
    public void setViewsVisible(int topLayoutVisi, int centerLayoutVisi, int bottomLayoutVisi, int failedLayoutVisi, int loadingVisi, int thumbVisi, int replayLayoutVisi) {
        if (topEnabled) setTopVisi(topLayoutVisi); else setTopVisi(View.GONE);
        if (centerEnabled) setCenterVisi(centerLayoutVisi); else setCenterVisi(View.GONE);
        if (bottomEnabled) setBottomVisi(bottomLayoutVisi); else setBottomVisi(View.GONE);
        if (failEnabled) failedLayout.setVisibility(failedLayoutVisi); else failedLayout.setVisibility(View.GONE);
        if (loadingEnabled) loadingProgressBar.setVisibility(loadingVisi);else loadingProgressBar.setVisibility(View.GONE);
        if (thumbEnabled) thumbView.setVisibility(thumbVisi);else thumbView.setVisibility(View.GONE);
        if (replayEnabled) replayLayout.setVisibility(replayLayoutVisi);else replayLayout.setVisibility(View.GONE);
    }

    @Override
    protected void setMainVisi(int visi) {
        if (bottomEnabled) setBottomVisi(visi); else setBottomVisi(View.GONE);
        if (centerEnabled) setCenterVisi(visi); else setCenterVisi(View.GONE);
        if (topEnabled) setTopVisi(visi); else setTopVisi(View.GONE);
    }


    boolean topEnabled,centerEnabled, bottomEnabled, failEnabled, loadingEnabled, thumbEnabled, replayEnabled;

    public void setAllEnabled(boolean yesOrno){
        topEnabled=yesOrno;
        centerEnabled=yesOrno;
        bottomEnabled=yesOrno;
        failEnabled=yesOrno;
        loadingEnabled=yesOrno;
        thumbEnabled=yesOrno;
        replayEnabled=yesOrno;
    }

    public void keepCentralControlsOnly(){
        setAllEnabled(false);
        centerEnabled=true;
    }

}
