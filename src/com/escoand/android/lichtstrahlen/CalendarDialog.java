package com.escoand.android.lichtstrahlen;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.escoand.android.lichtstrahlen_2014.R;

import de.escoand.android.library.CalendarAdapter;
import de.escoand.android.library.CalendarEvent;
import de.escoand.android.library.CalendarFragment;

public class CalendarDialog extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.calendar, null));
		return builder.create();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
		CalendarFragment calendar = (CalendarFragment) getFragmentManager()
				.findFragmentById(R.id.calendar);
		CalendarAdapter adapter = (CalendarAdapter) calendar.getListAdapter();

		Cursor cursor = new TextDatabase(getActivity()).getCalendarList();

		while (cursor.moveToNext()) {
			String begin = cursor.getString(cursor
					.getColumnIndex(TextDatabase.COLUMN_DATE));
			String end = cursor.getString(cursor
					.getColumnIndex(TextDatabase.COLUMN_DATE_UNTIL));
			String text = cursor.getString(cursor
					.getColumnIndex(TextDatabase.COLUMN_VERSE));
			String text_short = cursor.getString(cursor
					.getColumnIndex(TextDatabase.COLUMN_VERSE_SHORT));

			events.add(new CalendarEvent(new GregorianCalendar(Integer
					.valueOf(begin.substring(0, 4)), Integer.valueOf(begin
					.substring(4, 6)) - 1, Integer.valueOf(begin
					.substring(6, 8))),
					new GregorianCalendar(Integer.valueOf(end.substring(0, 4)),
							Integer.valueOf(end.substring(4, 6)) - 1, Integer
									.valueOf(end.substring(6, 8))), text,
					text_short));
		}

		cursor.close();

		calendar.setEvents(events.toArray(new CalendarEvent[events.size()]));
		adapter.zoomToEvents();
		adapter.WEEK_NUMBERS = false;

		super.onActivityCreated(savedInstanceState);
	}
}
