package com.nytaiji.nybase.httpShare;

//package webs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.nytaiji.nybase.model.NyHybrid;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Title: A simple Webserver Tutorial NO warranty, NO guarantee, MAY DO damage
 * to FILES, SOFTWARE, HARDWARE!! Description: This is a simple tutorial on
 * making a webserver posted on http://turtlemeat.com . Go there to read the
 * tutorial! This program and sourcecode is free for all, and you can copy and
 * modify it as you like, but you should give credit and maybe a link to
 * turtlemeat.com, you know R-E-S-P-E-C-T. You gotta respect the work that has
 * been put down.
 * <p>
 * Copyright: Copyright (c) 2002 Company: TurtleMeat
 *
 * @version 1.0
 * @author: Jon Berg <jon.berg[on_server]turtlemeat.com
 */

// file: server.java
// the real (http) serverclass
// it extends thread so the server is run in a different
// thread than the gui, that is to make it responsive.
// it's really just a macho coding thing.

public class WebServer extends Thread {
    private static final String TAG = "WebServer";
    // by design, we only serve one file at a time.

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Handler mHandler;
    private static int port;
    private UriInterpretation fileUri = null;
    private static ServerSocket serverSocket = null;
    @SuppressLint("StaticFieldLeak")
    private boolean webserverLoop = true;

    private Context context;

    private NyHybrid file = null;

    public static boolean isLimitted = true;

    // default port is 80
    public WebServer(int listen_port, Handler mHandler) {
        port = listen_port;
        this.mHandler = mHandler;
        if (serverSocket == null) {
            this.start();
        }
    }

    public void setFile(Context context, NyHybrid file) {
        this.context = context;
        this.file = file;
        // Log.e(TAG, "initHttpServer "+file.getPath());
    }

    public void setUnique(boolean islimitted) {
        WebServer.isLimitted = islimitted;
    }

    public void setUri(UriInterpretation fileUri) {
        this.fileUri = fileUri;
    }

    public UriInterpretation GetFile() {
        return fileUri;
    }


    public synchronized void stopServer() {
        s("Closing server...\n\n");
        webserverLoop = false;
        if (serverSocket != null) {
            try {
               /* Socket socket = serverSocket.accept();
                if (socket != null) {
                    socket.getInputStream().close();
                    socket.getOutputStream().close();
                    socket.close();
                }*/
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
        }
        this.interrupt();
    }


    private boolean normalBind(int thePort) {
        s("Attempting to bind on port " + thePort);
        try {
            serverSocket = new ServerSocket(thePort);
        } catch (Exception e) {
            s("Fatal Error:" + e.getMessage() + " " + e.getClass().toString());
            return false;
        }
        port = thePort;
        s("Binding was OK!");
        return true;
    }

    public void run() {
        //  s("Starting " + Util.myLogName + " server v" + BuildConfig.VERSION_NAME);
        if (!normalBind(port)) {
            return;
        }

        // go in a infinite loop, wait for connections, process request, send
        // response
        while (webserverLoop) {
            s("Ready, Waiting for requests...\n");
            try {
                Socket connectionSocket = serverSocket.accept();
                WebServerConnection theHttpConnection = null;
                if (fileUri != null)
                    theHttpConnection = new WebServerConnection(fileUri, connectionSocket, mHandler);
                else if (file != null)
                    theHttpConnection = new WebServerConnection(context, file, connectionSocket, mHandler);

                if (theHttpConnection != null) {
                    theHttpConnection.setUnique(isLimitted);
                    threadPool.submit(theHttpConnection);
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void s(String s2) { // an alias to avoid typing so much!
        Log.e(TAG, s2);
    }


}
