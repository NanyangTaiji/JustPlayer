package com.nytaiji.core.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import com.nytaiji.core.R;
import com.nytaiji.core.filter.extraFilters.GlBitmapOverlay;
import com.nytaiji.core.filter.extraFilters.GlWhiteBalanceFilter;
import com.nytaiji.epf.filter.GlBilateralFilter;
import com.nytaiji.epf.filter.GlBoxBlurFilter;
import com.nytaiji.epf.filter.GlBrightnessFilter;
import com.nytaiji.epf.filter.GlBulgeDistortionFilter;
import com.nytaiji.epf.filter.GlCGAColorspaceFilter;
import com.nytaiji.epf.filter.GlContrastFilter;
import com.nytaiji.epf.filter.GlCrosshatchFilter;
import com.nytaiji.epf.filter.GlExposureFilter;
import com.nytaiji.epf.filter.GlFilter;
import com.nytaiji.epf.filter.GlFilterGroup;
import com.nytaiji.epf.filter.GlGammaFilter;
import com.nytaiji.epf.filter.GlGaussianBlurFilter;
import com.nytaiji.epf.filter.GlGrayScaleFilter;
import com.nytaiji.epf.filter.GlHalftoneFilter;
import com.nytaiji.epf.filter.GlHazeFilter;
import com.nytaiji.epf.filter.GlHighlightShadowFilter;
import com.nytaiji.epf.filter.GlHueFilter;
import com.nytaiji.epf.filter.GlInvertFilter;
import com.nytaiji.epf.filter.GlLookUpTableFilter;
import com.nytaiji.epf.filter.GlLuminanceFilter;
import com.nytaiji.epf.filter.GlLuminanceThresholdFilter;
import com.nytaiji.epf.filter.GlMonochromeFilter;
import com.nytaiji.epf.filter.GlOpacityFilter;
import com.nytaiji.epf.filter.GlPixelationFilter;
import com.nytaiji.epf.filter.GlPosterizeFilter;
import com.nytaiji.epf.filter.GlRGBFilter;
import com.nytaiji.epf.filter.GlSaturationFilter;
import com.nytaiji.epf.filter.GlSepiaFilter;
import com.nytaiji.epf.filter.GlSharpenFilter;
import com.nytaiji.epf.filter.GlSketchEffect;
import com.nytaiji.epf.filter.GlSolarizeFilter;
import com.nytaiji.epf.filter.GlSphereRefractionFilter;
import com.nytaiji.epf.filter.GlSwirlFilter;
//import com.nytaiji.epf.filter.GlThresholdEdgeDetectionFilter;
import com.nytaiji.epf.filter.GlToneCurveFilter;
import com.nytaiji.epf.filter.GlToneFilter;
import com.nytaiji.epf.filter.GlVibranceFilter;
import com.nytaiji.epf.filter.GlVignetteFilter;
import com.nytaiji.epf.filter.GlWatermarkFilter;
import com.nytaiji.epf.filter.GlWeakPixelInclusionFilter;
import com.nytaiji.epf.filter.GlZoomBlurFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//copy from https://github.com/fahimfarhan/SimpleVideoEditor
//providing getFilterPairs()

public enum FilterType {
    DEFAULT,
    BILATERAL_BLUR,
    BOX_BLUR,
    BRIGHTNESS,
    BULGE_DISTORTION,
    CGA_COLORSPACE,
    CONTRAST,
    CROSSHATCH,
    EXPOSURE,
    FILTER_GROUP_SAMPLE,
    GAMMA,
    GAUSSIAN_FILTER,
    GRAY_SCALE,
    HALFTONE,
    HAZE,
    HIGHLIGHT_SHADOW,
    HUE,
    INVERT,
    LOOK_UP_TABLE_SAMPLE,
    LUMINANCE,
    LUMINANCE_THRESHOLD,
    THRESHOLD_EDGE_DETECTION,
    SKETCH,
    MONOCHROME,
    OPACITY,
    OVERLAY,
    PIXELATION,
    POSTERIZE,
    RGB,
    SATURATION,
    SEPIA,
    SHARP,
    SOLARIZE,
    SPHERE_REFRACTION,
    SWIRL,
    TONE_CURVE_SAMPLE,
    TONE,
    VIBRANCE,
    VIGNETTE,
    WATERMARK,
    WEAK_PIXEL,
    WHITE_BALANCE,
    ZOOM_BLUR,
    BITMAP_OVERLAY_SAMPLE;

