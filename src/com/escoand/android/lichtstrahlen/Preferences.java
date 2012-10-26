package com.escoand.android.lichtstrahlen;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.TimePicker;

import com.escoand.android.lichtstrahlen_2013.R;

public class Preferences extends PreferenceActivity {
	public static final int DIALOG_REMIND_ID = 3;

	OnPreferenceChangeListener changed = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent intent = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(
							getBaseContext().getPackageName());
			finish();
			startActivity(intent);
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		/* restart after changed */
		findPreference("inverse").setOnPreferenceChangeListener(changed);
		findPreference("scale").setOnPreferenceChangeListener(changed);

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
			int hour = PreferenceManager.getDefaultSharedPreferences(
					getBaseContext()).getInt("remind_hour", 9);
			int minute = PreferenceManager.getDefaultSharedPreferences(
					getBaseContext()).getInt("remind_minute", 0);

			/* dialog */
			TimePickerDialog dialog = new TimePickerDialog(this,
					new OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker view, int hour,
								int minute) {
							PreferenceManager
									.getDefaultSharedPreferences(
											getBaseContext()).edit()
									.putBoolean("remind", true)
									.putInt("remind_hour", hour)
									.putInt("remind_minute", minute).commit();
							((CheckBoxPreference) findPreference("remind"))
									.setChecked(true);
							// startReminder(hour, minute);
						}
					}, hour, minute, true);

			/* cancel button */
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					getString(android.R.string.cancel), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							PreferenceManager
									.getDefaultSharedPreferences(
											getBaseContext()).edit()
									.putBoolean("remind", false).commit();
							((CheckBoxPreference) findPreference("remind"))
									.setChecked(false);
							// stopReminder();
						}
					});

			/* cancel */
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					PreferenceManager
							.getDefaultSharedPreferences(getBaseContext())
							.edit().putBoolean("remind", false).commit();
					((CheckBoxPreference) findPreference("remind"))
							.setChecked(false);
					// stopReminder();
				}
			});

			return dialog;
		}
		return super.onCreateDialog(id);
	}
}
