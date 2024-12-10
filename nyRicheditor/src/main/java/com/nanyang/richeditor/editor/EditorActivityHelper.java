package com.nanyang.richeditor.editor;

import static android.view.View.VISIBLE;

import static com.nanyang.richeditor.editor.EditorUtils.hideSoftInput;
import static com.nytaiji.nybase.utils.SystemUtils.getClipboardItem;
//import static com.nytaiji.drawview.utils.ImageLoader.getClipboardItem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import com.nanyang.richeditor.R;
import com.nytaiji.nybase.view.ColorPaletteView;


import java.util.Arrays;
import java.util.List;

public class EditorActivityHelper {
    private ActionType mType;
    private ColorPaletteView colorPicker;
    private NyRichEditor mNyRichEditor;


    public ActionType getActionType() {
        return mType;
    }

    public void setColorPicker(ColorPaletteView colorPicker) {
        this.colorPicker = colorPicker;
    }


    public NyRichEditor setRichEditor(Context context, NyRichEditor nyRichEditor, LinearLayout richtextBarContainer, RichEditorCallback mRichEditorCallback) {
        //TODO
        mNyRichEditor = nyRichEditor;
        mNyRichEditor.initView(mRichEditorCallback);
        //TODO
        //--------------------------------------------------//
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,
                context.getResources().getDisplayMetrics());
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9,
                context.getResources().getDisplayMetrics());
        richtextBarContainer.removeAllViews();

        for (int i = 0, size = mActionTypeList.size(); i < size; i++) {
            final ActionImageView actionImageView = new ActionImageView(context);
            actionImageView.setLayoutParams(new LinearLayout.LayoutParams(width, width));
            actionImageView.setPadding(padding, padding, padding, padding);
            actionImageView.setActionType(mActionTypeList.get(i));
            actionImageView.setTag(mActionTypeList.get(i));
            actionImageView.setActivatedColor(R.color.colorAccent);
            actionImageView.setDeactivatedColor(R.color.tintColor);
            actionImageView.setRichEditorAction(mNyRichEditor);
            actionImageView.setFocusable(true);
            //  actionImageView.setFocusableInTouchMode(true);
            //      actionImageView.setBackgroundResource(R.drawable.btn_colored_material);
            actionImageView.setImageResource(mActionTypeIconList.get(i));
            int finalI = i;
            actionImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mType = mActionTypeList.get(finalI);
                    switch (mType) {
                        case UNDO:
                            mNyRichEditor.undo();
                            break;
                        case REDO:
                            mNyRichEditor.redo();
                        case FORE_COLOR:
                        case BACK_COLOR:
                            colorPicker.setVisibility(VISIBLE);
                            // hideSoftInput(context); //will disselect the word
                            break;
                        case LINK:
                            onClickInsertLink(context);
                            break;
                        case HEAD:
                            getFontSizeDialog(context, true);
                            break;
                        case SIZE:
                            getFontSizeDialog(context, false);
                            break;
                        case TABLE:
                            // mRichEditor.insertTable(rows, cols)
                            break;
                        default:
                            actionImageView.command();
                    }
                }
            });
            richtextBarContainer.addView(actionImageView);
        }
        return mNyRichEditor;
    }

    public void onClickInsertLink(Context context) {
        //TODO ny no effect for the next line
        // if (mNyRichEditor==null) Toast.makeText(context, "Insert a Richtext item first!", Toast.LENGTH_SHORT).show();
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_edit_hyperlink, null);
        EditText etAddress = rootView.findViewById(R.id.et_address);
        EditText etDisplayText = rootView.findViewById(R.id.et_display_text);
        CharSequence charSequence = getClipboardItem(context);
        if (charSequence != null) {
            etAddress.setText(charSequence.toString());
        }
        new android.app.AlertDialog.Builder(context)
                .setTitle(R.string.edit_hyperlink)
                .setView(rootView)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO to be makeup
                        if (!etDisplayText.getText().toString().isEmpty())
                            mNyRichEditor.insertLink(etAddress.getText().toString(), etDisplayText.getText().toString());
                        else
                            mNyRichEditor.insertLink(etAddress.getText().toString(), etAddress.getText().toString());
                        //TODO
                        dialog.dismiss();
                    }
                })
                .show();

        hideSoftInput((Activity) context);
    }


    private int sizeSelected = 2;

    private void getFontSizeDialog(Context context, boolean isHead) {
        String SAMPLEHTML = "<p><div style=\"text-align:center;\"><font size=\"7\">F7 </font><font size=\"6\"><b>H1</b><span style=\"font-weight: normal;\">F6</span></font>&nbsp;<font size=\"5\"><b>H2</b><span style=\"font-weight: normal;\">F5</span> <br> </font><font size=\"4\"><b>H3</b><span style=\"font-weight: normal;\">F4</span> </font><font size=\"3\"><b>H4</b><span style=\"font-weight: normal;\">F3</span> </font><font size=\"2\"><b>H5</b><span style=\"font-weight: normal;\">F2</span> </font><font size=\"1\"><b>H6</b><span style=\"font-weight: normal;\">F1</span></font></p>";
        final View root = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_font, null);
        NyRichEditor sample = root.findViewById(R.id.sample);
        sample.setHtml(SAMPLEHTML);
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(root)
                .setTitle("Size Selection")
                .setNegativeButton(R.string.cancel, null)
                .show();
        RadioGroup head = (RadioGroup) root.findViewById(R.id.header_group);
        RadioGroup font = (RadioGroup) root.findViewById(R.id.font_group);
        if (isHead) head.setVisibility(VISIBLE);
        else font.setVisibility(VISIBLE);

        head.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.h1) {
                    mNyRichEditor.setHeading(1);
                } else if (checkedId == R.id.h2) {
                    mNyRichEditor.setHeading(2);
                } else if (checkedId == R.id.h3) {
                    mNyRichEditor.setHeading(3);
                } else if (checkedId == R.id.h4) {
                    mNyRichEditor.setHeading(4);
                } else if (checkedId == R.id.h5) {
                    mNyRichEditor.setHeading(5);
                } else if (checkedId == R.id.h6) {
                    mNyRichEditor.setHeading(6);
                }
                alertDialog.dismiss();
            }
        });

      /*  if (sizeSelected == 1) {
            font.check(R.id.f1);
        } else if (sizeSelected == 2) {
            font.check(R.id.f2);
        } else if (sizeSelected == 3) {
            font.check(R.id.f3);
        } else if (sizeSelected == 4) {
            font.check(R.id.f4);
        } else if (sizeSelected == 5) {
            font.check(R.id.f5);
        } else if (sizeSelected == 6) {
            font.check(R.id.f6);
        } else font.check(R.id.f7);*/


        font.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.f1) {
                    sizeSelected = 1;
                } else if (checkedId == R.id.f2) {
                    sizeSelected = 2;
                } else if (checkedId == R.id.f3) {
                    sizeSelected = 3;
                } else if (checkedId == R.id.f4) {
                    sizeSelected = 4;
                } else if (checkedId == R.id.f5) {
                    sizeSelected = 5;
                } else if (checkedId == R.id.f6) {
                    sizeSelected = 6;
                } else if (checkedId == R.id.f7) {
                    sizeSelected = 7;
                }
                mNyRichEditor.setFontSize(sizeSelected);
                alertDialog.dismiss();
            }
        });
    }


    private final List<ActionType> mActionTypeList =
            Arrays.asList(
                    ActionType.LINE, ActionType.LINK,
                    ActionType.BOLD, ActionType.ITALIC, ActionType.UNDERLINE,
                    ActionType.FORE_COLOR, ActionType.BACK_COLOR, ActionType.SIZE, ActionType.HEAD,

                    ActionType.JUSTIFY_LEFT, ActionType.JUSTIFY_CENTER, ActionType.JUSTIFY_RIGHT,
                    ActionType.ORDERED, ActionType.UNORDERED,
                    ActionType.INDENT, ActionType.OUTDENT,
                    ActionType.SUBSCRIPT, ActionType.SUPERSCRIPT, ActionType.BLOCK_QUOTE
                    /* ActionType.STRIKETHROUGH,*/
                    /*ActionType.JUSTIFY_FULL,ActionType.NORMAL, */
                    /*ActionType.BLOCK_CODE, , ActionType.TABLE,*/
                    /* ActionType.CODE_VIEW*/
            );
    private final List<Integer> mActionTypeIconList =
            Arrays.asList(
                    R.drawable.ic_line, R.drawable.ic_insert_link,
                    R.drawable.ic_format_bold, R.drawable.ic_format_italic, R.drawable.ic_format_underlined,
                    R.drawable.icons8_color_dropper_48, R.drawable.icons8_text_color_48, R.drawable.icons8_lowercase_48, R.drawable.head,
                    //
                    R.drawable.ic_format_align_left, R.drawable.ic_format_align_center, R.drawable.ic_format_align_right,
                    R.drawable.ic_format_list_numbered, R.drawable.ic_format_list_bulleted,
                    R.drawable.ic_format_indent_decrease, R.drawable.ic_format_indent_increase,
                    R.drawable.ic_format_subscript, R.drawable.ic_format_superscript, R.drawable.ic_format_quote
                /*,    R.drawable.ic_format_para, R.drawable.ic_code_block,R.drawable.ic_format_align_justify,R.drawable.ic_table
                       R.drawable.ic_code_review*//* R.drawable.ic_format_strikethrough,*/
            );


    //
    //-----------------------unused----------------------
    /*
    private final RichEditorCallback.OnGetHtmlListener onGetHtmlListener = html -> {
        if (TextUtils.isEmpty(html)) {
            Toast.makeText(this, "Empty Html String", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, html, Toast.LENGTH_SHORT);
    };


    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    implementation 'io.reactivex.rxjava3:rxjava:3.1.0'
    import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
    import io.reactivex.rxjava3.core.Observable;
    import io.reactivex.rxjava3.core.ObservableOnSubscribe;
    import io.reactivex.rxjava3.core.Observer;
    import io.reactivex.rxjava3.disposables.Disposable;
    import io.reactivex.rxjava3.schedulers.Schedulers;
     */
