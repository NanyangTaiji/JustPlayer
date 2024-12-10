package com.nytaiji.core.listener;

import com.nytaiji.nybase.model.NyVideoGroup;

import java.util.List;

public interface JsonLoadCallback {

    void onVideoGroups(List<NyVideoGroup> groups) ;
}
