package com.nytaiji.nybase.libmediaStreamer;


import static com.nytaiji.nybase.utils.NyFileUtil.containZip;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;


import com.nytaiji.nybase.model.NyHybrid;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;

import javax.crypto.Cipher;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpEntityEnclosingRequest;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.MethodNotSupportedException;
import cz.msebera.android.httpclient.RequestLine;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.entity.AbstractHttpEntity;
import cz.msebera.android.httpclient.entity.InputStreamEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.DefaultBHttpServerConnection;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.protocol.HttpProcessor;
import cz.msebera.android.httpclient.protocol.HttpProcessorBuilder;
import cz.msebera.android.httpclient.protocol.HttpRequestHandler;
import cz.msebera.android.httpclient.protocol.HttpService;
import cz.msebera.android.httpclient.protocol.ResponseContent;
import cz.msebera.android.httpclient.protocol.UriHttpRequestHandlerMapper;
import cz.msebera.android.httpclient.util.EntityUtils;

/* https://libeasy.alwaysdata.net/libmedia/network/#server*/
public abstract class AbsHttpServer implements HttpServer {

    public Context context;

    /* renamed from: a */
    public DefaultBHttpServerConnection serverConnection;

    /* renamed from: a */
    private HttpService httpService;

    /* renamed from: a */
    private HttpSource dataSource;

    /* renamed from: a */
    private Thread thread;

    /* renamed from: a */
    private ServerSocket serverSocket;
    //  private boolean handleZipSource;

    /* renamed from: b */
    private boolean handleSmbSource;
    /* access modifiers changed from: private */

    /* renamed from: c */
    public boolean isRunning;

    /* renamed from: a */
    private final ServerHandler serverHandler;

    /* renamed from: e */
    public enum ServerType {
        SINGLEHTTPSERVER,
        WIFIHTTPSERVER;
    }

    /* renamed from: a */
    private static class TypeArray {

        /* renamed from: a */
        static final int[] serverTypeArray;

        /* renamed from: b */
        static final int[] proxyArray;

        static {
            int[] iArr = new int[Diagnostic.Code.values().length];
            proxyArray = iArr;
            try {
                iArr[Diagnostic.Code.PROXY.ordinal()] = 1;
            } catch (NoSuchFieldError ignored) {
            }
            int[] iArr2 = new int[ServerType.values().length];
            serverTypeArray = iArr2;
            try {
                iArr2[ServerType.SINGLEHTTPSERVER.ordinal()] = 1;
            } catch (NoSuchFieldError ignored) {
            }
            try {
                serverTypeArray[ServerType.WIFIHTTPSERVER.ordinal()] = 2;
            } catch (NoSuchFieldError ignored) {
            }
        }
    }


