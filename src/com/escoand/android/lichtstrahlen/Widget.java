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

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.escoand.android.lichtstrahlen_2013.R;

public class Widget extends AppWidgetProvider {

	@SuppressLint("NewApi")
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		TextDatabase db = new TextDatabase(context);
		Cursor cursor = db.getDate(new Date());

		/* content */
		views.setTextViewText(R.id.widgetTitle, cursor.getString(cursor
				.getColumnIndex(TextDatabase.COLUMN_TITLE)));
		views.setTextViewText(R.id.widgetVerse, cursor.getString(cursor
				.getColumnIndex(TextDatabase.COLUMN_VERSE)));
		views.setTextViewText(R.id.widgetDay,
				new SimpleDateFormat("dd.").format(new Date()));
		views.setTextViewText(R.id.widgetMonth,
				new SimpleDateFormat("MMM").format(new Date()));

		// TODO: background image

		/* close */
		try {
			cursor.close();
			db.close();
		} catch (Exception e) {
		}

		/* onclick listener */
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.widget, pendingIntent);

		/* update */
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}

