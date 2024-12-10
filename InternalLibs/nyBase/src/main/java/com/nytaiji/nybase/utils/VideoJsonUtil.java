package com.nytaiji.nybase.utils;


import static com.nytaiji.nybase.crypt.EncryptUtil.LevelCipherOnly;
import static com.nytaiji.nybase.crypt.EncryptUtil.StrToEncryptStr;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.nytaiji.nybase.crypt.EncryptUtil;
import com.nytaiji.nybase.model.Folder;
import com.nytaiji.nybase.model.NyVideo;
import com.nytaiji.nybase.model.NyVideoGroup;
import com.nytaiji.nybase.R;
import com.nytaiji.nybase.model.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import javax.crypto.Cipher;


public class VideoJsonUtil {


    private static final String TAG = "JsonUtil";

    public static List<String> readListFromPath(Context context, String path) {
        if (path.contains("_NY")) return readListFromEncryptFile(context, new File(path));
        else return readListFromFile(new File(path));
    }

    public static List<String> readListFromFile(File file) {
        return readListFromFile(file, null);
    }

    public static List<String> readListFromFile(File file, String passWord) {
        Cipher cipher = null;
        if (passWord != null) cipher = LevelCipherOnly(passWord);
        BufferedReader r;
        List<String> list = new ArrayList<String>();
        try {
            r = new BufferedReader(new FileReader(file));
            String line;
            while (true) {
                if ((line = r.readLine()) == null) {
                    Log.e(TAG, "readListFromFile line =" + line);
                    break;
                } else {
                    if (cipher != null) {
                        Log.e(TAG, StrToEncryptStr(line, cipher));
                        list.add(StrToEncryptStr(line, cipher));
                    } else list.add(line);
                }
            }
            r.close();
        } catch (Exception e) {
            e.printStackTrace(); // file not found
        }
        return list;
    }


    public static List<String> readListFromEncryptFile(Context context, File file) {
        File tempFile = new File("temp.txt");
        final Cipher cipher = LevelCipherOnly(1);
        EncryptUtil.StandardEncryptFile(context, file, tempFile, cipher);
        List<String> list = readListFromFile(tempFile);
        tempFile.delete();
        return list;
    }

