package com.nytaiji.nybase.filePicker;

import static android.view.View.VISIBLE;
import static com.nytaiji.nybase.utils.NyFileUtil.getParentPath;
import static com.nytaiji.nybase.utils.NyFileUtil.safelyStartActivityForResult;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.nytaiji.nybase.R;
import com.nytaiji.nybase.filePicker.FilePickDialog;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.GeneralCallback;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.utils.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MediaSelection {
    private static Dialog mediaDialog;

    public static final int REQUEST_CODE_GET_ALL = 10080;
    public static final int REQUEST_CODE_GET_VIDEO = 10081;
    public static final int REQUEST_CODE_GET_IMAGE = 10082;

    private static SharedPreferences sharedPreferences;

    public static String DEFAULT_MEDIA = "DEFAULT_MEDIA";

    private static String defaultType = "*/*";


    public static void getMediaLinkDialog(FragmentActivity context, String startLink, GeneralCallback acttionCallback, GeneralCallback generalCallback) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppContextProvider.getAppContext());
        defaultType = getStringValue(context, DEFAULT_MEDIA, "*/*");
        //  Log.e("MediaSelection", "MediaSelection Default =" + defaultType);

        final View root = context.getLayoutInflater().inflate(R.layout.dialog_media_selection, null);
        String parent = null;
        if (startLink != null) parent = getParentPath(startLink);
        final String finalParent = parent;

        //---
        EditText dialogUrlEdittext = root.findViewById(R.id.dialog_url_edittext);
        root.findViewById(R.id.dialog_url_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtils.copyToClipboard(context, "");//clearClipboard
                dialogUrlEdittext.setText("");
            }
        });

        root.findViewById(R.id.dialog_url_pick_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editedLink = dialogUrlEdittext.getText().toString();
                if (acttionCallback != null) acttionCallback.SingleString(editedLink);
                mediaDialog.dismiss();
            }
        });

        //--------------------above is for the handdling direct link input--------------------------------//
        View localFile = root.findViewById(R.id.dialog_file_selection);
        localFile.setVisibility(VISIBLE);
        localFile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaDialog.dismiss();
                        FilePickDialog.pickupFile(context, true, generalCallback);
                    }
                }
        );

        View uriSelection = root.findViewById(R.id.dialog_uri_selection);
        uriSelection.setVisibility(VISIBLE);
        uriSelection.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaDialog.dismiss();
                        //  defaultType = getStringValue(context, DEFAULT_MEDIA, "*.*");
                        if (Objects.equals(defaultType, "*/*"))
                            uriSelectionBySystem(context, "*/*", REQUEST_CODE_GET_ALL);
                        else if (defaultType.equals("video/*"))
                            uriSelectionBySystem(context, "video/*", REQUEST_CODE_GET_VIDEO);
                        else uriSelectionBySystem(context, "image/*", REQUEST_CODE_GET_IMAGE);
                    }
                }
        );

        View mediaSelection = root.findViewById(R.id.dialog_media_selection);
        mediaSelection.setVisibility(VISIBLE);
        mediaSelection.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaDialog.dismiss();
                        // mediaSelectionBySystem(EditorActivity.this, "video/*|image/*|audio/*", requestCode);
                        if (Objects.equals(defaultType, "*/*"))
                            mediaSelectionBySystem(context, "video/*|image/*|audio/*", REQUEST_CODE_GET_ALL);
                        else if (defaultType.equals("video/*"))
                            mediaSelectionBySystem(context, "video/*", REQUEST_CODE_GET_VIDEO);
                        else
                            mediaSelectionBySystem(context, "image/*", REQUEST_CODE_GET_IMAGE);
                    }
                }
        );

        RadioButton allItems = root.findViewById(R.id.default_all);
        allItems.setChecked(Objects.equals(defaultType, "*/*"));
        if (!Objects.equals(defaultType, "application/pdf")
                && !Objects.equals(defaultType, "video/*")
                && !Objects.equals(defaultType, "image/*")) {
            allItems.setChecked(true);
            defaultType = "*/*";
            setStringValue(DEFAULT_MEDIA, defaultType);
        }
        allItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultType = "*/*";
                setStringValue(DEFAULT_MEDIA, defaultType);
            }
        });

        RadioButton dVideo = root.findViewById(R.id.default_video);
        dVideo.setChecked(Objects.equals(defaultType, "video/*"));
        dVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultType = "video/*";
                setStringValue(DEFAULT_MEDIA, defaultType);
            }
        });

        RadioButton dImage = root.findViewById(R.id.default_image);
        dImage.setChecked(Objects.equals(defaultType, "image/*"));
        dImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultType = "image/*";
                setStringValue(DEFAULT_MEDIA, defaultType);
            }
        });

        RadioButton muPDF = root.findViewById(R.id.default_mupdf);
        muPDF.setChecked(Objects.equals(defaultType, "application/pdf"));
        muPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultType = "application/pdf";
                setStringValue(DEFAULT_MEDIA, defaultType);
            }
        });

        //-- List handling
        ArrayList<String> allMedias = new ArrayList<>();
        if (startLink != null) allMedias = fetchAllMedias(startLink);
        Set<Integer> selectedItems = new HashSet<>();
        ListView lvMain = (ListView) root.findViewById(R.id.lvMain);
        // here we adjust list elements choice mode
      //  lvMain.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
     //   ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
             //   android.R.layout.simple_list_item_multiple_choice, allMedias);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                R.layout.list_item_with_checkbox, R.id.textView, allMedias) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                CheckBox checkBox = view.findViewById(R.id.checkBox);

                // Set a tag to identify the position of the item
                checkBox.setTag(position);

                // Set the checked state based on the selection status
                checkBox.setChecked(selectedItems.contains(position));

                // Set click listener for CheckBox to toggle selection
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        int pos = (Integer) cb.getTag();

                        if (cb.isChecked()) {
                            // If CheckBox is checked, add position to selectedItems
                            selectedItems.add(pos);
                        } else {
                            // If CheckBox is unchecked, remove position from selectedItems
                            selectedItems.remove((Integer) pos);
                        }
                    }
                });

                return view;
            }
        };

        //TODO ny
        if (allMedias!=null) lvMain.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ArrayList<String> finalAllMedias = allMedias;
        builder.setView(root)
                .setTitle("Select Media")
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, null);

        if (finalParent != null) {
            ArrayList<String> selectedPaths = new ArrayList<>();

            builder.setPositiveButton("Get list checked items", (dialog, which) -> {
                for (Integer position : selectedItems) {
                    selectedPaths.add((new File(finalParent, finalAllMedias.get(position)).getAbsolutePath()));
                }
                if (generalCallback != null) generalCallback.MultiStrings(selectedPaths);
            });


            builder.setNeutralButton("Get all links", (dialog, which) -> {
                for (int i = 0; i < finalAllMedias.size(); i++) {
                    selectedPaths.add(new File(finalParent, finalAllMedias.get(i)).getAbsolutePath());
                }
                if (generalCallback != null) generalCallback.MultiStrings(selectedPaths);
            });

        }

        mediaDialog = builder.create();
        mediaDialog.show();

        // paste whatever there is in the clipboard (hopefully it is a valid video url)
        if (SystemUtils.getClipboardItem(context) != null) {
            String charSequence = SystemUtils.getClipboardItem(context).toString();
            if (charSequence.contains("http") || charSequence.contains("storage"))
                dialogUrlEdittext.setText(charSequence);
            else SystemUtils.copyToClipboard(context, "");//clearClipboard
        }
    }

    public static void filePiackupFromAmaze(Activity context, String mimeType, int requestcode) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.amaze.nymanager", 0);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setPackage("com.amaze.nymanager");
            intent.setType(mimeType);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);//多选
            context.startActivityForResult(intent, requestcode);
        } catch (PackageManager.NameNotFoundException e) {
            // Toast.makeText(context, "Package nyFileManager did not installed", Toast.LENGTH_SHORT).show();
            mediaSelectionBySystem(context, "video/*|image/*|audio/*", requestcode);
            //Not working for next
            // uriSelectionBySystem(context, "video/*|image/*|audio/*", requestcode);
        }
    }

    public static void mediaSelectionBySystem(Activity context, String mimeType, int requestcode) {
        if (Objects.equals(mimeType, "*.*")) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(mimeType);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            context.startActivityForResult(intent, requestcode);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            // todo: this thing might need some work :/, eg open from google drive, stuff like that
            intent.setTypeAndNormalize(mimeType);
            context.startActivityForResult(Intent.createChooser(intent, "Select Media"), requestcode);
        }
    }

    public static void uriSelectionBySystem(Activity context, String mimeType, int requestcode) {
        final Intent intent = createBaseFileIntent(Intent.ACTION_OPEN_DOCUMENT, getMoviesFolderUri());
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Set the MIME type using setType
        intent.setType(mimeType);

        // Use EXTRA_MIME_TYPES with setStringArrayExtra to specify accepted MIME types
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] mimeTypes = {mimeType};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }

        if (Build.VERSION.SDK_INT < 30) {
            final ComponentName systemComponentName = getSystemComponent(context, intent);
            if (systemComponentName != null) {
                intent.setComponent(systemComponentName);
            }
        }

        safelyStartActivityForResult(context, intent, requestcode);
    }


    public static ComponentName getSystemComponent(Context context, Intent intent) {
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resolveInfos.size() < 2) {
            return null;
        }
        int systemCount = 0;
        ComponentName componentName = null;
        for (ResolveInfo resolveInfo : resolveInfos) {
            int flags = resolveInfo.activityInfo.applicationInfo.flags;
            boolean system = (flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (system) {
                systemCount++;
                componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            }
        }
        if (systemCount == 1) {
            return componentName;
        }
        return null;
    }


    public static Intent createBaseFileIntent(final String action, final Uri initialUri) {
        final Intent intent = new Intent(action);

        // http://stackoverflow.com/a/31334967/1615876
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);

        if (Build.VERSION.SDK_INT >= 26 && initialUri != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        return intent;
    }

    public static Uri getMoviesFolderUri() {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 26) {
            final String authority = "com.android.externalstorage.documents";
            final String documentId = "primary:" + Environment.DIRECTORY_MOVIES;
            uri = DocumentsContract.buildDocumentUri(authority, documentId);
        }
        return uri;
    }

    public static ArrayList<String> fetchAllMedias(String path) {
        //TODO ny 2024-12-9
        if (path.contains("zip")) return null;
        // Log.e("FileUtils", "path = " + path);
        ArrayList<String> allMedias = new ArrayList<>();

        File file1 = new File(path);
        if (!file1.isDirectory()) {
            file1 = file1.getParentFile();
        }
        //TODO ny
        if (file1==null) return null;
       // assert file1 != null;
        File[] files = file1.listFiles();
        if (files != null) {
            for (File file : files) {
                String tmp = file.getName();
                if (NyFileUtil.isMedia(tmp))
                    allMedias.add(tmp);
            }
        }
        // Log.e("FileUtils", "allMedias = " + allMedias.toString());
        Collections.sort(allMedias);
        return allMedias;
    }


    public static void setStringValue(String key, String value) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppContextProvider.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringValue(Context context, String key, String def) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, def);
    }


    //--------------------------//

    public static void getSavedMediaDialog(FragmentActivity context, ArrayList<String> allMedias, GeneralCallback generalCallback) {
        final View root = context.getLayoutInflater().inflate(R.layout.dialog_media_saved, null);
        ListView lvMain = (ListView) root.findViewById(R.id.lvMain);
        lvMain.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_multiple_choice, allMedias);
        lvMain.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(root)
                .setTitle("Select Media")
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, null);

        builder.setPositiveButton("Get list checked items", (dialog, which) -> {
            SparseBooleanArray sbArray = lvMain.getCheckedItemPositions();
            ArrayList<String> paths = new ArrayList<String>();
            for (int i = 0; i < sbArray.size(); i++) {
                int key = sbArray.keyAt(i);
                if (sbArray.get(key)) {
                    paths.add(allMedias.get(key));
                }
            }
            if (generalCallback != null) generalCallback.MultiStrings(paths);
        });

        builder.setNeutralButton("Get all in the list", (dialog, which) -> {
            if (generalCallback != null) generalCallback.MultiStrings(allMedias);
            //TODO delete the link after all be loaded
            //TODO 2023-9-9
            // new File(saveLink).delete();
        });
        builder.create().show();
    }

}
