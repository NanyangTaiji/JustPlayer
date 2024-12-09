package com.nytaiji.nybase.model;

import static android.text.TextUtils.replace;
import static com.nytaiji.nybase.crypt.EncryptUtil.getPasswordFromFileName;
import static com.nytaiji.nybase.filePicker.MediaSelection.DEFAULT_MEDIA;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_BOX;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_DROPBOX;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_GOOGLE_DRIVE;
import static com.nytaiji.nybase.model.NyHybridMode.CLOUD_PREFIX_ONE_DRIVE;
import static com.nytaiji.nybase.model.NyHybridMode.FTPS_URI_PREFIX;
import static com.nytaiji.nybase.model.NyHybridMode.FTP_URI_PREFIX;
import static com.nytaiji.nybase.model.NyHybridMode.PREFIX_OTG;
import static com.nytaiji.nybase.model.NyHybridMode.SMB_URI_PREFIX;
import static com.nytaiji.nybase.model.NyHybridMode.SSH_URI_PREFIX;
import static com.nytaiji.nybase.utils.NyFileUtil.containZip;
import static com.nytaiji.nybase.utils.NyFileUtil.extractPureName;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;
import static com.nytaiji.nybase.utils.NyMimeTypes.isCommonMimeType;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;
import com.nytaiji.nybase.crypt.NyCipherFactory;
import com.nytaiji.nybase.crypt.NyCipherInputStream;
import com.nytaiji.nybase.playlist.PlaylistFile;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.NyMimeTypes;
import com.nytaiji.nybase.utils.PreferenceHelper;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;

/**
 * Hybrid file for handeling all types of files, to be extended by HybridFile of AmazeFilemanager
 */
public class NyHybrid {
    // protected final Logger LOG = LoggerFactory.getLogger(HybridFile.class);
    private static String TAG = "NyHybrid";
    public static final String DOCUMENT_FILE_PREFIX =
            "content://com.android.externalstorage.documents";

    protected String path;
    protected OpenMode mode;
    protected String name;

    protected int vIndex = 0;

    protected boolean isDirectory = false;

    protected String mimeType = null;

    protected Uri uri = null;
    //TODO ny
    // protected final DataUtils dataUtils = DataUtils.getInstance();


