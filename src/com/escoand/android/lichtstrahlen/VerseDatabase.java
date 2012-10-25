package com.escoand.android.lichtstrahlen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.escoand.android.lichtstrahlen_2013.R;

/* helper to access database */
public class VerseDatabase extends SQLiteOpenHelper {
	private final Context context;
	private SQLiteDatabase database;

	private static final String DATABASE_NAME = "verses";
	private static final int DATABASE_VERSION = 13;

	private static final String TABLE_NAME = "verses";
	public static final String TABLE_COLUMN_DATE = "date";
	public static final String TABLE_COLUMN_AUTHOR = "author";
	public static final String TABLE_COLUMN_TITLE = "title";
	public static final String TABLE_COLUMN_VERSE = "verse";
	public static final String TABLE_COLUMN_TEXT = "text";
	public static final String TABLE_COLUMN_ORDERID = "orderid";
	public static final String TABLE_SQL_DROP = "DROP TABLE IF EXISTS "
			+ TABLE_NAME;
	private static final String TABLE_SQL_CREATE = "CREATE VIRTUAL TABLE "
			+ TABLE_NAME + " USING fts3(" + TABLE_COLUMN_DATE + ", "
			+ TABLE_COLUMN_AUTHOR + ", " + TABLE_COLUMN_VERSE + ", "
			+ TABLE_COLUMN_TITLE + ", " + TABLE_COLUMN_TEXT + ", "
			+ TABLE_COLUMN_ORDERID + ")";

	public VerseDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	/* create new database */
	@Override
	public void onCreate(SQLiteDatabase db) {
		database = db;
		database.execSQL(TABLE_SQL_CREATE);
		loadData();
	}

	/* re-create new database */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL(TABLE_SQL_DROP);
			onCreate(db);
		}
	}

	private void loadData() {
		InputStream stream = context.getResources().openRawResource(R.raw.data);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		ContentValues values = new ContentValues();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] cols = line.split("\\|");
				if (cols.length < 6)
					continue;
				values.clear();
				values.put(TABLE_COLUMN_ORDERID, Integer.parseInt(cols[0]));
				values.put(TABLE_COLUMN_DATE, cols[1].trim());
				values.put(TABLE_COLUMN_AUTHOR, cols[2].trim());
				values.put(TABLE_COLUMN_VERSE, cols[3].trim());
				values.put(TABLE_COLUMN_TITLE, cols[4].trim());
				values.put(TABLE_COLUMN_TEXT, cols[5].trim());
				database.insert(TABLE_NAME, null, values);
				if (cols.length >= 8 && !cols[6].equals("")
						&& !cols[7].equals("")) {
					values.remove(TABLE_COLUMN_AUTHOR);
					values.put(TABLE_COLUMN_ORDERID, -1);
					values.put(TABLE_COLUMN_TITLE,
							context.getString(R.string.mainWeek));
					values.put(TABLE_COLUMN_TEXT, cols[6].trim());
					values.put(TABLE_COLUMN_VERSE, cols[7].trim());
					System.err.println(values);
					database.insert(TABLE_NAME, null, values);
				}
				if (cols.length >= 10 && !cols[8].equals("")
						&& !cols[9].equals("")) {
					values.remove(TABLE_COLUMN_AUTHOR);
					values.put(TABLE_COLUMN_ORDERID, -2);
					values.put(TABLE_COLUMN_TITLE,
							context.getString(R.string.mainMonth));
					values.put(TABLE_COLUMN_TEXT, cols[8].trim());
					values.put(TABLE_COLUMN_VERSE, cols[9].trim());
					System.err.println(values);
					database.insert(TABLE_NAME, null, values);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Cursor getDateCursor(Date date) {
		String datestring = new SimpleDateFormat("yyyyMMdd").format(date);
		Cursor cursor = getReadableDatabase()
				.query(TABLE_NAME,
						new String[] { TABLE_COLUMN_TITLE, TABLE_COLUMN_VERSE,
								TABLE_COLUMN_TEXT, TABLE_COLUMN_AUTHOR,
								"rowid as _id" }, TABLE_COLUMN_DATE + "=?",
						new String[] { datestring }, null, null,
						TABLE_COLUMN_ORDERID);
		if (cursor != null)
			cursor.moveToFirst();
		//System.err.println(datestring + "=" + cursor.getCount());
		return cursor;
	}

	public HashMap<String, String> getDateHashMap(Date date) {
		Cursor cursor = getDateCursor(date);
		HashMap<String, String> result = new HashMap<String, String>();
		if (cursor != null && cursor.getCount() > 0)
			for (int i = 0; i < cursor.getColumnCount(); i++)
				result.put(cursor.getColumnName(i), cursor.getString(i));
		return result;
	}

	public Cursor getListCursor() {
		Cursor cursor = getReadableDatabase()
				.query(TABLE_NAME,
						new String[] {
								TABLE_COLUMN_VERSE,
								"min(" + TABLE_COLUMN_DATE + ") as "
										+ TABLE_COLUMN_DATE,
								"max(" + TABLE_COLUMN_DATE + ") as "
										+ TABLE_COLUMN_DATE + "_until",
								"rowid as _id" }, TABLE_COLUMN_ORDERID + ">0",
						new String[] {}, TABLE_COLUMN_ORDERID, null,
						TABLE_COLUMN_ORDERID);
		if (cursor != null)
			cursor.moveToFirst();
		System.err.println(cursor.getCount());
		return cursor;
	}

	public ArrayList<HashMap<String, String>> getListHashMap() {
		Cursor cursor = getListCursor();
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		if (cursor != null)
			while (cursor.moveToNext()) {
				HashMap<String, String> item = new HashMap<String, String>();
				for (int i = 0; i < cursor.getColumnCount(); i++)
					item.put(cursor.getColumnName(i), cursor.getString(i));
				result.add(item);
			}
		return result;
	}
}