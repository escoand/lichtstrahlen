package com.escoand.android.lichtstrahlen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;

/**
 * @author aschoenf
 *
 */
public class Notes extends Object {
	private Context context;
	private HashMap<Date, String> notes = new HashMap<Date, String>();
	
	// constructor
	public Notes(Context context) {
		super();
		
		// set context
		this.context = context;

		// read notes
		_load();
	}
	
	
	private void _load() {
		try {
			ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(context.openFileInput("notes.ser")));
			notes = (HashMap<Date, String>) stream.readObject();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void _save() {
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(context.openFileOutput("notes.ser", Context.MODE_PRIVATE)));
			stream.writeObject(notes);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Add new note
	 * @param date
	 * @param note
	 */
	public void add(Date date, String note) {
		Date tmp = new Date(date.getYear(), date.getMonth(), date.getDate());
		
		notes.put(tmp, note);
		
		_save();
	}
	
	/**
	 * Check if note exist for date
	 * @param date
	 * @return if note for date exists
	 */
	public boolean exist(Date date) {
		Date tmp = new Date(date.getYear(), date.getMonth(), date.getDate());
		
		return notes.containsKey(tmp);
	}
	
	/**
	 * Get note for date
	 * @param date
	 * @return note
	 */
	public String get(Date date) {
		Date tmp = new Date(date.getYear(), date.getMonth(), date.getDate());
		
		return notes.get(tmp).toString();
	}
	
	/**
	 * Remove note
	 * @param date
	 */
	public void remove(Date date) {
		Date tmp = new Date(date.getYear(), date.getMonth(), date.getDate());
		
		notes.remove(tmp);
		
		_save();
	}
}
