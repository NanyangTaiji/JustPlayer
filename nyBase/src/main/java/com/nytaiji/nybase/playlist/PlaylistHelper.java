package com.nytaiji.nybase.playlist;

import static com.nytaiji.nybase.model.NyHybrid.generateMode;
import static com.nytaiji.nybase.utils.NyFileUtil.extractPureName;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlaylistHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "playlistManager.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String TABLE_PLAYLIST_FILES = "playlist_files";

    // Table playlists columns
    private static final String KEY_PLAYLIST_ID = "id";
    private static final String KEY_PLAYLIST_NAME = "name";
    private static final String KEY_PLAYLIST_SECURE = "secure";

    // Table playlist_files columns
    private static final String KEY_PLAYLIST_FILES_ID = "id";
    private static final String KEY_PLAYLIST_ID_FK = "playlist_id";
    private static final String KEY_FILE_URI = "file_uri";
    private static final String KEY_SECURE = "secure";
    private static final String KEY_OPEN_MODE = "open_mode";

    private static PlaylistHelper instance;

    private Context context;

    // Singleton pattern: private constructor
    private PlaylistHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Singleton pattern: static method to get the instance
    public static synchronized PlaylistHelper getInstance(Context context) {
        // Create a new instance if it doesn't exist
        if (instance == null) {
            instance = new PlaylistHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Create table queries
    private static final String CREATE_TABLE_PLAYLISTS = "CREATE TABLE " + TABLE_PLAYLISTS + "("
            + KEY_PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PLAYLIST_NAME + " TEXT,"
            + KEY_PLAYLIST_SECURE + " INTEGER DEFAULT 0)"; // 0 for not secure, 1 for secure

    private static final String CREATE_TABLE_PLAYLIST_FILES = "CREATE TABLE " + TABLE_PLAYLIST_FILES + "("
            + KEY_PLAYLIST_FILES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PLAYLIST_ID_FK + " INTEGER,"
            + KEY_FILE_URI + " TEXT,"
            + KEY_SECURE + " INTEGER DEFAULT 0," // 0 for false, 1 for true
            + KEY_OPEN_MODE + " TEXT,"
            + "FOREIGN KEY(" + KEY_PLAYLIST_ID_FK + ") REFERENCES " + TABLE_PLAYLISTS + "(" + KEY_PLAYLIST_ID + "))";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PLAYLISTS);
        db.execSQL(CREATE_TABLE_PLAYLIST_FILES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_FILES);
        onCreate(db);
    }

    // Create a new playlist
    public long createPlaylist(String name) {
        return createPlaylist(name, false); // Default to not secure
    }

    // Create a new playlist with security option
    public long createPlaylist(String name, boolean secure) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST_NAME, name);
        values.put(KEY_PLAYLIST_SECURE, secure ? 1 : 0); // Store 1 for secure, 0 for not secure
        long playlistId = db.insert(TABLE_PLAYLISTS, null, values);
        db.close();
        return playlistId;
    }

    // Add a file to a playlist
    public void addFileToPlaylist(long playlistId, String fileUri, boolean secure, OpenMode openMode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST_ID_FK, playlistId);
        values.put(KEY_FILE_URI, fileUri);
        values.put(KEY_SECURE, secure ? 1 : 0);
        if (openMode != null) values.put(KEY_OPEN_MODE, openMode.toString());
        else values.put(KEY_OPEN_MODE, generateMode(null, fileUri).toString());
        db.insert(TABLE_PLAYLIST_FILES, null, values);
        db.close();
    }

    // Retrieve all playlists from the database and return them as Playlist objects
    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = new ArrayList<>();

        // Query the database to retrieve all playlists
        String selectQuery = "SELECT * FROM " + TABLE_PLAYLISTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through the cursor to create Playlist objects
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_PLAYLIST_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_PLAYLIST_NAME));
                int secureInt = cursor.getInt(cursor.getColumnIndex(KEY_PLAYLIST_SECURE));
                boolean secure = (secureInt == 1); // Convert int to boolean

                // Create a new Playlist object and add it to the list
                Playlist playlist = new Playlist(id, name, secure);
                playlists.add(playlist);
            } while (cursor.moveToNext());
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();
        Collections.sort(playlists, new Comparator<Playlist>() {
            @Override
            public int compare(Playlist file1, Playlist file2) {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });
        // Return the list of Playlist objects
        return playlists;
    }

    // Get all playlists
    public List<String> getAllPlaylistNames() {
        List<String> playlistNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLAYLISTS, null);
        if (cursor.moveToFirst()) {
            do {
                String playlistName = cursor.getString(cursor.getColumnIndex(KEY_PLAYLIST_NAME));
                playlistNames.add(playlistName);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return playlistNames;
    }

    // Get playlist ID by name
    public long getPlaylistIdByName(String playlistName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLAYLISTS, new String[]{KEY_PLAYLIST_ID}, KEY_PLAYLIST_NAME + "=?",
                new String[]{playlistName}, null, null, null);
        long playlistId = -1;
        if (cursor.moveToFirst()) {
            playlistId = cursor.getLong(cursor.getColumnIndex(KEY_PLAYLIST_ID));
        }
        cursor.close();
        return playlistId;
    }


    public void updatePlaylist(Playlist playlist) {
        updatePlaylist(playlist.getId(), playlist.getName(), playlist.isSecure());
    }

    // update a playlist
    public void updatePlaylist(long playlistId, String newName, boolean secure) {
        String name = getPlaylistNameById(playlistId);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST_NAME, newName);
        values.put(KEY_PLAYLIST_SECURE, secure ? 1 : 0); // Store 1 for secure, 0 for not secure
        db.update(TABLE_PLAYLISTS, values, KEY_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)});
        db.close();
        if (!name.equals(newName))
            Toast.makeText(context, name + " is renamed as " + newName, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, name + " is updated!", Toast.LENGTH_SHORT).show();
    }

    public String getPlaylistNameById(long playlistId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String playlistName = null;

        // Query the database to retrieve the playlist name with the given ID
        Cursor cursor = db.query(TABLE_PLAYLISTS, new String[]{KEY_PLAYLIST_NAME}, KEY_PLAYLIST_ID + "=?",
                new String[]{String.valueOf(playlistId)}, null, null, null);

        // Check if the cursor contains data
        if (cursor != null && cursor.moveToFirst()) {
            playlistName = cursor.getString(cursor.getColumnIndex(KEY_PLAYLIST_NAME));
            cursor.close();
        }

        // Close the database connection
        db.close();

        return playlistName;
    }


    // Delete a playlist
    public void deletePlaylist(long playlistId) {
        String name = getPlaylistNameById(playlistId);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLISTS, KEY_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)});
        db.delete(TABLE_PLAYLIST_FILES, KEY_PLAYLIST_ID_FK + " = ?", new String[]{String.valueOf(playlistId)});
        db.close();
        Toast.makeText(context, name + " is removed", Toast.LENGTH_SHORT).show();
    }


    // Check if a playlist exists with the given name
    public boolean playlistExists(String playlistName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLAYLISTS, null, KEY_PLAYLIST_NAME + "=?",
                new String[]{playlistName}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //--------------------files in a playlist-----------------------//
    // Retrieve files in a playlist
    public List<PlaylistFile> getFilesInPlaylist(long playlistId) {
        List<PlaylistFile> playlistFiles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLAYLIST_FILES, null, KEY_PLAYLIST_ID_FK + "=?",
                new String[]{String.valueOf(playlistId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_PLAYLIST_FILES_ID));
                String fileUri = cursor.getString(cursor.getColumnIndex(KEY_FILE_URI));
                boolean secure = cursor.getInt(cursor.getColumnIndex(KEY_SECURE)) == 1;
                OpenMode openMode = OpenMode.valueOf(cursor.getString(cursor.getColumnIndex(KEY_OPEN_MODE)));
                playlistFiles.add(new PlaylistFile(fileUri, secure, openMode));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Collections.sort(playlistFiles, new Comparator<PlaylistFile>() {
            @Override
            public int compare(PlaylistFile file1, PlaylistFile file2) {
                // Split filenames and extensions
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

        return playlistFiles;
    }


    // Get files in a playlist
    public List<String> getFilesPathInPlaylist(long playlistId) {
        List<String> uris = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLAYLIST_FILES + " WHERE " + KEY_PLAYLIST_ID_FK + "=?", new String[]{String.valueOf(playlistId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String uriString = cursor.getString(cursor.getColumnIndex(KEY_FILE_URI));
                uris.add(uriString);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return uris;
    }


    // Remove a file from a playlist
    public void removeFileFromPlaylist(long playlistId, String fileUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST_FILES, KEY_PLAYLIST_ID_FK + "=? AND " + KEY_FILE_URI + "=?", new String[]{String.valueOf(playlistId), fileUri.toString()});
        db.close();
    }


    public void changeFileSecurity(long playlistId, String fileUri, boolean secure) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SECURE, secure ? 1 : 0); // Store 1 for secure, 0 for not secure
        db.update(TABLE_PLAYLIST_FILES, values, KEY_PLAYLIST_ID_FK + " = ? AND " + KEY_FILE_URI + " = ?",
                new String[]{String.valueOf(playlistId), fileUri});
        db.close();
    }

    // Check if a file exists in a playlist
    public boolean fileExistsInPlaylist(long playlistId, String fileUri) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLAYLIST_FILES +
                        " WHERE " + KEY_PLAYLIST_ID_FK + "=? AND " + KEY_FILE_URI + "=?",
                new String[]{String.valueOf(playlistId), fileUri});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

}

