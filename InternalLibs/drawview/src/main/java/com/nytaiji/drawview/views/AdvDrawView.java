package com.nytaiji.drawview.views;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.nytaiji.nybase.view.ColorPaletteView;
import com.nytaiji.drawview.R;
import com.nytaiji.drawview.dialogs.DrawAttribsDialog;
import com.nytaiji.drawview.enums.DrawingCapture;
import com.nytaiji.drawview.enums.DrawingMode;
import com.nytaiji.drawview.enums.DrawingTool;
import com.nytaiji.drawview.utils.AnimateUtils;
import com.nytaiji.drawview.utils.SerializablePaint;



/**
 * Created by IngMedina on 29/04/2017.
 */

public class AdvDrawView extends DrawView implements View.OnClickListener {
    private static final String TAG = "AdvDrawView";
    private DrawView mDrawView;
    private Context context;
    private static final int REQUEST_CODE_IMAGE = 10080;
    private static final int REQUEST_CODE_FILE = 10081;
    //region CONSTANTS
    private final int STORAGE_PERMISSIONS = 1000;
    private final int STORAGE_PERMISSIONS2 = 2000;
    private ImageView clear, lock, undo, redo;
    private CardView mLoadingBackground;
    private View view;

    public DrawView getPaint() {
        return mDrawView;
    }

    //region CONSTRUCTORS
    public AdvDrawView(@NonNull Context context) {
        this(context, null);
    }

