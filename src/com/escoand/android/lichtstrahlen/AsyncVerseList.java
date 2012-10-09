package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.escoand.android.lichtstrahlen_2013.R;

public class AsyncVerseList extends
		AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
	private MainActivity parent = null;
	private VerseDatabaseHelper dbh = null;

	public AsyncVerseList(MainActivity ls) {
		super();
		parent = ls;
		dbh = new VerseDatabaseHelper(parent.getApplicationContext());
	}

	@Override
	protected void onPreExecute() {
		parent.progress.show();
	}

	@Override
	protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
		return dbh.getListHashMap();
	}

	@Override
	protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
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
									parent.date = DateFormat.getDateInstance()
											.parse(element.get("date"));
									new AsyncVerse(parent).execute();
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}).create();
		parent.selection.show();

		parent.progress.dismiss();
	}
}