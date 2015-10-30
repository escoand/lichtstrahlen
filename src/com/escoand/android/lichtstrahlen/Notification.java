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
		bundle = intent.getExtras();

		/* receiver */
		receiver = PendingIntent.getActivity(context, 0,
				new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
				PendingIntent.FLAG_UPDATE_CURRENT);

		/* notification */
		notification = new android.app.Notification.Builder(context).setSmallIcon(bundle.getInt("icon"))
				.setContentTitle(bundle.getString("title")).setContentText(bundle.getString("message"))
				.setTicker(bundle.getString("ticker")).setAutoCancel(true).setContentIntent(receiver).build();
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
	}
}
