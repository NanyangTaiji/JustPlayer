
package com.nytaiji.core.subtitle;

import com.nytaiji.core.subtitle.model.Subtitle;
import com.nytaiji.core.subtitle.runtime.AppTaskExecutor;

/**
 * @author AveryZhong.
 */

public class UIRenderTask implements Runnable {

    private Subtitle mSubtitle;
    private final SubtitleEngine.OnSubtitleChangeListener mOnSubtitleChangeListener;

    public UIRenderTask(final SubtitleEngine.OnSubtitleChangeListener l) {
        mOnSubtitleChangeListener = l;
    }

    @Override
    public void run() {
        if (mOnSubtitleChangeListener != null) {
            mOnSubtitleChangeListener.onSubtitleChanged(mSubtitle);
        }
    }

    public void execute(final Subtitle subtitle) {
        mSubtitle = subtitle;
        AppTaskExecutor.mainThread().execute(this);
    }
}
