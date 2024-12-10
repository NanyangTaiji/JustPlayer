package com.nytaiji.cloud.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

/* renamed from: b.a.a.b.a */
public class DbCursor implements Cursor {

    /* renamed from: a */
    private Cursor cursor;

    /* renamed from: b */
    private Context context;

    public DbCursor(Context context, Cursor cursor) {
        this.cursor = cursor;
        this.context = context;
    }

    public void close() {
        this.cursor.close();
    }

    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        this.cursor.copyStringToBuffer(i, charArrayBuffer);
    }

    public void deactivate() {
        this.cursor.deactivate();
    }

    public byte[] getBlob(int i) {
        return this.cursor.getBlob(i);
    }

    public int getColumnCount() {
        return this.cursor.getColumnCount();
    }

    public int getColumnIndex(String str) {
        return this.cursor.getColumnIndex(str);
    }

    public int getColumnIndexOrThrow(String str) {
        return this.cursor.getColumnIndexOrThrow(str);
    }

    public String getColumnName(int i) {
        return this.cursor.getColumnName(i);
    }

    public String[] getColumnNames() {
        return this.cursor.getColumnNames();
    }

    public int getCount() {
        return this.cursor.getCount();
    }

    public double getDouble(int i) {
        return this.cursor.getDouble(i);
    }

    public Bundle getExtras() {
        return this.cursor.getExtras();
    }

    public float getFloat(int i) {
        return (float) this.cursor.getLong(i);
    }

    public int getInt(int i) {
        return this.cursor.getInt(i);
    }

    public long getLong(int i) {
        return this.cursor.getLong(i);
    }

    public Uri getNotificationUri() {
        if (Build.VERSION.SDK_INT >= 19) {
            return this.cursor.getNotificationUri();
        }
        return null;
    }

    public int getPosition() {
        return this.cursor.getPosition();
    }

    public short getShort(int i) {
        return this.cursor.getShort(i);
    }

    public String getString(int i) {
        try {
            return this.cursor.getString(i);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getType(int i) {
        return this.cursor.getType(i);
    }

    public boolean getWantsAllOnMoveCalls() {
        return this.cursor.getWantsAllOnMoveCalls();
    }

    public boolean isAfterLast() {
        return this.cursor.isAfterLast();
    }

    public boolean isBeforeFirst() {
        return this.cursor.isBeforeFirst();
    }

    public boolean isClosed() {
        return this.cursor.isClosed();
    }

    public boolean isFirst() {
        return this.cursor.isFirst();
    }

    public boolean isLast() {
        return this.cursor.isLast();
    }

    public boolean isNull(int i) {
        return this.cursor.isNull(i);
    }

    public boolean move(int i) {
        return this.cursor.move(i);
    }

    public boolean moveToFirst() {
        return this.cursor.moveToFirst();
    }

    public boolean moveToLast() {
        return this.cursor.moveToLast();
    }

    public boolean moveToNext() {
        return this.cursor.moveToNext();
    }

    public boolean moveToPosition(int i) {
        return this.cursor.moveToPosition(i);
    }

    public boolean moveToPrevious() {
        return this.cursor.moveToPrevious();
    }

    public void registerContentObserver(ContentObserver contentObserver) {
        this.cursor.registerContentObserver(contentObserver);
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        this.cursor.registerDataSetObserver(dataSetObserver);
    }

    public boolean requery() {
        return this.cursor.requery();
    }

    public Bundle respond(Bundle bundle) {
        return this.cursor.respond(bundle);
    }

    public void setExtras(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23) {
            this.cursor.setExtras(bundle);
        }
    }

    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        this.cursor.setNotificationUri(contentResolver, uri);
    }

    public void unregisterContentObserver(ContentObserver contentObserver) {
        this.cursor.unregisterContentObserver(contentObserver);
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        this.cursor.unregisterDataSetObserver(dataSetObserver);
    }
}
