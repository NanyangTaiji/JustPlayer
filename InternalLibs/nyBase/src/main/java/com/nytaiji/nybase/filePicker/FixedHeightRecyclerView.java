package com.nytaiji.nybase.filePicker;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.nytaiji.nybase.utils.SystemUtils;


public class FixedHeightRecyclerView extends RecyclerView {

    private Context context;

    public FixedHeightRecyclerView(Context context) {
        super(context);
        this.context = context;
    }

    public FixedHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public FixedHeightRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = MeasureSpec.makeMeasureSpec(SystemUtils.getScreenHeight(context) / 2, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }
}
