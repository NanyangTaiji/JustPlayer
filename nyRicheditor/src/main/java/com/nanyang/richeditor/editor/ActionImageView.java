package com.nanyang.richeditor.editor;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.nanyang.richeditor.R;


/**
 * The Interface of Action Button
 * Created by even.wu on 22/8/17.
 */

public class ActionImageView extends AppCompatImageView {
    private ActionType mActionType;
    private NyRichEditor mNyRichEditor;
    private Context mContext;

    private boolean enabled = true;
    private boolean activated = true;

    private int enabledColor;
    private int disabledColor;
    private int activatedColor;
    private int deactivatedColor;

    public ActionImageView(Context context) {
        this(context, null);
    }

    public ActionImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ActionImageView);
        mActionType =
            ActionType.fromInteger(ta.getInteger(R.styleable.ActionImageView_actionType, 0));
        ta.recycle();
    }

    public ActionType getActionType() {
        return mActionType;
    }

    public void setActionType(ActionType mActionType) {
        this.mActionType = mActionType;
    }

    public NyRichEditor getRichEditorAction() {
        return mNyRichEditor;
    }

    public void setRichEditorAction(NyRichEditor mNyRichEditor) {
        this.mNyRichEditor = mNyRichEditor;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public void command() {
        //TODO RichEditorAction can not be null

        switch (mActionType) {
            case BOLD:
                mNyRichEditor.setBold();
                break;
            case ITALIC:
                mNyRichEditor.setItalic();
                break;
            case UNDERLINE:
                mNyRichEditor.setUnderline();
                break;
            case SUBSCRIPT:
                mNyRichEditor.setSubscript();
                break;
            case SUPERSCRIPT:
                mNyRichEditor.setSuperscript();
                break;
            case STRIKETHROUGH:
                mNyRichEditor.setStrikeThrough();
                break;
            case NORMAL:
                //TODO
                mNyRichEditor.removeFormat();
                break;
            case H1:
                mNyRichEditor.setHeading(1);
                break;
            case H2:
                mNyRichEditor.setHeading(2);
                break;
            case H3:
                mNyRichEditor.setHeading(3);
                break;
            case H4:
                mNyRichEditor.setHeading(4);
                break;
            case H5:
                mNyRichEditor.setHeading(5);
                break;
            case H6:
                mNyRichEditor.setHeading(6);
                break;
            case JUSTIFY_LEFT:
                mNyRichEditor.setAlignLeft();
                break;
            case JUSTIFY_CENTER:
                mNyRichEditor.setAlignCenter();
                break;
            case JUSTIFY_RIGHT:
                mNyRichEditor.setAlignRight();
                break;
            case JUSTIFY_FULL:
                //TODO
              //  mRichEditor.justifyFull();
                break;
            case ORDERED:
                mNyRichEditor.setNumbers();
                break;
            case UNORDERED:
                mNyRichEditor.setBullets();
                break;
            case INDENT:
                mNyRichEditor.setIndent();
                break;
            case OUTDENT:
                mNyRichEditor.setOutdent();
                break;
            case LINE:
                mNyRichEditor.insertLine();
                break;
            case BLOCK_QUOTE:
                mNyRichEditor.setBlockquote();
                break;
            case BLOCK_CODE:
                //TODO
             //   mRichEditor.formatBlockCode();
                break;
            case CODE_VIEW:
                //TODO
               // mRichEditor.codeView();
                break;
            default:
                break;
        }
    }

  /* public void command(String value) {

        //case FAMILY:
        //    mEditorMenuFragment.updateFontFamilyStates(value);
        //    break;
        //case SIZE:
        //    mEditorMenuFragment.updateFontStates(ActionType.SIZE, Double.valueOf(value));
        //    break;
        //case FORE_COLOR:
        //case BACK_COLOR:
        //    mEditorMenuFragment.updateFontColorStates(type, value);
        //    break;
        //case LINE_HEIGHT:
        //    mEditorMenuFragment.updateFontStates(ActionType.LINE_HEIGHT, Double.valueOf(value));
        //    break;

        switch (mActionType) {
            case FAMILY:
                break;
            case SIZE:
                break;
            case LINE_HEIGHT:
                break;
            case FORE_COLOR:
                break;
            case BACK_COLOR:
                break;
            case IMAGE:
                break;
            case LINK:
                break;
            case TABLE:
                break;
            default:
                break;
        }
    }*/

    public void resetStatus() {
    }

    public int getEnabledColor() {
        return enabledColor;
    }

    public void setEnabledColor(int enabledColor) {
        this.enabledColor = enabledColor;
    }

    public int getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(int disabledColor) {
        this.disabledColor = disabledColor;
    }

    public int getActivatedColor() {
        return activatedColor;
    }

    public void setActivatedColor(int activatedColor) {
        this.activatedColor = activatedColor;
    }

    public int getDeactivatedColor() {
        return deactivatedColor;
    }

    public void setDeactivatedColor(int deactivatedColor) {
        this.deactivatedColor = deactivatedColor;
    }

    public void notifyFontStyleChange(final ActionType type, final String value) {
        post(new Runnable() {
            @Override public void run() {
                switch (type) {
                    case BOLD:
                    case ITALIC:
                    case UNDERLINE:
                    case SUBSCRIPT:
                    case SUPERSCRIPT:
                    case STRIKETHROUGH:
                    case NORMAL:
                    case H1:
                    case H2:
                    case H3:
                    case H4:
                    case H5:
                    case H6:
                    case JUSTIFY_LEFT:
                    case JUSTIFY_CENTER:
                    case JUSTIFY_RIGHT:
                    case JUSTIFY_FULL:
                    case ORDERED:
                    case UNORDERED:
                        setColorFilter(ContextCompat.getColor(mContext,
                            Boolean.parseBoolean(value) ? getActivatedColor() : getDeactivatedColor()));
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
