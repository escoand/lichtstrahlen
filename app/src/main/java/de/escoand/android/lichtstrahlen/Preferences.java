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

import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.escoand.android.lichtstrahlen_2016.R;

public class Preferences extends PreferenceActivity {
    static final String PREF_REMIND = "remind";
    static final String PREF_REMIND_HOUR = "remind_hour";
    static final String PREF_REMIND_MINUTE = "remind_minute";

    private static final int DIALOG_REMIND_ID = 3;

    private static SharedPreferences prefs;

    OnPreferenceChangeListener changed = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(
                            getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
    };

    OnPreferenceChangeListener updateWidget = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Intent intent = new Intent(getApplicationContext(), Widget.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(getApplicationContext())
                    .getAppWidgetIds(
                            new ComponentName(getApplication(), Widget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		/* restart after changed */
        findPreference("inverse").setOnPreferenceChangeListener(changed);
        findPreference("scale").setOnPreferenceChangeListener(changed);

		/* update widget after changed */
        findPreference("widgetInverse").setOnPreferenceChangeListener(
                updateWidget);

		/* reminder */
        findPreference(PREF_REMIND).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        DialogFragment newFragment = new TimeDialog();
                        newFragment.show(getFragmentManager(), "timePicker");
                        return false;
                    }
                });
        findPreference(PREF_REMIND).setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        return false;
                    }
                });
    }
}
