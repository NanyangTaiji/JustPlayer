package com.amaze.filemanager.fileoperations.scanner;

import java.util.List;

public interface OnNetworkScanListener {
    void onComplete(List<Device> devices);
    void onFailed();
}