    //rename from a(e eVar) //
    AbsHttpServer(Context context, ServerType serverType) throws IOException {
        this.context = context.getApplicationContext();
        //TODO ny very important
        this.dataSource = new FileHttpSource(this.context);

        InetAddress byAddress;
        int ipAddress;
        int i = TypeArray.serverTypeArray[serverType.ordinal()];
        if (i == 1) {
            byAddress = InetAddress.getByAddress(new byte[]{Byte.MAX_VALUE, 0, 0, 1});
        } else if (i != 2) {
            byAddress = null;
        } else {
            if ((ipAddress = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress()) == 0) {
                byAddress = null;
            } else {
                byAddress = InetAddress.getByAddress(BigInteger.valueOf((long) (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN) ? Integer.reverseBytes(ipAddress) : ipAddress)).toByteArray());
            }
        }
        if (byAddress != null) {
            this.serverSocket = new ServerSocket(0, 1, byAddress);
            try {
                HttpProcessor build = HttpProcessorBuilder.create().add(new ResponseContent()).build();
                this.serverHandler = new ServerHandler();
                UriHttpRequestHandlerMapper uriHttpRequestHandlerMapper = new UriHttpRequestHandlerMapper();
                uriHttpRequestHandlerMapper.register("*", this.serverHandler);
                this.httpService = new HttpService(build, uriHttpRequestHandlerMapper);

                try {
                    Class.forName("jcifs.smb.SmbFile");
                    this.handleSmbSource = true;
                } catch (ClassNotFoundException e2) {
                    this.handleSmbSource = false;
                }

            } catch (NoClassDefFoundError e3) {
                if (e3.getMessage().contains("HttpProcessorBuilder")) {
                    NoClassDefFoundError noClassDefFoundError = new NoClassDefFoundError("Missing the HttpClient Library. Read the Integration Guidelines.");
                    StackTraceElement[] stackTrace = e3.getStackTrace();
                    noClassDefFoundError.setStackTrace(stackTrace.length > 2 ? new StackTraceElement[]{stackTrace[0], stackTrace[1], stackTrace[2]} : stackTrace);
                    throw noClassDefFoundError;
                }
                throw e3;
            }
        } else {
            throw new UnknownHostException("Unable to get host address");
        }
    }


    /* renamed from: a */
    private boolean isSmbLibAvailable() {
        if (this.handleSmbSource) {
            return true;
        }
        Log.e("SingleHttpServer", "Missing the jCIFS Library. Get it at http://jcifs.samba.org");
        return false;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: a */
    public boolean isAddressValid(InetAddress inetAddress) {
        return true;
    }

    @NonNull
    @Override
    public JsInterface getJsInterfaceObject() {
        return new ServerJsInterface();
    }


    //-------------class d-----------
    private class ServerJsInterface implements JsInterface {
        @JavascriptInterface
        public String getURL(final String path) {
            return AbsHttpServer.this.getURL(path);
        }

        @JavascriptInterface
        public String getURL(final String zipPath, final String entryPath, final String passWord) {
            return AbsHttpServer.this.getURL(zipPath, entryPath, passWord);
        }

        @JavascriptInterface
        public String getURL(final int mainVersion, final int patchVersion, final String entryPath) {
            return AbsHttpServer.this.getURL(mainVersion, patchVersion, entryPath);
        }
    }

    private long offset = -1L;

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @NonNull
    public HttpServer setCipher(Cipher cipher) {
        this.dataSource.setCipher(cipher);
        if (cipher != null) {
            Cipher c = cipher;
            String algorithm = cipher.getAlgorithm().toUpperCase();
            if ("AndroidOpenSSL".equalsIgnoreCase(c.getProvider().getName())) {
                int version = Build.VERSION.SDK_INT;
                if (18 <= version && version < 21 || algorithm.endsWith("PADDING") && !algorithm.endsWith("/NOPADDING")) {
                    Log.e("SingleHttpServer",
                            "With such a combination (Provider, Algorithm, OS Version), " +
                                    "you may encounter dysfunctions. Please consider the providing of a CipherFactory instead.");
                }
            }
        }

        return this;
    }

    @NonNull
    public HttpServer setCipherFactory(CipherFactory cipherFactory) {
        this.dataSource.setCipherFactory(cipherFactory);
        return this;
    }

    @NonNull
    @Override
    public HttpServer setDataSource(HttpSource httpSource) {
        if (httpSource != null) {
            this.dataSource = httpSource;
        }
        return this;
    }

    @Override
    public void start() {
        Thread thread = new Thread(new ServiceRunnalbe());
        this.thread = thread;
        thread.start();
    }

    @Override
    public void stop() {
        this.isRunning = false;
        Thread thread = this.thread;
        if (thread == null) {
            Log.e("SingleHttpServer", "Server is stopped without being started");
            return;
        }
        thread.interrupt();
        if (this.thread.isAlive()) {
            DefaultBHttpServerConnection defaultBHttpServerConnection = this.serverConnection;
            if (defaultBHttpServerConnection != null && defaultBHttpServerConnection.isOpen()) {
                try {
                    this.serverConnection.shutdown();
                } catch (IOException e) {
                    Log.e("SingleHttpServer", "Error while closing the client connection", e);
                }
                try {
                    this.thread.join(100);
                } catch (InterruptedException e2) {
                    Log.e("SingleHttpServer", "Interrupted while waiting for server stopping");
                }
            }
            if (!this.serverSocket.isClosed()) {
                try {
                    this.serverSocket.close();
                } catch (IOException e3) {
                    Log.e("SingleHttpServer", "Error while closing the server socket", e3);
                }
            }
        }
        try {
            this.thread.join(5000);
        } catch (InterruptedException e4) {
            Log.e("SingleHttpServer", "Interrupted while waiting for server stopping");
        }
        if (this.thread.isAlive()) {
            Log.e("SingleHttpServer", "Server still alive");
        }
        this.thread = null;
    }

    @Override
    public String getURL(String path) {
        if (isNetWork(path)) {
            path = "/" + path;
        }
        return this.setServerPath((String) path, null);
    }

    private boolean isNetWork(String path) {
        // Log.e(TAG, " sSpecialSource " + path);
        return path.toLowerCase().contains("http://")
                || path.contains("https://")
                || path.contains("asset://")
                || path.contains("ftp://")
                || path.contains("smb://")
                || path.contains("gdrive:")
                || path.contains("box:")
                || path.contains("dropbox:")
                || path.contains("onedrive:");
    }

    private NyHybrid nyHybrid = null;

    @Override
    public String getURL(NyHybrid nyHybrid) {
        this.nyHybrid = nyHybrid;
        String path = nyHybrid.getPath();
        if (isNetWork(path)) path = "/" + path;
        return setServerPath(path, null);
    }

    @Override
    public String getURL(DocumentFile documentFile) {
        return setServerPath("/" + documentFile.getUri().toString(), null);
    }

    @Nullable
    /* renamed from: a */
    private String setServerPath(String path, ArrayList<BasicNameValuePair> list) {
        try {
            return new URI("http", (String) null, this.serverSocket.getInetAddress().getHostAddress(), this.serverSocket.getLocalPort(), path, list != null ? URLEncodedUtils.format(list, "UTF-8") : null, (String) null).toASCIIString();
        } catch (URISyntaxException e) {
            Log.e("SingleHttpServer", "Unsupported URI syntax: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getURL(String path, String entry, String password) {
        ArrayList<BasicNameValuePair> arrayList = new ArrayList<>();
        arrayList.add(new BasicNameValuePair("p", password));
        arrayList.add(new BasicNameValuePair("e", entry));
        return setServerPath(path, arrayList);
    }

    @Override
    public String getURL(int mainversion, int patchversion, String entry) {
        ArrayList<BasicNameValuePair> arrayList = new ArrayList<>();
        arrayList.add(new BasicNameValuePair("m", String.valueOf(mainversion)));
        arrayList.add(new BasicNameValuePair("p", String.valueOf(patchversion)));
        arrayList.add(new BasicNameValuePair("e", entry));
        return setServerPath("/expansion", arrayList);
    }

    //---------------------------//
    //rename from class b
    private class ServerHandler implements HttpRequestHandler {
        private AbstractHttpEntity httpEntity;

        public void handle(HttpRequest httpRequest, final HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            Label_0711:
            {
                if (!AbsHttpServer.this.serverSocket.getInetAddress().isLoopbackAddress()) {
                    final AbsHttpServer absHttpServer = AbsHttpServer.this;
                    if (!absHttpServer.isAddressValid(absHttpServer.serverConnection.getRemoteAddress())) {
                        httpResponse.setStatusCode(403);
                        this.httpEntity = (AbstractHttpEntity) new StringEntity("Not the expected client", "UTF-8");
                        break Label_0711;
                    }
                }
                final RequestLine requestLine;
                final String method;
                if (!(method = (requestLine = httpRequest.getRequestLine()).getMethod()).toUpperCase(Locale.US).equals("GET")) {
                    Log.e("SingleHttpServer", "Unsupported method: " + method);
                    throw new MethodNotSupportedException(method + " method not supported");
                }

                //TODO ny 2023-4-15
                String url = requestLine.getUri().replace("file:///", "/");

                //   nyHybrid = new NyHybrid(url);
                final URI uri = URI.create(url);

                Log.e("AbsServer", "requestLine.getUri() =========== " + uri.getPath());

                //TODO ny fixed the thread interrupted exception for cloud storage
                if (requestLine.getUri().equals("/favicon.ico")) return;
                //   Log.e("AbsServer", "uri=============== " + uri.getPath());


                long offset = AbsHttpServer.this.offset;

                if (!containZip(requestLine.getUri())) {
                    final Header firstHeader;
                    final String value;
                    if ((firstHeader = httpRequest.getFirstHeader("Range")) != null && (value = firstHeader.getValue()) != null) {
                        final int index = value.indexOf(61);
                        final int index2 = value.indexOf(45);
                        if (index > 0 && index2 > 0) {
                            offset = offset + Long.parseLong(value.substring(index + 1, index2));
                        }
                    }
                }

                //  Log.e("AbsServer", "offset========== " + offset);
                int bn = (Boolean) httpContext.getAttribute("a.a") ? 1 : 0;

                if (httpRequest instanceof HttpEntityEnclosingRequest) {
                    EntityUtils.toByteArray(((HttpEntityEnclosingRequest) httpRequest).getEntity());
                }

                //
                //  Log.e("SingleHttpServer", "nyHybrid " + nyHybrid.getPath());
                if (nyHybrid != null) AbsHttpServer.this.dataSource.setSource(nyHybrid, offset);
                else AbsHttpServer.this.dataSource.setSource(uri, offset);

                if (!AbsHttpServer.this.dataSource.isExisting()) {
                    Log.e("SingleHttpServer", "dataSource not Existing ");
                    httpResponse.setStatusCode(404);
                    this.httpEntity = (AbstractHttpEntity) new StringEntity("Resource " + AbsHttpServer.this.dataSource.getUriString() + " not found", "UTF-8");
                } else if (!AbsHttpServer.this.dataSource.isReadable()) {
                    Log.e("SingleHttpServer", "dataSource not readable ");
                    httpResponse.setStatusCode(403);
                    this.httpEntity = (AbstractHttpEntity) new StringEntity("Resource " + AbsHttpServer.this.dataSource.getUriString() + " cannot be read", "UTF-8");
                } else {
                    InputStream inputStream = null;
                    try {
                        inputStream = AbsHttpServer.this.dataSource.getInputStream();
                        if (inputStream != null) Log.e("SingleHttpServer", "inputStream success");
                    } catch (IOException ignored) {
                        Log.e("SingleHttpServer", "inputStream fails ");
                    }
                    if (inputStream == null) {
                        httpResponse.setStatusCode(500);
                        this.httpEntity = (AbstractHttpEntity) new StringEntity("Resource " + AbsHttpServer.this.dataSource.getUriString() + " cannot be streamed", "UTF-8");
                    } else if (bn == 0 && rnd() == 3) {
                        httpResponse.setStatusCode(500);
                        this.httpEntity = (AbstractHttpEntity) new StringEntity("");
                    } else {
                        int statusCode;
                        if (AbsHttpServer.this.dataSource.isPartial()) {
                            statusCode = 206;
                        } else {
                            statusCode = 200;
                        }
                        httpResponse.setStatusCode(statusCode);
                        if (AbsHttpServer.this.dataSource.isPartial()) {
                            Log.e("SingleHttpServer", "dataSource.isPartial() ");
                            final long contentSize;
                            final long n = contentSize = AbsHttpServer.this.dataSource.getContentSize();
                            final String s = "Content-Range";
                            final StringBuilder append = new StringBuilder().append("bytes ").append(AbsHttpServer.this.dataSource.getOffset()).append("-");
                            String string;
                            if (n > 0L) {
                                final StringBuilder sb = new StringBuilder();
                                new StringBuilder();
                                string = sb.append(contentSize - 1L).append("/").append(contentSize).toString();
                            } else {
                                string = "";
                            }
                            httpResponse.addHeader(s, append.append(string).toString());
                        }
                        this.httpEntity = (AbstractHttpEntity) new InputStreamEntity(inputStream, AbsHttpServer.this.dataSource.getContentLength());
                        final String contentType;
                        if ((contentType = AbsHttpServer.this.dataSource.getContentType()) != null) {
                            Log.e("SingleHttpServer", "contentType= " + contentType);
                            this.httpEntity.setContentType(contentType);
                        }
                    }
                }
            }
            httpResponse.setEntity((HttpEntity) this.httpEntity);
        }

        //rename from a()
        void dismiss() throws IOException {
            final AbstractHttpEntity httpEntity;
            if ((httpEntity = this.httpEntity) != null) {
                httpEntity.getContent().close();
                this.httpEntity = null;
            }
        }
    }

    public static int rnd() {
        return (int) Math.floor(Math.random() * 10.0d);
    }

    //---from class c----

    private class ServiceRunnalbe implements Runnable {

        private ServiceRunnalbe() {
        }

        public void run() {
            AbsHttpServer.this.isRunning = true;
            //TODO ny
            String attribute = "a.a";
            boolean isSucceessfull;

            while (AbsHttpServer.this.isRunning && !AbsHttpServer.this.serverSocket.isClosed()) {
                IOException ioException;
                IOException exception;

                label738:
                {
                    Socket socket;
                    try {
                        socket = AbsHttpServer.this.serverSocket.accept();
                    } catch (SocketException e) {
                        continue;
                    } catch (IOException e) {
                        ioException = e;
                        break label738;
                    }

                    //   Socket socket1 = socket;

                    AbsHttpServer.this.serverConnection = new DefaultBHttpServerConnection(8192);

                    BasicHttpContext basicHttpContext;
                    try {
                        AbsHttpServer.this.serverConnection.bind(socket);
                        basicHttpContext = new BasicHttpContext();
                    } catch (SocketException e) {
                        continue;
                    } catch (IOException e) {
                        ioException = e;
                        break label738;
                    }

                    BasicHttpContext basicHttpContext1 = basicHttpContext;

                    isSucceessfull = false;


                    basicHttpContext.setAttribute(attribute, isSucceessfull);

                    while (true) {

                        if (!AbsHttpServer.this.isRunning) {
                            break;
                        }


                        if (!AbsHttpServer.this.serverConnection.isOpen()) {
                            break;
                        }

                        boolean success = false;

                        label740:
                        {
                            IOException exception1;
                            label607:
                            {
                                label606:
                                {
                                    label605:
                                    {
                                        label604:
                                        {
                                            label603:
                                            {
                                                try {
                                                    success = true;
                                                    AbsHttpServer.this.httpService.handleRequest(AbsHttpServer.this.serverConnection, basicHttpContext1);
                                                    success = false;
                                                    break label606;
                                                } catch (SocketException e) {
                                                    success = false;
                                                    break label605;
                                                } catch (IllegalStateException e) {
                                                    success = false;
                                                    break label604;
                                                } catch (HttpException e) {
                                                    success = false;
                                                } catch (IOException e) {
                                                    success = false;
                                                    break label603;
                                                } finally {
                                                    if (success) {
                                                        try {
                                                            AbsHttpServer.this.serverHandler.dismiss();
                                                        } catch (IOException e) {
                                                            exception1 = e;
                                                            Log.e("SingleHttpServer", "Error while releasing the handler resources", exception1);
                                                        }
                                                    }
                                                }

                                                try {
                                                    AbsHttpServer.this.serverHandler.dismiss();
                                                    continue;
                                                } catch (IOException e) {
                                                    exception1 = e;
                                                    break label607;
                                                }
                                            }

                                            try {
                                                AbsHttpServer.this.serverHandler.dismiss();
                                                break;
                                            } catch (IOException e) {
                                                exception = e;
                                                break label740;
                                            }
                                        }

                                        try {
                                            AbsHttpServer.this.serverHandler.dismiss();
                                            break;
                                        } catch (IOException e) {
                                            exception = e;
                                            break label740;
                                        }
                                    }

                                    try {
                                        AbsHttpServer.this.serverHandler.dismiss();
                                        break;
                                    } catch (IOException e) {
                                        exception = e;
                                        break label740;
                                    }
                                }

                                try {
                                    AbsHttpServer.this.serverHandler.dismiss();
                                    continue;
                                } catch (IOException e) {
                                    exception1 = e;
                                }
                            }

                            Log.e("SingleHttpServer", "Error while releasing the handler resources", exception1);
                            continue;
                        }

                        Log.e("SingleHttpServer", "Error while releasing the handler resources", exception);
                        break;
                    }

                    try {
                        AbsHttpServer.this.serverConnection.shutdown();
                        continue;
                    } catch (IOException var67) {
                        exception = var67;
                    }

                    Log.e("SingleHttpServer", "Error while closing the client socket", exception);
                    continue;
                }

                exception = ioException;
                Log.e("SingleHttpServer", "Error while waiting for client connection", exception);
            }

            if (!AbsHttpServer.this.serverSocket.isClosed() && AbsHttpServer.this.serverConnection != null) {
                try {
                    AbsHttpServer.this.serverConnection.close();
                } catch (IOException e) {
                    Log.e("SingleHttpServer", "Error while closing the server socket", e);
                }
            }

        }
    }
}
