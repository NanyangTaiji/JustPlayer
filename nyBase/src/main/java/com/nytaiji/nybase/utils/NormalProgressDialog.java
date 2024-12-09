package com.nytaiji.nybase.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

/**
 * @author LLhon
 * @Date 2017/6/29 17:48
 * @description 封装ProgressDialog
 */

public class NormalProgressDialog extends ProgressDialog implements DialogInterface.OnCancelListener {

    private final WeakReference<Context> mContextWeakReference;
    private volatile static NormalProgressDialog sDialog;

    public NormalProgressDialog(Context context) {
        this(context, DialogInterface.BUTTON_POSITIVE);
    }

    public NormalProgressDialog(Context context, int theme) {
        super(context, theme);

        mContextWeakReference = new WeakReference<Context>(context);
        setOnCancelListener(this);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Context context = mContextWeakReference.get();
        if (context != null) {
            //取消网络请求...

        }
    }

    public static synchronized void showLoading(Context context) {
        showLoading(context, "loading...");
    }

    public static synchronized void showLoading(Context context, CharSequence message) {
        showLoading(context, message, true);
    }

    public static synchronized void showLoading(Context context, CharSequence message, boolean cancelable) {
        try {
            if (sDialog != null && sDialog.isShowing()&& !((AppCompatActivity) context).isFinishing()) {
                sDialog.dismiss();
            }
            sDialog = new NormalProgressDialog(context);
            sDialog.setMessage(message);
            sDialog.setCancelable(cancelable);
            sDialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);

            if (sDialog != null && !sDialog.isShowing() && context != null && !((AppCompatActivity) context).isFinishing()) {
                sDialog.show();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void stopLoading() {
        try {
            if (sDialog != null && sDialog.isShowing()) {
                sDialog.dismiss();
            }
            sDialog = null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static  void stopLoading(AppCompatActivity activity) {
        if (activity!=null&&!activity.isFinishing()){
            stopLoading();
        }
    }
}
