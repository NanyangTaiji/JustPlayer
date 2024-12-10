package com.nytaiji.core.filter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nytaiji.core.R;
import com.nytaiji.core.view.RoundCornerImageView;


import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;


public class FilterOptionsAdapter extends RecyclerView.Adapter<FilterOptionsAdapter.FilterOptionsViewHolder> {
    private final List<Pair<FilterType, GPUImageFilterTools.ImageFilterType>> listFilters = FilterType.getFilterPairs();
    private final FilterListener filterListener;
    private final Bitmap[] thumbnail = new Bitmap[listFilters.size()];
    private final Activity activity;
    private final Bitmap bitmapThumbnail;
    private final GPUImage gpuImage;
    private FilterOptionsViewHolder currentHolder;
    private Map<FilterType, String> mp = new TreeMap<>();


    public FilterOptionsAdapter(Activity activity, FilterListener filterListener, Bitmap bitmapThumbnail) {
        this.activity = activity;
        this.filterListener = filterListener;
        this.bitmapThumbnail = bitmapThumbnail;
        this.thumbnail[0] = this.bitmapThumbnail;
        for (int i = 1; i < this.thumbnail.length; i++) {
            thumbnail[i] = null;
        }
        this.gpuImage = new GPUImage(activity);
        this.currentHolder = null;
        initNameMap();
    }

    @NonNull
    @Override
    public FilterOptionsAdapter.FilterOptionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_preview_item, parent, false);
        FilterOptionsViewHolder filterOptionsViewHolder = new FilterOptionsViewHolder(rootView);
        return filterOptionsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FilterOptionsAdapter.FilterOptionsViewHolder holder, int position) {
        String currentFilterName = mp.get(listFilters.get(position).first);
        holder.filterName.setText(currentFilterName);
        if (thumbnail[position] == null) {
            GPUImageFilter gpuImageFilter = GPUImageFilterTools.createFilterForType(activity, listFilters.get(position).second);
            gpuImage.setImage(bitmapThumbnail);
            gpuImage.setFilter(gpuImageFilter);
            thumbnail[position] = gpuImage.getBitmapWithFilterApplied();
        }
        holder.videoThumbnailPreview.setImageBitmap(thumbnail[position]);
        holder.itemView.setOnClickListener(v -> {
            filterListener.onFilterChecked(listFilters.get(position).first);
            changeTextColor(holder);
        });
        if ((position == 0) && (currentHolder == null)) { // onFirstTime(that is currentHolder = null), the DEFAULT filter(NO_FILTER) is selected, so it should have the green text
            changeTextColor(holder);
        }
    }

    @Override
    public int getItemCount() {
        return listFilters.size();
    }

    public static class FilterOptionsViewHolder extends RecyclerView.ViewHolder {
        private final RoundCornerImageView videoThumbnailPreview;
        private final TextView filterName;

        public FilterOptionsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.videoThumbnailPreview = itemView.findViewById(R.id.videoThumbnailPreview);
            this.filterName = itemView.findViewById(R.id.filterName);
        }
    }

    private void changeTextColor(FilterOptionsViewHolder holder) {
        if (currentHolder != null) {
            currentHolder.filterName.setTextColor(activity.getResources().getColor(R.color.textColorPrimary));
        }
        currentHolder = holder;
        currentHolder.filterName.setTextColor(activity.getResources().getColor(com.nytaiji.nybase.R.color.colorGold));
    }

    //  public interface FilterListener{
    //   void onFilterChange(FilterType selectedFilterType);
    // }

    private void initNameMap() {
        if (mp == null) mp = new TreeMap<>();
        mp.put(FilterType.DEFAULT, "Default");
        mp.put(FilterType.EXPOSURE, "Exposure");
        mp.put(FilterType.BRIGHTNESS, "Brightness");
        mp.put(FilterType.CONTRAST, "Contrast");
        mp.put(FilterType.SHARP, "Sharp");
        mp.put(FilterType.SATURATION, "Saturation");
        mp.put(FilterType.HUE, "Hue");
        mp.put(FilterType.GAMMA, "Gamma");
        mp.put(FilterType.HIGHLIGHT_SHADOW, "Highlight shadow");
        mp.put(FilterType.HAZE, "Haze");
        mp.put(FilterType.SEPIA, "Sepia");
        mp.put(FilterType.TONE, "Tone");
        mp.put(FilterType.VIBRANCE, "Vibrance");
        mp.put(FilterType.WHITE_BALANCE, "White balance");
        mp.put(FilterType.HALFTONE, "Halftone");
        mp.put(FilterType.OPACITY, "Opacity");
        mp.put(FilterType.MONOCHROME, "Monochrome");
        mp.put(FilterType.GRAY_SCALE, "Gray scale");
        mp.put(FilterType.PIXELATION, "Pixelation");
        mp.put(FilterType.POSTERIZE, "Posterize");
        mp.put(FilterType.SOLARIZE, "Solarize");
        mp.put(FilterType.VIGNETTE, "Vignette");
        mp.put(FilterType.LUMINANCE, "Luminance");
        mp.put(FilterType.LUMINANCE_THRESHOLD, "Luminance threshold");
        mp.put(FilterType.THRESHOLD_EDGE_DETECTION, "Threshold_edge");
        mp.put(FilterType.SKETCH, "Sketch");
        mp.put(FilterType.CROSSHATCH, "Cross hatch");
        mp.put(FilterType.WEAK_PIXEL, "Weak pixel");
        mp.put(FilterType.INVERT, "Invert");
        mp.put(FilterType.GAUSSIAN_FILTER, "Gaussian");
        mp.put(FilterType.BILATERAL_BLUR, "Bilateral blur");
        mp.put(FilterType.BOX_BLUR, "Box blur");
        mp.put(FilterType.ZOOM_BLUR, "Zoom blur");
        mp.put(FilterType.BULGE_DISTORTION, "Bluge distortion");
        mp.put(FilterType.CGA_COLORSPACE, "CGA Colorspace");
        mp.put(FilterType.RGB, "RGB");
        mp.put(FilterType.LOOK_UP_TABLE_SAMPLE, "Look up");
        mp.put(FilterType.OVERLAY, "Overlay");
        mp.put(FilterType.SPHERE_REFRACTION, "Sphere");
        mp.put(FilterType.TONE_CURVE_SAMPLE, "Tone curve");
        mp.put(FilterType.WATERMARK, "Watermark");
        mp.put(FilterType.FILTER_GROUP_SAMPLE, "Filter group");
        mp.put(FilterType.BITMAP_OVERLAY_SAMPLE, "Bitmap overlay");
    }
}
