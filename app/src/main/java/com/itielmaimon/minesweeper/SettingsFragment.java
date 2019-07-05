package com.itielmaimon.minesweeper;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        bindBooleanPreferenceSummaryToValue(findPreference(getString(R.string.night_mode_pref_key)));
        bindBooleanPreferenceSummaryToValue(findPreference(getString(R.string.vibration_pref_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.language_pref_key)));
        findPreference(getString(R.string.night_mode_pref_key)).setOnPreferenceChangeListener(sPreferenceListener);
        findPreference(getString(R.string.vibration_pref_key)).setOnPreferenceChangeListener(sPreferenceListener);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void bindBooleanPreferenceSummaryToValue(Preference preference) {

        // Trigger the listener immediately with the preference's
        // current value.
        if (PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false))
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                preference.getContext().getString(R.string.on));
        else
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    preference.getContext().getString(R.string.off));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private Preference.OnPreferenceChangeListener sPreferenceListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            getActivity().finish();
            startActivity(getActivity().getIntent());
            getActivity().overridePendingTransition(0, 0);
            return true;
        }
    };
}