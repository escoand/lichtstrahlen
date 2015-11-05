/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.escoand.android.lichtstrahlen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.escoand.android.lichtstrahlen.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        RemoteViews views = null;
        TextDatabase db = new TextDatabase(context);
        Cursor cursor = db.getDate(new Date());

		/* themes */
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                "widgetInverse", true))
            views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_light);
        else
            views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_dark);

		/* content */
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToLast();
            views.setTextViewText(R.id.widgetVerse, cursor.getString(cursor
                    .getColumnIndex(TextDatabase.COLUMN_VERSE)));
            views.setTextViewText(R.id.widgetTitle, cursor.getString(cursor
                    .getColumnIndex(TextDatabase.COLUMN_TITLE)));

			/* close */
            try {
                cursor.close();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

		/* no data */
        else {
            views.setTextViewText(R.id.widgetVerse,
                    context.getString(R.string.widgetNoData));
            views.setTextViewText(R.id.widgetTitle,
                    context.getString(R.string.widgetNoData2));
        }

		/* date */
        views.setTextViewText(R.id.widgetDay, new SimpleDateFormat("dd.",
                Locale.getDefault()).format(new Date()));
        views.setTextViewText(R.id.widgetMonth, new SimpleDateFormat("MMM",
                Locale.getDefault()).format(new Date()));

		/* onclick listener */
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

		/* preference button */
        intent = new Intent(context, Preferences.class);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widgetPreferences, pendingIntent);

		/* update */
        appWidgetManager.updateAppWidget(appWidgetIds, views);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
