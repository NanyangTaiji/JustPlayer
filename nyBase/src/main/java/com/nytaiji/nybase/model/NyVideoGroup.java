package com.nytaiji.nybase.model;

import java.util.ArrayList;
import java.util.List;


//-------------------------------online Adapter-----------------------------

public class NyVideoGroup {

    public final String title;
    public List<NyVideo> nyVideoInGroup;
    public boolean isChecked;
    public boolean isHidden;
    public boolean sortable;

    public NyVideoGroup(String title) {
        this.title = title;
        this.nyVideoInGroup = new ArrayList<NyVideo>();
        this.isHidden = false;
        this.isChecked = false;
        this.sortable = false;
    }

    public String getName() {
        return title;
    }

    public boolean contains(NyVideo nyVideo) {
        boolean hasIt = false;
        //  for (int j = 0; j < this.nyVideoInGroup.size(); j++) {
        for (NyVideo video : this.nyVideoInGroup) {
            if (!hasIt) hasIt = video.equals(nyVideo);
        }
        return hasIt;
    }

    public void add(NyVideo nyVideo) {
        nyVideoInGroup.add(nyVideo);
    }

    public void equates (NyVideoGroup nyVideoInGroup) {
      //  this.title =  nyVideoInGroup.title;
        this.nyVideoInGroup = nyVideoInGroup.nyVideoInGroup;
        this.isHidden = nyVideoInGroup.isHidden;
        this.isChecked = nyVideoInGroup.isChecked;
        this.sortable = nyVideoInGroup.sortable;
    }
}


