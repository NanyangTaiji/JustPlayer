package com.nanyang.richeditor.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.nanyang.richeditor.App;
import com.nanyang.richeditor.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class Category extends DatabaseModel {

    public static final int THEME_RED = 0;
    public static final int THEME_PINK = 1;
    public static final int THEME_PURPLE = 2;
    public static final int THEME_BLUE = 3;
    public static final int THEME_CYAN = 4;
    public static final int THEME_TEAL = 5;
    public static final int THEME_GREEN = 6;
    public static final int THEME_AMBER = 7;
    public static final int THEME_ORANGE = 8;

    public int counter;
    public String sortBy;           //borrow for Sorting criteria

    public Category() {
    }

    /**
     * Instantiates a new object of Category class using the data retrieved from database.
     *
     * @param cursor cursor object returned from a database query
     */
    public Category(Cursor cursor) {
        super(cursor);
        this.theme = cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_THEME));
        this.counter = cursor.getInt(cursor.getColumnIndex(OpenHelper.COLUMN_COUNTER));
        this.sortBy = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_REMARK));
    }

    /**
     * @return ContentValue object to be saved or updated
     */
    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        if (id == DatabaseModel.NEW_MODEL_ID) {
            values.put(OpenHelper.COLUMN_TYPE, type);
            values.put(OpenHelper.COLUMN_DATE, datelong);
            values.put(OpenHelper.COLUMN_COUNTER, counter);
            values.put(OpenHelper.COLUMN_ARCHIVED, isArchived);
        }

        values.put(OpenHelper.COLUMN_TITLE, title);
        values.put(OpenHelper.COLUMN_KEYWORDS, keywords);
        values.put(OpenHelper.COLUMN_EXTRA, secureKey);
        values.put(OpenHelper.COLUMN_REMARK, sortBy);
        values.put(OpenHelper.COLUMN_THEME, theme);
        values.put(OpenHelper.COLUMN_PROTECT, isProtected ? 1 : 0);
        values.put(OpenHelper.COLUMN_STARED, isStard ? 1 : 0);
        return values;
    }


    /**
     * @param theme the color id of category
     * @return the style of theme
     */
    public static int getStyle(int theme) {
        switch (theme) {
            case THEME_RED:
                return R.style.AppThemeRed;
            case THEME_PINK:
                return R.style.AppThemePink;
            case THEME_AMBER:
                return R.style.AppThemeAmber;
            case THEME_BLUE:
                return R.style.AppThemeBlue;
            case THEME_CYAN:
                return R.style.AppThemeCyan;
            case THEME_GREEN:
                return R.style.AppThemeGreen;
            case THEME_ORANGE:
                return R.style.AppThemeOrange;
            case THEME_PURPLE:
                return R.style.AppThemePurple;
            case THEME_TEAL:
                return R.style.AppThemeTeal;
        }

        return R.style.JsonViewTheme;
    }


    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Category && id == (((Category) o).id);
    }
}
