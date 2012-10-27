package com.escoand.android.lichtstrahlen;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Notification extends BroadcastReceiver {
	PendingIntent receiver = null;
	android.app.Notification notification = null;
	Bundle bundle = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		notification = new android.app.Notification();
		bundle = intent.getExtras();

		/* receiver */
		receiver = PendingIntent.getActivity(context, 0, new Intent(context,
				MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
				PendingIntent.FLAG_UPDATE_CURRENT);

		/* notification */
		notification.icon = bundle.getInt("icon");
		notification.tickerText = bundle.getString("ticker");
		notification.when = System.currentTimeMillis();
		notification.defaults = android.app.Notification.DEFAULT_SOUND
				| android.app.Notification.DEFAULT_VIBRATE;
		notification.flags = android.app.Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(context, bundle.getString("title"),
				bundle.getString("message"), receiver);
		((NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,
				notification);
	}
}
