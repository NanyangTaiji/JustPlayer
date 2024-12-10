package com.nanyang.richeditor.util;

import static com.nytaiji.nybase.utils.NyFileUtil.isVideo;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;

import com.nytaiji.nybase.model.NyEdit;
import com.nytaiji.nybase.model.NyEditGroup;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class FilesUtils {
    public final static String FILE_TAG = "/rich_editor";
    private static final String TAG = "FileUtils";

    /**
     * 随机生产文件名
     *
     * @return
     */
    private static String generateFileName() {
        return "poster" + System.currentTimeMillis();
    }

  /*  public static void clearLocalRichEditorCache() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + FILE_TAG);
        deleteDirectory(file);
    }*/

    public static String saveBitmap(Bitmap bmp) {
        String parent = Environment.getExternalStorageDirectory().getPath() + FILE_TAG;
        File parentF = new File(parent);
        File f = new File(parent, generateFileName() + ".png");
        if (!parentF.exists()) {
            parentF.mkdirs();
        }
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f.getAbsolutePath();
    }


    //To save json in a file use RetriveandSaveJSONdatafromfile.objectToFile(jsonObj)
    // and to fetch data from file use
    // path = Environment.getExternalStorageDirectory() + File.separator +
    // "/AppName/App_cache/data" + File.separator;
    // RetriveandSaveJSONdatafromfile.objectFromFile(path);

    public static void objectToFile(Object object, String dirPath, String fileName) {
        // String path = Environment.getExternalStorageDirectory() + File.separator + "/AppName/App_cache" + File.separator;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            File data = new File(dirPath, fileName);
            if (!data.createNewFile()) {
                data.delete();
                data.createNewFile();
            }
            Writer output = null;
            output = new BufferedWriter(new FileWriter(data));
            output.write(object.toString());
            output.close();
            //   Toast.makeText(getApplicationContext(), "Composition saved", Toast.LENGTH_LONG).show();

          /*  ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(data));
            objectOutputStream.writeObject(object);
            objectOutputStream.close();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NyEditGroup objectFromFile(String filePath) {
        NyEditGroup nyEditGroup = new NyEditGroup("test");
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));

            // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject title = (JSONObject) jsonObject.get("title");
            nyEditGroup.title = title.toJSONString();
            Log.e(TAG, "nyEditGroup.title " + nyEditGroup.title);
            JSONObject mdate = (JSONObject) jsonObject.get("mdate");

            nyEditGroup.mDate = mdate.toJSONString();
            Log.e(TAG, "nyEditGroup.mdate " + nyEditGroup.mDate);

            JSONObject keywords = (JSONObject) jsonObject.get("keywords");

            nyEditGroup.keyWords = keywords.toJSONString();
            Log.e(TAG, "nyEditGroup.keyWords " + nyEditGroup.keyWords);

            JSONArray itemList = (JSONArray) jsonObject.get("nyEditInGroup");
            Iterator<JSONObject> iterator = itemList.iterator();
            ArrayList<String> mlist = new ArrayList<String>();
            while (iterator.hasNext()) {
                mlist.add(iterator.next().toJSONString());
                Log.e(TAG, "iterator.next() " + iterator.next().toJSONString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return nyEditGroup;
    }

    public static boolean isEncryptedVideo(String url) {
        return url.contains("_NY") && isVideo(url);
    }

    private static NyEdit readNYEditJson(JsonReader reader) throws IOException {
        String richText = null;
        String imagePath = null;
        String videoPath = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String JsonName = reader.nextName();
            switch (JsonName) {
                case "videoPath":
                    videoPath = reader.nextString();
                    break;
                case "imagePath":
                    imagePath = reader.nextString();
                    break;
                case "richText":
                    richText = reader.nextString();
                    break;
                default:
                    //   throw new ParserException("Unsupported attribute name: " + name);
            }
        }
        reader.endObject();
        return new NyEdit(richText, imagePath, videoPath);
    }

}
