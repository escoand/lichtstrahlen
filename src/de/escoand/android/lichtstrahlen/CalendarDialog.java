package de.escoand.android.lichtstrahlen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import de.escoand.android.library.CalendarAdapter;
import de.escoand.android.library.CalendarEvent;
import de.escoand.android.library.CalendarFragment;
import de.escoand.android.library.OnCalendarEventClickListener;

public class CalendarDialog extends DialogFragment implements
		OnCalendarEventClickListener {
	private static AlertDialog dialog;
	private static CalendarAdapter adapter;
	private DateSelectListener listener;

	@Override
	public void onAttach(Activity activity) {
		listener = (DateSelectListener) activity;
		super.onAttach(activity);
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (dialog != null)
			return dialog;

		/* create dialog */
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.calendar, null));
		dialog = builder.create();

		CalendarFragment calendar = (CalendarFragment) getFragmentManager()
				.findFragmentById(R.id.calendar);
		adapter = (CalendarAdapter) calendar.getListAdapter();
		adapter.setOnCalendarEventClickedListener(this);

		/* create events */
		ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
		SimpleDateFormat frmt = new SimpleDateFormat("yyyyMMdd",
				Locale.getDefault());
		String text;
		String text_short;

		Cursor cursor = new TextDatabase(getActivity()).getCalendarList();
		while (cursor.moveToNext()) {
			GregorianCalendar begin = new GregorianCalendar();
			GregorianCalendar end = new GregorianCalendar();

			try {
				begin.setTime(frmt.parse(cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_DATE))));
				end.setTime(frmt.parse(cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_DATE_UNTIL))));

				text = cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_VERSE));
				text_short = cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_VERSE_SHORT));

				events.add(new CalendarEvent(begin, end, text, text_short));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		cursor.close();

		if (events.size() > 0) {
			adapter.setEvents(events.toArray(new CalendarEvent[events.size()]));
			adapter.zoomToEvents();
		}

		/* settings */
		adapter.WEEK_NUMBERS = false;
		adapter.EVENT_BACKGROUND = R.color.primary;
		adapter.EVENT_FOREGROUND = R.color.secondary;

		return dialog;
	}

	@Override
	public void onCalenderEventClick(CalendarEvent event) {
		listener.onDateSelect(event.getBegin());
	}
}
