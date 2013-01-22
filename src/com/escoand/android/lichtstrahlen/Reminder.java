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

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.escoand.android.lichtstrahlen_2013.R;

public class Reminder extends BroadcastReceiver {
	Intent notification = null;
	PendingIntent receiver = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		/* set info */
		notification = new Intent(context, Notification.class);
		notification.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notification.putExtra("icon", R.drawable.icon);
		notification.putExtra("info", context.getString(R.string.app_name)
				+ " - " + context.getString(R.string.msgRemind));
		notification.putExtra("title", context.getString(R.string.app_name));
		notification.putExtra("message", context.getString(R.string.msgRemind));

		/* receiver */
		receiver = PendingIntent.getBroadcast(context, 0, notification,
				PendingIntent.FLAG_UPDATE_CURRENT);

		/* start notification */
		if (prefs.getBoolean("remind", false)) {
			int hour = prefs.getInt("remind_hour", 9);
			int minute = prefs.getInt("remind_minute", 0);

			/* get date */
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			if (cal.before(Calendar.getInstance()))
				cal.add(Calendar.DAY_OF_YEAR, 1);

			/* schedule */
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
					.setRepeating(AlarmManager.RTC_WAKEUP,
							cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
							receiver);
		}

		/* stop notification */
		else
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
					.cancel(receiver);
	}
}
