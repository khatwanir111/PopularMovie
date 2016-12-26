package com.irk.android.popularmovies;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by Rajesh on 07-Dec-16.
 */

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_sort_by)));

    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        // For list preferences, look up the correct display value in
        // the preference's 'entries' list (since they have separate labels/values).
        ListPreference listPreference = (ListPreference) preference;
        int prefIndex = listPreference.findIndexOfValue(stringValue);
        if (prefIndex >= 0) {
            preference.setSummary(listPreference.getEntries()[prefIndex]);
        }
        return true;
    }
}
