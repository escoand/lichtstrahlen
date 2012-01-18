package com.escoand.android.lichtstrahlen_2012;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AsyncNotes extends
		AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
	private Lichtstrahlen parent = null;

	public AsyncNotes(Lichtstrahlen ls) {
		super();
		parent = ls;
	}

	@Override
	protected void onPreExecute() {
		/* show progress dialog */
		parent.progress.show();
	}

	@Override
	protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
		final SimpleDateFormat yearmonth = new SimpleDateFormat("yyyyMM");
		final SimpleDateFormat yearmonthday = new SimpleDateFormat("yyyyMMdd");
		Date curdate = (Date) parent.date.clone();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		XmlPullParser xml = null;
		String dateread = "";

		try {

			// run 2010 till 2015
			for (int year = 2010; year <= 2015; year++) {
				curdate.setYear(year - 1900);

				// run all months
				for (int month = 1; month <= 12; month++) {
					curdate.setMonth(month - 1);

					// get xml file
					DisplayMetrics metrics = new DisplayMetrics();
					parent.getWindowManager().getDefaultDisplay()
							.getMetrics(metrics);
					int id = parent.getResources().getIdentifier(
							"data_" + yearmonth.format(curdate), "xml",
							parent.getPackageName());

					// read xml file
					if (id != 0) {
						xml = parent.getResources().getXml(id);
						while (xml.getEventType() != XmlPullParser.END_DOCUMENT) {
							switch (xml.getEventType()) {
							case XmlPullParser.START_TAG:
								// read date of entry
								if (xml.getName().equals("entry"))
									dateread = xml.getAttributeValue(null,
											"date");

								// read verse if all or note for day exists
								// else
								if (xml.getName().equals("verse")
										&& parent.notes.exist(yearmonthday
												.parse(dateread))) {
									xml.next();
									HashMap<String, String> item = new HashMap<String, String>();
									item.put(
											"date",
											DateFormat.getDateInstance()
													.format(yearmonthday
															.parse(dateread)));
									item.put("verse", xml.getText());
									item.put("note", parent.notes
											.get(yearmonthday.parse(dateread)));
									list.add(item);
								}
								break;
							case XmlPullParser.END_TAG:
								if (xml.getName().equals("entry"))
									dateread = "";
								;
								break;
							}
							xml.next();
						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	@Override
	protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
		
		/* create list dialog */
		parent.selection = new AlertDialog.Builder(parent)
				.setCancelable(true)
				.setTitle(parent.getString(R.string.listNotes))
				.setAdapter(
						new SimpleAdapter(parent.getApplicationContext(),
								result, R.layout.noteslist, new String[] {
										"verse", "date", "note" }, new int[] {
										R.id.listVerse, R.id.listDate,
										R.id.listNote }),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								
								/* show note edit dialog */
								try {
									if (id == 0)
										parent.showDialog(Lichtstrahlen.DIALOG_NOTE_ID);

									/* go to date */
									else {
										@SuppressWarnings("unchecked")
										HashMap<String, String> element = (HashMap<String, String>) parent.selection
												.getListView()
												.getItemAtPosition(id);
										parent.date = DateFormat
												.getDateInstance().parse(
														element.get("date"));
										new AsyncVerse(parent).execute();
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}).create();

		/* add header */
		TextView addNote = new TextView(parent.getApplicationContext());
		addNote.setText(parent.getString(R.string.noteAdd));
		addNote.setPadding(5, 10, 5, 10);
		parent.selection.getListView().addHeaderView(addNote);
		parent.selection.show();

		/* hide progress dialog */
		parent.progress.dismiss();
	}
}
