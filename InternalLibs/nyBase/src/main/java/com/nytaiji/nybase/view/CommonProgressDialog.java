package com.nytaiji.nybase.view;


import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nytaiji.nybase.R;


/**
 * Created by xyoye on 2020/3/12.
 */

public class CommonProgressDialog extends Dialog {
    private CircleProgressView progressView;
    private TextView tipsTv;

    public CommonProgressDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_common_progerss);
        progressView = findViewById(R.id.progress_view);
        progressView.updateProgress(0);
        tipsTv = findViewById(R.id.tips_tv);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    public void updateTips(String tips) {
        tipsTv.setText(tips);
    }

    public void updateProgress(int progress) {
        progressView.updateProgress(progress);
    }
}
