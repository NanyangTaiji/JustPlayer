package com.nytaiji.cloud;


import static com.nytaiji.cloud.CloudContract.KEYS_DB;
import static com.nytaiji.cloud.CloudContract.PROVIDER_AUTHORITY;
import static com.nytaiji.cloud.CloudContract.TABLE_SECRET;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.net.Uri;

import com.nytaiji.cloud.database.DbCursor;
import com.nytaiji.cloud.database.DbHelper;

public class CloudProvider extends ContentProvider {

    /* renamed from: b */
    private static UriMatcher uriMatcher;

    /* renamed from: a */
    private DbHelper dbHelper;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        CloudProvider.uriMatcher = uriMatcher;
        uriMatcher.addURI(PROVIDER_AUTHORITY, "/" + KEYS_DB + "/" + TABLE_SECRET, 1);
        CloudProvider.uriMatcher.addURI(PROVIDER_AUTHORITY, "/" + KEYS_DB + "/" + TABLE_SECRET + "/#", 2);
    }

    /* renamed from: a */
    public DbCursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        DbCursor dbCursor;
        int match = uriMatcher.match(uri);
        if (match == 1) {
            dbCursor = this.dbHelper.searchSecretKey(str, strArr2);
        } else if (match != 2) {
            return null;
        } else {
            dbCursor = this.dbHelper.checkId(uri.getLastPathSegment());
        }
        if (dbCursor == null || dbCursor.getCount() <= 0 || !dbCursor.moveToFirst() || dbCursor.getString(1) != null) {
            return dbCursor;
        }
        return null;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        return -1;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public boolean onCreate() {
        this.dbHelper = new DbHelper(getContext());
        return true;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return -1;
    }
}
