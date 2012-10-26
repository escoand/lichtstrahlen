package com.escoand.android.lichtstrahlen;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		PendingIntent recv = null;
		Notification notify = new Notification();
		Bundle bundle = i.getExtras();

		/* receiver */
		recv = PendingIntent.getActivity(c, 0,
				new Intent(c, MainActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
				PendingIntent.FLAG_UPDATE_CURRENT);

		/* notification */
		notify.icon = bundle.getInt("icon");
		notify.tickerText = bundle.getString("ticker");
		notify.when = System.currentTimeMillis();
		notify.defaults = Notification.DEFAULT_SOUND
				| Notification.DEFAULT_VIBRATE;
		notify.flags = Notification.FLAG_AUTO_CANCEL;
		notify.setLatestEventInfo(c, bundle.getString("title"),
				bundle.getString("message"), recv);
		((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(0, notify);
	}
}
