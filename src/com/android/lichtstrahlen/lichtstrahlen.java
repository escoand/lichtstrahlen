package com.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class lichtstrahlen extends Activity {
	private Date date = new Date();
	private CharSequence[] verses = null;

	private LinearLayout frmMonth;
	private TextView txtMonthText;
	private TextView txtMonthVerse;
	private LinearLayout frmWeek;
	private TextView txtWeekText;
	private TextView txtWeekVerse;
	private TextView txtVerse;
	private TextView txtHeader;
	private TextView txtText;
	private TextView txtAuthor;
	
	private CharSequence[] lstVerses;

    private ProgressDialog progress;
    private AlertDialog alert;
    private AlertDialog selection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // init elements
        frmMonth = (LinearLayout) findViewById(R.id.month);
        txtMonthText = (TextView) findViewById(R.id.monthtext);
        txtMonthVerse = (TextView) findViewById(R.id.monthverse);
        frmWeek = (LinearLayout) findViewById(R.id.week);
        txtWeekText = (TextView) findViewById(R.id.weektext);
        txtWeekVerse = (TextView) findViewById(R.id.weekverse);
        txtVerse = (TextView) findViewById(R.id.verse);
        txtHeader = (TextView) findViewById(R.id.headline);
        txtText = (TextView) findViewById(R.id.text);
        txtAuthor = (TextView) findViewById(R.id.author);
        
        // init dialogs
        alert = new AlertDialog.Builder(this).create();
		alert.setCancelable(false);
		alert.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
        
        // read today
		if(txtText.getText().equals(""))
			new ReadingTask().execute();
	}

	
	// callback for configuration changed (e.g. orientation)
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	

	// callback for showing option menu
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
    

    // callback for clicking option item
	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menuBible:
			if(verses.length == 1)
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bibleserver.com/text/LUT/" + verses[0])));
			else if(verses.length > 1) {
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setCancelable(true);
				adb.setTitle(getString(R.string.select));
				adb.setItems(verses, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bibleserver.com/text/LUT/" + verses[item])));
					}
				});
				selection = adb.create();
				selection.show();
				return true;
			}
			else
				return false;
		case R.id.menuToday:
			date = new Date();
			new ReadingTask().execute();
			return true;
		case R.id.menuDate:
			showDialog(0);
			return true;
		case R.id.menuNotes:
			return true;
		case R.id.menuList:
			new VerseListTask().execute();
			return true;
		case R.id.menuInfo:
			String version = "";
			try {
				version = " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) { }
			alert.setTitle(getString(R.string.app_name) + version);
			alert.setMessage(getString(R.string.aboutMessage));
			alert.show();
			return true;
		default:
			return false;
		}
	}
    
	
	// callback for creating dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		Date date = new Date();
		switch(id) {
		case 0:
			return new DatePickerDialog(this, datepickerlistener, date.getYear() + 1900, date.getMonth(), date.getDate());
		}
		return null;
	}
    
    // callback for setting date
    private final DatePickerDialog.OnDateSetListener datepickerlistener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		    date.setYear(year - 1900);
		    date.setMonth(monthOfYear);
		    date.setDate(dayOfMonth);
		    new ReadingTask().execute();
		}
	};
	
	
	// task for reading one day
	private class ReadingTask extends AsyncTask<Object, Object, HashMap<String, String>> {

		@Override
		protected void onPreExecute() {
			// show progress dialog
			progress = ProgressDialog.show(lichtstrahlen.this, null, getString(R.string.wait), true, false);
		}

		@Override
		protected HashMap<String, String> doInBackground(Object... params) {
			final String datestring = new SimpleDateFormat("yyyyMMdd").format(date);
			XmlPullParser xml = null;
			int id;
			String dateread = ""; 
			HashMap<String, String> result = new HashMap<String, String>();
			
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
					if(xml.getEventType() == XmlPullParser.START_TAG) {
						// entry tag
						if(xml.getName().equals("entry")) {
							dateread = xml.getAttributeValue(null, "date");
						}
						else if(dateread.equals(datestring)) {
							// bible tag
							if(xml.getName().equals("verse")) {
								xml.next();
								result.put("verse", xml.getText());
							}
							// header tag
							else if(xml.getName().equals("header")) {
								xml.next();
								result.put("header", xml.getText());
							}
							// text tag
							else if(xml.getName().equals("text")) {
								xml.next();
								result.put("text", xml.getText());
							}
							// author tag
							else if(xml.getName().equals("author")) {
								xml.next();
								result.put("author", xml.getText());
							}
							// weektext tag
							else if(xml.getName().equals("weektext")) {
								xml.next();
								result.put("weektext", xml.getText());
							}
							// weekverse tag
							else if(xml.getName().equals("weekverse")) {
								xml.next();
								result.put("weekverse", xml.getText());
							}
							// monthtext tag
							else if(xml.getName().equals("monthtext")) {
								xml.next();
								result.put("monthtext", xml.getText());
							}
							// monthverse tag
							else if(xml.getName().equals("monthverse")) {
								xml.next();
								result.put("monthverse", xml.getText());
							}
						}
					}
					xml.next();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(HashMap<String, String> result) {
			Vector<CharSequence> versesTemp = new Vector<CharSequence>();
			
			setTitle(getString(R.string.app_name) + " für " + DateFormat.getDateInstance().format(date));
			
			// month
			if(result.containsKey("monthtext")) {
				frmMonth.setVisibility(View.VISIBLE);
				txtMonthText.setText(result.get("monthtext"));
				txtMonthVerse.setText(result.get("monthverse"));
				versesTemp.add(result.get("monthverse"));
			}
			else
				frmMonth.setVisibility(View.GONE);
			
			// week
			if(result.containsKey("weektext")) {
				frmWeek.setVisibility(View.VISIBLE);
				txtWeekText.setText(result.get("weektext"));
				txtWeekVerse.setText(result.get("weekverse"));
				versesTemp.add(result.get("weekverse"));
			}
			else
				frmWeek.setVisibility(View.GONE);
			
			// day
			if(result.containsKey("text")) {
				txtVerse.setText(result.get("verse"));
				txtHeader.setText(result.get("header"));
				txtText.setText(result.get("text"));
				txtAuthor.setText(result.get("author"));
				versesTemp.add(result.get("verse"));
			}
			else {
				txtVerse.setText(null);
				txtHeader.setText(null);
				txtText.setText("kein Text gefunden");
				txtAuthor.setText(null);
			}
			
			// set verses array
			verses = versesTemp.toArray(new CharSequence[versesTemp.size()]);
			
    		// hide progress dialog
    		progress.dismiss();
		}
	}
		
	private class VerseListTask extends AsyncTask<Object, Object, ArrayList<HashMap<String, String>>> {

		@Override
		protected void onPreExecute() {
			// show progress dialog
			progress = ProgressDialog.show(lichtstrahlen.this, null, getString(R.string.wait), true, false);
		}
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(Object... params) {
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
								dateread = "";;
							break;
					}
					xml.next();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return list;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			AlertDialog.Builder adb = new AlertDialog.Builder(lichtstrahlen.this);
			adb.setCancelable(true);
			adb.setTitle(getString(R.string.select));
			adb.setAdapter(
				new SimpleAdapter(lichtstrahlen.this, result,R.layout.verselist, new String[] {"verse", "date"}, new int[] {R.id.listVerse, R.id.listDate}),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bibleserver.com/text/LUT/" + verses[item])));
					}
			});
			selection = adb.create();
			selection.show();
			
    		// hide progress dialog
    		progress.dismiss();
		}
	}
}