    public static void saveStrToNormalFile(String str, File file) {
        //  File f = new File("gallerydump_img.txt");
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.println(str);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // file not found
        }
    }

    public static boolean saveListToFile(List<String> list, String filePath) {
        return saveListToFile(list, new File(filePath), null);
    }

    public static boolean saveListToFile(List<String> list, File file) {
        return saveListToFile(list, file, null);
    }

    public static boolean saveListToFile(List<String> list, File file, String passWord) {
        //  File f = new File("gallerydump_img.txt");
        Cipher cipher = null;
        if (passWord != null) cipher = LevelCipherOnly(passWord);
        try {
            PrintWriter pw = new PrintWriter(file);
            for (String line : list)
                if (passWord != null)
                    pw.println(StrToEncryptStr(line, cipher));
                else pw.println(line);
            pw.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // file not found
        }
        return false;
    }


    public static void saveListToEncryptFile(Context context, List<String> list, File file) {
        File tempFile = new File("temp.txt");
        saveListToFile(list, tempFile);
        final Cipher cipher = LevelCipherOnly(1);
        if (EncryptUtil.StandardEncryptFile(context, tempFile, file, cipher)) tempFile.delete();
    }

    public static void AddItem2ListFile(String str, File file) {
        List<String> list = new ArrayList<String>();
        if (file.exists()) list = readListFromFile(file);
        list.add(str);
        saveListToFile(list, file);
    }

    public static void DeleteItemfromListFile(String str, File file) {
        List<String> list = new ArrayList<String>();
        if (!file.exists()) return;
        list = readListFromFile(file);
        if (list.contains(str) && list.remove(str)) saveListToFile(list, file);
    }


    //-----------------------------------------------------

    public static void SaveAllVideosEncryptJSON(List<Folder> folderList, String filePath) {
        JSONArray array = GetAllFolderVideosJSON(folderList);
        String arrayStr = array.toString().replace("\\", "");
        Cipher cipher = LevelCipherOnly(1);
        //   String arrayStr2= EncryptUtil.StrToEncryptStr(arrayStr,cipher);

        String arrayStr2 = StrToEncryptStr(arrayStr, "nanyangtaiji1234", "1958061019621212");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath));
            outputStreamWriter.write(arrayStr2);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static void SaveAllFolderVideosJSON(List<Folder> folderList, String filePath) {
        JSONArray array = GetAllFolderVideosJSON(folderList);
        String arrayStr = array.toString().replace("\\", "");

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    public static JSONArray GetAllFolderVideosJSON(List<Folder> folders) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < folders.size(); i++) {
            Folder folder = folders.get(i);
            JSONObject temp = GetAllinDirJSON(folder);
            if (temp != null) array.put(temp);
        }
        return array;
    }

    public static void SaveVideosGroupsToAnotherDir(List<NyVideoGroup> nyVideoGroups, String groupName, String dir) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < nyVideoGroups.size(); i++) {
            NyVideoGroup nyVideoGroup = nyVideoGroups.get(i);
            JSONObject temp = getAllinGroupJSON(nyVideoGroup, nyVideoGroup.getName());
            if (temp != null) array.put(temp);
        }
        String arrayStr = array.toString().replace("\\", "");
        String filePath = dir + File.separator + groupName + ".txt";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static JSONObject GetAllinDirJSON(Folder folder) {
        boolean isEmpty = true;
        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            jsonObj.put("name", folder.getName());
            File[] dirFs = new File(folder.getPath()).listFiles();

            for (int i = 0; i < dirFs.length; i++) {
                if (dirFs[i].getName().contains(".mp4")) {
                    isEmpty = false;
                    array.put(GetFileJSON(dirFs[i]));
                }
            }
            jsonObj.put("links", array);

        } catch (Exception e) {
            Log.d("Exec", e.getMessage());
        }
        if (array.toString().equals("[]")) {
            return null;
        } else {
            return jsonObj;
        }
    }


    public static JSONObject GetAllinDirJSON(File dir) {
        boolean isEmpty = true;
        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            jsonObj.put("name", dir.getName());
            File[] dirFs = dir.listFiles();

            for (int i = 0; i < dirFs.length; i++) {
                if (dirFs[i].getName().contains(".mp4")) {
                    isEmpty = false;
                    array.put(GetFileJSON(dirFs[i]));
                }
            }
            jsonObj.put("links", array);

        } catch (Exception e) {
            Log.d("Exec", e.getMessage());
        }
        if (array.toString().equals("[]")) {
            return null;
        } else {
            return jsonObj;
        }
    }

    public static JSONObject GetFileJSON(File file) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("name", file.getName());
            jsonObj.put("path", file.getAbsolutePath());
            //  jsonObj.put("parent", file.getParent());
        } catch (Exception e) {
            Log.d("Exec", e.getMessage());
        }
        Log.d("File:", jsonObj.toString());
        return jsonObj;
    }

