package com.nytaiji.nybase.libmediaStreamer;

import static com.nytaiji.nybase.crypt.EncryptUtil.getPasswordFromFileName;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.model.Constants;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;

/* renamed from: dev.dworks.apps.anexplorer.http.FileDataSource */
public class FileHttpSource implements HttpSource {

    static String TAG = "FileHttpSource";

    Context context;

    /* renamed from: a */
    private long offset;

    /* renamed from: a */
    private AssetFileDescriptor assetFileDescriptor;

    /* renamed from: a */
    private DocumentFile documentFile;

    /* renamed from: a */
    private ZipEntry zipEntry;

    /* renamed from: a */
    private String entryName;

    /* renamed from: a */
    private String zipPass;

    /* renamed from: a */
    private CipherFactory cipherFactory;

    /* renamed from: a */
    private RemoteFile remoteFile;

    /* renamed from: a */
    private SourceType.Source sourceType;

    /* renamed from: a */
    private File file;

    /* renamed from: a */
    private URI uri;

    /* renamed from: a */
    private Cipher cipher;


    // private SmbFile smbFile;

    private boolean blockCipher;
    /* access modifiers changed from: private */

    public boolean randomCipher;

    /* renamed from: c */
    private boolean cipherCTR;

    /* renamed from: d */
    private boolean cipherECB;

    private boolean isPartial;

    //TODO ny
    private String path;

    private NyHybrid nyHybrid = null;

    private FileHeader fileHeader;

    FileHttpSource(Context context) {
        this.context = context;
    }


    @Override
    public long getContentLength() {
        long contentSize = getContentSize();
        if (contentSize != -1) {
            return contentSize - this.offset;
        }
        return -1;
    }

    @Override
    public long getContentSize() {
        if (this.blockCipher) {
            return -1;
        }
        switch (this.sourceType.ordinal()) {
            case 1:
                return this.file.length();
            case 2:
                DocumentFile documentFile = this.documentFile;
                if (documentFile != null) {
                    return documentFile.length();
                }
                return -1;
            case 3:
                if (this.fileHeader != null) {
                    return fileHeader.getUncompressedSize();
                }
                return -1;
            case 4:
                //TODO ny
                return -1;
               /* try {
                    if (this.smbFile != null) {
                        return this.smbFile.length();
                    }
                    return -1;
                } catch (Exception e) {
                    Log.e(TAG, "Unable to get the length of the resource: " + e.getMessage());
                    return -1;
                }*/
            case 5:
                AssetFileDescriptor assetFileDescriptor = this.assetFileDescriptor;
                if (assetFileDescriptor != null) {
                    return assetFileDescriptor.getLength();
                }
                return -1;
            case 6:
                return this.remoteFile.getLength();

            case 7:
                if (this.nyHybrid != null)
                    return this.nyHybrid.length(context);
            default:
                return -1;
        }
    }

    @Override
    public long getOffset() {
        return this.offset;
    }

