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

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.escoand.android.lichtstrahlen.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DaysAdapter extends CursorAdapter {
    static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd",
            Locale.getDefault());
    static final SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat
            .getDateInstance(DateFormat.LONG);
    NoteDatabase db;

    public DaysAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        db = new NoteDatabase(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return ((Activity) context).getLayoutInflater().inflate(
                R.layout.dayentry, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Date date = null;
        Date date_before = null;
        Date date_after = null;
        TextView tvDate = (TextView) view.findViewById(R.id.verseDate);
        TextView tvTitle = (TextView) view.findViewById(R.id.verseTitle);
        TextView tvVerse = (TextView) view.findViewById(R.id.verseVerse);
        TextView tvText = (TextView) view.findViewById(R.id.verseText);
        TextView tvAuthor = (TextView) view.findViewById(R.id.verseAuthor);
        EditText etNote = (EditText) view.findViewById(R.id.noteText);
        Button btSave = (Button) view.findViewById(R.id.noteSave);

		/* get dates */
        try {
            date = df.parse(cursor.getString(cursor
                    .getColumnIndex(TextDatabase.COLUMN_DATE)));
            if (!cursor.isFirst() && cursor.moveToPrevious()) {
                date_before = df.parse(cursor.getString(cursor
                        .getColumnIndex(TextDatabase.COLUMN_DATE)));
                cursor.moveToNext();
            }
            if (!cursor.isLast() && cursor.moveToNext()) {
                date_after = df.parse(cursor.getString(cursor
                        .getColumnIndex(TextDatabase.COLUMN_DATE)));
                cursor.moveToPrevious();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		/* date */
        if (date_before == null || date_before.compareTo(date) != 0) {
            tvDate.setText(df_ymd.format(date));
            tvDate.setVisibility(View.VISIBLE);
        } else
            tvDate.setVisibility(View.GONE);

		/* content */
        tvTitle.setText(cursor.getString(cursor
                .getColumnIndex(TextDatabase.COLUMN_TITLE)));
        tvVerse.setText(cursor.getString(cursor
                .getColumnIndex(TextDatabase.COLUMN_VERSE)));
        tvText.setText(cursor.getString(cursor
                .getColumnIndex(TextDatabase.COLUMN_TEXT)));

		/* author */
        if (!cursor.isNull(cursor.getColumnIndex(TextDatabase.COLUMN_AUTHOR))
                && !cursor.getString(
                cursor.getColumnIndex(TextDatabase.COLUMN_AUTHOR))
                .equals("")) {
            tvAuthor.setText(cursor.getString(cursor
                    .getColumnIndex(TextDatabase.COLUMN_AUTHOR)));
            tvAuthor.setVisibility(View.VISIBLE);
        } else
            tvAuthor.setVisibility(View.GONE);

		/* note */
        if (date_after == null || date_after.compareTo(date) != 0) {
            etNote.setText(db.getDateNote(date));
            btSave.setTag(date);
            btSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Date date = (Date) v.getTag();
                        String text = ((TextView) ((View) v.getParent())
                                .findViewById(R.id.noteText)).getText()
                                .toString();
                        db.setDateNote(date, text);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            ((View) etNote.getParent()).setVisibility(View.VISIBLE);
        } else
            ((View) etNote.getParent()).setVisibility(View.GONE);

		/* text size */
        int scale = context.getResources().getInteger(
                R.integer.default_text_size);
        try {
            scale = Integer.valueOf(PreferenceManager
                    .getDefaultSharedPreferences(context).getString(
                            "scale",
                            Integer.toString(context.getResources().getInteger(
                                    R.integer.default_text_size))));
        } catch (Exception e) {
        }
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
        tvVerse.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
        tvAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
    }
}
