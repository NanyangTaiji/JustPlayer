package com.nytaiji.core.filter;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.nytaiji.core.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;


public class FilterViewAdapter extends RecyclerView.Adapter<FilterViewAdapter.mViewHolder> {

    private final FilterListener mFilterListener;
    //  private final List<Pair<String, FilterType>> mPairList = new ArrayList<>();
    private final List<Pair<FilterType, GPUImageFilterTools.ImageFilterType>> listFilters = FilterType.getFilterPairs();
    private final Bitmap[] thumbnail = new Bitmap[listFilters.size()];
    private final Activity activity;
    private Bitmap bitmapThumbnail = null;
    private final GPUImage gpuImage;
    private mViewHolder currentHolder;

  /*  public FilterViewAdapter(FilterListener filterListener) {
        mFilterListener = filterListener;
       // setupFilters();
    }*/

    public FilterViewAdapter(Activity activity, FilterListener filterListener, Bitmap bitmapThumbnail) {
        mFilterListener = filterListener;
        this.activity = activity;
        this.bitmapThumbnail = bitmapThumbnail;
        initNameMap();
        this.thumbnail[0] = this.bitmapThumbnail;
        for (int i = 1; i < this.thumbnail.length; i++) {
            thumbnail[i] = null;
        }
        this.gpuImage = new GPUImage(activity);
        this.currentHolder = null;
    }


