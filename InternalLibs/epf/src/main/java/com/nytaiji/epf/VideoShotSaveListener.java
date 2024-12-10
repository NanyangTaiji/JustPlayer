package com.nytaiji.epf;

import java.io.File;

public interface VideoShotSaveListener {
    void result(boolean success, File file);
}