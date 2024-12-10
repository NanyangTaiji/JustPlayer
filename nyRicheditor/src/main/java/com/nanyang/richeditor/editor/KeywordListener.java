package com.nanyang.richeditor.editor;


import java.util.ArrayList;

public interface KeywordListener {

    public void onListChanged(ArrayList<String> chosenChildren, boolean toAppend);

}