    public static List<String> createFilterNameList()
    {
        List<String> filterNames = Arrays.asList(
                "DEFAULT",
                "EXPOSURE",
                "SATURATION",
                "SHARP",
                "BRIGHTNESS",
                "CONTRAST",
                "VIBRANCE",
                "GAMMA",
                "TONE",
                "HIGHLIGHT",
                "GAUSSIAN",
                "ZOOM_BLUR",
                "GRAY_SCALE",
                "BILA_BLUR",
                "BOX_BLUR",
                "DISTORTION",
                "CGA_COLOR",
                "CROSSHATCH",
                "HALFTONE",
                "HAZE",
                "HUE",
                "INVERT",
                "LOOK_UP_TABLE",
                "LUMINANCE",
                "THRESHOLD",
                "MONOCHROME",
                "OPACITY",
                "PIXELATION",
                "POSTERIZE",
                "RGB",
                "SEPIA",
                "SOLARIZE",
                "SPHERE",
                "SWIRL",
                "TONE_CURVE",
                "VIGNETTE",
                "WEAK_PIXEL",
                "WHITE_BALANCE"
        );
        return filterNames;

    }

    public static List<FilterType> createFilterList() {
        return Arrays.asList(FilterType.values());
    }

//    DEFAULT, BILATERAL_BLUR, BOX_BLUR, BRIGHTNESS, BULGE_DISTORTION, CGA_COLORSPACE, CONTRAST, CROSSHATCH, EXPOSURE,
//    FILTER_GROUP_SAMPLE, GAMMA, GAUSSIAN_FILTER, GRAY_SCALE, HALFTONE,
//    HAZE, HIGHLIGHT_SHADOW, HUE, INVERT, LUMINANCE, LUMINANCE_THRESHOLD, MONOCHROME, OPACITY, OVERLAY, PIXELATION,
//    POSTERIZE, RGB, SATURATION, SEPIA, SHARP, SOLARIZE, SPHERE_REFRACTION, SWIRL, TONE_CURVE_SAMPLE, VIBRANCE,
//    VIGNETTE, WEAK_PIXEL, WHITE_BALANCE, ZOOM_BLUR


