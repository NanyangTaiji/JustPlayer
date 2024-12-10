package com.nanyang.richeditor.editor;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

import java.util.Arrays;

public class KeyCategory {
    private String category;
    private KeyWord[] keyWords;

    public KeyCategory() {
    }

    public KeyCategory(String category, KeyWord[] keyWords) {
        this.category = category;
        this.keyWords = keyWords;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public KeyWord[] getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(KeyWord[] keyWords) {
        this.keyWords = keyWords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        KeyCategory tvGroup = (KeyCategory) o;
        return ObjectsCompat.equals(category, tvGroup.category) &&
                Arrays.equals(keyWords, tvGroup.keyWords);
    }

    @Override
    public int hashCode() {
        int result = ObjectsCompat.hash(category);
        result = 31 * result + Arrays.hashCode(keyWords);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "KeyCategory{" +
                "category='" + category + '\'' +
                ", keyWords=" + Arrays.toString(keyWords) +
                '}';
    }
}
