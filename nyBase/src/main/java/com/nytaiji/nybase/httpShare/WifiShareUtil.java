package com.nytaiji.nybase.httpShare;


import static com.nytaiji.nybase.filePicker.MediaSelection.DEFAULT_MEDIA;
import static com.nytaiji.nybase.utils.SystemUtils.copyToClipboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.NyMimeTypes;
import com.nytaiji.nybase.utils.PreferenceHelper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

public class WifiShareUtil {
    private static final String TAG = "shareUtil:--";

    private static final int HTTP_DEFAUL_PORT = 8888;

    private static int port;

    public static final String REROUTE_HTML = "r.html";

    public static String ServerUrlForExternal;
    public static String ServerUrlForInternal;
    private static Handler messageHandler;
    private static String bindingIP = null;
    private static WebServer webServer = null;

    private static final String reroutedHtml = Environment.getExternalStorageDirectory() + "/" + REROUTE_HTML;

    public static void httpShare(Context context, Uri uri, boolean isInternal, Handler handler) {

        if (uri != null) {
            messageHandler = handler;
            UriInterpretation uriInter = getFileUri(context, uri);
            Log.e(TAG, "share uriInter " + uriInter.getUri().getPath());
            initHttpServer(context, uriInter);
        }
        //  Log.e("preferredServerUrl", linkUrl);
        pushLink(context, getPreferredServerUrl(isInternal));
        copyToClipboard(context, getPreferredServerUrl(isInternal));
    }

    public static void httpShare(Context context, Uri uri, String linkUrl, Handler handler) {
        if (uri != null) {
            messageHandler = handler;
            UriInterpretation uriInter = getFileUri(context, uri);
            Log.e(TAG, "share uriInter------ " + uriInter.getUri().getPath());
            initHttpServer(context, uriInter);
        }
        //  Log.e("preferredServerUrl", linkUrl);
        pushLink(context, linkUrl);
        copyToClipboard(context, linkUrl);
    }

    public static void httpShare(Context context, NyHybrid file, String linkUrl, Handler handler) {
        if (file != null /*&& file.exists()*/) { //file.exists() not available for zip entries
            messageHandler = handler;
            initHttpServer(context, file);
        }
        pushLink(context, linkUrl);
        copyToClipboard(context, linkUrl);
    }

    public static void setUnique(boolean islimitted) {
        WebServer.isLimitted = islimitted;
    }


    public static String getPreferredServerUrl(boolean isInternal) {
        return getPreferredServerUrl(isInternal, true);
    }

    public static String getPreferredServerUrl(boolean isInternal, boolean isDefault) {
        port = isDefault ? HTTP_DEFAUL_PORT : getRandomPort(9000, 9999);
        CharSequence[] listOfServerUris = listOfIpAddresses(port);
        int i = 0;
        while (i < listOfServerUris.length) {
            String hostAddress = listOfServerUris[i].toString();
            if (!isInternal && hostAddress.contains("192.168.")) {
                ServerUrlForExternal = hostAddress;
                return hostAddress;
            } else if (isInternal && (hostAddress.contains("127.0.") /*|| hostAddress.contains("2406:3003")*/)) {
                ServerUrlForInternal = hostAddress;
                return hostAddress;
            }
            i++;
        }
        return null;
    }

    private static int getRandomPort(int minValue, int maxValue) {
        Random rn = new Random();
        return minValue + rn.nextInt(maxValue - minValue + 1);
    }

    public static void getDefaultServerUrl() {
        CharSequence[] listOfServerUris = listOfIpAddresses(HTTP_DEFAUL_PORT);
        int i = 0;
        while (i < listOfServerUris.length) {
            String hostAddress = listOfServerUris[i].toString();
            if (hostAddress.contains("192.168.")) {
                ServerUrlForExternal = hostAddress;
            }
            if (hostAddress.contains("127.0.") || hostAddress.contains("2406:3003")) {
                ServerUrlForInternal = hostAddress;
            }
            i++;
        }
    }

