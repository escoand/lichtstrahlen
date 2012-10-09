package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.ViewFlipper;

import com.escoand.android.lichtstrahlen_2013.R;

public class AsyncVerse extends AsyncTask<Void, Void, HashMap<String, String>> {
	private MainActivity parent = null;
	private ViewFlipper flipper = null;
	private VerseDatabaseHelper dbh = null;

	public AsyncVerse(MainActivity ls) {
		super();
		parent = ls;
		flipper = (ViewFlipper) parent.findViewById(R.id.flipper);
		dbh = new VerseDatabaseHelper(parent.getApplicationContext());
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (flipper.getChildCount() > 0) {
			int scale = (int) (((WebView) flipper.getCurrentView()).getScale() * 100);
			parent.getSharedPreferences(parent.getString(R.string.app_name),
					Context.MODE_PRIVATE).edit().putInt("scale", scale)
					.commit();
		}
	}

	@Override
	protected HashMap<String, String> doInBackground(Void... params) {
		return dbh.getDateHashMap(parent.date);
	}

	@Override
	protected void onPostExecute(HashMap<String, String> result) {
		WebView view = new WebView(parent);
		String content = "";
		Vector<CharSequence> versesTemp = new Vector<CharSequence>();

		parent.setTitle(parent.getString(R.string.app_name) + " "
				+ parent.getString(R.string.textFor) + " "
				+ DateFormat.getDateInstance().format(parent.date));

		/* month */
		if (result.containsKey("monthtext") && result.containsKey("monthverse")
				&& result.get("monthtext") != null
				&& result.get("monthverse") != null) {
			content += "<p>";
			content += "<div style=\"font-weight: bold;\">"
					+ TextUtils
							.htmlEncode(parent.getString(R.string.mainMonth))
							.replace("%", "&#37;") + "</div>";
			content += "<div>"
					+ TextUtils.htmlEncode(result.get("monthtext")).replace(
							"%", "&#37;") + "</div>";
			content += "<div style=\"text-align: right; white-space: nowrap;\">"
					+ TextUtils.htmlEncode(result.get("monthverse")).replace(
							"%", "&#37;") + "</div>";
			content += "</p>";
			versesTemp.add(result.get("monthverse"));
		}

		/* week */
		if (result.containsKey("weektext") && result.containsKey("weekverse")
				&& result.get("weektext") != null
				&& result.get("weekverse") != null) {
			content += "<p>";
			content += "<div style=\"font-weight: bold;\">"
					+ TextUtils.htmlEncode(parent.getString(R.string.mainWeek))
							.replace("%", "&#37;") + "</div>";
			content += "<div>"
					+ TextUtils.htmlEncode(result.get("weektext")).replace("%",
							"&#37;") + "</div>";
			content += "<div style=\"text-align: right; white-space: nowrap;\">"
					+ TextUtils.htmlEncode(result.get("weekverse")).replace(
							"%", "&#37;") + "</div>";
			content += "</p>";
			versesTemp.add(result.get("weekverse"));
		}

		/* day */
		if (result.containsKey("verse") && result.containsKey("title")
				&& result.containsKey("text") && result.containsKey("author")) {
			content += "<p>";
			content += "<div style=\"float: right; text-align: right; white-space: nowrap;\">"
					+ TextUtils.htmlEncode(result.get("verse")).replace("%",
							"&#37;") + "</div>";
			content += "<div style=\"font-weight: bold;\">"
					+ TextUtils.htmlEncode(result.get("title")).replace("%",
							"&#37;") + "</div>";
			content += "<div style=\"clear: both;\">"
					+ TextUtils.htmlEncode(result.get("text")).replace("%",
							"&#37;") + "</div>";
			content += "<div style=\"text-align: right;\">"
					+ TextUtils.htmlEncode(result.get("author")).replace("%",
							"&#37;") + "</div>";
			content += "</p>";
			versesTemp.add(result.get("verse"));

			/* note */
			if (parent.notes.exist(parent.date)) {
				content += "<p>";
				content += "<div style=\"font-weight: bold;\">"
						+ TextUtils.htmlEncode(
								parent.getString(R.string.mainNote)).replace(
								"%", "&#37;") + "</div>";
				content += "<div>"
						+ TextUtils.htmlEncode(parent.notes.get(parent.date))
								.replace("%", "&#37;") + "</div>";
				content += "</p>";
			}
		} else {
			content = TextUtils.htmlEncode(
					parent.getString(R.string.mainNothing)).replace("%",
					"&#37;");
		}

		/* set content */
		view.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);

		/* scale */
		view.getSettings().setSupportZoom(true);
		view.getSettings().setBuiltInZoomControls(true);
		view.setInitialScale(parent.getSharedPreferences(
				parent.getString(R.string.app_name), Context.MODE_PRIVATE)
				.getInt("scale", 100));

		/* set verses array */
		parent.verses = versesTemp.toArray(new CharSequence[versesTemp.size()]);

		/* animation */
		flipper.addView(view);
		flipper.showNext();
		if (flipper.getChildCount() > 1)
			flipper.removeViewAt(0);
	}
}