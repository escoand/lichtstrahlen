package com.escoand.android.lichtstrahlen;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.escoand.android.lichtstrahlen_2012.R;

public class ReminderService extends Service {
	@Override
	public void onStart(Intent intent, int startId) {
		Calendar cal = Calendar.getInstance();
		PendingIntent recv = null;
		Notification notify = new Notification();
		Bundle bundle = intent.getExtras();

		/* load settings */
		int hour = getSharedPreferences(getString(R.string.app_name),
				Context.MODE_PRIVATE).getInt("remind_hour", 9);
		int minute = getSharedPreferences(getString(R.string.app_name),
				Context.MODE_PRIVATE).getInt("remind_minute", 0);

		/* get time */
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH), hour, minute, 0);

		/* receiver */
		recv = PendingIntent.getActivity(getApplicationContext(), 0,
				new Intent(getApplicationContext(), MainActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
				PendingIntent.FLAG_UPDATE_CURRENT);

		/* notification */
		notify.icon = bundle.getInt("icon");
		notify.tickerText = bundle.getString("ticker");
		notify.when = cal.getTimeInMillis();
		notify.defaults = Notification.DEFAULT_SOUND
				| Notification.DEFAULT_VIBRATE;
		notify.flags = Notification.FLAG_ONLY_ALERT_ONCE;
		notify.setLatestEventInfo(getApplicationContext(),
				bundle.getString("title"), bundle.getString("message"), recv);
		((NotificationManager) getApplicationContext().getSystemService(
				Context.NOTIFICATION_SERVICE)).notify(0, notify);

		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
