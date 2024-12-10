package com.nytaiji.nybase.model;


import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.util.ObjectsCompat;

import org.json.JSONException;
import org.json.JSONObject;


public class NyEdit implements Parcelable {
    String richText=null;
    String imagePath=null;
    String videoPath=null;

    public NyEdit() {

    }


    public NyEdit(String richText, String imagePath, String videoPath) {
        this.richText = richText;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
    }


    public NyEdit(Cursor cursor) {
        richText = cursor.getString(0);
        imagePath = cursor.getString(1);
        videoPath = cursor.getString(2);
    }

    protected NyEdit(Parcel in) {
        richText = in.readString();
        imagePath = in.readString();
        videoPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //sequence must be consistent with previous method: NyVideo(parcel in)
        dest.writeString(richText);
        dest.writeString(imagePath);
        dest.writeString(videoPath);
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("richText", getRichText());
            object.put("imagePath", getImagePath());
            object.put("videoPath", getVideoPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NyEdit> CREATOR = new Creator<NyEdit>() {
        @Override
        public NyEdit createFromParcel(Parcel in) {
            return new NyEdit(in);
        }

        @Override
        public NyEdit[] newArray(int size) {
            return new NyEdit[size];
        }
    };

    public String getRichText() {
        return richText;
    }

    public void setRichText(String richText) {
        this.richText = richText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }


    @Override
    public int hashCode() {
        return ObjectsCompat.hash(richText, imagePath,videoPath);
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if (obj instanceof NyEdit) {
            NyEdit temp = (NyEdit) obj;
            //   if(this.name == temp.name && this.path== temp.path) return true;
            return this.richText.equals(temp.richText)
                    &&this.imagePath.equals(temp.imagePath)
                    &&this.videoPath.equals(temp.videoPath);
        }
        return false;

    }

 /*   @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return (this.name.hashCode() + this.path.hashCode());
        // return this.path.hashCode();
    }
*/
}
