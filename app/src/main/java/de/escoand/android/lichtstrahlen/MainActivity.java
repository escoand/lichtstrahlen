/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.escoand.android.lichtstrahlen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.escoand.android.lichtstrahlen.R;

import java.util.Date;

import de.escoand.android.library.OnSwipeTouchListener;

public class MainActivity extends Activity implements DateSelectListener,
        ScriptureSelectListener, OnQueryTextListener {
    static final int TIMER_SPLASH = 2000;

    TextDatabase db = null;
    Cursor data = null;
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

    /* init database */
    @Override
    protected void onResume() {
        DBInit init = new DBInit();
        init.fullInit = false;
        init.execute();
        super.onResume();
    }

    /* save current state */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("date", date.getTime());
        super.onSaveInstanceState(outState);
    }

    /* load last state */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState,
                                       PersistableBundle persistentState) {
        date = new Date(savedInstanceState.getLong("date"));
        // super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    /* clean stop */
    @Override
    protected void onStop() {
        data.close();
        db.close();
        super.onStop();
    }

    /* create option menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem menu_item = menu.findItem(R.id.menuSearch);
        ((SearchView) menu_item.getActionView()).setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    /* show option menu */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = null;

        if (menu == null)
            return false;

		/* (un)hide items */
        item = menu.findItem(R.id.menuDayScriptures);
        if (item != null)
            item.setEnabled(data != null && data.getCount() != 0);
        item = menu.findItem(R.id.menuShare);
        if (item != null)
            item.setEnabled(data != null && data.getCount() != 0);

        return super.onPrepareOptionsMenu(menu);
    }

    /* option item clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

        switch (item.getItemId()) {

		/* scripture */
            case R.id.menuDayScriptures:
                if (data.getCount() == 1)
                    onScriptureSelect(data.getString(data
                            .getColumnIndex(TextDatabase.COLUMN_VERSE)));
                else if (data.getCount() > 1) {
                    String[] items = new String[data.getCount()];
                    int i = 0;

                    data.moveToFirst();
                    data.moveToPrevious();
                    while (data.moveToNext()) {
                        System.err
                                .println(i
                                        + " "
                                        + data.getString(data
                                        .getColumnIndex(TextDatabase.COLUMN_VERSE)));
                        items[i++] = data.getString(data
                                .getColumnIndex(TextDatabase.COLUMN_VERSE));
                    }

                    DayScriptureDialog scriptures = new DayScriptureDialog(items);
                    scriptures.show(getFragmentManager(),
                            getString(R.id.menuDayScriptures));
                }
                break;

		/* today */
            case R.id.menuToday:
                showDay(new Date());
                break;

		/* calendar */
            case R.id.menuCalendar:
                DialogFragment calendar = new CalendarDialog();
                calendar.show(getFragmentManager(), getString(R.id.menuCalendar));
                break;

		/* scripture list */
            case R.id.menuScriptures:
                DialogFragment scriptures = new ScriptureDialog();
                scriptures.show(getFragmentManager(),
                        getString(R.id.menuScriptures));
                break;

		/* notes list */
            case R.id.menuNotes:
                DialogFragment notes = new NotesDialog();
                notes.show(getFragmentManager(), getString(R.id.menuNotes));
                break;

		/* share */
            case R.id.menuShare:
                String text = data.getString(data
                        .getColumnIndex(TextDatabase.COLUMN_TITLE))
                        + " ("
                        + data.getString(data
                        .getColumnIndex(TextDatabase.COLUMN_VERSE))
                        + ")\n\n"
                        + data.getString(data
                        .getColumnIndex(TextDatabase.COLUMN_TEXT))
                        + ")\n\n"
                        + data.getString(data
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
                about.show(getFragmentManager(), getString(R.id.menuAbout));
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
        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.setInAnimation(getApplicationContext(), R.anim.in_right);
        flipper.setOutAnimation(getApplicationContext(), R.anim.out_left);

		/* next day */
        date.setTime(date.getTime() + 24 * 60 * 60 * 1000);
        showDay();
    }

    /* go to previous day */
    public void prevDay() {

		/* animation */
        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.setInAnimation(getApplicationContext(), R.anim.in_left);
        flipper.setOutAnimation(getApplicationContext(), R.anim.out_right);

		/* previous day */
        date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
        showDay();
    }

    /* go to today */
    public void showDay(Date date) {

		/* animation */
        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
        flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get day */
        this.date = date;
        showDay();
    }

    /* refresh day text */
    public void showDay() {
        data = db.getDate(this.date);
        refreshTextList(false, true);
    }

    /* fts and show */
    private void showSearch(String search) {

		/* animation */
        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.setInAnimation(getApplicationContext(), R.anim.in_alpha);
        flipper.setOutAnimation(getApplicationContext(), R.anim.out_alpha);

		/* get data */
        data = db.getSearch(search);
        refreshTextList(true, false);
    }

    /* show new entry in text list */
    private void refreshTextList(boolean showDate, boolean showFoot) {
        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        View container = getLayoutInflater().inflate(R.layout.daylist, flipper,
                false);
        ListView list = (ListView) container.findViewById(R.id.dayList);
        TextView empty = (TextView) container.findViewById(R.id.dayEmpty);

        list.setDivider(null);
        list.setEmptyView(empty);

		/* swipe gestures */
        list.setOnTouchListener(swipeListener);
        empty.setOnTouchListener(swipeListener);

        list.setAdapter(new DaysAdapter(this, data));

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
        for (int id : new int[]{R.id.menuDayScriptures, R.id.menuCalendar,
                R.id.menuScriptures, R.id.menuNotes, R.id.menuAbout}) {
            dialog = (DialogFragment) getFragmentManager().findFragmentByTag(
                    getString(id));
            if (dialog != null)
                dialog.dismiss();
        }
        showDay(date);
    }

    /* scripture selected */
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

    /* db init */
    final class DBInit extends AsyncTask<Void, Void, Void> {
        public boolean fullInit = true;
        private Date start = new Date();

        /* show info and splash */
        @SuppressLint("InflateParams")
        @Override
        protected void onPreExecute() {
            ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
            if (PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                    .getBoolean("splash", true))
                flipper.addView(getLayoutInflater().inflate(R.layout.splash,
                        null));
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params) {
            db = new TextDatabase(getBaseContext());
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
}