/**
 * 异步方式插入图片
 *
 * @param imagePath 图片路径
 * <p>
 * 上传照片
 * <p>
 * 上传视频
 * <p>
 * 上传照片数据
 * <p>
 * 上传照片
 * <p>
 * 上传视频
 * <p>
 * 上传照片数据
 * <p>
 * 上传照片
 * <p>
 * 上传视频
 * <p>
 * 上传照片数据
 */
   /* private void insertImagesSync(final String imagePath) {
        insertDialog.show();
        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                //Log.i("NewActivity", "###imagePath="+imagePath);
                subscriber.onNext(imagePath);
                subscriber.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        insertDialog.dismiss();

                        Toast.makeText(RichEditorActivity.this, "图片插入成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        Toast.makeText(RichEditorActivity.this, "图片插入失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(String imagePath) {
                        editorContainer.insertImage(imagePath);
                    }
                });
    }

    /**
     * 异步方式插入视频
     *
     * @param videoPath 视频路径
     */
  /*  private void insertVideosSync(final String videoPath, final String firstImgUrl) {
        insertDialog.show();
        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                subscriber.onNext(videoPath);
                subscriber.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        Toast.makeText(RichEditorActivity.this, "视频插入失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        insertDialog.dismiss();

                        Toast.makeText(RichEditorActivity.this, "视频插入成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(String videoPath) {
                        editorContainer.insertVideo(videoPath, firstImgUrl);
                    }
                });
    }*/


