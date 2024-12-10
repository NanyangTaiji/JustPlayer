package com.nanyang.richeditor.editor;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

public class KeyWord {
    private String parentKey;
    private String key;

    public KeyWord() {
    }

    public KeyWord(String parentKey, String key) {
        this.parentKey = parentKey;
        this.key = key;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        KeyWord keyWord = (KeyWord) o;
        return ObjectsCompat.equals(parentKey, keyWord.parentKey) &&
                ObjectsCompat.equals(key, keyWord.key);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(parentKey, key);
    }

    @NonNull
    @Override
    public String toString() {
        return "KeyWord{" +
                "parentKey='" + parentKey + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