    public static ArrayList<Pair<FilterType, GPUImageFilterTools.ImageFilterType> > getFilterPairs() {
        ArrayList<Pair<FilterType, GPUImageFilterTools.ImageFilterType> > arraylist = new ArrayList<>();
        arraylist.add(new Pair<>(DEFAULT, GPUImageFilterTools.ImageFilterType.DEFAULT));
        arraylist.add(new Pair<>(EXPOSURE,GPUImageFilterTools.ImageFilterType.EXPOSURE));
        arraylist.add(new Pair<>(SHARP,GPUImageFilterTools.ImageFilterType.SHARPEN));
        arraylist.add(new Pair<>(BRIGHTNESS,GPUImageFilterTools.ImageFilterType.BRIGHTNESS));
        arraylist.add(new Pair<>(CONTRAST,GPUImageFilterTools.ImageFilterType.CONTRAST));
        arraylist.add(new Pair<>(SATURATION,GPUImageFilterTools.ImageFilterType.SATURATION));
        arraylist.add(new Pair<>(GAMMA,GPUImageFilterTools.ImageFilterType.GAMMA));
        arraylist.add(new Pair<>(HUE,GPUImageFilterTools.ImageFilterType.HUE));
        arraylist.add(new Pair<>(WHITE_BALANCE,GPUImageFilterTools.ImageFilterType.WHITE_BALANCE));
        arraylist.add(new Pair<>(VIBRANCE,GPUImageFilterTools.ImageFilterType.VIBRANCE));
        arraylist.add(new Pair<>(HAZE,GPUImageFilterTools.ImageFilterType.HAZE));
        arraylist.add(new Pair<>(HIGHLIGHT_SHADOW,GPUImageFilterTools.ImageFilterType.HIGHLIGHT_SHADOW));
        arraylist.add(new Pair<>(BILATERAL_BLUR,GPUImageFilterTools.ImageFilterType.BILATERAL_BLUR));
        arraylist.add(new Pair<>(BOX_BLUR,GPUImageFilterTools.ImageFilterType.BOX_BLUR));
        arraylist.add(new Pair<>(BULGE_DISTORTION,GPUImageFilterTools.ImageFilterType.BULGE_DISTORTION));
        arraylist.add(new Pair<>(CGA_COLORSPACE,GPUImageFilterTools.ImageFilterType.CGA_COLORSPACE));
        arraylist.add(new Pair<>(CROSSHATCH,GPUImageFilterTools.ImageFilterType.CROSSHATCH));
        arraylist.add(new Pair<>(FILTER_GROUP_SAMPLE,GPUImageFilterTools.ImageFilterType.FILTER_GROUP));
        arraylist.add(new Pair<>(GAUSSIAN_FILTER,GPUImageFilterTools.ImageFilterType.GAUSSIAN_BLUR));
        arraylist.add(new Pair<>(GRAY_SCALE,GPUImageFilterTools.ImageFilterType.GRAYSCALE));
        arraylist.add(new Pair<>(HALFTONE,GPUImageFilterTools.ImageFilterType.HALFTONE));
        arraylist.add(new Pair<>(INVERT,GPUImageFilterTools.ImageFilterType.INVERT));
        arraylist.add(new Pair<>(LUMINANCE,GPUImageFilterTools.ImageFilterType.LUMINANCE));
        arraylist.add(new Pair<>(LUMINANCE_THRESHOLD,GPUImageFilterTools.ImageFilterType.LUMINANCE_THRESHSOLD));
        arraylist.add(new Pair<>(THRESHOLD_EDGE_DETECTION,GPUImageFilterTools.ImageFilterType.THRESHOLD_EDGE_DETECTION));
        arraylist.add(new Pair<>(SKETCH,GPUImageFilterTools.ImageFilterType.SKETCH));
        arraylist.add(new Pair<>(MONOCHROME,GPUImageFilterTools.ImageFilterType.MONOCHROME));
        arraylist.add(new Pair<>(OPACITY,GPUImageFilterTools.ImageFilterType.OPACITY));
        arraylist.add(new Pair<>(OVERLAY,GPUImageFilterTools.ImageFilterType.BLEND_OVERLAY));
        arraylist.add(new Pair<>(PIXELATION,GPUImageFilterTools.ImageFilterType.PIXELATION));
        arraylist.add(new Pair<>(POSTERIZE,GPUImageFilterTools.ImageFilterType.POSTERIZE));
        arraylist.add(new Pair<>(RGB,GPUImageFilterTools.ImageFilterType.RGB));
        arraylist.add(new Pair<>(SEPIA,GPUImageFilterTools.ImageFilterType.SEPIA));
        arraylist.add(new Pair<>(SOLARIZE,GPUImageFilterTools.ImageFilterType.SOLARIZE));
        arraylist.add(new Pair<>(SPHERE_REFRACTION,GPUImageFilterTools.ImageFilterType.SPHERE_REFRACTION));
        arraylist.add(new Pair<>(SWIRL,GPUImageFilterTools.ImageFilterType.SWIRL));
        arraylist.add(new Pair<>(TONE_CURVE_SAMPLE,GPUImageFilterTools.ImageFilterType.TONE_CURVE));
        arraylist.add(new Pair<>(VIGNETTE,GPUImageFilterTools.ImageFilterType.VIGNETTE));
        arraylist.add(new Pair<>(WEAK_PIXEL,GPUImageFilterTools.ImageFilterType.WEAK_PIXEL_INCLUSION));
        arraylist.add(new Pair<>(ZOOM_BLUR,GPUImageFilterTools.ImageFilterType.ZOOM_BLUR));
        return arraylist;
    }

