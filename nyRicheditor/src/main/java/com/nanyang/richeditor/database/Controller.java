package com.nanyang.richeditor.database;


import static com.nanyang.richeditor.database.DatabaseModel.NEW_MODEL_ID;
import static com.nanyang.richeditor.database.DatabaseUtils.readJsonFromPath;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nanyang.richeditor.App;
import com.nytaiji.nybase.utils.NyFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class Controller {
    public static final int SORT_TITLE_ASC = 0;
    public static final int SORT_TITLE_DESC = 1;
    public static final int SORT_DATE_ASC = 2;
    public static final int SORT_DATE_DESC = 3;

    /**
     * The singleton instance of Controller class
     */
    private Controller instance = null;

    private SQLiteOpenHelper helper;

    private final String[] sorts = {
            OpenHelper.COLUMN_TITLE + " ASC",
            OpenHelper.COLUMN_TITLE + " DESC",
            OpenHelper.COLUMN_DATE + " ASC",
            OpenHelper.COLUMN_DATE + " DESC"
    };

    public Controller(Context context) {
        helper = App.getDbHelper();
        instance = this;
    }

    /**
     * Instantiates the singleton instance of Controller class
     *
     * @param context the application context
     */
    //  public static void create(Context context) {
    //  instance = new Controller(context);
    //  }

    /**
     * Reads data from json array
     *
     * @param json an array of json objects
     * @throws Exception
     */
    public void readBackup(JSONArray json) throws Exception {
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            int length = json.length();
            for (int i = 0; i < length; i++) {
                JSONObject item = json.getJSONObject(i);
                //self correction for replacing spaces into "_" in title
                ContentValues values = new ContentValues();
                values.put(OpenHelper.COLUMN_ID, item.getLong(OpenHelper.COLUMN_ID));
                values.put(OpenHelper.COLUMN_TITLE, item.getString(OpenHelper.COLUMN_TITLE).replace(" ", "_"));

                try {
                    values.put(OpenHelper.COLUMN_KEYWORDS, item.getString(OpenHelper.COLUMN_KEYWORDS));
                } catch (JSONException e) {
                    values.put(OpenHelper.COLUMN_KEYWORDS, "");
                }
                try {
                    values.put(OpenHelper.COLUMN_REFERENCE, item.getString(OpenHelper.COLUMN_REFERENCE));
                } catch (JSONException e) {
                    values.put(OpenHelper.COLUMN_REFERENCE, "");
                }
                try {
                    values.put(OpenHelper.COLUMN_REMARK, item.getString(OpenHelper.COLUMN_REMARK));
                } catch (JSONException e) {
                    values.put(OpenHelper.COLUMN_REMARK, "");
                }

                try {
                    values.put(OpenHelper.COLUMN_BODY, item.getString(OpenHelper.COLUMN_BODY));
                } catch (JSONException e) {
                    values.put(OpenHelper.COLUMN_BODY, "");
                }

                try {
                    values.put(OpenHelper.COLUMN_EXTRA, item.getString(OpenHelper.COLUMN_EXTRA));
                } catch (JSONException e) {
                    values.put(OpenHelper.COLUMN_EXTRA, "");
                }

                values.put(OpenHelper.COLUMN_TYPE, item.getInt(OpenHelper.COLUMN_TYPE));
                values.put(OpenHelper.COLUMN_DATE, item.getString(OpenHelper.COLUMN_DATE));
                values.put(OpenHelper.COLUMN_ARCHIVED, item.getInt(OpenHelper.COLUMN_ARCHIVED));
                values.put(OpenHelper.COLUMN_PROTECT, item.getInt(OpenHelper.COLUMN_PROTECT));
                values.put(OpenHelper.COLUMN_STARED, item.getInt(OpenHelper.COLUMN_STARED));
                values.put(OpenHelper.COLUMN_THEME, item.getInt(OpenHelper.COLUMN_THEME));
                values.put(OpenHelper.COLUMN_COUNTER, item.getInt(OpenHelper.COLUMN_COUNTER));
                values.put(OpenHelper.COLUMN_PARENT_ID, item.getLong(OpenHelper.COLUMN_PARENT_ID));

                db.replace(
                        OpenHelper.TABLE_NOTES,
                        null,
                        values
                );
            }
        } finally {
            db.close();
        }
    }

    /**
     * Writes data to file
     *
     * @param fos an object of FileOutputStream
     * @throws Exception
     */
    public void writeBackup(FileOutputStream fos) throws Exception {
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    OpenHelper.TABLE_NOTES,
                    null, null, null, null, null, null
            );

            if (cursor != null) {
                boolean needComma = false;
                while (cursor.moveToNext()) {
                    if (needComma) {
                        fos.write(",".getBytes(StandardCharsets.UTF_8));
                    } else {
                        needComma = true;
                    }

                    JSONObject item = new JSONObject();
                    item.put(OpenHelper.COLUMN_ID, cursor.getLong(cursor.getColumnIndex(OpenHelper.COLUMN_ID)));
                    item.put(OpenHelper.COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_TITLE)));
                    item.put(OpenHelper.COLUMN_KEYWORDS, cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_KEYWORDS)));
                    item.put(OpenHelper.COLUMN_REFERENCE, cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_REFERENCE)));
                    item.put(OpenHelper.COLUMN_REMARK, cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_REMARK)));
                    item.put(OpenHelper.COLUMN_BODY, cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_BODY)));
                    item.put(OpenHelper.COLUMN_TYPE, cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_TYPE)));
                    item.put(OpenHelper.COLUMN_DATE, cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_DATE)));
                    item.put(OpenHelper.COLUMN_ARCHIVED, cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_ARCHIVED)));
                    item.put(OpenHelper.COLUMN_PROTECT, cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_PROTECT)));
                    item.put(OpenHelper.COLUMN_STARED, cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_STARED)));
                    item.put(OpenHelper.COLUMN_THEME, cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_THEME)));
                    item.put(OpenHelper.COLUMN_COUNTER, cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_COUNTER)));
                    item.put(OpenHelper.COLUMN_PARENT_ID, cursor.getLong(cursor.getColumnIndex(OpenHelper.COLUMN_PARENT_ID)));
                    item.put(OpenHelper.COLUMN_EXTRA, cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_EXTRA)));

                    fos.write(item.toString().getBytes(StandardCharsets.UTF_8));
                }

                cursor.close();
            }
        } finally {
            db.close();
        }
    }

    /**
     * Reads all notes or categories from database
     *
     * @param cls         the class of the model type
     * @param columns     the columns must be returned from the query
     * @param where       the where clause of the query.
     * @param whereParams the parameters of where clause.
     * @param sortId      the sort id of categories or notes
     * @param <T>         a type which extends DatabaseModel
     * @return a list of notes or categories
     */
    public <T extends DatabaseModel> ArrayList<T> findNotes(Class<T> cls, String[] columns, String where, String[] whereParams, int sortId) {
        ArrayList<T> items = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();
        Log.e("Controlloer", "where =" + where);
        Log.e("Controlloer", "whereParams =" + Arrays.toString(whereParams));
        try {
            Cursor c = db.query(
                    OpenHelper.TABLE_NOTES,
                    columns,
                    where,
                    whereParams,
                    null, null,
                    sorts[sortId]
            );

            if (c != null) {
                while (c.moveToNext()) {
                    try {
                        items.add(cls.getDeclaredConstructor(Cursor.class).newInstance(c));
                    } catch (Exception ignored) {
                    }
                }

                c.close();
            }

            return items;
        } finally {
            db.close();
        }
    }

    /**
     * Reads a note or category from the database
     *
     * @param cls the class of the model type
     * @param id  primary key of note or category
     * @param <T> a type which extends DatabaseModel
     * @return a new object of T type
     */
    public <T extends DatabaseModel> T findNote(Class<T> cls, long id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    OpenHelper.TABLE_NOTES,
                    null,
                    OpenHelper.COLUMN_ID + " = ?",
                    new String[]{
                            String.format(Locale.US, "%d", id)
                    },
                    null, null, null
            );

            if (cursor == null) return null;

            if (cursor.moveToFirst()) {
                try {
                    return cls.getDeclaredConstructor(Cursor.class).newInstance(cursor);
                } catch (Exception e) {
                    return null;
                }
            }

            return null;
        } finally {
            db.close();
        }
    }

    /**
     * Change the amount of category counter
     *
     * @param categoryId the id of category
     * @param amount     to be added (negative or positive)
     */
    public void addCategoryCounter(long categoryId, int amount) {
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            Cursor cursor = db.rawQuery(
                    "UPDATE " + OpenHelper.TABLE_NOTES + " SET " + OpenHelper.COLUMN_COUNTER + " = " + OpenHelper.COLUMN_COUNTER + " + ? WHERE " + OpenHelper.COLUMN_ID + " = ?",
                    new String[]{
                            String.format(Locale.US, "%d", amount),
                            String.format(Locale.US, "%d", categoryId)
                    }
            );

            if (cursor != null) {
                cursor.moveToFirst();
                cursor.close();
            }
        } finally {
            db.close();
        }
    }

    /**
     * Restores last deleted notes
     */
    public void undoDeletion() {
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            Cursor cursor = db.query(
                    OpenHelper.TABLE_UNDO,
                    null, null, null, null, null, null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String query = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_SQL));
                    if (query != null) {
                        Cursor nc = db.rawQuery(
                                query,
                                null
                        );

                        if (nc != null) {
                            nc.moveToFirst();
                            nc.close();
                        }
                    }
                }

                cursor.close();
            }

            clearUndoTable(db);
        } finally {
            db.close();
        }
    }

    /**
     * Clears the undo table
     *
     * @param db an object of writable SQLiteDatabase
     */
    public void clearUndoTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("DELETE FROM " + OpenHelper.TABLE_UNDO, null);
        if (cursor != null) {
            cursor.moveToFirst();
            cursor.close();
        }
    }

    /**
     * Deletes a note or category (and its children) from the database
     *
     * @param ids        a list of the notes' IDs
     * @param categoryId the id of parent category
     */
    public void deleteNotes(String[] ids, long categoryId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            clearUndoTable(db);

            StringBuilder where = new StringBuilder();
            StringBuilder childWhere = new StringBuilder();

            boolean needOR = false;
            for (int i = 0; i < ids.length; i++) {
                if (needOR) {
                    where.append(" OR ");
                    childWhere.append(" OR ");
                } else {
                    needOR = true;
                }
                where.append(OpenHelper.COLUMN_ID).append(" = ?");
                childWhere.append(OpenHelper.COLUMN_PARENT_ID).append(" = ?");
            }

            int count = db.delete(
                    OpenHelper.TABLE_NOTES,
                    where.toString(),
                    ids
            );

            if (categoryId == NEW_MODEL_ID) {
                db.delete(
                        OpenHelper.TABLE_NOTES,
                        childWhere.toString(),
                        ids
                );
            } else {
                Cursor cursor = db.rawQuery(
                        "UPDATE " + OpenHelper.TABLE_NOTES + " SET " + OpenHelper.COLUMN_COUNTER + " = " + OpenHelper.COLUMN_COUNTER + " - ? WHERE " + OpenHelper.COLUMN_ID + " = ?",
                        new String[]{
                                String.format(Locale.US, "%d", count),
                                String.format(Locale.US, "%d", categoryId)
                        }
                );

                if (cursor != null) {
                    cursor.moveToFirst();
                    cursor.close();
                }
            }
        } finally {
            db.close();
        }
    }

    /**
     * Inserts or updates a note or category in the database and increments the counter
     * of category if the deleted object is an instance of Note class
     *
     * @param note   the object of type T
     * @param values ContentValuse of the object to be inserted or updated
     * @param <T>    a type which extends DatabaseModel
     * @return the id of saved note
     */
    public <T extends DatabaseModel> long saveNote(T note, ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            if (note.id > NEW_MODEL_ID) {
                // Update note
                db.update(
                        OpenHelper.TABLE_NOTES,
                        note.getContentValues(),
                        OpenHelper.COLUMN_ID + " = ?",
                        new String[]{
                                String.format(Locale.US, "%d", note.id)
                        }
                );
                return note.id;
            } else {
                // Create a new note
                note.id = db.insert(
                        OpenHelper.TABLE_NOTES,
                        null,
                        values
                );

                if (note instanceof Note) {
                    // Increment the counter of category
                    Cursor cursor = db.rawQuery(
                            "UPDATE " + OpenHelper.TABLE_NOTES + " SET " + OpenHelper.COLUMN_COUNTER + " = " + OpenHelper.COLUMN_COUNTER + " + 1 WHERE " + OpenHelper.COLUMN_ID + " = ?",
                            new String[]{
                                    String.format(Locale.US, "%d", ((Note) note).parentId)
                            }
                    );

                    if (cursor != null) {
                        cursor.moveToFirst();
                        cursor.close();
                    }
                }

                return note.id;
            }
        } catch (Exception e) {
            return NEW_MODEL_ID;
        } finally {
            db.close();
        }
    }

    /**
     * Reads a category by its id
     *
     * @param id primary key of category
     * @return the category object or null if it was not found
     */
    public Category findCategoryById(long id) {
        return findNote(Category.class, id);
    }

    /**
     * Reads all categories
     *
     * @return a list of categories which is populated by database
     */
    // TODO ny not include body to save memory  T
    public ArrayList<Category> findAllCategories() {
        return findNotes(
                Category.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_KEYWORDS,
                        OpenHelper.COLUMN_REMARK,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PROTECT,
                        OpenHelper.COLUMN_STARED,
                        OpenHelper.COLUMN_THEME,
                        OpenHelper.COLUMN_COUNTER,
                        OpenHelper.COLUMN_EXTRA
                },
                OpenHelper.COLUMN_TYPE + " = ? AND " + OpenHelper.COLUMN_ARCHIVED + " = ?",
                new String[]{
                        String.format(Locale.US, "%d", DatabaseModel.TYPE_CATEGORY),
                        "0"
                },
                App.sortCategoriesBy
        );
    }

    /**
     * @return a category id by title, to ensure a unique title
     */
    public Long categoryIdbyTitle(String title) {
        ArrayList<Category> all = findAllCategories();
        for (Category c : all) {
            if (Objects.equals(c.title, title)) return c.id;
        }
        //create a new Category with title
        Category category = new Category();
        category.id = NEW_MODEL_ID;
        category.title = title;
        Long id = category.save();
        return id;
    }

    /**
     * Reads a note by its id
     *
     * @param id primary key of note
     * @return the note object or null if it was not found
     */
    public Note findNote(long id) {
        return findNote(Note.class, id);
    }

    /**
     * Reads all notes
     *
     * @param categoryId the id of parent category
     * @return a list of notes which is populated by database
     */
    public ArrayList<Note> findAllNotesInCategory(long categoryId) {
        String sort = findCategoryById(categoryId).sortBy;
        int sortBy = sort == null || sort.isEmpty() ? App.sortNotesBy : Integer.parseInt(sort);
        return findNotes(
                Note.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_KEYWORDS,
                        OpenHelper.COLUMN_REMARK,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PARENT_ID,
                        OpenHelper.COLUMN_PROTECT,
                        OpenHelper.COLUMN_STARED,
                        OpenHelper.COLUMN_EXTRA
                },
                OpenHelper.COLUMN_TYPE + " != ? AND " + OpenHelper.COLUMN_PARENT_ID + " = ? AND " + OpenHelper.COLUMN_ARCHIVED + " = ?",
                new String[]{
                        String.format(Locale.US, "%d", DatabaseModel.TYPE_CATEGORY),
                        String.format(Locale.US, "%d", categoryId),
                        "0"
                },

                sortBy
        );
    }


    public ArrayList<Note> dateSearch(String date, boolean isEarlier) {

        return findNotes(
                Note.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_KEYWORDS,
                        OpenHelper.COLUMN_REMARK,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PARENT_ID,
                        OpenHelper.COLUMN_PROTECT,
                        OpenHelper.COLUMN_STARED,
                        OpenHelper.COLUMN_EXTRA
                },
                isEarlier ? OpenHelper.COLUMN_DATE + "<= ?" : OpenHelper.COLUMN_DATE + ">= ?",
                new String[]{
                        date
                },
                App.sortNotesBy
        );
    }

    public ArrayList<Note> starSearch() {
        return findNotes(
                Note.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_KEYWORDS,
                        OpenHelper.COLUMN_REMARK,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PARENT_ID,
                        OpenHelper.COLUMN_PROTECT,
                        OpenHelper.COLUMN_STARED,
                        OpenHelper.COLUMN_EXTRA
                },
                OpenHelper.COLUMN_STARED + "= ? AND " + OpenHelper.COLUMN_TYPE + " != ?",
                new String[]{
                        "1",
                        "0"
                },
                App.sortNotesBy
        );
    }


    public ArrayList<Note> simpleSearch(String searchKey) {
        searchKey = searchKey.trim();

        return findNotes(
                Note.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_KEYWORDS,
                        OpenHelper.COLUMN_REMARK,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PARENT_ID,
                        OpenHelper.COLUMN_PROTECT,
                        OpenHelper.COLUMN_STARED,
                        OpenHelper.COLUMN_EXTRA
                },
                OpenHelper.COLUMN_TITLE + " like ? AND " + OpenHelper.COLUMN_TYPE + " != ?  OR "
                        + OpenHelper.COLUMN_KEYWORDS + " like ? OR "
                        + OpenHelper.COLUMN_REMARK + " like ?",
                new String[]{
                        "%" + searchKey + "%",
                        "0",
                        "%" + searchKey + "%",
                        "%" + searchKey + "%"
                },

                App.sortNotesBy
        );
    }

    public ArrayList<Note> deepSearch(String searchKey) {
        searchKey = searchKey.trim();
        if (searchKey.isEmpty()) return null;
        return findNotes(
                Note.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_KEYWORDS,
                        OpenHelper.COLUMN_REMARK,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PARENT_ID,
                        OpenHelper.COLUMN_PROTECT,
                        OpenHelper.COLUMN_STARED,
                        OpenHelper.COLUMN_EXTRA
                },
                OpenHelper.COLUMN_TITLE + " like ? AND " + OpenHelper.COLUMN_TYPE + " != ?  OR "
                        + OpenHelper.COLUMN_KEYWORDS + " like ? OR "
                        + OpenHelper.COLUMN_REMARK + " like ?  OR "
                        + OpenHelper.COLUMN_BODY + " like ? ",
                new String[]{
                        "%" + searchKey + "%",
                        "0",
                        "%" + searchKey + "%",
                        "%" + searchKey + "%",
                        "%" + searchKey + "%"
                },

                App.sortNotesBy
        );
    }

    public String idToTitle(long id) {
        return findNote(id).title;
    }

    //to check whether the title has been used in the same category, return -1 for unused.
    public long titleToId(long parentId, String title) {
        ArrayList<Note> all = findAllNotesInCategory(parentId);
        for (Note note : all) {
            if (note.title.equals(title)) return note.id;
        }
        return NEW_MODEL_ID;
    }

    public long titleToId(String title) {
        ArrayList<Note> all = simpleSearch(title);
        for (Note note : all) {
            if (note.title.equals(title)) return note.id;
        }
        return NEW_MODEL_ID;
    }

    public String formatTitle(long parentId, String title) {
        int i = 2;
        //auto correction of space in the title
        String newTitle = title.replace(" ", "_");
        while (titleToId(parentId, newTitle) != NEW_MODEL_ID) {
            newTitle = String.format(Locale.US, "%s(%d)", title, i);
            i++;
        }
        return newTitle;
    }

    public void jsonToDatabase(Context context, String jsonPath, String dataName) throws Exception {
        // Step 1: Read JSON data from the backup file
        JSONObject jsonObject = readJsonFromPath(jsonPath);

        // Step 2: Create a temporary database in external storage
        File tempDbFile = new File(NyFileUtil.getAppDirectory(context), dataName);
        OpenHelper tempOpenHelper = new OpenHelper(context, tempDbFile.getAbsolutePath());

        SQLiteDatabase db = tempOpenHelper.getWritableDatabase();
        db.rawQuery("PRAGMA journal_mode=WAL;", null).close(); // Set WAL mode properly
        db.close(); // Close immediately after setting WAL mode

        jsonToDatabase(tempOpenHelper, jsonObject);
    }

    public void jsonToDatabase(OpenHelper OpenHelper, JSONObject jsonObject) throws Exception {
        SQLiteDatabase db = OpenHelper.getWritableDatabase();
        db.beginTransaction(); // Begin transaction
        try {
            // Process notes array
            if (jsonObject.has(OpenHelper.TABLE_NOTES)) {
                JSONArray notesArray = jsonObject.getJSONArray(OpenHelper.TABLE_NOTES);
                int notesLength = notesArray.length();

                for (int i = 0; i < notesLength; i++) {
                    JSONObject item = notesArray.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(OpenHelper.COLUMN_ID, item.getLong(OpenHelper.COLUMN_ID));
                    values.put(OpenHelper.COLUMN_TITLE, item.getString(OpenHelper.COLUMN_TITLE).replace(" ", "_"));

                    values.put(OpenHelper.COLUMN_KEYWORDS, item.optString(OpenHelper.COLUMN_KEYWORDS, ""));
                    values.put(OpenHelper.COLUMN_REFERENCE, item.optString(OpenHelper.COLUMN_REFERENCE, ""));
                    values.put(OpenHelper.COLUMN_REMARK, item.optString(OpenHelper.COLUMN_REMARK, ""));
                    values.put(OpenHelper.COLUMN_BODY, item.optString(OpenHelper.COLUMN_BODY, ""));
                    values.put(OpenHelper.COLUMN_EXTRA, item.optString(OpenHelper.COLUMN_EXTRA, ""));
                    values.put(OpenHelper.COLUMN_TYPE, item.getInt(OpenHelper.COLUMN_TYPE));
                    values.put(OpenHelper.COLUMN_DATE, item.getString(OpenHelper.COLUMN_DATE));
                  //  values.put(OpenHelper.COLUMN_MODIFICATION, item.getString(OpenHelper.COLUMN_MODIFICATION));
                    values.put(OpenHelper.COLUMN_ARCHIVED, item.getInt(OpenHelper.COLUMN_ARCHIVED));
                    values.put(OpenHelper.COLUMN_PROTECT, item.getInt(OpenHelper.COLUMN_PROTECT));
                    values.put(OpenHelper.COLUMN_STARED, item.getInt(OpenHelper.COLUMN_STARED));
                    values.put(OpenHelper.COLUMN_THEME, item.getInt(OpenHelper.COLUMN_THEME));
                    values.put(OpenHelper.COLUMN_COUNTER, item.getInt(OpenHelper.COLUMN_COUNTER));
                    values.put(OpenHelper.COLUMN_PARENT_ID, item.getLong(OpenHelper.COLUMN_PARENT_ID));

                    db.replace(OpenHelper.TABLE_NOTES, null, values);
                }
            }

            // Process metadata array
          /*  if (jsonObject.has(OpenHelper.TABLE_SYNC)) {
                JSONArray metadataArray = jsonObject.getJSONArray(OpenHelper.TABLE_SYNC);
                int metadataLength = metadataArray.length();

                for (int i = 0; i < metadataLength; i++) {
                    JSONObject item = metadataArray.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(OpenHelper.COLUMN_KEY, item.getString(OpenHelper.COLUMN_KEY));
                    values.put(OpenHelper.COLUMN_VALUE, item.getString(OpenHelper.COLUMN_VALUE));

                    db.replace(OpenHelper.TABLE_SYNC, null, values);
                }
            }*/

            db.setTransactionSuccessful(); // Mark the transaction as successful
        } finally {
            db.endTransaction(); // End transaction
            db.close();
        }
    }


    /**
     * Writes data to file
     *
     * @param fos an object of FileOutputStream
     * @throws Exception
     */
   /* public void writeBackup(FileOutputStream fos) throws Exception {
        ((OpenHelper) helper).saveToJson(fos);
    }*/

    /**
     * Reads data from a JSON object
     *
     * @param jsonObject the JSON object containing notes and metadata arrays
     * @throws Exception
     */
    public void readBackup(JSONObject jsonObject) throws Exception {
        // SQLiteDatabase db = helper.getWritableDatabase();  // Should use getWritableDatabase for write operations
        jsonToDatabase((OpenHelper) helper, jsonObject);
    }

}
