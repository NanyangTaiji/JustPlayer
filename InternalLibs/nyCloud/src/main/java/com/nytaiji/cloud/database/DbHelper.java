package com.nytaiji.cloud.database;

import static com.nytaiji.cloud.CloudContract.COLUMN_CLIENT_ID;
import static com.nytaiji.cloud.CloudContract.COLUMN_CLIENT_SECRET_KEY;
import static com.nytaiji.cloud.CloudContract.COLUMN_ID;
import static com.nytaiji.cloud.CloudContract.KEYS_DB;
import static com.nytaiji.cloud.CloudContract.KEY_DATABASE_VERSION;
import static com.nytaiji.cloud.CloudContract.TABLE_SECRET;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nytaiji.cloud.CloudAccounts;

/* renamed from: b.a.a.a.a */
public class DbHelper extends SQLiteOpenHelper {

    /* renamed from: a */
    private final Context context;

    public static void initCloudDbHelper(Context context) {
        DbHelper dbHelper = new DbHelper(context);
        //check CloudRail
        if (dbHelper.checkId("1") == null || dbHelper.checkId("1").getString(1) == null) {
            dbHelper.delete(CloudAccounts.values().length);
            dbHelper.saveCloudInfos(CloudAccounts.values());
        }
    }

    public DbHelper(Context context) {
        super(context, KEYS_DB, (SQLiteDatabase.CursorFactory) null, KEY_DATABASE_VERSION, (DatabaseErrorHandler) null);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE " + TABLE_SECRET + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_CLIENT_ID + " TEXT," + COLUMN_CLIENT_SECRET_KEY + " TEXT)");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SECRET);
        onCreate(sQLiteDatabase);
    }

    /* renamed from: a */
    public void delete(int i) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        for (int i2 = 1; i2 <= i; i2++) {
            writableDatabase.delete(TABLE_SECRET, COLUMN_ID + " = ?", new String[]{i2 + ""});
        }
        writableDatabase.close();
    }

    /* renamed from: b */
    public DbCursor checkId(String id) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor rawQuery = readableDatabase.rawQuery("SELECT * FROM " + TABLE_SECRET + " where " + COLUMN_ID + " = \"" + id + "\"", (String[]) null);
        if (rawQuery.moveToFirst()) {
            DbCursor cursor = new DbCursor(this.context, rawQuery);
            readableDatabase.close();
            return cursor;
        }
        readableDatabase.close();
        return null;
    }

    /* renamed from: c */
    public DbCursor searchSecretKey(String str, String[] strArr) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor query = readableDatabase.query(TABLE_SECRET, (String[]) null, str + " in (?, ?, ?, ?, ?, ?)", strArr, (String) null, (String) null, (String) null);
        if (query.moveToFirst()) {
            DbCursor dbCursor = new DbCursor(this.context, query);
            readableDatabase.close();
            return dbCursor;
        }
        readableDatabase.close();
        return null;
    }

    /* renamed from: d */
    public void saveCloudInfos(CloudAccounts[] cloudInfors) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        for (CloudAccounts cloudInfor : cloudInfors) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_CLIENT_ID, cloudInfor.getClientId());
                contentValues.put(COLUMN_CLIENT_SECRET_KEY, cloudInfor.getClientKey());
                writableDatabase.insert(TABLE_SECRET, (String) null, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        writableDatabase.close();
    }
}
