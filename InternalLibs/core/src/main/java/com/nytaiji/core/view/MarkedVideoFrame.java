package com.nytaiji.core.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.core.R;
import com.nytaiji.nybase.utils.BitmapUtil;
import com.nytaiji.nybase.utils.NyFileUtil;


/**
 */

public class MarkedVideoFrame extends AdvancedVideoFrame implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private Button ButtonColor;
    private SeekBar penSizeSeekBar;
    private com.nytaiji.core.view.nyDrawingView nyDrawingView;
    private int penToggle = 3;  //start with yellow color
    private RelativeLayout marker;
    private String filename;
    private Context mContext;
    //TODO
    private final Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;


    public MarkedVideoFrame(@NonNull Context context) {
        this(context, null);
    }

    public MarkedVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkedVideoFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.marked_registered_video_layout;
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        initializeUI();
        initPaintMode();

    }

    private void initPaintMode() {
        nyDrawingView.initializePen();
        nyDrawingView.setPenSize(10);
        nyDrawingView.setPenColor(Color.YELLOW);
    }


    private void initializeUI() {
        marker = findViewById(R.id.marker);
        marker.setVisibility(INVISIBLE);

        nyDrawingView = findViewById(R.id.scratch_pad);

        Button saveImage = findViewById(R.id.save_image);
        saveImage.setOnClickListener(this);

        Button shareImage = findViewById(R.id.share_image);
        shareImage.setOnClickListener(this);

        Button saveMark = findViewById(R.id.save_mark);
        saveMark.setOnClickListener(this);

        Button undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(this);

        ButtonColor = findViewById(R.id.pen_color_button);
        ButtonColor.setOnClickListener(this);

        Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);

        Button MarkReleaseButton = findViewById(R.id.markRelease);
        MarkReleaseButton.setOnClickListener(this);

        penSizeSeekBar = findViewById(R.id.pen_size_seekbar);
        penSizeSeekBar.setOnSeekBarChangeListener(this);

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.save_image) {
            nyDrawingView.loadImage(getTextureView().getBitmap());
            nyDrawingView.ReDoAll();
            saveMarkedBmp(format);
        } else if (id == R.id.save_mark) {
            saveMarkedBmp(format);
        } else if (id == R.id.share_image) {
            nyDrawingView.loadImage(getTextureView().getBitmap());
            nyDrawingView.ReDoAll();
            saveMarkedBmp(format);
            if (format == Bitmap.CompressFormat.JPEG) filename = filename + ".jpg";
            else if (format == Bitmap.CompressFormat.PNG) filename = filename + ".png";
            else if (format == Bitmap.CompressFormat.WEBP) filename = filename + ".webp";
            NyFileUtil.shareMedia(mContext, filename, "image/*");
        } else if (id == R.id.undo_button) {
            nyDrawingView.undo();
            /*   } else if (id == R.id.pen_button) {
             */
        } else if (id == R.id.clear_button) {
            nyDrawingView.clearAll();
        } else if (id == R.id.exo_play) {
            pause();
            marker.setVisibility(VISIBLE);
        } else if (id == R.id.markRelease) {
            nyDrawingView.clearAll();
            marker.setVisibility(INVISIBLE);
            start();
        } else if (id == R.id.pen_color_button) {
            int colorId = NyFileUtil.getActivity(mContext).getResources().getColor(com.nytaiji.nybase.R.color.md_yellow_600);
            penToggle++;
            if (penToggle == 1) {
                colorId = NyFileUtil.getActivity(mContext).getResources().getColor(com.nytaiji.nybase.R.color.md_green_800);
            } else if (penToggle == 2) {
                // ButtonColor.setText("Yellow");
                colorId = NyFileUtil.getActivity(mContext).getResources().getColor(com.nytaiji.nybase.R.color.md_yellow_600);
            } else if (penToggle == 3) {
                //   ButtonColor.setText("White");
                colorId = NyFileUtil.getActivity(mContext).getResources().getColor(com.nytaiji.nybase.R.color.colorWhite);
            } else if (penToggle == 4) {
                colorId = NyFileUtil.getActivity(mContext).getResources().getColor(com.nytaiji.nybase.R.color.md_red_900);
            } else if (penToggle == 5) {
                //  ButtonColor.setText("Blue");
                colorId = NyFileUtil.getActivity(mContext).getResources().getColor(com.nytaiji.nybase.R.color.md_blue_500);
                penToggle = 0;
            }
            ButtonColor.setBackgroundColor(colorId);
            nyDrawingView.setPenColor(colorId);
            // penSizeSeekBar.setBackgroundColor(colorId);
        }
    }

    private void saveMarkedBmp(Bitmap.CompressFormat format) {
        String sPath = NyFileUtil.getImageDir();
        String sVideo = NyFileUtil.getFileNameWithoutExtFromPath(videoUrl);
        String sDuration = Long.toString(getCurrentPosition());
        filename = sPath + sVideo + "_" + sDuration;
        if (BitmapUtil.saveBitmap(filename, nyDrawingView.getImageBitmap(), format, 100)) {
            Toast.makeText(mContext, "Save Success " + filename, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int seekBarId = seekBar.getId();
        if (seekBarId == R.id.pen_size_seekbar) {
            nyDrawingView.setPenSize(i);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Intentionally Empty
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //Intentionally Empty
    }


}
