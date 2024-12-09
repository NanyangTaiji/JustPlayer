package com.nytaiji.nybase.model;

import static com.nytaiji.nybase.crypt.EncryptUtil.getPasswordFromFileName;
import static com.nytaiji.nybase.filePicker.MediaSelection.DEFAULT_MEDIA;
import static com.nytaiji.nybase.utils.NyFileUtil.containZip;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;
import static com.nytaiji.nybase.utils.NyMimeTypes.isCommonMimeType;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;
import com.nytaiji.nybase.crypt.NyCipherFactory;
import com.nytaiji.nybase.crypt.NyCipherInputStream;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.NyMimeTypes;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;

/**
 * Hybrid file for handeling all types of files, to be extended by HybridFile of AmazeFilemanager
 */
public class NyHybridMode {
    private static String TAG = "NyHybrid";

    public static final long RECOVERY_UNSET = Long.MIN_VALUE;
    private static final String EMPTY_STRING = "";

    public static final String DOCUMENT_FILE_PREFIX =
            "content://com.android.externalstorage.documents";

    protected String path;
    // protected OpenMode mode;
    protected String name;

    protected int vIndex = 0;

    protected boolean isDirectory = false;

    protected String mimeType = null;

    protected Uri uri = null;

    protected long startPosition = -1L;

    private boolean isAutoQueued;

    public long getRecoveryPosition() {
        return startPosition;
    }

