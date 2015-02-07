package de.escoand.android.lichtstrahlen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.escoand.android.lichtstrahlen_2015.R;

public class AboutDialog extends DialogFragment {

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View view = inflater.inflate(R.layout.about, null);

		((TextView) view.findViewById(R.id.txtAbout)).setText(
				Html.fromHtml(getString(R.string.about)),
				TextView.BufferType.SPANNABLE);
		((TextView) view.findViewById(R.id.txtAbout))
				.setMovementMethod(LinkMovementMethod.getInstance());

		dialog.setView(view);

		return dialog.create();
	}
}