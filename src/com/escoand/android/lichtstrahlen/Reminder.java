package com.escoand.android.lichtstrahlen;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.escoand.android.lichtstrahlen_2012.R;

public class Reminder extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Bundle bundle = intent.getExtras();

		PendingIntent pintent = PendingIntent.getBroadcast(context, 0, intent,
				0);

		Notification notify = new Notification(
				R.drawable.icon,
				bundle.getString("title") + " - " + bundle.getString("message"),
				System.currentTimeMillis());

		notify.setLatestEventInfo(context, bundle.getString("title"),
				bundle.getString("message"), pintent);

		manager.notify(0, notify);

	}
}
