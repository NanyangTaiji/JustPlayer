package com.nytaiji.nybase.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NyEditGroup {
    public String title;
    public String mDate;
    public String keyWords;
    public List<NyEdit> nyEditInGroup;
    public boolean isChecked;
    public boolean isHidden;

    public NyEditGroup(String title) {
        this.title = title;
        this.nyEditInGroup = new ArrayList<NyEdit>();
        this.mDate = null;
        this.keyWords = "";
        this.isHidden = false;
        this.isChecked = false;
    }

    public NyEditGroup(String title, String mdate, String keywords, ArrayList NyEditArray) {
        this.title = title;
        this.nyEditInGroup = NyEditArray;
        this.mDate = mdate;
        this.keyWords = keywords;
        this.isHidden = false;
        this.isChecked = false;
    }


    public boolean contains(NyEdit nyEdit) {
        boolean hasIt = false;
        for (NyEdit mNyEdit : this.nyEditInGroup) {
            if (!hasIt) hasIt = mNyEdit.equals(nyEdit);
        }
        return hasIt;
    }

    public void add(NyEdit nyEdit) {
        nyEditInGroup.add(nyEdit);
    }


    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();

        try {
            object.put("title", title);
            object.put("mDate", mDate);
            object.put("keyWords", keyWords);
            int groupsize = nyEditInGroup.size();
            JSONArray array = new JSONArray();
            for (int i = 0; i < groupsize; i++) {
                array.put(nyEditInGroup.get(i).toJSONObject());
            }
            object.put("nyEditInGroup", array);
            object.put("isHidden", isHidden);
            object.put("isChecked", isChecked);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }


    public void equates(NyEditGroup mNyEditGroup) {
        this.title = mNyEditGroup.title;
        this.mDate = mNyEditGroup.mDate;
        this.keyWords = mNyEditGroup.keyWords;
        this.nyEditInGroup = mNyEditGroup.nyEditInGroup;
        this.isChecked = mNyEditGroup.isChecked;
        this.isHidden = mNyEditGroup.isHidden;
    }
}


