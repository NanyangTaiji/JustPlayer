package com.nanyang.richeditor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.nybase.filePicker.FilePickDialog;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class EditGestureActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextview;
    private Button mDone, mRefresh, mBack, mLeft, mRight, mNext;
    private Gesture mGesture;
    private GestureOverlayView mEditoverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gesture);
        initView();
        initGestureOverlayView();
    }

    private void initGestureOverlayView() {
        mEditoverlay.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
        mEditoverlay.setFadeOffset(1000);
        mEditoverlay.setGestureStrokeWidth(15);
        mEditoverlay.addOnGestureListener(new GestureOverlayView.OnGestureListener() {
            @Override
            public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {

            }

            @Override
            public void onGesture(GestureOverlayView overlay, MotionEvent event) {

            }

            @Override
            public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
                //获取修改后的手势
                mGesture = overlay.getGesture();
            }

            @Override
            public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {

            }
        });
    }

    private void initView() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.refresh).setOnClickListener(this);
        findViewById(R.id.done).setOnClickListener(this);
        findViewById(R.id.input).setOnClickListener(this);
        findViewById(R.id.goLeft).setOnClickListener(this);
        findViewById(R.id.goright).setOnClickListener(this);
        findViewById(R.id.goUp).setOnClickListener(this);
        findViewById(R.id.goDown).setOnClickListener(this);


        mEditoverlay = (GestureOverlayView) findViewById(R.id.editoverlay);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                if (mGesture != null)
                GestureManager.getInstance(getBaseContext()).changeRefreshGesture(mGesture);
                break;
            case R.id.back:
                if (mGesture != null)
                    GestureManager.getInstance(getBaseContext()).changeBackGesture(mGesture);
                break;
            case R.id.goLeft:
                if (mGesture != null)
                    GestureManager.getInstance(getBaseContext()).changeGoLeftGesture(mGesture);
                break;
            case R.id.goright:
                if (mGesture != null)
                    GestureManager.getInstance(getBaseContext()).changeGoRightGesture(mGesture);
                break;
            case R.id.goUp:
                if (mGesture != null)
                    GestureManager.getInstance(getBaseContext()).changeGoUpGesture(mGesture);
                break;
            case R.id.goDown:
                if (mGesture != null)
                    GestureManager.getInstance(getBaseContext()).changeGoDownGesture(mGesture);
                break;

            case R.id.input:
                restoreData();
                break;
            case R.id.done:

                finish();
                break;
        }

    }

    private void restoreData() {
        FilePickDialog.newInstance(
                R.string.restore,
                null,
                new FilePickDialog.ImportListener() {
                    @Override
                    public void onSelect(final String path) {
                        File source = new File(path);
                        File dest = new File(NyFileUtil.getAppDirectory(EditGestureActivity.this), "gestures");
                        if (source.exists() && source.length() > 0 && copyFile(source, dest))
                            GestureManager.getInstance(getBaseContext()).setGestureLib(dest);
                    }

                    @Override
                    public void onError(String msg) {
                        new AlertDialog.Builder(EditGestureActivity.this)
                                .setTitle(R.string.restore_error)
                                .setMessage(msg)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
        ).show(getSupportFragmentManager(), "");
    }

    private boolean copyFile(File source, File dest) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e("tag", "Failed to copy files:", e);
            return false;
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException ignored) {
            }

        }
        return true;
    }
}