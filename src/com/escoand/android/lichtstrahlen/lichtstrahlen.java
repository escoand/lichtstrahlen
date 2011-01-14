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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class lichtstrahlen extends Activity {
	private static final int DIALOG_ABOUT_ID = 0;
	private static final int DIALOG_DATE_ID = 1;
	private static final int DIALOG_NOTE_ID = 2;
	private static final String BIBLE_URL = "http://www.bibleserver.com/text/";
	
	private Date date = new Date();
	private final DateFormat dateformat = DateFormat.getDateInstance();
	private CharSequence[] verses = null;
	private Notes notes = null;

    public ProgressDialog progress = null;
    private AlertDialog selection = null;
    
    private GestureDetector gesture = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// init
		((WebView) findViewById(R.id.content)).getSettings().setSupportZoom(true);
		((WebView) findViewById(R.id.content)).getSettings().setBuiltInZoomControls(true);
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.msgWait));
		gesture = new GestureDetector(new Gestures(this));
		notes = new Notes(this);
		new VerseTask().execute();
	}


	// callback for gestures
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(gesture.onTouchEvent(event))
			return true;
		return super.onTouchEvent(event);
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		super.dispatchTouchEvent(event);
		return onTouchEvent(event);
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
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BIBLE_URL + verses[0])));
				return true;
			}
			else if(verses.length > 1) {
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setCancelable(true);
				adb.setTitle(getString(R.string.listVerses));
				adb.setItems(verses, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BIBLE_URL + verses[item])));
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
				return new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						    date.setYear(year - 1900);
						    date.setMonth(monthOfYear);
						    date.setDate(dayOfMonth);
						    new VerseTask().execute();
						}
					},
					date.getYear() + 1900,
					date.getMonth(),
					date.getDate());
			case DIALOG_ABOUT_ID:
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.about);

				try {
					((TextView) dialog.findViewById(R.id.txtVersion)).setText("Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return dialog;
			case DIALOG_NOTE_ID:
				dialog.setTitle(getString(R.string.noteNote) + " " + getString(R.string.textFor) + " " + dateformat.format(date));
				dialog.setContentView(R.layout.noteedit);
				
				// load note
				if(notes.exist(date))
					((TextView) dialog.findViewById(R.id.noteText)).setText(notes.get(date));
				
				// callback for save note
				dialog.findViewById(R.id.noteSave).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// save note
						View parent = (View) v.getParent().getParent();
						notes.add(date, ((TextView) parent.findViewById(R.id.noteText)).getText().toString());
						
						// back to main
						lichtstrahlen.this.dismissDialog(DIALOG_NOTE_ID);
						new VerseTask().execute();
						Toast.makeText(getApplicationContext(), getString(R.string.noteSaved), Toast.LENGTH_LONG).show();
					}
				});
				
				// callback for delete note
				dialog.findViewById(R.id.noteDelete).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder adb = new AlertDialog.Builder(lichtstrahlen.this);
						adb.setMessage(getString(R.string.noteDeleteQuestion))
							.setCancelable(false)
							.setPositiveButton(getString(R.string.buttonYes), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									// delete note
									notes.remove(date);
									
									// back to main
									dialog.dismiss();
									lichtstrahlen.this.dismissDialog(DIALOG_NOTE_ID);
									new VerseTask().execute();
									Toast.makeText(getApplicationContext(), getString(R.string.noteDeleted), Toast.LENGTH_LONG).show();
								}
							})
							.setNegativeButton(getString(R.string.buttonNo), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							})
							.create()
							.show();
					}
				});
				
				// callback for cancel
				dialog.findViewById(R.id.noteCancel).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// back to main
						lichtstrahlen.this.dismissDialog(DIALOG_NOTE_ID);
					}
				});
				
				return dialog;
		}
		return null;
	}
	
	
	// go to next day
	public void nextDay() {
		date.setTime(date.getTime() + 24 * 60 * 60 * 1000);
		new VerseTask().execute();
	}
	
	// go to previous day
	public void prevDay() {
		date.setTime(date.getTime() - 24 * 60 * 60 * 1000);
		new VerseTask().execute();
	}
	
	
	// read verse in background
	public class VerseTask extends AsyncTask<Void, Void, HashMap<String, String>> {
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
			String content = "";
			
			setTitle(getString(R.string.app_name) + " " + getString(R.string.textFor) + " " + dateformat.format(date));
			
			// month
			if(result.containsKey("monthtext")) {
				content += "<p>";
				content += "<div style=\"font-weight: bold;\">" + TextUtils.htmlEncode(getString(R.string.mainMonth)) + "</div>";
				content += "<div>" + TextUtils.htmlEncode(result.get("monthtext")) + "</div>";
				content += "<div style=\"text-align: right; white-space: nowrap;\">" + TextUtils.htmlEncode(result.get("monthverse")) + "</div>";
				content += "</p>";
				versesTemp.add(result.get("monthverse"));
			}
			
			// week
			if(result.containsKey("weektext")) {
				content += "<p>";
				content += "<div style=\"font-weight: bold;\">" + TextUtils.htmlEncode(getString(R.string.mainWeek)) + "</div>";
				content += "<div>" + TextUtils.htmlEncode(result.get("weektext")) + "</div>";
				content += "<div style=\"text-align: right; white-space: nowrap;\">" + TextUtils.htmlEncode(result.get("weekverse")) + "</div>";
				content += "</p>";
				versesTemp.add(result.get("weekverse"));
			}
			
			// day
			if(result.containsKey("text")) {
				content += "<p>";
				content += "<div style=\"float: right; text-align: right; white-space: nowrap;\">" + TextUtils.htmlEncode(result.get("verse")) + "</div>";
				content += "<div style=\"font-weight: bold;\">" + TextUtils.htmlEncode(result.get("header")) + "</div>";
				content += "<div style=\"clear: both;\">" + TextUtils.htmlEncode(result.get("text")) + "</div>";
				content += "<div style=\"text-align: right;\">" + TextUtils.htmlEncode(result.get("author")) + "</div>";
				content += "</p>";
				versesTemp.add(result.get("verse"));
				
				// note
				if(notes.exist(date)) {
					content += "<p>";
					content += "<div style=\"font-weight: bold;\">" + TextUtils.htmlEncode(getString(R.string.mainNote)) + "</div>";
					content += "<div>" + TextUtils.htmlEncode(notes.get(date)) + "</div>";
					content += "</p>";
				}
			}
			else {
				content = getString(R.string.mainNothing);
			}
			
			// set content
			content = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" + content + "</body></html>";
			((WebView) findViewById(R.id.content)).loadData(content, "text/html", "utf-8");
			
			// scroll to top
			((WebView) findViewById(R.id.content)).scrollTo(0, 0);
			
			// set verses array
			verses = versesTemp.toArray(new CharSequence[versesTemp.size()]);
			
    		// hide progress dialog
    		progress.dismiss();
		}
	}
	

	// read notes list in background
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
			// create list dialog
			selection = new AlertDialog.Builder(lichtstrahlen.this)
				.setCancelable(true)
				.setTitle(getString(R.string.menuNotes))
				.setAdapter(
					new SimpleAdapter(
						lichtstrahlen.this,
						result,
						R.layout.noteslist,
						new String[] {"verse", "date", "note"},
						new int[] {R.id.listVerse, R.id.listDate, R.id.listNote}),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							try {
								// show note edit dialog
								if(id == 0)
									showDialog(DIALOG_NOTE_ID);
								
								// go to date
								else {
									HashMap<String, String> element = (HashMap<String, String>) selection.getListView().getItemAtPosition(id);
									date = dateformat.parse(element.get("date"));
									new VerseTask().execute();
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
				})
				.create();
			
			// add header
			TextView addNote = new TextView(lichtstrahlen.this);
			addNote.setText(getString(R.string.noteAdd));
			addNote.setPadding(5, 10, 5, 10);
			selection.getListView().addHeaderView(addNote);
			
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
			// create list dialog
			selection = new AlertDialog.Builder(lichtstrahlen.this).setCancelable(true)
				.setTitle(getString(R.string.menuList))
				.setAdapter(
					new VerseListAdapter(
						lichtstrahlen.this,
						result,
						R.layout.list,
						new String[] {"verse", "date"},
						new int[] {R.id.listVerse, R.id.listDate}),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							try {
								HashMap<String, String> element = (HashMap<String, String>) selection.getListView().getItemAtPosition(id);
								date = dateformat.parse(element.get("date"));
								new VerseTask().execute();
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				)
				.create();
			selection.show();
			
    		// hide progress dialog
    		progress.dismiss();
		}
	}
}