    private static void initHttpServer(Context context, UriInterpretation myUri) {
        if (myUri == null) {
            return;
        }
        if (webServer != null) stopHttpServer();
        webServer = new WebServer(port, messageHandler);
        PreferenceHelper.getInstance().setString(DEFAULT_MEDIA, NyMimeTypes.getMimeTypeFromPath(myUri.toString()));
        webServer.setUri(myUri);
    }

    private static void initHttpServer(Context context, NyHybrid file) {
        if (file == null) {
            return;
        }
        //  Log.e(TAG, "initHttpServer "+file.getPath());
        if (webServer != null) stopHttpServer();
        webServer = new WebServer(port, messageHandler);
        PreferenceHelper.getInstance().setString(DEFAULT_MEDIA, NyMimeTypes.getMimeTypeFromPath(file.getPath()));
        webServer.setFile(context, file);
    }

    public static void stopHttpServer() {
        WebServer p = webServer;
        webServer = null;
        if (p != null) {
            p.stopServer();
        }
        WebServerConnection.reset();
        // Snackbar.make(view, context.getResources().getString(R.string.no_sharing_anymore), Snackbar.LENGTH_SHORT).show();
    }

    private static UriInterpretation getFileUri(Context mContext, Uri dataUri) {
        if (dataUri == null) return null;
        return new UriInterpretation(dataUri, mContext.getContentResolver());
    }

