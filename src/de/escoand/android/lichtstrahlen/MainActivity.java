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

package de.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import de.escoand.android.library.OnSwipeTouchListener;

public class MainActivity extends Activity implements DateSelectListener,
		ScriptureSelectListener, OnQueryTextListener {
	static final int TIMER_SPLASH = 2000;
	ViewFlipper flipper = null;
	TextDatabase db_text = null;
	NoteDatabase db_note = null;
	Cursor data_text = null;
	Date date = new Date();

	private OnSwipeTouchListener swipeListener = new OnSwipeTouchListener(
			getBaseContext()) {
		@Override
		public void onSwipeRight() {
			prevDay();
			super.onSwipeRight();
		}

		@Override
		public void onSwipeLeft() {
			nextDay();
			super.onSwipeLeft();
		}
	};

	/* db init */
	final class DBInit extends AsyncTask<Void, Void, Void> {
		private Date start = new Date();
		public boolean fullInit = true;

		/* show info and splash */
		@SuppressLint("InflateParams")
		@Override
		protected void onPreExecute() {
			if (fullInit) {
				if (PreferenceManager.getDefaultSharedPreferences(
						getBaseContext()).getBoolean("splash", true)) {
					Toast.makeText(getApplicationContext(),
							R.string.msgLoading, Toast.LENGTH_LONG).show();
					flipper.addView(getLayoutInflater().inflate(
							R.layout.splash, null));
				}
			}
			super.onPreExecute();
		}

		protected Void doInBackground(Void... params) {
			db_text = new TextDatabase(getBaseContext());
			db_note = new NoteDatabase(getBaseContext());
			return null;
		}

		/* hide splash */
		@Override
		protected void onPostExecute(Void result) {
			if (fullInit
					&& PreferenceManager.getDefaultSharedPreferences(
							getBaseContext()).getBoolean("splash", true)) {

				/* timer */
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
						nextDay();
					}
				}, TIMER_SPLASH + new Date().getTime() - start.getTime());
			}

			/* no splash */
			else
				showDay(date);

			super.onPostExecute(result);
		}
	}

	/* init gui */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getBoolean("inverse", false)) {
			setTheme(R.style.Theme_Light);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				getActionBar().setBackgroundDrawable(
						new ColorDrawable(getResources().getColor(
								R.color.primary)));
		} else
			setTheme(R.style.Theme);
		setContentView(R.layout.main);

		super.onCreate(savedInstanceState);
	}

	/* go to view */
	@Override
	protected void onResume() {
		DBInit init = new DBInit();
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		init.fullInit = false;
		init.execute();

		super.onResume();
	}

	/* save current date */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("date", date.getTime());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState,
			PersistableBundle persistentState) {
		// super.onRestoreInstanceState(savedInstanceState, persistentState);
	}

	/* clean stop */
	@Override
	protected void onStop() {
		data_text.close();
		db_text.close();
		db_note.close();
		super.onStop();
	}

	/* callback for creating option menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		MenuItem menu_item = menu.findItem(R.id.menuSearch);
		((SearchView) menu_item.getActionView()).setOnQueryTextListener(this);
		return super.onCreateOptionsMenu(menu);
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
			if (data_text.getCount() == 1)
				onScriptureSelect(data_text.getString(data_text
						.getColumnIndex(TextDatabase.COLUMN_VERSE)));
			else if (data_text.getCount() > 1) {
				String[] items = new String[data_text.getCount()];
				int i = 0;

				data_text.moveToFirst();
				data_text.moveToPrevious();
				while (data_text.moveToNext()) {
					System.err
							.println(i
									+ " "
									+ data_text.getString(data_text
											.getColumnIndex(TextDatabase.COLUMN_VERSE)));
					items[i++] = data_text.getString(data_text
							.getColumnIndex(TextDatabase.COLUMN_VERSE));
				}

				DayScriptureDialog scriptures = new DayScriptureDialog(items);
				scriptures.show(getFragmentManager(), "dayscriptures");
			}
			break;

		/* today */
		case R.id.menuToday:
			showDay(new Date());
			break;

		/* calendar */
		case R.id.menuDate:
			DialogFragment calendar = new CalendarDialog();
			calendar.show(getFragmentManager(), "calendar");
			break;

		/* scripture list */
		case R.id.menuList:
			DialogFragment scriptures = new ScriptureDialog();
			scriptures.show(getFragmentManager(), "scriptures");
			break;

		/* notes list */
		case R.id.menuNotes:
			DialogFragment notes = new NotesDialog();
			notes.show(getFragmentManager(), "notes");
			break;

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
			break;

		/* preferences */
		case R.id.menuPreference:
			intent = new Intent(getBaseContext(), Preferences.class);
			startActivity(intent);
			break;

		/* info */
		case R.id.menuAbout:
			DialogFragment about = new AboutDialog();
			about.show(getFragmentManager(), "about");
			break;

		/* ... */
		default:
			return false;
		}

		return true;
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

	/* refresh day text */
	public void showDay() {
		data_text = db_text.getDate(this.date);
		refreshTextList(false, true);
	}

	/* fts and show */
	private void showSearch(String search) {

		/* animation */
		flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get data */
		data_text = db_text.getSearch(search);
		refreshTextList(true, false);
	}

	/* show new entry in text list */
	private void refreshTextList(boolean showDate, boolean showFoot) {
		View container = getLayoutInflater().inflate(R.layout.daylist, flipper,
				false);
		ListView list = (ListView) container.findViewById(R.id.dayList);
		TextView empty = (TextView) container.findViewById(R.id.dayEmpty);

		list.setDivider(null);
		list.setEmptyView(empty);

		/* swipe gestures */
		list.setOnTouchListener(swipeListener);
		empty.setOnTouchListener(swipeListener);

		list.setAdapter(new CursorAdapter(this, data_text, 0) {
			private final SimpleDateFormat df = new SimpleDateFormat(
					"yyyyMMdd", Locale.getDefault());
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
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					((View) etNote.getParent()).setVisibility(View.VISIBLE);
				} else
					((View) etNote.getParent()).setVisibility(View.GONE);

				/* text size */
				int scale = getResources().getInteger(
						R.integer.default_text_size);
				try {
					scale = Integer.valueOf(PreferenceManager
							.getDefaultSharedPreferences(getBaseContext())
							.getString(
									"scale",
									Integer.toString(getResources().getInteger(
											R.integer.default_text_size))));
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

	/* date selected */
	@Override
	public void onDateSelect(Date date) {
		DialogFragment dialog;
		for (String tag : new String[] { "calendar", "scriptures",
				"dayscriptures", "notes" }) {
			dialog = (DialogFragment) getFragmentManager().findFragmentByTag(
					tag);
			if (dialog != null)
				dialog.dismiss();
		}
		showDay(date);
	}

	/* show scripture */
	@Override
	public void onScriptureSelect(String scripture) {
		String url = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString("scriptureUrl",
				getString(R.string.scriptureUrlDefault))
				+ scripture.replaceAll(" ", "");

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

	/* search */
	@Override
	public boolean onQueryTextSubmit(String query) {
		showSearch(query);
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}
}