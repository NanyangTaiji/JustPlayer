package com.nanyang.richeditor.database;

import static com.nanyang.richeditor.database.CloudAccounts.BOX;
import static com.nanyang.richeditor.database.CloudAccounts.CLOUDRAIL;
import static com.nanyang.richeditor.database.CloudAccounts.ONEDRIVE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.OneDrive;
import com.nanyang.richeditor.App;
import com.nanyang.richeditor.JsonViewActivity;
import com.nanyang.richeditor.R;
import com.nytaiji.nybase.filePicker.FilePickDialog;
import com.nytaiji.nybase.filePicker.FolderPickDialog;
import com.nytaiji.nybase.utils.NyFileUtil;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class DatabaseUtils {


    public static void downloadCloudDatabase(String cloudUrl, String dbRemote) throws IOException {
        URL url = new URL(cloudUrl);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(dbRemote)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void copyDatabase(Context context, String backupPath, String databaseName) throws IOException {
        // Get the path to the database in the app's private storage
        String databasePath = context.getDatabasePath(databaseName).getAbsolutePath();

        // Open the database as an input stream
        InputStream myInput = new FileInputStream(databasePath);

        // Open the destination file as an output stream
        OutputStream myOutput = new FileOutputStream(backupPath);

        // Transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public static void copyAll(Context context) {
        try {
            for (DataLib lib : App.getAllLibs()) {
                String destinationPath = new File(NyFileUtil.getAppDirectory(context), lib.getName()).getAbsolutePath();
                copyDatabase(context, destinationPath, lib.getName());
                Timber.tag("DatabaseCopy").d("Database copied to: %s", destinationPath);
            }
        } catch (IOException e) {
            Timber.tag("DatabaseCopy").e(e, "Failed to copy database");
        }
    }

    public static void safelyRestoreDatabase(Context context, String backupPath, String databaseName) {
        SQLiteDatabase myDatabase = null;
        try {
            // Get the database path
            File databaseFile = context.getDatabasePath(databaseName);

            // Close the database if open
            if (databaseFile.exists()) {
                myDatabase = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                if (myDatabase.isOpen()) {
                    myDatabase.close();
                }
            }

            // Restore the database
            restoreDatabase(context, backupPath, databaseName);

            // Reopen the database if needed
            myDatabase = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            Log.d("DatabaseRestore", "Database restored and reopened successfully!");
        } catch (Exception e) {
            Log.e("DatabaseRestore", "Error restoring database: " + e.getMessage());
        } finally {
            // Close the database again if necessary
            if (myDatabase != null && myDatabase.isOpen()) {
                myDatabase.close();
            }
        }
    }

    public static void restoreDatabase(Context context, String backupPath, String databaseName) throws IOException {
        // Get the path to the database in the app's private storage
        String databasePath = context.getDatabasePath(databaseName).getAbsolutePath();

        // Open the backup file as an input stream
        InputStream myInput = new FileInputStream(backupPath);

        // Open the app's database file as an output stream
        OutputStream myOutput = new FileOutputStream(databasePath);

        // Transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }



    public static void cloudUpload(Context context, String path) {
        CloudRail.setAppKey(CLOUDRAIL.getClientKey());
        CloudStorage cs = new Box(context, BOX.getClientId(), BOX.getClientKey());
        //  CloudStorage cs = new OneDrive(context, ONEDRIVE.getClientId(), ONEDRIVE.getClientKey());
        // CloudStorage cs = new GoogleDrive(this, GDRIVE.getClientId(), "", CLOUD_AUTHENTICATOR_REDIRECT_URI, GDRIVE.getClientKey());
        //  CloudStorage cs = new Dropbox(this, DROPBOX.getClientId(), DROPBOX.getClientKey());
        File file = new File(path);
        new Thread() {
            @Override
            public void run() {
                if (!cs.exists("/memento")) cs.createFolder("/memento");
                InputStream stream = null;
                try {
                    stream = new FileInputStream(file);
                    long size = file.length();
                    cs.upload("/memento/" + file.getName(), stream, size, false);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
                .start();
    }

    public static JSONObject readJsonFromPath(String path) throws Exception {
        DataInputStream dis = new DataInputStream(new FileInputStream(path));
        byte[] backup_data = new byte[dis.available()];
        dis.readFully(backup_data);
        JSONObject json = new JSONObject(new String(backup_data));
        dis.close();
        return json;
    }


    public static void readBackupFile(Context context, String path) throws Exception {
        new Controller(context).readBackup(readJsonFromPath(path));
    }

    public static void autoSave(Context context) throws Exception {
        //TODO ny
        copyAll(context);
        //
        for (int i = 0; i < App.getAllLibs().size(); i++) {
            App.setCurrentLib(i);
            File file = new File(NyFileUtil.getAppDirectory(context), "data_" + i + "." + App.BACKUP_EXTENSION);
            File back = new File(NyFileUtil.getAppDirectory(context), "data_" + i + ".bak");
            File ex_back = new File(NyFileUtil.getAppDirectory(context), "tmp_" + i + App.BACKUP_EXTENSION);
            if (back.exists()) back.renameTo(ex_back);
            if (file.exists()) file.renameTo(back);
            File tmp = new File(NyFileUtil.getAppDirectory(context), "tmp_" + i + "." + App.BACKUP_EXTENSION);
            saveBackupFile(context, tmp.getAbsolutePath());
            //21.96 * 1096 is the minimun database size for zero item
            int MIN_DATABASE_SIZE = 24069;
            if (tmp.exists() && tmp.length() > MIN_DATABASE_SIZE) {
                if (tmp.renameTo(file) && ex_back.delete())
                    Toast.makeText(context, "Auto save succeded!", Toast.LENGTH_SHORT).show();
            } else {
                if (back.renameTo(file) &&
                        ex_back.renameTo(back))
                    Toast.makeText(context, "No auto saving", Toast.LENGTH_SHORT).show();
            }
            if (tmp.exists()) tmp.delete();
        }
    }

    public static String formatSavePath(String current_path, String filename_prefix, String extension) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        //TODO correction for Calendar.MONTH index fron 0 to 11
        filename_prefix = String.format(Locale.US, "%s-%d-%02d-%02d", filename_prefix, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

        File save_path = new File(current_path, String.format("%s.%s", filename_prefix, extension));

        int i = 2;
        while (save_path.exists()) {
            save_path = new File(current_path, String.format(Locale.US, "%s(%d).%s", filename_prefix, i, extension));
            i++;
        }

        return save_path.getAbsolutePath();
    }

    public static void saveBackupFile(Context context, String path) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            new Controller(context).writeBackup(fos);
            fos.flush();
        } finally {
            if (fos != null) fos.close();
        }
    }

    //-----------------moved from MementoActivity----------------------------//

    public static void viewJson(Context context) {
        FilePickDialog.newInstance(
                        R.string.json_viewer,
                        new String[]{App.BACKUP_EXTENSION},
                        new FilePickDialog.ImportListener() {
                            @Override
                            public void onSelect(final String path) {
                                Intent intent = new Intent(context, JsonViewActivity.class);
                                intent.setData(Uri.parse(path));
                                context.startActivity(intent);
                            }

                            @Override
                            public void onError(String msg) {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.restore_error)
                                        .setMessage(msg)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        })
                .show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }

    public static void backupData(Context context) {
      /* if(!fileProcessPermitted) {
            Toast.makeText(mementoActivity, "Get permission write first!", Toast.LENGTH_SHORT).show();
            return;
        }*/

        FolderPickDialog.newInstance(
                R.string.backup,
                new FolderPickDialog.SaveListener() {
                    @Override
                    public void onSelect(final String path) {
                        new Thread() {
                            @Override
                            public void run() {
                                ((FragmentActivity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(context)
                                                .setTitle(R.string.backup)
                                                .setMessage(((FragmentActivity) context).getString(R.string.backup_saved, path))
                                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        try {
                                                            saveBackupFile(context, formatSavePath(path, "data_" + App.getCurrentLib(), App.BACKUP_EXTENSION));
                                                        } catch (Exception e) {
                                                            Toast.makeText(context, "back up error", Toast.LENGTH_SHORT).show();
                                                            throw new RuntimeException(e);
                                                        }
                                                        cloudUpload(context, path);
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .show();
                                    }
                                });
                                interrupt();
                            }

                        }.start();
                    }

                    @Override
                    public void onError() {
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        ).show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }


    public static void restoreData(Context context, FilePickDialog.ImportListener listener) {
       /* if (!fileProcessPermitted) {
            Toast.makeText(mementoActivity, "Get permission read first!", Toast.LENGTH_SHORT).show();
            return;
        }*/
        FilePickDialog.newInstance(
                        R.string.restore,
                        new String[]{App.BACKUP_EXTENSION},
                        new FilePickDialog.ImportListener() {
                            @Override
                            public void onSelect(final String path) {
                                if (listener != null) listener.onSelect(path);
                                // restoreFromPath(path);
                            }

                            @Override
                            public void onError(String msg) {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.restore_error)
                                        .setMessage(msg)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        })
                .show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }

    public static void autoRestore(Context context, int i, FilePickDialog.ImportListener listener) {
        if (new Controller(context).findAllCategories().isEmpty()) {
            try {
                File file = new File(NyFileUtil.getAppDirectory(context), "data_" + i + "." + App.BACKUP_EXTENSION);
                File back = new File(NyFileUtil.getAppDirectory(context), "data_" + i + ".bak");
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Restore from backup?")
                        .setMessage("Database backup is found, do you want to restore?")
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            if (file.exists() && listener != null)
                                listener.onSelect(file.getPath());
                                //restoreFromPath();
                            else {
                                if (back.exists() && listener != null) {
                                    listener.onSelect(back.getPath());
                                    //  restoreFromPath(back.getPath());
                                }
                            }
                            dialog.dismiss();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void dbToJson(Context context) {

        FilePickDialog.newInstance(
                R.string.Jsontodb,
                new FilePickDialog.ImportListener() {
                    @Override
                    public void onSelect(final String path) {
                        new Thread() {
                            @Override
                            public void run() {
                                ((FragmentActivity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            new Controller(context).jsonToDatabase(context, path, NyFileUtil.getFileNameWithoutExtFromPath(path) + ".db");
                                            Toast.makeText(context, NyFileUtil.getFileNameWithoutExtFromPath(path) + ".db created!", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(context, NyFileUtil.getFileNameWithoutExtFromPath(path) + ".db created failed!", Toast.LENGTH_SHORT).show();
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                                interrupt();
                            }

                        }.start();
                    }

                    @Override
                    public void onError(String msg) {
                    }
                }
        ).show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }

    public static void synchronizeTask(Context context) {
        FilePickDialog.newInstance(
                R.string.synchronize1,
                new FilePickDialog.ImportListener() {
                    @Override
                    public void onSelect(final String path1) {
                        selectSecondPath(context, path1);
                    }

                    @Override
                    public void onError(String msg) {
                    }
                }
        ).show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }

    private static void selectSecondPath(Context context, String path1) {
        FilePickDialog.newInstance(
                R.string.synchronize2,
                new FilePickDialog.ImportListener() {
                    @Override
                    public void onSelect(final String path2) {
                        if (path2 != path1) {
                            //TODO ny
                            // Synchronization.synchronize(context, path1, path2);
                        } else {
                            Toast.makeText(context, "Two databasse are identical", Toast.LENGTH_SHORT).show();
                            selectSecondPath(context, path1);
                        }
                    }

                    @Override
                    public void onError(String msg) {
                    }
                }
        ).show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }
}
