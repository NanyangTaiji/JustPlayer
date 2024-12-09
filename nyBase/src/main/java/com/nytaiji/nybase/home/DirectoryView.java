package com.nytaiji.nybase.home;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.nytaiji.nybase.R;


public class DirectoryView extends FrameLayout {
    private float mPosition = 0f;

    private int mWidth;

    public DirectoryView(Context context) {
        super(context);
    }

    public DirectoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        setPosition(mPosition);
    }

    public float getPosition() {
        return mPosition;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setPosition(float position) {
        mPosition = position;
        setX((mWidth > 0) ? (mPosition * mWidth) : 0);

      //  if(Utils.hasLollipop()){
            if (mPosition != 0) {
             //   setTranslationZ(8);
                setTranslationZ(getResources().getDimensionPixelSize(R.dimen.dir_elevation));
            } else {
                setTranslationZ(0);
            }
      //  }
    }
}
