package de.escoand.android.lichtstrahlen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class DayScriptureDialog extends DialogFragment {
	ScriptureSelectListener listener;
	String[] items;

	public DayScriptureDialog(String[] items) {
		this.items = items;
	}

	@Override
	public void onAttach(Activity activity) {
		listener = (ScriptureSelectListener) activity;
		super.onAttach(activity);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

		dialog.setCancelable(true)

		/* data for list */
		.setItems(items,

		/* listener */
		new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onScriptureSelect(items[which]);
			}
		});

		return dialog.create();
	}
}
