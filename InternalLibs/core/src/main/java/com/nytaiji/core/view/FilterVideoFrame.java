package com.nytaiji.core.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nytaiji.core.R;
import com.nytaiji.core.filter.FilterAdjuster;
import com.nytaiji.core.filter.FilterListener;
import com.nytaiji.core.filter.FilterType;
import com.nytaiji.core.filter.FilterViewAdapter;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.epf.filter.GlFilter;
import com.nytaiji.epf.filter.GlFilterGroup;

import java.util.ArrayList;


/**
 * An extension of StandardVideoView such that
 */

public class FilterVideoFrame extends IntermediateVideoFrame implements FilterListener {
    public static final String TAG = "FilterVideoFrame";

   // private static FilterViewAdapter mFilterViewAdapter;
    private RecyclerView filterView;
    protected AppCompatSeekBar filterSeekBar;
    private FilterAdjuster adjuster;
    private ArrayList<GlFilter> glFilters = new ArrayList<GlFilter>();
    private ArrayList<FilterType> filterSelected = new ArrayList<FilterType>();
    private GlFilter adjustedfilter = null;
    private int[] adjPercentage = new int[FilterType.values().length];
    private int adjIndex = 0;
    private int totalFilters = 0;
    private Context context;


    public FilterVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public FilterVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.filter_video_frame;
    }

    //---------------------------
    //  private ImageView encryptToggle;
    private ImageView filterToggle;
    private LinearLayoutManager llmFilters;

    protected void initView(Context context) {
        super.initView(context);

        //-----------------
        filterToggle = findViewById(R.id.bt_filter);
        filterToggle.setOnClickListener(this);
        filterSeekBar = findViewById(R.id.sb_filter_adjust);
        filterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (adjuster != null) {
                    adjuster.adjust(adjustedfilter, progress);
                    adjPercentage[adjIndex] = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //  filterSeekBar.setVisibility(GONE);
                //  bottomBasic.setVisibility(VISIBLE);
            }
        });
        //--------init filters-----------------
        filterView = findViewById(R.id.rvFilterView);
        llmFilters = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        filterView.setLayoutManager(llmFilters);
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.FROYO)
    @Override
    public void setVideoUrl(String url) {
        super.setVideoUrl(url);
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Video.Thumbnails.MINI_KIND);
        FilterViewAdapter mFilterViewAdapter = new FilterViewAdapter(NyFileUtil.getActivity(context), this, thumbnail);
        filterView.setAdapter(mFilterViewAdapter);
        totalFilters = mFilterViewAdapter.getItemCount();
    }


    private void reset() {
        FilterViewAdapter.mViewHolder mv;
        for (int i = 0; i < totalFilters; i++) {
            mv = (FilterViewAdapter.mViewHolder) filterView.findViewHolderForAdapterPosition(i);
            if (mv != null) mv.clear();
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.bt_filter) {
            if (filterView.getVisibility() != VISIBLE) filterView.setVisibility(VISIBLE);
            else {
                filterView.setVisibility(GONE);
                filterSeekBar.setVisibility(GONE);
            }
        }
    }


    @Override
    public void onFilterChecked(FilterType filterType) {
        filterSeekBar.setVisibility(View.GONE);
       // Log.e("FilterVideoFrame", "filterType = " + filterType);
        if (filterType == FilterType.DEFAULT) {
          //  Log.e("FilterVideoFrame", "filterType = " + filterType);
            glFilters.clear();
          //  Log.e("FilterVideoFrame", "glFilters " + glFilters);
            setGlFilter(FilterType.createGlFilter(FilterType.DEFAULT, getContext()));
            reset();
            return;
        }

        if (filterSelected.contains(filterType)) {
            filterSelected.remove(filterType);
        } else {
            filterSelected.add(filterType);
        }
        //  Log.e("FilterVideoFrame", "filterSelected "+filterSelected);

        glFilters.clear();
        for (int i = 0; i < filterSelected.size(); i++) {
            GlFilter filter = FilterType.createGlFilter(filterSelected.get(i), getContext());
            glFilters.add(filter);
            adjIndex = FilterType.valueOf(filterSelected.get(i).toString()).ordinal();
            adjuster = FilterType.createFilterAdjuster(filterSelected.get(i));
            if (adjuster != null) adjuster.adjust(filter, adjPercentage[adjIndex]);
            //endable adjuster for the current one
            if (filterSelected.get(i) == filterType && adjuster != null) {
                adjustedfilter = filter;
                filterSeekBar.setProgress(adjPercentage[adjIndex]);
                filterSeekBar.setVisibility(adjuster != null ? View.VISIBLE : View.GONE);
            }
        }

        if (glFilters.size() != 0) {
            Log.e("FilterVideoFrame", "glFilters " + glFilters);
            setGlFilter(new GlFilterGroup(glFilters));
        } else setGlFilter(FilterType.createGlFilter(FilterType.DEFAULT, getContext()));
    }

}
