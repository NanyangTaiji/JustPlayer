package com.nytaiji.zoom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;


import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by xuliwen on 2019/10/12
 * <p>
 * 简介：与业务无关的缩放 ViewGroup，只能有一个直接子 View
 * <p>
 * 实现过程：结合自身业务需要，参考了 LargeImageView、ScrollView 来实现
 * <p>
 * 作用：
 * 1、单指、多指滑动及惯性滑动
 * 2、双击缩放
 * 3、多指缩放
 * <p>
 * 注意点：
 * 1、如果子 View 宽、高小于 ZoomLayout，会将子 View 在宽、高方向上居中
 */

//If you return false than the touch event will be passed to
// the next View further up in the view hierarchy and you will receive no follow up calls.
public class DoubleTapZoom extends LinearLayout {


    private static final String TAG = "ZoomLayout";
    private static final float DEFAULT_MIN_ZOOM = 1.0f;
    private static final float DEFAULT_MAX_ZOOM = 4.0f;
    private static final float DEFAULT_DOUBLE_CLICK_ZOOM = 2.0f;
    private static final boolean DOUBLE_ZOOM = true;

    private float mDoubleClickZoom;
    private float mMinZoom;
    private float mMaxZoom;
    private float mCurrentZoom = 1;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private boolean mScrollBegin; // 是否已经开始滑动

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private OverScroller mOverScroller;
    private ScaleHelper mScaleHelper;
    private AccelerateInterpolator mAccelerateInterpolator;
    private DecelerateInterpolator mDecelerateInterpolator;
    private ZoomLayoutGestureListener mZoomLayoutGestureListener;
    private int mLastChildHeight;
    private int mLastChildWidth;
    private int mLastHeight;
    private int mLastWidth;
    private int mLastCenterX;
    private int mLastCenterY;
    private boolean mNeedReScale;

    //SetEnabled(false) first and initiate with TouchEvent's event.getPointerCount()>=2
    //ended with double tap;

    public DoubleTapZoom(Context context) {
        super(context);
        init(context, null);
    }

