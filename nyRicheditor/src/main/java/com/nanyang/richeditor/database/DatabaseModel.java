package com.nanyang.richeditor.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.nanyang.richeditor.App;
import com.nanyang.richeditor.R;


abstract public class DatabaseModel {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_NOTE = 1;
    public static final int TYPE_WEBSITE = 2;
    public static final int TYPE_YOUTUBE = 3;

    public static final long NEW_MODEL_ID = -1;
    public static final long SEARCH_GLOBAL = -2;
    public static final long SEARCH_CLICK = -3;
    public static final long SEARCH_SELECT = -4;
    public long id = NEW_MODEL_ID;

    public int type;
    public String title;
    public String keywords;
    public String secureKey;
    public long datelong;
    public boolean isArchived;
    public boolean isProtected;
    public boolean isStard;
    public int theme;

    //the position is not saved in database
    public int position = 0;

    public DatabaseModel() {
    }

    /**
     * Instantiates a new object of DatabaseModel class using the data retrieved from database.
     *
     * @param cursor cursor object returned from a database query
     */
    public DatabaseModel(Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndex(OpenHelper.COLUMN_ID));
        this.type = cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_TYPE));
        this.title = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_TITLE));
        this.keywords = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_KEYWORDS));
        this.secureKey = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_EXTRA));
        try {
            this.datelong = Long.parseLong(cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_DATE)));
        } catch (NumberFormatException nfe) {
           // Log.e("DataBaseModel", "NumberFormatException " + nfe.toString());
            this.datelong = System.currentTimeMillis();
        }
        this.isArchived = cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_ARCHIVED)) == 1;
        this.isProtected = cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_PROTECT)) == 1;
        this.isStard = cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_STARED)) == 1;
    }

    /**
     * Inserts or updates a note or category
     *
     * @return true if the note is saved.
     */
    public long save() {
        return new Controller(App.instance).saveNote(this, getContentValues());
    }

    /**
     * Toggle archived state and
     *
     * @return true if the action is completed.
     */
   /* public boolean archiveToggle() {
        ContentValues values = new ContentValues();
        values.put(OpenHelper.COLUMN_ARCHIVED, !isArchived);

        if (Controller.instance.saveNote(this, values) != DatabaseModel.NEW_MODEL_ID) {
            isArchived = !isArchived;
            return true;
        }

        return false;
    }*/

    public boolean starToggle() {
        ContentValues values = new ContentValues();
        values.put(OpenHelper.COLUMN_STARED, !isStard);

        if (new Controller(App.instance.getApplicationContext()).saveNote(this, values) != DatabaseModel.NEW_MODEL_ID) {
            isStard = !isStard;
            return true;
        }

        return false;
    }

    public boolean protectionToggle() {
        ContentValues values = new ContentValues();
        values.put(OpenHelper.COLUMN_PROTECT, !isProtected);

        if (new Controller(App.instance).saveNote(this, values) != DatabaseModel.NEW_MODEL_ID) {
            isProtected = !isProtected;
            return true;
        }

        return false;
    }


    /**
     * @return color of the theme
     */
    public int getThemeBackground() {
        switch (theme) {
            case Category.THEME_RED:
                return R.drawable.circle_red;
            case Category.THEME_PINK:
                return R.drawable.circle_pink;
            case Category.THEME_AMBER:
                return R.drawable.circle_amber;
            case Category.THEME_BLUE:
                return R.drawable.circle_blue;
            case Category.THEME_CYAN:
                return R.drawable.circle_cyan;
            case Category.THEME_GREEN:
                return R.drawable.circle_green;
            case Category.THEME_ORANGE:
                return R.drawable.circle_orange;
            case Category.THEME_PURPLE:
                return R.drawable.circle_purple;
            case Category.THEME_TEAL:
                return R.drawable.circle_teal;
        }

        return R.drawable.circle_main;
    }

    /**
     * @return ContentValue object to be saved or updated
     */
    abstract public ContentValues getContentValues();

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof DatabaseModel && id == (((DatabaseModel) o).id);
    }
}