    @NonNull
    @Override
    public mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_row_view, parent, false);
        return new mViewHolder(view);
    }

   /* @Override
   /* public void onBindViewHolder(@NonNull mViewHolder holder, int position) {
        Pair<String, FilterType> filterPair = mPairList.get(position);
        Bitmap bitmap=null;
        if (bitmapThumbnail==null) bitmap= getBitmapFromAsset(holder.itemView.getContext(), filterPair.first);
        else{

        }
        holder.mImageFilterView.setImageBitmap(bitmap);
        holder.mTxtFilterName.setText(filterPair.second.name().replace("_", " "));
    }*/

    @Override
    public void onBindViewHolder(@NonNull mViewHolder holder, int position) {
        String currentFilterName = nameMap.get(listFilters.get(position).first);
        holder.mTxtFilterName.setText(currentFilterName);
        if (thumbnail[position] == null) {
            GPUImageFilter gpuImageFilter = GPUImageFilterTools.createFilterForType(activity, listFilters.get(position).second);
            if (gpuImageFilter != null) {
                gpuImage.setImage(bitmapThumbnail);
                gpuImage.setFilter(gpuImageFilter);
                thumbnail[position] = gpuImage != null ? gpuImage.getBitmapWithFilterApplied() : null;
            }
        }
        if (thumbnail[position] != null)
            holder.mImageFilterView.setImageBitmap(thumbnail[position]);
        else holder.mImageFilterView.setImageBitmap(bitmapThumbnail);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.isChecked = !holder.isChecked;
                if (holder.isChecked) holder.mTxtFilterName.setTextColor(Color.RED);
                else holder.mTxtFilterName.setTextColor(Color.WHITE);
                //  itemView.setSelected(isChecked);
                mFilterListener.onFilterChecked(listFilters.get(position).first);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listFilters.size();
    }


    public class mViewHolder extends RecyclerView.ViewHolder {
        boolean isChecked = false;
        ImageView mImageFilterView;
        TextView mTxtFilterName;

        public void clear() {
            mTxtFilterName.setTextColor(Color.WHITE);
        }

        public void checked() {
            mTxtFilterName.setTextColor(Color.RED);
        }

        mViewHolder(View itemView) {
            super(itemView);
            mImageFilterView = itemView.findViewById(R.id.imgFilterView);
            mTxtFilterName = itemView.findViewById(R.id.txtFilterName);
        }
    }

    /*
     holder.itemView.setOnClickListener(v -> {
        mFilterListener.onFilterChecked(listFilters.get(position).first);
        changeTextColor(holder);
    });
        if ((position == 0) && (currentHolder == null)) { // onFirstTime(that is currentHolder = null), the DEFAULT filter(NO_FILTER) is selected, so it should have the green text
        changeTextColor(holder);
    }
    */

    private Bitmap getBitmapFromAsset(Context context, String strName) {
        AssetManager assetManager = context.getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
            return BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    private void setupFilters() {
        mPairList.add(new Pair<>("filters/original.jpg", FilterType.DEFAULT));
        mPairList.add(new Pair<>("filters/auto_fix.png", FilterType.BRIGHTNESS));
        mPairList.add(new Pair<>("filters/brightness.png", FilterType.CONTRAST));
        mPairList.add(new Pair<>("filters/documentary.png", FilterType.EXPOSURE));
        mPairList.add(new Pair<>("filters/dual_tone.png", FilterType.FILTER_GROUP_SAMPLE));
        mPairList.add(new Pair<>("filters/fill_light.png", FilterType.GRAY_SCALE));
        mPairList.add(new Pair<>("filters/fish_eye.png", FilterType.MONOCHROME));
        mPairList.add(new Pair<>("filters/grain.png", FilterType.OPACITY));
        mPairList.add(new Pair<>("filters/gray_scale.png", FilterType.LUMINANCE));
        mPairList.add(new Pair<>("filters/negative.png", FilterType.PIXELATION));
        mPairList.add(new Pair<>("filters/posterize.png", FilterType.RGB));
        mPairList.add(new Pair<>("filters/saturate.png", FilterType.SATURATION));
        mPairList.add(new Pair<>("filters/sepia.png", FilterType.SEPIA));
        mPairList.add(new Pair<>("filters/sharpen.png", FilterType.TONE_CURVE_SAMPLE));
        mPairList.add(new Pair<>("filters/temprature.png", FilterType.TONE));
        mPairList.add(new Pair<>("filters/vignette.png", FilterType.VIGNETTE));
        mPairList.add(new Pair<>("filters/cross_process.png", FilterType.WHITE_BALANCE));
    }*/

    private Map<FilterType, String> nameMap = new TreeMap<>();

    private void initNameMap() {
        if (nameMap == null) nameMap = new TreeMap<>();
        nameMap.put(FilterType.DEFAULT, "Default");
        nameMap.put(FilterType.EXPOSURE, "Exposure");
        nameMap.put(FilterType.BRIGHTNESS, "Brightness");
        nameMap.put(FilterType.CONTRAST, "Contrast");
        nameMap.put(FilterType.SHARP, "Sharp");
        nameMap.put(FilterType.SATURATION, "Saturation");
        nameMap.put(FilterType.HUE, "Hue");
        nameMap.put(FilterType.GAMMA, "Gamma");
        nameMap.put(FilterType.HIGHLIGHT_SHADOW, "Highlight shadow");
        nameMap.put(FilterType.HAZE, "Haze");
        nameMap.put(FilterType.SEPIA, "Sepia");
        nameMap.put(FilterType.TONE, "Tone");
        nameMap.put(FilterType.VIBRANCE, "Vibrance");
        nameMap.put(FilterType.WHITE_BALANCE, "White balance");
        nameMap.put(FilterType.HALFTONE, "Halftone");
        nameMap.put(FilterType.OPACITY, "Opacity");
        nameMap.put(FilterType.MONOCHROME, "Monochrome");
        nameMap.put(FilterType.GRAY_SCALE, "Gray scale");
        nameMap.put(FilterType.PIXELATION, "Pixelation");
        nameMap.put(FilterType.POSTERIZE, "Posterize");
        nameMap.put(FilterType.SOLARIZE, "Solarize");
        nameMap.put(FilterType.VIGNETTE, "Vignette");
        nameMap.put(FilterType.LUMINANCE, "Luminance");
        nameMap.put(FilterType.LUMINANCE_THRESHOLD, "Luminance threshold");
        nameMap.put(FilterType.THRESHOLD_EDGE_DETECTION, "Threshold_edge");
        nameMap.put(FilterType.SKETCH, "Sketch");
        nameMap.put(FilterType.CROSSHATCH, "Cross hatch");
        nameMap.put(FilterType.WEAK_PIXEL, "Weak pixel");
        nameMap.put(FilterType.INVERT, "Invert");
        nameMap.put(FilterType.GAUSSIAN_FILTER, "Gaussian");
        nameMap.put(FilterType.BILATERAL_BLUR, "Bilateral blur");
        nameMap.put(FilterType.BOX_BLUR, "Box blur");
        nameMap.put(FilterType.ZOOM_BLUR, "Zoom blur");
        nameMap.put(FilterType.BULGE_DISTORTION, "Bluge distortion");
        nameMap.put(FilterType.CGA_COLORSPACE, "CGA Colorspace");
        nameMap.put(FilterType.RGB, "RGB");
        nameMap.put(FilterType.LOOK_UP_TABLE_SAMPLE, "Look up");
        nameMap.put(FilterType.OVERLAY, "Overlay");
        nameMap.put(FilterType.SPHERE_REFRACTION, "Sphere");
        nameMap.put(FilterType.TONE_CURVE_SAMPLE, "Tone curve");
        nameMap.put(FilterType.WATERMARK, "Watermark");
        nameMap.put(FilterType.FILTER_GROUP_SAMPLE, "Filter group");
        nameMap.put(FilterType.BITMAP_OVERLAY_SAMPLE, "Bitmap overlay");
    }
}