    public DoubleTapZoom(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DoubleTapZoom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //TODO
        if (!isEnabled()) return false;
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            // 最后一根手指抬起的时候，重置 mScrollBegin 为 false
            mScrollBegin = false;
        }
        mGestureDetector.onTouchEvent(ev);
        mScaleDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }


    private void init(Context context, @Nullable AttributeSet attrs) {
        if (!isEnabled()) return;

        mScaleDetector = new ScaleGestureDetector(context, mSimpleOnScaleGestureListener);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
        mOverScroller = new OverScroller(getContext());
        mScaleHelper = new ScaleHelper();
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        setWillNotDraw(false);
        if (attrs != null) {
            TypedArray array = null;
            try {
                array = context.obtainStyledAttributes(attrs, R.styleable.ZoomLayout);
                mMinZoom = array.getFloat(R.styleable.ZoomLayout_min_zoom, DEFAULT_MIN_ZOOM);
                mMaxZoom = array.getFloat(R.styleable.ZoomLayout_max_zoom, DEFAULT_MAX_ZOOM);
                mDoubleClickZoom = array.getFloat(R.styleable.ZoomLayout_double_click_zoom, DEFAULT_DOUBLE_CLICK_ZOOM);
                //for the convineince of externally setting
                if (mDoubleClickZoom > mMaxZoom) {
                    mDoubleClickZoom = mMaxZoom;
                }
            } catch (Exception e) {
                Log.e(TAG, TAG, e);
            } finally {
                if (array != null) {
                    array.recycle();
                }
            }
        }
    }

    private final ScaleGestureDetector.SimpleOnScaleGestureListener mSimpleOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float newScale;
            newScale = mCurrentZoom * detector.getScaleFactor();
            if (newScale > mMaxZoom) {
                newScale = mMaxZoom;
            } else if (newScale < mMinZoom) {
                newScale = mMinZoom;
            }
            setScale(newScale, (int) detector.getFocusX(), (int) detector.getFocusY());
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (mZoomLayoutGestureListener != null) {
                mZoomLayoutGestureListener.onScaleGestureBegin();
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };

    private final GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {


        @Override
        public boolean onDown(MotionEvent e) {
            if (!mOverScroller.isFinished()) {
                mOverScroller.abortAnimation();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setEnabled(false);
            return false;
           /* float newScale;
            if (mCurrentZoom < 1) {
                newScale = 1;
            } else if (mCurrentZoom < mDoubleClickZoom) {
                newScale = mDoubleClickZoom;
            } else {
                newScale = 1;
            }
            smoothScale(newScale, (int) e.getX(), (int) e.getY());
            if (mZoomLayoutGestureListener != null) {
                mZoomLayoutGestureListener.onDoubleTap();
            }*/

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mScrollBegin) {
                mScrollBegin = true;
                if (mZoomLayoutGestureListener != null) {
                    mZoomLayoutGestureListener.onScrollBegin();
                }
            }
            processScroll((int) distanceX, (int) distanceY, getScrollRangeX(), getScrollRangeY());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };


    private boolean fling(int velocityX, int velocityY) {
        if (Math.abs(velocityX) < mMinimumVelocity) {
            velocityX = 0;
        }
        if (Math.abs(velocityY) < mMinimumVelocity) {
            velocityY = 0;
        }
        final int scrollY = getScrollY();
        final int scrollX = getScrollX();
        final boolean canFlingX = (scrollX > 0 || velocityX > 0) &&
                (scrollX < getScrollRangeX() || velocityX < 0);
        final boolean canFlingY = (scrollY > 0 || velocityY > 0) &&
                (scrollY < getScrollRangeY() || velocityY < 0);
        boolean canFling = canFlingY || canFlingX;
        if (canFling) {
            velocityX = Math.max(-mMaximumVelocity, Math.min(velocityX, mMaximumVelocity));
            velocityY = Math.max(-mMaximumVelocity, Math.min(velocityY, mMaximumVelocity));
            int height = getHeight() - getPaddingBottom() - getPaddingTop();
            int width = getWidth() - getPaddingRight() - getPaddingLeft();
            int bottom = getContentHeight();
            int right = getContentWidth();
            mOverScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 0, Math.max(0, right - width), 0,
                    Math.max(0, bottom - height), 0, 0);
            notifyInvalidate();
            return true;
        }
        return true;
    }

    public void smoothScale(float newScale, int centerX, int centerY) {
        if (mCurrentZoom > newScale) {
            if (mAccelerateInterpolator == null) {
                mAccelerateInterpolator = new AccelerateInterpolator();
            }
            mScaleHelper.startScale(mCurrentZoom, newScale, centerX, centerY, mAccelerateInterpolator);
        } else {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = new DecelerateInterpolator();
            }
            mScaleHelper.startScale(mCurrentZoom, newScale, centerX, centerY, mDecelerateInterpolator);
        }
        notifyInvalidate();
    }

    private void notifyInvalidate() {
        // 效果和 invalidate 一样，但是会使得动画更平滑
        ViewCompat.postInvalidateOnAnimation(this);
    }


    public void setScale(float scale, int centerX, int centerY) {
        mLastCenterX = centerX;
        mLastCenterY = centerY;
        float preScale = mCurrentZoom;
        mCurrentZoom = scale;
        int sX = getScrollX();
        int sY = getScrollY();
        int dx = (int) ((sX + centerX) * (scale / preScale - 1));
        int dy = (int) ((sY + centerY) * (scale / preScale - 1));
        if (getScrollRangeX() < 0) {
            child().setPivotX(child().getWidth() / 2);
            child().setTranslationX(0);
        } else {
            child().setPivotX(0);
            int willTranslateX = -(child().getLeft());
            child().setTranslationX(willTranslateX);
        }
        if (getScrollRangeY() < 0) {
            child().setPivotY(child().getHeight() / 2);
            child().setTranslationY(0);
        } else {
            int willTranslateY = -(child().getTop());
            child().setTranslationY(willTranslateY);
            child().setPivotY(0);
        }
        child().setScaleX(mCurrentZoom);
        child().setScaleY(mCurrentZoom);
        processScroll(dx, dy, getScrollRangeX(), getScrollRangeY());
        notifyInvalidate();
    }


    private void processScroll(int deltaX, int deltaY,
                               int scrollRangeX, int scrollRangeY) {
        int oldScrollX = getScrollX();
        int oldScrollY = getScrollY();
        int newScrollX = oldScrollX + deltaX;
        int newScrollY = oldScrollY + deltaY;
        final int left = 0;
        final int right = scrollRangeX;
        final int top = 0;
        final int bottom = scrollRangeY;

        if (newScrollX > right) {
            newScrollX = right;
        } else if (newScrollX < left) {
            newScrollX = left;
        }

        if (newScrollY > bottom) {
            newScrollY = bottom;
        } else if (newScrollY < top) {
            newScrollY = top;
        }
        if (newScrollX < 0) {
            newScrollX = 0;
        }
        if (newScrollY < 0) {
            newScrollY = 0;
        }
        //  Log.e(TAG, "newScrollX = " + newScrollX + " ,newScrollY = " + newScrollY);
        scrollTo(newScrollX, newScrollY);
    }


    private int getScrollRangeX() {
        final int contentWidth = getWidth() - getPaddingRight() - getPaddingLeft();
        return (getContentWidth() - contentWidth);
    }

    private int getContentWidth() {
        return (int) (child().getWidth() * mCurrentZoom);
    }

    private int getScrollRangeY() {
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        return getContentHeight() - contentHeight;
    }

    private int getContentHeight() {
        return (int) (child().getHeight() * mCurrentZoom);
    }

    private View child() {
        return getChildAt(0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mNeedReScale) {
            // 需要重新刷新，因为宽高已经发生变化
            setScale(mCurrentZoom, mLastCenterX, mLastCenterY);
            mNeedReScale = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        child().setClickable(true);
        if (child().getHeight() < getHeight() || child().getWidth() < getWidth()) {
            setGravity(Gravity.CENTER);
        } else {
            setGravity(Gravity.TOP);
        }
        if (mLastChildWidth != child().getWidth() || mLastChildHeight != child().getHeight() || mLastWidth != getWidth()
                || mLastHeight != getHeight()) {
            // 宽高变化后，记录需要重新刷新，放在下次 onLayout 处理，避免 View 的一些配置：比如 getTop() 没有初始化好
            // 下次放在 onLayout 处理的原因是 setGravity 会在 onLayout 确定完位置，这时候去 setScale 导致位置的变化就不会导致用户看到
            // 闪一下的问题
            mNeedReScale = true;
        }
        mLastChildWidth = child().getWidth();
        mLastChildHeight = child().getHeight();
        mLastWidth = child().getWidth();
        mLastHeight = getHeight();
        if (mNeedReScale) {
            notifyInvalidate();
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScaleHelper.computeScrollOffset()) {
            setScale(mScaleHelper.getCurScale(), mScaleHelper.getStartX(), mScaleHelper.getStartY());
        }
        if (mOverScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mOverScroller.getCurrX();
            int y = mOverScroller.getCurrY();
            if (oldX != x || oldY != y) {
                final int rangeY = getScrollRangeY();
                final int rangeX = getScrollRangeX();
                processScroll(x - oldX, y - oldY, rangeX, rangeY);
            }
            if (!mOverScroller.isFinished()) {
                notifyInvalidate();
            }
        }
    }


    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int usedTotal = getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin +
                heightUsed;
        final int childHeightMeasureSpec;
        if (lp.height == WRAP_CONTENT) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - usedTotal),
                    MeasureSpec.UNSPECIFIED);
        } else {
            childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                            + heightUsed, lp.height);
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }


    /**
     * 是否可以在水平方向上滚动
     * 举例: ViewPager 通过这个方法判断子 View 是否可以水平滚动，从而解决滑动冲突
     */
    @Override
    public boolean canScrollHorizontally(int direction) {
        if (direction > 0) {
            return getScrollX() < getScrollRangeX();
        } else {
            return getScrollX() > 0 && getScrollRangeX() > 0;
        }
    }

    /**
     * 是否可以在竖直方向上滚动
     * 举例: ViewPager 通过这个方法判断子 View 是否可以竖直滚动，从而解决滑动冲突
     */
    @Override
    public boolean canScrollVertically(int direction) {
        if (direction > 0) {
            return getScrollY() < getScrollRangeY();
        } else {
            return getScrollY() > 0 && getScrollRangeY() > 0;
        }
    }

    public void setZoomLayoutGestureListener(ZoomLayoutGestureListener zoomLayoutGestureListener) {
        mZoomLayoutGestureListener = zoomLayoutGestureListener;
    }

    public interface ZoomLayoutGestureListener {
        void onScrollBegin();

        void onScaleGestureBegin();

        void onDoubleTap();
    }
}