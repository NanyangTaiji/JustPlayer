package com.nytaiji.core.dialog;

import static com.nytaiji.nybase.utils.SystemUtils.dp2px;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nytaiji.core.R;
import com.nytaiji.nybase.utils.VideoProperty;

public class SeekDialog {

    protected Dialog dialog;
    protected Context context;
    protected View anchorView;
    protected ImageView mDialogIcon;
    protected Drawable mDialogProgressBarDrawable;
    protected ProgressBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected int mDialogProgressHighLightColor = -11;

    protected int mDialogProgressNormalColor = -11;

    public SeekDialog(@NonNull Context context, View anchorView) {
        this.context = context;
        this.anchorView = anchorView;
    }

    protected int getLayoutId() {
        return R.layout.video_seek_dialog;
    }

    public void show(float deltaX, long seekTimePosition, long totalTimeDuration) {
        if (dialog == null) {
            View localView = LayoutInflater.from(context).inflate(getLayoutId(), null);
            if (localView.findViewById(R.id.duration_progressbar) instanceof ProgressBar) {
                mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
                if (mDialogProgressBarDrawable != null) {
                    mDialogProgressBar.setProgressDrawable(mDialogProgressBarDrawable);
                }
            }
            if (localView.findViewById(R.id.tv_current) instanceof TextView) {
                mDialogSeekTime = ((TextView) localView.findViewById(R.id.tv_current));
            }
            if (localView.findViewById(R.id.tv_duration) instanceof TextView) {
                mDialogTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            }
            if (localView.findViewById(R.id.duration_image_tip) instanceof ImageView) {
                mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            }
            dialog = new Dialog(context, R.style.video_style_dialog_progress);
            dialog.setContentView(localView);
            dialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
            dialog.getWindow().addFlags(32);
            dialog.getWindow().addFlags(16);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (mDialogProgressNormalColor != -11 && mDialogTotalTime != null) {
                mDialogTotalTime.setTextColor(mDialogProgressNormalColor);
            }
            if (mDialogProgressHighLightColor != -11 && mDialogSeekTime != null) {
                mDialogSeekTime.setTextColor(mDialogProgressHighLightColor);
            }
            WindowManager.LayoutParams localLayoutParams = dialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.CENTER;
            localLayoutParams.width = dp2px(context, 100);
            localLayoutParams.height = dp2px(context, 100);
            int location[] = new int[2];
            anchorView.getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            dialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
        if (mDialogSeekTime != null) {
            mDialogSeekTime.setText(VideoProperty.stringForTime(seekTimePosition));
        }
        if (mDialogTotalTime != null) {
            mDialogTotalTime.setText(VideoProperty.stringForTime(totalTimeDuration));
        }
        if (totalTimeDuration > 0)
            if (mDialogProgressBar != null) {
                mDialogProgressBar.setProgress((int) (seekTimePosition * 100 / totalTimeDuration));
            }
        if (deltaX > 0) {
            if (mDialogIcon != null) {
                mDialogIcon.setBackgroundResource(com.nytaiji.nybase.R.drawable.video_forward_icon);
            }
        } else {
            if (mDialogIcon != null) {
                mDialogIcon.setBackgroundResource(com.nytaiji.nybase.R.drawable.video_backward_icon);
            }
        }

    }


    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}
