/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.escoand.android.lichtstrahlen;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.escoand.android.library.AbstractDatabase;

public class NoteDatabase extends AbstractDatabase {
    public static final String DATABASE_NAME = "notes";
    public static final int DATABASE_VERSION = 1;

    protected static final String COLUMN_DATE = "date";
    protected static final String COLUMN_TEXT = "text";

    public NoteDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        TABLE_NAME = DATABASE_NAME;
        COLUMNS = new String[]{COLUMN_DATE, COLUMN_TEXT};
    }

    /* set note for specific date */
    public boolean setDateNote(Date date, String note) {
        long res = -1;
        String date_string = DateFormat.format("yyyyMMdd", date).toString();

		/* delete note */
        getWritableDatabase().delete(TABLE_NAME, COLUMN_DATE + "=?",
                new String[]{date_string});

		/* save note */
        if (!note.equals("")) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE, date_string);
            values.put(COLUMN_TEXT, note);
            res = insertItem(values);
        }

        return res != -1;
    }

    /* get note for specific date */
    @SuppressLint("SimpleDateFormat")
    public String getDateNote(Date date) {
        Cursor cursor = getItems(new String[]{COLUMN_TEXT}, COLUMN_DATE
                        + "=?",
                new String[]{new SimpleDateFormat("yyyyMMdd").format(date)},
                null);
        if (cursor.getCount() >= 1)
            return cursor.getString(cursor.getColumnIndex(COLUMN_TEXT));
        else
            return null;
    }

    /* get list */
    public Cursor getNoteList() {
        return getItems(new String[]{COLUMN_DATE, COLUMN_TEXT}, COLUMN_DATE);
    }
}