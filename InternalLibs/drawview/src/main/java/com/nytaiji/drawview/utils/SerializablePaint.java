package com.nytaiji.drawview.utils;

import android.graphics.Paint;

import java.io.Serializable;

/**
 * Empty class that handles a parcelable Paint object
 * Created by IngMedina on 28/04/2017.
 */

public class SerializablePaint extends Paint implements Serializable {
    int penWidth;
    int eraserWidth;
    public void setPenWidth(int width){
        this.penWidth=width;
    }
    public int getPenWidth(){
        return this.penWidth;
    }

    public void setEraserWidth(int width){
        this.eraserWidth=width;
    }
    public int getEraserWidth(){
        return this.eraserWidth;
    }
}
