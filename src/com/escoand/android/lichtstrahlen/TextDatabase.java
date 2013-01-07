/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.escoand.android.lichtstrahlen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.escoand.android.lichtstrahlen_2013.R;

@SuppressLint("SimpleDateFormat")
public class TextDatabase extends SQLiteOpenHelper {
	private final Context context;
	private SQLiteDatabase database;

	private static final String DATABASE_NAME = "verses";
	private static final int DATABASE_VERSION = 16;

	private static final String TABLE_NAME = "verses";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_AUTHOR = "author";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_VERSE = "verse";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_ORDERID = "orderid";

	private static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	private static final String SQL_CREATE = "CREATE VIRTUAL TABLE "
			+ TABLE_NAME + " USING fts3(" + COLUMN_DATE + ", " + COLUMN_AUTHOR
			+ ", " + COLUMN_VERSE + ", " + COLUMN_TITLE + ", " + COLUMN_TEXT
			+ ", " + COLUMN_ORDERID + ")";

	public TextDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	/* create new database */
	@Override
	public void onCreate(SQLiteDatabase db) {
		database = db;
		database.execSQL(SQL_CREATE);
		loadData();
	}

	/* re-create new database */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL(SQL_DROP);
			onCreate(db);
		}
	}

	/* load data from csv */
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
				values.put(COLUMN_ORDERID, Integer.parseInt(cols[0]));
				values.put(COLUMN_DATE, cols[1].trim());
				values.put(COLUMN_AUTHOR, cols[2].trim());
				values.put(COLUMN_VERSE, cols[3].trim());
				values.put(COLUMN_TITLE, cols[4].trim());
				values.put(COLUMN_TEXT, cols[5].trim());
				database.insert(TABLE_NAME, null, values);
				if (cols.length >= 8 && !cols[6].equals("")
						&& !cols[7].equals("")) {
					values.remove(COLUMN_AUTHOR);
					values.put(COLUMN_ORDERID, -1);
					values.put(COLUMN_TITLE,
							context.getString(R.string.mainWeek));
					values.put(COLUMN_TEXT, cols[6].trim());
					values.put(COLUMN_VERSE, cols[7].trim());
					database.insert(TABLE_NAME, null, values);
				}
				if (cols.length >= 10 && !cols[8].equals("")
						&& !cols[9].equals("")) {
					values.remove(COLUMN_AUTHOR);
					values.put(COLUMN_ORDERID, -2);
					values.put(COLUMN_TITLE,
							context.getString(R.string.mainMonth));
					values.put(COLUMN_TEXT, cols[8].trim());
					values.put(COLUMN_VERSE, cols[9].trim());
					database.insert(TABLE_NAME, null, values);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* get data for specific date */
	public Cursor getDate(Date date) {
		String datestring = new SimpleDateFormat("yyyyMMdd").format(date);
		Cursor cursor = getReadableDatabase().query(
				TABLE_NAME,
				new String[] { COLUMN_TITLE, COLUMN_VERSE, COLUMN_TEXT,
						COLUMN_AUTHOR, "rowid as _id" }, COLUMN_DATE + "=?",
				new String[] { datestring }, null, null, COLUMN_ORDERID);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	/* get data for specific date */
	public Cursor getDateWithDate(Date date) {
		String datestring = new SimpleDateFormat("yyyyMMdd").format(date);
		Cursor cursor = getReadableDatabase().query(
				TABLE_NAME,
				new String[] { COLUMN_TITLE, COLUMN_VERSE, COLUMN_TEXT,
						COLUMN_AUTHOR, COLUMN_DATE, "rowid as _id" },
				COLUMN_DATE + "=?", new String[] { datestring }, null, null,
				COLUMN_ORDERID);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	/* get list */
	public Cursor getList() {
		Cursor cursor = getReadableDatabase().query(TABLE_NAME,
				new String[] { COLUMN_VERSE, COLUMN_DATE, "rowid as _id" },
				COLUMN_ORDERID + ">0", new String[] {}, COLUMN_ORDERID, null,
				COLUMN_ORDERID);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	/* get search result */
	public Cursor getSearch(String searchfor) {
		Cursor cursor = getReadableDatabase().query(
				TABLE_NAME,
				new String[] { COLUMN_TITLE, COLUMN_VERSE, COLUMN_TEXT,
						COLUMN_AUTHOR, COLUMN_DATE, "rowid as _id" },
				TABLE_NAME + " MATCH ?", new String[] { searchfor }, null,
				null, COLUMN_DATE);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}
}