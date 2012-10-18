package com.escoand.android.lichtstrahlen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.escoand.android.lichtstrahlen_2013.R;

public class Preferences extends PreferenceActivity {
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
		// FIXME implement reminder dialog and settings

		// /* remind */
		// case R.id.menuRemind:
		// PendingIntent recv = PendingIntent.getBroadcast(
		// getApplicationContext(), 0, reminder,
		// PendingIntent.FLAG_NO_CREATE);
		// if (recv == null)
		// showDialog(DIALOG_REMIND_ID);
		// else
		// recv.cancel();
		// return true;
		// View item = null;
		// onPreferenceChangeListener
		// /* reminder */
		// item = menu.findItem(R.id.menuRemind);
		// if (item != null) {
		// int hour = getSharedPreferences(getString(R.string.app_name),
		// Context.MODE_PRIVATE).getInt("remind_hour", 9);
		// int minute = getSharedPreferences(getString(R.string.app_name),
		// Context.MODE_PRIVATE).getInt("remind_minute", 0);
		// item.setTitle(getString(R.string.menuRemind)
		// + String.format(" (%02d:%02d)", hour, minute));
		// item.setChecked(PendingIntent.getBroadcast(getApplicationContext(),
		// 0, reminder, PendingIntent.FLAG_NO_CREATE) != null);
		// }

		// /* notify */
		// case DIALOG_REMIND_ID:
		//
		// /* load settings */
		// int hour = getSharedPreferences(getString(R.string.app_name),
		// Context.MODE_PRIVATE).getInt("remind_hour", 9);
		// int minute = getSharedPreferences(getString(R.string.app_name),
		// Context.MODE_PRIVATE).getInt("remind_minute", 0);
		//
		// /* set picker */
		// TimePickerDialog.OnTimeSetListener cb = new
		// TimePickerDialog.OnTimeSetListener() {
		// @Override
		// public void onTimeSet(TimePicker view, int hour, int minute) {
		//
		// /* save settings */
		// getSharedPreferences(getString(R.string.app_name),
		// Context.MODE_PRIVATE).edit()
		// .putInt("remind_hour", hour)
		// .putInt("remind_minute", minute).commit();
		//
		// /* get time */
		// Calendar cal = Calendar.getInstance();
		// cal.set(Calendar.HOUR_OF_DAY, hour);
		// cal.set(Calendar.MINUTE, minute);
		// cal.set(Calendar.SECOND, 0);
		// if (cal.before(Calendar.getInstance()))
		// cal.add(Calendar.DAY_OF_YEAR, 1);
		//
		// /* receiver */
		// PendingIntent recv = PendingIntent.getBroadcast(
		// getApplicationContext(), 0, reminder,
		// PendingIntent.FLAG_UPDATE_CURRENT);
		//
		// /* set reminder */
		// ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
		// .setRepeating(AlarmManager.RTC_WAKEUP,
		// cal.getTimeInMillis(),
		// AlarmManager.INTERVAL_DAY, recv);
		// }
		// };
		//
		// return new TimePickerDialog(this, cb, hour, minute, true);

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		/* restart after changed */
		findPreference("inverse").setOnPreferenceChangeListener(changed);
		findPreference("scale").setOnPreferenceChangeListener(changed);
	}
}
