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

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.TimePicker;

import de.escoand.android.lichtstrahlen.R;

public class Preferences extends PreferenceActivity {
	private static SharedPreferences prefs;
	private static final int DIALOG_REMIND_ID = 3;

	OnPreferenceChangeListener changed = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent intent = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(
							getBaseContext().getPackageName());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
	};

	OnPreferenceChangeListener updateWidget = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent intent = new Intent(getApplicationContext(), Widget.class);
			intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			int ids[] = AppWidgetManager.getInstance(getApplicationContext())
					.getAppWidgetIds(
							new ComponentName(getApplication(), Widget.class));
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
			sendBroadcast(intent);
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		/* restart after changed */
		findPreference("inverse").setOnPreferenceChangeListener(changed);
		findPreference("scale").setOnPreferenceChangeListener(changed);

		/* update widget after changed */
		findPreference("widgetInverse").setOnPreferenceChangeListener(
				updateWidget);

		/* reminder */
		findPreference("remind").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						showDialog(DIALOG_REMIND_ID);
						return false;
					}
				});
		findPreference("remind").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						return false;
					}
				});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_REMIND_ID:

			/* get settings */
			int hour = prefs.getInt("remind_hour", 9);
			int minute = prefs.getInt("remind_minute", 0);

			/* dialog */
			TimePickerDialog dialog = new TimePickerDialog(this,
					new OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker view, int hour,
								int minute) {
							prefs.edit().putInt("remind_hour", hour)
									.putInt("remind_minute", minute).commit();
							initReminder();
						}
					}, hour, minute, true);

			/* ok button */
			dialog.setButton(DialogInterface.BUTTON_POSITIVE,
					getString(android.R.string.ok), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							initReminder(true);
						}
					});

			/* cancel button */
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					getString(android.R.string.cancel), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							initReminder(false);
						}
					});

			/* back key */
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					initReminder(false);
				}
			});

			return dialog;
		}

		return super.onCreateDialog(id);
	}

	private void initReminder(boolean state) {
		prefs.edit().putBoolean("remind", state).commit();
		((CheckBoxPreference) findPreference("remind")).setChecked(state);
		initReminder();
	}

	private void initReminder() {
		Intent intent = new Intent(getBaseContext(), Receiver.class);
		intent.setAction("de.escoand.android.lichtstrahlen.INIT_REMINDER");
		getBaseContext().sendBroadcast(intent);
	}
}
