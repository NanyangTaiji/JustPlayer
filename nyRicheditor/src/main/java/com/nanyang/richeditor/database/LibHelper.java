package com.nanyang.richeditor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LibHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String MASTER_DB_NAME = "master.db";
    private static final String TABLE_DATALIB = "DataLib";

    // Column names for DataLib table
    private static final String COLUMN_ORDER = "_order";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_UPDATE_DATE = "updateDate";
    private static final String COLUMN_IS_MODIFIED = "isModified";

    private int totalLib = -1;

    public LibHelper(Context context) {
        super(context, MASTER_DB_NAME, null, VERSION);
        // Ensuring that the database is created and then initializing it with default DataLibs if empty
        SQLiteDatabase db = this.getWritableDatabase();
        initializeDatabaseWithDefaultData(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDataLibTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DATALIB + " (" +
                COLUMN_ORDER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_UPDATE_DATE + " INTEGER, " +
                COLUMN_IS_MODIFIED + " INTEGER DEFAULT 0" +
                ")";
        db.execSQL(createDataLibTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade as needed
    }

    private void initializeDatabaseWithDefaultData(SQLiteDatabase db) {
        // Check if the table has entries, if not, insert default DataLib
        if (getDataLibCount(db) == 0) {
            insertDataLib(db, new DataLib("data0.db", "Lib_1"));
            insertDataLib(db, new DataLib("data1.db", "Lib_2"));
            insertDataLib(db, new DataLib("data2.db", "Lib_3"));
        }
    }

    // Method to get the total count of DataLib entries
    public int getDataLibCount() {
        if (totalLib != -1) return totalLib;
        SQLiteDatabase db = this.getReadableDatabase();
        return getDataLibCount(db);
    }

    // Private method to get the total count of DataLib entries using a given database reference
    private int getDataLibCount(SQLiteDatabase db) {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_DATALIB;
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalLib = cursor.getInt(0);
            }
            cursor.close();
        }
        return totalLib;
    }

    // Method to insert a new DataLib entry
    public long insertDataLib(DataLib dataLib) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertDataLib(db, dataLib);
    }

    // Private method to insert a new DataLib entry using a given database reference
    private long insertDataLib(SQLiteDatabase db, DataLib dataLib) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, dataLib.getName());
        values.put(COLUMN_TITLE, dataLib.getTitle());
        values.put(COLUMN_UPDATE_DATE, dataLib.getUpdateDate() == -1L ? System.currentTimeMillis() : dataLib.getUpdateDate());
        values.put(COLUMN_IS_MODIFIED, dataLib.isModified() ? 1 : 0);

        long id = db.insert(TABLE_DATALIB, null, values);
        totalLib = getDataLibCount(db); // Recalculate totalLib after insertion
        return id;
    }

    // Method to retrieve a DataLib by order
    public DataLib getDataLibByOrder(int order) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATALIB, null, COLUMN_ORDER + "=?", new String[]{String.valueOf(order)}, null, null, null);

        DataLib dataLib = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                dataLib = new DataLib(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                );
                dataLib.setUpdateDate(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATE_DATE)));
                dataLib.setModified(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MODIFIED)) == 1);
            }
            cursor.close();
        }
        return dataLib;
    }

    // Method to swap the order of two DataLib entries
    public void swapDataLibOrder(DataLib dataLib1, DataLib dataLib2) {
        int order1 = dataLib1.getOrder();
        int order2 = dataLib2.getOrder();

        // Update the order of dataLib1 with order2
        ContentValues values1 = new ContentValues();
        values1.put(COLUMN_ORDER, order2);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_DATALIB, values1, COLUMN_ORDER + " = ?", new String[]{String.valueOf(order1)});

        // Update the order of dataLib2 with order1
        ContentValues values2 = new ContentValues();
        values2.put(COLUMN_ORDER, order1);
        db.update(TABLE_DATALIB, values2, COLUMN_ORDER + " = ?", new String[]{String.valueOf(order2)});
    }

    // Method to retrieve a DataLib by name
    public DataLib getDataLibByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATALIB, null, COLUMN_NAME + "=?", new String[]{name}, null, null, null);

        DataLib dataLib = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                dataLib = new DataLib(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                );
                dataLib.setUpdateDate(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATE_DATE)));
                dataLib.setModified(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MODIFIED)) == 1);
            }
            cursor.close();
        }
        return dataLib;
    }

    // Method to update a DataLib entry
    public void updateDataLib(DataLib dataLib) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER, dataLib.getOrder());
        values.put(COLUMN_NAME, dataLib.getName());
        values.put(COLUMN_TITLE, dataLib.getTitle());
        values.put(COLUMN_UPDATE_DATE, dataLib.getUpdateDate());
        values.put(COLUMN_IS_MODIFIED, dataLib.isModified() ? 1 : 0);
        db.update(TABLE_DATALIB, values, COLUMN_ORDER + " = ?", new String[]{String.valueOf(dataLib.getOrder())});
    }

    // Method to delete a DataLib entry by order
    public void deleteDataLib(int order) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATALIB, COLUMN_ORDER + " = ?", new String[]{String.valueOf(order)});
        // Decrement order for entries with order greater than the deleted one
        Cursor cursor = db.query(TABLE_DATALIB, null, COLUMN_ORDER + " > ?", new String[]{String.valueOf(order)}, null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            DataLib dataLib = getDataLibByOrder(cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER)));
            dataLib.setOrder(dataLib.getOrder() - 1);
            updateDataLib(dataLib);
        }
        if (cursor != null) cursor.close();
        totalLib--; // Decrease totalLib count
    }

    // Method to get all DataLib entries
    public List<DataLib> getAllDataLibs() {
        List<DataLib> dataLibList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATALIB, null, null, null, null, null, COLUMN_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DataLib dataLib = new DataLib(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                );
                dataLib.setUpdateDate(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATE_DATE)));
                dataLib.setModified(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MODIFIED)) == 1);
                dataLibList.add(dataLib);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return dataLibList;
    }

    // Method to clear all DataLib entries from the database
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_DATALIB, null, null);
            db.setTransactionSuccessful();
            totalLib = 0;
        } finally {
            db.endTransaction();
        }
    }
}

   /* public void swapDataLibOrder(int order1, int order2) {
        // Retrieve the DataLib entries by their order
        DataLib lib1 = getDataLibByOrder(order1);
        DataLib lib2 = getDataLibByOrder(order2);

        if (lib1 == null || lib2 == null) {
            // One or both of the DataLib entries do not exist
            return;
        }

        // Swap their order values
        int tempOrder = lib1.getOrder();
        lib1.setOrder(lib2.getOrder());
        lib2.setOrder(tempOrder);

        // Update the database with the new order values
        updateDataLib(lib1);
        updateDataLib(lib2);
    }*/
