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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.escoand.android.lichtstrahlen_2015.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotesDialog extends DialogFragment {
    DateSelectListener listener;
    Cursor cursor;
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    @Override
    public void onAttach(Activity activity) {
        listener = (DateSelectListener) activity;
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        NoteDatabase db = new NoteDatabase(getActivity());
        cursor = db.getNoteList();

        dialog.setCancelable(true)

		/* data for list */
                .setAdapter(new CursorAdapter(getActivity(), cursor, 0) {
                                SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT);
                                TextView tvDate, tvNote;
                                Date date;
                                String text;

                                /* inflate layout */
                                @Override
                                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    return inflater.inflate(R.layout.noteentry, parent, false);
                                }

                                /* set item data */
                                @Override
                                public void bindView(View view, Context context, Cursor cursor) {
                                    tvDate = (TextView) view.findViewById(R.id.listDate);
                                    tvNote = (TextView) view.findViewById(R.id.listNote);
                                    try {
                                        date = df.parse(cursor.getString(cursor
                                                .getColumnIndex(NoteDatabase.COLUMN_DATE)));
                                        text = cursor.getString(cursor
                                                .getColumnIndex(NoteDatabase.COLUMN_TEXT));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

				/* show */
                                    tvDate.setText(df_ymd.format(date));
                                    tvNote.setText(text);
                                }
                            },

		/* listener */
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                cursor.moveToPosition(item);
                                try {
                                    listener.onDateSelect(df.parse(cursor.getString(cursor
                                            .getColumnIndex(TextDatabase.COLUMN_DATE))));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

        return dialog.create();
    }
}