    public AdvDrawView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvDrawView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.adv_drawing_view, this);
        mDrawView = view.findViewById(R.id.draw_view);

        mDrawView.setHistorySwitch(true);
        mDrawView.setZoomEnabled(false);

        clear = view.findViewById(R.id.action_clear);
        lock = view.findViewById(R.id.action_lock);
        undo = view.findViewById(R.id.action_undo);
        redo = view.findViewById(R.id.action_redo);
        setListeners();
        initOnClickListener();
        ColorPaletteView colorPicker = findViewById(R.id.cpv_color);
        colorPicker.setOnColorChangeListener(color -> {
            mDrawView.setDrawColor(Color.parseColor(color));
        });
    }

    private void initOnClickListener() {
        int[] viewIds = new int[]{
                R.id.action_clear,
                R.id.action_lock,
                R.id.action_eraser,
                R.id.action_undo,
                R.id.action_redo,
                R.id.action_draw_attrs,
                R.id.draw_text,
                R.id.draw_free,
                R.id.draw_straight,
                R.id.draw_arrow,
                R.id.draw_circle,
                R.id.draw_oval,
                //    R.id.draw_triangle,
                R.id.draw_rectangle,
                R.id.draw_shape,
                // R.id.iv_image,
                // R.id.iv_camera
        };

        for (int viewId : viewIds) {
            // Log.e(TAG, "viewID  " + viewId);
            view.findViewById(viewId).setOnClickListener(this);
        }

    }

    private int pickCode;

  /*  private void chooseBackgroundImage() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.choose_background_title)
                .items(R.array.image_source)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {
                            pickCode = REQUEST_CODE_FILE;
                            chooseBackgroundImageFile(pickCode);
                        }
                        if (which == 1) {
                            pickCode = REQUEST_CODE_IMAGE;
                            chooseBackgroundImageFile(pickCode);
                        } else if (which == 2) {
                            chooseBackgroundImageURL();
                        }
                        return true;
                    }
                })
                .positiveText(android.R.string.ok)
                .show();
    }

    //tobe overrided
  /*  public void chooseBackgroundImageFile(int pickCode) {

    }

    public void chooseBackgroundImageURL() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.choose_background_title)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)
                .input(R.string.choose_background_url, R.string.choose_background_url_default,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                mDrawView.setBackgroundImage(
                                        input.toString(),
                                        BackgroundType.URL,
                                        BackgroundScale.CENTER_INSIDE);
                            }
                        }).show();
    }*/


    public void onClick(View view) {
        mDrawView.setDrawingMode(DrawingMode.DRAW);
        int id = view.getId();
        if (id == R.id.action_clear) {
            clearDraw();
        } else if (id == R.id.action_lock) {
            // mDrawView.setHistorySwitch(true);
            //   mDrawView.setZoomEnabled(!mDrawView.isZoomEnabled());
            mDrawView.setHistorySwitch(!mDrawView.getHistorySwitch());
        } else if (id == R.id.action_save) {
            createCapture(drawingCaptureFormat);
        } else if (id == R.id.action_eraser) {
            mDrawView.setDrawingMode(DrawingMode.ERASER);
        } else if (id == R.id.action_undo) {
            mDrawView.undo();
            canUndoRedo();
        } else if (id == R.id.action_redo) {
            mDrawView.redo();
            canUndoRedo();
        } else if (id == R.id.action_draw_attrs) {
            changeDrawAttributes();
        } else if (id == R.id.draw_text) {
            // mDrawView.mode(DrawingMode.TEXT);
            //requestText();
        } else if (id == R.id.draw_free) {
            mDrawView.setDrawingTool(DrawingTool.values()[0]);
        } else if (id == R.id.draw_straight) {
            mDrawView.setDrawingTool(DrawingTool.values()[1]);
        } else if (id == R.id.draw_arrow) {
            mDrawView.setDrawingTool(DrawingTool.values()[2]);
        } else if (id == R.id.draw_circle) {
            mDrawView.setDrawingTool(DrawingTool.values()[3]);
        } else if (id == R.id.draw_oval) {
            mDrawView.setDrawingTool(DrawingTool.values()[4]);
        } else if (id == R.id.draw_rectangle) {
            mDrawView.setDrawingTool(DrawingTool.values()[5]);
            //  } else if (id == R.id.draw_triangle) {
            //    mDrawView.tool(DrawingTool.values()[6]);
        } else if (id == R.id.draw_shape) {
            changeDrawShap();
                /*  case R.id.iv_image:
                break;
            case R.id.iv_camera:
                break;*/
        }
    }

    private void setListeners() {
        mDrawView.setOnDrawViewListener(new OnDrawViewListener() {
            @Override
            public void onStartDrawing() {
                canUndoRedo();
            }

            @Override
            public void onEndDrawing() {
                canUndoRedo();
                lock.setVisibility(VISIBLE);
                if (clear.getVisibility() == View.INVISIBLE) {
                    AnimateUtils.ScaleInAnimation(clear, 50, 300, new OvershootInterpolator(), true);
                    //  AnimateUtils.ScaleInAnimation(saveDraw, 50, 300, new OvershootInterpolator(), true);

                }
            }

            @Override
            public void onClearDrawing() {
                canUndoRedo();

                if (clear.getVisibility() == View.VISIBLE) {
                    lock.setVisibility(INVISIBLE);
                    AnimateUtils.ScaleOutAnimation(clear, 50, 300, new OvershootInterpolator(), true);
                    //  AnimateUtils.ScaleOutAnimation(saveDraw, 50, 300, new OvershootInterpolator(), true);
                }
            }

            @Override
            public void onRequestText() {
                /*requestText();*/
            }

            @Override
            public void onAllMovesPainted() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        canUndoRedo();
                        // if (!mDrawView.isDrawViewEmpty())
                        //   mFabClearDraw.setVisibility(View.VISIBLE);
                    }
                }, 300);
            }
        });
    }

    private DrawingCapture drawingCaptureFormat = DrawingCapture.BITMAP;

    public void saveCaptureFormat(DrawingCapture drawingCapture) {
        this.drawingCaptureFormat = drawingCapture;
    }

  /*  public Object[] saveDraw(DrawingCapture drawingCapture) {
        return mDrawView.createCapture(drawingCapture);
    }*/

    private void clearDraw() {
        mDrawView.clearHistory();
        //  mDrawView.restartDrawing();
    }

 /*   private void requestText() {
        new MaterialDialog.Builder(context)
                .title(R.string.request_text_title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(context.getString(R.string.request_text), "",
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                mDrawView.refreshLastText(input.toString());
                            }
                        }).show();
    }*/

    private void canUndoRedo() {
        if (!mDrawView.canUndo()) {
            undo.setEnabled(false);
            undo.setImageResource(R.drawable.ic_action_content_undo_disabled);
        } else {
            undo.setEnabled(true);
            undo.setImageResource(R.drawable.icons8_undo_48);
        }
        if (!mDrawView.canRedo()) {
            redo.setEnabled(false);
            redo.setImageResource(R.drawable.ic_action_content_redo_disabled);
        } else {
            redo.setEnabled(true);
            redo.setImageResource(R.drawable.icons8_redo_48);
        }
    }

    private void changeDrawAttributes() {
        DrawAttribsDialog drawAttribsDialog = DrawAttribsDialog.newInstance();
        drawAttribsDialog.setPaint(mDrawView.getCurrentPaint());
        drawAttribsDialog.setOnCustomViewDialogListener(new DrawAttribsDialog.OnCustomViewDialogListener() {
            @Override
            public void onRefreshPaint(SerializablePaint newPaint) {
                // mDrawView.setDrawColor(newPaint.getColor())
              /*  getPaint().setPaintStyle(newPaint.getStyle())
                        .setDither(newPaint.isDither())
                        .setPenWidth(newPaint.getPenWidth())
                        .setEraserWidth((int) newPaint.getEraserWidth())
                        .setDrawAlpha(newPaint.getAlpha())
                        .setAntiAlias(newPaint.isAntiAlias())
                        .setLineCap(newPaint.getStrokeCap())
                        .setFontFamily(newPaint.getTypeface())
                        .setFontSize((int)newPaint.getTextSize());
//                If you prefer, you can easily refresh new attributes using this method*/

                mDrawView.refreshAttributes(newPaint);
            }
        });
        drawAttribsDialog.show(getAppCompatActivity(context).getSupportFragmentManager(), "drawAttribs");
    }

    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static AppCompatActivity getAppCompatActivity(Context context) {
        if (context == null) return null;

        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextWrapper) {
            return getAppCompatActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }


    private void changeDrawShap() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final DrawingTool tool = DrawingTool.SHAPE;// DrawingTool.values()[which];
        final String[] items = new String[]{"3", "4", "5", "6", "7", "8", "9", "10"};
        builder.setTitle("Choose sides");

        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDrawView.tool(tool, Integer.parseInt(items[which]));
                dialog.dismiss();
            }
        });


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
