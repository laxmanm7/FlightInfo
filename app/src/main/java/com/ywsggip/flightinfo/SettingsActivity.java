package com.ywsggip.flightinfo;



import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_PREF_CURRENCY = "pref_currency";

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            //init summary value
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.pref_key_currency));

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(KEY_PREF_CURRENCY))
                {
//                    Preference currencyPref = findPreference(key);
//                    currencyPref.setSummary(sharedPreferences.getString(key,""));

                    ListPreference listPreference = (ListPreference) findPreference(key);
                    int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key,""));
                    if(prefIndex >= 0) {
                        listPreference.setSummary(listPreference.getEntry());
                    }
                }
        }


        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}