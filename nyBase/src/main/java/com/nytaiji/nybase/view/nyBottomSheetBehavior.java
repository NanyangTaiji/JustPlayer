package com.nytaiji.nybase.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.nytaiji.nybase.R;

import java.util.Arrays;
import java.util.List;


public class nyBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {


    public nyBottomSheetBehavior() {}

    public nyBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    Rect globalRect = new Rect();
    private boolean skippingInterception = false;
    private final List<Integer> skipInterceptionOfElements = Arrays.asList(R.id.list_sub);
    private boolean mAllowUserDragging = true;
    public void setDragable(boolean allowUserDragging) {
        mAllowUserDragging = allowUserDragging;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {

        if (!mAllowUserDragging) {
            return false;
        }

        // Drop following when action ends
        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            skippingInterception = false;
        }

        // Found that user still swiping, continue following
        if (skippingInterception || getState() == BottomSheetBehavior.STATE_SETTLING) {
            return false;
        }

      //  setSkipCollapsed(event.getPointerCount() == 2);
        if (event.getPointerCount() == 2) {
            return super.onInterceptTouchEvent(parent, child, event);
        }


        // Don't need to do anything if bottomSheet isn't expanded
        if (getState() == BottomSheetBehavior.STATE_EXPANDED && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Without overriding scrolling will not work when user touches these elements
            for (final Integer element : skipInterceptionOfElements) {
                final View view = child.findViewById(element);
                if (view != null) {
                    final boolean visible = view.getGlobalVisibleRect(globalRect);
                    if (visible && globalRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        skippingInterception = true;
                        return false;
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(parent, child, event);

    }
}
