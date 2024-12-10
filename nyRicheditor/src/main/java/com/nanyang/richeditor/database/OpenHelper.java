package com.nanyang.richeditor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenHelper extends SQLiteOpenHelper {
	private static final int VERSION = 3;
	//private static final String NAME = "data0.db";

	public static final String TABLE_NOTES = "notes";
	public static final String TABLE_UNDO = "undo";

	//the position is not saved in database
	public static final String POSITION = "position";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "_title";
	public static final String COLUMN_KEYWORDS = "_keywords";
	public static final String COLUMN_REFERENCE = "_reference";
	public static final String COLUMN_REMARK = "_remark";
    public static final String COLUMN_BODY = "_body";
    public static final String COLUMN_TYPE = "_type";
    public static final String COLUMN_DATE = "_date";
    public static final String COLUMN_ARCHIVED = "_archived";
	public static final String COLUMN_STARED = "_stared";
    public static final String COLUMN_THEME = "_theme";
    public static final String COLUMN_COUNTER = "_counter";
    public static final String COLUMN_PARENT_ID = "_parent";
    public static final String COLUMN_EXTRA = "_extra";
    public static final String COLUMN_SQL = "_sql";
    public static final String COLUMN_PROTECT = "_protected";

	public OpenHelper(Context context, String name) {
		super(context, name, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Main table to store notes and categories
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + " (" +
				COLUMN_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_PARENT_ID + " INTEGER DEFAULT -1, " +
				COLUMN_TITLE     + " TEXT DEFAULT '', " +
				COLUMN_KEYWORDS  + " TEXT DEFAULT '', " +
				COLUMN_REFERENCE  + " TEXT DEFAULT '', " +
				COLUMN_REMARK  + " TEXT DEFAULT '', " +
				COLUMN_BODY      + " TEXT DEFAULT '', " +
				COLUMN_TYPE      + " INTEGER DEFAULT 0, " +
				COLUMN_ARCHIVED  + " INTEGER DEFAULT 0, " +
				COLUMN_PROTECT   + " INTEGER DEFAULT 0, " +
				COLUMN_STARED   + " INTEGER DEFAULT 0, " +
				COLUMN_THEME     + " INTEGER DEFAULT 0, " +
				COLUMN_COUNTER   + " INTEGER DEFAULT 0, " +
				COLUMN_DATE      + " TEXT DEFAULT '', " +
				COLUMN_EXTRA     + " TEXT DEFAULT ''" +
				")"
		);

		// Undo table to make delete queries restorable
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + TABLE_UNDO + " (" +
				COLUMN_SQL + " TEXT" +
				")"
		);

		// A trigger to empty UNDO table, add restoring sql query to UNDO table, then delete all child notes before deleting the parent note
		db.execSQL(
			"CREATE TRIGGER IF NOT EXISTS _t1_dn BEFORE DELETE ON " + TABLE_NOTES + " BEGIN " +
				"INSERT INTO " + TABLE_UNDO + " VALUES('INSERT INTO " + TABLE_NOTES +
				"(" + COLUMN_ID + "," + COLUMN_PARENT_ID + "," + COLUMN_TITLE + ","+ COLUMN_KEYWORDS+ ","  + COLUMN_BODY + "," + COLUMN_TYPE + "," + COLUMN_ARCHIVED+ ","+ COLUMN_PROTECT  + "," + COLUMN_STARED  + "," + COLUMN_THEME + "," + COLUMN_COUNTER + "," + COLUMN_DATE + "," + COLUMN_EXTRA + ")" +
				"VALUES('||old." + COLUMN_ID + "||','||old." + COLUMN_PARENT_ID + "||','||quote(old." + COLUMN_TITLE + ")||','||quote(old." + COLUMN_KEYWORDS + ")||','||quote(old."+ COLUMN_BODY + ")||','||old." + COLUMN_TYPE + "||','||old." + COLUMN_ARCHIVED + "||','||old."  + COLUMN_PROTECT + "||','||old."+ COLUMN_STARED + "||','||old."+ COLUMN_THEME + "||','||old." + COLUMN_COUNTER + "||','||quote(old." + COLUMN_DATE + ")||','||quote(old." + COLUMN_EXTRA + ")||')'); END"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}


	/*public static void closeDatabase(){
		if (mInstance!=null) {
			mInstance.close();
			mInstance = null;
		}
	}*/

	/*public void saveToJson(FileOutputStream fos) throws Exception {
		SQLiteDatabase db = getReadableDatabase();

		Cursor cursorNotes = null;
		Cursor cursorMetadata = null;

		try {
			cursorNotes = db.query(TABLE_NOTES, null, null, null, null, null, null);
			cursorMetadata = db.query(TABLE_SYNC, null, null, null, null, null, null);

			JSONObject jsonBackup = new JSONObject();
			JSONArray notesArray = new JSONArray();
			JSONArray metadataArray = new JSONArray();

			if (cursorNotes != null && cursorNotes.moveToFirst()) {
				do {
					JSONObject note = new JSONObject();
					note.put(COLUMN_ID, cursorNotes.getLong(cursorNotes.getColumnIndex(COLUMN_ID)));
					note.put(COLUMN_TITLE, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_TITLE)));
					note.put(COLUMN_KEYWORDS, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_KEYWORDS)));
					note.put(COLUMN_REFERENCE, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_REFERENCE)));
					note.put(COLUMN_REMARK, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_REMARK)));
					note.put(COLUMN_BODY, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_BODY)));
					note.put(COLUMN_TYPE, cursorNotes.getInt(cursorNotes.getColumnIndex(COLUMN_TYPE)));
					note.put(COLUMN_DATE, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_DATE)));
					note.put(COLUMN_MODIFICATION, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_MODIFICATION)));
					note.put(COLUMN_ARCHIVED, cursorNotes.getInt(cursorNotes.getColumnIndex(COLUMN_ARCHIVED)));
					note.put(COLUMN_PROTECT, cursorNotes.getInt(cursorNotes.getColumnIndex(COLUMN_PROTECT)));
					note.put(COLUMN_STARED, cursorNotes.getInt(cursorNotes.getColumnIndex(COLUMN_STARED)));
					note.put(COLUMN_THEME, cursorNotes.getInt(cursorNotes.getColumnIndex(COLUMN_THEME)));
					note.put(COLUMN_COUNTER, cursorNotes.getInt(cursorNotes.getColumnIndex(COLUMN_COUNTER)));
					note.put(COLUMN_PARENT_ID, cursorNotes.getLong(cursorNotes.getColumnIndex(COLUMN_PARENT_ID)));
					note.put(COLUMN_EXTRA, cursorNotes.getString(cursorNotes.getColumnIndex(COLUMN_EXTRA)));
					notesArray.put(note);
				} while (cursorNotes.moveToNext());
			}

			if (cursorMetadata == null || !cursorMetadata.moveToFirst()) {
				Log.e("DbHelper", "cursorMetadata ==null");
				if (cursorMetadata != null) {
					cursorMetadata.close();
				}
				initializeSyncMetadata(db);
				cursorMetadata = db.query(TABLE_SYNC, null, null, null, null, null, null);
			}

			if (cursorMetadata != null && cursorMetadata.moveToFirst()) {
				Log.e("DbHelper", "cursorMetadata != null");
				do {
					JSONObject metadata = new JSONObject();
					metadata.put(COLUMN_KEY, cursorMetadata.getString(cursorMetadata.getColumnIndex(COLUMN_KEY)));
					metadata.put(COLUMN_VALUE, cursorMetadata.getString(cursorMetadata.getColumnIndex(COLUMN_VALUE)));
					metadataArray.put(metadata);
				} while (cursorMetadata.moveToNext());
			}

			jsonBackup.put(TABLE_NOTES, notesArray);
			jsonBackup.put(TABLE_SYNC, metadataArray);

			fos.write(jsonBackup.toString().getBytes(StandardCharsets.UTF_8));
		} finally {
			if (cursorNotes != null) {
				cursorNotes.close();
			}
			if (cursorMetadata != null) {
				cursorMetadata.close();
			}
			db.close();
		}
	}*/