    @Override
    public String getContentType() {
        String fname = null;

        switch (this.sourceType.ordinal()) {
            case 1: {
                fname = this.file.getName();
                break;
            }

            case 2: {
                final DocumentFile documentFile;
                if ((documentFile = this.documentFile) != null)
                    return documentFile.getType();
                break;
            }

            case 3: {
                if (this.fileHeader != null)
                    fname = this.fileHeader.getFileName();
                break;
            }

            case 4: {
              /*  final SmbFile smbFile;
                if ((smbFile = this.smbFile) != null)
                    fname = smbFile.getName();*/
                break;
            }

            case 5: {
                if (this.assetFileDescriptor != null)
                    fname = new File(String.valueOf(this.assetFileDescriptor)).getName();
                break;
            }

            case 6: {
                String scheme = this.remoteFile.getScheme();
                if (scheme != null) {
                    return scheme;
                }
                fname = this.remoteFile.path;
                break;
            }

            case 7: {
                if (this.nyHybrid != null)
                    fname = this.nyHybrid.getName(context);
                break;
            }
        }

        if (fname == null) {
            return null;
        }

        final String guessContentTypeFromName = URLConnection.guessContentTypeFromName(fname);
        if ("text/texmacs".equals(guessContentTypeFromName)) {
            return "application/octet-stream";
        }
        return guessContentTypeFromName;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        long length = 0L;
        InputStream inputStream = null;

        switch (this.sourceType.ordinal()) {

            case 1: {
                inputStream = new FileInputStream(file);
                path = file.getPath();
            }

            case 2: {
                if (this.documentFile != null) {
                    path = this.documentFile.getName();
                    inputStream = context.getContentResolver().openInputStream(this.documentFile.getUri());
                }
                break;
            }

            case 3: {
                if (this.fileHeader != null) {
                    try {
                        ZipFile zipFile = new ZipFile(path);
                        //
                        if (this.zipPass != null) zipFile.setPassword(this.zipPass.toCharArray());
                        inputStream = zipFile.getInputStream(this.fileHeader);
                        path = this.fileHeader.getFileName();
                        if (inputStream == null) Log.e(TAG, "ZipFile inputstream fail ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else Log.e(TAG, "ZipEntry is null");
                break;
            }

            case 4: {
               /* final SmbFile smbFile;
                if ((smbFile = this.smbFile) != null) {
                    inputStream = smbFile.getInputStream();
                }*/
                break;
            }

            case 5: {
                final AssetFileDescriptor assetFileDescriptor;
                if ((assetFileDescriptor = this.assetFileDescriptor) != null) {
                    inputStream = assetFileDescriptor.createInputStream();
                    path = this.assetFileDescriptor.getFileDescriptor().toString();
                }
                break;
            }

            case 6: {
                inputStream = this.remoteFile.getInputStream();
                length = this.remoteFile.getLength();
                path = this.remoteFile.getPath();
                if (this.remoteFile.getCipher() != null)
                    cipher = this.remoteFile.getCipher();

                break;
            }

            case 7: {
                inputStream = this.nyHybrid.getInputStream(context);
                path = this.nyHybrid.getPath();
                break;
            }

        }

        if (inputStream == null) {
            return null;
        }

        //TODO ny
        InputStream stream = inputStream;
        if (getPasswordFromFileName(path)!=null && Integer.parseInt(getPasswordFromFileName(path)) > 5)
            inputStream.skip(Constants.ENCRYPT_SKIP);

        if (cipher != null) {
            final EncryptInputStream encryptInputStream = new EncryptInputStream(inputStream, cipher);
            encryptInputStream.setBoolean(this.cipherECB, this.cipherCTR);
            final CipherFactory cipherFactory = this.cipherFactory;
            if (cipherFactory != null && this.cipherECB) {
                encryptInputStream.setCipherFactory(cipherFactory);
            }
            stream = (InputStream) encryptInputStream;
        }

        //  if (cipher != null) stream = (InputStream) new NyCipherInputStream(inputStream, cipher);

        final long lng;
        if ((lng = this.offset - length) != 0L) {
            long skip;
            long lng2;
            long n2;
            // for (long n = lng; (n2 = lcmp(lng2 = n - (skip = c2.skip(n)), 0L)) > 0 && skip > 0L; n = lng2) { }
            for (long n = lng; (n2 = (skip = (lng2 = n - (n = stream.skip(n)))) == 0L ? 0 : (skip < 0L ? -1 : 1)) > 0 && n > 0L; n = lng2) {
            }
            if (n2 > 0) {
                Log.e(TAG, "missing " + lng2 + " of the " + lng + " bytes to skip");
                throw new IOException("Unable to skip enough");
            }
        }

        return stream;
    }

    @Override
    public String getUriString() {
        URI uri = this.uri;
        if (uri != null) {
            return uri.toString();
        }
        return null;
    }

    @Override
    public boolean isPartial() {
        return this.offset != 0;
    }

    @Override
    public boolean isReadable() {
        switch (this.sourceType.ordinal()) {
            case 1:
                return this.file.canRead() && this.file.isFile() && !this.file.isHidden();

            case 2:
                DocumentFile documentFile = this.documentFile;
                return documentFile != null && documentFile.canRead() && this.documentFile.isFile();

            case 3:
                return this.fileHeader != null;

            case 4:
             /*   try {
                    return this.smbFile != null && this.smbFile.canRead() && this.smbFile.isFile() && !this.smbFile.isHidden();
                } catch (Exception e) {
                    Log.e(TAG, "Unable to test the readability of the resource: " + e.getMessage());
                    return false;
                }*/

            case 5:
                return this.assetFileDescriptor != null;

            case 6:
                return this.remoteFile.isLocal();

            case 7:
                //TODO ny
                return this.nyHybrid.getPath() != null;

            default:
                return false;
        }
    }

    @Override
    public boolean isExisting() {

        switch (this.sourceType.ordinal()) {
            case 1:
                return this.file.exists();

            case 2:
                DocumentFile documentFile = this.documentFile;
                return documentFile != null && documentFile.exists();
            case 3:
                return this.fileHeader != null;

            case 4:
              /*  try {
                    return this.smbFile != null && this.smbFile.exists();
                } catch (Exception e) {
                    Log.e(TAG, "Unable to test the existence of the resource: " + e.getMessage());
                    return false;
                }*/

            case 5:
                return this.assetFileDescriptor != null;

            case 6:
                return this.remoteFile.isValidOnline();

            case 7:
                // if (this.nyHybrid != null) return this.nyHybrid.getPath().contains("zip") || this.nyHybrid.exists();
                return nyHybrid != null;
            default: //uri
                return uri != null;
        }
    }

    /*
    you can use a block algorithm and select a mode that does not require padding to effectively use a block cipher as a stream cipher.
        Here are some examples:

        AES/CFB/NoPadding
        AES/CTR/NoPadding
        AES/CTS/NoPadding
        AES/CBC/WithCTS
        AES/OFB/NoPadding
    */
    @Override
    public void setCipher(Cipher cipher) {
        boolean blockCipher;
        boolean isCTR;
        boolean isECB = true;
        this.cipher = cipher;
        if (cipher == null) {
            this.blockCipher = false;
            return;
        }
        String algorithm = cipher.getAlgorithm();
        if (algorithm != null) {
            algorithm = algorithm.toUpperCase(Locale.US);
        }

        blockCipher = this.cipher.getBlockSize() != 0 && algorithm != null && !algorithm.contains("/CFB") && !algorithm.contains("/OFB") && !algorithm.contains("/CTR") && !algorithm.contains("/CTS") && !algorithm.contains("/WITHCTS");
        this.blockCipher = blockCipher;

        this.randomCipher = algorithm != null && (algorithm.contains("/CTR") || algorithm.contains("/CBC") || algorithm.contains("/CFB") || algorithm.contains("/ECB"));

        isCTR = algorithm != null && algorithm.contains("/CTR");
        this.cipherCTR = isCTR;

        if (algorithm == null || !this.randomCipher || algorithm.contains("/ECB")) {
            isECB = false;
        }
        this.cipherECB = isECB;
    }

    @Override
    public void setCipherFactory(CipherFactory cipherFactory) {
        this.cipherFactory = cipherFactory;
    }

    @Override
    public void setSource(NyHybrid nyHybrid, long optOffset) {

        this.isPartial = optOffset >= 0L;
        if (optOffset == -1L) {
            optOffset = 0L;
        }
        this.offset = optOffset;

        this.nyHybrid = nyHybrid;
        this.sourceType = SourceType.Source.HYBRIDFILE;
        // this.uri = URI.create(hybridFile.getPath());

        CipherFactory cipherFactory = this.cipherFactory;
        if (cipherFactory != null && this.cipher == null) {
            try {
                setCipher(cipherFactory.getCipher());
            } catch (GeneralSecurityException e) {
                Log.e(TAG, "Unable to get an initial cipher: " + e.getMessage());
            }
        }
    }


    @Override
    public void setSource(URI uri, long optOffset) {
        this.uri = uri;

        this.isPartial = optOffset >= 0L;

        if (optOffset == -1L) {
            optOffset = 0L;
        }

        this.offset = optOffset;
        this.path = uri.getPath();
        Log.e(TAG, " setSource Uri     " + this.path);
        List<NameValuePair> parse = URLEncodedUtils.parse(uri, "UTF-8");
        HashMap<String, String> hashMap = new HashMap<>(parse.size());
        for (NameValuePair nameValuePair : parse) {
            hashMap.put(nameValuePair.getName(), nameValuePair.getValue());
        }
        this.sourceType = SourceType.Source.DEFAULT;
        this.file = null;
        this.documentFile = null;
        this.entryName = (String) hashMap.get("e");
        this.zipPass = (String) hashMap.get("p");
        this.zipEntry = null;

        // this.smbFile = null;
        this.assetFileDescriptor = null;
        this.remoteFile = null;

        CipherFactory cipherFactory = this.cipherFactory;
        if (this.cipher == null && cipherFactory != null) {
            try {
                setCipher(cipherFactory.getCipher());
            } catch (GeneralSecurityException e) {
                Log.e(TAG, "Unable to get an initial cipher: " + e.getMessage());
            }
        }

        if (isNetWorkSource(path)) {
            this.sourceType = SourceType.Source.HYBRIDFILE;
      /*  } else if (path.startsWith("/smb://")) {
            this.sourceType = SourceType.Source.SMB;
            try {
                this.smbFile = new SmbFile(path.substring(1));
            } catch (MalformedURLException e4) {
                Log.e(TAG, "Unable to construct the resource: " + e4.getMessage());
            }*/
        } else if (path.startsWith("/content://")) {
            this.sourceType = SourceType.Source.DOCUMENT_FILE;
            try {
                this.documentFile = DocumentFile.fromSingleUri(context, Uri.parse(path.substring(1)));
            } catch (IllegalArgumentException e5) {
                Log.e(TAG, "Unable to construct the Document File: " + e5.getMessage());
            }
        } else if (path.startsWith("/asset://")) {
            this.sourceType = SourceType.Source.ASSET_FILE;
            if (context != null) {
                this.entryName = path.substring(9);
                try {
                    this.assetFileDescriptor = context.getAssets().openFd(this.entryName);
                } catch (IOException e6) {
                    Log.e(TAG, "Unable to open the Asset File: " + e6.getMessage());
                }
            }
        } else if (path.startsWith("/http://") || path.startsWith("/https://") || path.startsWith("/ftp://")) {
            this.sourceType = SourceType.Source.REMOTE;
            RemoteFile remoteFile = new RemoteFile(this, path.substring(1));
            this.remoteFile = remoteFile;
            try {
                remoteFile.init(this.cipher, this.offset, this.cipherFactory, this.randomCipher, this.cipherECB, this.cipherCTR);
            } catch (Exception e7) {
                Log.e(TAG, "Unable to construct the resource: " + e7.getMessage());
            }
        } else if (this.entryName != null) {
            this.sourceType = SourceType.Source.ZIP_ENTRY;
            try {
                // Log.e(TAG, "Zip path" + path);
                ZipFile zipFile = new ZipFile(path);
                if (this.zipPass.length() > 0)
                    zipFile.setPassword(this.zipPass.toCharArray());
                //TODO ny to handle with Chinese characters
                List<FileHeader> headerList = zipFile.getFileHeaders();
                for (FileHeader fileHeader : headerList) {
                    if (!fileHeader.isDirectory()
                            && fileHeader.getFileName().contains(this.entryName)) {
                        Log.e(TAG, "fileHeader.getFileName() = " + fileHeader.getFileName());
                        this.fileHeader = fileHeader;
                    }
                }
              /*  ZipEntry zipEntry = zipFile.getEntry("/" + decodeUrl(entryName));
                if (zipEntry == null) zipEntry = zipFile.getEntry("/" + entryName);
                if (zipEntry == null) zipEntry = zipFile.getEntry(decodeUrl(entryName));
                if (zipEntry == null) zipEntry = zipFile.getEntry(entryName);
                this.zipEntry = zipEntry;*/
            } catch (IOException e8) {
                Log.e(TAG, "Unable to open the Zip Expansion File: " + e8.getMessage());
            }
        } else {
            //DEFAULT for FILE
            this.sourceType = SourceType.Source.FILE;
            this.file = new File(path);
        }
    }

    private boolean isNetWorkSource(String path) {
        // Log.e(TAG, " sSpecialSource " + path);
        return path.toLowerCase().contains("gdrive:")
                || path.contains("smb:")
                || path.contains("box:")
                || path.contains("dropbox:")
                || path.contains("onedrive:");
    }

    private String decodeUrl(String url) {
        if (url.toLowerCase().contains("%")) {
            try {
                return URLDecoder.decode(url, "UTF-8");

            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return url;
    }


//-------------------------------------------------------------------------------//

//-------------class b---------
    /* renamed from: dev.dworks.apps.anexplorer.http.FileDataSource$b */

    private static class CipherProcess {

        /* renamed from: a */
        private final int intA;

        /* renamed from: b */
        private int intB;

        /* renamed from: a */
        private final long longA;

        /* renamed from: a */
        private final boolean blA;

        /* renamed from: b */
        private final boolean blB;

        /* renamed from: a */
        private final byte[] byteA;


        //   EncrypteDataSource(Cipher cipher, long j, boolean z, boolean z2) {
        CipherProcess(Cipher cipher, long j, boolean z, boolean z2) {
            this.intA = cipher.getBlockSize();
            this.byteA = cipher.getIV();
            this.blA = z;
            this.blB = z2;
            this.longA = j / ((long) this.intA);
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public long getLength() {
            long j = this.longA;
            if (this.blA && !this.blB && j != 0) {
                j--;
            }
            return j * ((long) this.intA);
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public Cipher getCipher(InputStream inputStream, CipherFactory cipherFactory) throws IOException, GeneralSecurityException {
            if (this.blA) {
                long j = this.longA;
                if (j != 0) {
                    int i = this.intA;
                    byte[] bArr = new byte[i];
                    if (this.blB) {
                        System.arraycopy(this.byteA, 0, bArr, 0, i);
                        int i2 = i - 1;
                        while (true) {
                            int i3 = ((int) (255 & j)) + (bArr[i2] & 255);
                            int i4 = i2 - 1;
                            bArr[i2] = (byte) i3;
                            if ((i3 >> 8) > 0) {
                                for (int i5 = i4; i5 >= 0; i5--) {
                                    byte b = (byte) (bArr[i5] + 1);
                                    bArr[i5] = b;
                                    if (b != 0) {
                                        break;
                                    }
                                }
                            }
                            j >>= 8;
                            if (j <= 0 || i4 < 0) {
                                break;
                            }
                            i2 = i4;
                        }
                    } else {
                        this.intB = inputStream.read(bArr);
                    }
                    return cipherFactory.rebaseCipher(bArr);
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public int getIntB() {
            return this.intB;
        }
    }


    //   e
    public enum SourceScheme {
        ONLINE,
        LOCAL;
    }

    /* renamed from: dev.dworks.apps.anexplorer.http.FileDataSource$d */
    private static class RemoteFile {

        /* renamed from: a */
        private int response = -1;

        /* renamed from: a */
        private long fileLength = 0;

        /* renamed from: a */
        private SourceScheme sourceScheme;

        /* renamed from: a */
        final FileHttpSource dataSource;

        /* renamed from: a */
        private InputStream inputStream;

        /* renamed from: a */
        private Cipher cipher;

        /* renamed from: b */
        private long available = -1;

        /* renamed from: a */
        private final String path;

        /* renamed from: b */
        private String scheme;

        RemoteFile(FileHttpSource fileDataSource, String path) {
            this.dataSource = fileDataSource;
            this.path = path;
        }

        // public void a(javax.crypto.Cipher r11, long r12, dev.dworks.apps.anexplorer.http.CipherFactory r14, boolean r15, boolean r16, boolean r17) throws java.io.IOException, java.security.GeneralSecurityException {
        public void init(Cipher cipher, final long offset, final CipherFactory cipherFactory,
                         final boolean randomCipher, final boolean cipherECB, final boolean cipherCTR) throws IOException, GeneralSecurityException {
            CipherProcess cipherProcess = null;
            final URL url = new URL(path);
            if (url.getProtocol().startsWith("http")) {
                this.sourceScheme = SourceScheme.ONLINE;
            } else {
                this.sourceScheme = SourceScheme.LOCAL;
            }
            if (SourceScheme.ONLINE == this.sourceScheme) {
                if (cipher != null) {
                    if (randomCipher && (cipherFactory != null || !cipherECB) && fileLength != 0L) {
                        this.response = (cipherProcess = new CipherProcess((Cipher) cipher, offset, cipherECB, cipherCTR)).getIntB();
                    }
                } else {
                    this.available = offset;
                }
            }
            URLConnection connection;
            if (SourceScheme.LOCAL == this.sourceScheme && url.getFile().indexOf(32) != -1) {
                connection = url.openConnection(Proxy.NO_PROXY);
            } else {
                connection = url.openConnection();
            }
            if (this.fileLength != 0L) {
                connection.setRequestProperty("Range", "bytes=" + this.fileLength + "-");
            }
            Label_0354:
            {
                try {
                    this.inputStream = new BufferedInputStream(connection.getInputStream());
                } catch (FileNotFoundException ex4) {
                    Log.w(TAG, "Remote response: " + connection.getHeaderField((String) null));
                } catch (IOException ex1) {
                    final String message = ex1.getMessage();
                    if (SourceScheme.LOCAL == this.sourceScheme && message != null && message.contains("Unable to retrieve file: ")) {
                        try {
                            this.response = Integer.parseInt(message.substring(message.length() - 3));
                            Log.e(TAG, "Remote reply: " + this.response);
                            break Label_0354;
                        } catch (NumberFormatException ex3) {
                            Log.e(TAG, "Unable to parse: " + message);
                            throw ex1;
                        }
                    }
                    throw ex1;
                }
            }
            if (connection instanceof HttpURLConnection) {
                this.response = ((HttpURLConnection) connection).getResponseCode();
            }
            if (this.fileLength != 0L && this.response == 200) {
                Log.w(TAG, "Range header not supported by the remote server");
                this.fileLength = 0L;
            } else if (cipherProcess != null) {
                this.cipher = cipherProcess.getCipher(this.inputStream, cipherFactory);
                this.fileLength += cipherProcess.getLength();
            }
            final int a3;
            if ((a3 = this.response) == 200) {
                this.available = connection.getContentLength();
            } else {
                final String headerField;
                final int lastIndex;
                if (a3 == 206 && (headerField = connection.getHeaderField("Content-Range")) != null && (lastIndex = headerField.lastIndexOf(47)) != -1) {
                    final String substring = headerField.substring(lastIndex);
                    try {
                        this.available = Long.parseLong(substring);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if ("content/unknown".equals(this.scheme = connection.getContentType())) {
                this.scheme = null;
            }
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public boolean isLocal() {
            if (this.sourceScheme.ordinal() != 1) {
                return true;
            }
            int i = this.response;
            return i != 401 && i != 403 && i != 407;
        }

        /* access modifiers changed from: package-private */
        /* renamed from: b */
        public boolean isValidOnline() {
            int i = this.sourceScheme.ordinal();
            if (i == 1) {
                int response = this.response;
                return response != 404 && response != 410;
            } else return i != 2 || this.response != 550;
        }

        /* access modifiers changed from: package-private */
        /* renamed from: b */
        public long getAvailable() {
            return this.available;
        }

        /* access modifiers changed from: package-private */
        /* renamed from: b */
        public String getPath() {
            return this.path;
        }


        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public InputStream getInputStream() {
            return this.inputStream;
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public long getLength() {
            return this.fileLength;
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public Cipher getCipher() {
            return this.cipher;
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public String getScheme() {
            return this.scheme;
        }

    }

    /* renamed from: dev.dworks.apps.anexplorer.http.FileDataSource$c */
    public class EncryptInputStream extends FilterInputStream {

        /* renamed from: a */
        private int intA;

        /* renamed from: b */
        private int intB;

        /* renamed from: a */
        private CipherFactory cipherFactory;

        /* renamed from: a */
        //  final FileDataSource fileDataSource;

        /* renamed from: a */
        private Cipher cipher;

        /* renamed from: a */
        private final byte[] byteA;

        /* renamed from: b */
        private byte[] byteB;

        /* renamed from: a */
        private boolean blA;

        /* renamed from: b */
        private boolean cipherECB;

        /* renamed from: c */
        private boolean cipherCTR;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        EncryptInputStream(InputStream inputStream, Cipher cipher) {
            super(inputStream);
            // this.fileDataSource = fileDataSource;
            this.cipher = cipher;
            int blockSize = cipher.getBlockSize();
            int max = Math.max(blockSize, 1);
            int max2 = Math.max(max, (4096 / max) * max);
            this.byteA = new byte[max2];

            this.byteB = new byte[((blockSize > 0 ? blockSize * 2 : 0) + max2)];
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */

        public void setBoolean(boolean cipherECB, boolean cipherCTR) {
            this.cipherECB = cipherECB;
            this.cipherCTR = cipherCTR;
        }

        @Override
        public int available() throws IOException {
            return this.intB - this.intA;
        }

        @Override
        public void close() throws IOException {
            super.in.close();
            try {
                this.cipher.doFinal();
            } catch (GeneralSecurityException ignored) {
                //TODO ny
                if (this.cipher.equals(FileHttpSource.this.cipher)) {
                    FileHttpSource.this.cipher = null;
                }
                if (this.cipherFactory.equals(FileHttpSource.this.cipherFactory)) {
                    FileHttpSource.this.cipherFactory = null;
                }
            }
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read() throws IOException {
            if (this.intA == this.intB && !m1a()) {
                return -1;
            }
            byte[] bArr = this.byteB;
            int i = this.intA;
            this.intA = i + 1;
            return bArr[i] & 255;
        }

        @Override
        public int read(@NonNull byte[] bArr, int i, int i2) throws IOException {
            int i3 = 0;
            while (i3 < i2) {
                if (this.intA != this.intB || m1a()) {
                    int min = Math.min(i2 - i3, this.intB - this.intA);
                    System.arraycopy(this.byteB, this.intA, bArr, i, min);
                    i += min;
                    this.intA += min;
                    i3 += min;
                } else if (i3 == 0) {
                    return -1;
                } else {
                    return i3;
                }
            }
            return i3;
        }


        @Override
        public long skip(final long l) throws IOException {
            if (FileHttpSource.this.randomCipher && (!this.cipherECB || this.cipher != null)) {
                final CipherProcess cipherProcess = new CipherProcess(this.cipher, l, this.cipherECB, this.cipherCTR);
                long l1 = cipherProcess.getLength();
                if (l1 != 0L) {
                    long skip;
                    long l3;
                    int n3;

                    //   for (long l2 = l1; (n3 = lcmp(l3 = l2 - (skip = super.in.skip(l2)), 0L)) > 0 && skip > 0L; l2 = l3) { }
                    for (long l2 = l1; (n3 = (skip = (l3 = l2 - (l2 = super.in.skip(l2)))) == 0L ? 0 : (skip < 0L ? -1 : 1)) > 0 && l2 > 0L; l2 = l3) {
                    }

                    if (n3 <= 0) {
                        final InputStream in = super.in;
                        try {
                            if (this.cipherFactory != null)
                                this.cipher = cipherProcess.getCipher(in, this.cipherFactory);
                            else if (this.cipher != null) cipher = this.cipher;
                            l1 += cipherProcess.getIntB();
                            if (this.cipher != null) {
                                return l1 + this.m0a(l - l1);
                            }
                            return l1 + this.m0a(l - l1);
                        } catch (GeneralSecurityException ex) {
                            Log.e(TAG, "Unable to get a new cipher: " + ex.getMessage());
                            throw new IOException("Failed to get a new cipher: " + ex.getMessage());
                        }
                    }
                    Log.e(TAG, "missing " + l3 + " of the " + l1 + " bytes to skip");
                    throw new IOException("Unable to skip enough");
                }
                return l1 + this.m0a(l - l1);
            }
            return this.m0a(l);
        }

        /* access modifiers changed from: package-private */
        /* renamed from: a */
        public void setCipherFactory(CipherFactory cipherFactory) {
            this.cipherFactory = cipherFactory;
        }

        /* renamed from: a */
        private boolean m1a() throws IOException {
            if (this.blA) {
                return false;
            }
            if (this.in != null) {
                this.intA = 0;
                this.intB = 0;
                while (this.intB == 0) {
                    int outputSize = this.cipher.getOutputSize(this.byteA.length);
                    byte[] bArr = this.byteB;
                    if (bArr == null || bArr.length < outputSize) {
                        this.byteB = new byte[outputSize];
                    }
                    int read = this.in.read(this.byteA);
                    if (read == -1) {
                        try {
                            int doFinal = this.cipher.doFinal(this.byteB, 0);
                            this.intB = doFinal;
                            this.blA = true;
                            return doFinal != 0;
                        } catch (Exception e) {
                            throw new IOException("Error while finalizing cipher", e);
                        }
                    } else {
                        try {
                            this.intB = this.cipher.update(this.byteA, 0, read, this.byteB, 0);
                        } catch (ShortBufferException e2) {
                            throw new AssertionError(e2);
                        }
                    }
                }
                return true;
            }
            throw new NullPointerException("in == null");
        }


        /* renamed from: a */
        private long m0a(long j) throws IOException {
            long j2 = 0;
            while (j2 < j && (this.intA != this.intB || m1a())) {
                int min = (int) Math.min(j - j2, (long) (this.intB - this.intA));
                this.intA += min;
                j2 += (long) min;
            }
            return j2;
        }
    }
}
