package de.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class ScriptureDialog extends DialogFragment {
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
		TextDatabase db = new TextDatabase(getActivity());
		cursor = db.getList();

		dialog.setCancelable(true).setTitle(getString(R.string.menuList))

		/* data for list */
		.setAdapter(new CursorAdapter(getActivity(), cursor) {
			SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT);
			TextView tvVerse, tvVerseUntil, tvDate, tvDateUntil;
			String verse, verse_until, date, date_until;
			int count;

			/* inflate layout */
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				return inflater.inflate(R.layout.scriptureentry, parent, false);
			}

			/* set item data */
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				tvVerse = (TextView) view.findViewById(R.id.listVerse);
				tvVerseUntil = (TextView) view
						.findViewById(R.id.listVerseUntil);
				tvDate = (TextView) view.findViewById(R.id.listDate);
				tvDateUntil = (TextView) view.findViewById(R.id.listDateUntil);
				verse = cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_VERSE));
				verse_until = cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_VERSE_UNTIL));
				count = cursor.getInt(cursor.getColumnIndex("count"));
				try {
					date = df_ymd.format(df.parse(cursor.getString(cursor
							.getColumnIndex(TextDatabase.COLUMN_DATE))));
					date_until = df_ymd.format(df.parse(cursor.getString(cursor
							.getColumnIndex(TextDatabase.COLUMN_DATE_UNTIL))));
				} catch (Exception e) {
					// e.printStackTrace();
				}

				/* single date */
				if (count <= 1) {
					tvVerse.setText(verse);
					tvDate.setText(date);
					tvVerseUntil.setVisibility(View.GONE);
					tvDateUntil.setVisibility(View.GONE);
				}

				/* date range */
				else {
					tvVerse.setText(verse.replaceAll("(-[0-9]+| - [0-9, ]+)$",
							""));
					tvVerseUntil.setText(getString(R.string.textUntil)
							+ " "
							+ verse_until.replaceAll(
									"([0-9]+[a-z]?-|[0-9,]+ - )", ""));
					tvDate.setText(getString(R.string.textFrom) + " " + date);
					tvDateUntil.setText(getString(R.string.textUntil) + " "
							+ date_until);
					tvVerseUntil.setVisibility(View.VISIBLE);
					tvDateUntil.setVisibility(View.VISIBLE);
				}
			}
		},

		new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				cursor.moveToPosition(item);
				try {
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
							listener.onDateSelect(df.parse(cursor.getString(cursor
									.getColumnIndex(TextDatabase.COLUMN_DATE))));

		return dialog.create();
	}
}
