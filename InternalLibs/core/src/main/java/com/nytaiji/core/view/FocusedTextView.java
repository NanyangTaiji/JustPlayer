package com.nytaiji.core.view;

import android.content.Context;
import android.util.AttributeSet;

public class FocusedTextView extends androidx.appcompat.widget.AppCompatTextView {

        public FocusedTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        /**
         * 重新此方法，是为了告诉系统，TextView可以获取到焦点啦
         * @return
         */
        @Override
        public boolean isFocused() {
            return true;
        }

}
