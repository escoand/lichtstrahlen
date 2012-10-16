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

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		/* restart after changed */
		findPreference("inverse").setOnPreferenceChangeListener(changed);
		findPreference("scale").setOnPreferenceChangeListener(changed);
	}
}