    public static GlFilter createGlFilter(FilterType filterType, Context context) {
        switch (filterType) {
            case BILATERAL_BLUR:
                return new GlBilateralFilter();
            case BOX_BLUR:
                return new GlBoxBlurFilter();
            case BRIGHTNESS:
                GlBrightnessFilter glBrightnessFilter = new GlBrightnessFilter();
                glBrightnessFilter.setBrightness(0.2f);
                return glBrightnessFilter;
            case BULGE_DISTORTION:
                return new GlBulgeDistortionFilter();
            case CGA_COLORSPACE:
                return new GlCGAColorspaceFilter();
            case CONTRAST:
                GlContrastFilter glContrastFilter = new GlContrastFilter();
                glContrastFilter.setContrast(2.5f);
                return glContrastFilter;
            case CROSSHATCH:
                return new GlCrosshatchFilter();
            case EXPOSURE:
                return new GlExposureFilter();
            case FILTER_GROUP_SAMPLE:
                return new GlFilterGroup(new GlSepiaFilter(), new GlVignetteFilter());
            case GAMMA:
                GlGammaFilter glGammaFilter = new GlGammaFilter();
                glGammaFilter.setGamma(2f);
                return glGammaFilter;
            case GAUSSIAN_FILTER:
                return new GlGaussianBlurFilter();
            case GRAY_SCALE:
                return new GlGrayScaleFilter();
            case HALFTONE:
                return new GlHalftoneFilter();
            case HAZE:
                GlHazeFilter glHazeFilter = new GlHazeFilter();
                glHazeFilter.setSlope(-0.5f);
                return glHazeFilter;
            case HIGHLIGHT_SHADOW:
                return new GlHighlightShadowFilter();
            case HUE:
                return new GlHueFilter();
            case INVERT:
                return new GlInvertFilter();
            case LOOK_UP_TABLE_SAMPLE:
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), com.nytaiji.nybase.R.drawable.lookup_sample);
                return new GlLookUpTableFilter(bitmap);
            case LUMINANCE:
                return new GlLuminanceFilter();
            case LUMINANCE_THRESHOLD:
                return new GlLuminanceThresholdFilter();
          //  case THRESHOLD_EDGE_DETECTION:
               // return new GlThresholdEdgeDetectionFilter();
            case SKETCH:
                return new GlSketchEffect();
            case MONOCHROME:
                return new GlMonochromeFilter();
            case OPACITY:
                return new GlOpacityFilter();
            case PIXELATION:
                return new GlPixelationFilter();
            case POSTERIZE:
                return new GlPosterizeFilter();
            case RGB:
                GlRGBFilter glRGBFilter = new GlRGBFilter();
                glRGBFilter.setRed(0f);
                return glRGBFilter;
            case SATURATION:
                return new GlSaturationFilter();
            case SEPIA:
                return new GlSepiaFilter();
            case SHARP:
                GlSharpenFilter glSharpenFilter = new GlSharpenFilter();
                glSharpenFilter.setSharpness(4f);
                return glSharpenFilter;
            case SOLARIZE:
                return new GlSolarizeFilter();
            case SPHERE_REFRACTION:
                return new GlSphereRefractionFilter();
            case SWIRL:
                return new GlSwirlFilter();
            case TONE_CURVE_SAMPLE:
                try {
                    InputStream is = context.getAssets().open("tone_cuver_sample.acv");
                    return new GlToneCurveFilter(is);
                } catch (IOException e) {
                    Log.e("FilterType", "Error");
                }
                return new GlFilter();
            case TONE:
                return new GlToneFilter();
            case VIBRANCE:
                GlVibranceFilter glVibranceFilter = new GlVibranceFilter();
                glVibranceFilter.setVibrance(3f);
                return glVibranceFilter;
            case VIGNETTE:
                return new GlVignetteFilter();
            case WEAK_PIXEL:
                return new GlWeakPixelInclusionFilter();
            case WHITE_BALANCE:
                GlWhiteBalanceFilter glWhiteBalanceFilter = new GlWhiteBalanceFilter();
                glWhiteBalanceFilter.setTemperature(2400f);
                glWhiteBalanceFilter.setTint(2f);
                return glWhiteBalanceFilter;
            case ZOOM_BLUR:
                return new GlZoomBlurFilter();
            case WATERMARK:
                return new GlWatermarkFilter(BitmapFactory.decodeResource(context.getResources(), com.nytaiji.nybase.R.drawable.banner), GlWatermarkFilter.Position.RIGHT_BOTTOM);

