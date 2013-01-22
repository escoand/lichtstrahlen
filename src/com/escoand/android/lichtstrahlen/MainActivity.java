/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.escoand.android.lichtstrahlen_2013.R;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {
	private static final int TIMER_SPLASH = 2000;
	private GestureDetector gesture = null;
	private ViewFlipper flipper = null;
	private TextDatabase db_text = null;
	private NoteDatabase db_note = null;
	private Cursor data_text = null;
	private Cursor data_verses = null;
	private Menu menu = null;
	private MenuItem menu_item = null;
	private EditText txt_search = null;
	public Date date = new Date();

	/* db init */
	final class DBInit extends AsyncTask<Void, Void, Void> {
		private Date start = new Date();
		public boolean fullInit = true;

		/* show info and splash */
		@Override
		protected void onPreExecute() {
			if (fullInit) {
				Toast.makeText(getApplicationContext(), R.string.msgLoading,
						Toast.LENGTH_LONG).show();
				if (PreferenceManager.getDefaultSharedPreferences(
						getBaseContext()).getBoolean("splash", true))
					flipper.addView(getLayoutInflater().inflate(
							R.layout.splash, null));
			}
			super.onPreExecute();
		}

		protected Void doInBackground(Void... params) {
			db_text = new TextDatabase(getBaseContext());
			db_note = new NoteDatabase(getBaseContext());
			return null;
		}

		/* hide info and splash */
		@Override
		protected void onPostExecute(Void result) {
			if (fullInit
					&& PreferenceManager.getDefaultSharedPreferences(
							getBaseContext()).getBoolean("splash", true)) {
				date.setTime(date.getTime() - 24 * 60 * 60 * 1000);

				/* timer */
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						nextDay();
					}
				}, TIMER_SPLASH + new Date().getTime() - start.getTime());
			}

			/* no splash */
			else
				showDay(new Date());

			super.onPostExecute(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DBInit init = new DBInit();

		/* themes */
		if (!PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getBoolean("inverse", false))
			setTheme(R.style.Light);
		else
			setTheme(R.style.Dark);

		/* show */
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* init */
		gesture = new GestureDetector(new Gestures(this));
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		if (savedInstanceState != null)
			init.fullInit = false;
		init.execute();
	}

	/* clean stop */
	@Override
	protected void onStop() {
		if (data_text != null)
			data_text.close();
		db_text.close();
		db_note.close();
		super.onStop();
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
		this.menu = menu;
		getMenuInflater().inflate(R.menu.options_menu, menu);
		createActionView();
		return super.onCreateOptionsMenu(menu);
	}

	/* init action view */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void createActionView() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			return;
		menu_item = menu.findItem(R.id.menuSearch);
		SearchView search = (SearchView) menu_item.getActionView();
		if (menu_item != null && search != null)
			search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextChange(String arg0) {
					return false;
				}

				@Override
				public boolean onQueryTextSubmit(String query) {
					showSearch(query);
					menu_item.collapseActionView();
					return true;
				}
			});
	}

	/* callback for showing option menu */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = null;

		if (menu == null)
			return false;

		/* (un)hide items */
		item = menu.findItem(R.id.menuBible);
		if (item != null)
			item.setEnabled(data_text != null && data_text.getCount() != 0);
		item = menu.findItem(R.id.menuShare);
		if (item != null)
			item.setEnabled(data_text != null && data_text.getCount() != 0);

		return super.onPrepareOptionsMenu(menu);
	}

	/* callback for clicking option item */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;

		switch (item.getItemId()) {

		/* scripture */
		case R.id.menuBible:
			if (data_text.getCount() == 1) {
				data_text.moveToFirst();
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getString(R.string.sciptureUrl)
						+ PreferenceManager.getDefaultSharedPreferences(this)
								.getString("translation", "")
						+ "/"
						+ data_text
								.getString(
										data_text
												.getColumnIndex(TextDatabase.COLUMN_VERSE))
								.replaceAll(" ", "")));
				startActivity(intent);
				return true;

			} else if (data_text.getCount() > 1) {
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

			/* search */
		case R.id.menuSearch:
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				showDialog(R.id.menuSearch);
			return true;

			/* scripture list */
		case R.id.menuList:
			showDialog(R.id.menuList);
			return true;

			/* notes list */
		case R.id.menuNotes:
			showDialog(R.id.menuNotes);
			return true;

			/* share */
		case R.id.menuShare:
			String text = data_text.getString(data_text
					.getColumnIndex(TextDatabase.COLUMN_TITLE))
					+ " ("
					+ data_text.getString(data_text
							.getColumnIndex(TextDatabase.COLUMN_VERSE))
					+ ")\n\n"
					+ data_text.getString(data_text
							.getColumnIndex(TextDatabase.COLUMN_TEXT))
					+ ")\n\n"
					+ data_text.getString(data_text
							.getColumnIndex(TextDatabase.COLUMN_AUTHOR))
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
			return new AlertDialog.Builder(this)
					.setCancelable(true)
					.setTitle(getString(R.string.listVerses))

					/* on click */
					.setCursor(data_text,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int item) {
									data_text.moveToPosition(item);
									Intent intent = new Intent(
											Intent.ACTION_VIEW);
									intent.setData(Uri
											.parse(getString(R.string.sciptureUrl)
													+ data_text
															.getString(
																	data_text
																			.getColumnIndex(TextDatabase.COLUMN_VERSE))
															.replaceAll(" ", "")));
									startActivity(intent);
								}
							}, TextDatabase.COLUMN_VERSE)

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

			/* search */
		case R.id.menuSearch:
			txt_search = new EditText(this);
			return new AlertDialog.Builder(this)
					.setCancelable(true)
					.setTitle(R.string.menuSearch)
					.setMessage(R.string.searchMessage)
					.setView(txt_search)
					.setPositiveButton(android.R.string.search_go,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									showSearch(txt_search.getText().toString());
								}
							}).create();

			/* verse list */
		case R.id.menuList:
			if (data_verses == null)
				data_verses = db_text.getList();
			return new AlertDialog.Builder(this).setCancelable(true)
					.setTitle(getString(R.string.menuList))

					/* data for list */
					.setAdapter(new CursorAdapter(this, data_verses) {
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
							verse = cursor.getString(cursor
									.getColumnIndex(TextDatabase.COLUMN_VERSE));
							try {
								date = df
										.parse(cursor.getString(cursor
												.getColumnIndex(TextDatabase.COLUMN_DATE)));
								date_until = df
										.parse(cursor.getString(cursor
												.getColumnIndex(TextDatabase.COLUMN_DATE
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
							data_text.moveToPosition(item);
							showDay(data_verses.getString(data_verses
									.getColumnIndex(TextDatabase.COLUMN_DATE)));
						}
					}).create();

			/* notes list */
		case R.id.menuNotes:
			return new AlertDialog.Builder(this)
					.setCancelable(true)
					.setTitle(getString(R.string.listNotes))

					/* data for list */
					.setAdapter(
							new CursorAdapter(this, db_note.getListCursor()) {
								private final SimpleDateFormat df = new SimpleDateFormat(
										"yyyyMMdd");
								SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT);
								TextView tvDate, tvNote;
								Date date;
								String text;

								/* inflate layout */
								@Override
								public View newView(Context context,
										Cursor cursor, ViewGroup parent) {
									return getLayoutInflater().inflate(
											R.layout.noteentry, parent, false);
								}

								/* set item data */
								@Override
								public void bindView(View view,
										Context context, Cursor cursor) {
									tvDate = (TextView) view
											.findViewById(R.id.listDate);
									tvNote = (TextView) view
											.findViewById(R.id.listNote);
									try {
										date = df
												.parse(cursor.getString(cursor
														.getColumnIndex(NoteDatabase.COLUMN_DATE)));
										text = cursor
												.getString(cursor
														.getColumnIndex(NoteDatabase.COLUMN_TEXT));
									} catch (Exception e) {
										// e.printStackTrace();
									}

									/* show */
									tvDate.setText(df_ymd.format(date));
									tvNote.setText(text);
								}
							},

							/* on click */
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int item) {
									Cursor cursor = db_note.getListCursor();
									cursor.moveToPosition(item);
									showDay(cursor.getString(cursor
											.getColumnIndex(NoteDatabase.COLUMN_DATE)));
									removeDialog(R.id.menuNotes);
								}
							})

					/* on cancel */
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							removeDialog(R.id.menuNotes);
						}
					})

					/* create */
					.create();

			/* info */
		case R.id.menuInfo:
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.about);

			try {
				/*
				 * ((TextView) dialog.findViewById(R.id.txtVersion))
				 * .setText("Version " + getPackageManager().getPackageInfo(
				 * getPackageName(), 0).versionName);
				 */
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
		data_text = db_text.getDate(this.date);
		refreshTextList(false, true);

		/* (de)activate menu items */
		onPrepareOptionsMenu(menu);
	}

	/* fts and show */
	private void showSearch(String search) {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get data */
		data_text = db_text.getSearch(search);
		refreshTextList(true, false);

		/* (de)activate menu items */
		onPrepareOptionsMenu(menu);
	}

	/* show new entry in text list */
	private void refreshTextList(boolean showDate, boolean showFoot) {
		View container = getLayoutInflater().inflate(R.layout.daylist, flipper,
				false);
		ListView list = (ListView) container.findViewById(R.id.dayList);
		TextView empty = (TextView) container.findViewById(R.id.dayEmpty);

		list.setDivider(null);
		list.setEmptyView(empty);

		list.setAdapter(new CursorAdapter(this, data_text) {
			private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat
					.getDateInstance(DateFormat.LONG);

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
				Date date = null;
				Date date_before = null;
				Date date_after = null;
				TextView tvDate = (TextView) view.findViewById(R.id.verseDate);
				TextView tvTitle = (TextView) view
						.findViewById(R.id.verseTitle);
				TextView tvVerse = (TextView) view
						.findViewById(R.id.verseVerse);
				TextView tvText = (TextView) view.findViewById(R.id.verseText);
				TextView tvAuthor = (TextView) view
						.findViewById(R.id.verseAuthor);
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
				if (!cursor.isNull(cursor
						.getColumnIndex(TextDatabase.COLUMN_AUTHOR))
						&& !cursor
								.getString(
										cursor.getColumnIndex(TextDatabase.COLUMN_AUTHOR))
								.equals("")) {
					tvAuthor.setText(cursor.getString(cursor
							.getColumnIndex(TextDatabase.COLUMN_AUTHOR)));
					tvAuthor.setVisibility(View.VISIBLE);
				} else
					tvAuthor.setVisibility(View.GONE);

				/* note */
				if (date_after == null || date_after.compareTo(date) != 0) {
					etNote.setText(db_note.getDateNote(date));
					btSave.setTag(date);
					btSave.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								Date date = (Date) v.getTag();
								String text = ((TextView) ((View) v.getParent())
										.findViewById(R.id.noteText)).getText()
										.toString();
								db_note.setDateNote(date, text);
								Toast.makeText(getApplicationContext(),
										getString(R.string.noteSaved),
										Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					((View) etNote.getParent()).setVisibility(View.VISIBLE);
				} else
					((View) etNote.getParent()).setVisibility(View.GONE);

				/* text size */
				int scale = R.dimen.default_text_size;
				try {
					scale = Integer.valueOf(PreferenceManager
							.getDefaultSharedPreferences(getBaseContext())
							.getString("scale",
									Integer.toString(R.dimen.default_text_size)));
				} catch (Exception e) {
				}
				tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
				tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
				tvVerse.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
				tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);
				tvAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale);

			}
		});

		/* add to flipper */
		flipper.addView(container);
		flipper.showNext();
		if (flipper.getChildCount() > 1)
			flipper.removeViewAt(0);
	}
}