package com.escoand.android.lichtstrahlen;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.escoand.android.lichtstrahlen_2013.R;

public class Preferences extends PreferenceActivity {

	@Override
	public void onContentChanged() {

//		/* remind */
//	case R.id.menuRemind:
//		PendingIntent recv = PendingIntent.getBroadcast(
//				getApplicationContext(), 0, reminder,
//				PendingIntent.FLAG_NO_CREATE);
//		if (recv == null)
//			showDialog(DIALOG_REMIND_ID);
//		else
//			recv.cancel();
//		return true;
		
		super.onContentChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		View item = null;
//
//		/* reminder */
//		item = menu.findItem(R.id.menuRemind);
//		if (item != null) {
//			int hour = getSharedPreferences(getString(R.string.app_name),
//					Context.MODE_PRIVATE).getInt("remind_hour", 9);
//			int minute = getSharedPreferences(getString(R.string.app_name),
//					Context.MODE_PRIVATE).getInt("remind_minute", 0);
//			item.setTitle(getString(R.string.menuRemind)
//					+ String.format(" (%02d:%02d)", hour, minute));
//			item.setChecked(PendingIntent.getBroadcast(getApplicationContext(),
//					0, reminder, PendingIntent.FLAG_NO_CREATE) != null);
//		}
//
//		/* inverse */
//		item = menu.findItem(R.id.menuInverse);
//		if (item != null) {
//			item.setChecked(getSharedPreferences(getString(R.string.app_name),
//					Context.MODE_PRIVATE).getBoolean("inverse", false));
//		}

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