/**
 * 上传照片
 */
  /*  public void uploadImage() {
        PictureSelecctDialog pictureSelecctDialog = new PictureSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case PictureSelecctDialog.FROM_ALBUM:
                    PictureSelector.create(this)
                            .openGallery(PictureMimeType.ofImage())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.MULTIPLE)
                            .forResult(REQUEST_CODE_CHOOSE_IMAGE);
                    break;
                case PictureSelecctDialog.TAKE_PICTURE:
                    PictureSelector.create(this)
                            .openCamera(PictureMimeType.ofImage())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .isPreviewImage(true)
                            .forResult(REQUEST_CODE_CHOOSE_IMAGE);
                    break;
                default:
                    break;
            }
        });
        pictureSelecctDialog.show();
    }*/

//----------------打开输入法的时候点击以外区域自动关闭------------------

  /*  @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                if (hideInputMethod(this, v)) {
                    return true; //隐藏键盘时，其他控件不响应点击事件==》注释则不拦截点击事件
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }*/



    /*public void uploadImageData() {
        PictureSelecctDialog pictureSelecctDialog = new PictureSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case PictureSelecctDialog.FROM_ALBUM:
                    PictureSelector.create(this)
                            .openGallery(PictureMimeType.ofImage())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.MULTIPLE)
                            .forResult(REQUEST_CODE_CHOOSE_IMAGE_DATA);
                    break;
                case PictureSelecctDialog.TAKE_PICTURE:
                    PictureSelector.create(this)
                            .openCamera(PictureMimeType.ofImage())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .isPreviewImage(true)
                            .forResult(REQUEST_CODE_CHOOSE_IMAGE_DATA);
                    break;
                default:
                    break;
            }
        });
        pictureSelecctDialog.show();
}*/


