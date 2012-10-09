package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.escoand.android.lichtstrahlen_2013.R;

public class VerseListAdapter extends SimpleAdapter {
	Context context;
	ArrayList<? extends Map<String, ?>> data;
	SimpleDateFormat df_ymd = (SimpleDateFormat) DateFormat
			.getDateInstance(DateFormat.SHORT);
	SimpleDateFormat df_ym = (SimpleDateFormat) DateFormat
			.getDateInstance(DateFormat.SHORT);

	public VerseListAdapter(Context context,
			ArrayList<? extends Map<String, ?>> data, int resource,
			String[] from, int[] to) {
		super(context, data, resource, from, to);
		this.context = context;
		this.data = data;

		/* date format */
		if (Locale.getDefault().getLanguage().equals("de"))
			df_ym.applyPattern("dd.MM.");
		else
			df_ym.applyPattern(df_ym.toPattern().replaceAll(
					"[^\\p{Alpha}]*y+[^\\p{Alpha}]*", ""));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView txt = (TextView) view.findViewById(R.id.listDate);

		/* get date */
		Date date = new Date(0);
		Date until = new Date(0);

		try {
			date = DateFormat.getDateInstance().parse(
					data.get(position).get("date").toString());
			until = DateFormat.getDateInstance().parse(
					data.get(position).get("until").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* set text */
		if (date.equals(until))
			txt.setText(df_ym.format(date));
		else if (date.getYear() == until.getYear()
				&& date.getMonth() == until.getMonth())
			txt.setText(df_ym.format(date) + " "
					+ context.getString(R.string.textUntil) + " "
					+ df_ym.format(until));
		else if (date.getYear() == until.getYear())
			txt.setText(df_ym.format(date) + " "
					+ context.getString(R.string.textUntil) + " "
					+ df_ym.format(until));
		else
			txt.setText(df_ymd.format(date) + " "
					+ context.getString(R.string.textUntil) + " "
					+ df_ymd.format(until));

		return view;
	}
}
