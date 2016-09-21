package jp.tmhouse.android.elgoog.elgoog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.webkit.WebView;

/**
 * Created by mutoh on 16/9/20.
 */
public class SettingsActivity extends PreferenceActivity {
    private Prefs m_prefs;
    private SharedPreferences   m_sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        m_sp = PreferenceManager.getDefaultSharedPreferences(this);
        m_prefs = new Prefs(this);
        setupSummary();

        m_sp.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(
                            SharedPreferences sharedPreferences, String key) {
                        setupSummary();
                    }
                }
        );
    }

    private void setupSummary() {
        ListPreference lp = (ListPreference)
                getPreferenceScreen().findPreference(
                        getString(R.string.userAgentListKey));
        lp.setSummary(m_prefs.getUserAgent());
    }
}
