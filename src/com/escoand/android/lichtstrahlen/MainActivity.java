package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.escoand.android.lichtstrahlen_2013.R;

public class MainActivity extends Activity {
	public static final int DIALOG_ABOUT_ID = 0;
	public static final int DIALOG_DATE_ID = 1;
	public static final int DIALOG_NOTE_ID = 2;
	public static final int DIALOG_REMIND_ID = 3;
	public static final String BIBLE_URL = "http://www.bibleserver.com/text/";
	private static final int TIMER_SPLASH = 2000;

	public Date date = new Date();
	public CharSequence[] verses = null;
	public Notes notes = null;
	ViewFlipper flipper = null;

	public ProgressDialog progress = null;
	public AlertDialog selection = null;

	private GestureDetector gesture = null;

	private Intent reminder = null;
	VerseDatabase dbh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		/* theme */
		if (!getSharedPreferences(getString(R.string.app_name),
				Context.MODE_PRIVATE).getBoolean("inverse", false)) {
			setTheme(android.R.style.Theme_Light);
		}

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
		if (getSharedPreferences(getString(R.string.app_name),
				Context.MODE_PRIVATE).getBoolean("splash", true)) {
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
			gotoDay();
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
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* callback for showing option menu */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menuRemind);
		if (item != null) {

			/* load settings */
			int hour = getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).getInt("remind_hour", 9);
			int minute = getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).getInt("remind_minute", 0);

			item.setTitle(getString(R.string.menuRemind)
					+ String.format(" (%02d:%02d)", hour, minute));
			item.setChecked(PendingIntent.getBroadcast(getApplicationContext(),
					0, reminder, PendingIntent.FLAG_NO_CREATE) != null);
		}

		item = menu.findItem(R.id.menuInverse);
		if (item != null) {
			item.setChecked(getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).getBoolean("inverse", false));
		}

		item = menu.findItem(R.id.menuSplash);
		if (item != null) {
			item.setChecked(getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).getBoolean("splash", true));
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/* callback for clicking option item */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;

		switch (item.getItemId()) {

		/* scripture */
		case R.id.menuBible:
			if (verses.length == 1) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(BIBLE_URL
						+ verses[0].toString().replaceAll(" ", "")));
				startActivity(intent);
				return true;

			} else if (verses.length > 1) {
				selection = new AlertDialog.Builder(this)
						.setCancelable(true)
						.setTitle(getString(R.string.listVerses))
						.setItems(verses,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int item) {
										Intent intent = new Intent(
												Intent.ACTION_VIEW);
										intent.setData(Uri.parse(BIBLE_URL
												+ verses[item].toString()
														.replaceAll(" ", "")));
										startActivity(intent);
									}
								}).create();
				selection.show();
				return true;
			}
			return false;

			/* today */
		case R.id.menuToday:
			gotoDay();
			return true;

			/* calendar */
		case R.id.menuDate:

			/* show date picker */
			showDialog(DIALOG_DATE_ID);
			return true;

			/* notes */
		case R.id.menuNotes:
			new AsyncNotes(this).execute();
			return true;

			/* scripture list */
		case R.id.menuList:

			/* show list */
			selection = new AlertDialog.Builder(this).setCancelable(true)
					.setTitle(getString(R.string.menuList))

					/* data for list */
					.setAdapter(new CursorAdapter(this, dbh.getListCursor()) {
						private final SimpleDateFormat df = new SimpleDateFormat(
								"yyyyMMdd");

						/* load layout */
						@Override
						public View newView(Context context, Cursor cursor,
								ViewGroup parent) {
							getLayoutInflater();
							return LayoutInflater.from(context).inflate(
									R.layout.list, parent, false);
						}

						/* set item data */
						@Override
						public void bindView(View view, Context context,
								Cursor cursor) {
							((TextView) view.findViewById(R.id.listVerse))
									.setText(cursor.getString(cursor
											.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE)));
							try {
								((TextView) view.findViewById(R.id.listDate))
										.setText(DateFormat
												.getDateInstance()
												.format(df.parse(cursor.getString(cursor
														.getColumnIndex(VerseDatabase.TABLE_COLUMN_DATE)))));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					},

					/* on click */
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							Cursor cursor = ((Cursor) ((CursorAdapter) selection
									.getListView().getAdapter()).getItem(id));
							gotoDay(cursor.getString(cursor
									.getColumnIndex(VerseDatabase.TABLE_COLUMN_DATE)));
						}
					}).create();
			selection.show();
			return true;

			/* share */
		case R.id.menuShare:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, verses[0].toString());
			startActivity(Intent.createChooser(intent,
					getResources().getText(R.string.menuShare)));
			return true;

			/* remind */
		case R.id.menuRemind:
			PendingIntent recv = PendingIntent.getBroadcast(
					getApplicationContext(), 0, reminder,
					PendingIntent.FLAG_NO_CREATE);
			if (recv == null)
				showDialog(DIALOG_REMIND_ID);
			else
				recv.cancel();
			return true;

			/* inverse colors */
		case R.id.menuInverse:
			intent = getIntent();
			getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).edit()
					.putBoolean("inverse", !item.isChecked()).commit();
			finish();
			startActivity(intent);
			return true;

			/* splash */
		case R.id.menuSplash:
			item.setChecked(!item.isChecked());
			getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).edit()
					.putBoolean("splash", item.isChecked()).commit();
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

		/* calendar */
		case DIALOG_DATE_ID:
			return new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							gotoDay(year * 10000 + (monthOfYear + 1) * 100
									+ dayOfMonth);
						}
					}, date.getYear() + 1900, date.getMonth(), date.getDate());

			/* notes */
		case DIALOG_NOTE_ID:
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
							reloadDay();
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
											reloadDay();
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

			/* notify */
		case DIALOG_REMIND_ID:

			/* load settings */
			int hour = getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).getInt("remind_hour", 9);
			int minute = getSharedPreferences(getString(R.string.app_name),
					Context.MODE_PRIVATE).getInt("remind_minute", 0);

			/* set picker */
			TimePickerDialog.OnTimeSetListener cb = new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hour, int minute) {

					/* save settings */
					getSharedPreferences(getString(R.string.app_name),
							Context.MODE_PRIVATE).edit()
							.putInt("remind_hour", hour)
							.putInt("remind_minute", minute).commit();

					/* get time */
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, minute);
					cal.set(Calendar.SECOND, 0);
					if (cal.before(Calendar.getInstance()))
						cal.add(Calendar.DAY_OF_YEAR, 1);

					/* receiver */
					PendingIntent recv = PendingIntent.getBroadcast(
							getApplicationContext(), 0, reminder,
							PendingIntent.FLAG_UPDATE_CURRENT);

					/* set reminder */
					((AlarmManager) getSystemService(Context.ALARM_SERVICE))
							.setRepeating(AlarmManager.RTC_WAKEUP,
									cal.getTimeInMillis(),
									AlarmManager.INTERVAL_DAY, recv);
				}
			};

			return new TimePickerDialog(this, cb, hour, minute, true);

			/* info */
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
				e.printStackTrace();
			}

			return dialog;
		}

		return null;
	}

	/* go to today */
	public void gotoDay() {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get day */
		date = new Date();
		reloadDay();
	}

	/* go to day by long */
	public void gotoDay(long date) {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get day */
		try {
			this.date = new SimpleDateFormat("yyyyMMdd").parse(String
					.valueOf(date));
			reloadDay();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/* go to day by string */
	public void gotoDay(String date) {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get day */
		try {
			this.date = new SimpleDateFormat("yyyyMMdd").parse(date);
			reloadDay();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/* go to next day */
	public void nextDay() {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_right);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_left);

		/* next day */
		date.setTime(date.getTime() + 24 * 60 * 60 * 1000);
		reloadDay();
	}

	/* go to previous day */
	public void prevDay() {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_left);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_right);

		/* previous day */
		date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
		reloadDay();
	}

	/* refresh day text */
	public void reloadDay() {
		ListView list = new ListView(getApplicationContext());
		list.setDivider(null);
		list.setAdapter(new CursorAdapter(this, dbh.getDateCursor(date)) {

			/* load layout */
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				getLayoutInflater();
				return getLayoutInflater().inflate(R.layout.verses, parent,
						false);
			}

			/* set item data */
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				((TextView) view.findViewById(R.id.verseTitle)).setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_TITLE)));
				((TextView) view.findViewById(R.id.verseVerse)).setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_VERSE)));
				((TextView) view.findViewById(R.id.verseText)).setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_TEXT)));
				if (cursor.isNull(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_AUTHOR))
						|| cursor
								.getString(
										cursor.getColumnIndex(VerseDatabase.TABLE_COLUMN_AUTHOR))
								.equals(""))
					((TextView) view.findViewById(R.id.verseAuthor))
							.setVisibility(View.GONE);
				((TextView) view.findViewById(R.id.verseAuthor)).setText(cursor.getString(cursor
						.getColumnIndex(VerseDatabase.TABLE_COLUMN_AUTHOR)));
			}
		});

		if (list.getCount() > 0)
			flipper.addView(list);
		else
			flipper.addView(getLayoutInflater().inflate(R.layout.empty, null));
		flipper.showNext();
		if (flipper.getChildCount() > 1)
			flipper.removeViewAt(0);

		setTitle(getString(R.string.app_name) + " "
				+ getString(R.string.textFor) + " "
				+ DateFormat.getDateInstance().format(date));
	}
}