//----------------------------------------

    public static void readVideoGroups(JsonReader reader, List<NyVideoGroup> groups) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            readVideoGroup(reader, groups);
        }
        reader.endArray();
    }

    private static void readVideoGroup(JsonReader reader, List<NyVideoGroup> groups) throws IOException {
        String groupName = "";
        ArrayList<NyVideo> NyVideos = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "name":
                    groupName = reader.nextString();
                    break;
                case "links":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        NyVideos.add(readVideoEntry(reader, false));
                    }
                    reader.endArray();
                    break;
                case "group_comment":
                    reader.nextString(); // Ignore.
                    break;
                default:
                    //    throw new ParserException("Unsupported name: " + name);
            }
        }
        reader.endObject();

        NyVideoGroup group = getGroup(groupName, groups);
        group.nyVideoInGroup.addAll(NyVideos);
    }

    public static void saveCheckedinAllGroupsToList(List<NyVideoGroup> nyVideoGroups, String groupName) {
        saveCheckedinAllGroupsToList(nyVideoGroups, groupName, 0);
    }

    public static void saveCheckedinAllGroupsToList(List<NyVideoGroup> nyVideoGroups, String groupName, int location) {
        //for Input use
        String toDir = location == Constants.LIST_GROUP ? NyFileUtil.getSavedDir() : NyFileUtil.getOnlineDir();
        JSONArray array = new JSONArray();
        array.put(getCheckedInAllGroupsJSON(nyVideoGroups, groupName));
        // if (array==null) return false;
        String arrayStr = array.toString().replace("\\", "");
        try {
            File outputFile = new File(toDir, groupName + ".bak");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());

        }
    }

    public static void appendCheckedInAllGroupsToList(List<NyVideoGroup> nyVideoGroups, List<String> groupNames) {
        JSONArray array = new JSONArray();
        array.put(getCheckedInAllGroupsJSON(nyVideoGroups, "tempxxxxxx"));
        String arrayStr = array.toString().replace("\\", "");
        for (String groupName : groupNames) {
            // Log.e("groupname", groupName);
            String newarrayStr = arrayStr.replace("tempxxxxxx", groupName);
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                Date now = new Date();
                File outputFile = new File(NyFileUtil.getSavedDir(), groupName + "-" + formatter.format(now) + ".bak");

                //  Toast.makeText(context,groupName,Toast.LENGTH_LONG).show();
                //   if (!outputFile.exists()) return false;
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile, false));
                outputStreamWriter.write(newarrayStr);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File append failed: " + e.toString());
            }
        }
    }


    public static void saveAllinGroupToList(Context context, NyVideoGroup nyVideoGroup, String groupName) {
        saveAllinGroupToList(context, nyVideoGroup, groupName, 0);
    }

    public static void saveAllinGroupToList(Context context, NyVideoGroup nyVideoGroup, String groupName, int location) {
        String toDir = (location == Constants.LIST_GROUP ? NyFileUtil.getSavedDir() : NyFileUtil.getOnlineDir());
        JSONArray array = new JSONArray();
        array.put(getAllinGroupJSON(nyVideoGroup, groupName));
        // if (array==null) return false;
        String arrayStr = array.toString().replace("\\", "");

        try {
            File outputFile = new File(toDir, groupName + ".txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());

        }
    }

    public static void saveAllinGroupToEncryptedList(Context context, NyVideoGroup nyVideoGroup, String groupName) {
        saveAllinGroupToEncryptedList(context, nyVideoGroup, groupName, 0);
    }

    public static void saveAllinGroupToEncryptedList(Context context, NyVideoGroup nyVideoGroup, String groupName, int location) {
        String toDir = (location == Constants.LIST_GROUP ? NyFileUtil.getSavedDir() : NyFileUtil.getOnlineDir());
        JSONArray array = new JSONArray();
        array.put(getAllinGroupJSON(nyVideoGroup, groupName));
        // if (array==null) return false;
        String arrayStr = array.toString().replace("\\", "");

        try {
            File outputFile = new File(toDir, groupName + ".tmp");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
            File jsonPath = new File(NyFileUtil.getSavedDir(), groupName + "_NY1.txt");
            final Cipher cipher = LevelCipherOnly(1);
            EncryptUtil.StandardEncryptFile(context, outputFile, jsonPath, cipher);
            outputFile.delete();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    //------------------

    private static JSONObject getAllinGroupJSON(NyVideoGroup nyVideoGroup, String groupName) {

        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            jsonObj.put("name", groupName);

            List<NyVideo> videoList = nyVideoGroup.nyVideoInGroup;
            int groupsize = videoList.size();
            for (int i = 0; i < groupsize; i++) {
                array.put(videoList.get(i).toJSONObject());
            }

            jsonObj.put("links", array);

        } catch (Exception e) {
            Log.d("Exec--------", e.getMessage());
        }
        if (array.toString().equals("[]")) {
            return null;
        } else {
            return jsonObj;
        }
    }


    private static JSONObject getAllinGroupJSON(List<NyVideo> videoList, String groupName) {

        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            jsonObj.put("name", groupName);
            int groupsize = videoList.size();
            for (int i = 0; i < groupsize; i++) {
                array.put(videoList.get(i).toJSONObject());
            }
            jsonObj.put("links", array);
        } catch (Exception e) {
            Log.d("Exec   JOSON", e.getMessage());
        }
        if (array.toString().equals("[]")) {
            return null;
        } else {
            return jsonObj;
        }
    }

    public static void saveVideoToTxt(NyVideo nyVideo, String groupName) {
        JSONArray array = new JSONArray();
        array.put(nyVideo.toJSONObject());
        String arrayStr = array.toString().replace("\\", "");
        try {
            File outputFile = new File(NyFileUtil.getSavedDir(), groupName + ".txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile, true));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "video save error: " + groupName + ".txt" + e.toString());
        }
    }


    /*
    public static JSONObject getVideoJSON_old(NyVideo nyVideo) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("name", nyVideo.name);
            jsonObj.put("path", nyVideo.path);
            jsonObj.put("passWord", nyVideo.passWord);
            jsonObj.put("download", nyVideo.download);
            //  jsonObj.put("parent", file.getParent());
        } catch (Exception e) {
            Log.d("Exec", e.getMessage());
        }
        Log.d("File:", jsonObj.toString());
        return jsonObj;
    }*/

    private static JSONObject getVideoJSON(NyVideo nyVideo, String groupName) {

        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            jsonObj.put("name", groupName);
            array.put(nyVideo.toJSONObject());
            jsonObj.put("links", array);
        } catch (Exception e) {
            Log.d("Exec   JOSO-", e.getMessage());
        }
        if (array.toString().equals("[]")) {
            return null;
        } else {
            return jsonObj;
        }
    }

    public static void appendVideoToList(NyVideo nyVideo, String groupName) {
        appendVideoToList(nyVideo, groupName, 0);
    }


    public static void appendVideoToList(NyVideo nyVideo, String groupName, int location) {
        String toDir = location == Constants.LIST_GROUP ? NyFileUtil.getSavedDir() : NyFileUtil.getOnlineDir();
        JSONObject jsonObject = getVideoJSON(nyVideo, groupName);
        JSONArray array = new JSONArray();
        array.put(jsonObject);
        String arrayStr = array.toString().replace("\\", "");
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
            Date now = new Date();
            File outputFile = new File(toDir, groupName + "-" + formatter.format(now) + ".bak");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile, false));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File append failed: " + e.toString());

        }
    }

    public static void shareCheckedInGroups(Context context, List<NyVideoGroup> nyVideoGroups, String shareName) {
        JSONArray array = new JSONArray();
        array.put(getCheckedInAllGroupsJSON(nyVideoGroups, "shareName"));
        String arrayStr = array.toString().replace("\\", "");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        Date now = new Date();
        File outputFile = new File(NyFileUtil.getSavedDir(), shareName + "-" + formatter.format(now) + ".bak");
        String shareFilePath = outputFile.getAbsolutePath();

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile, false));
            outputStreamWriter.write(arrayStr);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File append failed: " + e.toString());
        }

        NyFileUtil.shareFile(context, shareFilePath);
    }


    private static NyVideo readVideoEntry(JsonReader reader, boolean insidePlaylist) throws IOException {
        String name = null;
        String path = null;
        String passWord = null;
        int unlock = 0;
        int download = 0;
        String thumbUrl = null;
        String sphericalStereoMode = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String JsonName = reader.nextName();
            switch (JsonName) {
                case "name":
                    name = reader.nextString();
                    break;
                case "path":
                    path = reader.nextString();
                    break;
                case "passWord":
                    passWord = reader.nextString();
                    break;
                case "download":
                    download = reader.nextInt();
                    break;
                case "unlock":
                    unlock = reader.nextInt();
                    break;
                case "thumbUrl":
                    thumbUrl = reader.nextString();
                    break;
            /*    case "spherical_stereo_mode":
                    Assertions.checkState(
                            !insidePlaylist, "Invalid attribute on nested item: spherical_stereo_mode");
                    sphericalStereoMode = reader.nextString();
                    break;*/
                default:
                    //   throw new ParserException("Unsupported attribute name: " + name);
            }
        }
        reader.endObject();
        return new NyVideo(name, path, passWord, download, null);
    }

    private static NyVideoGroup getGroup(String groupName, List<NyVideoGroup> groups) {
        for (int i = 0; i < groups.size(); i++) {
            if (groupName.equals(groups.get(i).title)) {
                return groups.get(i);
            }
        }
        //in case not in the groups, define a new one
        NyVideoGroup group = new NyVideoGroup(groupName);
        groups.add(group);
        return group;
    }


    public static List<String> getFileNameWithOutExtListsFromDir(Context context, File parentDir) {
        List<String> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                String fileName = file.getName();
                if ((fileName.endsWith(".txt")
                        || fileName.endsWith(".bak")
                        || fileName.endsWith(".json"))
                        && (!fileName.contains(context.getResources().getString(R.string.input)))
                        && (!fileName.contains(context.getResources().getString(R.string.history)))
                        && (!fileName.contains(context.getResources().getString(R.string.newdownload)))
                ) {
                    inFiles.add(getOnlyFileName(file.getName().replace("_NY1", "")));
                }
            }
        }
        return inFiles;
    }

    public static List<String> getListNameWithOutExtFromDir(Context context, File parentDir) {
        List<String> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                String fileName = file.getName();
                if (fileName.endsWith(".txt") && !fileName.contains(context.getResources().getString(R.string.newdownload))) {
                    inFiles.add(getOnlyFileName(file.getName().replace("_NY1", "")));
                }
            }
        }
        return inFiles;
    }


    public static List<String> getFileNameWithExtFromDir(Context context, File parentDir) {
        List<String> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                inFiles.add(file.getName());
            }
        }
        return inFiles;
    }

    public static List<String> getFileNameWithOutExtFromDir(Context context, File parentDir) {
        List<String> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                //  String fileName = file.getName();
                inFiles.add(getOnlyFileName(file.getName()));
            }
        }
        return inFiles;
    }

    public static List<String> getJsonNameWithOutExtFromDir(Context context, File parentDir) {
        List<String> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                String fileName = file.getName();
                if (fileName.endsWith(".json")) {
                    inFiles.add(getOnlyFileName(file.getName().replace("_NY1", "")));
                }
            }
        }
        return inFiles;
    }


    private static String getOnlyFileName(String path) {

        if (path.contains("/")) {
            path = path.substring(path.lastIndexOf('/') + 1);
        }
        if (path.contains(".")) {
            path = path.substring(0, path.indexOf('.'));
        }
        return path;
    }


    public static JSONObject getCheckedInAllGroupsJSON(List<NyVideoGroup> nyVideoGroups, String groupName) {
        return getAllinGroupsJSON(nyVideoGroups, groupName, true);
    }

    public static JSONObject getAllinGroupsJSON(List<NyVideoGroup> nyVideoGroups, String groupName, boolean checkedOnly) {

        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            jsonObj.put("name", groupName);
            for (NyVideoGroup group : nyVideoGroups) {
                List<NyVideo> videoList = group.nyVideoInGroup;
                int groupsize = videoList.size();
                for (int i = 0; i < groupsize; i++) {
                    if (!checkedOnly || videoList.get(i).isChecked) {
                        array.put(videoList.get(i).toJSONObject());
                    }
                }
            }
            jsonObj.put("links", array);
            // Log.w(TAG, "array" + array.toString());
        } catch (Exception e) {
            Log.d("Exec", e.getMessage());
        }
        if (array.toString().equals("[]")) {
            return null;
        } else {
            return jsonObj;
        }
    }

    public static List<String> getUrlListFromVideoList(List<NyVideo> videoList) {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < videoList.size(); i++) {
            urls.add(videoList.get(i).path);
        }
        return urls;
    }


}