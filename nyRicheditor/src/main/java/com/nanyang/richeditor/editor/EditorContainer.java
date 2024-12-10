package com.nanyang.richeditor.editor;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hdl.calendardialog.CalendarView;
import com.hdl.calendardialog.CalendarViewDialog;
import com.nanyang.richeditor.R;
import com.nytaiji.nybase.utils.NyFormatter;
//import com.nytaiji.drawview.views.AdvDrawView;
import com.nytaiji.nybase.view.DragLinearLayout;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.nanyang.richeditor.editor.EditorUtils.hideSoftInput;
import static com.nytaiji.nybase.utils.SystemUtils.hideStatusBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class EditorContainer extends ScrollView implements TextWatcher {
    private static String TAG = "EditorContainer";
    public static final int EDIT_PADDING = 0; // edittext常规padding是10dp
    public static String COMMA = "@#";
    public static String sIndex = "1";
    // public static String DEFAULTHTML = "<p><div style=\"text-align:center;\"><b>(" + sIndex + ")</b></div><hr><div style=\"text-align:left;\"<br><br></p>";
    public static String DEFAULTHTML = "<p><hr><div style=\"text-align:left;\"<br><br><br></p>";
    public static String EMPTYTHTML = "<p><hr></p>";

    private int viewTagIndex = -1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    public DragLinearLayout dragLinearLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private final LayoutInflater inflater;
    private int noOfRichtext = -1;
    public NyRichEditor lastFocusEdit; // 最近被聚焦的EditText
    private final LinearLayout.LayoutParams editParam;
    private Context mContext;
    private FocusChangeCallback focusChangeCallback;
    private EditText eTitle, eDate, eRemark;
    private long dateTimeMills = 0L;
    public ArrayList<String> keywords = new ArrayList<>();
    public ArrayList<String> reference = new ArrayList<>();
    private LinearLayout keywordsHolder, referenceHolder;
    private RichEditor.OnTextChangeListener onTextChangeListener;
    private OnFocusChangeListener onFocusChangeListener; // 所有EditText的焦点监听listener
    private boolean movable = false;

    //-----------
    public void setFocusChangeCallback(FocusChangeCallback focusChangeCallback) {
        this.focusChangeCallback = focusChangeCallback;
    }

    public void resetIndex() {
        //Top has no tag
        noOfRichtext = -1;
        viewTagIndex = 0;
    }

    //-------------
    public EditorContainer(Context context) {
        this(context, null);
    }

    public EditorContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public EditorContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = LayoutInflater.from(context);
        this.mContext = context;

        // 1. 初始化allLayout
        dragLinearLayout = new DragLinearLayout(context);
        dragLinearLayout.setOrientation(LinearLayout.VERTICAL);
        dragLinearLayout.setContainerScrollView(this);
        //子控件拖拽监听
        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                if (buttonCallout != null)
                    buttonCallout.trigged(ITEMCHANGE, null);
                //移除FirstView
                dragLinearLayout.removeDragView(firstView);
                //移除SecondView
                if (secondView instanceof RelativeLayout) {
                    if ("image".equals(secondView.getTag(R.id.web_view))) {
                        dragLinearLayout.removeDragView(secondView);
                    } else {
                        dragLinearLayout.removeView(secondView);
                    }
                } else {
                    dragLinearLayout.removeView(secondView);
                }
                if (firstPosition >= secondPosition) {
                    //底下的View往上拖,先添加firstView
                    dragLinearLayout.addDragView(firstView, firstView.findViewById(R.id.move), secondPosition);
                    //添加SecondView
                    if (secondView instanceof RelativeLayout) {
                        if ("image".equals(secondView.getTag(R.id.web_view))) {
                            dragLinearLayout.addDragView(secondView, secondView.findViewById(R.id.move), firstPosition);
                        } else {
                            dragLinearLayout.addDragView(secondView, secondView.findViewById(R.id.move), firstPosition);
                        }
                    } else {
                        dragLinearLayout.addDragView(secondView, firstPosition);
                    }
                } else {
                    //上面往底下拖,先添加SecondView
                    if (secondView instanceof RelativeLayout) {
                        if ("image".equals(secondView.getTag(R.id.web_view))) {
                            dragLinearLayout.addDragView(secondView, secondView.findViewById(R.id.move), firstPosition);
                        } else {
                            dragLinearLayout.addDragView(secondView, secondView.findViewById(R.id.move), firstPosition);
                        }
                    } else {
                        dragLinearLayout.addDragView(secondView, firstPosition);
                    }
                    dragLinearLayout.addDragView(firstView, firstView.findViewById(R.id.move), secondPosition);
                }


            }
        });
        //allLayout.setBackgroundColor(Color.WHITE);
        setupLayoutTransitions();
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        dragLinearLayout.setPadding(20, 15, 20, 15);//设置间距，防止生成图片时文字太靠边，不能用margin，否则有黑边
        addView(dragLinearLayout, layoutParams);

        // 2. 初始化键盘退格监听, 主要用来处理点击回删按钮时，view的一些列合并操作
       /* keyListener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    EditText edit = (EditText) v;
                    onBackspacePress(edit);
                }
                return false;
            }
        };*/

        onFocusChangeListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //TODO ny
                hideStatusBar((AppCompatActivity) getContext());

                if (hasFocus) {
                    lastFocusEdit = (NyRichEditor) v;
                    if (focusChangeCallback != null) focusChangeCallback.onFocusChanged(hasFocus);
                }
            }
        };


        editParam = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        createTop(EDIT_PADDING);

        //
        final RelativeLayout firstEdit = createRichText(DEFAULTHTML, dip2px(context, EDIT_PADDING));
        dragLinearLayout.addDragView(firstEdit, editParam);
        lastFocusEdit = firstEdit.findViewById(R.id.editor);
        lastFocusEdit.focusEditor();

        setEditable(true, false);
    }


    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        // 只在图片View添加或remove时，触发transition动画
        LayoutTransition transitioner = new LayoutTransition();
        dragLinearLayout.setLayoutTransition(transitioner);
        transitioner.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            }

            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                transition.isRunning();
            }
        });
        transitioner.setDuration(300);
    }

    public int dip2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }


    public int getLastRichtextIndex() {
        int available = -1;
        int num = dragLinearLayout.getChildCount();
        // Log.e("MixedContainer", "dragLinearLayout.getChildCount()=" + num);

        if (num == 1) return available;
        for (int index = 1; index < num; index++) { //start index 1 to skip head
            View itemView = dragLinearLayout.getChildAt(index);
            if ("richtext".equals(itemView.getTag(R.id.web_view))) available = index;

        }
        return available;
    }


    public void setEditable(boolean editable, boolean movable) {
        this.movable = movable;

        int[] viewIds = new int[]{
                R.id.editor_title,
                R.id.editor_date,
                R.id.txt_remark,
                R.id.cmd_keywords,
                R.id.cmd_reference
        };

        for (int viewId : viewIds) {
            if (findViewById(viewId) != null) findViewById(viewId).setEnabled(editable);
        }

        ((NyDragLinearLayout) keywordsHolder).setLongClickDrag(movable);
        ((NyDragLinearLayout) keywordsHolder).setClickToDragListener(new NyDragLinearLayout.ILongClickToDragListener() {
            @Override
            public void onLongClickToDrag(View dragableView) {
                if (buttonCallout != null && movable) buttonCallout.trigged(ITEMCHANGE, null);
            }
        });

        ((NyDragLinearLayout) referenceHolder).setLongClickDrag(movable);
        ((NyDragLinearLayout) referenceHolder).setClickToDragListener(new NyDragLinearLayout.ILongClickToDragListener() {
            @Override
            public void onLongClickToDrag(View dragableView) {
                if (buttonCallout != null && movable) buttonCallout.trigged(ITEMCHANGE, null);
            }
        });

        findViewById(R.id.cmd_keywords).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonCallout != null && editable)
                    buttonCallout.trigged(KEYWORDBUTTON, "local");
            }
        });


        findViewById(R.id.cmd_reference).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonCallout != null && editable)
                    buttonCallout.trigged(REFERENCEBUTTON, "local");
            }
        });

        findViewById(R.id.cmd_reference).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (buttonCallout != null && editable)
                    buttonCallout.trigged(REFERENCEBUTTON, "global");
                return true;
            }
        });


        int num = dragLinearLayout.getChildCount();

        if (num == 1) return;
        for (int index = 1; index < num; index++) { //start index 1 to skip head
            View itemView = dragLinearLayout.getChildAt(index);
            if (itemView.findViewById(R.id.close) != null)
                itemView.findViewById(R.id.close).setVisibility(movable ? VISIBLE : GONE);
            if (itemView.findViewById(R.id.move) != null)
                itemView.findViewById(R.id.move).setVisibility(movable ? VISIBLE : GONE);
            if ("richtext".equals(itemView.getTag(R.id.web_view))) {
                NyRichEditor nyRichEditor = itemView.findViewById(R.id.editor);
                //TODO the next is critical to disable popup of softkeyboard
                nyRichEditor.setInputEnabled(editable);
                // nyRichEditor.setEditorBackgroundColor(Color.TRANSPARENT);
                itemView.findViewById(R.id.merge_down).setVisibility(movable ? VISIBLE : INVISIBLE);
                itemView.findViewById(R.id.split).setVisibility(movable ? VISIBLE : INVISIBLE);
            } else {// for ServerImage and ServerVideo
                NyRichEditor nyRichEditor = itemView.findViewById(R.id.server_view);
                nyRichEditor.setInputEnabled(editable);
            }

            View show = itemView.findViewById(R.id.show);
            if (show != null)
                show.setVisibility(editable || movable ? INVISIBLE : VISIBLE);

        }

        if (movable) showTop(false);
        else showTop(topOff);
        if (buttonCallout != null)
            buttonCallout.trigged(RICHTEXT_NUM, String.valueOf(noOfRichtext));

        hideStatusBar((AppCompatActivity) getContext());
    }

    public void clearRichBackGround() {
        int num = dragLinearLayout.getChildCount();

        if (num == 1) return;
        for (int index = 1; index < num; index++) { //start index 1 to skip head
            View itemView = dragLinearLayout.getChildAt(index);
            if ("richtext".equals(itemView.getTag(R.id.web_view))) {
                NyRichEditor nyRichEditor = itemView.findViewById(R.id.editor);
                //TODO the next is critical to disable popup of softkeyboard
                // nyRichEditor.setInputEnabled(toShow);
                //  nyRichEditor.setEditorBackgroundColor(toShow ? Color.LTGRAY : Color.WHITE);
                nyRichEditor.setEditorBackgroundColor(R.color.transparent);
            }

        }
    }

    public void setOnTextChangeListener(RichEditor.OnTextChangeListener onTextChangeListener) {
        this.onTextChangeListener = onTextChangeListener;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
    }


    public void clearAllLayout() {
        int num = dragLinearLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = dragLinearLayout.getChildAt(index);
            //   Log.e("EditContainer", "clearAllLayout() itemView.getTag=" + itemView.getTag(R.id.nyRichEditor));

            if ("image".equals(itemView.getTag(R.id.web_view))) {
                SeverImageView item = itemView.findViewById(R.id.server_view);
                if (item != null) {
                    item.stopServer();
                    item.removeFormat();
                    item.removeAllViews();
                }
            } else if ("video".equals(itemView.getTag(R.id.web_view))) {
                ServerVideoView item = itemView.findViewById(R.id.server_view);
                if (item != null) {
                    item.stopServer();
                    item.removeFormat();
                    item.removeAllViews();
                }
            } else if ("richtext".equals(itemView.getTag(R.id.web_view))) {
                NyRichEditor item = itemView.findViewById(R.id.editor);
                //   item.clearLocalRichEditorCache();
                item.removeFormat();
                item.removeAllViews();
            }

        }

        dragLinearLayout.removeAllViews();
        resetIndex();
    }

    public int getNextIndex() {
        return dragLinearLayout.getChildCount();
    }


    public void findKeyinAllText(String key) {
        //TODO ny global replacement is not allowed
        int num = dragLinearLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = dragLinearLayout.getChildAt(index);
            if ("richtext".equals(itemView.getTag(R.id.web_view))) {
                NyRichEditor item = itemView.findViewById(R.id.editor);
                String content = item.getHtml();
                content = content.replace("<mark>", "").replace("</mark>", "");
                if (!key.isEmpty()) {
                    content = content.replace(key, "<mark>" + key + "</mark>");
                }
                item.setHtml(content);
            }

        }
    }

    public void findKeyinCurrentText(String key) {
        String content = lastFocusEdit.getHtml();
        content = content.replace("<mark>", "").replace("</mark>", "");
        if (!key.isEmpty()) {
            content = content.replace(key, "<mark>" + key + "</mark>");

        }
        lastFocusEdit.setHtml(content);
    }

    public void replaceKeyinCurrentText(String key, String replacement) {
        String content = lastFocusEdit.getHtml();
        content = content.replace("<mark>", "").replace("</mark>", "");
        content = content.replace(key, "<mark>" + replacement + "</mark>");
        lastFocusEdit.setHtml(content);
    }

    public void clearMark() {
        String content = lastFocusEdit.getHtml();
        content = content.replace("<mark>", "").replace("</mark>", "");
        lastFocusEdit.setHtml(content);
    }

    //--------------------------------EditText---------------------

    /**
     * 在特定位置插入EditText
     **/

    public void addRichTextAtIndex(final int index, String html) {
        RelativeLayout layout = createRichText(html, EDIT_PADDING);
        ImageView move = layout.findViewById(R.id.move);
        dragLinearLayout.addDragView(layout, move, index);
        clearRichBackGround();
    }

    /**
     * 生成文本输入框
     */
    //  private String htmlContent = "<p>#nytaiji<br><br><br><br><br><br>@nytaiji</p>";
    public RelativeLayout createRichText(String html, int paddingTop) {
        noOfRichtext++;
        if (buttonCallout != null)
            buttonCallout.trigged(RICHTEXT_NUM, String.valueOf(noOfRichtext));

        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.edit_richtext, null);
        int myTagIndex = viewTagIndex++;
        layout.setTag(myTagIndex);
        // Log.e(TAG, "myTagIndex=" + myTagIndex);
        layout.setTag(R.id.web_view, "richtext");
        int editNormalPadding = 0;
        layout.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);

        final NyRichEditor editText = layout.findViewById(R.id.editor);
        editText.setBackgroundColor(getResources().getColor(R.color.colorSilver));
        // Log.e(TAG, "myTagIndex="+myTagIndex+" layout.getTag()="+layout.getTag());
        editText.setTag(myTagIndex);
        editText.setOnFocusChangeListener(onFocusChangeListener);

        if (onTextChangeListener != null) editText.setOnTextChangeListener(onTextChangeListener);
        //no use for the next
        // editText.setPlaceholder("Insert text here...");
        lastFocusEdit = editText;
        if (html != null) lastFocusEdit.setHtml(html);
        else lastFocusEdit.setHtml(EMPTYTHTML);
        lastFocusEdit.focusEditor();


        layout.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemCloseClick(layout);
                noOfRichtext--;
                if (buttonCallout != null)
                    buttonCallout.trigged(RICHTEXT_NUM, String.valueOf(noOfRichtext));
            }
        });

        layout.findViewById(R.id.show).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (buttonCallout != null) buttonCallout.trigged(EXPORT, editText.getHtml());
                return true;
            }
        });

        layout.findViewById(R.id.show).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonCallout != null) {
                    if (!editText.getHtml().contains("youtube"))
                        buttonCallout.trigged(CODESHOW, editText.getHtml());
                    else buttonCallout.trigged(VIDEOSHOW, extractYoutubeLink(editText.getHtml()));
                }
            }
        });


        layout.findViewById(R.id.merge_down).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextIndex = findNextRichText(myTagIndex);
                //  Log.e(TAG, "myIndex=" + myIndex + "nextIndex=" + nextIndex);
                if (myIndex > 0 && nextIndex > myIndex) mergeTwoRichTexts(myIndex, nextIndex);
            }
        });

        layout.findViewById(R.id.split).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBreak();
            }
        });

        return layout;
    }

    private void insertBreak() {
        String html = "<p></p>$$$%%%@@@<p></p><hr>";
        lastFocusEdit.insertHtml(html);
        if (buttonCallout != null) buttonCallout.trigged(RELOAD, null);
    }

    private int myIndex = -1;

    private int findNextRichText(int myTagIndex) {
        //  Log.e(TAG, "dragLinearLayout.getChildCount()=" + dragLinearLayout.getChildCount());
        for (int index = 0; index < dragLinearLayout.getChildCount(); index++) {
            View itemView = dragLinearLayout.getChildAt(index);
            //  Log.e(TAG, "itemView.getTag()=" + itemView.getTag());
            if (itemView.getTag().equals(myTagIndex)) myIndex = index;
            if (myIndex > 0 && "richtext".equals(itemView.getTag(R.id.web_view)) && index > myIndex)
                return index;
        }
        return -1;
    }

    private void mergeTwoRichTexts(int index1, int index2) {
        String mergeHtml = ((NyRichEditor) dragLinearLayout.getChildAt(index1).findViewById(R.id.editor)).getHtml();
        mergeHtml += ((NyRichEditor) dragLinearLayout.getChildAt(index2).findViewById(R.id.editor)).getHtml();
        //  Log.e(TAG, "mergetHtml" + mergeHtml);
        mergeHtml = mergeHtml.replace("<p></p><p></p><hr>", "");
        mergeHtml = mergeHtml.replace("<p></p><p></p>", "");
        // Log.e(TAG, "mergetHtml" + mergeHtml);
        ((NyRichEditor) dragLinearLayout.getChildAt(index1).findViewById(R.id.editor)).setHtml(mergeHtml);
        removeViewAtIndex(index2);

    }

    /**
     * 清除特定位置的View
     */
    private void removeViewAtIndex(int index) {
        View childAt = dragLinearLayout.getChildAt(index);
        // if (childAt.getTag(0).toString().contains("image")) childAt.
        onItemCloseClick(childAt);
    }


    public void addEmptyAtIndex(final int index) {
        RelativeLayout layout = createRichText(EMPTYTHTML, EDIT_PADDING);
        ImageView move = layout.findViewById(R.id.move);
        dragLinearLayout.addDragView(layout, move, index);
    }


    //---------------------------------TopJson----------------------------------------------------
    private LinearLayout topJson;

    public void createTop(int paddingTop) {
        topJson = (LinearLayout) inflater.inflate(R.layout.edit_top, null);
        topJson.setTag(viewTagIndex++);
        topJson.setTag(R.id.web_view, "topJson");
        int editNormalPadding = 0;
        topJson.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);

        eDate = topJson.findViewById(R.id.editor_date);
        eDate.addTextChangedListener(this);
        //TODO do not initiate a date
        //setDate(System.currentTimeMillis());
        eDate.setLongClickable(true);
        eDate.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(getDate()));
                // List<Long> markDay = new ArrayList<>();
                //  markDay.add(getDate());
                CalendarViewDialog.getInstance()
                        .init(getContext())
                        // .addMarks(markDay)
                        .setCalendar(calendar)
                        .setSelectedDay(getDate())
                        .setLimitMonth(false)

                        .show(new CalendarView.OnCalendarClickListener() {
                            @Override
                            public void onDayClick(Calendar daySelectedCalendar) {
                                dateTimeMills = daySelectedCalendar.getTimeInMillis();
                                setDate(dateTimeMills);
                                CalendarViewDialog.getInstance().close();
                                //  Toast.makeText(MainActivity.this, "选择的天数 : " + DateUtils.getDateTime(daySelectedCalendar.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onDayNotMarkClick(Calendar daySelectedCalendar) {
                                dateTimeMills = daySelectedCalendar.getTimeInMillis();
                                setDate(dateTimeMills);
                                CalendarViewDialog.getInstance().close();
                            }
                        });
                return true;
            }
        });
        eTitle = topJson.findViewById(R.id.editor_title);
        eTitle.addTextChangedListener(this);
        eRemark = topJson.findViewById(R.id.txt_remark);
        eRemark.addTextChangedListener(this);
        keywordsHolder = topJson.findViewById(R.id.editor_keywords);
        referenceHolder = topJson.findViewById(R.id.editor_reference);
        dragLinearLayout.addDragView(topJson, editParam);
    }

    public long getDate() {
        String mDate = eDate.getText().toString().trim();
        if (!mDate.isEmpty()) return NyFormatter.getDateLong(mDate);
        else return System.currentTimeMillis();
    }

    public void setTitle(String input) {
        eTitle.setText(input);
    }

    public String getTitle() {
        return eTitle.getText().toString();
    }

    public void setDate(String input) {
        eDate.setText(input);
    }

    public void setDate(long dateTimeMills) {
        this.dateTimeMills = dateTimeMills;
        eDate.setText(NyFormatter.getDateString(dateTimeMills));
    }


    public void setKeywords(ArrayList<String> keywords, boolean toAppend) {
        if (toAppend)
            this.keywords.addAll(keywords);
        else this.keywords = keywords;

        Set<String> set = new LinkedHashSet<String>(this.keywords);
        this.keywords.clear();
        this.keywords.addAll(set);

        keywordsHolder.removeAllViews();

        int hPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int vPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

        for (String key : this.keywords) {
            if (!key.isEmpty()) {
                TextView textView = new TextView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                textView.setText(key);
                textView.setLayoutParams(params);
                textView.setTextSize(15F);
                textView.setTextColor(Color.BLACK);
                //SetPadding (int left, int top, int right, int bottom);
                textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                textView.setPadding(hPadding, vPadding, hPadding, 0);
                textView.setShadowLayer(2F, 2F, 2F, Color.RED);
                //     textView.setBackgroundResource(R.drawable.btn_colored_material);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!movable && buttonCallout != null)
                            buttonCallout.trigged(KEYWORDITEM, textView.getText().toString());
                    }
                });
                ((NyDragLinearLayout) keywordsHolder).addDragView(textView, null);
            }
        }
    }

    public void addReference(ArrayList<String> selected, boolean toAppend) {
        if (selected == null || selected.isEmpty()) return;
        // referenceLayout.removeAllViews();
        int hPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int vPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

        for (String title : selected) {
            TextView textView = new TextView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textView.setText(title.trim());
            textView.setLayoutParams(params);
            textView.setTextSize(15F);
            textView.setTextColor(Color.BLACK);
            //SetPadding (int left, int top, int right, int bottom);
            textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            textView.setPadding(hPadding, vPadding, hPadding, 0);
            textView.setShadowLayer(2F, 2F, 2F, Color.RED);
            //     textView.setBackgroundResource(R.drawable.btn_colored_material);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!movable && buttonCallout != null)
                        buttonCallout.trigged(REFERENCEITEM, ((TextView) textView).getText().toString());
                }
            });
            //TODO to avoid reference itself
            if (!Objects.equals(title, eTitle.getText().toString()))
                ((NyDragLinearLayout) referenceHolder).addDragView(textView, null);
        }
    }


    public ArrayList<String> getKeywords() {
        keywords.clear();
        for (int i = 0; i < keywordsHolder.getChildCount(); i++) {
            keywords.add(((TextView) keywordsHolder.getChildAt(i)).getText().toString().trim());
        }
        return keywords;
    }

    public ArrayList<String> getReference() {
        reference.clear();
        for (int i = 0; i < referenceHolder.getChildCount(); i++) {
            reference.add(((TextView) referenceHolder.getChildAt(i)).getText().toString().trim());
        }
        return reference;
    }

    public void setRemark(String input) {
        if (input == null || input.isEmpty()) return;
        eRemark.setText(input);
        if (input.toLowerCase().contains("topoff")) {
            topOff = true;
            showTop(topOff);
        }
    }

    public String getRemark() {
        return eRemark.getText().toString();
    }

    private boolean topOff = false;

    public void showTop(boolean topOff) {
        //  Log.e("EditorContainer", "topOff =" + topOff);
        topJson.setVisibility(topOff ? GONE : VISIBLE);
    }


    //---------------------------------------free drawing--------------
    //TODO ny 2024-12-11 tmp out
  /*  public RelativeLayout createDrawing(String imagePath, int paddingTop) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.edit_draw, null);
        layout.setTag(viewTagIndex++);
        layout.setTag(R.id.web_view, "richtext");
        int editNormalPadding = 0;
        layout.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);

        final ImageView close = layout.findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final AdvDrawView drawing = layout.findViewById(R.id.drawing);
        drawing.setTag(layout.getTag());
        //  drawing.setOnFocusChangeListener(focusListener);
        //  drawing.setOnKeyListener(keyListener);
        return layout;
    }

    public void addDrawingAtIndex(final int index, String imagePath) {
        RelativeLayout layout = createDrawing(imagePath, EDIT_PADDING);
        ImageView move = layout.findViewById(R.id.move);
        dragLinearLayout.addDragView(layout, move, index);
    }*/


    //---------------------------------------------Other-----------------------------------------------

    /**
     * 处理视频叉掉的点击事件
     *
     * @param view 整个video对应的relativeLayout view
     * 删除类型 0代表backspace删除 1代表按红叉按钮删除
     */

    private List<String> undoList = new ArrayList<>();

    private void onItemCloseClick(View itemView) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Are you sure to delete the item?")
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
                        proceedDelete(itemView);
                    }
                });
        builder.show();

    }

    private void proceedDelete(View itemView) {
        dragLinearLayout.removeView(itemView);

        if ("image".equals(itemView.getTag(R.id.web_view))) {
            SeverImageView item = itemView.findViewById(R.id.server_view);
            if (item != null) {
                undoList.add("image:" + item.getAbsolutePath());
                item.stopServer();
            }
        } else if ("video".equals(itemView.getTag(R.id.web_view))) {
            ServerVideoView item = itemView.findViewById(R.id.server_view);
            if (item != null) {
                undoList.add("video:" + item.getAbsolutePath());
                item.stopServer();
            }
        } else if ("richtext".equals(itemView.getTag(R.id.web_view))) {
            NyRichEditor nyRichEditor = itemView.findViewById(R.id.editor);
            undoList.add("richtext:" + nyRichEditor.getHtml());
        }
        if (buttonCallout != null) buttonCallout.trigged(UNDOJOB, undoList.toString());

    }

    public List<String> getUndoList() {
        return undoList;
    }

    public void clearUndoList() {
        undoList.clear();
    }


    public List<String> exportEditString() {
        List<String> editList = new ArrayList<>();
        int num = dragLinearLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = dragLinearLayout.getChildAt(index);
            if ("image".equals(itemView.getTag(R.id.web_view))) {
                SeverImageView item = itemView.findViewById(R.id.server_view);
                if (item != null) editList.add("image:" + item.getAbsolutePath());
            } else if ("video".equals(itemView.getTag(R.id.web_view))) {
                ServerVideoView item = itemView.findViewById(R.id.server_view);
                if (item != null) editList.add("video:" + item.getAbsolutePath());
            } else if ("richtext".equals(itemView.getTag(R.id.web_view))) {
                NyRichEditor nyRichEditor = itemView.findViewById(R.id.editor);
                String item = nyRichEditor.getHtml();
                item = rgbColorToHex(item);
                item = item.replace(",", COMMA);
                item = item.replace("$$$%%%@@@", ",richtext:");  //for the split mark
                editList.add("richtext:" + item);
            }
        }
        return editList;
    }

    private String rgbColorToHex(String text) {
        while (text.contains("rgb(")) {
            String temp = text.substring(text.indexOf("rgb("));
            int endC = temp.indexOf(");");
            String tobeReplaced = temp.substring(0, endC + 1);
            String[] array = temp.substring(4, endC).split(",");
            String hexValue = String.format("#%02x%02x%02x", Integer.parseInt(array[0].trim()), Integer.parseInt(array[1].trim()), Integer.parseInt(array[2].trim()));
            text = text.replace(tobeReplaced, hexValue);
        }
        return text;
    }


    public List<String> exportRichText() {
        List<String> editList = new ArrayList<>();
        int num = dragLinearLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = dragLinearLayout.getChildAt(index);
            if ("richtext".equals(itemView.getTag(R.id.web_view))) {
                NyRichEditor nyRichEditor = itemView.findViewById(R.id.editor);
                editList.add("richtext:" + nyRichEditor.getHtml());
                // webView.evaluateJavascript("(function(){return document.getElementsByTagName('html')[0].innerHTML;})();",
              /*  richEditor.evaluateJavascript("(function(){return window.document.body.outerHTML;})();",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String html) {
                                html = html.replace("\\u003C", "<");
                                html = html.substring(html.lastIndexOf("contenteditable=") + 25, html.lastIndexOf("note-statusbar") - 32);
                                editList.add("richtext:" + html);
                            }
                        });*/
            }
        }
        return editList;
    }


    //TOTO ny unified callbacks
    public static final int UNDOJOB = 0; //RESERVE
    public static final int CODESHOW = 1;
    public static final int EXPORT = 2;
    public static final int VIDEOSHOW = 3;
    public static final int KEYWORDBUTTON = 6;
    public static final int KEYWORDITEM = 7;
    public static final int REFERENCEBUTTON = 8;
    public static final int REFERENCEITEM = 9;
    public static final int RICHTEXT_NUM = 10;
    public static final int ITEMCHANGE = 11;
    public static final int RELOAD = 12;

    private ButtonCallout buttonCallout;

    public void setButtonCallout(ButtonCallout buttonCallout) {
        this.buttonCallout = buttonCallout;
    }

    public static interface ButtonCallout {
        void trigged(int type, String content);
    }

    //TextWatcher
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (buttonCallout != null) buttonCallout.trigged(ITEMCHANGE, null);
    }


    //---------------------------------------------video-----------------------------------------------

    /**
     * 插入一个视频
     */

    public void insertServerVideo(String videoPath) {
        addSeverVideoViewAtIndex(getNextIndex(), videoPath);
        hideSoftInput(getContext());
    }


    public void addSeverVideoViewAtIndex(final int index, String videoPath) {
        int myTagIndex = viewTagIndex++;
        RelativeLayout layout =
                (RelativeLayout) inflater.inflate(R.layout.edit_server_video, null);
        //不允许视频拖拽
        // layout.setOnDragListener(null);
        layout.setTag(viewTagIndex++);
        layout.setTag(R.id.web_view, "video");

        int editNormalPadding = 0;
        layout.setPadding(editNormalPadding, EDIT_PADDING, editNormalPadding, EDIT_PADDING);

        ServerVideoView serverView = layout.findViewById(R.id.server_view);
        serverView.setBackgroundColor(getResources().getColor(R.color.colorSilver));
        serverView.setAbsolutePath(videoPath);
        serverView.setTag(myTagIndex);

        ImageView move = layout.findViewById(R.id.move);
        View close = layout.findViewById(R.id.close);
        close.setTag(layout.getTag());
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemCloseClick(layout);
                if (buttonCallout != null)
                    buttonCallout.trigged(ITEMCHANGE, null);
            }
        });
        dragLinearLayout.addDragView(layout, move, index);

        layout.findViewById(R.id.show).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Log.e(TAG, "show click");
                if (buttonCallout != null)
                    //buttonCallout.trigged(VIDEOSHOW, convertVideoToHtml(serverView.getServerPath()));
                    buttonCallout.trigged(VIDEOSHOW, convertToOriginalLink(serverView.getServerPath()));
                serverView.resumeServer();
            }
        });

    }

    private String extractYoutubeLink(String path) {
        Log.e(TAG, "extractYoutubeLink: =" + path);
        int index = path.indexOf("embed/");
        if (index > 0) {
            path = path.substring(index + 6);
            index = path.indexOf("frameborder");
            path = "https://m.youtube.com/watch?v=" + path.substring(0, index - 1);
            return path;
        }
        return path;
    }


    private String convertToOriginalLink(String path) {
        Log.e(TAG, "convertToOriginalLink: =" + path);
        if (path.contains("127.0.0.1")) {
            int index = path.indexOf("127.0.0.1:");
            path = path.substring(index + 1);
            index = path.indexOf("/");
            path = "file://" + path.substring(index);
            return path;
        }
        if (path.contains("/?"))
            path = path.substring(0, path.indexOf("/?"));  //remove download part of porntrex
        return path;
    }

    public void insertServerImage(String path) {
        addServerImageAtIndex(getNextIndex(), path);
        hideSoftInput(getContext());
    }


    public void addServerImageAtIndex(final int index, String path) {
        int myTagIndex = viewTagIndex++;
        RelativeLayout layout =
                (RelativeLayout) inflater.inflate(R.layout.edit_server_image, null);
        //不允许视频拖拽
        // layout.setOnDragListener(null);
        layout.setTag(viewTagIndex++);
        layout.setTag(R.id.web_view, "image");

        int editNormalPadding = 0;
        layout.setPadding(editNormalPadding, EDIT_PADDING, editNormalPadding, EDIT_PADDING);

        SeverImageView serverView = layout.findViewById(R.id.server_view);
        serverView.setBackgroundColor(getResources().getColor(R.color.colorSilver));
        serverView.setAbsolutePath(path);
        serverView.setTag(myTagIndex);

        ImageView move = layout.findViewById(R.id.move);
        View close = layout.findViewById(R.id.close);
        close.setTag(layout.getTag());
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemCloseClick(layout);
                if (buttonCallout != null)
                    buttonCallout.trigged(ITEMCHANGE, null);
            }
        });
        dragLinearLayout.addDragView(layout, move, index);

        layout.findViewById(R.id.show).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                serverView.resumeServer();
                serverView.loadImage();
            }
        });

        layout.findViewById(R.id.show).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (buttonCallout != null)
                    buttonCallout.trigged(VIDEOSHOW, convertToOriginalLink(serverView.getServerPath()));
                serverView.resumeServer();
                return true;
            }
        });

    }
}