    public static Handler getMessageHandler(View view) {
        Handler messageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case WebServerConnection.HANDLER_CONNECTION_START:
                        String msg = String.format("Connected Ip", (String) inputMessage.obj);
                        if (view != null) Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
                        break;
                    case WebServerConnection.HANDLER_CONNECTION_END:
                        String msg2 = String.format("Disconnected Ip", (String) inputMessage.obj);
                        if (view != null) Snackbar.make(view, msg2, Snackbar.LENGTH_LONG).show();
                        break;
                    default:
                        super.handleMessage(inputMessage);
                }
            }

        };
        return messageHandler;
    }

    //---------------------------------------------------------//
    public static void webReroute(Context context, String httpUrl, Handler handler) {
        if (httpUrl != null) {
            messageHandler = handler;
            if (httpUrl.toLowerCase().contains("%")) {
                // httpUrl = decodeString(httpUrl);
                try {
                    httpUrl = URLDecoder.decode(httpUrl, "UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
            savePathToRerouteHtml(httpUrl);
            UriInterpretation uriInter = getFileUri(context, Uri.fromFile(new File(reroutedHtml)));
            Log.e(TAG, "reroute uriInter " + uriInter.getUri().getPath());
            initHttpServer(context, uriInter);
        }
        //  Avoid double push
        // pushLink(context, linkUrl);
        // copyToClipboard(context, linkUrl);
    }


    public static void saveContentToRerouteHtml(String content) {
        try {
            FileWriter writer = new FileWriter(reroutedHtml);
            writer.write(content.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePathToRerouteHtml(String httpUrl) {
        String newLine = System.getProperty("line.separator");

        Log.e(TAG, "savePathToRerouteHtml: -------  " + httpUrl);
        //
        StringBuilder content = new StringBuilder("<!DOCTYPE html>" + newLine).append("<head>").append(newLine)//
                .append("<meta http-equiv=\"refresh\" content=\"0" + ";url='")//
                .append(httpUrl + "'\" />" + newLine)
                .append("</head>" + newLine + "</html>");
        //  String output = content.toString();
        Log.e(TAG, "Stringbuilder " + content.toString());
        try {
            FileWriter writer = new FileWriter(reroutedHtml);
            writer.write(content.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //------------------------------------------------------------------
   /* public static void httpLocalShare(Context context, String path) {
        //   context.startService(new Intent(context, StreamService.class));
        //local file
        path = SmbUtils.wrapLocalFullURL(path);
        //process same as smb
        // path = SmbUtils.wrapStreamSmbURL(path, NanoStreamer.INSTANCE().getIp(), NanoStreamer.INSTANCE().getPort());

        path = path.replace("smb=%2F", "");
        //Log.e(TAG, "removed path " + path);
        pushLink(context, path);
        String nurl = URLDecoder.decode(path);
        webReroute(context, nurl, messageHandler);
        //
        copyToClipboard(context, nurl);
        // MediaManipulate.urlHandle(context, nurl);
    }*/

    public static String redirectStream(Context context, String path) {
        //  Log.e(TAG, "original path " + path);
        int index = path.indexOf("smb=%3B");
        if (index > 0) {
            //  context.startService(new Intent(context, StreamService.class));
            path = path.substring(index);
            //  Log.e(TAG, "removed path " + path);
            //  path = "http://" + NanoStreamer.INSTANCE().getIp() + File.pathSeparator + NanoStreamer.INSTANCE().getPort() + "/" + path;
            //  Log.e(TAG, "restream path " + path);
            path = URLDecoder.decode(path);
        }
        return path;
    }


    public static void pushLink(Context context, String url) {
        if (bindingIP == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("data", 0);
            bindingIP = sharedPreferences.getString("ip", null);
        }

        //shareremote
        if (bindingIP != null) {
            push(bindingIP, url, "");
            Log.e(TAG, "Push to IP " + bindingIP + "for " + url);
            //  onlineInputHandle(getContext());
        }

    }

    /**
     * 把文字推送到指定的IP
     *
     * @param ip   对方的ip
     * @param text 推送的文字
     */
    public static void push(String ip, String text, String type) {
        // android.util.Log.e(TAG, "share url: " + url);
        String shareUrl = "";
        try {
            shareUrl = Base64.encodeToString(text.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (type == null || type == "") type = "url"; //default video url
        String finalShareUrl = "http://" + ip + ":8081/pushVideo?" + type + "=" + shareUrl;

        Log.e(TAG, "share url: " + finalShareUrl);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection connect = Jsoup.connect(finalShareUrl).ignoreHttpErrors(true)
                        .ignoreContentType(true)
                        .timeout(5000);
                try {
                    connect.get();
                    // Toast.makeText(context, "share to IP: " + ip, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static String decodeString(String url) {
        String decodedUrl = null;
        try {
            decodedUrl = new String(Base64.decode(url, Base64.DEFAULT), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodedUrl;
    }

    public static String encodeString(String url) {
        String encodedUrl = null;
        try {
            encodedUrl = Base64.encodeToString(url.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedUrl;
    }

    public static CharSequence[] listOfIpAddresses(int port) {
        ArrayList<String> arrayOfIps = new ArrayList<String>(); // preferred IPs
        ArrayList<String> arrayOfIps4 = new ArrayList<String>(); // IPv4 addresses
        ArrayList<String> arrayOfIps6 = new ArrayList<String>(); // IPv6 addresses
        ArrayList<String> arrayOfIpsL = new ArrayList<String>(); // loopback


        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();

                // Log.d(Util.myLogName, "Inteface: " + intf.getDisplayName());
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    String theIpTemp = inetAddress.getHostAddress();
                    String theIp = getServerUrl(theIpTemp, port);

                    if (inetAddress instanceof Inet6Address) {
                        arrayOfIps6.add(theIp);
                    } else if (inetAddress.isLoopbackAddress()) {
                        arrayOfIpsL.add(theIp);
                    } else if (intf.getDisplayName().matches("wlan.*")) {
                        arrayOfIps.add(0, theIp); // prefer IPv4 on wlan interface
                    } else {
                        arrayOfIps4.add(theIp);
                    }
                }
            }

            // append IP lists in order of preference
            arrayOfIps.addAll(arrayOfIps4);
            arrayOfIps.addAll(arrayOfIps6);
            arrayOfIps.addAll(arrayOfIpsL);

            if (arrayOfIps.size() == 0) {
                String firstIp = getServerUrl("0.0.0.0", port);
                arrayOfIps.add(firstIp);
            }

        } catch (SocketException ex) {
            Log.e("httpServer", ex.toString());
        }

        return arrayOfIps.toArray(new CharSequence[0]);
    }

    public static String findIPAddress(@NonNull Context context) {

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        try {
            String address;
            if (wifiManager.getConnectionInfo() != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                InetAddress inetAddress = InetAddress.getByAddress(byteBuffer.putInt(wifiInfo.getIpAddress()).array());
                address = inetAddress.getHostAddress();
            } else {
                address = null;
            }

            return address;
        } catch (Exception e) {
            Log.e(TAG, "Error finding IpAddress: " + e.getMessage(), (Throwable) e);
            return null;
        }
    }

    private static String getServerUrl(String ipAddress, int port) {
        if (port == 80) {
            return "http://" + ipAddress + "/";
        }
        if (ipAddress.contains(":")) {
            // IPv6
            int pos = ipAddress.indexOf("%");
            // java insists in adding %wlan and %p2p0 to everything
            if (pos > 0) {
                ipAddress = ipAddress.substring(0, pos);
            }
            return "http://[" + ipAddress + "]:" + port + "/";
        }
        return "http://" + ipAddress + ":" + port + "/";
    }


    public static void decodeIp(Context context, String scanedIp) {
        if (scanedIp.startsWith("nyServerIp")) {
            String bindingIP = scanedIp.split("-")[1];
            SharedPreferences sharedPreferences = context.getSharedPreferences("data", 0);
            sharedPreferences.edit().putString("ip", bindingIP).apply();
            Toast.makeText(context, "绑定成功到" + bindingIP, Toast.LENGTH_LONG).show();

        } else if (scanedIp.startsWith("nyMirrorIp")) {
            String url = scanedIp.split("-")[1];
            url = "http://" + url;
            Log.e(TAG, "URL request:--" + url);
            WifiShareUtil.openUrl(context, url);
        }
    }


    public static void clearBindingIps(Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", 0);
        sharedPreferences.edit().putString("ip", null).apply();
    }


    public static void openUrl(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
      /*  Bundle arguments = new Bundle();
        Fragment fragment = new Fragment_WebView();
        //   arguments.putString(FRAGMENT_TITLE_KEY, folder.getName());
        arguments.putString(URL_PATH_KEY, url);
        fragment.setArguments(arguments);
        setFragment(fragment);*/
    }

    public static void remoteShare(Context mContext, String preferredServerUrl) {
        // mFileUtil.copy_shareFilePath(this, trueUrl.getText().toString());
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, preferredServerUrl);
        mContext.startActivity(Intent.createChooser(i, "Smb File"));
    }

    public static String decodeUrl(String url) {
        if (url.toLowerCase().contains("%")) {
            try {
                return URLDecoder.decode(url, "UTF-8");

            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return url;
    }

    public static String encodeUrl(String url) {
        if (url.toLowerCase().contains("%")) {
            try {
                return URLEncoder.encode(url, "UTF-8");

            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return url;
    }


    public static void loadTextHtml(WebView webView, String html) {
        html = "-                                                    -" + html;
        //  nyWebView.loadUrl("javascript:(document.body.style.backgroundColor ='red');");
        //  nyWebView.loadUrl("javascript:(document.body.style.fontSize ='20pt');");
        //  nyWebView.loadUrl("javascript:(document.body.style.color ='yellow');");
        // The generated HTML
        String base64 = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
        webView.loadData(base64, "text/html", "base64");
    }

    /*
    public static void webShare(Context context, Uri uri, String linkUrl, Handler handler, int player) {
        webShare(context, uri, linkUrl, handler);
        switch (player) {
            case 0:
                MediaManipulate.ToNyPlayer(context, linkUrl);
                break;
            case 1:
                MediaManipulate.ToNyBrowser(context, linkUrl);
                break;
            case 2:
                MediaManipulate.ToVlcPlayer(context, linkUrl);
                break;
        }

    }


    public static void mediaHandle(Context context, String url, int player) {
        switch (player) {
            case 0:
                MediaManipulate.ToNyPlayer(context, url);
                break;
            case 1:
                MediaManipulate.ToNyBrowser(context, url);
                break;
            case 2:
                MediaManipulate.ToVlcPlayer(context, url);
                break;
        }

    }*/


}
