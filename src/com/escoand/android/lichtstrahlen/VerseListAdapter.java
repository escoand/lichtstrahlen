package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class VerseListAdapter extends SimpleAdapter {
	ArrayList<? extends Map<String, ?>> data;
	
	public VerseListAdapter(Context context, ArrayList<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView txt = (TextView) view.findViewById(R.id.listDate);
		
		// get date
		Date date = new Date(0);
		Date until = new Date(0);

		try {
			date = DateFormat.getDateInstance().parse(data.get(position).get("date").toString());
			until = DateFormat.getDateInstance().parse(data.get(position).get("until").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// set text
		if(date.equals(until))
			txt.setText(new SimpleDateFormat("dd.MM.").format(date));
		else if(date.getYear() == until.getYear() && date.getMonth() == until.getMonth())
			txt.setText(new SimpleDateFormat("dd.").format(date) + " bis " + new SimpleDateFormat("dd.MM.").format(until));
		else if(date.getYear() == until.getYear())
			txt.setText(new SimpleDateFormat("dd.MM.").format(date) + " bis " + new SimpleDateFormat("dd.MM.").format(until));
		else
			txt.setText(new SimpleDateFormat("dd.MM.yyyy").format(date) + " bis " + new SimpleDateFormat("dd.MM.yyyy").format(until));
		
		return view;
	}
}
