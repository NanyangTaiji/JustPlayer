package com.amaze.filemanager.fileoperations.scanner;

import java.util.List;

public interface OnTracerouteListener {
    void onRouteAdd(Route route);
    void onComplete(List<Route> routes);
    void onFailed();
}
