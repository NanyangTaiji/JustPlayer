package com.nytaiji.nybase.filePicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.nytaiji.nybase.utils.SystemUtils;


public class MaxHeightScrollView extends ScrollView {

    private Context context;

    public MaxHeightScrollView(Context context) {
        super(context);
        this.context = context;
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @SuppressLint("NewApi")
    public MaxHeightScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(SystemUtils.getScreenHeight(context) / 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
