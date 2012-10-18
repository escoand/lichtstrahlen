// TODO implement widget
// TODO implement full text search

package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.escoand.android.lichtstrahlen_2013.R;

public class MainActivity extends Activity {
	public static final int DIALOG_ABOUT_ID = 0;
	public static final int DIALOG_DATE_ID = 1;
	public static final int DIALOG_NOTE_ID = 2;
	public static final int DIALOG_REMIND_ID = 3;
	public static final int DIALOG_LIST_ID = 4;
	public static final int DIALOG_BIBLE_ID = 5;
	public static final String BIBLE_URL = "http://www.bibleserver.com/text/";
	private static final int TIMER_SPLASH = 2000;

	public Date date = new Date();
	public Notes notes = null;
	ViewFlipper flipper = null;

	public ProgressDialog progress = null;
	private Cursor data = null;
	private Cursor data2 = null;

	private GestureDetector gesture = null;

	private Intent reminder = null;
	VerseDatabase dbh;

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

		/* reminder */
		reminder = new Intent(this, Reminder.class);
		reminder.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		reminder.putExtra("icon", R.drawable.icon);
		reminder.putExtra("info", getString(R.string.app_name) + " - "
				+ getString(R.string.msgRemind));
		reminder.putExtra("title", getString(R.string.app_name));
		reminder.putExtra("message", getString(R.string.msgRemind));