    public NyHybrid(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public NyHybrid(String path) {
        this.path = path;
        //the next can not be extended to HybridFile
        // this.name = getSimpleName();
    }


    public NyHybrid(String folder, String name, boolean isDirectory) {
        //this(mode, path);
        //  if (folder.startsWith("/")) folder = folder.substring(1);
        this.name = name;
        this.isDirectory = isDirectory;
        new NyHybrid(folder + "/" + name);
    }

    public NyHybrid(Context context, String path) {
        new NyHybrid(path);
        // this.generateMode(context);
    }

    //TODO to be overrided
    public NyHybrid(Uri uri) {
        this.uri = uri;
        Log.e(TAG, "1 NyHybrid(Uri uri) uri.toString = " + uri.toString());
        if (isOnline(uri)) this.path = uri.toString();
        else this.path = NyFileUtil.getPath(AppContextProvider.getAppContext(), uri);
        Log.e(TAG, "1 NyHybrid(Uri uri) path = " + path);
        this.name = getName();
        // generateMode(AppContextProvider.getAppContext());
        this.mimeType = getMimeType();
    }

    public NyHybrid(Uri uri, String mimeType) {
        this.uri = uri;
        Log.e(TAG, "2 NyHybrid(Uri uri) uri.toString = " + uri.toString());
        if (isOnline(uri)) this.path = uri.toString();
        else this.path = NyFileUtil.getPath(AppContextProvider.getAppContext(), uri);
        Log.e(TAG, "2 NyHybrid(Uri uri) path = " + path);
        this.name = getName();
        //  generateMode(AppContextProvider.getAppContext());
        this.mimeType = mimeType;
    }


    //TODO ny 2024-5-2--------------------------------------------------------------------
    public NyHybrid(OpenMode mode, String path) {
        this.path = path;
        this.mode = mode;
    }

    public void generateMode(Context context) {
        if (path.startsWith(SMB_URI_PREFIX)) {
            mode = OpenMode.SMB;
        } else if (path.startsWith(SSH_URI_PREFIX)) {
            mode = OpenMode.SFTP;
        } else if (path.startsWith(PREFIX_OTG)) {
            mode = OpenMode.OTG;
        } else if (path.startsWith(FTP_URI_PREFIX)
                || path.startsWith(FTPS_URI_PREFIX)) {
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
        } else if (context == null) {
            mode = OpenMode.FILE;
        } else {
            mode = OpenMode.FILE;
        }
    }

    public void setMode(OpenMode mode) {
        this.mode = mode;
    }

    public OpenMode getMode() {
        return mode;
    }


    //TODO ny
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


    //------------------------------------------------------------------------

    public Uri getUri() {
        return this.uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }


    public void setPath(String path) {
        this.path = path;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        if (this.mimeType != null) return mimeType;
        else if (path != null) {
            mimeType = NyMimeTypes.getMimeTypeFromPath(path);
        } else if (uri != null) {
            mimeType = NyMimeTypes.getMimeTypeFromPath(uri.toString());
        } else mimeType = PreferenceHelper.getInstance().getString(DEFAULT_MEDIA);
        return mimeType;
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

    public long getSmartLength(Context context) {
        long s = Objects.requireNonNull(getFile()).length();

        if (containZip(path)) {
            Log.e("NyHybrid", "path ---------" + path);
            String zippath;
            String zippass = null;
            String zipentry;
            int indexw = path.indexOf("/p=");
            int indexe = path.indexOf("/e=");
            if (indexw > 0 && indexe > 0) {
                zippath = path.substring(0, indexw);

                zippass = path.substring(indexw + 3, indexe);
                //  Log.e("NyHybrid", "zippass ---------" + zippass);
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
                    Log.e("NyHybrid", "ZipFile inputstream fail ");
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
        if (path.contains("null")) path = path.replace("null", "");
        if (path.contains("file://")) path = path.replace("file://", "");
        Log.e(TAG, " String getPath() " + path);
        try {
            path = URLDecoder.decode(path, "UTF-8");
            return path;
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.e(TAG, "failed to decode " + path + e.toString());
            return null;
        }
    }


    public boolean isSimpleFile() {
        return isLocal();
    }

    public String getSimpleName() {
        String name = null;
        StringBuilder builder = new StringBuilder(path);
        name = builder.substring(builder.lastIndexOf("/") + 1, builder.length());
        return name;
    }

    public String getName(Context context) {
        return getFile().getName();
    }

    public String getName() {
        String name = null;
        if (path != null) name = NyFileUtil.getLastSegmentFromString(path);
        else if (uri != null)
            name = NyFileUtil.getLastSegmentFromString(uri.toString());
        return name;
    }

    //2024-5-2
    public void setName(String name) {
        this.name = name;
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
            Log.e("NyHybrid", "containZip(path) =" + path);
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
                        Log.e("NyHybrid", "ZipFile inputstream success");
                    }
                }
            } catch (IOException ignored) {
                Log.e("NyHybrid", "ZipFile inputstream fail ");
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
        InputStream finalInputStream = null;
        if (path.contains("_NY")) {
            try {
                if (Integer.parseInt(getPasswordFromFileName(path)) > 5)
                    inputStream.skip(Constants.ENCRYPT_SKIP);
                Cipher ces = new NyCipherFactory(path).getCipher();
                finalInputStream = new NyCipherInputStream(inputStream, ces);
                //Log.e("NyHybrid", "decripted inputstream success");
            } catch (GeneralSecurityException ignored) {
                Log.e("NyHybrid", "NyCipherInputStream fail ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else finalInputStream = inputStream;
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
            Log.e(TAG, "path =" + path);
            Log.e(TAG, "File(path) =" + file1.getAbsolutePath());
            File file2 = null;
            if (!file1.isDirectory()) {
                //TODO ny 2024-5-7 remove the following parts
                //  nyHybrids.add(new NyHybrid(file1.getName(), file1.getAbsolutePath()));
                file2 = file1.getParentFile();
            }
            if (file2 == null) return nyHybrids;

            File[] files = file2.listFiles();
            if (files != null) {
                for (File file : files) {
                    String tmpType = NyMimeTypes.getMimeTypeFromPath(file.getAbsolutePath());
                    Log.e(TAG, "tmpType =" + tmpType);
                    if (tmpType != null && isCommonMimeType(tmpType, mimeType)) {
                        nyHybrids.add(new NyHybrid(file.getName(), file.getAbsolutePath()));
                        //  if (file.getName().equals(name)) vIndex = i;
                        // i++;
                    }
                }
            }
        }

        if (nyHybrids.size() > 1) {
            Collections.sort(nyHybrids, new Comparator<NyHybrid>() {
                @Override
                public int compare(NyHybrid file1, NyHybrid file2) {
                    String name1 = extractPureName(file1.getPath());
                    String name2 = extractPureName(file2.getPath());

                    // Compare alphabetic parts
                    // Extract numeric parts from the filenames
                    String[] parts1 = name1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String[] parts2 = name2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

                    // Compare non-numeric parts lexicographically
                    int result = parts1[0].compareTo(parts2[0]);
                    if (result != 0) {
                        return result;
                    }

                    // Compare numeric parts numerically
                    int num1 = Integer.parseInt(parts1[1]);
                    int num2 = Integer.parseInt(parts2[1]);
                    return Integer.compare(num1, num2);
                }
            });
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


    public static OpenMode generateMode(Context context, String path) {
        OpenMode mode = OpenMode.FILE;
        if (path.startsWith(SMB_URI_PREFIX)) {
            mode = OpenMode.SMB;
        } else if (path.startsWith(SSH_URI_PREFIX)) {
            mode = OpenMode.SFTP;
        } else if (path.startsWith(PREFIX_OTG)) {
            mode = OpenMode.OTG;
        } else if (path.startsWith(FTP_URI_PREFIX)
                || path.startsWith(FTPS_URI_PREFIX)) {
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
        } else if (context == null) {
            mode = OpenMode.FILE;
        } else {
            // In some cases, non-numeric path is passed into HybridFile while mode is still
            // CUSTOM here. We are forcing OpenMode.FILE in such case too. See #2225
            if (OpenMode.UNKNOWN.equals(mode) || OpenMode.CUSTOM.equals(mode)) {
                mode = OpenMode.FILE;
            }
        }
        return mode;
    }


    public ArrayList<NyHybrid> listChildrenFiles(Context context) {
        if (!isDirectory(context) || new File(path).listFiles() == null) return null;
        ArrayList<NyHybrid> arrayList = new ArrayList<>();
        if (mode == null) generateMode(context);
        for (File file : new File(path).listFiles()) {
            arrayList.add(new NyHybrid(mode, file.getAbsolutePath()));
        }
        return arrayList;
    }

}
