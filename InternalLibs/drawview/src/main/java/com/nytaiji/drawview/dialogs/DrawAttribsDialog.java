package com.nytaiji.drawview.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.DialogFragment;

import com.nytaiji.drawview.R;
import com.nytaiji.drawview.utils.SerializablePaint;

/**
 * Created by Ing. Oscar G. Medina Cruz on 07/11/2016.
 */

public class DrawAttribsDialog extends DialogFragment {
    // LISTENER
    private OnCustomViewDialogListener onCustomViewDialogListener;

    // VARS
    private SerializablePaint mPaint;

    public DrawAttribsDialog() {
    }

    public static DrawAttribsDialog newInstance() {
        return new DrawAttribsDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_draw_attribs, null);
        AppCompatSeekBar seekBarPenWidth = view.findViewById(R.id.seek_pen_width);
        final TextView textViewPenWidth = view.findViewById(R.id.tv_pen_width);
        AppCompatSeekBar seekBarOpacity = view.findViewById(R.id.seek_opacity);
        final TextView textViewOpacity = view.findViewById(R.id.tv_opacity);
        final AppCompatSeekBar seekBarEraserWidth = view.findViewById(R.id.seek_eraser_width);
        final TextView textViewEraserWidth = view.findViewById(R.id.tv_eraser_width);
        AppCompatCheckBox appCompatCheckBoxAntiAlias = view.findViewById(R.id.chb_anti_alias);
        AppCompatCheckBox appCompatCheckBoxDither = view.findViewById(R.id.chb_dither);
        AppCompatRadioButton appCompatRadioButtonFill = view.findViewById(R.id.rb_fill);
        AppCompatRadioButton appCompatRadioButtonFillStroke = view.findViewById(R.id.rb_fill_stroke);
        AppCompatRadioButton appCompatRadioButtonStroke = view.findViewById(R.id.rb_stroke);
        AppCompatRadioButton appCompatRadioButtonButt = view.findViewById(R.id.rb_butt);
        AppCompatRadioButton appCompatRadioButtonRound = view.findViewById(R.id.rb_round);
        AppCompatRadioButton appCompatRadioButtonSquare = view.findViewById(R.id.rb_square);

        seekBarPenWidth.setProgress( mPaint.getPenWidth());
        textViewPenWidth.setText(getContext().getResources().getString(R.string.stroke_width, mPaint.getPenWidth()));
        seekBarPenWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mPaint.setPenWidth(i);
                textViewPenWidth.setText(getContext().getResources().getString(R.string.stroke_width, i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarEraserWidth.setProgress( mPaint.getEraserWidth());
        textViewEraserWidth.setText(getContext().getResources().getString(R.string.eraser_width, mPaint.getEraserWidth()));
        seekBarEraserWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mPaint.setEraserWidth(i);
                textViewEraserWidth.setText(getContext().getResources().getString(R.string.eraser_width, i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarOpacity.setProgress((int) mPaint.getAlpha());
        textViewOpacity.setText(getContext().getResources().getString(R.string.opacity, (int) mPaint.getAlpha()));
        seekBarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mPaint.setAlpha(i);
                textViewOpacity.setText(getContext().getResources().getString(R.string.opacity, i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        appCompatCheckBoxAntiAlias.setChecked(mPaint.isAntiAlias());
        appCompatCheckBoxDither.setChecked(mPaint.isDither());
        appCompatRadioButtonFill.setChecked(mPaint.getStyle() == Paint.Style.FILL);
        appCompatRadioButtonFillStroke.setChecked(mPaint.getStyle() == Paint.Style.FILL_AND_STROKE);
        appCompatRadioButtonStroke.setChecked(mPaint.getStyle() == Paint.Style.STROKE);
        appCompatRadioButtonButt.setChecked(mPaint.getStrokeCap() == Paint.Cap.BUTT);
        appCompatRadioButtonRound.setChecked(mPaint.getStrokeCap() == Paint.Cap.ROUND);
        appCompatRadioButtonSquare.setChecked(mPaint.getStrokeCap() == Paint.Cap.SQUARE);


        appCompatCheckBoxAntiAlias.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setAntiAlias(b);
            }
        });

        appCompatCheckBoxDither.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setDither(b);
            }
        });

        appCompatRadioButtonFill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setStyle(Paint.Style.FILL);
            }
        });

        appCompatRadioButtonFillStroke.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            }
        });

        appCompatRadioButtonStroke.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setStyle(Paint.Style.STROKE);
            }
        });

        appCompatRadioButtonButt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setStrokeCap(Paint.Cap.BUTT);
            }
        });

        appCompatRadioButtonRound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setStrokeCap(Paint.Cap.ROUND);
            }
        });

        appCompatRadioButtonSquare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setStrokeCap(Paint.Cap.SQUARE);
            }
        });

    /*    appCompatRadioButtonDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setTypeface(Typeface.DEFAULT);
            }
        });

        appCompatRadioButtonMonospace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setTypeface(Typeface.MONOSPACE);
            }
        });

        appCompatRadioButtonSansSerif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setTypeface(Typeface.SANS_SERIF);
            }
        });

        appCompatRadioButtonSerif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mPaint.setTypeface(Typeface.SERIF);
            }
        });*/

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (onCustomViewDialogListener != null)
                            onCustomViewDialogListener.onRefreshPaint(mPaint);
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    // METHODS
    public void setPaint(SerializablePaint paint) {
        this.mPaint = paint;
    }

    // INTERFACE
    public void setOnCustomViewDialogListener(OnCustomViewDialogListener onCustomViewDialogListener) {
        this.onCustomViewDialogListener = onCustomViewDialogListener;
    }

    public interface OnCustomViewDialogListener {
        void onRefreshPaint(SerializablePaint newPaint);
    }


      /*    AppCompatRadioButton appCompatRadioButtonDefault = view.findViewById(R.id.rb_default);
        AppCompatRadioButton appCompatRadioButtonMonospace = view.findViewById(R.id.rb_monospace);
        AppCompatRadioButton appCompatRadioButtonSansSerif = view.findViewById(R.id.rb_sans_serif);
        AppCompatRadioButton appCompatRadioButtonSerif = view.findViewById(R.id.rb_serif);
    */

          /*  appCompatRadioButtonDefault.setChecked(mPaint.getTypeface() == Typeface.DEFAULT);
        appCompatRadioButtonMonospace.setChecked(mPaint.getTypeface() == Typeface.MONOSPACE);
        appCompatRadioButtonSansSerif.setChecked(mPaint.getTypeface() == Typeface.SANS_SERIF);
        appCompatRadioButtonSerif.setChecked(mPaint.getTypeface() == Typeface.SERIF);*/
}

