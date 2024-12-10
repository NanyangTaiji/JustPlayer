package com.nytaiji.core.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nytaiji.core.base.BaseVideoFrame;

import java.lang.ref.WeakReference;

public class ListVideoManager {

    private static ListVideoManager instance;

    protected WeakReference<BaseVideoFrame> videoViewWeakReference;

    protected int curPos = -1;

    public static ListVideoManager getInstance() {
        if (instance == null) {
            instance = new ListVideoManager();
        }
        return instance;
    }

    private BaseVideoFrame getCurrentVideoView() {
        return videoViewWeakReference != null ? videoViewWeakReference.get() : null;
    }

    private void removePlayerFromParent() {
        BaseVideoFrame currentVideoView = getCurrentVideoView();
        if (currentVideoView != null && currentVideoView.getParent() != null) {
            ((ViewGroup) currentVideoView.getParent()).removeView(currentVideoView);
        }
    }

    private View getChildViewAt(ListView listView, int position) {
        return listView.getChildAt(position + listView.getHeaderViewsCount() - listView.getFirstVisiblePosition());
    }

    private View getChildViewAt(RecyclerView recyclerView, int position) {
        return recyclerView.getLayoutManager().findViewByPosition(position);
    }

    public void play(@NonNull ListView listView, @IdRes int containerId, int position, String url, String title) {
        play(listView, containerId, position, url, title, null);
    }

    /**
     *
     * @param listView       视频所在的ListView
     * @param containerId    ListView的item中用于包裹video的容器ID
     * @param position       视频所在item的位置
     * @param url            视频url
     * @param title          视频title
     * @param customVideoView  自定义VideoView
     */
    public void play(@NonNull ListView listView, @IdRes int containerId, int position, String url, String title, BaseVideoFrame customVideoView) {
        curPos = position;
        View curPosView = getChildViewAt(listView, position);
        initVideoView(listView.getContext(), curPosView, containerId, url, title, customVideoView);
    }

    public void play(@NonNull RecyclerView recyclerView, @IdRes int containerId, int position, String url, String title) {
        play(recyclerView, containerId, position, url, title, null);
    }


    /**
     *
     * @param recyclerView      视频所在的RecyclerView
     * @param containerId    RecyclerView的item中用于包裹video的容器ID
     * @param position       视频所在item的位置
     * @param url            视频url
     * @param title          视频title
     * @param customVideoView  自定义VideoView
     */
    public void play(@NonNull RecyclerView recyclerView, @IdRes int containerId, int position, String url, String title, BaseVideoFrame customVideoView) {
        curPos = position;
        View curPosView = getChildViewAt(recyclerView, position);
        initVideoView(recyclerView.getContext(), curPosView, containerId, url, title, customVideoView);
    }

    protected void initVideoView(Context context, View curPosView, @IdRes int containerId, String url, String title, BaseVideoFrame customVideoView) {
        BaseVideoFrame currentVideoView = getCurrentVideoView();
        if (currentVideoView != null) {
            currentVideoView.release();
            removePlayerFromParent();
        }
        currentVideoView = customVideoView;

        if (currentVideoView == null) {
            currentVideoView = newVideoViewInstance(context);
        }

        videoViewWeakReference = new WeakReference<>(currentVideoView);

        ViewGroup containerView = null;
        if (curPosView != null) {
            containerView = curPosView.findViewById(containerId);
        }
        if (containerView != null) {
            containerView.removeAllViews();
            containerView.addView(currentVideoView);
            currentVideoView.setVideoUrl(url);
            currentVideoView.setTitle(title);
            currentVideoView.invalidate();
            currentVideoView.start();
        }
    }

    public void destroy() {
        BaseVideoFrame currentVideoView = getCurrentVideoView();
        if (currentVideoView != null) {
            currentVideoView.release();
        }
    }

    public void release() {
        BaseVideoFrame currentVideoView = getCurrentVideoView();
        if (currentVideoView != null) {
            currentVideoView.release();
        }
        removePlayerFromParent();
    }

    public void pause() {
        BaseVideoFrame currentVideoView = getCurrentVideoView();
        if (currentVideoView != null) {
            currentVideoView.pause();
        }
    }

    public boolean isFullScreen() {
        BaseVideoFrame currentVideoView = getCurrentVideoView();
        return currentVideoView != null && currentVideoView.isFullScreen();
    }

    public boolean onBackKeyPressed() {
        BaseVideoFrame currentVideoView = getCurrentVideoView();
        if (currentVideoView != null) {
            return currentVideoView.onBackKeyPressed();
        }
        return false;
    }

    public int getCurPos() {
        return curPos;
    }

    //用于BaseVideoView的继承扩展
    protected BaseVideoFrame newVideoViewInstance(Context context) {
        return new ListVideoFrame(context);
    }
}
