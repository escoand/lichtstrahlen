package com.escoand.android.lichtstrahlen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;

/**
 * Implements a general way of dealing with notes for dates.
 * Save the notes as a serialized HashMap.
 * @author escoand
 */
public class Notes extends Object {
	private Context context;
	private HashMap<Date, String> notes = new HashMap<Date, String>();
	
	/**
	 * Constructs a new empty <code>Notes</code> instance.<br/>
	 * Reads the notes from last session.
	 * @param context the context.
	 */
	public Notes(Context context) {
		super();
		
		// set context
		this.context = context;

		// read notes
		_load();
	}
	
	
	private Date _normalize(Date date) {
		return new Date(date.getYear(), date.getMonth(), date.getDate());
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
	 * Sets a note for the specific date.<br/>
	 * Saves the whole <code>Notes</code> object.
	 * @param date the date
	 * @param note the note
	 */
	public void add(Date date, String note) {
		notes.put(_normalize(date), note);
		
		_save();
	}
	
	/**
	 * Returns whether a note exists for the specified date.
	 * @param date the date to search for.
	 * @return true if a note exists for the specified date, false otherwise.
	 */
	public boolean exist(Date date) {
		return notes.containsKey(_normalize(date));
	}
	
	/**
	 * Returns the note for the specified date.
	 * @param date the date
	 * @return the note for the specified date, or null if no note for the specified date is found.
	 */
	public String get(Date date) {
		return notes.get(_normalize(date)).toString();
	}
	
	/**
	 * Removes the note for the specified key.<br/>
	 * Saves the whole <code>Notes</code> object.
	 * @param date the date
	 */
	public void remove(Date date) {
		notes.remove(_normalize(date));
		
		_save();
	}
}
