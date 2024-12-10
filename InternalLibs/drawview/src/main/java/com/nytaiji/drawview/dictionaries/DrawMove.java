package com.nytaiji.drawview.dictionaries;

import android.graphics.Matrix;

import com.nytaiji.drawview.enums.DrawingMode;
import com.nytaiji.drawview.enums.DrawingTool;
import com.nytaiji.drawview.utils.SerializablePaint;
import com.nytaiji.drawview.utils.SerializablePath;

import java.io.Serializable;

/**
 * Created by Ing. Oscar G. Medina Cruz on 07/11/2016.
 * <p>
 * Dictionary class that save move for draw in the view, this allow the user to make a history
 * of the user movements in the view and make a redo/undo.
 *
 * @author Ing. Oscar G. Medina Cruz
 */

public class DrawMove implements Serializable {

    //region VARS
    private static DrawMove mDrawMove;

    private SerializablePaint mPaint;
    private DrawingMode mDrawingMode = null;
    private DrawingTool mDrawingTool = null;
    private int mDrawingShapeSides=-1;
    //private List<SerializablePath> mDrawingPathList;
    private SerializablePath mDrawingPath;
    private float mStartX, mStartY, mEndX, mEndY;
    private String mText;
    private Matrix mBackgroundMatrix;
    private byte[] mBackgroundImage;
    //endregion

    //region CONSTRUCTORS
    private DrawMove() {
    }

    public static DrawMove newInstance() {
        mDrawMove = new DrawMove();
        return mDrawMove;
    }
    //endregion

    //region GETTERS

    public SerializablePaint getPaint() {
        if (mDrawMove != null)
            return mPaint;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawingMode getDrawingMode() {
        if (mDrawMove != null)
            return mDrawingMode;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawingTool getDrawingTool() {
        if (mDrawMove != null)
            return mDrawingTool;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public SerializablePath getDrawingPath() {
        if (mDrawMove != null)
            return mDrawingPath;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public float getStartX() {
        if (mDrawMove != null)
            return mStartX;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public float getStartY() {
        if (mDrawMove != null)
            return mStartY;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public float getEndX() {
        if (mDrawMove != null)
            return mEndX;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public float getEndY() {
        if (mDrawMove != null)
            return mEndY;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public String getText() {
        if (mDrawMove != null)
            return mText;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public Matrix getBackgroundMatrix() {
        if (mDrawMove != null)
            return mBackgroundMatrix;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public byte[] getBackgroundImage() {
        if (mDrawMove != null)
            return mBackgroundImage;
        else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public int getDrawingShapeSides() {
        return mDrawingShapeSides;
    }

    //endregion

    //region SETTERS

    public DrawMove setPaint(SerializablePaint paint) {
        if (mDrawMove != null) {
            mDrawMove.mPaint = paint;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setDrawingMode(DrawingMode drawingMode) {
        if (mDrawMove != null) {
            mDrawMove.mDrawingMode = drawingMode;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setDrawingTool(DrawingTool drawingTool) {
        if (mDrawMove != null) {
            mDrawMove.mDrawingTool = drawingTool;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setDrawingPathList(SerializablePath drawingPath) {
        if (mDrawMove != null) {
            mDrawMove.mDrawingPath = drawingPath;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setStartX(float startX) {
        if (mDrawMove != null) {
            mDrawMove.mStartX = startX;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setStartY(float startY) {
        if (mDrawMove != null) {
            mDrawMove.mStartY = startY;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setEndX(float endX) {
        if (mDrawMove != null) {
            mDrawMove.mEndX = endX;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setEndY(float endY) {
        if (mDrawMove != null) {
            mDrawMove.mEndY = endY;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setText(String text) {
        if (mDrawMove != null) {
            mDrawMove.mText = text;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setBackgroundImage(byte[] backgroundImage, Matrix backgroundMatrix) {
        if (mDrawMove != null) {
            mDrawMove.mBackgroundImage = backgroundImage;
            mDrawMove.mBackgroundMatrix = backgroundMatrix;
            return mDrawMove;
        } else throw new RuntimeException("Create new instance of DrawMove first!");
    }

    public DrawMove setDrawingShapeSides(int drawingShapeSides) {
        this.mDrawingShapeSides = drawingShapeSides;
        return mDrawMove;
    }

    //endregion
}