/**
 * 上传视频
 */
  /*  public void uploadVideo() {
        VideoSelecctDialog videoSelecctDialog = new VideoSelecctDialog(this, v -> {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case VideoSelecctDialog.FROM_ALBUM:
                    PictureSelector.create(this)
                            .openGallery(PictureMimeType.ofVideo())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.SINGLE)
                            .forResult(REQUEST_CODE_CHOOSE_VIDEO);
                    break;
                case VideoSelecctDialog.BY_CAMERA:
                    PictureSelector.create(this)
                            .openCamera(PictureMimeType.ofVideo())
                            .imageEngine(GlideEngine.createGlideEngine())
                            .selectionMode(PictureConfig.SINGLE)
                            .forResult(REQUEST_CODE_CHOOSE_VIDEO);
                    break;
                default:
                    break;
            }
        });
        videoSelecctDialog.show();
    }*/


  /*  public void onClickAction() {
        if (flAction.getVisibility() == VISIBLE) {
            flAction.setVisibility(View.GONE);
        } else {
            flAction.setVisibility(VISIBLE);
            if (isKeyboardShowing) {
                editorContainer.hideKeyBoard();
                //  KeyboardUtils.hideSoftInput(this);
            }

        }
    }*/


/**
 * 上传照片数据
 */

   /* public void uploadImageData() {
        requestCode = REQUEST_CODE_EDITOR_IMAGE_DATA;
        getMediaLinkDialog("image/*");
    }

    public void uploadImage() {
        requestCode = REQUEST_CODE_EDITOR_IMAGE;
        getMediaLinkDialog("image/*");
    }*/

//This is for the implementation in EditMenuFragment
  /*  public class MOnActionPerformListener implements OnActionPerformListener {

        @Override
        public void onActionPerform(ActionType type, Object... values) {
            Log.e(TAG, "ActionType type " + type);

            if (mRichEditor == null) {
                return;
            }

            String value = "";
            if (values != null && values.length > 0) {
                value = (String) values[0];
            }

            switch (type) {
                case SIZE:
                    //TODO
                    mRichEditor.fontSize(Integer.parseInt(value));
                    break;
                case LINE_HEIGHT:
                    //TODO
                    //  mRichEditor.lineHeight(Double.parseDouble(value));
                    break;
                case FORE_COLOR:
                    mRichEditor.foreColor(value);
                    break;
                case BACK_COLOR:
                    mRichEditor.backColor(value);
                    break;
                case FAMILY:
                    //TODO
                    //  mRichEditor.fontName(value);
                    break;
                case IMAGE:
                    uploadImageData();
                    //   onClickInsertImageData();
                    break;
                case LINK:
                    onClickInsertLink();
                    break;
                case TABLE:
                    onClickInsertTable();
                    break;
                default:
                    ActionImageView actionImageView = llActionBarContainer.findViewWithTag(type);
                    if (actionImageView != null) {
                        actionImageView.performClick();
                    }
                    break;

            }
        }
    }*/
  /*  private EditTableFragment tableFragment = null;

    private void onClickInsertTable() {
        //TODO
        KeyboardUtils.hideSoftInput(this);
        tableFragment = new EditTableFragment();
        tableFragment.setOnTableListener((rows, cols) -> mRichEditor.insertTable(rows, cols));
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_container, tableFragment, EditHyperlinkFragment.class.getName())
                .commit();
    }*/

         /*  KeyboardUtils.registerSoftInputChangedListener(this, height -> {
            isKeyboardShowing = height > 0;
            if (height > 0) {
                //  flAction.setVisibility(View.GONE);
                // ViewGroup.LayoutParams params = flAction.getLayoutParams();
                // params.height = height;
                // flAction.setLayoutParams(params);
            } else //if (flAction.getVisibility() != VISIBLE) {
                //  flAction.setVisibility(View.GONE);

            // }
        });*/
}
