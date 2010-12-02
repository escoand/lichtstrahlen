package com.android.lichtstrahlen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.app.ListActivity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.SimpleAdapter;

public class VerseList extends ListActivity {
	SimpleAdapter adapter;
	ArrayList<HashMap<String, String>> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setListAdapter(new SimpleAdapter( 
			this, 
			list,
			R.layout.list,
			new String[] {"verse", "date"},
			new int[] {R.id.listVerse, R.id.listDate}));
		
		getListView().setTextFilterEnabled(true);
		
		loadContent(new Date("2011-01-01"));
	}
	
	public void loadContent(Date date) {
		// load content
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
		XmlPullParser xml = null;
		int id;
		String dateread = ""; 
		
		// open xml file
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		id = new Resources(getAssets(), metrics, null).getIdentifier("data_" + new SimpleDateFormat("yyyyMM").format(date), "xml", "com.android.lichtstrahlen");
		metrics = null;
		if(id == 0)
			id = R.xml.data;
		xml = getResources().getXml(id);
		
		try {
			while(xml.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch(xml.getEventType()) {
					case XmlPullParser.START_TAG:
						if(xml.getName().equals("entry"))
							dateread = xml.getAttributeValue(null, "date");
						else if(xml.getName().equals("verse")) {
							xml.next();
							HashMap<String, String> item = new HashMap<String, String>();
							item.put("date", dateread);
							item.put("verse", xml.getText());
							list.add(item);
						}
						break;
					case XmlPullParser.END_TAG:
						if(xml.getName().equals("entry"))
							dateread = "";
						break;
				}
				xml.next();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
