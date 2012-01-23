package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import com.escoand.android.lichtstrahlen_2012.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

public class AsyncVerseList extends
		AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
	private MainActivity parent = null;

	public AsyncVerseList(MainActivity ls) {
		super();
		parent = ls;
	}

	@Override
	protected void onPreExecute() {
		// show progress dialog
		parent.progress.show();
	}

	@Override
	protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
		final SimpleDateFormat format_year = new SimpleDateFormat("yyyy");
		final SimpleDateFormat format_yearmonthday = new SimpleDateFormat(
				"yyyyMMdd");
		Date curdate = (Date) parent.date.clone();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		XmlPullParser xml = null;

		Date dateread = new Date(0);
		Date dateuntil = new Date(0);

		// run 2010 till 2015
		for (int year = 2010; year <= 2015; year++) {
			curdate.setYear(year - 1900);

			// get xml file
			try {
				DisplayMetrics metrics = new DisplayMetrics();
				parent.getWindowManager().getDefaultDisplay()
						.getMetrics(metrics);
				int id = parent.getResources().getIdentifier(
						"list_" + format_year.format(curdate), "xml",
						parent.getPackageName());

				// read xml file
				if (id != 0) {
					xml = parent.getResources().getXml(R.xml.list_2012);
					while (xml.getEventType() != XmlPullParser.END_DOCUMENT) {
						switch (xml.getEventType()) {
						case XmlPullParser.START_TAG:
							// read date of entry
							if (xml.getName().equals("entry")) {
								dateread = format_yearmonthday.parse(xml
										.getAttributeValue(null, "date"));
								if (xml.getAttributeValue(null, "until") != null)
									dateuntil = format_yearmonthday.parse(xml
											.getAttributeValue(null, "until"));
								else
									dateuntil = dateread;
							}

							// read verse if all or note for day exists
							else if (xml.getName().equals("verse")) {
								xml.next();
								HashMap<String, String> item = new HashMap<String, String>();
								item.put("date", DateFormat.getDateInstance().format(dateread));
								item.put("until", DateFormat.getDateInstance().format(dateuntil));
								item.put("verse", xml.getText());
								if (parent.notes.exist(dateread))
									item.put("note", parent.notes.get(dateread));
								list.add(item);
							}
							break;
						case XmlPullParser.END_TAG:
							if (xml.getName().equals("entry"))
								dateread = new Date(0);
							break;
						}
						xml.next();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	@Override
	protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
		// create list dialog
		parent.selection = new AlertDialog.Builder(parent)
				.setCancelable(true)
				.setTitle(parent.getString(R.string.menuList))
				.setAdapter(
						new VerseListAdapter(parent.getApplicationContext(),
								result, R.layout.list, new String[] { "verse",
										"date" }, new int[] { R.id.listVerse,
										R.id.listDate }),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								try {
									@SuppressWarnings("unchecked")
									HashMap<String, String> element = (HashMap<String, String>) parent.selection
											.getListView()
											.getItemAtPosition(id);
									parent.date = DateFormat.getDateInstance().parse(element
											.get("date"));
									new AsyncVerse(parent).execute();
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}).create();
		parent.selection.show();

		// hide progress dialog
		parent.progress.dismiss();
	}
}