    public boolean isAutoQueued() {
        return isAutoQueued;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Item States, keep external access out
    ////////////////////////////////////////////////////////////////////////////

    public void setAutoQueued(final boolean autoQueued) {
        isAutoQueued = autoQueued;
    }

    private Throwable error;

    @Nullable
    public Throwable getError() {
        return error;
    }


    public static final String SMB_URI_PREFIX = "smb://";
    private static final String STORAGE_PRIMARY = "primary";
    private static final String COM_ANDROID_EXTERNALSTORAGE_DOCUMENTS = "com.android.externalstorage.documents";

    public static final String[] ANDROID_DATA_DIRS = new String[]{"Android/data", "Android/obb"};

    public static final List ANDROID_DEVICE_DATA_DIRS = Arrays.asList(
            new File(Environment.getExternalStorageDirectory(), "Android/data").getAbsolutePath(),
            new File(Environment.getExternalStorageDirectory(), "Android/obb").getAbsolutePath());
    public static final String FTP_URI_PREFIX = "ftp://";
    public static final String FTPS_URI_PREFIX = "ftps://";
    public static final String SSH_URI_PREFIX = "ssh://";
    public static final String PREFIX_OTG = "otg:/";
    private static final String PREFIX_DOCUMENT_FILE = "content:/";
    public static final String PREFIX_MEDIA_REMOVABLE = "/mnt/media_rw";
    private static final String PATH_SEPARATOR_ENCODED = "%2F";
    private static final String PRIMARY_STORAGE_PREFIX = "primary%3AA";
    private static final String PATH_ELEMENT_DOCUMENT = "document";
    public static final String CLOUD_PREFIX_BOX = "box:/";
    public static final String CLOUD_PREFIX_DROPBOX = "dropbox:/";
    public static final String CLOUD_PREFIX_GOOGLE_DRIVE = "gdrive:/";
    public static final String CLOUD_PREFIX_ONE_DRIVE = "onedrive:/";

    public static final String CLOUD_NAME_GOOGLE_DRIVE = "Google Drive™";
    public static final String CLOUD_NAME_DROPBOX = "Dropbox";
    public static final String CLOUD_NAME_ONE_DRIVE = "One Drive";
    public static final String CLOUD_NAME_BOX = "Box";


    protected OpenMode mode;

    //TODO ny
    public void setMode(OpenMode mode) {
        this.mode = mode;
    }

    public OpenMode getMode() {
        return mode;
    }


    public void setRecoveryPosition(long startPosition) {
        this.startPosition = startPosition;
    }
    //TODO ny
    // protected final DataUtils dataUtils = DataUtils.getInstance();


    public NyHybridMode(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public NyHybridMode(String path) {
        this.path = path;
        generateMode(AppContextProvider.getAppContext());
        //the next can not be extended to HybridFile
        this.name = getName();
    }

    //TODO to be overrided
    public NyHybridMode(Uri uri) {
        this.uri = uri;
        Log.e(TAG, "1 NyHybrid(Uri uri) uri.toString = " + uri.toString());
        if (isOnline(uri)) this.path = uri.toString();
        else this.path = NyFileUtil.getPath(AppContextProvider.getAppContext(), uri);
        Log.e(TAG, "1 NyHybrid(Uri uri) path = " + path);
        this.name = getName();
        generateMode(AppContextProvider.getAppContext());
        this.mimeType = getMimeType();
    }

    public NyHybridMode(Uri uri, String mimeType) {
        this.uri = uri;
        Log.e(TAG, "2 NyHybrid(Uri uri) uri.toString = " + uri.toString());
        if (isOnline(uri)) this.path = uri.toString();
        else this.path = NyFileUtil.getPath(AppContextProvider.getAppContext(), uri);
        Log.e(TAG, "2 NyHybrid(Uri uri) path = " + path);
        this.name = getName();
        generateMode(AppContextProvider.getAppContext());
        this.mimeType = mimeType;
    }

    public NyHybridMode(String folder, String name, boolean isDirectory) {
        //this(mode, path);
        //  if (folder.startsWith("/")) folder = folder.substring(1);
        this.name = name;
        this.isDirectory = isDirectory;
        new NyHybrid(folder + "/" + name);
    }

    public NyHybridMode(Context context, String path) {
        new NyHybrid(path);
        this.generateMode(context);
    }


    public NyHybridMode(String path, OpenMode mode) {
        this.path = path;
        this.mode = mode;
    }

    public NyHybridMode(OpenMode mode, String folder, String name, boolean isDirectory) {
        this.mode = mode;
        this.name = name;
        if (folder.startsWith(SMB_URI_PREFIX) || isSmb() || isDocumentFile() || isOtgFile()) {
            Uri.Builder pathBuilder = Uri.parse(this.path).buildUpon().appendEncodedPath(name);
            if ((folder.startsWith(SMB_URI_PREFIX) || isSmb()) && isDirectory) {
                pathBuilder.appendEncodedPath("/");
            }
            this.path = pathBuilder.build().toString();
        } else if (folder.startsWith(SSH_URI_PREFIX) || isSftp()) {
            this.path += "/" + name;
        } else if (isRoot() && folder.equals("/")) {
            // root of filesystem, don't concat another '/'
            this.path += name;
        } else {
            this.path += folder + "/" + name;
        }
    }

    //TODO ny newly added

    public void generateMode(Context context) {
        if (path.startsWith(SMB_URI_PREFIX)) {
            mode = OpenMode.SMB;
        } else if (path.startsWith(SSH_URI_PREFIX)) {
            mode = OpenMode.SFTP;
        } else if (path.startsWith(PREFIX_OTG)) {
            mode = OpenMode.OTG;
        } else if (path.startsWith(FTP_URI_PREFIX) || path.startsWith(FTPS_URI_PREFIX)) {
            mode = OpenMode.FTP;
        } else if (path.startsWith(DOCUMENT_FILE_PREFIX)) {
            mode = OpenMode.DOCUMENT_FILE;
        } else if (path.startsWith(CLOUD_PREFIX_BOX)) {
            mode = OpenMode.BOX;
        } else if (path.startsWith(CLOUD_PREFIX_ONE_DRIVE)) {
            mode = OpenMode.ONEDRIVE;
        } else if (path.startsWith(CLOUD_PREFIX_GOOGLE_DRIVE)) {
            mode = OpenMode.GDRIVE;
        } else if (path.startsWith(CLOUD_PREFIX_DROPBOX)) {
            mode = OpenMode.DROPBOX;
        } else {
            mode = OpenMode.FILE;
        }
    }

    public boolean isLocal() {
        return mode == OpenMode.FILE || new File(path).exists();
    }

    public boolean isRoot() {
        return mode == OpenMode.ROOT;
    }

    public boolean isSmb() {
        return mode == OpenMode.SMB;
    }

    public boolean isSftp() {
        return mode == OpenMode.SFTP;
    }

    public boolean isOtgFile() {
        return mode == OpenMode.OTG;
    }

    public boolean isFtp() {
        return mode == OpenMode.FTP;
    }

    public boolean isDocumentFile() {
        return mode == OpenMode.DOCUMENT_FILE;
    }

    public boolean isBoxFile() {
        return mode == OpenMode.BOX;
    }

    public boolean isDropBoxFile() {
        return mode == OpenMode.DROPBOX;
    }

    public boolean isOneDriveFile() {
        return mode == OpenMode.ONEDRIVE;
    }

    public boolean isGoogleDriveFile() {
        return mode == OpenMode.GDRIVE;
    }

    public boolean isAndroidDataDir() {
        return mode == OpenMode.ANDROID_DATA;
    }

    public boolean isCloudDriveFile() {
        return isBoxFile() || isDropBoxFile() || isOneDriveFile() || isGoogleDriveFile();
    }

    /**
     * Whether file is a simple file (i.e. not a directory/smb/otg/other)
     *
     * @return true if file; other wise false
     */
    public boolean isSimpleFile() {
        return !isSmb()
                && !isOtgFile()
                && !isDocumentFile()
                && !android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches()
                && (getFile() != null && !getFile().isDirectory())
                && !isOneDriveFile()
                && !isGoogleDriveFile()
                && !isDropBoxFile()
                && !isBoxFile()
                && !isSftp()
                && !isFtp();
    }


    public void setPath(String path) {
        this.path = path;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        if (this.mimeType != null) return mimeType;
        else {
            String mimeType = NyMimeTypes.getMimeTypeFromPath(path);
            if (mimeType == null)
                mimeType = PreferenceManager.getDefaultSharedPreferences(AppContextProvider.getAppContext()).getString(DEFAULT_MEDIA, "video/*");
            return mimeType;
        }
    }


    @Nullable
    public File getFile() {
        return new File(path);
    }

    /**
     * Helper method to find length
     */
    public long length(Context context) {
        return getSmartLength(context);
    }

    public long getDuration() {
        return (getSmartLength(AppContextProvider.getAppContext()));
    }

    public long getSmartLength(Context context) {
        long s = Objects.requireNonNull(getFile()).length();

        if (containZip(path)) {
            Log.e(TAG, "path ---------" + path);
            String zippath;
            String zippass = null;
            String zipentry;
            int indexw = path.indexOf("/p=");
            int indexe = path.indexOf("/e=");
            if (indexw > 0 && indexe > 0) {
                zippath = path.substring(0, indexw);

                zippass = path.substring(indexw + 3, indexe);
                //  Log.e(TAG, "zippass ---------" + zippass);
                zipentry = path.substring(indexe + 3);
                try {
                    ZipFile zipFile = new ZipFile(zippath);
                    //
                    if (zippass.length() > 0) zipFile.setPassword(zippass.toCharArray());

                    List<FileHeader> headerList = zipFile.getFileHeaders();
                    for (FileHeader fileHeader : headerList) {
                        if (!fileHeader.isDirectory()
                                && fileHeader.getFileName().contains(zipentry)) {
                            s = fileHeader.getUncompressedSize();
                        }
                    }
                } catch (IOException ignored) {
                    Log.e(TAG, "ZipFile inputstream fail ");
                }
            }
        }
        return s;
    }

    /**
     * Path accessor. Avoid direct access to path since path may have been URL encoded.
     *
     * @return URL decoded path
     */
    public String getPath() {
        path = path.replace("null", "");
        Log.e(TAG, " String getPath() " + path);
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.e(TAG, "failed to decode " + path + e.toString());
            return null;
        }
    }


    public String getName() {
        String name = null;
        if (path != null) name = NyFileUtil.getLastSegmentFromString(path);
        else if (path == null && uri != null)
            name = NyFileUtil.getLastSegmentFromString(uri.toString());
        return name;
    }


    public Uri getUri() {
        return uri;
    }

    //TODO to be overrided
    public String getName(Context context) {
        return getName();
    }

    /**
     * Helper method to get parent path
     */
    public String getParent(Context context) {
        return getFile().getParent();
    }

    public boolean isDirectory(Context context) {
        boolean isDirectory;
        isDirectory = getFile().isDirectory();
        return isDirectory;
    }

    private static String formatUriForDisplayInternal(
            @NonNull String scheme, @NonNull String host, @NonNull String path) {
        return String.format("%s://%s%s", scheme, host, path);
    }

    @Nullable
    public InputStream getInputStream(Context context) {
        try {
            if (uri != null)
                return context.getContentResolver().openInputStream(uri);
            if (path.contains("http"))
                return new URL(path).openStream();
            else return getLocalInputStream(context);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public InputStream getLocalInputStream(Context context) { //handle zip file
        InputStream inputStream = null;
        if (containZip(path)) {
            Log.e(TAG, "containZip(path) =" + path);
            int indexw = path.indexOf("/p=");
            int indexe = path.indexOf("/e=");
            String zippath = path.substring(0, indexw);
            String zippass = path.substring(indexw + 3, indexe);
            String zipentry = path.substring(indexe + 3);
            try {
                ZipFile zipFile = new ZipFile(zippath);
                if (zippass.length() > 0) zipFile.setPassword(zippass.toCharArray());
                List<FileHeader> headerList = zipFile.getFileHeaders();
                for (FileHeader fileHeader : headerList) {
                    if (!fileHeader.isDirectory()
                            && fileHeader.getFileName().contains(zipentry)) {
                        inputStream = zipFile.getInputStream(fileHeader);
                        Log.e(TAG, "ZipFile inputstream success");
                    }
                }
            } catch (IOException ignored) {
                Log.e(TAG, "ZipFile inputstream fail ");
            }
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    inputStream = Files.newInputStream(Paths.get(path));
                } else inputStream = new FileInputStream(path);
            } catch (IOException ignored) {
            }
        }
        return inputStream;
    }

    public InputStream getDecryptedInputStream(Context context) {
        InputStream inputStream = getInputStream(context);
        InputStream finalInputStream = inputStream;
        if (path.contains("_NY")) {
            Log.e(TAG, "getDecryptedInputStream");
            try {
                if (Integer.parseInt(getPasswordFromFileName(path)) > 5)
                    inputStream.skip(Constants.ENCRYPT_SKIP);
                Cipher ces = new NyCipherFactory(path).getCipher();
                finalInputStream = new NyCipherInputStream(inputStream, ces);
                //Log.e(TAG, "decripted inputstream success");
            } catch (GeneralSecurityException ignored) {
                Log.e(TAG, "NyCipherInputStream fail ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return finalInputStream;
    }

    public boolean exists() {
        boolean exists = false;
        exists = getSmartLength(null) > 0;
        return exists;
    }

    public ArrayList<NyHybrid> commonrades() {
        if (path == null) path = getPath();
        Log.e(TAG, "commonrades path: " + path);
        //  Log.e(TAG, "Uri.to: " + uri.toString());
        if (path == null && uri != null)
            path = NyFileUtil.getPath(AppContextProvider.getAppContext(), uri);
        if (mimeType == null) mimeType = getMimeType();
        Log.e(TAG, "commonrades mimeType = " + mimeType);
        ArrayList<NyHybrid> nyHybrids = new ArrayList<>();
        int i = 0;

        //TODO
        if (containZip(path)) {
            int indexw = path.indexOf("/p=");
            int indexe = path.indexOf("/e=");

            String zippath = path.substring(0, indexw);
            String zippass = path.substring(indexw + 3, indexe);
            String zipentry = path.substring(indexe + 3);
            // Log.e(TAG, "Zip path=---------------------" + zipentry);

            try {
                // Log.e(TAG, "Zip path" + path);
                ZipFile zipDir = new ZipFile(zippath);
                if (zippass.length() > 0)
                    zipDir.setPassword(zippass.toCharArray());
                //TODO ny to handle with Chinese characters
                List<FileHeader> headerList = zipDir.getFileHeaders();
                for (FileHeader fileHeader : headerList) {
                    if (!fileHeader.isDirectory()) {
                        // Log.e(TAG, "fileHeader.getFileName() =" + fileHeader.getFileName());
                        String tmpType = NyMimeTypes.getMimeTypeFromPath(fileHeader.getFileName());
                        if (tmpType != null && tmpType.contains(mimeType)) {
                            NyHybrid nyHybrid = new NyHybrid(fileHeader.getFileName(), zippath + "/p=" + zippass + "/e=" + fileHeader.getFileName());
                            nyHybrids.add(nyHybrid);
                            if (fileHeader.getFileName().contains(zipentry)) {
                                vIndex = i;
                            }
                            i++;
                        }
                    }
                }
            } catch (ZipException e8) {
                Log.e(TAG, "Unable to open the Zip Expansion File: " + e8.getMessage());
                return null;
            }
        } else if (isOnline(path)) {
            Log.e(TAG, "online path =" + path); //should not use new NyHybrid(uri, mimeType)
            if (uri != null) nyHybrids.add(new NyHybrid(uri, mimeType));
            else nyHybrids.add(new NyHybrid(Uri.parse(path), mimeType));
        } else if (isSimpleFile()) {
            File file1 = new File(path);
            File file2 = null;
            if (!file1.isDirectory()) {
                nyHybrids.add(new NyHybrid(file1.getName(), file1.getAbsolutePath()));
                file2 = file1.getParentFile();
            }
            assert file2 != null;
            File[] files = file2.listFiles();
            if (files != null) {
                for (File file : files) {
                    String tmpType = NyMimeTypes.getMimeTypeFromPath(file.getAbsolutePath());
                    Log.e(TAG, "tmpType =" + tmpType);
                    if (tmpType != null && isCommonMimeType(tmpType, mimeType)) {
                        nyHybrids.add(new NyHybrid(file.getName(), file.getAbsolutePath()));
                        if (file.getName().equals(name)) vIndex = i;
                        i++;
                    }
                }
            }
        }
        //  Log.e("NyHybrid", "nyHybrids commarades" + nyHybrids.toString());
        return nyHybrids;
    }

    //--------------static method-------------//
    public int getIndex() {
        if (vIndex > -1) return vIndex;
        ArrayList<NyHybrid> list = commonrades();
        if (list == null) return -1;
        int i = 0;
        for (NyHybrid file : list) {
            if (file.getPath().equals(path)) {
                vIndex = i;
                return i;
            } else i++;
        }
        return 0;
    }
}


