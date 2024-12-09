
package com.nytaiji.nybase.httpShare;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.NyMimeTypes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WebServerConnection implements Runnable {
    private static final String TAG = "WebServerConnection";
    public static final int HANDLER_CONNECTION_START = 42;
    public static final int HANDLER_CONNECTION_END = 4242;
    public static final int BUFFER_SIZE = 128 * 1096;  //default block size for portable drive >= 64 G
    //  private ShareHttpActivity launcherActivity;
    private final Handler mHandler;
    private UriInterpretation uriInterpretation = null;
    private final Socket connectionSocket;
    private UriInterpretation fileUri = null;

    private NyHybrid nyHybrid = null;

    private Context context;

    private static int connected = 0;

    private String ipAddress = "";
    //   private String myipAddress=null; //added 2021-1-7 to avoid message from own device.

    public WebServerConnection(UriInterpretation fileUri, Socket connectionSocket, Handler messageHandler) {
        this.fileUri = fileUri;
        this.connectionSocket = connectionSocket;
        this.mHandler = messageHandler;
        //  this.myipAddress= wifiShareUtil.getLocalIpAddress();
    }

    public WebServerConnection(Context context, NyHybrid nyHybrid, Socket connectionSocket, Handler messageHandler) {
        this.context = context;
        this.nyHybrid = nyHybrid;
        this.connectionSocket = connectionSocket;
        this.mHandler = messageHandler;
        Log.e(TAG, "NyHybrid file------ " + nyHybrid.getPath());
    }

    private static String httpReturnCodeToString(int return_code) {
        switch (return_code) {
            case 200:
                return "200 OK";
            case 302:
                return "302 Moved Temporarily";
            case 400:
                return "400 Bad Request";
            case 403:
                return "403 Forbidden";
            case 404:
                return "404 Not Found";
            case 500:
                return "500 Internal Server Error";
            case 501:
            default:
                return "501 Not Implemented";
        }
    }

    public static void reset() {
        connected = 0;
    }

    private static String lastIpAddress = "";

    private boolean isLimitted = true;  //default is unique connection

    public void setUnique(boolean islimitted) {
        this.isLimitted = islimitted;
    }

    public void run() {
        ipAddress = getClientIpAddress();
        //  s("ipAddress------------------------------ "+ipAddress);
        connected++;
        if (connected > 1 && !ipAddress.equals(lastIpAddress) && isLimitted) return;

        lastIpAddress = ipAddress; //to allow reshare from same IpAddress

        mHandler.handleMessage(mHandler.obtainMessage(HANDLER_CONNECTION_START, ipAddress));

        try {
            InputStream theInputStream;
            try {
                theInputStream = connectionSocket.getInputStream();
            } catch (IOException e1) {
                s("Error getting the InputString from connection socket.");
                e1.printStackTrace();
                return;
            }

            OutputStream theOutputStream;
            try {
                theOutputStream = connectionSocket.getOutputStream();
            } catch (IOException e1) {
                s("Error getting the OutputStream from connection socket.");
                e1.printStackTrace();
                return;
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(theInputStream));

            DataOutputStream output = new DataOutputStream(theOutputStream);

            http_handler(input, output);
            try {
                output.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } finally {
            s("Closing connection.");
            connected--;
            mHandler.handleMessage(mHandler.obtainMessage(HANDLER_CONNECTION_END, ipAddress));
        }
    }


    private String getClientIpAddress() {
        InetAddress client = connectionSocket.getInetAddress();
        return client.getHostAddress() + "/" + client.getHostName();
    }

    // our implementation of the hypertext transfer protocol
    // its very basic and stripped down
    private void http_handler(BufferedReader input, DataOutputStream output) {
        String header;
        try {
            header = input.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        String upperCaseHeader = header.toUpperCase();
        Boolean sendOnlyHeader = false;

        if (upperCaseHeader.startsWith("HEAD")) {
            sendOnlyHeader = true;
        } else {
            if (!upperCaseHeader.startsWith("GET")) {
                dealWithUnsupportedMethod(output);
                return;
            }
        }

        String path = getRequestedFilePath(header);

        if (path == "") {
            s("path is null!!!");
            return;
        }

        if (nyHybrid == null && fileUri == null) {
            s("No input resource");
            return;
        }

        String fileUriStr = fileUri != null ? fileUri.getUri().toString() : nyHybrid.getName(context);

        s("Client requested: [" + path + "][" + fileUriStr + "]");

        if (path.equals("/favicon.ico")) { // we have no favicon
            shareFavIcon(output);
            return;
        }

        if (fileUriStr.startsWith("http://")
                || fileUriStr.startsWith("https://")
                || fileUriStr.startsWith("ftp://")
                || fileUriStr.startsWith("maito:")
                || fileUriStr.startsWith("callto:")
                || fileUriStr.startsWith("skype:")) {
            // we will work as a simple URL redirector
            redirectToFinalPath(output, fileUriStr);
            return;
        }

        if (fileUri != null) {
            try {
                uriInterpretation = fileUri;
            } catch (SecurityException e) {
                e.printStackTrace();
                s("Share Via HTTP has no permition to read such file.");
                return;
            }
        } else if (nyHybrid != null) {
            s("file size to be transferred is " + nyHybrid.length(context));
        }

        if (path.equals("/")) {
            shareRootUrl(output);
            return;
        }
        shareOneFile(output, sendOnlyHeader, fileUriStr);
    }

    private void shareOneFile(DataOutputStream output, Boolean sendOnlyHeader, String fileUriStr) {

        InputStream requestedfile = null;

        try {
            if (uriInterpretation != null) {
                //  s("request Uri");
                requestedfile = uriInterpretation.getInputStream();
            } else if (nyHybrid != null) {
                // s("request file");
                requestedfile = nyHybrid.getDecryptedInputStream(context);
            }
        } catch (FileNotFoundException e) {
            try {
                s("I couldn't locate file. I am sending the input as text/plain");
                // instead of sending a 404, we will send the contact as text/plain
                output.writeBytes(construct_http_header(200, "text/plain"));
                output.writeBytes(fileUriStr);

                // if you could not open the file send a 404
                //s("Sending HTTP ERROR 404:" + e.getMessage());
                //output.writeBytes(construct_http_header(404, null));
                return;
            } catch (IOException e2) {
                s("errorX:" + e2.getMessage());
                return;
            }
        } // print error to gui
        catch (UnknownHostException | MalformedURLException e) {
            e.printStackTrace();
        }
        //  }
        // happy day scenario

        String outputString = construct_http_header(200, uriInterpretation != null ? uriInterpretation.getMime() : NyMimeTypes.getMimeTypeFromPath(nyHybrid.getPath()));
        s(TAG + outputString);

        try {
            output.writeBytes(outputString);

            // if it was a HEAD request, we don't print any BODY
            if (!sendOnlyHeader) {
                //TODO
                //no difference for the buffer size 10*1096*1096
                byte[] buffer = new byte[BUFFER_SIZE];
                for (int n; (n = requestedfile.read(buffer)) != -1; ) {
                    output.write(buffer, 0, n);
                }
                //}

            }
            requestedfile.close();
        } catch (IOException ignored) {
        }
    }

    private void redirectToFinalPath(DataOutputStream output, String thePath) {

        String redirectOutput = construct_http_header(302, null, thePath);
        try {
            // if you could not open the file send a 404
            output.writeBytes(redirectOutput);
            // close the stream
        } catch (IOException e2) {
        }
    }

    private void shareRootUrl(DataOutputStream output) {
       /* if (theUriInterpretation.isDirectory()) {
            redirectToFinalPath(output, theUriInterpretation.getName() + ".ZIP");
            return;
        }*/

        if (uriInterpretation != null) {
            redirectToFinalPath(output, uriInterpretation.getName());
        } else {
            redirectToFinalPath(output, nyHybrid.getName(context));
        }
    }

    private void shareFavIcon(DataOutputStream output) {
        try {
            // if you could not open the file send a 404
            output.writeBytes(construct_http_header(404, null));
            // close the stream
        } catch (IOException e2) {
        }
    }

    private String getRequestedFilePath(String inputHeader) {
        String path;
        String tmp2 = new String(inputHeader);

        // tmp contains "GET /index.html HTTP/1.0 ......."
        // find first space
        // find next space
        // copy whats between minus slash, then you get "index.html"
        // it's a bit of dirty code, but bear with me...
        int start = 0;
        int end = 0;
        for (int a = 0; a < tmp2.length(); a++) {
            if (tmp2.charAt(a) == ' ' && start != 0) {
                end = a;
                break;
            }
            if (tmp2.charAt(a) == ' ' && start == 0) {
                start = a;
            }
        }
        path = tmp2.substring(start + 1, end); // fill in the path
        return path;
    }

    private void dealWithUnsupportedMethod(DataOutputStream output) {
        try {
            output.writeBytes(construct_http_header(501, null));
        } catch (Exception e3) { // if some error happened catch it
            s("_error:" + e3.getMessage());
        } // and display error
    }

    private void s(String s2) { // an alias to avoid typing so much!
        Log.e(TAG, "[" + ipAddress + "] " + s2);
    }

    private String construct_http_header(int return_code, String mime) {
        return construct_http_header(return_code, mime, null);
    }

    // it is not always possible to get the file size :(
    private String getFileSizeHeader() {
        String s = "";
        if (uriInterpretation != null && uriInterpretation.getSize() > 0) {
            s = "Content-Length: "
                    + uriInterpretation.getSize() + "\r\n";
        } else if (nyHybrid != null && nyHybrid.getSmartLength(context) > 0) {
            s = "Content-Length: "
                    + nyHybrid.length(context) + "\r\n";
        }
        return s;
    }

    private String generateRandomFileNameForTextPlainContent() {
        return "StringContent-" + Math.round((Math.random() * 100000000)) + ".txt";
    }

    // this method makes the HTTP header for the response
    // the headers job is to tell the browser the result of the request
    // among if it was successful or not.
    private String construct_http_header(int return_code, String mime,
                                         String location) {

        StringBuilder output = new StringBuilder();
        output.append("HTTP/1.0 ");
        output.append(httpReturnCodeToString(return_code) + "\r\n");
        output.append(getFileSizeHeader());
        SimpleDateFormat format = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz");


        output.append("Date: " + format.format(new Date()) + "\r\n");

        output.append("Connection: close\r\n"); // we can't handle persistent
        // connections
        output.append("Server: ").append(TAG).append("\r\n");

        if (location == null && return_code == 302) {
            location = generateRandomFileNameForTextPlainContent();
        }
        if (location != null) {
            // we don't want cache for the root URL
            if (!location.startsWith("http://") && !location.startsWith("https://")) {  //if it is already an URL leave it as it is
                try {
                    int pos = location.indexOf("://");
                    if (pos > 0 && pos < 10) {
                        // so russians can download their files as well :)
                        // but if a protocol like http://, than we may as well redirect
                        location = URLEncoder.encode(location, "UTF-8");
                        s("after urlencode location:" + location);
                    }
                } catch (UnsupportedEncodingException e) {
                    s(Log.getStackTraceString(e));
                }
            }

            output.append("Location: ").append(location).append("\r\n"); // server name

            output.append("Expires: Tue, 03 Jul 2001 06:00:00 GMT\r\n");
            output.append("Cache-Control: no-store, no-cache, must-revalidate, max-age=0\r\n");
            output.append("Cache-Control: post-check=0, pre-check=0\r\n");
            output.append("Pragma: no-cache\r\n");
        }
        if (mime != null) {
          /*  if (fileUri.size() > 1) {
                mime = "multipart/x-zip";
            }*/
            output.append("Content-Type: ").append(mime).append("\r\n");
        }
        output.append("\r\n");
        return output.toString();
    }

}