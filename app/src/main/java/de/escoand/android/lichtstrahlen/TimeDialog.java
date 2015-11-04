/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.escoand.android.lichtstrahlen;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TimePicker;

public class TimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private static SharedPreferences prefs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int hour = prefs.getInt(Preferences.PREF_REMIND_HOUR, 9);
        int minute = prefs.getInt(Preferences.PREF_REMIND_MINUTE, 0);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                android.text.format.DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        prefs.edit().putBoolean(Preferences.PREF_REMIND, false).commit();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        prefs.edit()
                .putBoolean(Preferences.PREF_REMIND, true)
                .putInt(Preferences.PREF_REMIND_HOUR, hourOfDay)
                .putInt(Preferences.PREF_REMIND_MINUTE, minute)
                .commit();
        Intent intent = new Intent(getActivity(), Receiver.class);
        intent.setAction("de.escoand.android.lichtstrahlen.INIT_REMINDER");
        getActivity().sendBroadcast(intent);
    }
}