            case BITMAP_OVERLAY_SAMPLE:
                return new GlBitmapOverlay(context, BitmapFactory.decodeResource(context.getResources(), com.nytaiji.nybase.R.drawable.banner), GlBitmapOverlay.Position.RIGHT_TOP);
            default:
                return new GlFilter();
        }
    }

    public static FilterAdjuster createFilterAdjuster(FilterType filterType) {
        switch (filterType) {
            case BILATERAL_BLUR:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlBilateralFilter) filter).setBlurSize(range(percentage, 0.0f, 1.0f));
                    }
                };
            case BOX_BLUR:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlBoxBlurFilter) filter).setBlurSize(range(percentage, 0.0f, 1.0f));
                    }
                };
            case BRIGHTNESS:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlBrightnessFilter) filter).setBrightness(range(percentage, -1.0f, 1.0f));
                    }
                };
            case CONTRAST:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlContrastFilter) filter).setContrast(range(percentage, 0.0f, 2.0f));
                    }
                };
            case CROSSHATCH:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlCrosshatchFilter) filter).setCrossHatchSpacing(range(percentage, 0.0f, 0.06f));
                        ((GlCrosshatchFilter) filter).setLineWidth(range(percentage, 0.0f, 0.006f));
                    }
                };
            case EXPOSURE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlExposureFilter) filter).setExposure(range(percentage, -2.0f, 4.0f));
                    }
                };
            case GAMMA:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlGammaFilter) filter).setGamma(range(percentage, 0.0f, 2.4f));
                    }
                };
            case HAZE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlHazeFilter) filter).setDistance(range(percentage, -0.3f, 0.3f));
                        ((GlHazeFilter) filter).setSlope(range(percentage, -0.3f, 0.3f));
                    }
                };
            case HIGHLIGHT_SHADOW:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlHighlightShadowFilter) filter).setShadows(range(percentage, 0.0f, 1.0f));
                        ((GlHighlightShadowFilter) filter).setHighlights(range(percentage, 0.0f, 1.0f));
                    }
                };
            case HUE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlHueFilter) filter).setHue(range(percentage, 0.0f, 360.0f));
                    }
                };
            case LUMINANCE_THRESHOLD:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlLuminanceThresholdFilter) filter).setThreshold(range(percentage, 0.0f, 1.0f));
                    }
                };
            case MONOCHROME:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlMonochromeFilter) filter).setIntensity(range(percentage, 0.0f, 1.0f));
                    }
                };
            case OPACITY:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlOpacityFilter) filter).setOpacity(range(percentage, 0.0f, 1.0f));
                    }
                };
            case PIXELATION:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlPixelationFilter) filter).setPixel(range(percentage, 1.0f, 100.0f));
                    }
                };
            case POSTERIZE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        // In theorie to 256, but only first 50 are interesting
                        ((GlPosterizeFilter) filter).setColorLevels((int) range(percentage, 1, 50));
                    }
                };
            case RGB:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlRGBFilter) filter).setRed(range(percentage, 0.0f, 1.0f));
                    }
                };
            case SATURATION:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlSaturationFilter) filter).setSaturation(range(percentage, 0.0f, 2.0f));
                    }
                };
            case SHARP:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlSharpenFilter) filter).setSharpness(range(percentage, -4.0f, 4.0f));
                    }
                };
            case SOLARIZE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlSolarizeFilter) filter).setThreshold(range(percentage, 0.0f, 1.0f));
                    }
                };
            case SWIRL:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlSwirlFilter) filter).setAngle(range(percentage, 0.0f, 2.0f));
                    }
                };
            case VIBRANCE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlVibranceFilter) filter).setVibrance(range(percentage, -1.2f, 1.2f));
                    }
                };
            case VIGNETTE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlVignetteFilter) filter).setVignetteStart(range(percentage, 0.0f, 1.0f));
                    }
                };
            case WHITE_BALANCE:
                return new FilterAdjuster() {
                    @Override
                    public void adjust(GlFilter filter, int percentage) {
                        ((GlWhiteBalanceFilter) filter).setTemperature(range(percentage, 2000.0f, 8000.0f));
                    }
                };
            default:
                return null;
        }
    }

    private static float range(int percentage, float start, float end) {
        return (end - start) * percentage / 100.0f + start;
    }
}
