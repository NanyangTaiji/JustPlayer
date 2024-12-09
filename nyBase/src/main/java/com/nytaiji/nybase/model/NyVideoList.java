package com.nytaiji.nybase.model;

import java.util.ArrayList;


public class NyVideoList {
    private ArrayList<NyVideo> videoArrayList;
    private int currentItem;


    public NyVideoList(ArrayList<NyVideo> videoArrayList, int currentItem) {
        this.videoArrayList = videoArrayList;
        this.currentItem = currentItem;

    }

    public ArrayList<NyVideo> getList() {
        return videoArrayList;
    }

    public int getCurrentItem() {
        return currentItem;
    }

    public void setList(ArrayList<NyVideo> videoArrayList) {
        this.videoArrayList = videoArrayList;
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }

    public boolean contains(NyVideo nyVideo) {
        boolean hasIt = false;
        for (NyVideo video : this.videoArrayList) {
            if (!hasIt) hasIt = video.equals(nyVideo);
        }
        return hasIt;
    }

    public void add(NyVideo nyVideo) {
        videoArrayList.add(nyVideo);
    }

}


