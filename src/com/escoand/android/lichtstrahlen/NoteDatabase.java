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

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;

public class NoteDatabase extends SQLiteOpenHelper {
	private SQLiteDatabase database;

	private static final String DATABASE_NAME = "notes";
	private static final int DATABASE_VERSION = 2;

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
	@SuppressLint("SimpleDateFormat")
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
	public void setDateNote(Date date, String note) {
		String date_string = DateFormat.format("yyyyMMdd", date).toString();
		ContentValues values = new ContentValues();
		values.put(COLUMN_DATE, date_string);
		values.put(COLUMN_TEXT, note);

		getWritableDatabase().delete(TABLE_NAME, COLUMN_DATE + "=?",
				new String[] { date_string });
		getWritableDatabase().insert(TABLE_NAME, null, values);
	}

	/* get list */
	public Cursor getListCursor() {
		Cursor cursor = getReadableDatabase().query(TABLE_NAME,
				new String[] { COLUMN_DATE, COLUMN_TEXT, "rowid as _id" },
				null, new String[] {}, null, null, COLUMN_DATE);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}
}