package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;

import com.escoand.android.lichtstrahlen_2012.R;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.ViewFlipper;

public class AsyncVerse extends AsyncTask<Void, Void, HashMap<String, String>> {
	private final SimpleDateFormat yearmonth = new SimpleDateFormat("yyyyMM");
	private MainActivity parent = null;
	private ViewFlipper flipper = null;

	public AsyncVerse(MainActivity ls) {
		super();
		parent = ls;
		flipper = (ViewFlipper) parent.findViewById(R.id.flipper);
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
		String datestring = new SimpleDateFormat("yyyyMMdd")
				.format(parent.date);
		XmlPullParser xml = null;
		int id = 0;
		String dateread = "";
		HashMap<String, String> result = new HashMap<String, String>();

		/* get xml file */
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			parent.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			id = parent.getResources().getIdentifier(
					"data_" + yearmonth.format(parent.date), "xml",
					parent.getPackageName());

			/* read xml file */
			if (id != 0) {
				xml = parent.getResources().getXml(id);
				while (xml.getEventType() != XmlPullParser.END_DOCUMENT) {
					if (xml.getEventType() == XmlPullParser.START_TAG) {

						/* entry tag */
						if (xml.getName().equals("entry")) {
							dateread = xml.getAttributeValue(null, "date");
						} else if (dateread.equals(datestring)) {

							/* bible tag */
							if (xml.getName().equals("verse")) {
								xml.next();
								result.put("verse", xml.getText());
							}

							/* header tag */
							else if (xml.getName().equals("header")) {
								xml.next();
								result.put("header", xml.getText());
							}

							/* text tag */
							else if (xml.getName().equals("text")) {
								xml.next();
								result.put("text", xml.getText());
							}
							/* author tag */
							else if (xml.getName().equals("author")) {
								xml.next();
								result.put("author", xml.getText());
							}

							/* weektext tag */
							else if (xml.getName().equals("weektext")) {
								xml.next();
								if (xml.getEventType() == XmlPullParser.TEXT)
									result.put("weektext", xml.getText());
							}

							/* weekverse tag */
							else if (xml.getName().equals("weekverse")) {
								xml.next();
								if (xml.getEventType() == XmlPullParser.TEXT)
									result.put("weekverse", xml.getText());
							}

							/* monthtext tag */
							else if (xml.getName().equals("monthtext")) {
								xml.next();
								if (xml.getEventType() == XmlPullParser.TEXT)
									result.put("monthtext", xml.getText());
							}

							/* monthverse tag */
							else if (xml.getName().equals("monthverse")) {
								xml.next();
								if (xml.getEventType() == XmlPullParser.TEXT)
									result.put("monthverse", xml.getText());
							}
						}
					}
					xml.next();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
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
		if (result.containsKey("monthtext")) {
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
		if (result.containsKey("weektext")) {
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
		if (result.containsKey("text")) {
			content += "<p>";
			content += "<div style=\"float: right; text-align: right; white-space: nowrap;\">"
					+ TextUtils.htmlEncode(result.get("verse")).replace("%",
							"&#37;") + "</div>";
			content += "<div style=\"font-weight: bold;\">"
					+ TextUtils.htmlEncode(result.get("header")).replace("%",
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
		view.loadData(content, "text/html", "utf-8");

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