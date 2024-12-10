package com.nytaiji.exoplayer.exoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nytaiji.exoplayer.R;
import com.nytaiji.exoplayer.trimView.NyTrimmerView;
import com.nytaiji.exoplayer.trimView.SimpleCropView;


/**
 * 1. create R.layout.CropVideoView
 * 2. link resources, add videoPlayerView(or whatever it i called)
 * 3. VVI: DETECT crop area size!!!
 */
public class nyEditVideoView extends NyTrimmerView implements CheckBox.OnCheckedChangeListener {
    private SimpleCropView simpleCropView;
    private OnSizeChangeListener onSizeChangeListener;
    private int editVideoViewHeight = 0;
    private int editVideoViewWidth = 0;
    private boolean isCropable = false;
    private RadioGroup cropRange;


    @Override
    protected int getLayoutId() {
        return R.layout.video_edit_view;
    }

    public nyEditVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public nyEditVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void init(Context context) {
        super.init(context);
        simpleCropView = findViewById(R.id.cropOverlayView);
        changeClickableState(simpleCropView, true);
        cropRange = findViewById(R.id.crop_range);
        //------------------------------//
        ((CheckBox) findViewById(R.id.cb_trim)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.cb_crop)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.cb_rotate)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.cb_rf)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.cb_compress)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.cb_quite)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.crop_1_1)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.crop_4_3)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.crop_3_4)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.crop_16_9)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.crop_9_16)).setOnCheckedChangeListener(this);
    }

    @Override
    public void setVideoUrl(final String videoPath) {
        super.setVideoUrl(videoPath);
    }

   // public SimpleCropView getSimpleCropView() {
       // return simpleCropView;
  //  }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setChecked(isChecked);

        if (buttonView.getId() == R.id.cb_crop) {
            isCropable = isChecked;
            if (isCropable) {
                simpleCropView.setVisibility(View.VISIBLE);
                cropRange.setVisibility(View.VISIBLE);
            } else {
                simpleCropView.setVisibility(View.GONE);
                cropRange.setVisibility(View.GONE);
            }
        }else if (buttonView.getId() == R.id.cb_trim) {
          if (isChecked) mRangeLayout.setVisibility(View.VISIBLE);else mRangeLayout.setVisibility(View.GONE);

        } else if (buttonView.getId() == R.id.crop_0) {
            if (isChecked) simpleCropView.setVisibility(View.GONE);
        } else if (buttonView.getId() == R.id.crop_4_3) {
            if (isChecked) simpleCropView.resetAspectRatio(4, 3);
        } else if (buttonView.getId() == R.id.crop_16_9) {
            if (isChecked) simpleCropView.resetAspectRatio(16, 9);
        } else if (buttonView.getId() == R.id.crop_9_16) {
            if (isChecked) simpleCropView.resetAspectRatio(9, 16);
        } else if (buttonView.getId() == R.id.crop_3_4) {
            if (isChecked) simpleCropView.resetAspectRatio(3, 4);
        }
    }

    private void changeClickableState(View view, boolean isClickable) {
        view.setClickable(isClickable);
        view.setFocusable(isClickable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.editVideoViewHeight = h;
        this.editVideoViewWidth = w;
        if (this.onSizeChangeListener != null) {
            this.onSizeChangeListener.doOnSizeChange(w, h, oldw, oldh);
        }

    }

    public void setOnSizeChangeListener(OnSizeChangeListener listener) {
        this.onSizeChangeListener = listener;
    }

    public int getEditVideoViewHeight() {
        return editVideoViewHeight;
    }

    public int getEditVideoViewWidth() {
        return editVideoViewWidth;
    }


    public interface OnSizeChangeListener {
        void doOnSizeChange(int width, int height, int oldwidth, int oldheight);
    }
}
