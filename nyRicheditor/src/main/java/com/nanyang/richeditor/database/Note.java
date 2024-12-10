package com.nanyang.richeditor.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;

import com.nanyang.richeditor.App;

import java.util.ArrayList;
import java.util.Locale;

public class Note extends DatabaseModel {
    public long parentId;
    public String body;
    public String reference;
    public String remark;

    public Note() {
    }

    /**
     * Instantiates a new object of Note class using the data retrieved from database.
     *
     * @param cursor cursor object returned from a database query
     */
    @SuppressLint("Range")
    public Note(Cursor cursor) {
        super(cursor);
        this.parentId = cursor.getLong(cursor.getColumnIndex(OpenHelper.COLUMN_PARENT_ID));
        try {
            this.body = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_BODY));
            this.reference = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_REFERENCE));
            this.remark = cursor.getString(cursor.getColumnIndex(OpenHelper.COLUMN_REMARK));
        } catch (Exception ignored) {
        }
    }

    /**
     * @return ContentValue object to be saved or updated
     */
    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        //TODO ny do not modify
        if (id == DatabaseModel.NEW_MODEL_ID) {
            values.put(OpenHelper.COLUMN_TYPE, type);
            values.put(OpenHelper.COLUMN_ARCHIVED, isArchived);
        }
        if (id != DatabaseModel.NEW_MODEL_ID) {
            values.put(OpenHelper.COLUMN_ID, id);
        }
        values.put(OpenHelper.COLUMN_PARENT_ID, parentId);
        values.put(OpenHelper.COLUMN_TITLE, title);
        values.put(OpenHelper.COLUMN_KEYWORDS, keywords);
        values.put(OpenHelper.COLUMN_REFERENCE, reference);
        values.put(OpenHelper.COLUMN_REMARK, remark);
        values.put(OpenHelper.COLUMN_DATE, datelong);
        values.put(OpenHelper.COLUMN_BODY, body);
        values.put(OpenHelper.COLUMN_EXTRA, secureKey);
        values.put(OpenHelper.COLUMN_PROTECT, isProtected ? 1 : 0);
        values.put(OpenHelper.COLUMN_STARED, isStard ? 1 : 0);
        return values;
    }

   /* public ArrayList<Note> search(String keyword) {
        ArrayList<Note> results = null;
        try {
            SQLiteDatabase sqLiteDatabase = helper.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + contactTable + " where " + nameColumn + " like ?", new String[] { "%" + keyword + "%" });
            if (cursor.moveToFirst()) {
                results = new ArrayList<Contact>();
                do {
                    Contact contact = new Contact();
                    contact.setId(cursor.getInt(0));
                    contact.setName(cursor.getString(1));
                    contact.setPhone(cursor.getString(2));
                    contact.setAddress(cursor.getString(3));
                    contact.setEmail(cursor.getString(4));
                    contact.setDescription(cursor.getString(5));
                    results.add(contact);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            results = null;
        }
        return results;
    }*/


    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Note && id == (((Note) o).id);
    }
}
