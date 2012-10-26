package com.escoand.android.lichtstrahlen;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.escoand.android.lichtstrahlen_2013.R;

public class Reminder extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* set info */
		Intent reminder = new Intent(getBaseContext(),
				NotificationReceiver.class);
		reminder.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		reminder.putExtra("icon", R.drawable.icon);
		reminder.putExtra("info", getBaseContext().getString(R.string.app_name)
				+ " - " + getBaseContext().getString(R.string.msgRemind));
		reminder.putExtra("title", getBaseContext()
				.getString(R.string.app_name));
		reminder.putExtra("message",
				getBaseContext().getString(R.string.msgRemind));

		/* start reminder */
		if (PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getBoolean("remind", false)) {
			int hour = PreferenceManager.getDefaultSharedPreferences(
					getBaseContext()).getInt("remind_hour", 9);
			int minute = PreferenceManager.getDefaultSharedPreferences(
					getBaseContext()).getInt("remind_minute", 0);

			/* get time */
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			if (cal.before(Calendar.getInstance()))
				cal.add(Calendar.DAY_OF_YEAR, 1);

			/* receiver */
			PendingIntent recv = PendingIntent.getBroadcast(getBaseContext(),
					0, reminder, PendingIntent.FLAG_UPDATE_CURRENT);

			/* set reminder */
			((AlarmManager) getSystemService(Context.ALARM_SERVICE))
					.setRepeating(AlarmManager.RTC_WAKEUP,
							cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
							recv);
		}

		/* stop reminder */
		else
			stopService(reminder);
		
		finish();
	}
}
