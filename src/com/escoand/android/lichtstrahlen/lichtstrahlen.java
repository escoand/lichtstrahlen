package com.escoand.android.lichtstrahlen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class lichtstrahlen extends Activity {
	static final int DIALOG_ABOUT_ID = 0;
	static final int DIALOG_DATE_ID = 1;
	
	private Date date = new Date();
	private final DateFormat dateformat = DateFormat.getDateInstance();
	private CharSequence[] verses = null;
	private Notes notes = null;

    public ProgressDialog progress;
    private AlertDialog selection;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // init elements
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.wait));
        
        // read initially
        notes = new Notes(this);
		new VerseTask().execute();
		
		// callback for save note
		findViewById(R.id.noteSave).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notes.add(date, ((TextView) findViewById(R.id.noteText)).getText().toString());
			}
		});
		
		// callback for delete note
		findViewById(R.id.noteDelete).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notes.remove(date);
			}
		});
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
			if(verses.length == 1) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bibleserver.com/text/LUT/" + verses[0])));
				return true;
			}
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
			return false;
		case R.id.menuToday:
			date = new Date();
			new VerseTask().execute();
			return true;
		case R.id.menuDate:
			showDialog(DIALOG_DATE_ID);
			return true;
		case R.id.menuNotes:
			new NotesListTask().execute();
			return true;
		case R.id.menuList:
			new VerseListTask().execute();
			return true;
		case R.id.menuInfo:
			showDialog(DIALOG_ABOUT_ID);
			return true;
		default:
			return false;
		}
	}
    
	
	// callback for creating dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = new Dialog(this);
		switch(id) {
			case DIALOG_DATE_ID:
				return new DatePickerDialog(this, datepickerlistener, date.getYear() + 1900, date.getMonth(), date.getDate());
			case DIALOG_ABOUT_ID:
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.about);

				try {
					((TextView) dialog.findViewById(R.id.txtVersion)).setText("Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return dialog;
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
		    new VerseTask().execute();
		}
	};
	
	
	// task for reading one day
	private class VerseTask extends AsyncTask<Void, Void, HashMap<String, String>> {
		private final SimpleDateFormat yearmonth = new SimpleDateFormat("yyyyMM");
		private final String datestring = new SimpleDateFormat("yyyyMMdd").format(date);

		@Override
		protected void onPreExecute() {
			// show progress dialog
			progress.show();
		}

		@Override
		protected HashMap<String, String> doInBackground(Void... params) {
			XmlPullParser xml = null;
			int id;
			String dateread = ""; 
			HashMap<String, String> result = new HashMap<String, String>();
			
			// get xml file
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			id = new Resources(getAssets(), metrics, null).getIdentifier("data_" + yearmonth.format(date), "xml", getPackageName());

			// read xml file
			if(id != 0) {
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
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(HashMap<String, String> result) {
			Vector<CharSequence> versesTemp = new Vector<CharSequence>();
			
			setTitle(getString(R.string.app_name) + " " + getString(R.string.textFor) + " " + dateformat.format(date));
			
			// month
			if(result.containsKey("monthtext")) {
				findViewById(R.id.month).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.monthtext)).setText(result.get("monthtext"));
				((TextView) findViewById(R.id.monthverse)).setText(result.get("monthverse"));
				versesTemp.add(result.get("monthverse"));
			}
			else
				findViewById(R.id.month).setVisibility(View.GONE);
			
			// week
			if(result.containsKey("weektext")) {
				findViewById(R.id.week).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.weektext)).setText(result.get("weektext"));
				((TextView) findViewById(R.id.weekverse)).setText(result.get("weekverse"));
				versesTemp.add(result.get("weekverse"));
			}
			else
				findViewById(R.id.week).setVisibility(View.GONE);
			
			// day
			if(result.containsKey("text")) {
				((TextView) findViewById(R.id.verse)).setText(result.get("verse"));
				((TextView) findViewById(R.id.headline)).setText(result.get("header"));
				((TextView) findViewById(R.id.text)).setText(result.get("text"));
				((TextView) findViewById(R.id.author)).setText(result.get("author"));
				versesTemp.add(result.get("verse"));
				findViewById(R.id.note).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.noteText)).setText(notes.get(date));
			}
			else {
				((TextView) findViewById(R.id.verse)).setText(null);
				((TextView) findViewById(R.id.headline)).setText(null);
				((TextView) findViewById(R.id.text)).setText(getString(R.string.noText));
				((TextView) findViewById(R.id.author)).setText(null);
				findViewById(R.id.note).setVisibility(View.GONE);
			}
			
			// set verses array
			verses = versesTemp.toArray(new CharSequence[versesTemp.size()]);
			
    		// hide progress dialog
    		progress.dismiss();
		}
	}
	

	// read verse list in background
	private class NotesListTask extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
		@Override
		protected void onPreExecute() {
			// show progress dialog
			progress.show();
		}
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
			final SimpleDateFormat yearmonth = new SimpleDateFormat("yyyyMM");
			final SimpleDateFormat yearmonthday = new SimpleDateFormat("yyyyMMdd");
			Date curdate = (Date) date.clone();
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
			XmlPullParser xml = null;
			String dateread = "";

			// run 2010 till 2015
			for(int year = 2010; year <= 2015; year++) {
				curdate.setYear(year - 1900);
				
				// run all months
				for(int month = 1; month <= 12; month++) {
					curdate.setMonth(month - 1);
					
					// get xml file
					DisplayMetrics metrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int id = new Resources(getAssets(), metrics, null).getIdentifier("data_" + yearmonth.format(curdate), "xml", getPackageName());
					
					// read xml file
					if(id != 0) {
						xml = getResources().getXml(id);
						try {
							while(xml.getEventType() != XmlPullParser.END_DOCUMENT) {
								switch(xml.getEventType()) {
									case XmlPullParser.START_TAG:
										// read date of entry
										if(xml.getName().equals("entry"))
											dateread = xml.getAttributeValue(null, "date");
										
										// read verse if all or note for day exists
										else if(xml.getName().equals("verse") && notes.exist(yearmonthday.parse(dateread))) {
											xml.next();
											HashMap<String, String> item = new HashMap<String, String>();
											item.put("date", dateformat.format(yearmonthday.parse(dateread)));
											item.put("verse", xml.getText());
											item.put("note", notes.get(yearmonthday.parse(dateread)));
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
					}
				}
			}
			
			return list;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			final AlertDialog.Builder adb = new AlertDialog.Builder(lichtstrahlen.this);
			
			// create list dialog
			adb.setCancelable(true);
			adb.setTitle(getString(R.string.menuNotes));
			adb.setAdapter(
				new SimpleAdapter(
					lichtstrahlen.this,
					result,
					R.layout.noteslist,
					new String[] {"verse", "date", "note"},
					new int[] {R.id.listVerse, R.id.listDate, R.id.listNote}),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						try {
							HashMap<String, String> element = (HashMap<String, String>) selection.getListView().getItemAtPosition(item);
							date = dateformat.parse(element.get("date"));
							new VerseTask().execute();
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
			});
			
			selection = adb.create();
			selection.show();
			
    		// hide progress dialog
    		progress.dismiss();
		}
	}
	
	
	// read verse list in background
	private class VerseListTask extends AsyncTask<Boolean, Void, ArrayList<HashMap<String, String>>> {
		@Override
		protected void onPreExecute() {
			// show progress dialog
			progress.show();
		}
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(Boolean... params) {
			final SimpleDateFormat format_year = new SimpleDateFormat("yyyy");
			final SimpleDateFormat format_yearmonthday = new SimpleDateFormat("yyyyMMdd");
			Date curdate = (Date) date.clone();
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
			XmlPullParser xml = null;
			
			Date dateread = new Date(0);
			Date dateuntil = new Date(0);

			// run 2010 till 2015
			for(int year = 2010; year <= 2015; year++) {
				curdate.setYear(year - 1900);
					
				// get xml file
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				int id = new Resources(getAssets(), metrics, null).getIdentifier("list_" + format_year.format(curdate), "xml", getPackageName());
				
				// read xml file
				if(id != 0) {
					xml = getResources().getXml(id);
					try {
						while(xml.getEventType() != XmlPullParser.END_DOCUMENT) {
							switch(xml.getEventType()) {
								case XmlPullParser.START_TAG:
									// read date of entry
									if(xml.getName().equals("entry")) {
										dateread = format_yearmonthday.parse(xml.getAttributeValue(null, "date"));
										if(xml.getAttributeValue(null, "until") != null)
											dateuntil = format_yearmonthday.parse(xml.getAttributeValue(null, "until"));
										else
											dateuntil = dateread;
									}
									
									// read verse if all or note for day exists
									else if(xml.getName().equals("verse")) {
										xml.next();
										HashMap<String, String> item = new HashMap<String, String>();
										item.put("date", dateformat.format(dateread));
										item.put("until", dateformat.format(dateuntil));
										item.put("verse", xml.getText());
										if(notes.exist(dateread))
											item.put("note", notes.get(dateread));
										list.add(item);
									}
									break;
								case XmlPullParser.END_TAG:
									if(xml.getName().equals("entry"))
										dateread = new Date(0);
									break;
							}
							xml.next();
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			return list;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			final AlertDialog.Builder adb = new AlertDialog.Builder(lichtstrahlen.this);
			
			// create list dialog
			adb.setCancelable(true);
			adb.setTitle(getString(R.string.menuList));
			adb.setAdapter(
				new VerseListAdapter(
					lichtstrahlen.this,
					result,
					R.layout.list,
					new String[] {"verse", "date"},
					new int[] {R.id.listVerse, R.id.listDate}),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						try {
							HashMap<String, String> element = (HashMap<String, String>) selection.getListView().getItemAtPosition(item);
							date = dateformat.parse(element.get("date"));
							new VerseTask().execute();
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
			});
			
			selection = adb.create();
			selection.show();
			
    		// hide progress dialog
    		progress.dismiss();
		}
	}
}
