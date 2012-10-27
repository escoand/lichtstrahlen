// TODO implement widget
// TODO implement full text search

package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.escoand.android.lichtstrahlen_2013.R;

public class MainActivity extends Activity {
	private static final int TIMER_SPLASH = 2000;
	private GestureDetector gesture = null;
	private ViewFlipper flipper = null;
	private VerseDatabase db_day = null;
	private NoteDatabase db_note = null;
	private Cursor data_day = null;
	private Cursor data_list = null;
	public Date date = new Date();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		/* theme */
		if (!PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getBoolean("inverse", false))
			setTheme(android.R.style.Theme_Light);
		else
			setTheme(android.R.style.Theme_Black);

		/* show */
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* init */
		gesture = new GestureDetector(new Gestures(this));
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		db_day = new VerseDatabase(getBaseContext());
		db_note = new NoteDatabase(getBaseContext());

		/* splash */
		if (PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getBoolean("splash", true)) {
			date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
			flipper.addView(getLayoutInflater().inflate(R.layout.splash, null));

			/* timer */
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					nextDay();
				}
			}, TIMER_SPLASH);
		}

		/* no splash */
		else
			showDay(new Date());
	}

	/* callback for gestures */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gesture.onTouchEvent(event))
			return true;
		return super.onTouchEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		super.dispatchTouchEvent(event);
		return onTouchEvent(event);
	}

	/* callback for creating option menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* callback for showing option menu */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = null;

		/* no data */
		item = menu.findItem(R.id.menuBible);
		if (item != null)
			item.setEnabled(data_day != null && data_day.getCount() != 0);
		item = menu.findItem(R.id.menuShare);
		if (item != null)
			item.setEnabled(data_day != null && data_day.getCount() != 0);

		return super.onPrepareOptionsMenu(menu);
	}

	/* callback for clicking option item */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;

		switch (item.getItemId()) {

		/* scripture */
		case R.id.menuBible:
			if (data_day.getCount() == 1) {
				data_day.moveToFirst();
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getString(R.string.sciptureUrl)
						+ data_day
								.getString(
										data_day.getColumnIndex(VerseDatabase.COLUMN_VERSE))
								.replaceAll(" ", "")));
				startActivity(intent);
				return true;

			} else if (data_day.getCount() > 1) {
				showDialog(R.id.menuBible);
				return true;
			}
			return false;

			/* today */
		case R.id.menuToday:
			showDay(new Date());
			return true;

			/* calendar */
		case R.id.menuDate:
			showDialog(R.id.menuDate);
			return true;

			/* scripture list */
		case R.id.menuList:
			showDialog(R.id.menuList);
			return true;

			/* share */
		case R.id.menuShare:
			String text = data_day.getString(data_day
					.getColumnIndex(VerseDatabase.COLUMN_TITLE))
					+ " ("
					+ data_day.getString(data_day
							.getColumnIndex(VerseDatabase.COLUMN_VERSE))
					+ ")\n\n"
					+ data_day.getString(data_day
							.getColumnIndex(VerseDatabase.COLUMN_TEXT))
					+ ")\n\n"
					+ data_day.getString(data_day
							.getColumnIndex(VerseDatabase.COLUMN_AUTHOR))
					+ "\n\n"
					+ getString(R.string.shareText)
					+ " "
					+ getString(R.string.shareUrl);

			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getTitle());
			intent.putExtra(Intent.EXTRA_TEXT, text);
			startActivity(Intent.createChooser(intent,
					getText(R.string.menuShare)));
			return true;

			/* preferences */
		case R.id.menuPreference:
			intent = new Intent(getBaseContext(), Preferences.class);
			startActivityForResult(intent, 0);
			return true;

			/* info */
		case R.id.menuInfo:
			showDialog(R.id.menuInfo);
			return true;

			/* ... */
		default:
			return false;
		}
	}

	/* callback for creating dialog */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = new Dialog(this);

		switch (id) {

		/* bible */
		case R.id.menuBible:
			return new AlertDialog.Builder(this).setCancelable(true)
					.setTitle(getString(R.string.listVerses))

					/* on click */
					.setCursor(data_day, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							data_day.moveToPosition(item);
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(getString(R.string.sciptureUrl)
									+ data_day
											.getString(
													data_day.getColumnIndex(VerseDatabase.COLUMN_VERSE))
											.replaceAll(" ", "")));
							startActivity(intent);
						}
					}, VerseDatabase.COLUMN_VERSE)

					/* destroy after select */
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							removeDialog(R.id.menuBible);
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							removeDialog(R.id.menuBible);
						}
					})

					/* destroy after cancel */
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							removeDialog(R.id.menuBible);
						}
					})

					.create();

			/* calendar */
		case R.id.menuDate:
			return new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							showDay(year * 10000 + (monthOfYear + 1) * 100
									+ dayOfMonth);
						}
					}, date.getYear() + 1900, date.getMonth(), date.getDate());

		case R.id.menuList:
			// TODO show until scripture
			if (data_list == null)
				data_list = db_day.getListCursor();
			return new AlertDialog.Builder(this).setCancelable(true)
					.setTitle(getString(R.string.menuList))

					/* data for list */
					.setAdapter(new CursorAdapter(this, data_list) {
						private final SimpleDateFormat df = new SimpleDateFormat(
								"yyyyMMdd");
						SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT);
						TextView tvVerse, tvDate;
						Date date, date_until;
						String verse;

						/* inflate layout */
						@Override
						public View newView(Context context, Cursor cursor,
								ViewGroup parent) {
							return getLayoutInflater().inflate(
									R.layout.scriptureentry, parent, false);
						}

						/* set item data */
						@Override
						public void bindView(View view, Context context,
								Cursor cursor) {
							tvVerse = (TextView) view
									.findViewById(R.id.listVerse);
							tvDate = (TextView) view
									.findViewById(R.id.listDate);
							verse = cursor
									.getString(cursor
											.getColumnIndex(VerseDatabase.COLUMN_VERSE));
							try {
								date = df
										.parse(cursor.getString(cursor
												.getColumnIndex(VerseDatabase.COLUMN_DATE)));
								date_until = df
										.parse(cursor.getString(cursor
												.getColumnIndex(VerseDatabase.COLUMN_DATE
														+ "_until")));
							} catch (Exception e) {
								// e.printStackTrace();
							}

							/* show */
							tvVerse.setText(verse);
							if (date.equals(date_until))
								tvDate.setText(df_ymd.format(date));
							else
								tvDate.setText(getString(R.string.textFrom)
										+ " " + df_ymd.format(date));
						}
					},

					/* on click */
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							data_day.moveToPosition(item);
							showDay(data_list.getString(data_list
									.getColumnIndex(VerseDatabase.COLUMN_DATE)));
						}
					}).create();

			/* info */
		case R.id.menuInfo:
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.about);

			try {
				((TextView) dialog.findViewById(R.id.txtVersion))
						.setText("Version "
								+ getPackageManager().getPackageInfo(
										getPackageName(), 0).versionName);
				((TextView) dialog.findViewById(R.id.txtAbout)).setText(
						Html.fromHtml(getString(R.string.about)),
						TextView.BufferType.SPANNABLE);
				((TextView) dialog.findViewById(R.id.txtAbout))
						.setMovementMethod(LinkMovementMethod.getInstance());
			} catch (Exception e) {
				// e.printStackTrace();
			}

			return dialog;
		}

		return null;
	}

	/* go to next day */
	public void nextDay() {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_right);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_left);

		/* next day */
		date.setTime(date.getTime() + 24 * 60 * 60 * 1000);
		showDay();
	}

	/* go to previous day */
	public void prevDay() {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_left);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_right);

		/* previous day */
		date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
		showDay();
	}

	/* go to today */
	public void showDay(Date date) {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get day */
		this.date = date;
		showDay();
	}

	/* go to day by long */
	public void showDay(long date) {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get day */
		try {
			this.date = new SimpleDateFormat("yyyyMMdd").parse(String
					.valueOf(date));
			showDay();
		} catch (ParseException e) {
			// e.printStackTrace();
		}
	}

	/* go to day by string */
	public void showDay(String date) {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get day */
		try {
			this.date = new SimpleDateFormat("yyyyMMdd").parse(date);
			showDay();
		} catch (ParseException e) {
			// e.printStackTrace();
		}
	}

	/* refresh day text */
	public void showDay() {
		View container = getLayoutInflater().inflate(R.layout.daylist, flipper,
				false);
		ListView list = (ListView) container.findViewById(R.id.dayList);
		TextView empty = (TextView) container.findViewById(R.id.dayEmpty);
		Float size = Float.valueOf(PreferenceManager
				.getDefaultSharedPreferences(getBaseContext()).getString(
						"scale", "18"));

		list.setDivider(null);
		list.setEmptyView(empty);

		/* note */
		// TODO save not on focus lost
		list.addFooterView(getLayoutInflater().inflate(R.layout.noteedit, null,
				false));
		((TextView) list.findViewById(R.id.noteText)).setText(db_note
				.getDateNote(date));
		list.findViewById(R.id.noteSave).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						System.out.println("note saved");
						db_note.setDateNode(date,
								((TextView) findViewById(R.id.noteText))
										.getText().toString());
					}
				});

		data_day = db_day.getDateCursor(date);
		list.setAdapter(new CursorAdapter(this, data_day) {
			Float size = Float.valueOf(PreferenceManager
					.getDefaultSharedPreferences(getBaseContext()).getString(
							"scale", "18"));

			/* disable selecting */
			@Override
			public boolean isEnabled(int position) {
				return false;
			}

			/* load layout */
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return getLayoutInflater().inflate(R.layout.dayentry, parent,
						false);
			}

			/* set item data */
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				TextView title = (TextView) view.findViewById(R.id.verseTitle);
				TextView verse = (TextView) view.findViewById(R.id.verseVerse);
				TextView text = (TextView) view.findViewById(R.id.verseText);
				TextView author = (TextView) view
						.findViewById(R.id.verseAuthor);

				/* content */
				title.setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.COLUMN_TITLE)));
				verse.setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.COLUMN_VERSE)));
				text.setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.COLUMN_TEXT)));
				if (cursor.isNull(cursor
						.getColumnIndex(VerseDatabase.COLUMN_AUTHOR))
						|| cursor
								.getString(
										cursor.getColumnIndex(VerseDatabase.COLUMN_AUTHOR))
								.equals(""))
					author.setVisibility(View.GONE);
				author.setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.COLUMN_AUTHOR)));

				/* font size */
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
				verse.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
				text.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
				author.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			}
		});

		/* font size */
		empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

		/* add to flipper */
		flipper.addView(container);
		flipper.showNext();
		if (flipper.getChildCount() > 1)
			flipper.removeViewAt(0);

		/* title */
		setTitle(getString(R.string.app_name) + " "
				+ getString(R.string.textFor) + " "
				+ DateFormat.getDateInstance().format(date));
	}
}