package com.escoand.android.lichtstrahlen;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;

public class NoteDatabase extends SQLiteOpenHelper {
	private SQLiteDatabase database;

	private static final String DATABASE_NAME = "notes";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME = "notes";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_TEXT = "text";

	private static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	private static final String SQL_CREATE = "CREATE VIRTUAL TABLE "
			+ TABLE_NAME + " USING fts3(" + COLUMN_DATE + ", " + COLUMN_TEXT
			+ ")";

	public NoteDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/* create new database */
	@Override
	public void onCreate(SQLiteDatabase db) {
		database = db;
		database.execSQL(SQL_CREATE);
	}

	/* re-create new database */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL(SQL_DROP);
			onCreate(db);
		}
	}

	/* get note for specific date */
	public String getDateNote(Date date) {
		String datestring = new SimpleDateFormat("yyyyMMdd").format(date);
		Cursor cursor = getReadableDatabase().query(TABLE_NAME,
				new String[] { COLUMN_TEXT }, COLUMN_DATE + "=?",
				new String[] { datestring }, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getString(0);
		}
		return null;
	}

	/* set note for specific date */
	public void setDateNode(Date date, String note) {
		String date_string = DateFormat.format("yyyyMMdd", date).toString();
		ContentValues values = new ContentValues();
		values.put(COLUMN_DATE, date_string);
		values.put(COLUMN_TEXT, note);

		getWritableDatabase().delete(TABLE_NAME, COLUMN_DATE + "=?",
				new String[] { date_string });
		getWritableDatabase().insert(TABLE_NAME, null, values);
	}
}