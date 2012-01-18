package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.util.Date;

import com.escoand.android.lichtstrahlen_2012.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Lichtstrahlen extends Activity {
	public static final int DIALOG_ABOUT_ID = 0;
	public static final int DIALOG_DATE_ID = 1;
	public static final int DIALOG_NOTE_ID = 2;
	public static final String BIBLE_URL = "http://www.bibleserver.com/text/";

	public Date date = new Date();
	public CharSequence[] verses = null;
	public Notes notes = null;
	ViewFlipper flipper = null;

	public ProgressDialog progress = null;
	public AlertDialog selection = null;

	private GestureDetector gesture = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* init */
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.msgWait));
		gesture = new GestureDetector(new Gestures(this));
		notes = new Notes(this);
		new AsyncVerse(this).execute();

		/* animation */
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);
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

	/* callback for showing option menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	/* callback for clicking option item */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		/* scripture */
		case R.id.menuBible:
			if (verses.length == 1) {
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(BIBLE_URL
								+ verses[0].toString().replaceAll(" ", ""))));
				return true;
			} else if (verses.length > 1) {
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setCancelable(true);
				adb.setTitle(getString(R.string.listVerses));
				adb.setItems(verses, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse(BIBLE_URL
										+ verses[item].toString().replaceAll(
												" ", ""))));
					}
				});
				selection = adb.create();
				selection.show();
				return true;
			}
			return false;

			/* today */
		case R.id.menuToday:

			/* animation */
			ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
			flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
			flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

			/* go to today */
			date = new Date();
			new AsyncVerse(this).execute();
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
			new AsyncVerseList(this).execute();
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

							/* animation */
							ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
							flipper.setInAnimation(getApplicationContext(),
									R.anim.in_alpha);
							flipper.setOutAnimation(getApplicationContext(),
									R.anim.out_alpha);

							/* goto date */
							date.setYear(year - 1900);
							date.setMonth(monthOfYear);
							date.setDate(dayOfMonth);
							new AsyncVerse(Lichtstrahlen.this).execute();
						}
					}, date.getYear() + 1900, date.getMonth(), date.getDate());

			/* notes */
		case DIALOG_NOTE_ID:
			dialog.setTitle(getString(R.string.noteNote) + " "
					+ getString(R.string.textFor) + " "
					+ DateFormat.getDateInstance().format(date));
			dialog.setContentView(R.layout.noteedit);

			// load note
			if (notes.exist(date))
				((TextView) dialog.findViewById(R.id.noteText)).setText(notes
						.get(date));

			// callback for save note
			dialog.findViewById(R.id.noteSave).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// save note
							View parent = (View) v.getParent().getParent();
							notes.add(date, ((TextView) parent
									.findViewById(R.id.noteText)).getText()
									.toString());

							// back to main
							dismissDialog(DIALOG_NOTE_ID);
							new AsyncVerse(Lichtstrahlen.this).execute();
							Toast.makeText(getApplicationContext(),
									getString(R.string.noteSaved),
									Toast.LENGTH_LONG).show();
						}
					});

			// callback for delete note
			dialog.findViewById(R.id.noteDelete).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							AlertDialog.Builder adb = new AlertDialog.Builder(
									getApplicationContext());
							adb.setMessage(
									getString(R.string.noteDeleteQuestion))
									.setCancelable(false)
									.setPositiveButton(
											getString(R.string.buttonYes),
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int id) {
													// delete note
													// notes.remove(date);

													// back to main
													dialog.dismiss();
													getParent().dismissDialog(
															DIALOG_NOTE_ID);
													new AsyncVerse(
															Lichtstrahlen.this)
															.execute();
													Toast.makeText(
															getApplicationContext(),
															getString(R.string.noteDeleted),
															Toast.LENGTH_LONG)
															.show();
												}
											})
									.setNegativeButton(
											getString(R.string.buttonNo),
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int id) {
													dialog.cancel();
												}
											}).create().show();
						}
					});

			// callback for cancel
			dialog.findViewById(R.id.noteCancel).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// back to main
							dismissDialog(DIALOG_NOTE_ID);
						}
					});

			return dialog;

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

	/* go to next day */
	public void nextDay() {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_right);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_left);

		/* next day */
		date.setTime(date.getTime() + 24 * 60 * 60 * 1000);
		new AsyncVerse(this).execute();
	}

	/* go to previous day */
	public void prevDay() {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_left);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_right);

		/* previous day */
		date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
		new AsyncVerse(this).execute();

	}
}
