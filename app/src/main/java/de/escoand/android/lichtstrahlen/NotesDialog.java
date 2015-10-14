package de.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.content.DialogInterface.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.escoand.android.lichtstrahlen_2015.R;

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