		/* init */
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.msgWait));
		gesture = new GestureDetector(new Gestures(this));
		notes = new Notes(this);

		/* flipper */
		flipper = (ViewFlipper) findViewById(R.id.flipper);

		/* database */
		dbh = new VerseDatabase(getApplicationContext());

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
		// super.onCreateOptionsMenu(menu);
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
			item.setEnabled(data != null && data.getCount() != 0);
		item = menu.findItem(R.id.menuShare);
		if (item != null)
			item.setEnabled(data != null && data.getCount() != 0);

		return super.onPrepareOptionsMenu(menu);
	}

	/* callback for clicking option item */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;

		switch (item.getItemId()) {

		/* scripture */
		case R.id.menuBible:
			if (data.getCount() == 1) {
				data.moveToFirst();
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(BIBLE_URL
						+ data.getString(
								data.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE))
								.replaceAll(" ", "")));
				startActivity(intent);
				return true;

			} else if (data.getCount() > 1) {
				showDialog(DIALOG_BIBLE_ID);
				return true;
			}
			return false;

			/* today */
		case R.id.menuToday:
			showDay(new Date());
			return true;

			/* calendar */
		case R.id.menuDate:
			showDialog(DIALOG_DATE_ID);
			return true;

			/* notes */
		case R.id.menuNotes:
			new AsyncNotes(this).execute();
			return true;

			/* scripture list */
		case R.id.menuList:
			showDialog(DIALOG_LIST_ID);
			return true;

			/* share */
		case R.id.menuShare:
			String text = data.getString(data
					.getColumnIndex(VerseDatabase.TABLE_COLUMN_TITLE))
					+ " ("
					+ data.getString(data
							.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE))
					+ ")\n\n"
					+ data.getString(data
							.getColumnIndex(VerseDatabase.TABLE_COLUMN_TEXT))
					+ ")\n\n"
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
			startActivity(intent);
			return true;

			/* info */
		case R.id.menuInfo:
			showDialog(DIALOG_ABOUT_ID);
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
		// FIXME dialog shows old data, but uses new one at selecting
		case DIALOG_BIBLE_ID:
			return new AlertDialog.Builder(this).setCancelable(true)
					.setTitle(getString(R.string.listVerses))
					.setCursor(data, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							data.moveToPosition(item);
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(BIBLE_URL
									+ data.getString(
											data.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE))
											.replaceAll(" ", "")));
							startActivity(intent);
						}
					}, VerseDatabase.TABLE_COLUMN_VERSE).create();

			/* calendar */
		case DIALOG_DATE_ID:
			return new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							showDay(year * 10000 + (monthOfYear + 1) * 100
									+ dayOfMonth);
						}
					}, date.getYear() + 1900, date.getMonth(), date.getDate());

			/* notes */
		case DIALOG_NOTE_ID:
			// TODO reimplement notes
			dialog.setTitle(getString(R.string.noteNote) + " "
					+ getString(R.string.textFor) + " "
					+ DateFormat.getDateInstance().format(date));
			dialog.setContentView(R.layout.noteedit);

			/* load note */
			if (notes.exist(date))
				((TextView) dialog.findViewById(R.id.noteText)).setText(notes
						.get(date));

			/* callback for save note */
			dialog.findViewById(R.id.noteSave).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {

							/* save note */
							View parent = (View) v.getParent().getParent();
							notes.add(date, ((TextView) parent
									.findViewById(R.id.noteText)).getText()
									.toString());

							/* back to main */
							dismissDialog(DIALOG_NOTE_ID);
							showDay();
							Toast.makeText(getApplicationContext(),
									getString(R.string.noteSaved),
									Toast.LENGTH_LONG).show();
						}
					});

			/* callback for delete note */
			dialog.findViewById(R.id.noteDelete).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							AlertDialog.Builder adb = new AlertDialog.Builder(
									getApplicationContext());
							adb.setCancelable(false);
							adb.setMessage(getString(R.string.noteDeleteQuestion));
							adb.setPositiveButton(
									getString(R.string.buttonYes),
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											/* delete note */
											// notes.remove(date);

											/* back to main */
											dialog.dismiss();
											getParent().dismissDialog(
													DIALOG_NOTE_ID);
											showDay();
											Toast.makeText(
													getApplicationContext(),
													getString(R.string.noteDeleted),
													Toast.LENGTH_LONG).show();
										}
									});
							adb.setNegativeButton(getString(R.string.buttonNo),
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									}).create().show();
						}
					});

			/* callback for cancel */
			dialog.findViewById(R.id.noteCancel).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							/* back to main */
							dismissDialog(DIALOG_NOTE_ID);
						}
					});

			return dialog;

		case DIALOG_LIST_ID:
			if (data2 == null)
				data2 = dbh.getListCursor();
			return new AlertDialog.Builder(this).setCancelable(true)
					.setTitle(getString(R.string.menuList))

					/* data for list */
					.setAdapter(new CursorAdapter(this, data2) {
						private final SimpleDateFormat df = new SimpleDateFormat(
								"yyyyMMdd");
						SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT);
						View tvUntil;
						TextView tvVerse, tvVerseUntil, tvDate, tvDateUntil;
						Date date, date_until;
						String verse, verse_until;

						/* inflate layout */
						@Override
						public View newView(Context context, Cursor cursor,
								ViewGroup parent) {
							return getLayoutInflater().inflate(R.layout.list,
									parent, false);
						}

						/* set item data */
						@Override
						public void bindView(View view, Context context,
								Cursor cursor) {
							tvUntil = view.findViewById(R.id.listUntil);
							tvVerse = (TextView) view
									.findViewById(R.id.listVerse);
							tvVerseUntil = (TextView) view
									.findViewById(R.id.listVerseUntil);
							tvDate = (TextView) view
									.findViewById(R.id.listDate);
							tvDateUntil = (TextView) view
									.findViewById(R.id.listDateUntil);
							verse = cursor
									.getString(cursor
											.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE));
							verse_until = cursor
									.getString(cursor
											.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE
													+ "_until"));
							try {
								date = df
										.parse(cursor.getString(cursor
												.getColumnIndex(VerseDatabase.TABLE_COLUMN_DATE)));
								date_until = df
										.parse(cursor.getString(cursor
												.getColumnIndex(VerseDatabase.TABLE_COLUMN_DATE
														+ "_until")));
							} catch (Exception e) {
								// e.printStackTrace();
							}

							/* show */
							tvVerse.setText(verse);
							tvDate.setText(df_ymd.format(date));
							if (!verse.equals(verse_until)) {
								tvUntil.setVisibility(View.VISIBLE);
								tvVerseUntil
										.setText(getString(R.string.textUntil)
												+ " " + verse_until);
								tvDateUntil.setText(df_ymd.format(date_until));
							} else
								tvUntil.setVisibility(View.GONE);
						}
					},

					/* on click */
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							data.moveToPosition(item);
							showDay(data2.getString(data2
									.getColumnIndex(VerseDatabase.TABLE_COLUMN_DATE)));
						}
					}).create();

			/* info */
			// FIXME update about dialog
		case DIALOG_ABOUT_ID:
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
		ListView list = new ListView(getApplicationContext());
		list.setDivider(null);
		data = dbh.getDateCursor(date);
		list.setAdapter(new CursorAdapter(this, data) {
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
				return getLayoutInflater().inflate(R.layout.verses, parent,
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
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_TITLE)));
				verse.setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE)));
				text.setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_TEXT)));
				if (cursor.isNull(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_AUTHOR))
						|| cursor
								.getString(
										cursor.getColumnIndex(VerseDatabase.TABLE_COLUMN_AUTHOR))
								.equals(""))
					author.setVisibility(View.GONE);
				author.setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_AUTHOR)));

				/* font size */
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
				verse.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
				text.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
				author.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			}
		});

		/* add to flipper */
		if (list.getCount() > 0)
			flipper.addView(list);
		else
			// TODO empty textview as default empty textview
			flipper.addView(getLayoutInflater().inflate(R.layout.empty, null));
		flipper.showNext();
		if (flipper.getChildCount() > 1)
			flipper.removeViewAt(0);

		/* title */
		setTitle(getString(R.string.app_name) + " "
				+ getString(R.string.textFor) + " "
				+ DateFormat.getDateInstance().format(date));
	}
}