package com.nytaiji.nybase.libmediaStreamer;

import android.content.Context;

import java.io.IOException;

public class LocalSingleHttpServer extends AbsHttpServer {
    public LocalSingleHttpServer(Context context) throws IOException {
        super(context, ServerType.SINGLEHTTPSERVER);
    }
}