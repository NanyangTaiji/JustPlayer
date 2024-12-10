package com.nytaiji.core.dialog;

import static com.nytaiji.nybase.utils.SystemUtils.dp2px;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.nytaiji.core.R;

public class VolumeDialog {

    protected Dialog dialog;
    protected TextView mVoulmeDialogTv;
    protected int getLayoutId() {
        return R.layout.video_volume;
    }
    public void show(Context context, int percent, View anchorView) {
        if (dialog == null) {
            View localView = LayoutInflater.from(context).inflate(getLayoutId(), null);
            if (localView.findViewById(R.id.app_video_volume) instanceof TextView) {
                mVoulmeDialogTv = (TextView) localView.findViewById(R.id.app_video_volume);
            }
            dialog = new Dialog(context, R.style.video_style_dialog_progress);
            dialog.setContentView(localView);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams localLayoutParams = dialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.CENTER;
            localLayoutParams.width = dp2px(context, 80);
            localLayoutParams.height = dp2px(context, 80);
            int location[] = new int[2];
            anchorView.getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            dialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
        String volumeTx = context.getString(R.string.percentage, percent);
        if (mVoulmeDialogTv != null)
            mVoulmeDialogTv.setText(volumeTx);
    }


    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}
